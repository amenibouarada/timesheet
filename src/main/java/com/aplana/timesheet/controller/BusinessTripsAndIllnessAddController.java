package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.BusinessTrip;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.enums.BusinessTripTypesEnum;
import com.aplana.timesheet.enums.QuickReportTypesEnum;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessAddException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessAddForm;
import com.aplana.timesheet.form.validator.BusinessTripsAndIllnessAddFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.service.helper.EmployeeHelper;
import com.aplana.timesheet.util.EnumsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: vsergeev
 * Date: 25.01.13
 */
@Controller
public class BusinessTripsAndIllnessAddController extends AbstractController {

    public static final String ERROR_BUSINESS_TRIP_FIND = "Ошибка при получении отчета из БД!";

    @Autowired
    DictionaryItemService dictionaryItemService;
    @Autowired
    BusinessTripService businessTripService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    IllnessService illnessService;
    @Autowired
    ProjectService projectService;
    @Autowired
    BusinessTripsAndIllnessAddFormValidator businessTripsAndIllnessAddFormValidator;
    @Autowired
    EmployeeHelper employeeHelper;


    private static final Logger logger = LoggerFactory.getLogger(BusinessTripsAndIllnessController.class);

    /**
     * Возвращает форму с заполненными данными для редактирования отчетов
     */
    @RequestMapping(value = "/businesstripsandillnessadd/{reportId}/{reportFormed}")
    public ModelAndView editBusinessTripOrIllness(@PathVariable("reportId") Integer reportId,
                                                  @PathVariable("reportFormed") Integer reportFormed,
                                                  @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
                                                  BindingResult result) throws BusinessTripsAndIllnessAddException {
        QuickReportTypesEnum reportType = getReportTypeAsEnum(reportFormed);
        switch (reportType) {
            case ILLNESS:
                return getIllnessEditingForm(reportId, tsForm);
            case BUSINESS_TRIP:
                return getBusinessTripEditingForm(reportId, tsForm);
            default:
                throw new BusinessTripsAndIllnessAddException("Редактирование отчетов такого типа пока не реализовано!");
        }
    }

    /**
     * возвращает форму для создания больничного/командировки
     */
    @RequestMapping(value = "/businesstripsandillnessadd/{employeeId}")
    public ModelAndView showCreateBusinessTripOrIllnessForm(
            @PathVariable("employeeId") Integer employeeId,
            @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
            BindingResult result) {
        Employee employee = employeeService.find(employeeId);
        tsForm.setBeginDate(new Date());
        tsForm.setEndDate(new Date());
        return getModelAndViewCreation(employee);
    }

    @RequestMapping(value = "/businesstripsandillnessadd/")
    public ModelAndView showCreateBusinessTripOrIllnessForm() {
        return new ModelAndView(String.format("redirect:/businesstripsandillnessadd/-1"));
    }

    /**
     * пытаемя добавить новый больничный/командировку
     */
    @RequestMapping(value = "/businesstripsandillnessadd/tryAdd/{employeeId}")
    public ModelAndView validateAndAddBusinessTripOrIllness(
            @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
            BindingResult result,
            @PathVariable("employeeId") Integer employeeId,
            RedirectAttributes redirectAttributes) throws BusinessTripsAndIllnessAddException {
        redirectAttributes.addAttribute("back", "1");
        Employee employee = employeeService.find(employeeId);
        tsForm.setEmployee(employee);
        businessTripsAndIllnessAddFormValidator.validate(tsForm, result);

        if (result.hasErrors()) {
            return getModelAndViewCreation(employee);
        }

        QuickReportTypesEnum reportType = getReportTypeAsEnum(tsForm.getReportType());
        tsForm.setEmployee(employee);

        switch (reportType) {
            case BUSINESS_TRIP: {
                BusinessTrip businessTrip = businessTripService.addBusinessTrip(tsForm);
                return getModelAndViewSuccess(businessTrip.getEmployee(), businessTrip.getBeginDate());
            }
            case ILLNESS: {
                Illness illness = illnessService.addIllness(tsForm);
                return getModelAndViewSuccess(illness.getEmployee(), illness.getBeginDate());
            }
            default:
                throw new BusinessTripsAndIllnessAddException("Сохранение данных для такого типа отчета не реализовано!");
        }
    }

    /**
     * сохраняем измененный больничный/командировку
     */
    @RequestMapping(value = "/businesstripsandillnessadd/trySave/{reportId}")
    public ModelAndView validateAndSaveBusinessTripOrIllness(
            @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
            BindingResult result,
            @PathVariable("reportId") Integer reportId,
            RedirectAttributes redirectAttributes) throws BusinessTripsAndIllnessAddException {
        redirectAttributes.addAttribute("back", "1");
        tsForm.setReportId(reportId);
        businessTripsAndIllnessAddFormValidator.validate(tsForm, result);
        if (result.hasErrors()) {
            return getModelAndViewCreation(tsForm.getEmployee());
        }

        QuickReportTypesEnum reportType = getReportTypeAsEnum(tsForm.getReportType());

        switch (reportType) {
            case BUSINESS_TRIP:
                BusinessTrip businessTrip = businessTripService.saveBusinessTrip(tsForm, reportId);
                return getModelAndViewSuccess(businessTrip.getEmployee(), businessTrip.getBeginDate());
            case ILLNESS: {
                Illness illness = illnessService.saveIllness(tsForm, reportId);
                return getModelAndViewSuccess(illness.getEmployee(), illness.getEndDate());
            }
            default:
                throw new BusinessTripsAndIllnessAddException("Редактирование данных для такого типа отчета пока не реализовано!");
        }
    }

