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
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    /* формируем запрос по почте пользователя, проекту (необязат), дате (необязат) */
    private String genJqlQuery(String user, String project, Date date) {
        StringBuilder stringBuilder = new StringBuilder();

        if ( user != null && !user.equals("") ) {
            String onDate = "";
            String createdDate = "";
            if ( date != null ) {
                onDate = " on " + new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(date) + " ";
                createdDate = " or (reporter = " + user +
                        " and created > " + DateTimeUtil.dateToString(date) +
                        " and created < " + DateTimeUtil.dateToString(DateUtils.addDays(date, 1)) + ")";
            }

            if ( project != null && !project.equals("") )
                stringBuilder.append("project in (").append(project).append(") and ");

            stringBuilder
                    .append("(status changed by ").append(user).append(onDate)
                    .append(" or assignee changed from ").append(user).append(onDate)
                    .append(" or assignee changed to ").append(user).append(onDate)
                    .append(" or reporter changed from ").append(user).append(onDate)
                    .append(" or reporter changed to ").append(user).append(onDate)
                    .append((createdDate.isEmpty()) ? "" : createdDate)
                    .append(" or (status = \"3\" AND assignee = ").append(user)
                    .append("))");
/*  пример строки запроса*/
//  project in (APLANATS) and
// (status changed by Nlebedev on 2013-07-31
//  or assignee changed from Nlebedev on 2013-07-31
//  or assignee changed to Nlebedev on 2013-07-31
//  or reporter changed from Nlebedev on 2013-07-31
//  or reporter changed to Nlebedev on 2013-07-31
//  or (reporter = Nlebedev and created > 2013-07-31 and created < 2013-08-01)
//  or (status = "3" AND assignee = Nlebedev)
//  )

        }

        return stringBuilder.toString();
    }


    /* возвращаем строку с key и summary по каждой задаче */
    public String getDayIssues(Integer employeeId, String reportDate, Integer projectId) {
        StringBuilder stringBuilder = new StringBuilder();
        /* берём по умолчанию текущую дату */
        Date date = new Date();
        try {
            date = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).parse(reportDate);
        } catch (ParseException e) {
            logger.error("Сообщение об ошибке", e);
        }
        if (employeeId != null) {
            Employee user = emloyeeDAO.find(employeeId);
            if (user != null) {
                String userJira = user.getJiraName();
                Project project = projectDAO.find(projectId);
                String userProject = project.getJiraProjectKey();
                if (userProject != null && !userProject.equals("")) {
                    /* формируем запрос на JQL */
                    String query = genJqlQuery(userJira, userProject, date);
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
        }
        return stringBuilder.toString();
    }
}
