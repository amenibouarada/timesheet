package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.form.ActiveProjectsForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * Created by abayanov
 * Date: 04.06.14
 */
@Controller
public class ActiveProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ActiveProjectController.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRoleService projectRoleService;

    @Autowired
    ProjectManagerService projectManagerService;

    @Autowired
    DivisionService divisionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @RequestMapping(value = "/activeProjects", method = RequestMethod.GET)
    public ModelAndView showActiveProjects(
            @ModelAttribute("activeProjectForm") ActiveProjectsForm tsForm) {

        ModelAndView mav = new ModelAndView("activeProjects");
        if (tsForm.getDivisionId() == null) {
            TimeSheetUser securityUser = securityService.getSecurityPrincipal();
            tsForm.setDivisionId(securityUser.getEmployee().getDivision().getId());
        }

        fillActiveProjects(mav, tsForm.getDivisionId());
        return mav;
    }

    @RequestMapping(value = "/viewProjects/{projectId}", method = RequestMethod.GET)
    public ModelAndView viewActiveProject(
            @PathVariable("projectId") Integer projectId) {
        ModelAndView mav = new ModelAndView("viewProject");
        fillProjectInfo(projectId, mav);
        return mav;
    }

    private void fillProjectInfo(Integer projectId, ModelAndView mav) {
        Project project = projectService.find(projectId);
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
            for (ProjectManager projectManager : masterAnalystsList) {
                masterAnalysts.append(projectManager.getEmployee().getName())
                        .append(", ");
                ids.add(projectManager.getEmployee().getId());
            }
            if (masterAnalysts.length() > 0) {
                masterAnalysts.delete(masterAnalysts.length() - 2, masterAnalysts.length());
            }
        }

        if (teamleadersList.size() == 1) {
            teamleaders.append(teamleadersList.get(0).getEmployee().getName());
            ids.add(teamleadersList.get(0).getEmployee().getId());
        } else {
            for (ProjectManager projectManager : teamleadersList) {
                teamleaders.append(projectManager.getEmployee().getName())
                        .append(", ");
                ids.add(projectManager.getEmployee().getId());
            }
            if (teamleaders.length() > 0) {
                teamleaders.delete(teamleaders.length() - 2, teamleaders.length());
            }
        }
        if (project.getManager() != null) {
            ids.add(project.getManager().getId());
        }
        HashMap<Employee, List<ProjectRole>> employees = projectService.getEmployesWhoWasOnProjectByDates(DateUtils.addDays(new Date(), -30), new Date(), project, ids);
        List<Employee> employeesPlan = employeeProjectPlanService.getEmployesWhoWillWorkOnProject(project, new Date(), DateUtils.addDays(new Date(), 30), ids);

        HashMap<HashMap<String, String>,Integer> employeesStrings = new HashMap<HashMap<String, String>,Integer>();

        for (Employee employee : employees.keySet()) {
            String name = employee.getName();
            StringBuilder roles = new StringBuilder();
            for (ProjectRole role : employees.get(employee)) {
                roles.append(role.getName()).append(", ");
            }
            if (roles.length() > 0) {
                roles.delete(roles.length()-2, roles.length());
            } else {
                roles.append(employee.getJob().getName());
            }
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(name, roles.toString());
            employeesStrings.put(map, getSortedRole(employees.get(employee)));
        }

        for (Employee employee : employeesPlan) {
            if (!employeesStrings.containsKey(employee.getName())) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(employee.getName(),employee.getJob().getName());
                List<ProjectRole> projectRoles = new ArrayList<ProjectRole>();
                projectRoles.add(employee.getJob());
                employeesStrings.put(map, getSortedRole(projectRoles));
            }
        }

        Set<Map.Entry<HashMap<String, String>,Integer>> set = employeesStrings.entrySet();
        List<Map.Entry<HashMap<String, String>,Integer>> list = new ArrayList<Map.Entry<HashMap<String, String>,Integer>>(set);
        Collections.sort( list, new Comparator<Map.Entry<HashMap<String, String>,Integer>>()
        {
            public int compare( Map.Entry<HashMap<String, String>,Integer> o1, Map.Entry<HashMap<String, String>,Integer> o2 )
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        } );
        LinkedHashMap<String, String> sorted = new LinkedHashMap<String, String>();
        for (Map.Entry<HashMap<String, String>,Integer> entry : list){
            sorted.put((String)entry.getKey().keySet().toArray()[0], entry.getKey().get((String)entry.getKey().keySet().toArray()[0]));
        }

        mav.addObject("project", project);
        mav.addObject("teamEmployees", sorted.size() > 0 ? sorted : null);
        mav.addObject("masterAnalysts", masterAnalysts);
        mav.addObject("teamleaders", teamleaders);
    }


    private Integer getSortedRole(List<ProjectRole> projectRoles) {
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

    private void fillActiveProjects(ModelAndView mav, Integer divisionId) {
        Division division = divisionService.find(divisionId);
        List<Project> projects = projectService.getActiveProjectsByDivisionWithoutPresales(division);
        Iterable<Division> allDivisions = divisionService.getAllDivisions();

        mav.addObject("projects", projects);
        mav.addObject("divisionsList", allDivisions);
        mav.addObject("division_id", divisionId);
    }

}
