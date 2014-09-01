package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.VacationDays;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Created by abayanov
 * Date: 29.08.14
 */
@Repository
public class VacationDaysDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public VacationDays save(VacationDays vacationDays) {
        return entityManager.merge(vacationDays);
    }

    public VacationDays findById(Integer id) {
        return entityManager.find(VacationDays.class, id);
    }

    public VacationDays findByEmployee(Employee employee) {
        Query query = entityManager.createQuery(
               "select vd from VacationDays vd where vd.employee = :emp"
        );
        query.setParameter("emp", employee);
        return query.getResultList().size() == 1 ? (VacationDays) query.getResultList().get(0) : null;
    }
}
