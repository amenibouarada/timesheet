package com.aplana.timesheet.controller;

import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.google.common.collect.Lists;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import padeg.lib.Padeg;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.Calendar;

import static com.aplana.timesheet.form.VacationsForm.ALL_VALUE;
import static com.aplana.timesheet.form.VacationsForm.VIEW_TABLE;


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
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private VacationApprovalService vacationApprovalService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    MessageSource messageSource;
    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "/vacations", method = RequestMethod.GET)
    public ModelAndView prepareToShowVacations(
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm
    ) {
        HttpSession session = request.getSession(false);
        Employee employee = session.getAttribute("employeeId") != null
                ? employeeService.find((Integer)session.getAttribute("employeeId"))
                : securityService.getSecurityPrincipal().getEmployee();
        vacationsForm.setDivisionId(employee.getDivision().getId());
        vacationsForm.setEmployeeId(employee.getId());
        vacationsForm.setCalToDate(DateTimeUtil.currentYearLastDay());
        vacationsForm.setCalFromDate(DateTimeUtil.currentMonthFirstDay());
        vacationsForm.setVacationType(0);
        vacationsForm.setRegions(new ArrayList<Integer>());
        // APLANATS-867
        vacationsForm.getRegions().add(VacationsForm.ALL_VALUE);
        vacationsForm.setViewMode(VIEW_TABLE);
        return showVacations(vacationsForm, null);
    }

    @RequestMapping(value = "/vacations", method = RequestMethod.POST)
    public ModelAndView showVacations(
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm,
            BindingResult result
    ) {
        Integer divisionId = vacationsForm.getDivisionId();
        Integer employeeId = vacationsForm.getEmployeeId();
        Date dateFrom = DateTimeUtil.stringToDate(vacationsForm.getCalFromDate(), DateTimeUtil.DATE_PATTERN);
        Date dateTo = DateTimeUtil.stringToDate(vacationsForm.getCalToDate(), DateTimeUtil.DATE_PATTERN);
        Integer projectId = vacationsForm.getProjectId();
        Integer managerId = vacationsForm.getManagerId();
        List<Integer> regions = vacationsForm.getRegions();
        final Integer vacationId = vacationsForm.getVacationId();
        final Integer approverId = vacationsForm.getApprovalId();

        vacationsFormValidator.validate(vacationsForm, result);

        if (vacationId != null) {
            try {
                vacationService.deleteVacation(vacationId);
                vacationsForm.setVacationId(null);
            } catch (DeleteVacationException ex) {
                result.rejectValue("vacationId", "error.vacations.deletevacation.failed", ex.getLocalizedMessage());
            }
        }

        if (vacationId == null && approverId != null) {
            try {
                vacationApprovalService.deleteVacationApprovalByIdAndCheckIsApproved(approverId);
                vacationsForm.setApprovalId(null);
            } catch (VacationApprovalServiceException e) {
                result.rejectValue("approvalID", "error.vacations.deletevacation.failed", e.getLocalizedMessage());
            }
        }

        if (result != null && result.hasErrors()){
            return prepareToShowVacations(new VacationsForm());
        }

        DictionaryItem vacationType = vacationsForm.getVacationType() != 0 ?
                dictionaryItemService.find(vacationsForm.getVacationType()) : null;

        final List<Vacation> vacations;
        if (employeeId != null && employeeId != ALL_VALUE){
            vacations = vacationService.findVacations(employeeId, dateFrom, dateTo, vacationType);
        } else {
            List<Employee> employees = employeeService.getEmployees(
                    employeeService.createDivisionList(divisionId),
                    employeeService.createManagerList(managerId),
                    employeeService.createRegionsList(regions),
                    employeeService.createProjectList(projectId),
                    dateFrom,
                    dateTo,
                    true
            );
            vacations = vacationService.findVacations(employees, dateFrom, dateTo, vacationType);
        }

        final ModelAndView modelAndView = createMAVForEmployeeWithDivisionAndManagerAndRegion("vacations", employeeId, divisionId);

        final int vacationsSize = vacations.size();

        final Map<Vacation,Integer> calDays = new HashMap<Vacation, Integer>(vacationsSize);
        final Map<Vacation,Integer> workDays = new HashMap<Vacation, Integer>(vacationsSize);

        modelAndView.addObject("projectId", vacationsForm.getProjectId() == null ? 0 : vacationsForm.getProjectId());
        modelAndView.addObject("regionList", getRegionList());
        modelAndView.addObject("calFromDate", dateFrom);
        modelAndView.addObject("calToDate", dateTo);
        modelAndView.addObject("vacationsList", Lists.reverse(vacations));
        modelAndView.addObject("vacationListByRegionJSON", vacationService.getVacationListByRegionJSON(dateFrom, dateTo,vacations));
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);
        modelAndView.addObject("holidayList", vacationService.getHolidayListJSON(dateFrom, dateTo));
        modelAndView.addObject("vacationTypes",
                dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId()));
        TimeSheetUser timeSheetUser = securityService.getSecurityPrincipal();
        Integer vacationsNeedsApprovalCount = 0;
        if (timeSheetUser!=null && timeSheetUser.getEmployee()!=null) {
            vacationsNeedsApprovalCount = vacationService.findVacationsNeedsApprovalCount(timeSheetUser.getEmployee().getId());
        }
        modelAndView.addObject("vacationNeedsApprovalCount", vacationsNeedsApprovalCount);
        String approvalPart=null;
        if (vacationsNeedsApprovalCount<5 && vacationsNeedsApprovalCount>0) {
                approvalPart = messageSource.getMessage("title.approval.part",null,null);
        } else {
                approvalPart = messageSource.getMessage("title.approval.parts", null, null);
        }
        if(approvalPart!=null && vacationsNeedsApprovalCount!=1){
            approvalPart = Padeg.getOfficePadeg(approvalPart, PadegConstants.Roditelnyy);
        }
        modelAndView.addObject("approvalPart", approvalPart);
        List<Region> regionListForCalc = new ArrayList<Region>();
        List<Integer> filledRegionsId = vacationsForm.getRegions().get(0).equals(ALL_VALUE)
                ? getRegionIdList()
                : vacationsForm.getRegions();
        for (Integer i : filledRegionsId){
            regionListForCalc.add(regionService.find(i));
        }

        final List<VacationInYear> calAndWorkDaysList = new ArrayList<VacationInYear>();

        Integer firstYear = DateTimeUtil.getYear(dateFrom);
        Integer lastYear = DateTimeUtil.getYear(dateTo);
        int summaryApproved = 0;
        int summaryRejected = 0;

        //Получаем списки, привязанные к типам отпусков
        Map<DictionaryItem,List<Vacation>> typedVacationMap = vacationService.splitVacationByTypes(vacations);
        //Проходим по существующим типам отпусков
        for(DictionaryItem item:typedVacationMap.keySet()){
            for (int i  = firstYear; i <= lastYear; i++){
                //Заполняются calDays, workDays
                getSummaryAndCalcDays(regionListForCalc, vacations, calDays, workDays, i);//TODO возможно упростить, сделать вместо двух вызовов один
                Map<String, Integer> map = getSummaryAndCalcDays(regionListForCalc, typedVacationMap.get(item), new HashMap<Vacation, Integer>(vacationsSize), new HashMap<Vacation, Integer>(vacationsSize), i);
                summaryApproved += map.get("summaryApproved");
                summaryRejected += map.get("summaryRejected");
                calAndWorkDaysList.add(new VacationInYear(item.getValue(),i, map.get("summaryCalDays"), map.get("summaryWorkDays")));
            }
        }
        modelAndView.addObject("years", lastYear-firstYear+1);
        modelAndView.addObject("summaryApproved", summaryApproved);
        modelAndView.addObject("summaryRejected", summaryRejected);
        modelAndView.addObject("curEmployee", securityService.getSecurityPrincipal().getEmployee());

        modelAndView.addObject("calDaysCount", calAndWorkDaysList);
        modelAndView.addObject(VacationsForm.MANAGER_ID, vacationsForm.getManagerId());
        modelAndView.addObject("vacationService", vacationService);
        return modelAndView;
    }

    private Map<String, Integer> getSummaryAndCalcDays(List<Region> regions, List<Vacation> vacations,
                                                       Map<Vacation,Integer> calDays,
                                                       Map<Vacation,Integer> workDays, int year
    ) {

        int summaryApproved = 0;
        int summaryRejected = 0;
        int summaryCalDays = 0;
        int summaryWorkDays = 0;

        if (!vacations.isEmpty()) {
            for (Region reg : regions){
                List<Vacation> differRegionVacations = new ArrayList<Vacation>();
                for (Vacation vac : vacations){
                    if (vac.getEmployee().getRegion().equals(reg)){
                        differRegionVacations.add(vac);
                    }
                }
                final int vacationsSize = differRegionVacations.size();
                if (!differRegionVacations.isEmpty()){
                    final Vacation firstVacation = differRegionVacations.get(0);
                    Date minDate = firstVacation.getBeginDate();
                    Date maxDate = firstVacation.getEndDate();

                    Date beginDate, endDate;
                    for (Vacation vacation : differRegionVacations) {
                        beginDate = vacation.getBeginDate();
                        endDate = vacation.getEndDate();

                        if (minDate.after(beginDate)) {
                            minDate = beginDate;
                        }

                        if (maxDate.before(endDate)) {
                            maxDate = endDate;
                        }

                        Integer diffInDays = getDiffInDays(beginDate, endDate);
                        calDays.put(vacation, diffInDays);
                    }

                    final List<Holiday> holidaysForRegion =
                            calendarService.getHolidaysForRegion(minDate, maxDate, reg);
                    final Calendar calendar = Calendar.getInstance();

                    calendar.set(Calendar.YEAR, year);
                    final Date currentYearBeginDate = DateUtils.truncate(calendar.getTime(), Calendar.YEAR);

                    calendar.setTime(currentYearBeginDate);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    calendar.set(Calendar.YEAR, year);

                    for (int i = 0; i < vacationsSize; i++) {
                        final Vacation vacation = differRegionVacations.get(i);
                        final int holidaysCount = vacationService.getHolidaysCount(holidaysForRegion, vacation.getBeginDate(), vacation.getEndDate());

                        final int calDaysCount = calDays.get(vacation);
                        final int workDaysCount = calDaysCount - holidaysCount;
                        workDays.put(vacation,workDaysCount);

                        final VacationStatusEnum vacationStatus =
                                EnumsUtils.getEnumById(vacation.getStatus().getId(), VacationStatusEnum.class);

                        if (vacationStatus == VacationStatusEnum.APPROVED) {
                            beginDate = vacation.getBeginDate();
                            endDate = vacation.getEndDate();

                            calendar.setTime(beginDate);
                            final int beginYear = calendar.get(Calendar.YEAR);

                            calendar.setTime(endDate);
                            final int endYear = calendar.get(Calendar.YEAR);

                            if (beginYear == year && year == endYear) {
                                summaryCalDays += calDaysCount;
                                summaryWorkDays += workDaysCount;
                            } if (beginYear < year && year == endYear){
                                long days = DateUtils.getFragmentInDays(endDate, Calendar.YEAR);
                                summaryCalDays += days;
                                summaryWorkDays += days - vacationService.getHolidaysCount(holidaysForRegion, currentYearBeginDate, endDate);
                            } if (beginYear == year && year < endYear){
                                long days = DateUtils.getFragmentInDays(beginDate, Calendar.YEAR);
                                summaryCalDays += days;
                                summaryWorkDays += days - vacationService.getHolidaysCount(holidaysForRegion, currentYearBeginDate, beginDate);
                            }
                            /* посчитаем количество одобрений/отклонений */
                            if ( vacationStatusInThisYear(vacation, year) )
                                summaryApproved++;
                        }

                        if (vacationStatus == VacationStatusEnum.REJECTED) {
                            /* посчитаем количество одобрений/отклонений */
                            if ( vacationStatusInThisYear(vacation, year) )
                                summaryRejected++;
                        }
                    }
                }
            }
        }

        final Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("summaryApproved", summaryApproved);
        map.put("summaryRejected", summaryRejected);
        map.put("summaryCalDays", summaryCalDays);
        map.put("summaryWorkDays", summaryWorkDays);

        return map;
    }

    /* определяет по дате началу отпуска принадлежность году */
    private Boolean vacationStatusInThisYear(Vacation vacation, int year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(vacation.getBeginDate());
        return cal.get(Calendar.YEAR) == year;
    }

    private int getDiffInDays(Date beginDate, Date endDate) {
        return (int) ((endDate.getTime() - beginDate.getTime()) / (24 * 3600 * 1000) + 1);
    }

    private List<Region> getRegionList() {
        return regionService.getRegions();
    }

    private List<Integer> getRegionIdList(){
        List<Integer> regionsIdList = new ArrayList<Integer>();
        for (Region region : getRegionList()){
            regionsIdList.add(region.getId());
        }
        return regionsIdList;
    }

    @RequestMapping(value = "/vacations_needs_approval")
    public ModelAndView showVacationsNeedsApproval(
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm,
            BindingResult result) {

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
        final int vacationsSize = vacations.size();

        final Map<Vacation,Integer> calDays = new HashMap<Vacation, Integer>(vacationsSize);
        final Map<Vacation,Integer> workDays = new HashMap<Vacation, Integer>(vacationsSize);

        modelAndView.addObject("vacationsList", Lists.reverse(vacations));
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);
        List<Region> regionListForCalc = new ArrayList<Region>();
        List<Integer> filledRegionsId = getRegionIdList();
        for (Integer i : filledRegionsId){
            regionListForCalc.add(regionService.find(i));
        }
        getSummaryAndCalcDays(regionListForCalc, vacations, calDays, workDays, DateTimeUtil.getYear(new Date()));//TODO возможно упростить, сделать вместо двух вызовов один

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
                DateTimeUtil.stringToDate(vacationsForm.getCalFromDate(), DateTimeUtil.DATE_PATTERN),
                DateTimeUtil.stringToDate(vacationsForm.getCalToDate(), DateTimeUtil.DATE_PATTERN),
                true
        );
        return employeeHelper.makeEmployeeListInJSON(employeeList);
    }

}
