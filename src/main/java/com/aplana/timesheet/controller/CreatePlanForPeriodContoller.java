package com.aplana.timesheet.controller;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.form.CreatePlanForPeriodForm;
import com.aplana.timesheet.form.validator.CreatePlanForPeriodFormValidator;
import com.aplana.timesheet.service.CreatePlanForPeriodService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class CreatePlanForPeriodContoller extends AbstractController {

    public static final String FORM = "createPlanForPeriodForm";

    public static final String GET_PROJECTS_URL = "/getProjectsAsJson";
    public static final String CREATE_PLAN_FOR_PERIOD_URL = "/createPlanForPeriod";
    private static final String VIEW = "createPlanForPeriod";

    public static final String FROM_DATE_PARAM = "fromDateParam";
    public static final String TO_DATE_PARAM = "toDateParam";

    public static final String PROJECT_ID = "id";
    public static final String PROJECT_NAME = "name";

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CreatePlanForPeriodService createPlanForPeriodService;

    @Autowired
    private CreatePlanForPeriodFormValidator createPlanForPeriodFormValidator;

    @RequestMapping(value = CREATE_PLAN_FOR_PERIOD_URL, method = RequestMethod.GET)
    public ModelAndView showForm(
            @ModelAttribute(FORM) CreatePlanForPeriodForm createPlanForPeriodForm
    ) {
        return createModelAndView(createPlanForPeriodForm);
    }

    private ModelAndView createModelAndView(CreatePlanForPeriodForm form) {
        final ModelAndView modelAndView = new ModelAndView(VIEW);

        modelAndView.addObject("employeeList", employeeService.getEmployees());
        // если дата не задана передаем проекты которые не закончились на 1 число текущего месяца
        Date fromDate = form.getFromDate() != null ? form.getFromDate() : DateTimeUtil.currentMonthFirstDayDate();
        modelAndView.addObject("projectList", getProjects( fromDate, form.getToDate()));

        return modelAndView;
    }

    private List<Project> getProjects(Date fromDate, Date toDate) {
        return projectService.getProjectsForPeriod(fromDate, toDate);
    }

    @RequestMapping(value = CREATE_PLAN_FOR_PERIOD_URL, method = RequestMethod.POST)
    public ModelAndView save(
            @ModelAttribute(FORM) CreatePlanForPeriodForm createPlanForPeriodForm,
            BindingResult bindingResult
    ) {
        createPlanForPeriodFormValidator.validate(createPlanForPeriodForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return createModelAndView(createPlanForPeriodForm);
        }

        final Project project = projectService.find(createPlanForPeriodForm.getProjectId());
        final Employee employee = employeeService.find(createPlanForPeriodForm.getEmployeeId());
        final Date fromDate = createPlanForPeriodForm.getFromDate();
        final Date toDate = createPlanForPeriodForm.getToDate();
        final Byte percentOfCharge = createPlanForPeriodForm.getPercentOfCharge();

        createPlanForPeriodService.createPlans(project, employee, fromDate, toDate, percentOfCharge);

        return new ModelAndView("redirect:" + PlanEditController.PLAN_EDIT_URL);
    }

    @RequestMapping(value = GET_PROJECTS_URL, headers = "Accept=application/json", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getProjectsAsJson(
            @RequestParam(value = FROM_DATE_PARAM, required = false) Date fromDate,
            @RequestParam(value = TO_DATE_PARAM, required = false) Date toDate
    ) {
        final List<Project> projects = getProjects(fromDate, toDate);
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Project project : projects) {
            builder.withElement(
                    anObjectBuilder().
                            withField(PROJECT_ID, JsonUtil.aNumberBuilder(project.getId())).
                            withField(PROJECT_NAME, aStringBuilder(project.getName()))
            );
        }

        return JsonUtil.format(builder);
    }

}
