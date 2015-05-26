package com.aplana.timesheet.service;

import com.aplana.timesheet.controller.quickreport.BusinessTripsQuickReport;
import com.aplana.timesheet.controller.quickreport.IllnessesQuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReportGenerator;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Periodical;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.enums.QuickReportTypesEnum;
import com.aplana.timesheet.enums.RegionsEnum;
import com.aplana.timesheet.exception.TSRuntimeException;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessControllerException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessForm;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateNumbers;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abayanov
 * Date: 13.08.14
 */
@Service
public class BusinessTripsAndIllnessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessTripService.class);

    public static final int ALL_EMPLOYEES = -1;
    public static final String ERROR_BUSINESS_TRIP_DELETE = "Ошибка при удалении командировки из БД!";
    public static final String ERROR_ILLNESS_DELETE = "Ошибка при удалении больничного из БД!";
    public static final int ALL_VALUE = -1;

    private static final String UNCNOWN_PRINTTYPE_ERROR_MESSAGE = "Ошибка: запрашивается неизвестный тип отчета!";
    private static final String INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE = "Ошибка: в настройках указана неверная дата начала отчетного года! Установлен год по умолчанию.";
    private static final String NO_PRINTTYPE_FINDED_ERROR_MESSAGE = "Ошибка: не удалось получить тип отчета!";
    private static final String UNCNOWN_REGION_EXCEPTION_MESSAGE = "Сотрудник имеет неизвестный регион!";

    //дефолтные дни начала года для регионов и Москвы
    private static final int DEFAULT_MOSCOW_YEAR_BEGIN_MONTH = java.util.Calendar.APRIL;
    private static final int DEFAULT_MOSCOW_YEAR_BEGIN_DAY = 1;
    private static final Integer DEFAULT_REGION_YEAR_BEGIN_MONTH = java.util.Calendar.SEPTEMBER;
    private static final Integer DEFAULT_REGION_YEAR_BEGIN_DAY = 1;

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
    private RegionService regionregionService;

    @Autowired
    private IllnessMailService illnessMailService;

    @Autowired
    TSPropertyProvider propertyProvider;

    @Autowired
    @Qualifier("illnessesQuickReportGenerator")
    QuickReportGenerator<IllnessesQuickReport> illnessesQuickReportGenerator;

    @Autowired
    @Qualifier("businessTripsQuickReportGenerator")
    QuickReportGenerator<BusinessTripsQuickReport> businessTripsQuickReportGenerator;

    /**
     * Удаляем больничный. Если удаление прошло нормально, то возвращаем пустую строку.
     */
    public String deleteIllness(Integer reportId) throws BusinessTripsAndIllnessControllerException {
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
    public String deleteBusinessTrip(Integer reportId) throws BusinessTripsAndIllnessControllerException {
        try {
            businessTripService.deleteBusinessTripById(reportId);
            return StringUtils.EMPTY;
        } catch (Exception e) {
            logger.error(ERROR_BUSINESS_TRIP_DELETE, e);
            throw new BusinessTripsAndIllnessControllerException(ERROR_BUSINESS_TRIP_DELETE, e);
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
        if (month < dateNumbers.getDatabaseMonth()) {
            year -= 1;
        }
        try {
            return DateTimeUtil.parseStringToDateForView(String.format("%s.%s.%s", dateNumbers.getDay(), dateNumbers.getDatabaseMonth(), year));
        } catch (TSRuntimeException e) {
            throw new BusinessTripsAndIllnessControllerException("Formatting error", e);
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

    public ModelAndView getBusinessTripsOrIllnessReport(Integer employeeId, Integer printType, Boolean showAll) throws BusinessTripsAndIllnessControllerException {
        Employee employee = employeeService.find(employeeId);
        Date dateFrom = new Date();
        Date dateTo = DateUtils.addMonths(dateFrom, 1);
        List<Integer> regions = new ArrayList<Integer>();
        regions.add(employee.getRegion().getId());

        return getBusinessTripsOrIllnessReport(employee.getDivision().getId(), regions, employee.getId(), employee.getManager().getId(), dateFrom, dateTo, printType, null, showAll);
    }

    public ModelAndView getBusinessTripsOrIllnessReport(Integer divisionId,
                                                        List<Integer> regions,
                                                        Integer employeeId,
                                                        Integer manager,
                                                        Date dateFrom,
                                                        Date dateTo,
                                                        Integer printtype,
                                                        BusinessTripsAndIllnessForm tsForm,      // so much parameters
                                                        Boolean showAll
    ) throws BusinessTripsAndIllnessControllerException {
        boolean hasAnyEmployee = false;
        boolean hasAnyReports = false;
        List<Employee> employeeList = new ArrayList<Employee>();
        HashMap<Employee, QuickReport> reports = new HashMap<Employee, QuickReport>();
        List<Division> divisionList = divisionService.getDivisions();
        final boolean allFlag = (employeeId == ALL_EMPLOYEES);
        if (allFlag) {
            employeeList = employeeService.getEmployeeByRegionAndManagerRecursiveAndDivision(regions, divisionId, manager);
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
        modelAndView.addObject("dateFrom", DateTimeUtil.formatDateIntoViewFormat(dateFrom));
        modelAndView.addObject("dateTo", DateTimeUtil.formatDateIntoViewFormat(dateTo));
        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject("managerId", manager == null ? -1 : manager);
        modelAndView.addObject("divisionList", divisionList);
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
}
