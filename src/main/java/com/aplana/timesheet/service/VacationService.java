package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.exception.service.NotDataForYearInCalendarException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.CreateVacationForm;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.vacationapproveprocess.VacationApprovalProcessService;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import com.aplana.timesheet.service.helper.ViewReportHelper;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.*;
import java.util.Calendar;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anArrayBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
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
    private ViewReportHelper viewReportHelper;

    @Autowired
    protected CalendarService calendarService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private TSPropertyProvider propertyProvider;

    private static final Logger logger = LoggerFactory.getLogger(VacationService.class);

    public static final String CANT_GET_EXIT_TO_WORK_EXCEPTION_MESSAGE = "Не удалось получить дату выхода из отпуска и количество дней.";

    @Transactional
    public void store(Vacation vacation) {
        vacationDAO.store(vacation);
    }

    @Transactional
    public Boolean isDayVacation(Employee employee, Date date){
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

    public List<Vacation> findVacations(List<Employee> employees, Date beginDate, Date endDate, DictionaryItem typeId){
        List<Vacation> vacations = new ArrayList<Vacation>();
        for (Employee employee : employees){
            List<Vacation> empVacation = findVacations(employee.getId(), beginDate, endDate, typeId);
            vacations.addAll(empVacation);
        }
        return vacations;
    }

    public List<Vacation> findVacationsByTypes(Integer year, Integer month, Integer employeeId,  List<DictionaryItem> types) {
        return vacationDAO.findVacationsByTypes(year,  month,  employeeId, types);
    }

    public List<Vacation> findVacationsByTypesAndStatuses(Integer year, Integer month, Integer employeeId,  List<DictionaryItem> types, List<DictionaryItem> statuses) {
        return vacationDAO.findVacationsByTypesAndStatuses(year, month, employeeId, types, statuses);
    }

    public List<Vacation> findVacationsByType(Integer year, Integer month, Integer employeeId,  DictionaryItem type) {
        return vacationDAO.findVacationsByType(year, month, employeeId, type);
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
    public void deleteVacation(Integer vacationId) {
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
            throw new DeleteVacationException(String.format(
                    "Нельзя удалить заявление на отпуск. Для удаления данного заявления " +
                            "необходимо написать на timesheet@aplana.com"));
        }
    }

    @Transactional
    public Vacation findVacation(Integer vacationId) {
        return vacationDAO.findVacation(vacationId);
    }


    public int getVacationsWorkdaysCount(Employee employee, Integer year, Integer month, VacationStatusEnum status) {
        int vacationsWorkdaysCount = vacationDAO.getVacationsWorkdaysCount(employee, year, month, VacationStatusEnum.APPROVED, null, true);
        return vacationsWorkdaysCount;
    }

    public Double getVacationsWorkdaysCount(Employee employee, Integer year, Integer month) throws NotDataForYearInCalendarException {
         return viewReportHelper.getCountVacationAndPlannedVacationDays(year,month,employee.getId()).doubleValue();
    }

    public Map<DictionaryItem, List<Vacation>> splitVacationByTypes(List<Vacation> vacations) {
        Map<DictionaryItem, List<Vacation>> map = new HashMap<DictionaryItem, List<Vacation>>();
        for (Vacation vac: vacations){
            if(map.keySet().contains(vac.getType())){
                map.get(vac.getType()).add(vac);
            } else{
                map.put(vac.getType(), Lists.newArrayList(vac));
            }
        }
        return map;
    }

    public List<Vacation> findVacationsNeedsApproval(Integer employeeId) {
        return vacationDAO.findVacationsNeedApproval(employeeId);
    }

    public Integer findVacationsNeedsApprovalCount(Integer employeeId) {
        return vacationDAO.findVacationsNeedApproval(employeeId).size();
    }

    public void createAndMailngVacation(CreateVacationForm createVacationForm, Employee employee, Employee curEmployee, boolean isApprovedVacation)
            throws VacationApprovalServiceException {

        final Vacation vacation = new Vacation();

        vacation.setCreationDate(new Date());
        vacation.setBeginDate(DateTimeUtil.stringToTimestamp(createVacationForm.getCalFromDate()));
        vacation.setEndDate(DateTimeUtil.stringToTimestamp(createVacationForm.getCalToDate()));
        vacation.setComment(createVacationForm.getComment().trim());
        vacation.setType(dictionaryItemService.find(createVacationForm.getVacationType()));
        vacation.setAuthor(curEmployee);
        vacation.setEmployee(employee);

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
                logger.error("Transaction rollbacked. Error saving vacation: {} ",e);
            } else {
                logger.error("TransactionStatus is null.");
            }
        }
        sendMailService.performVacationCreateMailing(vacation);
    }

    private boolean needsToBeApproved(Vacation vacation) {
        return !vacation.getStatus().getId().equals(VacationStatusEnum.APPROVED.getId());
    }

    public String getExitToWorkAndCountVacationDayJson(String beginDate,String endDate, Integer employeeId, Integer vacationTypeId){
        final JsonObjectNodeBuilder builder = anObjectBuilder();
        try {
            final Timestamp endDateT = DateTimeUtil.stringToTimestamp(endDate, CreateVacationForm.DATE_FORMAT);
            final Timestamp beginDateT = DateTimeUtil.stringToTimestamp(beginDate, CreateVacationForm.DATE_FORMAT);
            //Получаем день выхода на работу
            Date nextWorkDay = null;
            try {
                nextWorkDay = viewReportHelper.getNextWorkDay(endDateT, employeeId, null);
            } catch (NotDataForYearInCalendarException e) {
                String message = e.getMessage();
                logger.error("Error in getExitToWorkAndCountVacationDayJson : "+ message);
                builder.withField("error", aStringBuilder(message));
                String outString = JsonUtil.format(builder);
                return outString;
            }
            String format = DateFormatUtils.format(nextWorkDay, CreateVacationForm.DATE_FORMAT);
            builder.withField("exitDate", aStringBuilder(format));
            Employee emp = employeeService.find(employeeId);
            //Получаем кол-во выходных в отпуске
            final List<Holiday> holidaysForRegion =
                    calendarService.getHolidaysForRegion(beginDateT, endDateT, emp.getRegion());
            final Integer holidaysCount = getHolidaysCount(holidaysForRegion,beginDateT, endDateT);
            //Получаем кол-во дней в отпуске
            Integer vacationDayCount = DateTimeUtil.getAllDaysCount(beginDateT, endDateT).intValue();
            //Получаем кол-во дней в отпуске за исключением выходых
            Integer vacationWorkCount=0;
            if (vacationDayCount > 0) {
                vacationWorkCount = vacationDayCount - holidaysCount;
            }
            builder.withField("vacationWorkDayCount", aStringBuilder(vacationWorkCount.toString()));
            builder.withField("vacationDayCount", aStringBuilder((vacationDayCount<=0)?"0":vacationDayCount.toString()));
            /* проверка на необходимость вывода информ сообщения о попадании
            *  на пятницу для отпуска с сохранением содержания */
            if (vacationTypeId != null &&
                    vacationTypeId == VacationTypesEnum.WITH_PAY.getId() &&
                    vacationDayCount > propertyProvider.getVacantionFridayInformDays()) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(endDateT);

                if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
                    builder.withField("vacationFridayInform", aStringBuilder("true"));
            }

            String outString = JsonUtil.format(builder);
            return outString;
        } catch (Exception th) {
            logger.error(CANT_GET_EXIT_TO_WORK_EXCEPTION_MESSAGE, th);
            return CANT_GET_EXIT_TO_WORK_EXCEPTION_MESSAGE;
        }
    }

    /**
     * Вычисление кол-ва выходных дней в заданном периоде
     * @param holidaysForRegion
     * @param beginDate
     * @param endDate
     * @return  кол-ва выходных дней в заданном периоде
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

    @Transactional
    public Boolean isDayVacationWithoutPlanned(Employee employee, Date date){
        Boolean dayVacationWithoutPlanned = vacationDAO.isDayVacationWithoutPlanned(employee, date);
        return dayVacationWithoutPlanned;
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
                   ){
                    return Boolean.TRUE;
                }
                /* пользователь создатель или отпускник и статус не отклонено и не утверждено */
                if ( (vacation.getEmployee().equals(employee) || vacation.getAuthor().equals(employee) ) &&
                        vacation.getStatus().getId() != VacationStatusEnum.REJECTED.getId() &&
                        vacation.getStatus().getId() != VacationStatusEnum.APPROVED.getId()
                   ) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    public String getVacationListByRegionJSON(List<Vacation> vacationList ) {
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

    public JsonArrayNodeBuilder createVacationsNode(Employee employee, List<Vacation> vacationList){
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

    public String getHolidayListJSON(Date beginDate, Date endDate){
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

}
