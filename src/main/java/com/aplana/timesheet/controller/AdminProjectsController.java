package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.AdminProjectsForm;
import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dsysterov
 * @version 1.0
 */
@Controller
public class AdminProjectsController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProjectsController.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    DivisionService divisionService;

    @Autowired
    EmployeeService employeeService;

    @RequestMapping(value = "/admin/projects", method = RequestMethod.GET)
    public ModelAndView showGetPage(
            @CookieValue(value = "adminProjects_divisionId", defaultValue = "-1") Integer divisionId,
            @CookieValue(value = "adminProjects_managerId", defaultValue = "-1") Integer managerId,
            @CookieValue(value = "adminProjects_showActiveOnly", defaultValue = "false") Boolean showActiveOnly,
            @ModelAttribute("adminprojects") AdminProjectsForm form
    ) {
        return prepareProjectsMAV(divisionId, managerId, showActiveOnly);
    }

    @RequestMapping(value = "/admin/projects", method = RequestMethod.POST)
    public ModelAndView showPostPage(
            @RequestParam(value = "divisionId", required = false, defaultValue = "-1") Integer divisionId,
            @RequestParam(value = "managerId", required = false, defaultValue = "-1") Integer managerId,
            @RequestParam(value = "showActiveOnly", required = false, defaultValue = "false") Boolean showActiveOnly,
            @ModelAttribute("adminprojects") AdminProjectsForm form
    ) {
        return prepareProjectsMAV(divisionId, managerId, showActiveOnly);
    }

    private ModelAndView prepareProjectsMAV(Integer divisionId, Integer managerId, Boolean showActiveOnly) {
        ModelAndView modelAndView = new ModelAndView("adminProjects");
        modelAndView.addObject("divisionsList", divisionService.getAllDivisions());
        modelAndView.addObject("mainProjectManagersJSON", employeeService.getMainProjectManagersJSON());
        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("managerId", managerId);
        modelAndView.addObject("showActiveOnly", showActiveOnly);

        if (divisionId == -1) {
            if (managerId == -1) {
                modelAndView.addObject("projects", projectService.getProjectsByActive(showActiveOnly));
            } else {
                modelAndView.addObject("projects", projectService.getProjectsByManagerAndActive(
                        employeeService.find(managerId), showActiveOnly));
            }
        } else {
            if (managerId == -1) {
                modelAndView.addObject("projects", projectService.getProjectsByDivisionAndActive(
                        divisionService.find(divisionId), showActiveOnly));
            } else {
                modelAndView.addObject("projects", projectService.getProjectsByDivisionAndManagerAndActive(
                        divisionService.find(divisionId), employeeService.find(managerId), showActiveOnly));
            }
        }

        return modelAndView;
    }
}
