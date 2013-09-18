package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Project;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class ProjectServiceTest extends AbstractJsonTest {

    @Autowired
    private DivisionDAO divisionDAO;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectDAO projectDAO;

    private String getProjectListWithOwnerDivisionJson() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        List<Project> projectList = projectDAO.getAllProjects();
        for (Project project : projectList) {
            result.append("{\"id\":\"");
            result.append(project.getId());
            result.append("\",\"value\":\"");
            result.append(project.getName());
            result.append("\",\"state\":\"");
            result.append(project.getState().getId());
            result.append("\",\"active\":\"");
            result.append(project.isActive());
            result.append("\",\"ownerDivisionId\":\"");
            result.append(project.getDivision()!=null?project.getDivision().getId():0);
            result.append("\"}");
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("]");
        return result.toString();
    }

    private String getProjectListJson(List<Division> divisions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < divisions.size(); i++) {
            sb.append("{\"divId\":\"");
            sb.append(divisions.get(i).getId());
            Set<Project> projects = divisions.get(i).getProjects();
            sb.append("\",\"divProjs\":[");
            if (projects.size() > 0) {
                int count = 0;
                for (Project project : projects) {
                    sb.append("{\"id\":\"");
                    sb.append(project.getId());
                    sb.append("\",\"value\":\"");
                    sb.append(project.getName());
                    sb.append("\",\"state\":\"");
                    sb.append(project.getState().getId());
                    sb.append("\",\"active\":\"");
                    sb.append(project.isActive());
                    sb.append("\"}");
                    sb.append(",");
                    count++;
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("]}");
            } else {
                sb.append("{\"id\":\"0\",\"value\":\"\"}]}");
            }

            if (i < (divisions.size() - 1)) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String getProjectListJson() {
        return getProjectListAsJson(projectService.getAllProjects());
    }

    private String getProjectListAsJson(List<Project> projects){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (projects.size() > 0) {
            int count = 0;
            for (Project project : projects) {
                sb.append("{\"id\":\"");
                sb.append(project.getId());
                sb.append("\",\"value\":\"");
                sb.append(project.getName());
                sb.append("\",\"state\":\"");
                sb.append(project.getState().getId());
                sb.append("\",\"active\":\"");
                sb.append(project.isActive());
                sb.append("\"}");
                sb.append(",");
                count++;
            }
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append("{\"id\":\"0\",\"value\":\"\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    @Test
    public void testGetProjectListWithOwnerDivisionJson() throws Exception {
        assertJsonEquals(getProjectListWithOwnerDivisionJson(), projectService.getProjectListWithOwnerDivisionJson());
    }

    @Test
    public void testGetProjectListJsonWithDivisions() throws Exception {
        final List<Division> divisions = divisionDAO.getActiveDivisions();

        assertJsonEquals(getProjectListJson(divisions), projectService.getProjectListJson(divisions));
    }

    @Test
    public void testGetProjectListJson() throws Exception {
        assertJsonEquals(getProjectListJson(), projectService.getProjectListJson());
    }

    @Test
    public void testGetProjectListAsJson() throws Exception {
        final List<Project> projects = projectDAO.getAll();

        assertJsonEquals(getProjectListAsJson(projects), projectService.getProjectListAsJson(projects));
    }

}
