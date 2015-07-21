package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class DivisionDAO {
	private static final Logger logger = LoggerFactory.getLogger(DivisionDAO.class);
	
	@PersistenceContext
	private EntityManager entityManager;

    /**
     * Возвращает список активных подразделений
     */
	@SuppressWarnings("unchecked")
	public List<Division> getActiveDivisions() {
        Query query = entityManager.createQuery(
                "from Division as d where d.active=:active order by d.name asc"
        ).setParameter("active", true);

        return query.getResultList();
    }

	@SuppressWarnings("unchecked")
	public List<Division> getAllDivisions() {
        return  entityManager.createQuery(
                "from Division as d order by d.name asc"
        ).getResultList();
	}

    public List<Division> getDivisionCheck() {
        return  entityManager.createQuery(
                "from Division as d where d.isCheck = true"
        ).getResultList();
    }

	public Division find(Integer id) {
        Division division = entityManager.find(Division.class, id);
        if (division != null) Hibernate.initialize(division.getLeaderId());
        return division;
	}
	
	/**
	 * Ищет активное подразделение с указанным именем.
	 * @param title название подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	public Division find(String title) {
        Query query = entityManager.createQuery(
                "from Division as d where d.active=:active and lower(d.ldapName)=:title"
        ).setParameter( "active", true ).setParameter( "title", title.toLowerCase() );

        try {
			return  (Division) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Department with title '{}' not found.", title);
            return null;
		}
	}

    public Division findByDepartmentName(String departmentName) {
        logger.debug("findByDepartmentName: departmentName = {}", departmentName);
        // Проверяется, что слово обрамлено запятыми или конец/начало строки
        Integer id = (Integer) entityManager.createNativeQuery(
                "SELECT id FROM division as d WHERE lower(department_name) SIMILAR TO  :departmentName"
        ).setParameter("departmentName", ("(%_,|)" + departmentName + "(,_%|)").toLowerCase()).getSingleResult();
        return (Division) find(id);
    }

    public Division findByLeader(Integer employeeId) {
        Query query = entityManager.createQuery(
                "from Division as d where d.leaderId.id=:employeeId"
        ).setParameter("employeeId", employeeId);

        try {
            return  (Division) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Division findByLeader(Employee employee) {
        return findByLeader(employee.getId());
    }

    public void save(Division division) {
        if (division.getId() != null) {
            entityManager.merge(division);
        } else {
            division.setId((Integer) entityManager.createQuery("SELECT MAX(id) FROM Division").getSingleResult() + 1);
            entityManager.persist(division);
        }
        entityManager.flush();
    }

    public String setDivisions(List<Division> divisionsToSync) {
        final StringBuilder builder = new StringBuilder();

        for (Division division : divisionsToSync) {
            builder.append(String.format("Set leader %s to division %s\n", division.getLeader(), division.getName()));

            save(division);
        }

        return builder.toString();
    }
}