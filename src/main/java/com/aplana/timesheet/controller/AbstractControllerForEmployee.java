package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
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
    protected SecurityService securityService;
    @Autowired
    protected CalendarService calendarService;
    @Autowired
    protected RegionService regionService;
    @Autowired
    protected ProjectRoleService projectRoleService;

    protected static final String YEARS_LIST = "yearsList";

    public Employee getCurrentUser(){
        return session.getAttribute("employeeId") != null
                ? employeeService.find((Integer)session.getAttribute("employeeId"))
                : securityService.getSecurityPrincipal().getEmployee();
    }

    protected ModelAndView getCommonModelAndView(String viewName, Integer employeeId, Integer divisionId) {
        List<Division> divisionList = divisionService.getDivisions();
        final ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject("divisionList", divisionList);
        modelAndView.addObject("managerListJson", employeeService.getManagerListJsonNew());
        modelAndView.addObject("fullProjectListJsonWithDivisionId", projectService.getProjectListByDivisionsJson(divisionList, true));

        return modelAndView;
    }

    protected ModelAndView createMAVForEmployeeWithDivision(String viewName, Integer employeeId) {
        final ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject("divisionList", divisionService.getDivisions());
        modelAndView.addObject(EMPLOYEE, employeeService.find(employeeId));
        modelAndView.addObject(YEARS_LIST, DateTimeUtil.getYearsList(calendarService));
        modelAndView.addObject(AddEmployeeForm.ADD_FORM, new AddEmployeeForm());
        return modelAndView;
    }

    protected ModelAndView fillMavForAddEmployeesForm(ModelAndView modelAndView){
        modelAndView.addObject("divisionList", divisionService.getDivisions());
        modelAndView.addObject("projectTypeList",
                Arrays.asList(TypesOfActivityEnum.PROJECT, TypesOfActivityEnum.PRESALE, TypesOfActivityEnum.NON_PROJECT));
        modelAndView.addObject("projectListWithOwnerDivision", projectService.getProjectListWithOwnerDivisionJson());
        modelAndView.addObject("projectRoleList", projectRoleService.getProjectRoles());
        modelAndView.addObject("regionList", regionService.getRegions());
        modelAndView.addObject("managerList", employeeService.getManagerListJson());
        modelAndView.addObject("managerMapJson", employeeService.getManagerListForAllEmployee());
        modelAndView.addObject("all", TimeSheetConstants.ALL_VALUES);
        return modelAndView;
    }
}
