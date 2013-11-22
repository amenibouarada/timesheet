package com.aplana.timesheet.controller;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.TypesOfTimeSheetEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.validator.TimeSheetFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;
import static argo.jdom.JsonNodeFactories.*;

@Controller
public class TimeSheetController {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    ProjectManagerService projectManagerService;

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private TimeSheetFormValidator tsFormValidator;
    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private OvertimeCauseService overtimeCauseService;
    @Autowired
    private JiraService jiraService;

    @RequestMapping(value = "/timesheet", method = RequestMethod.GET)
    public ModelAndView showMainForm(@RequestParam(value = "date", required = false) String date,
                                     @RequestParam(value = "id", required = false) Integer id,
                                     @RequestParam(value = "type", required = false) Integer type) {
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        logger.info("Showing Time Sheet main page!");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("timesheet");

        TimeSheetForm tsForm = new TimeSheetForm();

        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        Employee employee = employeeService.find(id);
        if (employee != null) {
            tsForm.setDivisionId(employee.getDivision().getId());
            tsForm.setEmployeeId(id);
        } else if (securityUser != null) {
            if (id != null) {
                String format = String.format("Can't find user by ID = %s. Was set current application user.", id);
                logger.error(format);
            }
            tsForm.setDivisionId(securityUser.getEmployee().getDivision().getId());
            tsForm.setEmployeeId(securityUser.getEmployee().getId());
        }

        if (date != null) {
            tsForm.setCalDate(date);
            //выставляем дату для DateTextBox
            mav.addObject("selectedCalDateJson", timeSheetService.getSelectedCalDateJson(tsForm));
        } else {
            mav.addObject("selectedCalDateJson", "''");
        }

        mav.addObject("effortList", timeSheetService.getEffortList());
        mav.addObject("timeSheetForm", tsForm); // command object
        mav.addObject("selectedProjectRolesJson", "[{row:'0', role:''}]");
        mav.addObject("selectedProjectTasksJson", "[{row:'0', task:''}]");
        mav.addObject("selectedProjectsJson", "[{row:'0', project:''}]");
        mav.addObject("selectedWorkplaceJson", "[{row:'0', workplace:''}]");
        mav.addObject("selectedActCategoriesJson", "[{row:'0', actCat:''}]");
        mav.addAllObjects(timeSheetService.getListsToMAV(request));

        //если параметр передан и означает, что мы хотим загрузит черновик
        if (type != null && TypesOfTimeSheetEnum.DRAFT.getId() == type) {
            mav.addObject("data", "true");
        }

        return mav;
    }

    @RequestMapping(value = "/cqcodes", method = RequestMethod.GET)
    public String showCqCodes() {
        logger.info("Showing CQ Codes page!");
        return "cqcodes";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:timesheet";
    }

    @RequestMapping(value = "/newReport", method = RequestMethod.POST)
    public String newReport() {
        return "redirect:timesheet";
    }

    /**
     * Пользователь нажал на кнопку "Отправить новый отчёт" на странице selected.jsp
     *
     * @return редирект timesheet
     */
    @RequestMapping(value = "/sendNewReport", method = RequestMethod.POST)
    public String sendNewReport() {
        return "redirect:timesheet";
    }

    @RequestMapping(value = "/sendDraft", method = RequestMethod.POST)
    public ModelAndView sendDraft(@ModelAttribute("timeSheetForm") TimeSheetForm tsForm, BindingResult result) {

        tsFormValidator.validateDraft(tsForm, result);
        if (result.hasErrors()) {
            return getModelAndViewForTimesheet(tsForm, result);
        }

        timeSheetService.storeTimeSheet(tsForm, TypesOfTimeSheetEnum.DRAFT);

        ModelAndView mav = new ModelAndView("draftSaved");
        mav.addObject("timeSheetForm", tsForm);

        return mav;
    }

    /**
     * Метод для вызова загрузки черновика
     * @param date день за который заполнен черновик
     * @param employeeId идентификатор сотрудника заполнившего черновик
     * @return json строку с данными
     */
    @RequestMapping(value = "/timesheet/loadDraft", headers = "Accept=application/json;Charset=UTF-8")
    @ResponseBody
    public String loadDraft(@RequestParam("date") String date,
                            @RequestParam("employeeId") Integer employeeId) {
        return JsonUtil.format(getJsonObjectNodeBuilderForReport(date, employeeId, Arrays.asList(TypesOfTimeSheetEnum.DRAFT)));
    }

