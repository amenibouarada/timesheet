package com.aplana.timesheet.controller;

import argo.jdom.*;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.*;
import com.aplana.timesheet.form.PlanEditForm;
import com.aplana.timesheet.form.validator.PlanEditFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.Calendar;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static argo.jdom.JsonNodeFactories.*;
import static com.aplana.timesheet.util.JsonUtil.aNumberBuilder;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class PlanEditController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private PlanEditService planEditService;

    @Autowired
    private PlanEditExcelReportService planEditExcelReportService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanEditController.class);

    public static final String JSON_DATA_YEAR = "year";
    public static final String JSON_DATA_MONTH = "month";

    public static final String PLAN_SAVE_URL = "/planSave";
    public static final String PLAN_EDIT_URL = "/planEdit";

    public static final String EXPORT_TABLE_EXCEL = "/exportTableExcel";

    public  static final String COOKIE_SELECTION_ROW = "cookie_selection_row";
    public  static final String COOKIE_SCROLL_X = "cookie_scroll_x";
    public  static final String COOKIE_SCROLL_Y = "cookie_scroll_y";

    @RequestMapping(PLAN_EDIT_URL)
    public ModelAndView showForm(
            @ModelAttribute(PlanEditForm.FORM) PlanEditForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            @ModelAttribute("selectionRowIndex") String selectionRowIndex,
            @ModelAttribute("scrollX") String scrollX,
            @ModelAttribute("scrollY") String scrollY
    ) {
        planEditService.initForm(form, request);

        planEditService.saveCookie(form, response);
        planEditService.deleteCookie(response);

        return planEditService.createModelAndView(form, bindingResult);
    }

    @RequestMapping(value = PLAN_EDIT_URL, method = RequestMethod.POST)
    public ModelAndView showTable(
            @ModelAttribute(PlanEditForm.FORM) PlanEditForm form,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        final ModelAndView modelAndView = planEditService.createModelAndView(form, bindingResult);

        if (!bindingResult.hasErrors()) {
            planEditService.saveCookie(form, response);
            planEditService.deleteCookie(response);
        }

        return modelAndView;
    }

    @RequestMapping(value = EXPORT_TABLE_EXCEL+"/{year}/{month}", method = RequestMethod.POST)
    public ModelAndView exportTableExcel(
            @ModelAttribute("year") Integer year,
            @ModelAttribute("month") Integer month,
            @ModelAttribute(PlanEditForm.FORM) PlanEditForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        final Date date = DateTimeUtil.createDate(year, month);

        List<Project> projectList = planEditService.getProjects(form, date);
        String dataAsJson = planEditService.getDataAsJson(form, date, projectList);

        com.aplana.timesheet.dao.entity.Calendar calDate = calendarService.find(new Timestamp(date.getTime()));

        String reportName = String.format("Планирование занятости за %s %s года", calDate.getMonthTxt(),year.toString());

        //разбирается JSON из за того что может поменяться логика формирования данных планирования
        planEditExcelReportService.createAndExportReport(reportName, dataAsJson, projectList, response, request);
        return null;
    }

    @RequestMapping(value = PLAN_SAVE_URL, method = RequestMethod.POST)
    public String save(
            @ModelAttribute(PlanEditForm.FORM) PlanEditForm form,
            HttpServletResponse response,
            HttpServletRequest request,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            final JsonRootNode rootNode = JsonUtil.parse(form.getJsonData());
            final Integer year = JsonUtil.getDecNumberValue(rootNode, JSON_DATA_YEAR);
            final Integer month = JsonUtil.getDecNumberValue(rootNode, JSON_DATA_MONTH);

            planEditService.savePlans(rootNode, year, month);

            redirectAttributes.addFlashAttribute("selectionRowIndex", PlanEditService.getCookie(request, COOKIE_SELECTION_ROW));
            redirectAttributes.addFlashAttribute("scrollX", PlanEditService.getCookie(request, COOKIE_SCROLL_X));
            redirectAttributes.addFlashAttribute("scrollY", PlanEditService.getCookie(request, COOKIE_SCROLL_Y));
        } catch (InvalidSyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        planEditService.saveCookie(form, response);
        return "redirect:" + PLAN_EDIT_URL;
    }
}
