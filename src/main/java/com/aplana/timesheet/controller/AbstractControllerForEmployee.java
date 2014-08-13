package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.service.helper.EmployeeHelper;
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
public abstract class AbstractControllerForEmployee extends AbstractController{

    protected static final Logger logger = LoggerFactory.getLogger(AbstractControllerForEmployee.class);

    protected static final String EMPLOYEE = "employee";

    @Autowired
    private ProjectService projectService;

    @Autowired
    protected EmployeeService employeeService;

    @Autowired
    protected DivisionService divisionService;

    @Autowired
    protected EmployeeHelper employeeHelper;

    @Autowired
    protected SecurityService securityService;

    @Autowired
    protected CalendarService calendarService;

    protected static final String YEARS_LIST = "yearsList";

    public Employee getCurrentUser(){
        return session.getAttribute("employeeId") != null
                ? employeeService.find((Integer)session.getAttribute("employeeId"))
                : securityService.getSecurityPrincipal().getEmployee();
    }

    private ModelAndView getCommonModelAndView(String viewName, Integer employeeId, Integer divisionId) {
        List<Division> divisionList = divisionService.getDivisions();
        final ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject("divisionList", divisionList);
        modelAndView.addObject("managerListJson", employeeHelper.getManagerListJson());
        modelAndView.addObject("fullProjectListJsonWithDivisionId", projectService.getProjectListJson(divisionList, true));

        return modelAndView;
    }


    protected ModelAndView createModelAndViewForEmployee(String viewName, Integer employeeId, Integer divisionId) {
        List<Division> divisionList = divisionService.getDivisions();
        final ModelAndView modelAndView = getCommonModelAndView(viewName, employeeId, divisionId);
        modelAndView.addObject(EMPLOYEE, employeeService.find(employeeId));
        modelAndView.addObject("employeeListJson",
                employeeHelper.getEmployeeListWithLastWorkdayJson(divisionList, employeeService.isShowAll(request)));

        return modelAndView;
    }

    protected ModelAndView createMAVForEmployeeWithDivisionAndManagerAndRegion(String viewName, Integer employeeId, Integer divisionId) {
        List<Division> divisionList = divisionService.getDivisions();
        final ModelAndView modelAndView = getCommonModelAndView(viewName, employeeId, divisionId);
        modelAndView.addObject("employeeListJson",
                employeeHelper.getEmployeeListWithDivisionAndManagerAndRegionJson(divisionList, employeeService.isShowAll(request)));

        return modelAndView;
    }

    protected ModelAndView createMAVForEmployeeWithDivision(String viewName, Integer employeeId, Integer divisionId) {
        List<Division> divisionList = divisionService.getDivisions();
        final ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject("divisionList", divisionService.getDivisions());
        modelAndView.addObject(EMPLOYEE, employeeService.find(employeeId));
        modelAndView.addObject("employeeListJson",
                employeeHelper.getEmployeeListWithDivisionJson(divisionList, employeeService.isShowAll(request)));

        return modelAndView;
    }

    protected final ModelAndView createMAVForEmployeeWithDivisionWithYears(String viewName, Integer employeeId, Integer divisionId) {
        final ModelAndView modelAndView = createMAVForEmployeeWithDivision(viewName, employeeId, divisionId);
        modelAndView.addObject(YEARS_LIST, DateTimeUtil.getYearsList(calendarService));
        return modelAndView;
    }
}
