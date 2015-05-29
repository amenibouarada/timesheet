package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.QuickReportTypesEnum;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessControllerException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.EnumsUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: vsergeev
 * Date: 17.01.13
 */
@Controller
public class BusinessTripsAndIllnessController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessTripsAndIllnessController.class);

    @Autowired
    SecurityService securityService;

    @Autowired
    EmployeeService employeeService;

    @Autowired
    DivisionService divisionService;

    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    CalendarService calendarService;

    @Autowired
    DictionaryItemService dictionaryItemService;

    @Autowired
    BusinessTripService businessTripService;

    @Autowired
    IllnessService illnessService;

    @Autowired
    BusinessTripsAndIllnessService businessTripsAndIllnessService;

    @RequestMapping(value = "/businesstripsandillness/delete/{reportId}/{reportType}", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteReport(@PathVariable("reportId") Integer reportId,
                               @PathVariable("reportType") Integer reportType) {
        try {
            final QuickReportTypesEnum reportTypeAsEnum = EnumsUtils.getEnumById(reportType, QuickReportTypesEnum.class);
            switch (reportTypeAsEnum) {
                case BUSINESS_TRIP:
                    return businessTripsAndIllnessService.deleteBusinessTrip(reportId);
                case ILLNESS:
                    return businessTripsAndIllnessService.deleteIllness(reportId);
                default:
                    return "Удаление такого типа отчетов еще не реализовано!";
            }
        } catch (NoSuchElementException ex) {
            logger.error("Неизвестный тип отчета!", ex);
            return ("Ошибка при удалении: неизвестный тип отчета!");
        } catch (BusinessTripsAndIllnessControllerException ex) {
            return ex.getMessage();
        }
    }

    @RequestMapping(value = "/businesstripsandillness")
    public String showBusinessTripsAndIllnessDefaultCall() {
        Employee currentUser = securityService.getSecurityPrincipal().getEmployee();
        Integer divisionId = currentUser.getDivision().getId();
        Integer employeeId = currentUser.getId();

        return String.format("redirect:/businesstripsandillness/%s/%s", divisionId, employeeId);
    }

    @RequestMapping(value = "/businesstripsandillness/{divisionId}/{employeeId}")
    public ModelAndView showDefaultIllnessReport(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm tsForm
    ) throws BusinessTripsAndIllnessControllerException {
        Integer printtype = tsForm.getReportType();
        Integer manager = tsForm.getManagerId();
        List<Integer> regions = tsForm.getRegions();
        Date dateFrom = tsForm.getDateFrom();
        Date dateTo = tsForm.getDateTo();
        if (dateFrom == null || dateTo == null) {
            dateTo = new Date();
            tsForm.setDateTo(dateTo);
            Integer month = calendarService.getMonthFromDate(dateTo);
            Integer year = calendarService.getYearFromDate(dateTo);
            dateFrom = calendarService.getMinDateMonth(year,month);
            tsForm.setDateFrom(dateFrom);
        }
        return businessTripsAndIllnessService.getBusinessTripsOrIllnessReport(divisionId, regions, employeeId, manager, dateFrom, dateTo, printtype, tsForm, employeeService.isShowAll(request));
    }

    @RequestMapping(value = "/businesstripsandillness/businesstrip/{employeeId}")
    public ModelAndView showBusinessTrips(
            @PathVariable("employeeId") Integer employeeId,
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm businessTripsAndIllnessForm)
            throws BusinessTripsAndIllnessControllerException {
        Date dateFrom = businessTripsAndIllnessForm.getDateFrom();
        Date dateTo = businessTripsAndIllnessForm.getDateTo();
        if (dateFrom == null || dateTo == null) {
            dateFrom = new Date();
            businessTripsAndIllnessForm.setDateFrom(dateFrom);
            dateTo = DateUtils.addMonths(dateFrom, 1);
            businessTripsAndIllnessForm.setDateTo(dateTo);
        }
        return businessTripsAndIllnessService.getBusinessTripsOrIllnessReport(employeeId, QuickReportTypesEnum.BUSINESS_TRIP.getId(), employeeService.isShowAll(request));
    }

    @RequestMapping(value = "/businesstripsandillness/illness/{employeeId}")
    public ModelAndView showIllness(
            @PathVariable("employeeId") Integer employeeId,
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm businessTripsAndIllnessForm)
            throws BusinessTripsAndIllnessControllerException {
        Date dateFrom = businessTripsAndIllnessForm.getDateFrom();
        Date dateTo = businessTripsAndIllnessForm.getDateTo();
        if (dateFrom == null || dateTo == null) {
            dateFrom = new Date();
            businessTripsAndIllnessForm.setDateFrom(dateFrom);
            dateTo = DateUtils.addMonths(dateFrom, 1);
            businessTripsAndIllnessForm.setDateTo(dateTo);
        }
        return businessTripsAndIllnessService.getBusinessTripsOrIllnessReport(employeeId, QuickReportTypesEnum.ILLNESS.getId(), employeeService.isShowAll(request));
    }

    @RequestMapping(value = "/businesstripsandillness/{divisionId}/{employeeId}/{reportTypeId}")
    public ModelAndView showBusinessTripsAndIllnessWithResult(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @PathVariable("reportTypeId") Integer reportTypeId,
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm tsForm
    ) throws BusinessTripsAndIllnessControllerException {
        tsForm.setReportType(reportTypeId);
        return showDefaultIllnessReport(divisionId, employeeId, tsForm);
    }

}