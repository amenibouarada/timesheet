package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.service.DictionaryItemService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Список заявлений на отпуск для конкретного сотрудника
     */
    public List<Vacation> findVacations(Integer employeeId, Integer year) {
        final Query query =
                entityManager.createQuery("from Vacation v where v.employee.id = :emp_id and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("year", year);

        return query.getResultList();
    }

    public List<Vacation> findVacations(Integer employeeId, Date beginDate, Date endDate, DictionaryItem typeId){
        final Query query = typeId != null ?
                entityManager.createQuery("from Vacation v where v.employee.id = :emp_id and v.beginDate <= :endDate " +
                        "and v.endDate >= :beginDate and v.type = :typeId order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("beginDate", beginDate)
                        .setParameter("endDate", endDate).setParameter("typeId", typeId) :
                entityManager.createQuery("from Vacation v where v.employee.id = :emp_id and v.beginDate <= :endDate " +
                        "and v.endDate >= :beginDate order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("beginDate", beginDate)
                        .setParameter("endDate", endDate);
        return query.getResultList();
    }

    public List<Vacation> findVacations(Integer year, Integer month, Integer employeeId) {
        final Query query =
                entityManager.createQuery("from Vacation v " +
                        "where v.employee.id = :emp_id " +
                        "and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) " +
                        "and (MONTH(v.beginDate) = :month or MONTH(v.endDate) = :month) order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("year", year).setParameter("month",month);

        return query.getResultList();
    }

    public List<Vacation> findVacationsByTypes(Integer year, Integer month, Integer employeeId,  List<DictionaryItem> types) {
        final Query query =
                entityManager.createQuery("from Vacation v " +
                        "where v.employee.id = :emp_id " +
                        "and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) " +
                        "and (MONTH(v.beginDate) = :month or MONTH(v.endDate) = :month) " +
                        "and v.type in :types order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("year", year).setParameter("month",month).setParameter("types",types);

        return query.getResultList();
    }

    public List<Vacation> findVacationsByTypesAndStatuses(Integer year, Integer month, Integer employeeId,  List<DictionaryItem> types, List<DictionaryItem> statuses) {
        final Query query =
                entityManager.createQuery("from Vacation v " +
                        "where v.employee.id = :emp_id " +
                        "and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) " +
                        "and (MONTH(v.beginDate) = :month or MONTH(v.endDate) = :month) " +
                        "and v.type in :types " +
                        "and v.status in :statuses " +
                        "order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("year", year).setParameter("month",month).setParameter("types",types).setParameter("statuses",statuses);

        return query.getResultList();
    }

    public List<Vacation> findVacationsByType(Integer year, Integer month, Integer employeeId,  DictionaryItem type) {
        final Query query =
                entityManager.createQuery("from Vacation v " +
                        "where v.employee.id = :emp_id " +
                        "and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) " +
                        "and (MONTH(v.beginDate) = :month or MONTH(v.endDate) = :month) " +
                        "and v.type = :type order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("year", year).setParameter("month",month).setParameter("type",type);

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
                setParameter("status_id", VacationStatusEnum.REJECTED.getId()).setParameter("emp_id", employeeId).setParameter("type",typeVacation);;

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
                setParameter("status_id", VacationStatusEnum.REJECTED.getId()).setParameter("emp_id", employeeId).setParameter("type",typeVacation);

        return (Long) query.getSingleResult();
    }

    public Vacation findVacation(Integer vacationId) {
        final Query query = entityManager.createQuery("from Vacation v where v.id = :id").setParameter("id", vacationId);
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

    public List<Integer> getAllNotApprovedVacationsIds() {
        return entityManager.createQuery("select v.id from Vacation as v where v.status.id in :notApprovedStatuses")
                .setParameter("notApprovedStatuses", VacationStatusEnum.getNotApprovedStatuses()).getResultList();
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
            //typesVac.remove(dictionaryItemService.find(VacationTypesEnum.WITH_NEXT_WORKING.getId()));

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
                        "where (m.id = :emp_id ) and (s.id not in (:statuses)) " +
                        "order by v.beginDate")
                        .setParameter("emp_id", employeeId)
                        .setParameter("statuses", statuses);

        return query.getResultList();
    }
}
