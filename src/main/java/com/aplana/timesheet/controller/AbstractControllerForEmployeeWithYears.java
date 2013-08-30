package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractControllerForEmployeeWithYears extends AbstractControllerForEmployee {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractControllerForEmployeeWithYears.class);

    protected static final String YEARS_LIST = "yearsList";

    @Autowired
    protected CalendarService calendarService;

    @Override
    protected final ModelAndView createModelAndViewForEmployee(String viewName, Integer employeeId, Integer divisionId) {
        final ModelAndView modelAndView = super.createModelAndViewForEmployee(viewName, employeeId, divisionId);

        List<Calendar> years = DateTimeUtil.getYearsList(calendarService);

        modelAndView.addObject(YEARS_LIST, years);

        return modelAndView;
    }

    @Override
    protected final ModelAndView createMAVForEmployeeWithDivision(String viewName, Integer employeeId, Integer divisionId) {
        final ModelAndView modelAndView = super.createMAVForEmployeeWithDivision(viewName, employeeId, divisionId);

        List<Calendar> years = DateTimeUtil.getYearsList(calendarService);

        modelAndView.addObject(YEARS_LIST, years);

        return modelAndView;
    }
}
