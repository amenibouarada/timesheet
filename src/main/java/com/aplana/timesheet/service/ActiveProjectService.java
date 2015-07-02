package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.form.ActiveProjectsForm;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.google.common.base.Joiner;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.*;

/**
 * Created by abayanov
 * Date: 11.08.14
 */
@Service
public class ActiveProjectService {

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRoleService projectRoleService;

    @Autowired
    ProjectManagerService projectManagerService;

    @Autowired
    DivisionService divisionService;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    private SecurityService securityService;

    /**
     * Выводится информация по проекту
     * @param projectId
     * @param divisionId
     * @return
     * @throws ParseException
     */
    public ModelAndView fillProjectInfo(Integer projectId, Integer divisionId) throws ParseException {

        ModelAndView mav = new ModelAndView("viewProject");
        mav.addObject("divisionId", divisionId);
        Project project = projectService.find(projectId);
        //Заполняем главных аналитиков и тимлидеров
        List<Integer> ids = new ArrayList<Integer>();
        List<ProjectManager> masterAnalystsList = projectManagerService.getListMasterManagersByRole(ProjectRolesEnum.ANALYST.getId(), project);
        List<ProjectManager> teamleadersList = projectManagerService.getListMasterManagersByRole(ProjectRolesEnum.DEVELOPER.getId(), project);
        StringBuilder masterAnalysts = new StringBuilder();
        StringBuilder teamleaders = new StringBuilder();
        if (masterAnalystsList.size() == 1) {
            ProjectManager projectManager = masterAnalystsList.get(0);
            masterAnalysts.append(projectManager.getEmployee().getName());
            ids.add(projectManager.getEmployee().getId());
        } else {
            masterAnalysts.append(Joiner.on(", ").join(masterAnalystsList));
        }

        if (teamleadersList.size() == 1) {
            teamleaders.append(teamleadersList.get(0).getEmployee().getName());
            ids.add(teamleadersList.get(0).getEmployee().getId());
        } else {
            teamleaders.append(Joiner.on(", ").join(teamleadersList));
        }

        // Получение списка рабочих на проекте, и рабочих которых планируется привлечь
        if (project.getManager() != null) {
            ids.add(project.getManager().getId());
        }
        // ToDo заменить константы 30
        HashMap<Employee, List<ProjectRole>> employees = projectService.getEmployesWhoWasOnProjectByDates(DateUtils.addDays(new Date(), -30), new Date(), project, ids);
        List<Employee> employeesPlan = employeeProjectPlanService.getEmployeesWhoWillWorkOnProject(projectId, new Date(), DateUtils.addDays(new Date(), 30));

        HashMap<HashMap<String, String>,Integer> employeesStrings = new HashMap<HashMap<String, String>,Integer>();
        // Формируется мапа с именем пользователя, списком его ролей и приоритетом для сортировки
        for (Employee employee : employees.keySet()) {
            String name = employee.getName();
            StringBuilder roles = new StringBuilder();

            roles.append(Joiner.on(", ").join(employees.get(employee)));

            if (roles.length() == 0) {
                roles.append(employee.getJob().getName());
            }

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(name, roles.toString());
            employeesStrings.put(map, getSortedRole(employees.get(employee)));
        }
        //добавляются "запланированные" рабочие
        for (Employee employee : employeesPlan) {
            if (!employeesStrings.containsKey(employee.getName())) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(employee.getName(),employee.getJob().getName());
                List<ProjectRole> projectRoles = new ArrayList<ProjectRole>();
                projectRoles.add(employee.getJob());
                employeesStrings.put(map, getSortedRole(projectRoles));
            }
        }
        //Мапа преобразуется в лист и сортируется по приоритету
        Set<Map.Entry<HashMap<String, String>,Integer>> set = employeesStrings.entrySet();
        List<Map.Entry<HashMap<String, String>,Integer>> list = new ArrayList<Map.Entry<HashMap<String, String>,Integer>>(set);
        Collections.sort(list, new Comparator<Map.Entry<HashMap<String, String>, Integer>>() {
            public int compare(Map.Entry<HashMap<String, String>, Integer> o1, Map.Entry<HashMap<String, String>, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        LinkedHashMap<String, String> sorted = new LinkedHashMap<String, String>();
        for (Map.Entry<HashMap<String, String>,Integer> entry : list){
            sorted.put((String)entry.getKey().keySet().toArray()[0], entry.getKey().get(entry.getKey().keySet().toArray()[0]));
        }

        mav.addObject("project", project);
        mav.addObject("infiniteDate", DateUtils.addYears(new Date(), 5));
        mav.addObject("teamEmployees", sorted.size() > 0 ? sorted : null);
        mav.addObject("masterAnalysts", masterAnalysts);
        mav.addObject("teamleaders", teamleaders);
        return mav;
    }


    public Integer getSortedRole(List<ProjectRole> projectRoles) {
        List<Integer> integerListRoles = new ArrayList<Integer>();
        for (ProjectRole projectRole : projectRoles){
            integerListRoles.add(projectRole.getId());
        }

        if (integerListRoles.contains(ProjectRolesEnum.HEAD.getId())) {
            return 0;
        }
        if (integerListRoles.contains(ProjectRolesEnum.ANALYST.getId())) {
            return 1;
        }
        if (integerListRoles.contains(ProjectRolesEnum.DEVELOPER.getId())) {
            return 2;
        }
        if (integerListRoles.contains(ProjectRolesEnum.SYSTEM_ENGINEER.getId())) {
            return 3;
        }
        if (integerListRoles.contains(ProjectRolesEnum.TESTER.getId())) {
            return 4;
        }
        return 5;
    }

    public ModelAndView fillActiveProjects(ActiveProjectsForm tsForm) {
        ModelAndView mav = new ModelAndView("activeProjects");
        if (tsForm.getDivisionId() == null) {
            TimeSheetUser securityUser = securityService.getSecurityPrincipal();
            tsForm.setDivisionId(securityUser.getEmployee().getDivision().getId());
        }

        Division division = divisionService.find(tsForm.getDivisionId());
        List<Project> projects = projectService.getActiveProjectsByDivisionWithoutPresales(division);
        Iterable<Division> allDivisions = divisionService.getAllDivisions();

        mav.addObject("infiniteDate", DateUtils.addYears(new Date(), 5));
        mav.addObject("projects", projects);
        mav.addObject("divisionsList", allDivisions);
        mav.addObject("division_id", tsForm.getDivisionId());
        return mav;
    }
}
