package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 27.01.13
 */
@Repository
public class IllnessDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Illness> getEmployeeIllness(Employee employee) {
        Query query = entityManager.createQuery("from Illness as i where i.employee = :employee order by i.beginDate");
        query.setParameter("employee", employee);

        return (List<Illness>) query.getResultList();
    }

    public void setIllness(Illness illness) {
        entityManager.merge(illness);
        entityManager.flush();
    }

    public void deleteIllness(Illness illness) {
        entityManager.remove(illness);
    }

    public void deleteIllnessById(Integer reportId) {
        Query query = entityManager.createQuery("delete from Illness as i where i.id = :id");
        query.setParameter("id", reportId);

        query.executeUpdate();
    }

    public Illness find(Integer reportId) {
        Query query = entityManager.createQuery(
                "select i from Illness as i where i.id = :id"
        ).setParameter( "id", reportId );

        return (Illness) query.getSingleResult();
    }

    public Boolean isDayIllness(Employee employee, Date date){
        Query query = entityManager.createQuery(
                "SELECT i FROM Illness AS i WHERE i.employee = :employee AND :date BETWEEN i.beginDate AND i.endDate"
        ).setParameter("employee", employee).setParameter("date", date);
        if (query.getResultList().isEmpty()) {
            return false;
        }
        return true;
    }

    public List<Illness> getEmployeeIllnessByDates(Employee employee, Date beginDate, Date endDate) {
        return entityManager.createQuery("from Illness as i " +
                "where i.employee = :employee and i.beginDate <= :endDate and i.endDate >= :beginDate")
                .setParameter("employee", employee)
                .setParameter("beginDate", beginDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public int getIllnessWorkdaysCount(Employee employee, Integer year, Integer month) {
        /*
            Здравствуй, мой юный друг! Я понимаю, в каком ты пребываешь состоянии от ниже написанных строчек кода, но,
            пожалуйста, если ты знаешь, как сделать рабочий вариант на HQL - сделай это за меня.

            P.S.: проблема в том, что вариант на HQL ВСЕГДА возвращает 0.
        */

        final Query query = entityManager.createNativeQuery(
            String.format(
                "select" +
                "        (count(c) - count(h)) as days" +
                "    from" +
                "        illness as i" +
                "    left outer join calendar as c on (date_trunc('month', c.caldate) = {ts '%1$s'}) and (c.caldate between i.begin_date and i.end_date)" +
                "    left outer join holiday as h on (c.caldate = h.caldate) and (h.region is null or h.region = :region)" +
                "    where" +
                "        i.employee_id = :employee_id" +
                "        and {ts '%1$s'} between date_trunc('month', i.begin_Date) and date_trunc('month', i.end_Date)",
                String.format("%d-%d-1", year, month)
            )
        ).setParameter("employee_id", employee.getId()).setParameter("region", employee.getRegion().getId());

        return ((Number) query.getSingleResult()).intValue();
    }
}
