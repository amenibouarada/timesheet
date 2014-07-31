package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.dao.entity.VacationApprovalResult;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    private VacationApprovalResultService vacationApprovalResultService;

    /** Получить проектную роль согласующего*/
    public String getManagerRoleName(VacationApproval vacationApproval){
        if (isLineManager(vacationApproval.getVacation().getEmployee(), vacationApproval.getManager())){
            return LINE_MANAGER;
        }
        List<Project> projectList = getProjects(vacationApproval);
        for (Project project : projectList){
            if (project.getManager().getId().equals(vacationApproval.getManager().getId())){
                return String.format(PROJECT_LEADER, project.getName());
            }
        }
        if (projectList != null && !projectList.isEmpty()) {
            Project project = projectList.get(0);
            if (vacationApproval.getManager().getJob().getId().equals(ProjectRolesEnum.ANALYST.getId())){
                return String.format(SENIOR_ANALYST, project.getName());
            }
            return String.format(TEAM_LEADER, project.getName());
        }else{
            logger.error(String.format("Не удалось определить проектную роль согласующего \"%s\"",vacationApproval.getManager().getName()));
        }
        return "";
    }

    private List<Project> getProjects(VacationApproval vacationApproval){
        List<VacationApprovalResult> projectsVarList = vacationApprovalResultService.getVacationApprovalResultByManager(vacationApproval);
        List<Project> resultList = new ArrayList<Project>();
        for (VacationApprovalResult var : projectsVarList){
            resultList.add(var.getProject());
        }
        return resultList;
    }

    /** Проверка на линейного руководителя*/
    private Boolean isLineManager(Employee employee, Employee manager){
        Integer man = employee.getManager() != null
                ? employee.getManager().getId() : null;
        /* APLANATS-865
        Integer man2 = employee.getManager2() != null
                ? employee.getManager2().getId() : null;*/
        if (!(manager.getId().equals(man) /*|| manager.getId().equals(man2)*/)){
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
