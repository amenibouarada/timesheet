package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectManager;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;


@Repository
public class ProjectManagerDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProjectManagerDAO.class);

	@PersistenceContext
	private EntityManager entityManager;
	
	public ProjectManager find(Integer id) {
        if (id == null) {
            logger.warn("For unknown reasons, the ProjectManager ID is null.");
            return null;
        }
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

    public List<ProjectManager> getSortedListByProject(Project project) {
        Query query = entityManager.createQuery(
                "select case " +
                        "when (p.projectRole.id = :mang and p.master = true) then 1 " +
                        "when (p.projectRole.id = :analyst and p.master = true) then 2 " +
                        "when (p.projectRole.id = :dev and p.master = true) then 3 " +
                        "when (p.projectRole.id = :test and p.master = true) then 4 " +
                        "when (p.projectRole.id = :sys_eng and p.master = true) then 5 " +
                        "when (p.projectRole.id = :mang and p.master = false) then 6  " +
                        "when (p.projectRole.id = :analyst and p.master = false) then 7 " +
                        "when (p.projectRole.id = :dev and p.master = false) then 8 " +
                        "when (p.projectRole.id = :test and p.master = false) then 9 " +
                        "when (p.projectRole.id = :sys_eng and p.master = false) then 10 " +
                        "else 99 end as rolep, p " +
                        "from ProjectManager p where p.project = :project and p.active = true order by rolep asc")
                .setParameter("project", project)
                .setParameter("mang", ProjectRolesEnum.HEAD.getId())
                .setParameter("analyst", ProjectRolesEnum.ANALYST.getId())
                .setParameter("dev", ProjectRolesEnum.DEVELOPER.getId())
                .setParameter("test", ProjectRolesEnum.TESTER.getId())
                .setParameter("sys_eng", ProjectRolesEnum.SYSTEM_ENGINEER.getId());

        List resultList = query.getResultList();
        List<ProjectManager> projectManagers = new ArrayList<ProjectManager>();
        for (Object o : resultList) {
           projectManagers.add((ProjectManager)((Object[])o)[1]);
        }
        return projectManagers;
    }

    public List<ProjectManager> getListMasterManagersByRole(Integer roleId, Project project) {
        Query query = entityManager.createQuery(
                "select p from ProjectManager p where p.projectRole.id = :roleId and " +
                        "p.project = :project and " +
                        "p.active = true and p.master = true order by p.id")
                .setParameter("roleId", roleId)
                .setParameter("project", project);

        return query.getResultList();
    }
}