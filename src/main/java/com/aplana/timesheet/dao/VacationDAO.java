package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.service.DictionaryItemService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.Lists;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.aplana.timesheet.enums.VacationStatusEnum.APPROVED;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Repository
public class VacationDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    private static final Logger logger = LoggerFactory.getLogger(VacationDAO.class);

    private static final String VACATIONS_ORDER = " order by v.beginDate";
    private static final String VACATIONS_FOR_EMPLOYEES =
            "from Vacation v where v.employee.id = :emp_id and v.beginDate <= :endDate and v.endDate >= :beginDate ";
    private static final String VACATIONS_FOR_EMPLOYEES_WITH_TYPE = VACATIONS_FOR_EMPLOYEES + " and v.type = :typeId ";
    private static final String VACATIONS_FOR_EMPLOYEES_WITH_STATUS = VACATIONS_FOR_EMPLOYEES + " and v.status = :status ";

    public List<Vacation> findVacations(Integer employeeId, Date beginDate, Date endDate, DictionaryItem typeId){
        String queryString = typeId != null ? VACATIONS_FOR_EMPLOYEES_WITH_TYPE : VACATIONS_FOR_EMPLOYEES;
        queryString = queryString + VACATIONS_ORDER;
        final Query query = entityManager.createQuery(queryString)
                        .setParameter("emp_id", employeeId).setParameter("beginDate", beginDate)
                        .setParameter("endDate", endDate);
        if (typeId != null) query.setParameter("typeId", typeId);

        return query.getResultList();
    }

    public List<Vacation> findVacations(List<Employee> employees, Date beginDate, Date endDate, DictionaryItem typeId){
        String queryString = "from Vacation v where v.employee in :employees and v.beginDate <= :endDate " +
                        "and v.endDate >= :beginDate ";
        queryString = queryString + (typeId != null ? " and v.type = :typeId " : "");
        queryString = queryString + VACATIONS_ORDER;
        final Query query = entityManager.createQuery(queryString)
                        .setParameter("employees", employees)
                        .setParameter("beginDate", beginDate)
                        .setParameter("endDate", endDate);
        if (typeId != null) query.setParameter("typeId", typeId);
        return query.getResultList();
    }

    /**
     * Поиск утвержденных вакансий сотрудника за отрезок времени
     * @param employeeId идентификатор сотрудника
     * @param beginDate Начало отрезка дат за который происходит отпуск
     * @param endDate конец отрезка дат за который происходит отпуск
     * @param status ентити справочника статусов отпуска.
     * @see com.aplana.timesheet.enums.VacationStatusEnum
     * @return Список утверженных отпусков
     */
    public List<Vacation> findVacationsByStatus(Integer employeeId, Date beginDate, Date endDate, DictionaryItem status){
        final Query query = entityManager.createQuery(VACATIONS_FOR_EMPLOYEES_WITH_STATUS + VACATIONS_ORDER)
                .setParameter("emp_id", employeeId)
                .setParameter("beginDate", beginDate)
                .setParameter("endDate", endDate)
                .setParameter("status", status);
        return query.getResultList();
    }

    public List<Vacation> findVacationsByTypes(Integer year, Integer month, Integer employeeId,  List<DictionaryItem> types) {
        Calendar calendar = DateTimeUtil.getCalendar(year, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDay = calendar.getTime();
        Date lastDay = DateTimeUtil.stringToDate(DateTimeUtil.getLastDayOfMonth(new Timestamp(calendar.getTime().getTime())), DateTimeUtil.DB_DATE_PATTERN);

        final Query query =
                entityManager.createQuery("from Vacation v " +
                        "where v.employee.id = :emp_id " +
                        "and (v.endDate >= :firstDay and v.beginDate <= :lastDay) " +
                        "and v.type in :types order by v.beginDate")
                        .setParameter("emp_id", employeeId).
                        setParameter("firstDay", firstDay).
                        setParameter("lastDay",lastDay).
                        setParameter("types",types);

        return query.getResultList();
    }

    public List<Vacation> findVacationsByTypesAndStatuses(Integer year, Integer month, Integer employeeId,  List<DictionaryItem> types, List<DictionaryItem> statuses) {
        Calendar calendar = DateTimeUtil.getCalendar(year, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDay = calendar.getTime();
        Date lastDay = DateTimeUtil.stringToDate(DateTimeUtil.getLastDayOfMonth(new Timestamp(calendar.getTime().getTime())), DateTimeUtil.DB_DATE_PATTERN);

        final Query query =
                entityManager.createQuery("from Vacation v " +
                        "where v.employee.id = :emp_id " +
                        "and (v.endDate >= :firstDay and v.beginDate <= :lastDay) " +
                        "and v.type in :types " +
                        "and v.status in :statuses " +
                        "order by v.beginDate")
                        .setParameter("emp_id", employeeId).
                        setParameter("firstDay", firstDay).
                        setParameter("lastDay",lastDay).
                        setParameter("types",types).
                        setParameter("statuses",statuses);

        return query.getResultList();
    }

    public List<Vacation> findVacationsByType(Integer year, Integer month, Integer employeeId,  DictionaryItem type) {
        final Query query =
                entityManager.createQuery("from Vacation v " +
                        "where v.employee.id = :emp_id " +
                        "and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) " +
                        "and (MONTH(v.beginDate) = :month or MONTH(v.endDate) = :month) " +
                        "and v.type = :type order by v.beginDate")
                        .setParameter("emp_id", employeeId).
                        setParameter("year", year).
                        setParameter("month",month).
                        setParameter("type",type);

        return query.getResultList();
    }

    public void store(Vacation vacation) {
        final Vacation mergedVacation = entityManager.merge(vacation);

        entityManager.flush();

        vacation.setId(mergedVacation.getId());
    }

    public void delete(Vacation vacation) {
        Hibernate.initialize(vacation);

        entityManager.remove(vacation);
    }

    public Long getIntersectVacationsCount(Integer employeeId, Date fromDate, Date toDate, DictionaryItem typeVacation) {
        final Query query = entityManager.createQuery(
                "select count(*) as c " +
                        "from Vacation v, DictionaryItem di " +
                        "where di.id = :status_id and ((:from_date between v.beginDate and v.endDate) or (:to_date between v.beginDate and v.endDate) or (v.beginDate between :from_date and :to_date))" +
                        " and not v.status = di and v.employee.id = :emp_id and v.type <> :type"
        ).setParameter("from_date", fromDate).setParameter("to_date", toDate).
                setParameter("status_id", VacationStatusEnum.REJECTED.getId()).
                setParameter("emp_id", employeeId).
                setParameter("type",typeVacation);

        return (Long) query.getSingleResult();
    }

    public Long getIntersectPlannedVacationsCount(Integer employeeId, Date fromDate, Date toDate, DictionaryItem typeVacation) {
        final Query query = entityManager.createQuery(
                "select count(*) as c " +
                        "from Vacation v, DictionaryItem di " +
                        "where di.id = :status_id and ((:from_date between v.beginDate and v.endDate) or " +
                        "(:to_date between v.beginDate and v.endDate) or " +
                        "(v.beginDate between :from_date and :to_date))" +
                        " and not v.status = di and v.employee.id = :emp_id and v.type = :type"
        ).setParameter("from_date", fromDate).setParameter("to_date", toDate).
                setParameter("status_id", VacationStatusEnum.REJECTED.getId()).
                setParameter("emp_id", employeeId).
                setParameter("type",typeVacation);

        return (Long) query.getSingleResult();
    }

    public Vacation findVacation(Integer vacationId) {
        final Query query = entityManager.createQuery("from Vacation v inner join fetch v.employee e inner join fetch e.region where v.id = :id").setParameter("id", vacationId);
        return (Vacation) query.getSingleResult();
    }

    public Boolean isDayVacation(Employee employee, Date date){
        Query query = entityManager.createQuery(
                "SELECT i FROM Vacation AS i WHERE i.employee = :employee AND :date BETWEEN i.beginDate AND i.endDate AND i.status.id = :statusId"
        ).setParameter("employee", employee).setParameter("date", date).setParameter("statusId", APPROVED.getId());
        if (query.getResultList().isEmpty()) {
            return false;
        }
        return true;
    }

    public Boolean isDayVacationWithoutPlanned(Employee employee, Date date) {
        Query query = entityManager.createQuery(
                "SELECT i FROM Vacation AS i " +
                        "WHERE i.employee = :employee " +
                        "AND :date BETWEEN i.beginDate AND i.endDate " +
                        "AND i.status.id = :statusId " +
                        "AND i.type.id <> :typePlanned").
                setParameter("employee", employee).
                setParameter("date", date).
                setParameter("statusId", APPROVED.getId()).
                setParameter("typePlanned", VacationTypesEnum.PLANNED.getId());
        if (query.getResultList().isEmpty()) {
            return false;
        }
        return true;
    }

    public List<Integer> getAllNotApprovedVacationsIds() {
        return entityManager.createQuery("select v.id from Vacation as v where v.status.id in :notApprovedStatuses")
                .setParameter("notApprovedStatuses", VacationStatusEnum.getNotApprovedStatuses()).getResultList();
    }



    // todo наhql
    public int getVacationsWorkdaysCount(Employee employee, Integer year, Integer month) {
        Query query = entityManager.createNativeQuery("select " +
                "  count(distinct c.caldate) " +
                "from " +
                "  employee e " +
                "  inner join vacation v on (e.id = v.employee_id) " +
                "  inner join calendar c on (c.caldate between v.begin_date and v.end_date) " +
                "  left join holiday h on (h.caldate = c.caldate and (h.region = e.region or h.region is null)) " +
                "where " +
                "  v.employee_id = :employee_id " +
                "  and h.id is null " +
                "  and month = :month and year = :year " +
                "  and status_id != "+ VacationStatusEnum.REJECTED.getId()+" ");

        query.setParameter("employee_id", employee.getId()) ;
        query.setParameter("month", month);
        query.setParameter("year", year);

        return ((Number)query.getSingleResult()).intValue();
    }

    /**
     * Метод считает количество дней утвержденных отпусков в месяце без учета планируемых
     * @param employee
     * @param year
     * @param month
     * @param status - статус отпуска, захардкожено "Утвержден"
     * @param typeVacation - тип отпуска
     * @param withoutPlannedAndNextWork - не учитывать "Планируемые отпуска"
     * @return
     */
    public int getVacationsWorkdaysCount(Employee employee, Integer year, Integer month, VacationStatusEnum status,
                                         VacationTypesEnum typeVacation, Boolean withoutPlannedAndNextWork) {
        /*
            Здравствуй, мой юный друг! Я понимаю, в каком ты пребываешь состоянии от ниже написанных строчек кода, но,
            пожалуйста, если ты знаешь, как сделать рабочий вариант на HQL - сделай это за меня.

            P.S.: проблема в том, что вариант на HQL ВСЕГДА возвращает 0.
        */

        String textQuery = "select" +
                "        (count(c) - count(h)) as days" +
                "    from" +
                "        vacation as v" +
                "    left outer join calendar as c on (date_trunc('month', c.caldate) = {ts '%1$s'}) and (c.caldate between v.begin_date and v.end_date)" +
                "    left outer join holiday as h on (c.caldate = h.caldate) and (h.region is null or h.region = :region)" +
                "    where" +
                "        v.employee_id = :employee_id" +
                "        and v.status_id = :status_id" +
                "        and {ts '%1$s'} between date_trunc('month', v.begin_Date) and date_trunc('month', v.end_Date)";

        if (typeVacation != null) {
            textQuery += "and v.type_id = :type_id";
        }

        if (typeVacation == null && withoutPlannedAndNextWork) {
            textQuery += "and v.type_id in :types_id";
        }

        final Query query = entityManager.createNativeQuery(
                String.format(
                        textQuery,
                        String.format("%d-%d-1", year, month)
                )
        ).setParameter("employee_id", employee.getId()).setParameter("status_id", status.getId()).
                setParameter("region", employee.getRegion().getId());

        if (typeVacation != null) {
            query.setParameter("type_id", typeVacation.getId());
        }

        if (typeVacation == null && withoutPlannedAndNextWork) {
            List<DictionaryItem> typesVac = dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId());

            typesVac.remove(dictionaryItemService.find(VacationTypesEnum.PLANNED.getId()));

            query.setParameter("types_id", typesVac) ;
        }
        return ((Number) query.getSingleResult()).intValue();
    }

    public Vacation tryFindVacation(Integer vacationId) {
        try {
            return findVacation(vacationId);
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<Vacation> findVacationsNeedApproval(Integer employeeId) {
        List<Integer> statuses = Lists.newArrayList(VacationStatusEnum.APPROVED.getId(),VacationStatusEnum.REJECTED.getId());
        final Query query =
                entityManager.createQuery("select distinct v from VacationApproval va " +
                        "left outer join va.vacation as v " +
                        "left outer join va.manager as m " +
                        "left outer join v.status as s " +
                        "where (m.id = :emp_id ) " +
                        "and (s.id not in (:statuses)) " +
                        "and va.result is null " +
                        "order by v.beginDate")
                        .setParameter("emp_id", employeeId)
                        .setParameter("statuses", statuses);

        return query.getResultList();
    }

    public List<Vacation> getPlannedVacationByBeginDate(Date date, Boolean remind) {
        Query query = entityManager.createQuery(
                "select v from Vacation v where v.type.id=:type and v.beginDate <= :date and v.remind = :remind");

        query.setParameter("type", VacationTypesEnum.PLANNED.getId());
        query.setParameter("date", date);
        query.setParameter("remind", remind);

        return query.getResultList();
    }

    public List<Vacation> getPlannedVacationByBeginDateLess(Date date) {
        Query query = entityManager.createQuery("select v from Vacation v where v.type.id=:type and v.beginDate <= :date");

        query.setParameter("type", VacationTypesEnum.PLANNED.getId());
        query.setParameter("date", date);

        return query.getResultList();
    }

    public List<Vacation> getPlannedVacationsByBeginAndEndDates(Employee employee, Date beginDate, Date endDate) {
        Query query =
                entityManager.createQuery("select v from Vacation v where v.type.id=:type and v.beginDate = :beginDate and v.endDate=:endDate and v.employee = :employee").
                setParameter("type", VacationTypesEnum.PLANNED.getId())
                .setParameter("beginDate", beginDate)
                .setParameter("employee", employee)
                .setParameter("endDate", endDate);

        return query.getResultList();
    }

    /**
     *
     * @param employee
     * @param beginDate
     * @param endDate
     * @return
     */
    public Integer getVacationsCountByPeriod(Employee employee, Date beginDate, Date endDate, Boolean fact) {

        Query query = entityManager.createNativeQuery(
                "select count(c.*) from employee e " +
                "left join vacation v on (e.id = v.employee_id) " +
                "left join calendar c on (c.caldate between v.begin_date and v.end_date) " +
                "left join holiday h on (h.caldate = c.caldate and (h.region = e.region or h.region is null)) " +
                "where " +
                "v.employee_id = :employee and " +
                        (fact ? " (v.type_id in (:type1) and v.status_id in (:status1)) " :
                                "((v.type_id in (:type1) and v.status_id in (:status1)) or (v.type_id in (:type2) and v.status_id in (:status2,:status3,:status4,:status5))) ") +
                "and c.calDate >= :beginDate and c.calDate <= :endDate " +
                "and (h.id is null or h.consider = true)");
        if (fact) {
            query.setParameter("type1", VacationTypesEnum.WITH_PAY.getId());
            query.setParameter("status1", VacationStatusEnum.APPROVED.getId());
        } else {
            query.setParameter("type1", VacationTypesEnum.PLANNED.getId());
            query.setParameter("type2", VacationTypesEnum.WITH_PAY.getId());
            query.setParameter("status1", VacationStatusEnum.CREATED.getId());
            query.setParameter("status2", VacationStatusEnum.APPROVEMENT_WITH_PM.getId());
            query.setParameter("status3", VacationStatusEnum.APPROVED_BY_PM.getId());
            query.setParameter("status4", VacationStatusEnum.APPROVEMENT_WITH_LM.getId());
            query.setParameter("status5", VacationStatusEnum.APPROVED.getId());
        }

        query.setParameter("beginDate", beginDate);
        query.setParameter("endDate", endDate);
        query.setParameter("employee", employee);

        return ((Number)query.getSingleResult()).intValue();
    }

    public void detach(Vacation vacation) {
        entityManager.detach(vacation);
    }
}
