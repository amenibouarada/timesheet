package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.isEmpty;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Repository
public class EmployeeAssistantDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<EmployeeAssistant> find(Set<String> managersEmails) {
        if (isEmpty(managersEmails)) {
            throw new NoResultException();
        }

        final Query query = entityManager.createQuery(
                "from EmployeeAssistant ea where ea.employee.email in :emails"
        ).setParameter("emails", managersEmails);

        return query.getResultList();
    }

    public List<EmployeeAssistant> tryFind(Set<String> managersEmails) {
        try {
            return find(managersEmails);
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Transactional
    public int updateEmployeeAssistantActiveStatus(){
        final Query query = entityManager.createNativeQuery(
                "UPDATE employee_assistant eaMain SET active = (                    " +
                "    CASE WHEN eaMain.active IS NOT NULL THEN                       " +
                "        (                                                          " +
                "            SELECT (v.id IS NOT NULL)                              " +
                "            FROM employee_assistant ea                             " +
                "            LEFT JOIN vacation v ON                                " +
                "               ea.employee_id = v.employee_id                      " +
                "               AND :date BETWEEN v.begin_date AND v.end_date       " +
                "               AND v.status_id != :cancelStatus                    " +
                "               AND v.type_id != :planedType                        " +
                "            WHERE ea.id = eaMain.id                                " +
                "        )                                                          " +
                "    END                                                            " +
                ")                                                                  "
        )
        .setParameter("date", new Date())
        .setParameter("cancelStatus", VacationStatusEnum.REJECTED.getId())
        .setParameter("planedType", VacationTypesEnum.PLANNED.getId());

        return query.executeUpdate();
    }
}
