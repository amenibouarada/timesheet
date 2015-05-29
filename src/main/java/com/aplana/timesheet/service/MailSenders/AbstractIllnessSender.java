package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractIllnessSender extends MailSender<Illness> {

    private ProjectService projectService;
    private EmployeeService employeeService;

    public AbstractIllnessSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, ProjectService projectService, EmployeeService employeeService) {
        super(sendMailService, propertyProvider);
        this.projectService = projectService;
        this.employeeService = employeeService;
    }

    @Override
    public List<Mail> getMailList(Illness illness) {
        final Mail mail = new TimeSheetMail();

        final List<Project> projects = projectService.getProjectsForIllness(illness);

        List<String> emails = new ArrayList<String>();

        emails.add(illness.getEmployee().getEmail());

        Map<Employee, List<Project>> juniorProjectManagersAndProjects =
                employeeService.getJuniorProjectManagersAndProjects(projects, illness);
        for (Map.Entry entry: juniorProjectManagersAndProjects.entrySet()) {
            emails.add(((Employee) entry.getKey()).getEmail());
        }

        for (Project project : projects) {
            Employee manager = project.getManager();
            String email = manager.getEmail();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }

        List<Employee> employeeList = employeeService.getLinearEmployees(illness.getEmployee());
        for (Employee item :employeeList) {
            String email = item.getEmail();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }

        Employee manager2 = illness.getEmployee().getManager2();
        if (manager2 != null) {
            String email = manager2.getEmail();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }
        mail.setToEmails(emails);
        mail.setSubject(getSubject(illness));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(illness));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Illness illness) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(illness));

        return table;
    }

    protected String getBody(Illness illness) {
        return "%s";
    }

    protected String getSubject(Illness illness) {
        return "%s";
    }

    @Override
    protected final void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        try {
            if (mail.getParamsForGenerateBody() != null) {
                message.setText(mail.getParamsForGenerateBody().get(FIRST, MAIL_BODY), "UTF-8", "html");
            }else{
                super.initMessageBody(mail, message);
            }
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }
}