    @RequestMapping(value = "/timesheet", method = RequestMethod.POST)
    public ModelAndView sendTimeSheet(@ModelAttribute("timeSheetForm") TimeSheetForm tsForm, BindingResult result) {
        logger.info("Processing form validation for employee {} ({}).", tsForm.getEmployeeId(), tsForm.getCalDate());
        tsFormValidator.validate(tsForm, result);
        if (result.hasErrors()) {
            return getModelAndViewForTimesheet(tsForm, result);
        }
        TimeSheet timeSheet = timeSheetService.storeTimeSheet(tsForm, TypesOfTimeSheetEnum.REPORT);
        overtimeCauseService.store(timeSheet, tsForm);
        sendMailService.performMailing(tsForm);

        ModelAndView mav = new ModelAndView("selected");
        mav.addObject("timeSheetForm", tsForm);
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        return mav;
    }

    /**
     * Удаляет отчет по id. В случае если текущий авторизованный пользователь является руководителем сотрудника, добавившего отчет.
     *
     * @param id
     * @return OK или Error
     */
    @RequestMapping(value = "/timesheetDel/{id}", method = RequestMethod.POST)
    public String delTimeSheet(@PathVariable("id") Integer id, HttpServletRequest httpRequest) {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new SecurityException("Не найден пользователь в контексте безопасности.");
        }

        TimeSheet timeSheet = timeSheetService.find(id);

        logger.info("Удаляется отчет " + timeSheet + ". Инициатор: " + securityUser.getEmployee().getName());
        timeSheetService.delete(timeSheet);

        sendMailService.performTimeSheetDeletedMailing(timeSheet);

