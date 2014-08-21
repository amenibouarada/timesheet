package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.CreateVacationForm;
import com.aplana.timesheet.form.validator.CreateVacationFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.service.helper.EmployeeHelper;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.List;

import static com.aplana.timesheet.system.constants.RoleConstants.ROLE_ADMIN;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class CreateVacationController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessTripsAndIllnessController.class);

    private static final String CREATE_VACATION_FORM = "createVacationForm";

    @Autowired
    private CreateVacationFormValidator createVacationFormValidator;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private VacationService vacationService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    protected EmployeeHelper employeeHelper;
    @Autowired
    protected SendMailService sendMailService;
    @Autowired
    protected HttpServletRequest request;

    @RequestMapping(value = "/createVacation", method = RequestMethod.GET)
    public String prepareToCreateVacation() {
        final Employee employee = securityService.getSecurityPrincipal().getEmployee();

        return String.format(
                "redirect:/createVacation/%s",
                employee.getId()
        );
    }

    @RequestMapping("/createVacation/{employeeId}")
    public ModelAndView createVacation(
            @PathVariable("employeeId") Integer employeeId,
            @ModelAttribute(CREATE_VACATION_FORM) CreateVacationForm createVacationForm,
            BindingResult result
    ) {
        final Employee employee = (employeeId != null && employeeService.find(employeeId) != null)
                ? employeeService.find(employeeId)
                : securityService.getSecurityPrincipal().getEmployee();

        final Calendar calendar = getCalendar(new Timestamp(java.util.Calendar.getInstance().getTimeInMillis()));
        final Timestamp nextWorkDay = calendarService.getNextWorkDay(calendar,
                employeeService.find(employee.getId()).getRegion()).getCalDate(); //При выборе текущего сотрудника, поле Регион незаполнено

        createVacationForm.setDivisionId(employee.getDivision().getId());
        createVacationForm.setCalFromDate(DateTimeUtil.formatDateIntoDBFormat(nextWorkDay));
        createVacationForm.setCalToDate(DateTimeUtil.formatDateIntoDBFormat(nextWorkDay));
        createVacationForm.setEmployeeId(employee.getId());

        return getModelAndView(employee);
    }

    @RequestMapping(value = "/getExitToWorkAndCountVacationDay",  produces = "text/plain;Charset=UTF-8")
    @ResponseBody
    public String getExitToWorkAndCountVacationDay(
                                           @RequestParam("beginDate") String beginDate,
                                           @RequestParam("endDate") String endDate,
                                           @RequestParam("employeeId") Integer employeeId,
                                           @RequestParam("vacationTypeId") Integer vacationTypeId
    ) {
        return vacationService.getExitToWorkAndCountVacationDayJson(beginDate, endDate, employeeId, vacationTypeId);
    }

    @RequestMapping(value = "/validateAndCreateVacation/{employeeId}/{approved}", method = RequestMethod.POST)
    public ModelAndView validateAndCreateVacation(
            @PathVariable("employeeId") Integer employeeId,
            @PathVariable("approved") Integer approved,
            @ModelAttribute(CREATE_VACATION_FORM) CreateVacationForm createVacationForm,
            BindingResult bindingResult
    ) throws VacationApprovalServiceException {
        final Employee employee = employeeService.find(employeeId);
        final Employee curEmployee = securityService.getSecurityPrincipal().getEmployee();
        final boolean isApprovedVacation =
                (employeeService.isEmployeeAdmin(curEmployee.getId()) && BooleanUtils.toBoolean(approved));

        createVacationFormValidator.validate(createVacationForm, bindingResult, isApprovedVacation);

        if (bindingResult.hasErrors()) {
            return getModelAndView(employee);
        }

        vacationService.createAndMailVacation(createVacationForm, employee, curEmployee, isApprovedVacation);

        HttpSession session = request.getSession(false);
        session.setAttribute("employeeId", employeeId);
        return new ModelAndView("redirect:../../vacations");
    }

    @RequestMapping(value = "/validateAndCreateVacation", method = RequestMethod.GET)
    public String validateAndCreateVacation(
    ) {
        return "redirect:/vacations";
    }


    private ModelAndView getModelAndView(Employee employee) {
        final ModelAndView modelAndView = new ModelAndView("createVacation");

        List<Division> divisionList = divisionService.getDivisions();

        List<DictionaryItem> itemsByDictionaryId = dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId());
        if (!request.isUserInRole(ROLE_ADMIN)) {
            itemsByDictionaryId.remove(dictionaryItemService.find(VacationTypesEnum.CHILDBEARING.getId()));
            itemsByDictionaryId.remove(dictionaryItemService.find(VacationTypesEnum.CHILDCARE.getId()));
        }
        modelAndView.addObject("vacationTypes", itemsByDictionaryId);

        modelAndView.addObject("employee", employee);
        modelAndView.addObject("divisionId", employee.getDivision().getId());
        modelAndView.addObject("employeeId", employee.getId());
        modelAndView.addObject("divisionList", divisionList);
        modelAndView.addObject("employeeListJson",
                employeeHelper.getEmployeeListWithDivisionAndManagerAndRegionJson(divisionList, employeeService.isShowAll(request)));
        modelAndView.addObject("typeWithRequiredComment", CreateVacationFormValidator.TYPE_WITH_REQUIRED_COMMENT);
        modelAndView.addObject("typeVacationPlanned", VacationTypesEnum.PLANNED.getId());

        return modelAndView;
    }

    private Calendar getCalendar(Timestamp date) {
        final Calendar calendar = new Calendar();

        calendar.setCalDate(date);

        return calendar;
    }
}
