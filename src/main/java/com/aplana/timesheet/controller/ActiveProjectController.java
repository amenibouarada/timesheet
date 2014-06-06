package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectManager;
import com.aplana.timesheet.form.ActiveProjectsForm;
import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.service.ProjectManagerService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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
    ProjectManagerService projectManagerService;

    @Autowired
    DivisionService divisionService;

    @Autowired
    private SecurityService securityService;

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
        Project project = projectService.find(projectId);
        List<ProjectManager> projectManagers =  projectManagerService.getSortedListByProject(project);
        mav.addObject("project", project);
        mav.addObject("projectManagers", projectManagers);
        return mav;
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
