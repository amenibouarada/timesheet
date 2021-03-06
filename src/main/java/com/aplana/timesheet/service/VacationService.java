package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.CreateVacationForm;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.service.vacationapproveprocess.VacationApprovalProcessService;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;
import java.util.Calendar;

import static argo.jdom.JsonNodeBuilders.*;
import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static com.aplana.timesheet.form.VacationsForm.ALL_VALUE;
import static com.aplana.timesheet.system.constants.RoleConstants.ROLE_ADMIN;
import static com.aplana.timesheet.util.DateTimeUtil.VIEW_DATE_PATTERN;
import static com.aplana.timesheet.util.DateTimeUtil.dateToString;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class VacationService extends AbstractServiceWithTransactionManagement {

    @Autowired
    private VacationDAO vacationDAO;
    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private VacationApprovalProcessService vacationApprovalProcessService;
    @Autowired
    private VacationApprovalService vacationApprovalService;
    @Autowired
    protected CalendarService calendarService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private VacationDaysService vacationDaysService;
    @Autowired
    protected HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(VacationService.class);

    public static final String CANT_GET_EXIT_TO_WORK_EXCEPTION_MESSAGE = "Не удалось получить дату выхода из отпуска и количество дней.";

    private final double VACATION_KOEF = 2.33D;

    @Transactional
    public void store(Vacation vacation) {
        vacationDAO.store(vacation);
    }

    @Transactional
    public Boolean isDayVacation(Employee employee, Date date) {
        return vacationDAO.isDayVacation(employee, date);
    }

    @Transactional
    public List<Integer> getAllNotApprovedVacationsIds() {
        return vacationDAO.getAllNotApprovedVacationsIds();
    }

    public long getIntersectVacationsCount(Integer employeeId, Timestamp fromDate, Timestamp toDate, DictionaryItem item) {
        return vacationDAO.getIntersectVacationsCount(
                employeeId,
                fromDate,
                toDate,
                item
        );
    }

    public Long getIntersectPlannedVacationsCount(Integer employeeId, Date fromDate, Date toDate, DictionaryItem item) {
        return vacationDAO.getIntersectPlannedVacationsCount(
                employeeId,
                fromDate,
                toDate,
                item
        );
    }

    public List<Vacation> findVacations(Integer employeeId, Date beginDate, Date endDate, DictionaryItem typeId) {
        return vacationDAO.findVacations(employeeId, beginDate, endDate, typeId);
    }

    public List<Vacation> findVacations(List<Employee> employees, Date beginDate, Date endDate, DictionaryItem typeId) {
        return vacationDAO.findVacations(employees, beginDate, endDate, typeId);
    }

    public List<Vacation> findVacationsByTypes(Integer year, Integer month, Integer employeeId, List<DictionaryItem> types) {
        return vacationDAO.findVacationsByTypes(year, month, employeeId, types);
    }

    public List<Vacation> findVacationsByTypesAndStatuses(Integer year, Integer month, Integer employeeId, List<DictionaryItem> types, List<DictionaryItem> statuses) {
        return vacationDAO.findVacationsByTypesAndStatuses(year, month, employeeId, types, statuses);
    }

    public List<Vacation> findVacationsByType(Integer year, Integer month, Integer employeeId, DictionaryItem type) {
        return vacationDAO.findVacationsByType(year, month, employeeId, type);
    }

    @Transactional
    public Vacation findVacation(Integer vacationId) {
        return vacationDAO.findVacation(vacationId);
    }

    @Transactional
    public Vacation tryFindVacation(Integer vacationId) {
        return vacationDAO.tryFindVacation(vacationId);
    }

    @Transactional
    public void delete(Vacation vacation) {
        vacationDAO.delete(vacation);
    }


    @Transactional
    public void detach(Vacation vacation) {
        vacationDAO.detach(vacation);
    }

    @Transactional
    public String deleteVacation(Integer vacationId) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        String message = null;
        final Vacation vacation = tryFindVacation(vacationId);

        if (vacation == null) { // если вдруг удалил автор, а не сотрудник
            throw new DeleteVacationException("Запись не найдена");
        }

        final Employee employee = securityService.getSecurityPrincipal().getEmployee();

        if (isVacationDeletePermission(vacation, employee)) {
            /* для планируемых отпусков другая удалялка */
            if (vacation.getType().getId() == VacationTypesEnum.PLANNED.getId()) {
                sendMailService.performPlannedVacationDeletedMailing(vacation);
            } else {
                sendMailService.performVacationDeletedMailing(vacation);    //todo переделать, чтобы рассылка все-таки была после удаления?
            }
            delete(vacation);
        } else {
            message = "Нельзя удалить заявление на отпуск. Для удаления данного заявления необходимо написать на timesheet@aplana.com";
        }

        builder.withElement(
                anObjectBuilder().
                        withField("status", aNumberBuilder(message == null ? "0" : "-1")).
                        withField("message", aStringBuilder(message == null ? "" : message)));

        return JsonUtil.format(builder);
    }

    /**
     * Удаляет планируемые отпуска уволенного сотрудника
     *
     * @param employee
     */
    public void deleteFiredVacations(Employee employee) {
        vacationDAO.deleteFiredVacations(employee);
    }

    public int getApprovedVacationsWorkdaysCount(Employee employee, Integer year, Integer month, boolean toCurrentDate) {
        return vacationDAO.getApprovedVacationsWorkdaysCount(employee, year, month, null, toCurrentDate);
    }

    public Double getVacationsWorkdaysCount(Employee employee, Integer year, Integer month) {
        return (double) vacationDAO.getVacationsWorkdaysCount(employee, year, month);
    }

    public List<DictionaryItem> getVacationTypes(List<Vacation> vacations) {
        List<DictionaryItem> result = new ArrayList<DictionaryItem>();
        for (Vacation vacation : vacations) {
            if (!result.contains(vacation.getType())) {
                result.add(vacation.getType());
            }
        }
        // отсортируем
        Collections.sort(result, new Comparator() {
            @Override
            public int compare(Object type1, Object type2) {
                Integer typeId1 = ((DictionaryItem) type1).getId();
                Integer typeId2 = ((DictionaryItem) type2).getId();
                return typeId1.compareTo(typeId2);
            }
        });
        return result;
    }

    public List<Vacation> findVacationsNeedsApproval(Integer employeeId) {
        return vacationDAO.findVacationsNeedApproval(employeeId);
    }

    public Integer findVacationsNeedsApprovalCount(Integer employeeId) {
        return vacationDAO.findVacationsNeedApproval(employeeId).size();
    }

    public void createAndMailVacation(CreateVacationForm createVacationForm, Employee employee, Employee curEmployee, boolean isApprovedVacation)
            throws VacationApprovalServiceException {

        final Vacation vacation = new Vacation();

        vacation.setCreationDate(new Date());
        vacation.setBeginDate(DateTimeUtil.stringToTimestamp(createVacationForm.getCalFromDate()));
        vacation.setEndDate(DateTimeUtil.stringToTimestamp(createVacationForm.getCalToDate()));
        vacation.setComment(createVacationForm.getComment().trim());
        vacation.setType(dictionaryItemService.find(createVacationForm.getVacationType()));
        vacation.setAuthor(curEmployee);
        vacation.setEmployee(employee);
        vacation.setRemind(false);

        vacation.setStatus(dictionaryItemService.find(
                isApprovedVacation ? VacationStatusEnum.APPROVED.getId() : VacationStatusEnum.APPROVEMENT_WITH_PM.getId()
        ));

        TransactionStatus transactionStatus = null;

        try {
            transactionStatus = getNewTransaction();

            store(vacation);

            boolean isPlannedVacation = vacation.getType().getId().equals(VacationTypesEnum.PLANNED.getId());

            if (isPlannedVacation) {
                vacationApprovalProcessService.sendNoticeForPlannedVacaton(vacation);
            } else {
                List<Vacation> plannedVacations = vacationDAO.getPlannedVacationsByBeginAndEndDates(employee, vacation.getBeginDate(), vacation.getEndDate());
                for (Vacation plannedVacation : plannedVacations) {
                    delete(plannedVacation);
                }
                if (needsToBeApproved(vacation)) {
                    vacationApprovalProcessService.sendVacationApproveRequestMessages(vacation);       //рассылаем письма о согласовании отпуска
                } else {
                    vacationApprovalProcessService.sendBackDateVacationApproved(vacation);
                }
            }
            commit(transactionStatus);
        } catch (VacationApprovalServiceException e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
                logger.error("Transaction rollbacked. Error saving vacation: {} ", e);
            } else {
                logger.error("TransactionStatus is null.");
            }
        }
        sendMailService.performVacationCreateMailing(vacation);
    }

    private boolean needsToBeApproved(Vacation vacation) {
        return !vacation.getStatus().getId().equals(VacationStatusEnum.APPROVED.getId());
    }

    public String getExitToWorkAndCountVacationDayJson(String beginDateString,
                                                       String endDateString,
                                                       Integer employeeId,
                                                       Integer vacationTypeId
    ) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();
        try {
            Employee employee = employeeService.find(employeeId);
            final Timestamp beginDate = DateTimeUtil.stringToTimestamp(beginDateString, CreateVacationForm.DATE_FORMAT);
            final Timestamp endDate = DateTimeUtil.stringToTimestamp(endDateString, CreateVacationForm.DATE_FORMAT);

            //Получаем день выхода на работу
            com.aplana.timesheet.dao.entity.Calendar endDateCalendar = calendarService.find(endDate);
            com.aplana.timesheet.dao.entity.Calendar nextWorkDay =
                    calendarService.getNextWorkDay(endDateCalendar, employee.getRegion());
            String format = DateFormatUtils.format(nextWorkDay.getCalDate(), CreateVacationForm.DATE_FORMAT);
            builder.withField("exitDate", aStringBuilder(format));

            //Получаем кол-во дней в отпуске за исключением неучитываемых праздников
            Integer vacationDayCountExCons = calendarService.getCountDaysForPeriodForRegionExConsiderHolidays(
                    beginDate,
                    endDate,
                    employee.getRegion()
            );
            //Получаем кол-во рабочих дней в отпуске
            Integer vacationWorkCount = calendarService.getCountWorkDaysForPeriodForRegion(
                    beginDate,
                    endDate,
                    employee.getRegion());

            builder.withField("vacationWorkDayCount", aStringBuilder(vacationWorkCount.toString()));
            builder.withField("vacationDayCount", aStringBuilder((vacationDayCountExCons <= 0) ? "0" : vacationDayCountExCons.toString()));

            /*  проверка на необходимость вывода информ сообщения
                о необходимости оформления отпуска по вск
                для отпуска с сохранением содержания
            */
            Calendar calendar = java.util.Calendar.getInstance();

            calendar.setTime(beginDate);
            int beginWeekYear = calendar.get(Calendar.WEEK_OF_YEAR);

            calendar.setTime(endDate);
            int endWeekYear = calendar.get(Calendar.WEEK_OF_YEAR);

            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            Date beginWeekDate = calendar.getTime();

            int endDay = calendar.get(Calendar.DAY_OF_WEEK);
            int leftDays = Calendar.SATURDAY - endDay;
            calendar.add(Calendar.DATE, ++leftDays);
            Date sunday = calendar.getTime();

            //Количество рабочих дней в отпуске, если конец отпуска приходится
            //на следующую неделю то считается с понедельника след недели по дату конца отпуска
            Integer countWorkDaysVacationPeriod = calendarService.getCountWorkDaysForPeriodForRegion(
                    beginWeekYear != endWeekYear ? beginWeekDate : beginDate,
                    endDate,
                    employee.getRegion());

            //Количество рабочих дней в неделе приходящихся на конец отпуска
            Integer countWorkDaysWeek = calendarService.getCountWorkDaysForPeriodForRegion(
                    beginWeekDate,
                    DateUtils.addDays(beginWeekDate, 6),
                    employee.getRegion());

            // Количество учитываемых дней в период с понедельника последней недели отпуска по конец отпуска
            Integer countVacConsiderDaysOnEndWeek = calendarService.getCountDaysForPeriodForRegionExConsiderHolidays(
                    beginWeekYear != endWeekYear ? beginWeekDate : beginDate,
                    endDate,
                    employee.getRegion());

            // Количество учитываемых дней в неделе
            Integer countConsiderDaysOnEndWeek = calendarService.getCountDaysForPeriodForRegionExConsiderHolidays(
                    beginWeekYear != endWeekYear ? beginWeekDate : beginDate,
                    sunday,
                    employee.getRegion());

            if (    vacationTypeId != null &&
                    vacationTypeId == VacationTypesEnum.WITH_PAY.getId() &&
                    // проверка что в отпуск попала не вся учитываемая неделя
                    !countVacConsiderDaysOnEndWeek.equals(countConsiderDaysOnEndWeek) &&
                    // и в этот период попадают все рабочие дни
                    countWorkDaysVacationPeriod.equals(countWorkDaysWeek)
                    ) {
                builder.withField("vacationFridayInform", aStringBuilder("true"));
            }

            return JsonUtil.format(builder);
        } catch (Exception th) {
            logger.error(CANT_GET_EXIT_TO_WORK_EXCEPTION_MESSAGE, th);
            return CANT_GET_EXIT_TO_WORK_EXCEPTION_MESSAGE;
        }
    }

    /**
     * Вычисление кол-ва выходных дней в заданном периоде
     *
     * @param holidaysForRegion
     * @param beginDate
     * @param endDate
     * @return кол-ва выходных дней в заданном периоде
     */
    public int getHolidaysCount(List<Holiday> holidaysForRegion, final Date beginDate, final Date endDate) {
        return Iterables.size(Iterables.filter(holidaysForRegion, new Predicate<Holiday>() {
            @Override
            public boolean apply(@Nullable Holiday holiday) {
                final Timestamp calDate = holiday.getCalDate().getCalDate();
                return (
                        calDate.compareTo(beginDate) == 0 || calDate.compareTo(endDate) == 0 ||
                                calDate.after(beginDate) && calDate.before(endDate)
                );
            }
        }));
    }

    /**
     * Вычисление кол-ва выходных дней в заданном периоде для конкретного региона
     *
     * @param holidays
     * @param region
     * @param beginDate
     * @param endDate
     * @return кол-ва выходных дней в заданном периоде
     */
    public int getHolidaysCount(List<Holiday> holidays, final Region region, final Date beginDate, final Date endDate) {
        return Iterables.size(Iterables.filter(holidays, new Predicate<Holiday>() {
            @Override
            public boolean apply(@Nullable Holiday holiday) {
                final Timestamp calDate = holiday != null && holiday.getCalDate() != null ? holiday.getCalDate().getCalDate() : null;
                assert calDate != null;
                return (
                        (calDate.compareTo(beginDate) >= 0 && calDate.compareTo(endDate) <= 0) &&
                                (holiday.getRegion() == null || holiday.getRegion().getId().equals(region.getId()))
                );
            }
        }));
    }

    @Transactional
    public Boolean isDayVacationWithoutPlanned(Employee employee, Date date) {
        return vacationDAO.getVacationWithoutPlanned(employee, date) != null ? true : false;
    }

    public Vacation getVacationWithoutPlanned(Employee employee, Date date){
        return vacationDAO.getVacationWithoutPlanned(employee, date);
    }

    /**
     * Определяет является ли отпуск учитываемым в трудозатратах
     * @param vacation
     * @return
     */
    public Boolean isConsiderVacation(Vacation vacation){
        if (vacation == null) {return false;}
        return VacationTypesEnum.getConsiderVacationTypes().contains(
                VacationTypesEnum.getById(vacation.getType().getId()));
    }

    /* функция возвращает можно ли удалить планируемый отпуск в таблице заявлений на отпуск */
    public Boolean isVacationDeletePermission(Vacation vacation, Employee employee) {
        if (employee != null && vacation != null) {
            /* проверим Админ ли текущий пользователь */
            if (employeeService.isEmployeeAdmin(employee.getId())) {
                return Boolean.TRUE;
            } else {
                /* для запланированных отпусков проверяем что это либо создатель отпуска либо сам отпускник
                * либо является лин. рук. отпускника */
                if (vacation.getType().getId() == VacationTypesEnum.PLANNED.getId() &&
                        (vacation.getEmployee().equals(employee) ||
                                vacation.getAuthor().equals(employee) ||
                                employeeService.getLinearEmployees(vacation.getEmployee()).contains(employee)
                        )
                        ) {
                    return Boolean.TRUE;
                }
                /* пользователь создатель или отпускник и статус не отклонено и не утверждено */
                if ((vacation.getEmployee().equals(employee) || vacation.getAuthor().equals(employee)) &&
                        vacation.getStatus().getId() != VacationStatusEnum.REJECTED.getId() &&
                        vacation.getStatus().getId() != VacationStatusEnum.APPROVED.getId()
                        ) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    /**
     * Проверка на РЦК
     *
     * @param vacation
     * @return
     */
    public Boolean isVacationApprovePermission(Vacation vacation) {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        Employee manager = vacation.getEmployee().getDivision().getLeaderId();
        return securityUser.getEmployee().getId().equals(manager.getId());
    }

    public Boolean isVacationNotApproved(Vacation vacation) {
        return !vacation.getStatus().getId().equals(dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()).getId());
    }

    public String getVacationListByRegionJSON(Date dateFrom, Date dateTo, List<Vacation> vacationList) {
        List<Region> regionList = regionService.getRegions();
        List<Employee> employeeList = new ArrayList<Employee>();
        for (Vacation vacation : vacationList) {
            Employee employee = vacation.getEmployee();
            if (!(employeeList.contains(employee))) {
                employeeList.add(employee);
            }
        }
        final JsonArrayNodeBuilder result = anArrayBuilder();
        // для каждого проекта смотрим сотрудников у которых есть отпуск
        for (Region region : regionList) {
            JsonArrayNodeBuilder employeeNode = anArrayBuilder();
            boolean hasEmployees = false;
            for (Employee employee : employeeList) {
                if (employee.getRegion().getId().equals(region.getId())) {
                    JsonArrayNodeBuilder vacationNode = createVacationsNode(employee, vacationList);
                    hasEmployees = true;
                    employeeNode.withElement(anObjectBuilder().
                            withField("employee", aStringBuilder(employee.getName())).
                            withField("vacations", vacationNode));
                }
            }
            if (hasEmployees) {
                result.withElement(
                        anObjectBuilder().
                                withField("region_id", aStringBuilder(region.getId().toString())).
                                withField("region_name", aStringBuilder(region.getName())).
                                withField("employeeList", employeeNode).
                                withField("holidays", getHolidayInRegion(dateFrom, dateTo, region))
                );
            }
        }
        return JsonUtil.format(result);
    }

    private JsonArrayNodeBuilder getHolidayInRegion(Date begin, Date end, Region region) {
        JsonArrayNodeBuilder builder = anArrayBuilder();
        List<Holiday> holidays = calendarService.getHolidaysOnlyForRegion(begin, end, region);
        for (Holiday holiday : holidays) {
            builder.withElement(aStringBuilder(dateToString(holiday.getCalDate().getCalDate(), VIEW_DATE_PATTERN)));
        }
        return builder;
    }

    public JsonArrayNodeBuilder createVacationsNode(Employee employee, List<Vacation> vacationList) {
        JsonArrayNodeBuilder vacationNode = anArrayBuilder();
        for (Vacation vacation : vacationList) {
            if (vacation.getEmployee().equals(employee)) {
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

    public String getHolidayListJSON(Date beginDate, Date endDate) {
        final JsonArrayNodeBuilder result = anArrayBuilder();
        // т.к. отпуска могут начинаться ранее или позднее заданных дат, то на всякий случай прибавим к диапазону
        // по месяцу с обоих концов
        List<Holiday> holidays = calendarService.getHolidaysForRegion(DateUtils.addDays(beginDate, -30),
                DateUtils.addDays(endDate, 30),
                null);
        for (Holiday holiday : holidays) {
            result.withElement(aStringBuilder(
                    dateToString(
                            holiday.getCalDate().getCalDate(), VIEW_DATE_PATTERN)));
        }
        return JsonUtil.format(result);
    }

    @Transactional
    public String approveVacation(Integer vacationId) {
        Vacation vacation = findVacation(vacationId);
        Set<VacationApproval> vacationApprovals = vacation.getVacationApprovals();

        Date responseDate = new Date();

        vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()));
        store(vacation);

        vacationApprovalProcessService.sendBackDateVacationApproved(vacation);

        if (!vacationApprovals.isEmpty()) {
            for (VacationApproval vacationApproval : vacationApprovals) {
                vacationApproval.setResult(true);
                vacationApproval.setResponseDate(responseDate);
                StringBuilder comment = new StringBuilder("Согласовано ");
                if (isVacationApprovePermission(vacation)) {
                    comment.append("РЦК");
                } else if (request.isUserInRole(ROLE_ADMIN)) {
                    comment.append("Администратором");
                }
                vacationApproval.setComment(comment.toString());
                vacationApprovalService.store(vacationApproval);

            }
        }

        return JsonUtil.format(anObjectBuilder().
                withField("isApproved", JsonNodeBuilders.aTrueBuilder()));
    }

    public Map<Vacation, Integer> getCalDays(List<Vacation> vacations) {
        Map<Vacation, Integer> calDays = new HashMap<Vacation, Integer>(vacations.size());
        for (Vacation vacation : vacations) {
            Integer diffInDays = DateTimeUtil.getDiffInDays(vacation.getBeginDate(), vacation.getEndDate());
            calDays.put(vacation, diffInDays);
        }
        return calDays;
    }

    public Map<Vacation, Integer> getWorkDays(Map<Vacation, Integer> calDays, List<Holiday> holidays) {
        Map<Vacation, Integer> workDays = new HashMap<Vacation, Integer>(calDays.size());

        for (Map.Entry<Vacation, Integer> entry : calDays.entrySet()) {
            Vacation vacation = entry.getKey();
            Integer calDaysInVacation = entry.getValue();
            Region emplRegion = vacation.getEmployee().getRegion();
            final int holidaysCount = getHolidaysCount(holidays, emplRegion, vacation.getBeginDate(), vacation.getEndDate());
            final int workDaysCount = calDaysInVacation - holidaysCount;
            workDays.put(vacation, workDaysCount);
        }

        return workDays;
    }

    /* определяет по дате началу отпуска принадлежность году */
    private Boolean vacationStatusInThisYear(Vacation vacation, int year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(vacation.getBeginDate());
        return cal.get(Calendar.YEAR) == year;
    }

    // возвращает список отпусков за год определенного типа с указанием количества календарных или рабочих дней
    // в зависимости от того, какой список был передан
    private Map<Vacation, Integer> getDaysForYearByType(Map<Vacation, Integer> days, int year, DictionaryItem type) {
        Map<Vacation, Integer> daysForYearByType = new HashMap<Vacation, Integer>();
        for (Map.Entry<Vacation, Integer> entry : days.entrySet()) {
            Vacation vacation = entry.getKey();
            Integer calDaysInVacation = entry.getValue();

            if (vacationStatusInThisYear(vacation, year) &&
                    type.getId().equals(vacation.getType().getId())) {
                daysForYearByType.put(vacation, calDaysInVacation);
            }
        }
        return daysForYearByType;
    }

    // возвращает количество дней в списке отпусков (в зависимости от того какой список был передан)
    private int getSummaryDaysCount(Map<Vacation, Integer> days) {
        int summaryDays = 0;
        for (Map.Entry<Vacation, Integer> entry : days.entrySet()) {
            Vacation vacation = entry.getKey();
            Integer daysInVacation = entry.getValue();
            if (VacationStatusEnum.APPROVED.getId() == vacation.getStatus().getId() ||
                    VacationTypesEnum.PLANNED.getId() == vacation.getType().getId()
                    ) {
                summaryDays += daysInVacation;
            }
        }
        return summaryDays;
    }

    public List<VacationInYear> getSummaryDaysCountByYearAndType(List<DictionaryItem> vacationTypes,
                                                                 int firstYear, int lastYear,
                                                                 Map<Vacation, Integer> calDays,
                                                                 Map<Vacation, Integer> workDays) {
        List<VacationInYear> result = new ArrayList<VacationInYear>();
        for (DictionaryItem vacationType : vacationTypes) {
            for (int year = firstYear; year <= lastYear; year++) {
                Map<Vacation, Integer> calDaysForYearByType = getDaysForYearByType(calDays, year, vacationType);
                Map<Vacation, Integer> workDaysForYearByType = getDaysForYearByType(workDays, year, vacationType);
                int summaryCalDays = getSummaryDaysCount(calDaysForYearByType);
                int summaryWorkDays = getSummaryDaysCount(workDaysForYearByType);
                result.add(new VacationInYear(vacationType.getValue(), year, summaryCalDays, summaryWorkDays));
            }
        }
        return result;
    }

    // сортирует список отпусков по ФИО сотрудников, а внутри группы по сотруднику - по типу отпусков
    private void sortVacations(List<Vacation> vacations) {
        Collections.sort(vacations, new Comparator() {
            @Override
            public int compare(Object v1, Object v2) {
                String employeeName1 = ((Vacation) v1).getEmployee().getName();
                String employeeName2 = ((Vacation) v2).getEmployee().getName();
                int compareRes = employeeName2.compareTo(employeeName1);
                if (compareRes == 0) {
                    Integer typeId1 = ((Vacation) v1).getType().getId();
                    Integer typeId2 = ((Vacation) v2).getType().getId();
                    return typeId1.compareTo(typeId2);
                }
                return employeeName1.compareTo(employeeName2);
            }
        });
    }

    public List<Vacation> getVacationList(VacationsForm vacationsForm) {
        Integer divisionId = vacationsForm.getDivisionId();
        Integer employeeId = vacationsForm.getEmployeeId();
        Date dateFrom = DateTimeUtil.parseStringToDateForDB(vacationsForm.getCalFromDate());
        Date dateTo = DateTimeUtil.parseStringToDateForDB(vacationsForm.getCalToDate());
        Integer projectId = vacationsForm.getProjectId();
        Integer managerId = vacationsForm.getManagerId();
        List<Integer> regions = vacationsForm.getRegions();
        DictionaryItem vacationType = vacationsForm.getVacationType() != 0 ?
                dictionaryItemService.find(vacationsForm.getVacationType()) : null;

        List<Vacation> vacations = new ArrayList<Vacation>();
        if (employeeId != null && employeeId != ALL_VALUE) {
            vacations.addAll(findVacations(employeeId, dateFrom, dateTo, vacationType));
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
            vacations.addAll(findVacations(employees, dateFrom, dateTo, vacationType));
        }
        sortVacations(vacations);
        return vacations;
    }

    public String checkVacationCountDaysJSON(String beginDateString, String endDateString, Integer employeeId, Integer vacationTypeId) {
        Employee employee = employeeService.find(employeeId);

        final Timestamp beginDate = DateTimeUtil.stringToTimestamp(beginDateString, CreateVacationForm.DATE_FORMAT);
        final Timestamp endDate = DateTimeUtil.stringToTimestamp(endDateString, CreateVacationForm.DATE_FORMAT);

        Integer factCount = getVacationDaysCountForPeriod(beginDateString, employeeId, VacationTypesEnum.WITH_PAY.getId());
        Integer planCount = getVacationDaysCountForPeriod(beginDateString, employeeId, VacationTypesEnum.PLANNED.getId());

        //Получаем кол-во дней в отпуске за исключением неучитываемых праздников
        Integer vacationDayCountExCons = calendarService.getCountDaysForPeriodForRegionExConsiderHolidays(
                beginDate,
                endDate,
                employee.getRegion()
        );

        boolean factError = factCount - vacationDayCountExCons < 0 && vacationTypeId.equals(VacationTypesEnum.WITH_PAY.getId());
        boolean planError = planCount - vacationDayCountExCons < 0;

        JsonObjectNodeBuilder builder = anObjectBuilder().
                withField("error", factError ? JsonUtil.aNumberBuilder(-1) : planError ? JsonUtil.aNumberBuilder(1)  : JsonUtil.aNumberBuilder(0)).
                withField("message",aStringBuilder(
                        factError ? String.format("Внимание! Вы не можете запланировать отпуск на количество дней, больше %s дней", factCount.toString()) :
                        planError ? "Внимание! Создаваемый отпуск превышает допустимое количество дней. Продолжить?" : ""
                ));

        return JsonUtil.format(builder);
    }

    public String getVacationDaysCountForPeriodJSON(String beginDateString, Integer employeeId, Integer vacationTypeId) {
        Integer count = getVacationDaysCountForPeriod(beginDateString, employeeId, vacationTypeId);

        JsonObjectNodeBuilder builder = anObjectBuilder().
                withField("vacation_days_count", count == null ? aNullBuilder() : aNumberBuilder(count.toString()));

        return JsonUtil.format(builder);
    }

    private Integer getVacationDaysCountForPeriod(String beginDateString, Integer employeeId, Integer vacationTypeId) {
        Employee employee = employeeService.find(employeeId);
        final Timestamp beginDate = DateTimeUtil.stringToTimestamp(beginDateString, CreateVacationForm.DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(employee.getStartDate());
        Integer beginDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(beginDate);
        Integer vacDay = calendar.get(Calendar.DAY_OF_MONTH);
        Integer month = calendar.get(Calendar.MONTH);
        Integer year = calendar.get(Calendar.YEAR);
        //  Если день начала отпуска меньше даты устройства на работу
        //  то оставляем месяц без изменений т.к. calendar.get(Calendar.MONTH) дает -1 один месяц
        if (vacDay > beginDay) {
            ++month;
        }
        Integer count = null;
        if (vacationTypeId.equals(VacationTypesEnum.WITH_PAY.getId())) {
            count = getFactVacationDaysCount(employee, year, month);
        } else {
            count = getPlanVacationDaysCount(employee, year, month);
        }
        return count;
    }

    public Integer getPlanVacationDaysCount(Employee employee, Integer year, Integer month) {
        return getVacationDaysCount(employee, year, month, false);
    }

    public Integer getFactVacationDaysCount(Employee employee, Integer year, Integer month) {
        return getVacationDaysCount(employee, year, month, true);
    }

    public Integer getVacationDaysCount(Employee employee, Integer year, Integer month, Boolean fact) {
        VacationDays vacationDays = vacationDaysService.findByEmployee(employee);
        if (vacationDays != null) {
            Calendar calendarAct = Calendar.getInstance();
            calendarAct.setTime(employee.getStartDate());

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, --month);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_MONTH, calendarAct.get(Calendar.DAY_OF_MONTH));

            Date start = vacationDays.getActualizationDate();
            Date end = calendar.getTime();

            if (start.after(end)) {
                return 0;
            }

            int months = DateTimeUtil.getDiffInMonths(start, end);
            int vacDays = (int) (++months * VACATION_KOEF);
            vacDays += vacationDays.getCountDays();
            Integer vacationsCountByPeriod;
            if (!fact) {
                vacationsCountByPeriod = getPlannedVacationsCountByPeriod(employee, vacationDays.getActualizationDate());
            } else {
                vacationsCountByPeriod = getFactVacationsCountByPeriod(employee, vacationDays.getActualizationDate());
            }
            if (vacDays > 0) {
                vacDays -= vacationsCountByPeriod;
                return vacDays;
            } else {
                return vacDays;
            }
        }
        return null;
    }

    public Integer getPlannedVacationsCountByPeriod(Employee employee, Date beginDate) {
        return vacationDAO.getVacationsCountByPeriod(employee, beginDate, false);
    }


    public Integer getFactVacationsCountByPeriod(Employee employee, Date beginDate) {
        return vacationDAO.getVacationsCountByPeriod(employee, beginDate, true);
    }

    public String getVacActualizationDate(Employee employee, Integer year, Integer month) {
        Calendar calendarAct = Calendar.getInstance();
        calendarAct.setTime(employee.getStartDate());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, --month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, calendarAct.get(Calendar.DAY_OF_MONTH));
        return DateTimeUtil.formatDateIntoViewFormat(calendar.getTime());
    }
}