        return "redirect:" + httpRequest.getHeader("Referer");
    }

    /**
     * Возвращает планы предыдущего дня и на следующего дня.
     * Так же данные о том, есть ли на этот день черновик
     *
     * @param date       (2012-11-25)
     * @param employeeId (573)
     * @return Json String
     */
    @RequestMapping(value = "/timesheet/daySupData", headers = "Accept=application/json;Charset=UTF-8")
    @ResponseBody
    public String getPlans(@RequestParam("date") String date,
                           @RequestParam("employeeId") Integer employeeId) {

        JsonObjectNodeBuilder jsonObjectNodeBuilder = timeSheetService.getPlansJsonBuilder(date, employeeId);
        TimeSheet timeSheet = timeSheetService.findForDateAndEmployeeByTypes(date, employeeId, Arrays.asList(TypesOfTimeSheetEnum.DRAFT));
        jsonObjectNodeBuilder.withField("draft", aStringBuilder(timeSheet != null && timeSheet.getId() != null ? "1" : "0"));
        return JsonUtil.format(jsonObjectNodeBuilder);
    }

    @RequestMapping(value = "/timesheet/jiraIssues", headers = "Accept=application/octet-stream;Charset=UTF-8")
    @ResponseBody
    public String getJiraIssuesStr(@RequestParam("employeeId") Integer employeeId,
                                   @RequestParam("date") String date,
                                   @RequestParam("projectId") Integer projectId) {
        return jiraService.getDayIssues(employeeId, date, projectId);
    }

    @RequestMapping(value = "/employee/isDivisionLeader", headers = "Accept=application/json")
    @ResponseBody
    public String isDivisionLeader(@RequestParam("employeeId") Integer employeeId) {
        return JsonUtil.format(
                object(
                        field(
                                "isDivisionLeader",
                                employeeService.isEmployeeDivisionLeader(employeeId) ? trueNode() : falseNode())
                )
        );
    }



    /**
     * Возвращает {@link JsonObjectNodeBuilder} в котором находится сохраненный черновик
     * на дату и для пользователя
     *
     * @param date       - дата отчета
     * @param employeeId - идентификатор пользователя чей отчет прогружаем в @link JsonObjectNodeBuilder}
     * @param types      - типы отчета, один из которых может быть возращен на указанныую дату
     * @return {@link JsonObjectNodeBuilder}
     */
    private JsonObjectNodeBuilder getJsonObjectNodeBuilderForReport(String date, Integer employeeId, List<TypesOfTimeSheetEnum> types) {
        TimeSheet timeSheet = timeSheetService.findForDateAndEmployeeByTypes(date, employeeId, types);

        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final JsonObjectNodeBuilder builderNode = anObjectBuilder();
//        TimeSheetDetail timeSheetDetail=timeSheet.getTimeSheetDetails();
        if (timeSheet != null && timeSheet.getTimeSheetDetails() != null && timeSheet.getTimeSheetDetails().size() != 0) {
//            final JsonObjectNodeBuilder builder = anObjectBuilder();
            int i = 0;
            builderNode.withField("rows", aStringBuilder(String.valueOf(timeSheet.getTimeSheetDetails().size())));

            for (TimeSheetDetail timeSheetDetail : timeSheet.getTimeSheetDetails())
                builder.withElement(
                        anObjectBuilder().
                                withField("row", JsonUtil.aStringBuilder(i++)).
                                withField("activity_type_id", aStringBuilder(timeSheetDetail.getActType() != null ? timeSheetDetail.getActType().getId().toString() : "0")).
                                withField("workplace_id", aStringBuilder(timeSheetDetail.getWorkplace() != null ? timeSheetDetail.getWorkplace().getId().toString() : "0")).
                                withField("project_id", aStringBuilder(timeSheetDetail.getProject() != null ? timeSheetDetail.getProject().getId().toString() : "0")).
                                withField("project_role_id", aStringBuilder(timeSheetDetail.getProjectRole() != null ? timeSheetDetail.getProjectRole().getId().toString() : "0")).
                                withField("activity_category_id", aStringBuilder(timeSheetDetail.getActCat() != null ? timeSheetDetail.getActCat().getId().toString() : "0")).
                                withField("projectTask_id", aStringBuilder(timeSheetDetail.getProjectTask() != null ? timeSheetDetail.getProjectTask().getId().toString() : "0")).
                                withField("duration_id", aStringBuilder(timeSheetDetail.getDuration() != null ? timeSheetDetail.getDuration().toString() : "")).
                                withField("description_id", aStringBuilder(timeSheetDetail.getDescription() != null ? timeSheetDetail.getDescription() : "")).
                                withField("problem_id", aStringBuilder(timeSheetDetail.getProblem() != null ? timeSheetDetail.getProblem() : ""))
                );
            builderNode.withField("data", builder);
        }
        return builderNode;
    }

    /**
     * Формирует {@link ModelAndView} с отчетом для timesheet.jsp
     *
     * @param tsForm
     * @param result
     * @return
     */
    private ModelAndView getModelAndViewForTimesheet(TimeSheetForm tsForm, BindingResult result) {
        tsForm.unEscapeHTML();
        logger.info("TimeSheetForm for employee {} has errors. Form not validated.", tsForm.getEmployeeId());
        ModelAndView mavWithErrors = new ModelAndView("timesheet");
        mavWithErrors.addObject("timeSheetForm", tsForm);
        mavWithErrors.addObject("errors", result.getAllErrors());
        mavWithErrors.addObject("selectedProjectsJson", timeSheetService.getSelectedProjectsJson(tsForm));
        mavWithErrors.addObject("selectedProjectRolesJson", timeSheetService.getSelectedProjectRolesJson(tsForm));
        mavWithErrors.addObject("selectedProjectTasksJson", timeSheetService.getSelectedProjectTasksJson(tsForm));
        mavWithErrors.addObject("selectedWorkplaceJson", timeSheetService.getSelectedWorkplaceJson(tsForm));
        mavWithErrors.addObject(
                "selectedActCategoriesJson",
                timeSheetService.getSelectedActCategoriesJson(tsForm)
        );
        mavWithErrors.addObject("selectedCalDateJson", timeSheetService.getSelectedCalDateJson(tsForm));
        mavWithErrors.addObject("effortList", timeSheetService.getEffortList());
        mavWithErrors.addAllObjects(timeSheetService.getListsToMAV(request));

        return mavWithErrors;
    }
}