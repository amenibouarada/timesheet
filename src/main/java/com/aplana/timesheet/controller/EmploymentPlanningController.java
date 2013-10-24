package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
public class EmploymentPlanningController {
    private static final Logger logger = LoggerFactory.getLogger(EmploymentPlanningController.class);



    @Autowired
    private CalendarService calendarService;
    @Autowired
    private ProjectService projectService;



    private List<com.aplana.timesheet.dao.entity.Calendar> getYearList() {
        return DateTimeUtil.getYearsList(calendarService);
    }


    /* страница по умолчанию */
    @RequestMapping("/employmentPlanning")
    public ModelAndView showForm(
            @ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        final ModelAndView modelAndView = new ModelAndView("employmentPlanning");

        final List<Calendar> yearList = getYearList();

        modelAndView.addObject("yearList", yearList);
        modelAndView.addObject("monthList", calendarService.getMonthList(2013)); //todo исправить обработку месяцев и года
        modelAndView.addObject("projectList", projectService.getAllProjects());

        return modelAndView;
    }

    /* страница с запрошенными данными */
    @RequestMapping(value = "/employmentPlanning", method = RequestMethod.POST)
    public ModelAndView showTable(
            @ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        final ModelAndView modelAndView = new ModelAndView("employmentPlanning");

        final List<Calendar> yearList = getYearList();

        modelAndView.addObject("yearList", yearList);
        modelAndView.addObject("monthList", calendarService.getMonthList(2013)); //todo исправить обработку месяцев и года
        modelAndView.addObject("projectList", projectService.getProjectsByDates(new Date(),new Date()));

        return modelAndView;
    }


}
