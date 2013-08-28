package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.validator.TimeSheetFormValidator;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.EmployeeHelper;
import com.aplana.timesheet.util.JsonUtil;
import com.aplana.timesheet.util.TimeSheetUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static argo.jdom.JsonNodeFactories.*;

@Controller
public class TimeSheetController {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    ProjectManagerService projectManagerService;
    @Autowired
    private ProjectTaskService projectTaskService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private TimeSheetService timeSheetService;
    @Autowired
    private AvailableActivityCategoryService availableActivityCategoryService;
    @Autowired
    private TimeSheetFormValidator tsFormValidator;
    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private EmployeeLdapService employeeLdapService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EmployeeHelper employeeHelper;
    @Autowired
    private TSPropertyProvider propertyProvider;
    @Autowired
    private OvertimeCauseService overtimeCauseService;
    @Autowired
    private JiraService jiraService;

    @RequestMapping(value = "/timesheet", method = RequestMethod.GET)
    public ModelAndView showMainForm(@RequestParam(value = "date",required = false) String date,
                                     @RequestParam(value = "id",required = false) Integer id) {
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
                String format = String.format("Can't find user by ID = %s. Was set current application user.",id);
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
        mav.addAllObjects(getListsToMAV());

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

    //Пользователь нажал на кнопку "Отправить новый отчёт" на странице selected.jsp
    @RequestMapping(value = "/sendNewReport", method = RequestMethod.POST)
    public String sendNewReport() {
        return "redirect:timesheet";
    }

    @RequestMapping(value = "/timesheet", method = RequestMethod.POST)
    public ModelAndView sendTimeSheet(@ModelAttribute("timeSheetForm") TimeSheetForm tsForm, BindingResult result) {
        logger.info("Processing form validation for employee {} ({}).", tsForm.getEmployeeId(), tsForm.getCalDate());
        tsFormValidator.validate(tsForm, result);
        if (result.hasErrors()) {
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
            mavWithErrors.addAllObjects(getListsToMAV());

            return mavWithErrors;
        }
        TimeSheet timeSheet = timeSheetService.storeTimeSheet(tsForm);
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

    /*
      * Возвращает HashMap со значениями для заполнения списков сотрудников,
      * проектов, пресейлов, проектных задач, типов и категорий активности на
      * форме приложения.
      */
    private Map<String, Object> getListsToMAV() {
        Map<String, Object> result = new HashMap<String, Object>();

        List<DictionaryItem> typesOfActivity = dictionaryItemService.getTypesOfActivity();
        result.put("actTypeList", typesOfActivity);

        String typesOfActivityJson = dictionaryItemService.getDictionaryItemsInJson(typesOfActivity);
        result.put("actTypeJson", typesOfActivityJson);

        String workplacesJson = dictionaryItemService.getDictionaryItemsInJson(dictionaryItemService.getWorkplaces());
        result.put("workplaceJson", workplacesJson);

        result.put("overtimeCauseJson", dictionaryItemService.getDictionaryItemsInJson(dictionaryItemService
                .getOvertimeCauses()) );
        result.put("unfinishedDayCauseJson", dictionaryItemService.getDictionaryItemsInJson(dictionaryItemService
                .getUnfinishedDayCauses()
        ) );
        result.put("overtimeThreshold", propertyProvider.getOvertimeThreshold());
        result.put("undertimeThreshold", propertyProvider.getUndertimeThreshold());

        List<Division> divisions = divisionService.getDivisions();
        result.put("divisionList", divisions);

        String employeeListJson = employeeHelper.getEmployeeListJson(divisions, employeeService.isShowAll(request), true);
        result.put("employeeListJson", employeeListJson);

        List<DictionaryItem> categoryOfActivity = dictionaryItemService.getCategoryOfActivity();
        result.put("actCategoryList", categoryOfActivity);

        String actCategoryListJson = dictionaryItemService.getDictionaryItemsInJson(categoryOfActivity);
        result.put("actCategoryListJson", actCategoryListJson);

        result.put("availableActCategoriesJson", availableActivityCategoryService.getAvailableActCategoriesJson());

        result.put("projectListJson", projectService.getProjectListJson(divisions));
        result.put(
                "projectTaskListJson",
                projectTaskService.getProjectTaskListJson(projectService.getProjectsWithCq())
        );

        List<ProjectRole> projectRoleList = projectRoleService.getProjectRoles();

        for (int i = 0; i < projectRoleList.size(); i++) {
            if (projectRoleList.get(i).getCode().equals("ND")) {  // Убираем из списка роль "Не определена" APLANATS-270
                projectRoleList.remove(i);
                break;
            }
        }

        result.put("projectRoleList", projectRoleList);
        result.put("projectRoleListJson", projectRoleService.getProjectRoleListJson(projectRoleList));

        result.put("listOfActDescriptionJson", timeSheetService.getListOfActDescription());
        result.put(
                "typesOfCompensation",
                dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.TYPES_OF_COMPENSATION.getId())
        );
        result.put(
                "workOnHolidayCauseJson",
                dictionaryItemService.getDictionaryItemsInJson(DictionaryEnum.WORK_ON_HOLIDAY_CAUSE.getId())
        );

        return result;
    }

    /**
     * Возвращает планы предыдущего дня и на следующего дня
     * @param date (2012-11-25)
     * @param employeeId (573)
     * @return Json String
     */
    @RequestMapping(value = "/timesheet/plans", headers = "Accept=application/json;Charset=UTF-8")
    @ResponseBody
    public String getPlans(@RequestParam("date") String date,
                           @RequestParam("employeeId") Integer employeeId) {
        return timeSheetService.getPlansJson(date, employeeId);
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
}