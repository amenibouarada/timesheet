package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
}
