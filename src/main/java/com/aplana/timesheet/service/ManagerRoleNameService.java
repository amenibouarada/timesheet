package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aplana.timesheet.enums.ProjectRolesEnum.*;

/**
 * Сервис, который возвращает название роли руководителя для конкретного сотрудника
 * @author Aalikin
 * @since 16.05.13
 */

@Service
public class ManagerRoleNameService {

    private final String LINE_MANAGER = "Линейный Руководитель";
    private final String PROJECT_LEADER = "Руководитель проекта \"%s\"";
    private final String SENIOR_ANALYST = "Ведущий аналитик \"%s\"";
    private final String TEAM_LEADER = "Тим-лидер \"%s\"";

    private static final Logger logger = LoggerFactory.getLogger(ManagerRoleNameService.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * Получить проектную роль согласующего
     */
    public String getManagerRoleName(VacationApproval vacationApproval) {
        Employee manager = vacationApproval.getManager();
        Employee employee = vacationApproval.getVacation().getEmployee();
        if (isLineManager(employee, manager)) {
            return LINE_MANAGER;
        }
        List<Project> projects = projectService.getProjectsForVacation(vacationApproval.getVacation());
        Map<ProjectRolesEnum, List<String>> projectRoles = new HashMap<ProjectRolesEnum, List<String>>();

        // Группируем названия проектов по ролям согласующего
        for (Project project : projects) {
            List<Integer> managerProjectRoleId = employeeService.getEmployeeProjectRoleIds(project.getId(), manager.getId());
            for (Integer roleId : managerProjectRoleId) {
                if (roleId != null) {
                    ProjectRolesEnum projectRolesEnum = ProjectRolesEnum.getById(roleId);

                    switch (projectRolesEnum) {
                        case HEAD:
                            addProjectName(projectRoles, HEAD, project.getName());
                            break;
                        case DEVELOPER:
                            addProjectName(projectRoles, DEVELOPER, project.getName());
                            break;
                        case ANALYST:
                            addProjectName(projectRoles, ANALYST, project.getName());
                            break;
                        default:
                            logger.error(String.format("Не удалось определить проектную роль согласующего \"%s\"", manager.getName()));
                    }
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<ProjectRolesEnum, List<String>> entry : projectRoles.entrySet()) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append("</br>");
            }
            switch (entry.getKey()) {
                case HEAD:
                    stringBuilder.append(String.format(PROJECT_LEADER, StringUtils.join(entry.getValue(), ",</br>")));
                    break;
                case DEVELOPER:
                    stringBuilder.append(String.format(TEAM_LEADER, StringUtils.join(entry.getValue(), ",</br>")));
                    break;
                case ANALYST:
                    stringBuilder.append(String.format(SENIOR_ANALYST, StringUtils.join(entry.getValue(), ",</br>")));
                    break;
            }
        }

        return stringBuilder.toString();
    }

    private void addProjectName(Map<ProjectRolesEnum, List<String>> projectRoles, ProjectRolesEnum role, String projectName) {
        List<String> headProjectNames = !CollectionUtils.isEmpty(projectRoles.get(role)) ? projectRoles.get(role) : new ArrayList<String>();
        headProjectNames.add(projectName);
        projectRoles.put(role, headProjectNames);
    }

    /** Проверка на линейного руководителя*/
    private Boolean isLineManager(Employee employee, Employee manager){
        Integer man = employee.getManager() != null
                ? employee.getManager().getId() : null;

        if (!(manager.getId().equals(man))){
            if(man != null){
                return isLineManager(employee.getManager(), manager);
            }else{
                return false;
            }
        }else{
            return true;
        }
    }
}
