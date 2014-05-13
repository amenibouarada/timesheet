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
import org.apache.commons.lang3.StringEscapeUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Autowired
    DictionaryItemService dictionaryItemService;

    @RequestMapping(value = "/admin/projects/add", method = RequestMethod.GET)
     public ModelAndView showAddForm(
            @RequestParam(value = "divisionId", required = false, defaultValue = "-1") Integer divisionId,
            @RequestParam(value = "managerId", required = false, defaultValue = "-1") Integer managerId
    ) {
        ModelAndView modelAndView = new ModelAndView("adminProjectEdit");
        modelAndView.addObject("pageFunction", "add");
        modelAndView.addObject("divisionsList", divisionService.getAllDivisions());
        List<TypesOfActivityEnum> projectTypesOfActivityEnums = Arrays.asList(TypesOfActivityEnum.PROJECT, TypesOfActivityEnum.PRESALE);
        modelAndView.addObject("projectStateTypes", projectTypesOfActivityEnums);
        modelAndView.addObject("projectFundingTypes", ProjectFundingTypeEnum.values());
        modelAndView.addObject("divisionsEmployeesJSON", employeeService.getDivisionsEmployeesJSON(new Date()));
        modelAndView.addObject("employeesListJSON", employeeService.getAllEmployeesJSON());
        modelAndView.addObject("employeesList", employeeService.getAllEmployees());
        modelAndView.addObject("projectRoleTypes", ProjectRolesEnum.values());
        modelAndView.addObject("projectRoleTypesJSON",
                projectRoleService.getProjectRoleListJson(projectRoleService.getProjectRoles()));

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
        modelAndView.addObject("pageFunction", "edit");
        modelAndView.addObject("divisionsList", divisionService.getAllDivisions());
        List<TypesOfActivityEnum> projectTypesOfActivityEnums = Arrays.asList(TypesOfActivityEnum.PROJECT, TypesOfActivityEnum.PRESALE);
        modelAndView.addObject("projectStateTypes", projectTypesOfActivityEnums);
        modelAndView.addObject("projectFundingTypes", ProjectFundingTypeEnum.values());
        modelAndView.addObject("divisionsEmployeesJSON", employeeService.getDivisionsEmployeesJSON(new Date()));
        modelAndView.addObject("employeesListJSON", employeeService.getAllEmployeesJSON());
        modelAndView.addObject("employeesList", employeeService.getAllEmployees());
        modelAndView.addObject("projectRoleTypes", ProjectRolesEnum.values());
        modelAndView.addObject("projectRoleTypesJSON",
                projectRoleService.getProjectRoleListJson(projectRoleService.getProjectRoles()));

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
        form.setPassport(project.getPassport());
        ArrayList<Integer> formDivisions = new ArrayList<Integer>();
        for (Division division : project.getDivisions()) {
            formDivisions.add(division.getId());
        }
        form.setProjectDivisions(formDivisions);


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
            if (projectBillable.getStartDate() != null) {
                billable.setStartDate(dateFormat.format(projectBillable.getStartDate()));
            }
            if (projectBillable.getEndDate() != null) {
                billable.setEndDate(dateFormat.format(projectBillable.getEndDate()));
            }
            billable.setComment(projectBillable.getComment());
            billable.setToDelete("");
            projectBillableForms.add(billable);
        }
        form.setProjectBillables(projectBillableForms);

        modelAndView.addObject("projectform", form);
        return modelAndView;
    }

    @RequestMapping(value = "/admin/projects/save", method = RequestMethod.POST)
    public String saveProject(
            @ModelAttribute("projectform") AdminProjectForm form
    ) {
        Project project = projectService.find(form.getId());
        if (project == null) {
            project = new Project();
        }

        // Заполнение проекта данными с формы
        project.setName(StringEscapeUtils.unescapeHtml4(form.getName()));
        project.setDivision(divisionService.find(form.getDivision()));
        project.setManager(employeeService.find(form.getManager()));
        project.setCustomer(StringEscapeUtils.unescapeHtml4(form.getCustomer()));
        try {
            project.setStartDate(dateFormat.parse(form.getStartDate()));
            project.setEndDate(dateFormat.parse(form.getEndDate()));
        } catch (ParseException e) {
            logger.error("AdminProjectForm date parse error!", e);
            e.printStackTrace();
        }
        project.setState(dictionaryItemService.find(form.getState()));
        project.setFundingType(dictionaryItemService.find(form.getFundingType()));
        project.setJiraProjectKey(StringEscapeUtils.unescapeHtml4(form.getJiraKey()));
        project.setActive(form.getActive());
        project.setCqRequired(form.getCqRequired());

        Set<ProjectTask> projectTasks = new HashSet<ProjectTask>();
        for (AdminProjectTaskForm taskForm : form.getProjectTasks()) {
            if (taskForm.getToDelete().length() == 0) {
                ProjectTask projectTask = projectTaskService.find(taskForm.getId());
                if (projectTask == null) {
                    projectTask = new ProjectTask();
                }

                projectTask.setTaskName(StringEscapeUtils.unescapeHtml4(taskForm.getName()));
                projectTask.setDescription(StringEscapeUtils.unescapeHtml4(taskForm.getDescription()));
                projectTask.setActive(taskForm.getActive());
                projectTask.setSortOrder(StringEscapeUtils.unescapeHtml4(taskForm.getPriority()));
                projectTask.setProject(project);
                projectTasks.add(projectTask);
            }
        }
        project.setProjectTasks(projectTasks);

        Set<ProjectManager> projectManagers = new HashSet<ProjectManager>();
        for (AdminProjectManagerForm managerForm : form.getProjectManagers()) {
            if (managerForm.getToDelete().length() == 0) {
                ProjectManager projectManager = projectManagerService.find(managerForm.getId());
                if (projectManager == null) {
                    projectManager = new ProjectManager();
                }

                projectManager.setEmployee(employeeService.find(managerForm.getEmployee()));
                projectManager.setProjectRole(projectRoleService.find(managerForm.getProjectRole()));
                projectManager.setMaster(managerForm.getMaster());
                projectManager.setActive(managerForm.getActive());
                projectManager.setProject(project);
                projectManagers.add(projectManager);
            }
        }
        project.setProjectManagers(projectManagers);

        Set<EmployeeProjectBillable> employeeProjectBillables = new HashSet<EmployeeProjectBillable>();
        for (AdminProjectBillableForm billableForm : form.getProjectBillables()) {
            if (billableForm.getToDelete().length() == 0) {
                EmployeeProjectBillable projectBillable = employeeProjectBillableService.find(billableForm.getId());
                if (projectBillable == null) {
                    projectBillable = new EmployeeProjectBillable();
                }

                projectBillable.setEmployee(employeeService.find(billableForm.getEmployee()));
                projectBillable.setBillable(billableForm.getBillable());
                try {
                    projectBillable.setStartDate(dateFormat.parse(billableForm.getStartDate()));
                    projectBillable.setEndDate(dateFormat.parse(billableForm.getEndDate()));
                } catch (ParseException e) {
                    logger.error("billableForm date parse error!", e);
                    e.printStackTrace();
                }
                projectBillable.setComment(StringEscapeUtils.unescapeHtml4(billableForm.getComment()));
                projectBillable.setProject(project);
                employeeProjectBillables.add(projectBillable);
            } else {
                EmployeeProjectBillable projectBillable = employeeProjectBillableService.find(billableForm.getId());
                if (projectBillable != null) {
                    employeeProjectBillableService.delete(projectBillable);
                }
            }
        }
        project.setEmployeeProjectBillables(employeeProjectBillables);

        project.setPassport(StringEscapeUtils.unescapeHtml4(form.getPassport()));
        Set<Division> projectDivisions = new HashSet<Division>();
        List<Integer> projectDivisionIDs = form.getProjectDivisions();
        if (projectDivisionIDs == null) {
            projectDivisionIDs = new ArrayList<Integer>();
            projectDivisionIDs.add(form.getManagerDivision());
        }
        for (Integer divisionId : projectDivisionIDs) {
            projectDivisions.add(divisionService.find(divisionId));
        }
        project.setDivisions(projectDivisions);

        projectService.storeProject(project);

        return "redirect:/admin/projects";
    }
}
