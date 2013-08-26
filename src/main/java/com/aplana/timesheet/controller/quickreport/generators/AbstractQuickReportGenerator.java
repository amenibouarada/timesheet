package com.aplana.timesheet.controller.quickreport.generators;

import com.aplana.timesheet.controller.quickreport.QuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReportGenerator;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Periodical;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessControllerException;
import com.aplana.timesheet.service.BusinessTripService;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.IllnessService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 21.01.13
 */
public abstract class AbstractQuickReportGenerator <T extends QuickReport, K extends Periodical>  implements QuickReportGenerator<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractQuickReportGenerator.class);

    public static final String QUICK_REPORT_GENARATE_ERROR_MESSAGE = "Ошибка при генерации отчета!";
    @Autowired
    BusinessTripService businessTripService;

    @Autowired
    IllnessService illnessService;

    @Autowired
    CalendarService calendarService;

    @Autowired
    EmployeeService employeeService;

    public final T generate(Employee employee, Date periodBeginDate, Date periodEndDate, Date yearBeginDate, Date yearEndDate) throws BusinessTripsAndIllnessControllerException {
        try{
            T report = createQuickReport();

            List<K> allPeriodicles = getAllPeriodicals(employee);

            for (K periodical : allPeriodicles){

                periodical = makePeriodicalCoversOnlyOneYear(periodical, yearBeginDate, yearEndDate);    //т.к. собираем статистику только по одному году - отрсеиваем ненужные периоды и отрезаем ненужные части нужных (которые входят в отчетный год частично)

                if (periodicalIsFullyInPeriod(periodBeginDate, periodEndDate, periodical)) {    //период попадает в отчетный месяц, не выходя за его границы
                    report = addCommonPartOfPeriodicleToMounthStatistics(report, periodical);
                    report = addDifferentPartOfPeriodicleToMounthStatistics(report, periodical);
                }
                else if (periodicalCrossesTheBeginningOfTheMounth(periodBeginDate, periodical)) {   //период попадает на отчетный месяц и на предыдущие
                    report = addCommonPartOfPartOfPeriodicleToMounthStatistics(report, periodical, periodBeginDate, periodical.getEndDate());
                    report = addCommonPartOfPartOfPeriodicleToMounthStatistics(report, periodical, periodical.getBeginDate(), DateUtils.addDays(periodBeginDate, -1));
                }
                else if (periodicalCrossesTheEndOfTheMounth(periodEndDate, periodical)) {     //период попадает на отчетный месяц и следующие за ним
                    report = addCommonPartOfPartOfPeriodicleToMounthStatistics(report, periodical, periodical.getBeginDate(), periodEndDate);
                    report = addCommonPartOfPartOfPeriodicleToMounthStatistics(report, periodical, DateUtils.addDays(periodEndDate, 1), periodical.getEndDate());
                }
                else if (periodicalCoversAllPeriod(periodBeginDate, periodEndDate, periodical)) { //период польностью включает в себя отчетный месяц и захватывает предыдущие и последующие месяца
                    report = addPartOfPeriodicleToYearStatistics(report, periodical, periodical.getBeginDate(), DateUtils.addDays(periodBeginDate, -1));
                    report = addCommonPartOfPartOfPeriodicleToMounthStatistics(report, periodical, periodBeginDate, periodEndDate);
                    report = addPartOfPeriodicleToYearStatistics(report, periodical, DateUtils.addDays(periodEndDate, 1), periodical.getEndDate());
                }
                else if (periodicalIsFullyInPeriod(yearBeginDate, yearEndDate, periodical)) {    //период попадает в отчетный год и не захватывает отчетный месяц
                    report = addPeriodicalToYearStatistics(report, periodical, yearBeginDate, yearEndDate);
                }
            }

            addMounthStatisticsToYearStatistics(report);


            return report;

        } catch (Exception e){
            logger.error(QUICK_REPORT_GENARATE_ERROR_MESSAGE, e);
            throw new BusinessTripsAndIllnessControllerException(QUICK_REPORT_GENARATE_ERROR_MESSAGE, e);
        }
    }

    protected abstract T addMounthStatisticsToYearStatistics(T report);

    protected abstract T addPartOfPeriodicleToYearStatistics(T report, K periodical, Date beginDate, Date date) throws CloneNotSupportedException;

    protected abstract T addPeriodicalToYearStatistics(T report, K periodical, Date yearBeginDate, Date yearEndDate);

    protected abstract T addDifferentPartOfPeriodicleToMounthStatistics(T report, K periodical);

    protected abstract T createQuickReport();

    protected abstract List<K> getAllPeriodicals(Employee employee);

    private T addCommonPartOfPartOfPeriodicleToMounthStatistics(T report, K periodical, Date beginDate, Date endDate) throws CloneNotSupportedException {
        K partOfPeriodical = getPeriodicalWithFakeDates(periodical, beginDate, endDate);
        report = addCommonPartOfPeriodicleToMounthStatistics(report, partOfPeriodical);
        replaseFakeDatesWithRealDates(partOfPeriodical, periodical.getBeginDate(), periodical.getEndDate());
        return report;
    }

    private T addCommonPartOfPeriodicleToMounthStatistics(T report, K periodical) {
        Long calendarDaysCount = DateTimeUtil.getAllDaysCount(periodical.getBeginDate(), periodical.getEndDate());
        Integer holidaysCount = calendarService.getHolidaysCountForRegion(periodical.getBeginDate(),
                periodical.getEndDate(), periodical.getEmployee().getRegion());
        Long workingDays = calendarDaysCount - holidaysCount;
        periodical.setWorkingDays(workingDays);
        periodical.setCalendarDays(calendarDaysCount);
        report.getPeriodicalsList().add(periodical);
        report.setMounthCalendarDays(report.getMounthCalendarDays() + calendarDaysCount);
        report.setMounthWorkDays(report.getMounthWorkDays() + workingDays);

        return report;
    }

    protected K makePeriodicalCoversOnlyOneYear(K periodical, Date yearBeginDate, Date yearEndDate) {
        if (periodical.getBeginDate().before(yearBeginDate) && periodical.getEndDate().after(yearBeginDate)){
            periodical.setBeginDate(yearBeginDate);
        }
        if (periodical.getBeginDate().before(yearEndDate) && periodical.getEndDate().after(yearEndDate)){
            periodical.setEndDate(yearEndDate);
        }

        return periodical;
    }

    protected boolean periodicalCoversAllPeriod(Date periodBeginDate, Date periodEndDate, K periodical) {
        return (periodical.getBeginDate().compareTo(periodBeginDate)<=0) && (periodical.getEndDate().compareTo(periodEndDate)>=0);
    }

    protected boolean periodicalCrossesTheEndOfTheMounth(Date periodEndDate, K periodical) {
        return (periodical.getBeginDate().compareTo(periodEndDate)<=0) && (periodical.getEndDate().compareTo(periodEndDate)>=0);
    }

    protected boolean periodicalCrossesTheBeginningOfTheMounth(Date periodBeginDate, K periodical) {
        return (periodical.getEndDate().compareTo(periodBeginDate)>=0) && (periodical.getBeginDate().compareTo(periodBeginDate)<=0);
    }

    protected boolean periodicalIsFullyInPeriod(Date periodBeginDate, Date periodEndDate, K periodical) {
        return (periodical.getBeginDate().after(periodBeginDate) || DateUtils.isSameDay(periodical.getBeginDate(), periodBeginDate)) &&
                (periodical.getEndDate().before(periodEndDate) || (DateUtils.isSameDay(periodical.getEndDate(), periodEndDate)));
    }

    protected K getPeriodicalWithFakeDates(K periodical, Date beginDate, Date endDate) throws CloneNotSupportedException {
        K partOfPeriodical = (K) periodical.clone();
        partOfPeriodical.setBeginDate(beginDate);
        partOfPeriodical.setEndDate(endDate);
        return partOfPeriodical;
    }

    protected void replaseFakeDatesWithRealDates(Periodical periodicalWithFakeDates, Date beginDate, Date endDate) {
        periodicalWithFakeDates.setBeginDate(beginDate);
        periodicalWithFakeDates.setEndDate(endDate);
    }

}
