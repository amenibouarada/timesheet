package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.BusinessTripDAO;
import com.aplana.timesheet.dao.HolidayDAO;
import com.aplana.timesheet.dao.RegionDAO;
import com.aplana.timesheet.dao.ReportCheckDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aplana.timesheet.util.DateTimeUtil.DATE_PATTERN;
import static com.aplana.timesheet.util.DateTimeUtil.stringToDate;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = DataAccessException.class)
public class ReportCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ReportCheckService.class);

    private StringBuffer trace = new StringBuffer();

    private Boolean reportForming = false;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private ReportCheckDAO reportCheckDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private HolidayDAO holidayDAO;

    @Autowired
    private IllnessService illnessService;

    @Autowired
    private VacationService vacationService;

    @Autowired
    private RegionDAO regionDAO;

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Autowired
    private BusinessTripDAO businessTripDAO;

    /**
     * Метод формирования оповещений используемый в таймере
     */
    public void storeReportCheck() {

        trace.setLength(0);

        trace.append("Start send mails\n");

        String currentDay = DateTimeUtil.currentDay();

        // Выполняем проверки только по рабочим дням
        if (holidayDAO.isWorkDay(currentDay)) {
            String firstDay = DateTimeUtil.previousMonthFirstDay(),
                    endMonthDay = DateTimeUtil.endMonthDay(new Timestamp(System.currentTimeMillis())),
                    endPrevMonthDay = DateTimeUtil.endPrevMonthDay(),
                    lastSunday = DateTimeUtil.lastSunday(),
                    lastDay = lastSunday;
            // Если конец месяца
            if (DateTimeUtil.dayAfterDay(endMonthDay, lastSunday))
                lastDay = currentDay;
            // Если новый месяц - надо взять последний день предыдущего месяца
            if (DateTimeUtil.dayAfterDay(endPrevMonthDay, lastSunday))
                lastDay = endPrevMonthDay;
            // lastDay никогда не должен быть сегодняшним днем. т.к. проверка идет ночью и сегодняшний день еще только начался
            if (lastDay.equals(currentDay))
                lastDay = DateTimeUtil.decreaseDay(lastDay);
            storeReportCheck(firstDay, lastDay, lastSunday.equals(currentDay));
        }

        trace.append("Finish send mails\n");

    }

    /**
     * Заносит в базу список проверки заполнения отчетов по опред-м подразделениям за определенные дни
     *
     * @param firstDay
     * @param lastDay
     * @param sundayCheck
     */
    public void storeReportCheck(
            String firstDay, String lastDay, boolean sundayCheck
    ) {
        String[] divisionsSendMail = getDivisionSendMail();
        //logger.info("divisionlist is {}", mailConfig.getProperty("mail.divisions"));

        for (String divisionId : divisionsSendMail) {
            logger.info("division id is {}", Integer.parseInt(divisionId));
            storeReportCheck(divisionService.find(Integer.parseInt(divisionId)), firstDay, lastDay, sundayCheck);
        }
    }

    private String[] getDivisionSendMail() {
        List<Division> divisions = divisionService.getDivisionCheck();
        List<String> result = new ArrayList<String>();
        for (Division division : divisions) {
            result.add(division.getId().toString());
        }
        return result.toArray(new String[0]);
    }

    public boolean contain(List<BusinessTrip> businessTripList, Date date){
        for(BusinessTrip businessTrip : businessTripList){
            if (businessTrip.contain(date)){
                return true;
            }
        }
        return false;
    }

    /**
     * Заносит в базу список проверки заполнения отчетов по подразделению за определенные дни
     *
     * @param division
     * @param firstDay
     * @param lastDay
     * @param sundayCheck
     */
    @Transactional  // Траблы с LAZY. Пусть весь метод выполняется в одной транзакции
    public void storeReportCheck(Division division, String firstDay, String lastDay, boolean sundayCheck) {
        logger.info("storeReportcheck() for division {} entered", division.getId());
        Integer reportsNotSendNumber;
        List<ReportCheck> reportCheckList = new ArrayList<ReportCheck>();
        List<Employee> employeeList = employeeService.getEmployees(division, false);
        List<String> dayList = DateTimeUtil.splitDateRangeOnDays(firstDay, lastDay);

        Date firstDate = null;
        Date lastDate = null;
        try {
            firstDate = sdf.parse(firstDay);
            lastDate = sdf.parse(lastDay);
        } catch (ParseException e) {
            new RuntimeException(e);
        }

        String currentDay = DateTimeUtil.currentDay();
        for (Employee emp : employeeList) {
            logger.info("Employee {}", emp.getName());
            // если сотрудник работает и не начальник подразделения

            if (!emp.isDisabled(null) && emp.getManager() != null) {
                reportsNotSendNumber = 0;
                List<String> passedDays = new ArrayList<String>();
                List<BusinessTrip> businessTripList = businessTripDAO.getEmployeeBusinessTripsIntersectionDates(emp, firstDate, lastDate);
                for (String day : dayList) {
                    Date date = null;
                    try {
                        date = sdf.parse(day);
                    } catch (ParseException e) {
                        new RuntimeException(e);
                    }

                    Calendar calendar = calendarService.find(day);
                    //если рабочий день
                    if (holidayDAO.isWorkDay(day, emp.getRegion()) || contain(businessTripList, date)) {
                        //если день после устройства на работу включительно
                        if ( ! calendar.getCalDate().before( emp.getStartDate() ) ) {
                            //если сотрудник не списал рабочее время за этот день
                            if (timeSheetService.findForDateAndEmployee(day, emp.getId()) == null &&
                                !vacationService.isDayVacation(emp, stringToDate(day, DATE_PATTERN)) && // не был в отпуске
                                !illnessService.isDayIllness(emp, stringToDate(day, DATE_PATTERN)))     // и не болел
                            {
                                reportsNotSendNumber++;
                                logger.info("Passed day added");
                                passedDays.add(day);
                            }
                        }
                    }
                }
                // В reportCheckList попадают только те у кого есть долги
                if (reportsNotSendNumber > 0) {
                    ReportCheck reportCheck = new ReportCheck();
                    reportCheck.setCheckDate(currentDay);
                    reportCheck.setEmployee(emp);
                    reportCheck.setDivision(division);
                    reportCheck.setReportsNotSendNumber(reportsNotSendNumber);
                    reportCheck.setSundayCheck(sundayCheck);
                    reportCheck.setPassedDays(passedDays);
                    reportCheckList.add(reportCheck);
                }

            }
        }
        if (reportCheckList.size() > 0) {
            reportCheckDAO.setReportChecks(reportCheckList);

            Calendar currentCalendar = calendarService.find(currentDay);

            logger.info("Reportcheck object for division {} ({}) saved.", division.getId(), currentCalendar.getCalDate());

            sendMailService.performPersonalAlertMailing(reportCheckList);
            sendMailService.performManagerMailing(reportCheckList);

            // Если последний рабочий день месяца (по стране, без учета регионов)- рассылаем напоминания о заполнении отчетов для всех у кого нет долгов по отчетности
            Calendar workDay = calendarService.getLastWorkDay(currentCalendar);
            if (workDay.getCalDate().equals(DateTimeUtil.stringToTimestamp(currentDay, DateTimeUtil.DATE_PATTERN)))
                sendMailService.performEndMonthMailing(reportCheckList);
        } else
            logger.info("Reportchecks not found, all timesheets are filled");
    }

    public String getTrace() {
        return trace.toString();
    }

}