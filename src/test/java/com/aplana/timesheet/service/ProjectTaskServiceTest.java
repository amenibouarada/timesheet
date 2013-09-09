package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectTask;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class ProjectTaskServiceTest extends AbstractJsonTest {

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProjectService projectService;

    private String getProjectTaskListJson(List<Project> projects) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < projects.size(); i++) {
            Integer projectId = projects.get(i).getId();
            List<ProjectTask> tasks = projectTaskService.getProjectTasks(projectId);
            sb.append("{\"projId\":\"");
            sb.append(projectId);
            sb.append("\",\"projTasks\":[");
            for (int j = 0; j < tasks.size(); j++) {
                sb.append("{\"id\":");
                sb.append(tasks.get(j).getId());
                sb.append(",\"value\":\"");
                sb.append(tasks.get(j).getTaskName());
                sb.append("\"");
                sb.append(",\"desc\":\"");
                sb.append(tasks.get(j).getDescription());
                sb.append("\"}");
                if (j < (tasks.size() - 1)) {
                    sb.append(",");
                }
            }
            sb.append("]}");
            if (i < (projects.size() - 1)) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Test
    public void testGetProjectTaskListJson() throws Exception {
        final List<Project> projects = projectService.getAll();

        assertJsonEquals(getProjectTaskListJson(projects), projectTaskService.getProjectTaskListJson(projects));
    }
}
