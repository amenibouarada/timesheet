package com.aplana.timesheet.controller;

import com.aplana.timesheet.controller.quickreport.BusinessTripsQuickReport;
import com.aplana.timesheet.controller.quickreport.IllnessesQuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReportGenerator;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.PermissionsEnum;
import com.aplana.timesheet.enums.QuickReportTypesEnum;
import com.aplana.timesheet.enums.RegionsEnum;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessControllerException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.service.helper.EmployeeHelper;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.DateNumbers;
import com.aplana.timesheet.util.EnumsUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.timesheet.enums.PermissionsEnum.CHANGE_ILLNESS_BUSINESS_TRIP;
import static com.aplana.timesheet.enums.PermissionsEnum.VIEW_ILLNESS_BUSINESS_TRIP;

/**
 * User: vsergeev
 * Date: 17.01.13
 */
@Controller
public class BusinessTripsAndIllnessController extends AbstractController{

    public static final int ALL_VALUE = -1;

    private static final String UNCNOWN_PRINTTYPE_ERROR_MESSAGE = "Ошибка: запрашивается неизвестный тип отчета!";
    private static final String INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE = "Ошибка: в настройках указана неверная дата начала отчетного года! Установлен год по умолчанию.";
    private static final String INVALID_MOUNTH_BEGIN_DATE_ERROR_MESSAGE = "Ошибка: не удается определить отчетный месяц!";
    private static final String NO_PRINTTYPE_FINDED_ERROR_MESSAGE = "Ошибка: не удалось получить тип отчета!";
    private static final String ACCESS_ERROR_MESSAGE = "Не удается считать права на формирование отчетов!";
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "У Вас недостаточно прав для просмотра отчетов других сотрудников!";
    private static final String UNCNOWN_REGION_EXCEPTION_MESSAGE = "Сотрудник имеет неизвестный регион!";
    private static final String INVALID_DATES_IN_SETTINGS_EXCEPTION_MESSAGE = "В файле настроек указаны неверные даты для региона!";

    //дефолтные дни начала года для регионов и Москвы
    private static final int DEFAULT_MOSCOW_YEAR_BEGIN_MONTH = java.util.Calendar.APRIL;
    private static final int DEFAULT_MOSCOW_YEAR_BEGIN_DAY = 1;
    private static final Integer DEFAULT_REGION_YEAR_BEGIN_MONTH = java.util.Calendar.SEPTEMBER;
    private static final Integer DEFAULT_REGION_YEAR_BEGIN_DAY = 1;

    private static final Logger logger = LoggerFactory.getLogger(BusinessTripsAndIllnessController.class);

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    public static final int ALL_EMPLOYEES = -1;
    public static final String ERROR_BUSINESS_TRIP_DELETE = "Ошибка при удалении командировки из БД!";
    public static final String ERROR_ILLNESS_DELETE = "Ошибка при удалении больничного из БД!";

    @Autowired
    SecurityService securityService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    EmployeeHelper employeeHelper;
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
    private RegionService regionregionService;
    @Autowired
    private IllnessMailService illnessMailService;

    @Autowired
    @Qualifier("illnessesQuickReportGenerator")
    QuickReportGenerator<IllnessesQuickReport> illnessesQuickReportGenerator;
    @Autowired
    @Qualifier("businessTripsQuickReportGenerator")
    QuickReportGenerator<BusinessTripsQuickReport> businessTripsQuickReportGenerator;
    @Autowired
    TSPropertyProvider propertyProvider;

