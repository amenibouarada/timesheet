package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: iziyangirov
 * Date: 19.08.14
 */
public abstract class AbstractPlannedVacationSender extends AbstractVacationSenderWithCopyToAuthor {

    public AbstractPlannedVacationSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    ProjectService projectService;
    EmployeeService employeeService;

    abstract String getSubject(Vacation vacation);
    abstract String getBody(Vacation vacation);

    Table<Integer, String, String> getParamsForGenerateBody(Vacation params) {
        Table<Integer, String, String> table = HashBasedTable.create();
        table.put(FIRST, MAIL_BODY, getBody(params));
        return table;
    }

    @Override
    public List<Mail> getMainMailList(Vacation vacation) {
        final Mail mail = new TimeSheetMail();
        final Set<String> emails = new HashSet<String>();
        final Set<String> ccEmails = new HashSet<String>();

        Employee employee = vacation.getEmployee();
        /* добавляем руководителей проектов */
        final List<Project> projects = projectService.getProjectsForVacation(vacation);
        for (Project project : projects) {
            emails.add(project.getManager().getEmail());
        }
        /* делаем список линейных рук для оповещения */
        final List<Employee> linearEmployees = employeeService.getLinearEmployees(employee);
        for (Employee item : linearEmployees) {
            emails.add(item.getEmail());
        }
        /* добавляем доп руководителя */
        Employee manager2 = vacation.getEmployee().getManager2();
        if (manager2 != null) {
            emails.add(manager2.getEmail());
        }
        /* оповещаем отпускника и удалителя */
        if (!emails.contains(vacation.getEmployee().getEmail())) {
            ccEmails.add(vacation.getEmployee().getEmail());
        }
        if (!emails.contains(vacation.getAuthor().getEmail())) {
            ccEmails.add(vacation.getAuthor().getEmail());
        }

        //оповещаем центр
        if (employee.getDivision()!=null) {
            ccEmails.add(employee.getDivision().getVacationEmail());
        }

        mail.setToEmails(getNotBlankEmails(emails));
        mail.setCcEmails(getNotBlankEmails(ccEmails));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacation));

        return Arrays.asList(mail);
    }

}
