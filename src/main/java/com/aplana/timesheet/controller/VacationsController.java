package com.aplana.timesheet.controller;

import argo.jdom.JsonNodeBuilders;
import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static com.aplana.timesheet.form.VacationsForm.VIEW_TABLE;
import static com.aplana.timesheet.system.constants.RoleConstants.ROLE_ADMIN;


/**
 * @author rshamsutdinov, aalikin
 * @version 1.1
 */
@Controller
public class VacationsController extends AbstractControllerForEmployee {

    private static final String VACATION_FORM = "vacationsForm";

    @Autowired
    private VacationsFormValidator vacationsFormValidator;
    @Autowired
    private VacationService vacationService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    MessageSource messageSource;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DictionaryItemService dictionaryItemService;

    private Employee getCurrentUser(){
        return session.getAttribute("employeeId") != null
                ? employeeService.find((Integer)session.getAttribute("employeeId"))
                : securityService.getSecurityPrincipal().getEmployee();
    }

    private ModelAndView getMavForDefaultView(VacationsForm vacationsForm){
        final ModelAndView modelAndView = createMAVForEmployeeWithDivisionAndManagerAndRegion(
                "vacations", vacationsForm.getEmployeeId(), vacationsForm.getDivisionId());
        modelAndView.addObject("projectId", vacationsForm.getProjectId() == null ? 0 : vacationsForm.getProjectId());
        modelAndView.addObject("regionList", regionService.getRegions());
        modelAndView.addObject("vacationNeedsApprovalCount",
                vacationService.findVacationsNeedsApprovalCount(getCurrentUser().getId()));

        return modelAndView;
    }

    @RequestMapping(value = "/vacations", method = RequestMethod.GET)
    public ModelAndView prepareToShowVacations(
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm)
    {
        Employee employee = getCurrentUser();
        vacationsForm.setDivisionId(employee.getDivision().getId());
        vacationsForm.setEmployeeId(employee.getId());
        vacationsForm.setCalToDate(DateTimeUtil.currentYearLastDay());
        vacationsForm.setCalFromDate(DateTimeUtil.currentMonthFirstDay());
        vacationsForm.setVacationType(0);
        vacationsForm.setRegions(new ArrayList<Integer>());
        vacationsForm.getRegions().add(VacationsForm.ALL_VALUE);
        vacationsForm.setViewMode(VIEW_TABLE);
        return getMavForDefaultView(vacationsForm);
    }