    @RequestMapping(value = "/businesstripsandillness/delete/{reportId}/{reportType}", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteReport(@PathVariable("reportId") Integer reportId,
                               @PathVariable("reportType") Integer reportType) {
        try {
            final QuickReportTypesEnum reportTypeAsEnum = EnumsUtils.getEnumById(reportType, QuickReportTypesEnum.class);
            switch (reportTypeAsEnum) {
                case BUSINESS_TRIP:
                    return deleteBusinessTrip(reportId);
                case ILLNESS:
                    return deleteIllness(reportId);
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
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm tsForm,
            @RequestParam(value="back", required=false) String back
    ) throws BusinessTripsAndIllnessControllerException {

//        savepoint("businesstripsandillness");
        if (back != null){
            Integer backDivisionId = (Integer)session.getAttribute("divisionId");
            Integer backEmployeeId = (Integer)session.getAttribute("employeeId");
            BusinessTripsAndIllnessForm backTsForm = (BusinessTripsAndIllnessForm)session.getAttribute("tsForm");

            if (backDivisionId != null && backEmployeeId != null && backTsForm != null){
                divisionId = backDivisionId;
                employeeId = backEmployeeId;
                tsForm = backTsForm;
            }
        } else {
            session.setAttribute("divisionId", divisionId);
            session.setAttribute("employeeId", employeeId);
            session.setAttribute("tsForm", tsForm);
        }

        Integer printtype = tsForm.getReportType();
        Integer manager = tsForm.getManager();
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
        return getBusinessTripsOrIllnessReport(divisionId, regions, employeeId, manager, dateFrom, dateTo, printtype, tsForm);
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
        return getBusinessTripsOrIllnessReport(employeeId, QuickReportTypesEnum.BUSINESS_TRIP.getId());
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
        return getBusinessTripsOrIllnessReport(employeeId, QuickReportTypesEnum.ILLNESS.getId());
    }

    private ModelAndView getBusinessTripsOrIllnessReport(Integer employeeId, Integer printType) throws BusinessTripsAndIllnessControllerException {
        Employee employee = employeeService.find(employeeId);
        Date dateFrom = new Date();
        Date dateTo = DateUtils.addMonths(dateFrom, 1);
        List<Integer> regions = new ArrayList<Integer>();
        regions.add(employee.getRegion().getId());

        return getBusinessTripsOrIllnessReport(employee.getDivision().getId(), regions, employee.getId(), employee.getManager().getId(), dateFrom, dateTo, printType, null);
    }

    private ModelAndView getBusinessTripsOrIllnessReport(Integer divisionId, List<Integer> regions, Integer employeeId, Integer manager,
                                                         Date dateFrom,
                                                         Date dateTo,
                                                         Integer printtype,
                                                         BusinessTripsAndIllnessForm tsForm)
            throws BusinessTripsAndIllnessControllerException {
        boolean hasAnyEmployee = false;
        boolean hasAnyReports = false;
        List<Employee> employeeList = new ArrayList<Employee>();
        HashMap<Employee, QuickReport> reports = new HashMap<Employee, QuickReport>();
        List<Division> divisionList = divisionService.getDivisions();
        final boolean allFlag = (employeeId == ALL_EMPLOYEES);
        if (allFlag) {
            employeeList = employeeService.getEmployeeByRegionAndManagerAndDivision(regions, divisionId, manager);
        } else {
            employeeList.add(employeeService.find(employeeId));
        }
        if (employeeList != null && employeeList.size() != 0 ) {
            hasAnyEmployee = true;
            for (Employee employee : employeeList) {
                QuickReport report = getReport(printtype, employee, dateFrom, dateTo);
                if (report.getPeriodicalsList().size() > 0){
                    hasAnyReports = true;
                }
                reports.put(employee, report);
            }
        }
        for (QuickReport report : reports.values()) {
            report.setPeriodicalsList(clearDuplicatePeriodicals(report.getPeriodicalsList()));
        }

        ModelAndView modelAndView = new ModelAndView("businesstripsandillness");
        modelAndView.addObject("dateFrom", format.format(dateFrom));
        modelAndView.addObject("dateTo", format.format(dateTo));
        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject("managerId", manager == null ? -1 : manager);
        modelAndView.addObject("divisionList", divisionList);
        modelAndView.addObject("employeeListJson", employeeHelper.getEmployeeListWithLastWorkdayJson(divisionList, employeeService.isShowAll(request)));
        modelAndView.addObject("regionIds", getDefaultSelectRegion(regions));
        modelAndView.addObject("regionList", getRegionList());
        modelAndView.addObject("managerList", getManagerList());
        modelAndView.addObject("managerMapJson", getManagerListJson());
        modelAndView.addObject("reportsMap", reports);
        modelAndView.addObject("reportFormed", printtype);
        modelAndView.addObject("forAll", allFlag);
        modelAndView.addObject("hasAnyEmployee", hasAnyEmployee); // найден ли хотя бы один сотрудник
        modelAndView.addObject("hasAnyReports", hasAnyReports);   // найден ли хотя бы один отчет
        if (tsForm != null){
            modelAndView.addObject("businesstripsandillness", tsForm);
        }

        return modelAndView;
    }

    @RequestMapping(value = "/businesstripsandillness/{divisionId}/{employeeId}/{reportTypeId}")
    public ModelAndView showBusinessTripsAndIllnessWithResult(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @PathVariable("reportTypeId") Integer reportTypeId,
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm tsForm,
            @RequestParam(value="employeeId", required=false) String back
    ) throws BusinessTripsAndIllnessControllerException {
        tsForm.setReportType(reportTypeId);
        return showDefaultIllnessReport(divisionId, employeeId, tsForm, back);
    }
    /**
     * Удаляем больничный. Если удаление прошло нормально, то возвращаем пустую строку.
     */
    private String deleteIllness(Integer reportId) throws BusinessTripsAndIllnessControllerException {
        try {
            illnessMailService.sendDeleteMail(reportId);
            illnessService.deleteIllnessById(reportId);
            return StringUtils.EMPTY;
        } catch (Exception e) {
            logger.error(ERROR_ILLNESS_DELETE, e);
            throw new BusinessTripsAndIllnessControllerException(ERROR_ILLNESS_DELETE, e);
        }
    }

    /**
     * Удаляем командировку. Если удаление прошло нормально, то возвращаем пустую строку.
     */
    private String deleteBusinessTrip(Integer reportId) throws BusinessTripsAndIllnessControllerException {
        try {
            businessTripService.deleteBusinessTripById(reportId);
            return StringUtils.EMPTY;
        } catch (Exception e) {
            logger.error(ERROR_BUSINESS_TRIP_DELETE, e);
            throw new BusinessTripsAndIllnessControllerException(ERROR_BUSINESS_TRIP_DELETE, e);
        }
    }

    /**
     * Получаем права сотрудника для просмотреа отчетов. Если у сотрудника прав несколько, то возвращается разрешение с
     * наивысшим приоритетом.
     */
    private PermissionsEnum getRecipientPermission(Employee employee) throws BusinessTripsAndIllnessControllerException {
        Employee reportRecipient = getTimeSheetUser();
        Set<Permission> recipientPermissions = reportRecipient.getPermissions();
        if (recipientCanChangeReports(recipientPermissions)) {
            return CHANGE_ILLNESS_BUSINESS_TRIP;
        } else if (recipientRequiresOwnReports(employee, reportRecipient) || recipientCanViewAllReports(recipientPermissions)) {
            return VIEW_ILLNESS_BUSINESS_TRIP;
        } else {
            logger.error(ACCESS_DENIED_ERROR_MESSAGE, new BusinessTripsAndIllnessControllerException(ACCESS_DENIED_ERROR_MESSAGE));
            return null;
        }
    }

    /**
     * Получаем пользователя из спринга
     */
    private Employee getTimeSheetUser() throws BusinessTripsAndIllnessControllerException {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new BusinessTripsAndIllnessControllerException(ACCESS_ERROR_MESSAGE);
        }
        Employee employee = employeeService.find(securityUser.getEmployee().getId());  //из-за lazy loading приходится занова получать сотрудника для начала транзакции

        return employee;
    }

    /**
     * проверяем, может ли получатель редактировать отчеты
     */
    private boolean recipientCanChangeReports(Set<Permission> recipientPermissions) {
        return checkRecipientForPermission(recipientPermissions, CHANGE_ILLNESS_BUSINESS_TRIP);
    }

    /**
     * проверяем, может ли получатель видеть отчеты всех сотрудников
     */
    private boolean recipientCanViewAllReports(Set<Permission> recipientPermissions) {
        return checkRecipientForPermission(recipientPermissions, VIEW_ILLNESS_BUSINESS_TRIP);
    }

    /**
     * проверяем, не хочет ли сотрудник посмотреть свой собственный отчет
     */
    private boolean recipientRequiresOwnReports(Employee employee, Employee reportRecipient) {
        return reportRecipient.getId().equals(employee.getId());
    }

    /**
     * находим среди разрешений сотрудника нужное
     */
    private boolean checkRecipientForPermission(Set<Permission> recipientPermissions, final PermissionsEnum permission) {
        try {
            Iterables.find(recipientPermissions, new Predicate<Permission>() {
                @Override
                public boolean apply(@Nullable Permission perm) {
                    return perm.getId().equals(permission.getId());
                }
            });
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    private List<Integer> getDefaultSelectRegion(List<Integer> regionIds) {
        if (regionIds == null || regionIds.size() == 0) {
            regionIds = new ArrayList<Integer>();
            regionIds.add(-1);
        }
        return regionIds;
    }

    private List<Periodical> clearDuplicatePeriodicals(List<Periodical> periodicalList){
        List<Periodical> cleanPeriodicalList = new ArrayList<Periodical>();
        for (Periodical p : periodicalList){
            Boolean isAdded = false;
            for (Periodical cp : cleanPeriodicalList){
                if (p.getBeginDate().equals(cp.getBeginDate())){
                    cp.setWorkingDays(cp.getWorkingDays() + p.getWorkingDays());
                    cp.setCalendarDays(cp.getCalendarDays() + p.getCalendarDays());
                    isAdded = true;
                }
            }
            if (!isAdded){
                cleanPeriodicalList.add(p);
            }
        }
        return cleanPeriodicalList;
    }

    /**
     * В зависимости от типа запрашиваемого отчета, формируем сам отчет
     */
    private QuickReport getReport(Integer printtype, Employee employee, Date periodBeginDate, Date periodEndDate) throws BusinessTripsAndIllnessControllerException {

        Date yearBeginDate = getYearBeginDate(employee, calendarService.getMonthFromDate(periodBeginDate), calendarService.getYearFromDate(periodBeginDate));
        Date yearToDate = getYearBeginDate(employee, calendarService.getMonthFromDate(periodEndDate), calendarService.getYearFromDate(periodEndDate));
        Date yearEndDate = DateUtils.addDays(DateUtils.addYears(yearToDate, 1), -1);
        if (printtype == null) {
            throw new BusinessTripsAndIllnessControllerException(NO_PRINTTYPE_FINDED_ERROR_MESSAGE);
        }
        QuickReportGenerator generator = getQuickReportGenerator(printtype);

        return generator.generate(employee, periodBeginDate, periodEndDate, yearBeginDate, yearEndDate);
    }

    /**
     * возвращает дату (первое число по указанным месяцу и году)
     */
    private Date getMonthBeginDate(Integer month, Integer year) throws BusinessTripsAndIllnessControllerException {
        try {
            return format.parse(String.format("01.%s.%s", month, year));
        } catch (ParseException ex) {
            throw new BusinessTripsAndIllnessControllerException(INVALID_MOUNTH_BEGIN_DATE_ERROR_MESSAGE);
        }
    }

    /**
     * Возвращает дату начала ОТЧЕТНОГО года, в который входит выбранный месяц выбранного КАЛЕНДАРНОГО года.
     * даты начала/конца ОТЧЕТНЫХ годов берутся либо из дефолтных значений, либо из файла настроек таймшита
     */
    private Date getYearBeginDate(Employee employee, Integer month, Integer year) throws BusinessTripsAndIllnessControllerException {
        DateNumbers dateNumbers = getYearPeriodForEmployyesRegion(employee);
        return generateYearBeginDate(dateNumbers, month, year);
    }

    /**
     * Превращаем DateNumbers в Date попутно проверяя, в какой год попадает отчетный месяц.
     * Если отчетный месяц меньше месяца начала периода - значит период начался в предыдущем году относительно года отчетного месяца.
     * Год нужно уменьшить.
     */
    private Date generateYearBeginDate(DateNumbers dateNumbers, Integer month, Integer year) throws BusinessTripsAndIllnessControllerException {
        try {
            if (month < dateNumbers.getDatabaseMonth()) {
                year -= 1;
            }

            return format.parse(String.format("%s.%s.%s", dateNumbers.getDay(), dateNumbers.getDatabaseMonth(), year));
        } catch (ParseException e) {
            throw new BusinessTripsAndIllnessControllerException(INVALID_DATES_IN_SETTINGS_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получаем начало ОТЧЕТНОГО года для региона, в котором работает данный сотрудник.
     * Дата начала либо считывается из файла настроек таймшита, либо выставляется по умолчанию
     * (1.04 для Москвы или 1.09 для регионов)
     */
    private DateNumbers getYearPeriodForEmployyesRegion(Employee employee) throws BusinessTripsAndIllnessControllerException {
        RegionsEnum regionEnum = EnumsUtils.getEnumById(employee.getRegion().getId(), RegionsEnum.class);
        switch (regionEnum) {
            case MOSCOW: {
                try {
                    return getMoskowYearBeginDates();
                } catch (NumberFormatException ex) {
                    logger.error(INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE);
                    return getDefaultMoskowYearBeginDates();
                } catch (NullPointerException ex) {
                    return getDefaultMoskowYearBeginDates();
                }
            }
            case OTHERS:
            case UFA:
            case NIJNIY_NOVGOROD:
            case PERM: {
                try {
                    return getRegionsYearBeginDates();
                } catch (NumberFormatException ex) {
                    logger.error(INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE);
                    return getDefaultRegionsYearBeginDates();
                } catch (NullPointerException ex) {
                    return getDefaultMoskowYearBeginDates();
                }
            }
            default:
                throw new BusinessTripsAndIllnessControllerException(UNCNOWN_REGION_EXCEPTION_MESSAGE);
        }
    }

    /**
     * возвращает даты начала ОТЧЕТНОГО года для Москвы
     */
    private DateNumbers getMoskowYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setDay(propertyProvider.getQuickreportMoskowBeginDay());
        dateNumbers.setMonth(propertyProvider.getQuickreportMoskowBeginMonth());

        return dateNumbers;
    }


    /**
     * возвращает даты начала ОТЧЕТНОГО года для регионов
     */
    private DateNumbers getRegionsYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setDay(propertyProvider.getQuickreportRegionsBeginDay());
        dateNumbers.setMonth(propertyProvider.getQuickreportRegionsBeginMonth());

        return dateNumbers;
    }

    /**
     * Возвращает дефолтные даты начала ОТЧЕТНОГО года для регионов
     */
    private DateNumbers getDefaultRegionsYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setMonth(DEFAULT_REGION_YEAR_BEGIN_MONTH);
        dateNumbers.setDay(DEFAULT_REGION_YEAR_BEGIN_DAY);

        return dateNumbers;
    }

    /**
     * возвращает дефолтные даты начала ОТЧЕТНОГО года для Москвы
     */
    private DateNumbers getDefaultMoskowYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setMonth(DEFAULT_MOSCOW_YEAR_BEGIN_MONTH);
        dateNumbers.setDay(DEFAULT_MOSCOW_YEAR_BEGIN_DAY);

        return dateNumbers;
    }

    /**
     * В зависимости от типа отчета ввозвращает нужный генератор
     */
    private QuickReportGenerator getQuickReportGenerator(Integer printtype) throws BusinessTripsAndIllnessControllerException {
        QuickReportTypesEnum quickReportType = EnumsUtils.getEnumById(printtype, QuickReportTypesEnum.class);
        switch (quickReportType) {
            case ILLNESS:
                return illnessesQuickReportGenerator;
            case BUSINESS_TRIP:
                return businessTripsQuickReportGenerator;
            default:
                throw new BusinessTripsAndIllnessControllerException(UNCNOWN_PRINTTYPE_ERROR_MESSAGE);
        }
    }

    private List<Region> getRegionList() {
        return regionregionService.getRegions();
    }

    private List<Employee> getManagerList() {
        return employeeService.getManagerListForAllEmployee();
    }

    private String getManagerListJson() {
        return employeeService.getManagerListJson();
    }
}