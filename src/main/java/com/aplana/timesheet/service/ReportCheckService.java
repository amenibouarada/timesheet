package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aplana.timesheet.util.DateTimeUtil.dateListToStringList;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = DataAccessException.class)
public class ReportCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ReportCheckService.class);

    private StringBuffer trace = new StringBuffer();

    @Autowired
    private ReportCheckDAO reportCheckDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private HolidayDAO holidayDAO;

    @Autowired
    private TimeSheetDAO timeSheetDAO;

    /**
     * Метод формирования оповещений используемый в таймере
     */
    public void storeReportCheck() {
        logger.info("Starts forming alerts of arrears: " + new Date());

        trace.setLength(0);

        trace.append("Start send mails\n");

        String currentDay = DateTimeUtil.currentDay();

        // Выполняем проверки только по рабочим дням
        if (holidayDAO.isWorkDay(currentDay)) {
            String firstPrevMonthDay = DateTimeUtil.previousMonthFirstDay();
            String lastCurrentMonthSunday = DateTimeUtil.lastSunday();

            String lastDay = DateTimeUtil.decreaseDay(currentDay);
            logger.info("Alerts of arrears: storeReportCheck starting");
            storeReportCheck(firstPrevMonthDay, lastDay, lastCurrentMonthSunday.equals(currentDay));
        }

        trace.append("Finish send mails\n");

        logger.info("Forming alerts of arrears finished: " + new Date());
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

        List<ReportCheck> reportCheckList = new ArrayList<ReportCheck>();
        List<Employee> employeeList = employeeService.getEmployees(division, false);
        String currentDay = DateTimeUtil.currentDay();

        Date firstDate = DateTimeUtil.parseStringToDateForDB(firstDay);
        Date lastDate = DateTimeUtil.parseStringToDateForDB(lastDay);

        for (Employee emp : employeeList) {
            logger.info("Employee {}", emp.getName());
            List<Date> dateList = timeSheetDAO.getOverdueTimesheet(emp.getId().longValue(), firstDate, lastDate, false);

            if (dateList.size() > 0) {
                List<String> dayList = dateListToStringList(dateList);
                ReportCheck reportCheck = new ReportCheck();
                reportCheck.setCheckDate(currentDay);
                reportCheck.setEmployee(emp);
                reportCheck.setDivision(division);
                reportCheck.setReportsNotSendNumber(dateList.size());
                reportCheck.setSundayCheck(sundayCheck);
                reportCheck.setPassedDays(dayList);
                reportCheckList.add(reportCheck);
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
            if (workDay.getCalDate().equals(DateTimeUtil.stringToTimestamp(currentDay, DateTimeUtil.DB_DATE_PATTERN)))
                sendMailService.performEndMonthMailing(reportCheckList);
        } else {
            logger.info("Reportchecks not found, all timesheets are filled");
        }
    }

    public String getTrace() {
        return trace.toString();
    }

}