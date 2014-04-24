package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.EmployeeProjectBillable;
import com.aplana.timesheet.dao.entity.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author dsysterov
 * @version 1.0
 */
@Repository
public class EmployeeProjectBillableDAO {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDAO.class);

    @PersistenceContext
    private EntityManager entityManager;

    public EmployeeProjectBillable find(Integer id) {
        if (id == null) {
            logger.warn("For unknown reasons, the Employee ID is null.");
            return null;
        }

        return entityManager.find(EmployeeProjectBillable.class, id);
    }

    /**
     * Возвращает все отметки о возможности списания занятости по проекту.
     * @param project Проект
     * @return Список отметок
     */
    public List<EmployeeProjectBillable> findByProject(Project project) {
        Query query = entityManager.createQuery(
                "select b from EmployeeProjectBillable b where b.project = :project order by b.id")
                .setParameter("project", project);

        return query.getResultList();
    }

    /**
     * Удаляет из базы запись о возможности списания занятости.
     * @param projectBillable Запись о возможности списания занятости
     */
    public void delete(EmployeeProjectBillable projectBillable) {
        entityManager.remove(projectBillable);
    }
}
