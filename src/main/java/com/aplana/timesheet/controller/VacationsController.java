package com.aplana.timesheet.controller;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.constants.PadegConstants;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.util.JsonUtil;
import com.aplana.timesheet.util.TimeSheetUser;
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

import static argo.jdom.JsonNodeBuilders.*;
import static com.aplana.timesheet.form.VacationsForm.ALL_VALUE;
import static com.aplana.timesheet.form.VacationsForm.VIEW_TABLE;
import static com.aplana.timesheet.util.DateTimeUtil.VIEW_DATE_PATTERN;
import static com.aplana.timesheet.util.DateTimeUtil.dateToString;


/**
 * @author rshamsutdinov, aalikin
 * @version 1.1
 */
@Controller
public class VacationsController extends AbstractControllerForEmployee {

    private static final String VACATION_FORM = "vacationsForm";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

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
        Date dateFrom = DateTimeUtil.stringToDate(vacationsForm.getCalFromDate(), DATE_FORMAT);
        Date dateTo = DateTimeUtil.stringToDate(vacationsForm.getCalToDate(), DATE_FORMAT);
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
        modelAndView.addObject("vacationsList", revertList(vacations));
        modelAndView.addObject("vacationListByRegionJSON", getVacationListByRegionJSON(vacations));
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);
        modelAndView.addObject("holidayList", getHolidayListJSON(dateFrom, dateTo));
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
        Integer firstYear = dateFrom.getYear() + 1900;
        Integer lastYear = dateTo.getYear() + 1900;
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

    private List<Vacation> revertList(List<Vacation> list){
        for (Integer i = 0; i < list.size()/2; i++){
            Vacation vac = list.get(i);
            list.set(i, list.get(list.size()-i-1));
            list.set(list.size()-i-1, vac);
        }
        return list;
    }

    private String getVacationListByRegionJSON(List<Vacation> vacationList ) {
        List<Region> regionList = regionService.getRegions();
        List<Employee> employeeList = new ArrayList<Employee>();
        for (Vacation vacation : vacationList) {
            Employee employee = vacation.getEmployee();
            if ( ! (employeeList.contains(employee)) ){
                employeeList.add(employee);
            }
        }
        final JsonArrayNodeBuilder result = anArrayBuilder();
        // для каждого проекта смотрим сотрудников у которых есть отпуск
        for (Region region : regionList){
            JsonArrayNodeBuilder employeeNode = anArrayBuilder();
            boolean hasEmployees = false;
            for (Employee employee : employeeList){
                if (employee.getRegion().getId().equals(region.getId())){
                    JsonArrayNodeBuilder vacationNode = createVacationsNode(employee, vacationList);
                    hasEmployees = true;
                    employeeNode.withElement(anObjectBuilder().
                            withField("employee", aStringBuilder(employee.getName())).
                            withField("vacations", vacationNode));
                }
            }
            if (hasEmployees){
                result.withElement(
                        anObjectBuilder().
                                withField("region_id", aStringBuilder(region.getId().toString())).
                                withField("region_name", aStringBuilder(region.getName())).
                                withField("employeeList", employeeNode)
                );
            }
        }
        return JsonUtil.format(result);
    }

    private JsonArrayNodeBuilder createVacationsNode(Employee employee, List<Vacation> vacationList){
        JsonArrayNodeBuilder vacationNode = anArrayBuilder();
        for (Vacation vacation : vacationList){
            if (vacation.getEmployee().equals(employee)){
                vacationNode.withElement(anObjectBuilder().
                        withField("beginDate", aStringBuilder(dateToString(vacation.getBeginDate(), VIEW_DATE_PATTERN))).
                        withField("endDate", aStringBuilder(dateToString(vacation.getEndDate(), VIEW_DATE_PATTERN))).
                        withField("status", aStringBuilder(vacation.getStatus().getValue())).
                        withField("type", aStringBuilder(vacation.getType().getId().toString())).
                        withField("typeName", aStringBuilder(vacation.getType().getValue())));

            }
        }
        return vacationNode;
    }

    private String getHolidayListJSON(Date beginDate, Date endDate){
        final JsonArrayNodeBuilder result = anArrayBuilder();
        // т.к. отпуска могут начинаться ранее или позднее заданных дат, то на всякий случай прибавим к диапазону
        // по месяцу с обоих концов
        List<Holiday> holidays = calendarService.getHolidaysForRegion(DateUtils.addDays(beginDate, -30),
                                                                      DateUtils.addDays(endDate, 30),
                                                                      null);
        for (Holiday holiday : holidays){
            result.withElement(aStringBuilder(
                                            dateToString(
                                                holiday.getCalDate().getCalDate(), VIEW_DATE_PATTERN)));
        }
        return JsonUtil.format(result);
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
        /* непонятно зачем это вызывается если эти объекты не используются */
//        final ModelAndView modelAndView = createModelAndViewForEmployee("vacationsNeedsApproval", employee.getId(), employee.getDivision().getId());
        final ModelAndView modelAndView = new ModelAndView("vacationsNeedsApproval");
        modelAndView.addObject("curEmployee", securityService.getSecurityPrincipal().getEmployee());

        final List<Vacation> vacations = vacationService.findVacationsNeedsApproval(employee.getId());
        final int vacationsSize = vacations.size();

        final Map<Vacation,Integer> calDays = new HashMap<Vacation, Integer>(vacationsSize);
        final Map<Vacation,Integer> workDays = new HashMap<Vacation, Integer>(vacationsSize);

        modelAndView.addObject("vacationsList", revertList(vacations));
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);
        List<Region> regionListForCalc = new ArrayList<Region>();
        List<Integer> filledRegionsId = getRegionIdList();
        for (Integer i : filledRegionsId){
            regionListForCalc.add(regionService.find(i));
        }
        getSummaryAndCalcDays(regionListForCalc, vacations, calDays, workDays, new Date().getYear() + 1900);//TODO возможно упростить, сделать вместо двух вызовов один

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
        return vacationsNeedsApprovalCount>0?"("+vacationsNeedsApprovalCount+")":"";
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
                DateTimeUtil.stringToDate(vacationsForm.getCalFromDate(), DATE_FORMAT),
                DateTimeUtil.stringToDate(vacationsForm.getCalToDate(), DATE_FORMAT),
                true
        );
        return employeeHelper.makeEmployeeListInJSON(employeeList);
    }

}
