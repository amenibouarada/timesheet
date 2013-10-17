package com.aplana.timesheet.controller.quickreport.generators;

import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.controller.quickreport.IllnessesQuickReport;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 21.01.13
 */
@Component
@Qualifier(value = "illnessesReportGenerator")
public class IllnessesQuickReportGenerator extends AbstractQuickReportGenerator<IllnessesQuickReport, Illness> {

    @Override
    protected IllnessesQuickReport createQuickReport() {
        return new IllnessesQuickReport();
    }

    @Override
    protected List<Illness> getAllPeriodicals(Employee employee) {
        return illnessService.getEmployeeIllness(employee);
    }

    @Override
    protected IllnessesQuickReport addDifferentPartOfPeriodicleToMounthStatistics(IllnessesQuickReport report, Illness periodical) {
        return addWorkDaysOnIllnessesWorkedToMounthStatistics(report, periodical);
    }

    private IllnessesQuickReport addWorkDaysOnIllnessesWorkedToMounthStatistics(IllnessesQuickReport illnessesQuickReport, Illness illness) {
        double workDaysOnIllnessWorked =
                employeeService.getWorkDaysOnIllnessWorked(
                        illness.getEmployee(), illness.getBeginDate(), illness.getEndDate()
                ) / TimeSheetConstants.WORK_DAY_DURATION;
        illness.setWorkDaysOnIllnessWorked(workDaysOnIllnessWorked);
        illnessesQuickReport.setMounthWorkDaysOnIllnessWorked(illnessesQuickReport.getMounthWorkDaysOnIllnessWorked() + workDaysOnIllnessWorked);

        return illnessesQuickReport;
    }

    @Override
    protected IllnessesQuickReport addPeriodicalToYearStatistics(IllnessesQuickReport report, Illness periodical, Date yearBeginDate, Date yearEndDate) {
        if (periodicalIsFullyInPeriod(yearBeginDate, yearEndDate, periodical)) {
            report = addIllnessToYearStatistics(report, periodical);
        }

        return report;
    }

    private IllnessesQuickReport addIllnessToYearStatistics(IllnessesQuickReport report, Illness illness) {
        Long calendarDaysCount = DateTimeUtil.getAllDaysCount(illness.getBeginDate(), illness.getEndDate());
        Integer holidaysCount = calendarService.getHolidaysCountForRegion(illness.getBeginDate(), illness.getEndDate(),
                illness.getEmployee().getRegion());
        Long workingDays = calendarDaysCount - holidaysCount;
        double workDaysOnIllnessWorked =
                employeeService.getWorkDaysOnIllnessWorked(
                        illness.getEmployee(), illness.getBeginDate(), illness.getEndDate()
                ) / TimeSheetConstants.WORK_DAY_DURATION;
        report.setYearWorkDaysOnIllness(report.getYearWorkDaysOnIllness() + workingDays);
        report.setYearWorkDaysOnIllnessWorked(report.getYearWorkDaysOnIllnessWorked() + workDaysOnIllnessWorked);

        return report;
    }

    @Override
    protected IllnessesQuickReport addPartOfPeriodicleToYearStatistics(IllnessesQuickReport report, Illness periodical, Date beginDate, Date endDate) throws CloneNotSupportedException {
        Illness illness = periodical;
        Illness partOfIllness = getPeriodicalWithFakeDates(illness, beginDate, endDate);

        return addIllnessToYearStatistics(report, partOfIllness);
    }

    @Override
    protected IllnessesQuickReport addMounthStatisticsToYearStatistics(IllnessesQuickReport report) {
        //чтобы данные за месяц попали в годовую статистику
        report.setYearWorkDaysOnIllness(report.getYearWorkDaysOnIllness() + report.getMounthWorkDays());
        report.setYearWorkDaysOnIllnessWorked(report.getYearWorkDaysOnIllnessWorked() + report.getMounthWorkDaysOnIllnessWorked());
        return report;
    }

}
