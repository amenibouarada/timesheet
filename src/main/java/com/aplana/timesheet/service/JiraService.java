package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.JiraDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.google.common.base.Joiner;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class JiraService {
    private static final Logger logger = LoggerFactory.getLogger(JiraService.class);

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Autowired
    private EmployeeDAO emloyeeDAO;

    @Autowired
    private JiraDAO jiraDAO;

    @Autowired
    private ProjectDAO projectDAO;

    static final String IN_PROGRESS_STATUS = "In Progress";

    /* создание подключения к серверу JIRA */
    private JiraRestClient getRestClient() {
        /* получаем логины пароли для сервера JIRA */
        String jiraServerUrl = propertyProvider.getJiraServerUrl();
        if ( jiraServerUrl == null || jiraServerUrl.equals("") ) {
            logger.error("Can't read property jira.server.url");
            return null;
        }
        String jiraInsiderUserName = propertyProvider.getJiraUsername();
        if ( jiraInsiderUserName == null || jiraInsiderUserName.equals("") ) {
            logger.error("Can't read property jira.username");
            return null;
        }
        String jiraInsiderUserPassword = propertyProvider.getJiraPassword();
        if ( jiraInsiderUserPassword == null || jiraInsiderUserPassword.equals("") ) {
            logger.error("Can't read property jira.password");
            return null;
        }

        /* делаем подключение */
        JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        URI jiraServerUri = null;
        try {
            jiraServerUri = new URI(jiraServerUrl);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI", e);
        }
        return factory.createWithBasicHttpAuthentication(jiraServerUri, jiraInsiderUserName, jiraInsiderUserPassword);
    }

    /* формируем запрос по почте пользователя, проекту, дате
    *  пример строки запроса
        project = APLANATS and
        (status changed by Nlebedev on 2013-07-31
         or assignee changed from Nlebedev on 2013-07-31
         or assignee changed to Nlebedev on 2013-07-31
         or (reporter = Nlebedev and created = 2013-07-31)
         or (status = "In Progress" AND assignee = Nlebedev and created <= 2013-07-31)
        )
    */
    private String genJqlQueryForProject(String user, String project, String reportDate) {
        StringBuilder stringBuilder = new StringBuilder();
        String onDate = " on " + reportDate;
        stringBuilder
            .append("project = ").append(project).append(" and ")
            .append("(status changed by ").append(user).append(onDate)
            .append(" or assignee changed from ").append(user).append(onDate)
            .append(" or assignee changed to ").append(user).append(onDate)
            .append(" or (reporter = ").append(user).append(" and created = ").append(reportDate).append(")")
            .append(" or ").append(genJqlQueryInProgress(user, reportDate))
            .append(")");

        return stringBuilder.toString();
    }

    /* формируем запрос по почте пользователя, дате планах
    *
    *  пример строки запроса
        (status = "In Progress" AND assignee = Nlebedev and created <= 2013-07-31)
    *
    */
    private String genJqlQueryInProgress(String user, String reportDate) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(" (status =  \"" + IN_PROGRESS_STATUS + "\" AND assignee = ").append(user)
                .append(" and created <= ").append(reportDate)
                .append(")");
        return stringBuilder.toString();

    }

    /* возвращаем строку с key и summary по каждой задаче */
    public String getDayIssues(Integer employeeId, String reportDate, Integer projectId) {
        StringBuilder stringBuilder = new StringBuilder();
        Employee user = emloyeeDAO.find(employeeId);
        if (employeeId != null && user != null) {
            String userJira = user.getJiraName();
            Project project = projectDAO.find(projectId);
            String userProject = project.getJiraProjectKey();
            if (userProject != null && !userProject.equals("")) {
                    /* формируем запрос на JQL */
                String query = genJqlQueryForProject(userJira, userProject, reportDate);
                    /* создаём подключение к сервру JIRA */
                JiraRestClient jiraRestClient = getRestClient();
                    /* получаем список задач */
                List<Issue> issueList = jiraDAO.getIssues(jiraRestClient, query);
                    /* формируем строку с краткими данными */
                for (Issue item : issueList) {
                    stringBuilder.append("\r\n").append(item.getKey()).append(" - ").append(item.getSummary());
                }
                    /* подрезаем первый перенос строки */
                if (stringBuilder.length() > 0) {
                    stringBuilder.delete(0, 2);
                }
            }
        }
        return stringBuilder.toString();
    }

    public String getPlannedIssues(Integer employeeId, String reportDate) {
        Map<String, List<String>> projects = new HashMap<String, List<String>>();
        Employee user = emloyeeDAO.find(employeeId);
        StringBuilder stringBuilder = new StringBuilder();
        if (employeeId != null && user != null) {
            JiraRestClient jiraRestClient = getRestClient();

            List<Issue> issueList = jiraDAO.getIssues(jiraRestClient, genJqlQueryInProgress(user.getJiraName(), reportDate));

            for (Issue item : issueList) {
                Project project = projectDAO.findByJiraKey(item.getProject().getKey());
                String key = project != null ? project.getName() : "Проект неопределен";
                String format = String.format("%s - %s", item.getKey(), item.getSummary());
                if (projects.get(key) != null) {
                    projects.get(key).add(format);
                } else {
                    projects.put(key, new LinkedList<String>());
                    projects.get(key).add(format);
                }
            }

            for (Map.Entry<String, List<String>> entry : projects.entrySet()) {
                stringBuilder.append("\r\n").append(entry.getKey()).append(":");
                stringBuilder.append(Joiner.on("\r\n").join(projects.get(entry.getKey())));
            }
                    /* подрезаем первый перенос строки */
            if (stringBuilder.length() > 0) {
                stringBuilder.delete(0, 2);
            }
        }
        return stringBuilder.toString();
    }
}
