package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectManagerDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectManagerService.class);

	@Autowired
    ProjectManagerDAO projectManagerDAO;
	
	/**
	 * Возвращает объект класса ProjectManager по указанному идентификатору
	 */
    @Transactional(readOnly = true)
    public ProjectManager find(Integer id) {
		return projectManagerDAO.find(id);
	}

    /**
     * Возвращает объект класса ProjectManager, если найдено совпадение по паре employee/project
     */
    @Transactional(readOnly = true)
    public Boolean isProjectManager(Employee employee, Project project){
        return projectManagerDAO.isProjectManager(employee, project);
    }

    /**
     * Определяет есть ли активные Project Manager у сотрудника
     */
    @Transactional(readOnly = true)
    public Boolean hasActiveManagerEmployee(Employee employee) {
        List<ProjectManager> empProjectManagers = projectManagerDAO.findByEmployee(employee);
        for (ProjectManager manager : empProjectManagers) {
            if (manager.isActive())
                return true;
        }
        return false;
    }

    /**
     * Возвращает список всех записей проектных ролей по проекту.
     * @param project Проект
     * @return Список записей проектных ролей
     */
    @Transactional(readOnly = true)
    public List<ProjectManager> findByProject(Project project) {
        return projectManagerDAO.findByProject(project);
    }

    public void deactivateEmployeesRights(List<Employee> employees) {
        for (Employee employee : employees) {
                List<ProjectManager> empProjectManagers = projectManagerDAO.findByEmployee(employee);
                if (empProjectManagers != null) {
                    for (ProjectManager manager : empProjectManagers) {
                        manager.setActive(false);
                        projectManagerDAO.save(manager);
                    }
                }
        }
    }

    public List<ProjectManager> getSortedListByProject(Project project) {
        return projectManagerDAO.getSortedListByProject(project);
    }

    public List<ProjectManager> getListMasterManagersByRole(Integer roleId, Project project) {
        return projectManagerDAO.getListMasterManagersByRole(roleId, project);
    }
}