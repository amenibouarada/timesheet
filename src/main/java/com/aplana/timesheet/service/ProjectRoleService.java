package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.dao.ProjectRoleDAO;
import com.aplana.timesheet.dao.entity.ProjectRole;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;

@Service
public class ProjectRoleService {	

	@Autowired
	private ProjectRoleDAO projectRoleDAO;

	/** Возвращает объект класса ProjectRole по указанному идентификатору */
    @Transactional(readOnly = true)
    public ProjectRole find(Integer id) {
		return projectRoleDAO.find(id);
	}
	
	/**
	 * Возвращает объект класса ProjectRole по указанному идентификатору,
	 * соответсвующий активной проектой роли, либо null.
	 */
    @Transactional(readOnly = true)
    public ProjectRole findActive(Integer id) {
		return projectRoleDAO.findActive(id);
	}
	
	/** Возвращает активную проектную роль по названию */
    @Transactional(readOnly = true)
    public ProjectRole find(String title) {
		return projectRoleDAO.find(title);
	}

	/** Возвращает активные проектные роли. */
    @Transactional(readOnly = true)
    public List<ProjectRole> getProjectRoles() {
		return projectRoleDAO.getProjectRoles();
	}

    public String getProjectRoleListJson(Iterable<ProjectRole> projectRoleList) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (ProjectRole item : projectRoleList) {
            builder.withElement(
                    anObjectBuilder().
                            withField("id", JsonUtil.aStringBuilderNumber(item.getId())).
                            withField("value", aStringBuilder(item.getName()))
            );
        }

        return JsonUtil.format(builder);
    }

    public ProjectRole findJobForCreateEmployee(String ldapRoleTitle) {
        ProjectRole job = find(ldapRoleTitle);
        if (job != null) {
            return job;
        } else {
            return projectRoleDAO.find(ProjectRolesEnum.ANALYST.getId());
        }
    }

}