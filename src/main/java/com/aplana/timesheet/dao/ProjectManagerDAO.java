package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;


@Repository
public class ProjectManagerDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProjectManagerDAO.class);

	@PersistenceContext
	private EntityManager entityManager;
	
	public ProjectManager find(Integer id) {
		return entityManager.find(ProjectManager.class, id);
	}

    public Boolean isProjectManager(Employee employee, Project project) {
        final Query query =  entityManager.createQuery("from ProjectManager p " +
                "where p.project = :project and p.employee = :employee")
                .setParameter("project", project).setParameter("employee", employee);
        return query.getResultList().size() != 0;
    }

    public void save(ProjectManager manager)  {
        entityManager.merge(manager);
        entityManager.flush();
        logger.debug("Flushed manager object id = {}", manager.getId());
    }

    public List<ProjectManager> findByEmployee(Employee employee) {
        final Query query = entityManager.createQuery("from ProjectManager p " +
                "where p.employee = :employee").setParameter("employee", employee);
        return query.getResultList();
    }

    /**
     * Возвращает список всех записей проектных ролей по проекту.
     * @param project Проект
     * @return Список записей проектных ролей
     */
    public List<ProjectManager> findByProject(Project project) {
        Query query = entityManager.createQuery(
                "select p from ProjectManager p where p.project = :project order by p.id")
                .setParameter("project", project);

        return query.getResultList();
    }

    public Boolean isProjectParticipant(Project project, Employee employee){
        final Query query = entityManager.createQuery("FROM ProjectParticipant p " +
                "WHERE p.employee = :employee AND p.project = :project").
                setParameter("employee", employee).
                setParameter("project", project);
        return query.getResultList().size() > 0;
    }
}