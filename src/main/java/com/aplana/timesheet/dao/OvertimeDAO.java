package com.aplana.timesheet.dao;


import com.aplana.timesheet.dao.entity.Overtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class OvertimeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(OvertimeDAO.class);

    @Transactional
    public void save(Overtime overtime)  {
        entityManager.merge(overtime);
        entityManager.flush();
        logger.debug("Flushed overtime object id = {}", overtime.getId());
    }

    @Transactional
    // ToDo переделать на один запрос
    public void delete(List<Integer> ids)  {
        for (Integer id : ids){
            entityManager.remove(entityManager.find(Overtime.class, id));
        }
        entityManager.flush();
    }

    public List<Overtime> getOvertimes(int year, int month, Integer divisionOwner, Integer divisionEmployee){
        String queryString = "FROM Overtime AS ot " +
                       "WHERE ot.year = :year AND " +
                       "ot.month = :month ";
        boolean ownerDivSet = false;
        boolean employeeDivSet = false;
        if (divisionOwner != null && divisionOwner > 0){
            queryString += " AND ot.project.division.id = :divisionOwner ";
            ownerDivSet = true;
        }
        if (divisionEmployee != null && divisionEmployee > 0){
            queryString += " AND ot.employee.division.id = :divisionEmployee ";
            employeeDivSet = true;
        }
        queryString +=  " ORDER BY ot.employee.name";
        Query query = entityManager.createQuery(queryString).setParameter("year", year).setParameter("month", month);
        if (ownerDivSet) { query.setParameter("divisionOwner", divisionOwner); }
        if (employeeDivSet) { query.setParameter("divisionEmployee", divisionEmployee); }
        return query.getResultList();
    }
}
