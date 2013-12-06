package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Permission;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author pmakarov
 * @see <a href="">Аналитика</a>
 *      creation date: 04.12.13
 */
@Repository
public class PermissionDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public Permission find(Integer id){
        Query query = entityManager.createQuery("select p from Permission p where p.id = :id");
        query.setParameter("id", id);
        return (Permission)query.getSingleResult();
    }
}
