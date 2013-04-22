package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.ProjectRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ProjectRoleDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProjectRoleDAO.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Возвращает объект класса ProjectRole по указанному идентификатору или null.
     */
    public ProjectRole find(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager.find(ProjectRole.class, id);
    }

    /**
     * Возвращает объект класса ProjectRole по указанному идентификатору,
     * соответсвующий активной проектой роли, либо null.
     */
    public ProjectRole findActive(Integer id) {
        if (id == null) {
            return null;
        }

        Query query = entityManager.createQuery(
                "from ProjectRole as pr where pr.id=:id and pr.active=:active"
        ).setParameter("active", true).setParameter("id", id);
        try {
            return (ProjectRole) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Возвращает активные проектные роли.
     */
    @SuppressWarnings("unchecked")
    public List<ProjectRole> getProjectRoles() {
        Query query = entityManager.createQuery(
                "from ProjectRole as pr where pr.active=:active"
        ).setParameter("active", true);

        return query.getResultList();
    }

    /**
     * Возвращает активную проектную роль по названию
     */
    public ProjectRole find(String title) {
        if (title == null) {
            return null;
        }
        Query query = entityManager.createQuery(
                "from ProjectRole as pr where pr.active=:active and lower(pr.ldapTitle) like :title"
        ).setParameter("active", true).setParameter("title", "%" + title.toLowerCase() + "%");
        try {
            logger.debug("Title {}", title);
            return (ProjectRole) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ProjectRole findByName(String s) {
        Query query = entityManager.createQuery(
                "FROM ProjectRole AS pr WHERE pr.active=:active AND pr.name like :name"
        ).setParameter("active", true).setParameter("name", "%" + s + "%");

        return (ProjectRole) query.getSingleResult();
    }

    public ProjectRole findByCode(String s) {
        Query query = entityManager.createQuery(
                "FROM ProjectRole AS pr WHERE pr.active=:active AND pr.code=:code"
        ).setParameter("active", true).setParameter("code", s);

        return (ProjectRole) query.getResultList().get(0);
    }

    public ProjectRole getSysRole(Integer roleId) {
        Query query = entityManager.createQuery(
                "FROM ProjectRole AS pr WHERE pr.id=:id"
        ).setParameter("id", roleId);

        return (ProjectRole) query.getResultList().get(0);
    }
}