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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dsysterov
 * @version 1.0
 */
@Controller
public class AdminProjectEditController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProjectsController.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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


    @RequestMapping(value = "/admin/projects/add", method = RequestMethod.GET)
     public ModelAndView showAddForm(
            @RequestParam(value = "divisionId", required = false, defaultValue = "-1") Integer divisionId,
            @RequestParam(value = "managerId", required = false, defaultValue = "-1") Integer managerId
    ) {
        ModelAndView modelAndView = new ModelAndView("adminProjectEdit");
        modelAndView.addObject("divisionsList", divisionService.getAllDivisions());
        modelAndView.addObject("projectStateTypes", TypesOfActivityEnum.values());
        modelAndView.addObject("projectFundingTypes", ProjectFundingTypeEnum.values());
        modelAndView.addObject("divisionsEmployeesJSON", employeeService.getDivisionsEmployeesJSON(new Date()));

        AdminProjectForm form = new AdminProjectForm();
        form.setDivision(divisionId);

        modelAndView.addObject("projectform", form);

        return modelAndView;
    }

    @RequestMapping(value = "/admin/projects/edit", method = RequestMethod.GET)
    public ModelAndView showEditForm(
            @RequestParam(value = "projectId", required = true) Integer projectId
    ) {
        ModelAndView modelAndView = new ModelAndView("adminProjectEdit");
        modelAndView.addObject("divisionsList", divisionService.getAllDivisions());
        modelAndView.addObject("projectStateTypes", TypesOfActivityEnum.values());
        modelAndView.addObject("projectFundingTypes", ProjectFundingTypeEnum.values());
        modelAndView.addObject("divisionsEmployeesJSON", employeeService.getDivisionsEmployeesJSON(new Date()));
        modelAndView.addObject("employeesListJSON", employeeService.getAllEmployeesJSON());
        modelAndView.addObject("employeesList", employeeService.getAllEmployees());
        modelAndView.addObject("projectRoleTypes", ProjectRolesEnum.values());
        modelAndView.addObject("projectRoleTypesJSON", projectRoleService.getProjectRoleListJson(projectRoleService.getProjectRoles()));

        Project project = projectService.find(projectId);

        AdminProjectForm form = new AdminProjectForm();
        form.setId(project.getId());
        form.setName(project.getName());

        Division projectDivision = project.getDivision();
        form.setDivision((projectDivision != null)? projectDivision.getId() : 0);

        form.setManager(project.getManager().getId());
        modelAndView.addObject("managerId", project.getManager().getId());
        form.setManagerDivision(project.getManager().getDivision().getId());

        form.setCustomer(project.getCustomer());
        form.setStartDate(dateFormat.format(project.getStartDate()));
        form.setEndDate(dateFormat.format(project.getEndDate()));
        form.setState(project.getState().getId());
        form.setFundingType(project.getFundingType().getId());
        form.setJiraKey(project.getJiraProjectKey());
        form.setActive(project.isActive());
        form.setCqRequired(project.isCqRequired());

        ArrayList<Integer> formDivisions = new ArrayList<Integer>();
        for (Division division : project.getDivisions()) {
            formDivisions.add(division.getId());
        }
        form.setProjectDivisions(formDivisions);

        form.setPassport(project.getPassport());

        List<ProjectTask> projectTasks = projectTaskService.findAllByProject(project);
        List<AdminProjectTaskForm> projectTaskForms = new ArrayList<AdminProjectTaskForm>();
        for (ProjectTask projectTask : projectTasks) {
            AdminProjectTaskForm task = new AdminProjectTaskForm();
            task.setId(projectTask.getId());
            task.setName(projectTask.getTaskName());
            task.setDescription(projectTask.getDescription());
            task.setActive(projectTask.isActive());
            task.setPriority(projectTask.getSortOrder());
            task.setToDelete("");
            projectTaskForms.add(task);
        }
        form.setProjectTasks(projectTaskForms);

        List<ProjectManager> projectManagers = projectManagerService.findByProject(project);
        List<AdminProjectManagerForm> projectManagerForms = new ArrayList<AdminProjectManagerForm>();
        for (ProjectManager projectManager : projectManagers) {
            AdminProjectManagerForm manager = new AdminProjectManagerForm();
            manager.setId(projectManager.getId());
            manager.setEmployee(projectManager.getEmployee().getId());
            manager.setProjectRole(projectManager.getProjectRole().getId());
            manager.setMaster(projectManager.isMaster());
            manager.setActive(projectManager.isActive());
            manager.setReceivingNotifications(projectManager.isReceivingNotifications());
            manager.setToDelete("");
            projectManagerForms.add(manager);
        }
        form.setProjectManagers(projectManagerForms);

        List<EmployeeProjectBillable> projectBillables = employeeProjectBillableService.findByProject(project);
        List<AdminProjectBillableForm> projectBillableForms = new ArrayList<AdminProjectBillableForm>();
        for (EmployeeProjectBillable projectBillable : projectBillables) {
            AdminProjectBillableForm billable = new AdminProjectBillableForm();
            billable.setId(projectBillable.getId());
            billable.setEmployee(projectBillable.getEmployee().getId());
            billable.setStartDate(dateFormat.format(projectBillable.getStartDate()));
            billable.setEndDate(dateFormat.format(projectBillable.getEndDate()));
            billable.setComment(projectBillable.getComment());
            billable.setToDelete("");
            projectBillableForms.add(billable);
        }
        form.setProjectBillables(projectBillableForms);

        modelAndView.addObject("projectform", form);

        return modelAndView;
    }

    @RequestMapping(value = "/admin/projects/save", method = RequestMethod.POST)
    public ModelAndView showSaveOneForm(
            @ModelAttribute("projectform") AdminProjectForm form
    ) {
        ModelAndView modelAndView = new ModelAndView("adminProjectEdit");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/projects/edit", method = RequestMethod.POST)
    public ModelAndView showSaveTwoForm(
            @ModelAttribute("projectform") AdminProjectForm form,
            BindingResult bindingResult
    ) {
        ModelAndView modelAndView = new ModelAndView("adminProjectEdit");
        return modelAndView;
    }
}
