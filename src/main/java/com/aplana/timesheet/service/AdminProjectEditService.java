package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.AdminProjectBillableForm;
import com.aplana.timesheet.form.AdminProjectForm;
import com.aplana.timesheet.form.AdminProjectManagerForm;
import com.aplana.timesheet.form.AdminProjectTaskForm;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by abayanov
 * Date: 12.08.14
 */
@Service
public class AdminProjectEditService {

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

    public void saveProject(AdminProjectForm form) {
        Project project = projectService.find(form.getId());
        if (project == null) {
            project = new Project();
        }

        // Заполнение проекта данными с формы
        project.setName(StringEscapeUtils.unescapeHtml4(form.getName()));
        project.setDivision(divisionService.find(form.getDivision()));
        project.setManager(employeeService.find(form.getManager()));
        project.setCustomer(StringEscapeUtils.unescapeHtml4(form.getCustomer()));
        project.setStartDate(DateTimeUtil.parseStringToDateForDB(form.getStartDate()));
        project.setEndDate(DateTimeUtil.parseStringToDateForDB(form.getEndDate()));
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
                projectBillable.setStartDate(DateTimeUtil.parseStringToDateForDB(billableForm.getStartDate()));
                projectBillable.setEndDate(DateTimeUtil.parseStringToDateForDB(billableForm.getEndDate()));
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
    }

    public AdminProjectForm getAdminProjectForm(Project project) {
        AdminProjectForm form = new AdminProjectForm();
        form.setId(project.getId());
        form.setName(project.getName());
        Division projectDivision = project.getDivision();
        form.setDivision((projectDivision != null)? projectDivision.getId() : 0);
        form.setManager(project.getManager().getId());

        form.setManagerDivision(project.getManager().getDivision().getId());
        form.setCustomer(project.getCustomer());
        form.setStartDate(project.getStartDate() == null ? null : DateTimeUtil.formatDateIntoDBFormat(project.getStartDate()));
        form.setEndDate(project.getEndDate() == null ? null : DateTimeUtil.formatDateIntoDBFormat(project.getEndDate()));
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
        // Отсортируем по ФИО
        Collections.sort(projectManagers, new Comparator<ProjectManager>() {
            @Override
            public int compare(ProjectManager pm1, ProjectManager pm2) {
                return pm1.getEmployee().getName().compareTo(pm2.getEmployee().getName());
            }
        });
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
            billable.setBillable(projectBillable.getBillable());
            if (projectBillable.getStartDate() != null) {
                billable.setStartDate(DateTimeUtil.formatDateIntoDBFormat(projectBillable.getStartDate()));
            }
            if (projectBillable.getEndDate() != null) {
                billable.setEndDate(DateTimeUtil.formatDateIntoDBFormat(projectBillable.getEndDate()));
            }
            billable.setComment(projectBillable.getComment());
            billable.setToDelete("");
            projectBillableForms.add(billable);
        }
        form.setProjectBillables(projectBillableForms);
        return form;
    }
}