    @RequestMapping(value = "/vacations", method = RequestMethod.POST)
    public ModelAndView showVacations(
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm,
            BindingResult result,
            Locale locale
    ) {
        Date dateFrom = DateTimeUtil.parseStringToDateForDB(vacationsForm.getCalFromDate());
        Date dateTo = DateTimeUtil.parseStringToDateForDB(vacationsForm.getCalToDate());

        final ModelAndView modelAndView = getMavForDefaultView(vacationsForm);

        vacationsFormValidator.validate(vacationsForm, result);
        if (result != null && result.hasErrors()){
            return modelAndView;
        }

        // iziyangirov ToDo перенести в другой контроллер
        // удаление отпуска или согласования, результат операций вернется в result
        vacationService.deleteVacationOrApproval(result, vacationsForm);
        if (result != null && result.hasErrors()){
            return prepareToShowVacations(new VacationsForm());
        }

        final List<Vacation> vacations = vacationService.getVacationList(vacationsForm);
        List<Holiday> holidays = calendarService.getAllHolidaysInInterval(dateFrom, dateTo);
        Integer firstYear = DateTimeUtil.getYear(dateFrom);
        Integer lastYear = DateTimeUtil.getYear(dateTo);
        int summaryApproved = 0;
        int summaryRejected = 0;
        /* посчитаем количество одобреных и отклоненых отпусков */
        for (Vacation vacation : vacations){
            if (VacationStatusEnum.APPROVED.getId() == vacation.getStatus().getId()){
                summaryApproved++;
            }
            if (VacationStatusEnum.REJECTED.getId() == vacation.getStatus().getId()){
                summaryRejected++;
            }
        }

        final Map<Vacation,Integer> calDays  = vacationService.getCalDays(vacations);          // количество календарных дней для каждого отпуска
        final Map<Vacation,Integer> workDays = vacationService.getWorkDays(calDays, holidays); // количество рабочих дней для каждого отпуска

        //Получаем список отпусков, привязанные к типам отпусков
        final List<DictionaryItem> vacationTypes = vacationService.getVacationTypes(vacations);
        // получим итоги по годам в соответствии с типом отпуска
        final List<VacationInYear> calAndWorkDaysList =
                vacationService.getSummaryDaysCountByYearAndType(vacationTypes, firstYear, lastYear, calDays, workDays);

        modelAndView.addObject("vacationsList", vacations);
        modelAndView.addObject("vacationListByRegionJSON", vacationService.getVacationListByRegionJSON(dateFrom, dateTo, vacations));
        modelAndView.addObject("holidayList", vacationService.getHolidayListJSON(dateFrom, dateTo));
        modelAndView.addObject("vacationTypes", dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId()));
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);
        modelAndView.addObject("years", lastYear-firstYear+1);
        modelAndView.addObject("summaryApproved", summaryApproved);
        modelAndView.addObject("summaryRejected", summaryRejected);
        modelAndView.addObject("curEmployee", getCurrentUser());
        modelAndView.addObject("calDaysCount", calAndWorkDaysList);
        modelAndView.addObject(VacationsForm.MANAGER_ID, vacationsForm.getManagerId());
        modelAndView.addObject("vacationService", vacationService);
        return modelAndView;
    }

    @RequestMapping(value = "/vacations_needs_approval")
    public ModelAndView showVacationsNeedsApproval(
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm,
            BindingResult result)
    {

        // ToDo iziyangirov возможно стоит перенести это в отдельный метод контроллера, по другому адресу
        // и перенести в отдельный контроллер
        if (vacationsForm.getVacationId() != null) {
            try {
                vacationService.deleteVacation(vacationsForm.getVacationId());
                vacationsForm.setVacationId(null);
            } catch (DeleteVacationException ex) {
                result.rejectValue("vacationId", "error.vacations.deletevacation.failed", ex.getLocalizedMessage());
            }
        }

        Employee employee = securityService.getSecurityPrincipal().getEmployee();
        final ModelAndView modelAndView = new ModelAndView("vacationsNeedsApproval");
        modelAndView.addObject("curEmployee", securityService.getSecurityPrincipal().getEmployee());

        final List<Vacation> vacations = vacationService.findVacationsNeedsApproval(employee.getId());
        final Map<Vacation,Integer> calDays = new HashMap<Vacation, Integer>(vacations.size());
        final Map<Vacation,Integer> workDays = new HashMap<Vacation, Integer>(vacations.size());

        if (vacations != null && !vacations.isEmpty()){
            Date minDate = vacations.get(0).getBeginDate();
            Date maxDate = vacations.get(0).getEndDate();
            for (Vacation vacation : vacations){
                if (maxDate.before(vacation.getEndDate())){
                    maxDate = vacation.getEndDate();
                }
                if (minDate.after(vacation.getBeginDate())){
                    minDate = vacation.getBeginDate();
                }
            }

            List<Holiday> holidays = calendarService.getAllHolidaysInInterval(minDate, maxDate);
            calDays.putAll(vacationService.getCalDays(vacations));
            workDays.putAll(vacationService.getWorkDays(calDays, holidays));
        }

        modelAndView.addObject("vacationsList", Lists.reverse(vacations));
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);

        return modelAndView;
    }

    /**
     * Возвращает количество неутвержденных заявлений на отпуск в виде строк '(X)'
     */
    @RequestMapping(value = "/vacations/count", headers = "Accept=text/plain;Charset=UTF-8")
    @ResponseBody
    public String getVacationsCount() {
        Employee employee = securityService.getSecurityPrincipal().getEmployee();
        Integer vacationsNeedsApprovalCount = vacationService.findVacationsNeedsApprovalCount(employee.getId());
        return vacationsNeedsApprovalCount > 0 ? "("+vacationsNeedsApprovalCount+")" : "";
    }

    /**
     * Возвращает JSON список сотрудников по условиям заданным на форме
     */
    @RequestMapping(value = "/vacations/getEmployeeList", headers = "Accept=text/plain;Charset=UTF-8")
    @ResponseBody
    public String getEmployeeList(@ModelAttribute(VACATION_FORM) VacationsForm vacationsForm) {

        List<Employee> employeeList = employeeService.getEmployees(
                employeeService.createDivisionList(vacationsForm.getDivisionId()),
                employeeService.createManagerList(vacationsForm.getManagerId()),
                employeeService.createRegionsList(vacationsForm.getRegions()),
                employeeService.createProjectList(vacationsForm.getProjectId()),
                DateTimeUtil.parseStringToDateForDB(vacationsForm.getCalFromDate()),
                DateTimeUtil.parseStringToDateForDB(vacationsForm.getCalToDate()),
                true
        );
        return employeeHelper.makeEmployeeListInJSON(employeeList);
    }

    /**
     * Проверяет есть ли у сотрудника в этот день отпуск
     */
    @RequestMapping(value = "/vacations/checkDate", method = RequestMethod.GET)
    @ResponseBody
    public String checkDayIsVacation(@RequestParam("employeeId") Integer employeeId,
                                     @RequestParam("date") String date) {

        return employeeService.checkDayIsVacation(employeeId, date);
    }

    @RequestMapping(value = "/approveVacation", method = RequestMethod.GET, headers = "Accept=text/plain;Charset=UTF-8")
    @ResponseBody
    public String approveVacation(@RequestParam("vacationId") Integer vacationId) {
        Vacation vacation = vacationService.findVacation(vacationId);
        Boolean canApprove = vacationService.isVacationApprovePermission(vacation);
        if (request.isUserInRole(ROLE_ADMIN) || canApprove) {
            return vacationService.approveVacation(vacationId);
        } else {
            return JsonUtil.format(anObjectBuilder().
                    withField("isApproved", JsonNodeBuilders.aFalseBuilder())
                    .withField("message", JsonNodeBuilders.aStringBuilder("Недостаточно прав для выполнения данной операции")));
        }
    }

}
