package com.aplana.timesheet.service;

import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeProjectPlan;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.util.DateNumbers;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class CreatePlanForPeriodService {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Transactional
    public void createPlans(Project project, Employee employee, Date fromDate, Date toDate,
                            Byte percentOfCharge) {
        final Region region = employee.getRegion();
        final Calendar date = DateUtils.toCalendar(DateUtils.setDays(fromDate, 1));
        int workdaysCount;

        while (DateUtils.truncatedCompareTo(date.getTime(), toDate, Calendar.MONTH) <= 0) {
            final Date dateTime = date.getTime();
            final DateNumbers dateNumbers = new DateNumbers(dateTime);

            workdaysCount = calendarService.getWorkDaysCountForRegion(
                    region,
                    dateNumbers.getYear(),
                    dateNumbers.getDatabaseMonth(),
                    max(dateTime, fromDate),
                    min(DateUtils.setDays(dateTime, date.getActualMaximum(Calendar.DAY_OF_MONTH)), toDate)
            );

            employeeProjectPlanService.store(
                    createEmployeeProjectPlan(employee, project, workdaysCount, percentOfCharge, dateNumbers)
            );

            date.add(Calendar.MONTH, 1);
        }
    }

    private EmployeeProjectPlan createEmployeeProjectPlan(Employee employee, Project project, int workdaysCount,
                                                          Byte percentOfCharge, DateNumbers dateNumbers) {
        final int year = dateNumbers.getYear();
        final int month = dateNumbers.getDatabaseMonth();

        EmployeeProjectPlan employeeProjectPlan = employeeProjectPlanService.tryFind(employee, year, month, project);

        if (employeeProjectPlan == null) {
            employeeProjectPlan = new EmployeeProjectPlan();
        }

        employeeProjectPlan.setEmployee(employee);
        employeeProjectPlan.setProject(project);
        employeeProjectPlan.setYear(year);
        employeeProjectPlan.setMonth(month);
        employeeProjectPlan.setValue(workdaysCount * TimeSheetConstants.WORK_DAY_DURATION * percentOfCharge.doubleValue() / 100.0);

        return employeeProjectPlan;
    }

}
