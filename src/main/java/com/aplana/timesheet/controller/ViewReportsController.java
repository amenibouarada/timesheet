package com.aplana.timesheet.controller;

import com.aplana.timesheet.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.ViewReportsForm;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.form.validator.ViewReportsFormValidator;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.TimeSheetService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

@Controller
public class ViewReportsController extends AbstractControllerForEmployeeWithYears {

    @Autowired
    ViewReportsFormValidator tsFormValidator;
    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    private CalendarService calendarService;

    @RequestMapping(value = "/viewreports", method = RequestMethod.GET)
    public String sendViewReports() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("redirect:/viewreports/%s/%s/%s/%s", securityService.getSecurityPrincipal().getEmployee().getDivision().getId(), securityService.getSecurityPrincipal().getEmployee().getId(), calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1);
    }

    @RequestMapping(value = "/viewreports/{divisionId}/{employeeId}/{year}/{month}")
    public ModelAndView showDates(
            @PathVariable("divisionId") Integer divisionId, 
            @PathVariable("employeeId") Integer employeeId, 
            @PathVariable("year") Integer year, 
            @PathVariable("month") Integer month, 
            @ModelAttribute("viewReportsForm") ViewReportsForm tsForm, 
            BindingResult result
    ) {
        logger.info("year {}, month {}", year, month);
        tsFormValidator.validate(tsForm, result);

        ModelAndView mav = createModelAndViewForEmployee("viewreports", employeeId, divisionId);

        Employee employee = (Employee) mav.getModel().get(EMPLOYEE);

        mav.addObject("year", year);
        mav.addObject("month", month);
        mav.addObject("monthList", DateTimeUtil.getMonthListJson((List<Calendar>) mav.getModel().get(YEARS_LIST), calendarService));
        List<DayTimeSheet> dayTimeSheets = timeSheetService.findDatesAndReportsForEmployee(employee, year, month);
        mav.addObject("reports", dayTimeSheets);
        BigDecimal durationFact = BigDecimal.ZERO;
        for (Iterator<DayTimeSheet> iterator = dayTimeSheets.iterator(); iterator.hasNext(); ) {
            DayTimeSheet next = iterator.next();

            durationFact = durationFact.add(next.getDuration());
        }
        durationFact.setScale(1);
        mav.addObject("durationFact", durationFact.doubleValue());
        mav.addObject(
                "durationPlan",
                (calendarService.getWorkDaysCountForRegion(
                        employee.getRegion(),
                        year,
                        month
                ) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate())
        );

        return mav;
    }

}