    @RequestMapping(value = "/businesstripsandillnessadd/resultsuccess")
    public ModelAndView businessTripOrIllnessAddedResultSuccess() {
        ModelAndView modelAndView = new ModelAndView("businesstripsandillnessaddresult");
        modelAndView.addObject("result", 1);

        return modelAndView;
    }

    @RequestMapping(value = "/businesstripsandillnessadd/resultfailed")
    public ModelAndView businessTripOrIllnessAddedResultError() {
        ModelAndView modelAndView = new ModelAndView("businesstripsandillnessaddresult");
        modelAndView.addObject("result", 0);
        modelAndView.addObject("errorMsg", "Произошла ошибка при сохранении данных на стороне сервиса.");

        return modelAndView;
    }

    @RequestMapping(value = "/businesstripsandillnessadd/getprojects", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getProjects() {
        List<Project> projects = projectService.getAllProjects();
        return projectService.getProjectListAsJson(projects);
    }

    /**
     * возвращаем enum отчета по id. если такого id в emun-е не существует - бросаем exception
     */
    private QuickReportTypesEnum getReportTypeAsEnum(Integer reportId) throws BusinessTripsAndIllnessAddException {
        try {
            return EnumsUtils.getEnumById(reportId, QuickReportTypesEnum.class);
        } catch (NoSuchElementException ex) {
            throw new BusinessTripsAndIllnessAddException("Операция не поддерживается для данного типа отчета!", ex);
        }
    }

    /**
     * Возвращает формочку с табличкой по больничным или командировкам выбранного сотрудника за выбранный месяц и
     * результат о выполнении операции
     */
    private ModelAndView getModelAndViewSuccess(Employee employee, Date reportDate) {
        Integer divisionId = employee.getDivision().getId();
        Integer employeeId = employee.getId();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(reportDate);

        return new ModelAndView(String.format("redirect:/businesstripsandillness/%s/%s", divisionId, employeeId));
    }

    /**
     * заполняем форму для редактирования командировки и возвращаем пользователю
     */
    private ModelAndView getBusinessTripEditingForm(Integer reportId, BusinessTripsAndIllnessAddForm tsForm) throws BusinessTripsAndIllnessAddException {
        BusinessTrip businessTrip;
        try {
            businessTrip = businessTripService.find(reportId);
            tsForm.setReportType(QuickReportTypesEnum.BUSINESS_TRIP.getId());
            tsForm.setBeginDate(businessTrip.getBeginDate());
            tsForm.setEndDate(businessTrip.getEndDate());
            tsForm.setEmployee(businessTrip.getEmployee());
            tsForm.setBusinessTripType(businessTrip.getType().getId());
            if (businessTrip.getType().getId().equals(BusinessTripTypesEnum.PROJECT.getId())) {
                tsForm.setProjectId(businessTrip.getProject().getId());
            }
            tsForm.setComment(businessTrip.getComment());

            return getModelAndViewEditing(businessTrip.getEmployee(), businessTrip.getId());
        } catch (Exception e) {
            logger.error(ERROR_BUSINESS_TRIP_FIND, e);
            throw new BusinessTripsAndIllnessAddException(ERROR_BUSINESS_TRIP_FIND);
        }
    }

    /**
     * заполняем форму для редактирования больничного и возвращаем пользователю
     */
    private ModelAndView getIllnessEditingForm(Integer reportId, BusinessTripsAndIllnessAddForm tsForm) {
        Illness illness = illnessService.find(reportId);
        tsForm.setReportType(QuickReportTypesEnum.ILLNESS.getId());
        tsForm.setEmployee(illness.getEmployee());
        tsForm.setBeginDate(illness.getBeginDate());
        tsForm.setEndDate(illness.getEndDate());
        tsForm.setReason(illness.getReason().getId());
        tsForm.setComment(illness.getComment());

        return getModelAndViewEditing(illness.getEmployee(), illness.getId());
    }

    /**
     * возвращает форму для редактирования
     */
    private ModelAndView getModelAndViewEditing(Employee employee, Integer reportId) {
        ModelAndView modelAndView = getModelAndViewCreation(employee);
        modelAndView.addObject("reportId", reportId);

        return modelAndView;
    }

    /**
     * возвращаем форму для создания
     */
    private ModelAndView getModelAndViewCreation(Employee employee) {
        ModelAndView modelAndView = new ModelAndView("businesstripsandillnessadd");
        if (employee != null) modelAndView.addObject("employeeId", employee.getId());
        modelAndView.addObject("employeeList", employeeHelper.makeEmployeeListInJSON(employeeService.getEmployees()));
        return modelAndView;
    }


}
