package com.aplana.timesheet.controller;

import com.aplana.timesheet.form.ActiveProjectsForm;
import com.aplana.timesheet.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;

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
    private ActiveProjectService activeProjectService;

    @RequestMapping(value = "/activeProjects", method = RequestMethod.GET)
    public ModelAndView showActiveProjects(
            @ModelAttribute("activeProjectForm") ActiveProjectsForm tsForm) throws ParseException {

        return activeProjectService.fillActiveProjects(tsForm);
    }

    @RequestMapping(value = "/activeProjects/{divisionId}", method = RequestMethod.GET)
    public ModelAndView showActiveProjects(@PathVariable("divisionId") Integer divisionId,
                                           @ModelAttribute("activeProjectForm") ActiveProjectsForm tsForm) throws ParseException {
        tsForm = new ActiveProjectsForm();
        tsForm.setDivisionId(divisionId);
        return showActiveProjects(tsForm);
    }

    @RequestMapping(value = "/viewProjects//{divisionId}/{projectId}", method = RequestMethod.GET)
    public ModelAndView viewActiveProject(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("projectId") Integer projectId
    ) throws ParseException {
        return activeProjectService.fillProjectInfo(projectId, divisionId);

    }



}
