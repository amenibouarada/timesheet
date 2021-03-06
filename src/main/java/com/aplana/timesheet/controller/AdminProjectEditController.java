package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ProjectFundingTypeEnum;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.AdminProjectBillableForm;
import com.aplana.timesheet.form.AdminProjectForm;
import com.aplana.timesheet.form.AdminProjectManagerForm;
import com.aplana.timesheet.form.AdminProjectTaskForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.*;

/**
 * @author dsysterov
 * @version 1.0
 */
@Controller
public class AdminProjectEditController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProjectsController.class);

    @Autowired
    AdminProjectEditService adminProjectEditService;
    @Autowired
    ProjectService projectService;
    @Autowired
    DivisionService divisionService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    ProjectTaskService projectTaskService;
    @Autowired
    ProjectManagerService projectManagerService;
    @Autowired
    EmployeeProjectBillableService employeeProjectBillableService;
    @Autowired
    ProjectRoleService projectRoleService;
    @Autowired
    DictionaryItemService dictionaryItemService;

    private ModelAndView getCommonMAV(final AdminProjectForm form){
        ModelAndView modelAndView = new ModelAndView("adminProjectEdit");
        modelAndView.addObject("divisionsList", divisionService.getAllDivisions());
        modelAndView.addObject("projectStateTypes", Arrays.asList(TypesOfActivityEnum.PROJECT, TypesOfActivityEnum.PRESALE));
        modelAndView.addObject("projectFundingTypes", ProjectFundingTypeEnum.values());
        modelAndView.addObject("divisionsEmployeesJSON", employeeService.getDivisionsEmployeesJSON());
        modelAndView.addObject("employeesListJSON", employeeService.getAllEmployeesJSON());
        modelAndView.addObject("employeesList", employeeService.getAllEmployees());
        modelAndView.addObject("projectRoleTypes", ProjectRolesEnum.values());
        modelAndView.addObject("projectRoleTypesJSON",
                projectRoleService.getProjectRoleListJson(projectRoleService.getProjectRoles()));
        modelAndView.addObject("projectform", form);
        return modelAndView;
    }

    @RequestMapping(value = "/admin/projects/add", method = RequestMethod.GET)
     public ModelAndView showAddForm(
            @RequestParam(value = "divisionId", required = false, defaultValue = "-1") Integer divisionId
    ) {
        final AdminProjectForm form = new AdminProjectForm();
        form.setDivision(divisionId);
        ModelAndView modelAndView = getCommonMAV(form);
        modelAndView.addObject("pageFunction", "add");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/projects/edit", method = RequestMethod.GET)
    public ModelAndView showEditForm(
            @RequestParam(value = "projectId", required = true) Integer projectId
    ) {
        Project project = projectService.find(projectId);
        final AdminProjectForm form = adminProjectEditService.getAdminProjectForm(project);
        ModelAndView modelAndView = getCommonMAV(form);
        modelAndView.addObject("managerId", project.getManager().getId());
        modelAndView.addObject("pageFunction", "edit");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/projects/save", method = RequestMethod.POST)
    public String saveProject(
            @ModelAttribute("projectform") AdminProjectForm form
    ) {
        adminProjectEditService.saveProject(form);
        return "redirect:/admin/projects";
    }
}