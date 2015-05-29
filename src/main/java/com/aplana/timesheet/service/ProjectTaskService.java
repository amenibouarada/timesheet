package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.dao.ProjectTaskDAO;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectTask;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;

@Service
public class ProjectTaskService {
	@Autowired
	private ProjectTaskDAO projectTaskDAO;

	/**
	 * Возвращает активные проектные задачи по указанному проекту.
	 * @param projectId идентификатор проекта в базе данных
	 * @return список проектных задач
	 */
    @Transactional(readOnly = true)
    public List<ProjectTask> getProjectTasks(Integer projectId) {
		return projectTaskDAO.getProjectTasks(projectId);
	}

    /**
     * Возвращает все проектные задачи по указанному проекту.
     * @param project Проект
     * @return Список проектных задач
     */
    @Transactional(readOnly = true)
    public List<ProjectTask> findAllByProject(Project project) {
        return projectTaskDAO.findAllByProject(project);
    }
	
	/**
	 * Возвращает активную проектную задачу, относящуюся к указанному проекту,
	 * либо null, если проект или код задачи null, или такой задачи нет.
	 */
    @Transactional(readOnly = true)
    public ProjectTask find(Integer projectId, Integer projectTaskId) {
		return projectTaskDAO.find(projectId, projectTaskId);
	}

    @Transactional(readOnly = true)
    public String getProjectTaskListJson(List<Project> projects) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Project project : projects) {
            final Integer projectId = project.getId();
            final List<ProjectTask> tasks = getProjectTasks(projectId);
            final JsonArrayNodeBuilder tasksBuilder = anArrayBuilder();

            for (ProjectTask task : tasks) {
                tasksBuilder.withElement(
                        anObjectBuilder().
                                withField("id", aNumberBuilder(task.getId().toString())).
                                withField("value", aStringBuilder(task.getTaskName())).
                                withField("desc", aStringBuilder(task.getDescription()))
                );
            }

            builder.withElement(
                    anObjectBuilder().
                            withField("projId", JsonUtil.aStringBuilderNumber(projectId)).
                            withField("projTasks", tasksBuilder)
            );
        }

        return JsonUtil.format(builder);
    }

    public ProjectTask find(Integer projectTaskId) {
        return projectTaskId != null ? projectTaskDAO.find(projectTaskId) : null;
    }
}