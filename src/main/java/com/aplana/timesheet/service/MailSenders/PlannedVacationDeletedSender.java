package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PlannedVacationDeletedSender extends  AbstractVacationSenderWithCopyToAuthor {

    protected static final Logger logger = LoggerFactory.getLogger(PlannedVacationDeletedSender.class);

    private EmployeeService employeeService;
    private ProjectService projectService;

    public PlannedVacationDeletedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, EmployeeService employeeService, ProjectService projectService) {
        super(sendMailService, propertyProvider);
        this.employeeService = employeeService;
        this.projectService = projectService;
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

    private String getSubject(Vacation params) {
        return  String.format(" Удален планируемый отпуск у сотрудника %s", params.getEmployee().getName());
    }

    private String getBody(Vacation params) {
        final StringBuilder stringBuilder = new StringBuilder(
                String.format("Сотрудник \"%s\" удалил ", sendMailService.getSecurityPrincipal().getEmployee().getName())
        );

        final Employee employee = params.getEmployee();
        final Employee curUser = sendMailService.getSecurityPrincipal().getEmployee();

        if (params.getEmployee().equals(curUser)) {
            stringBuilder.append("своё заявление");
        } else {
            stringBuilder.append(
                    String.format("заявление сотрудника \"%s\"", employee.getName())
            );
        }

        stringBuilder.append(
                String.format(
                        " на %s за период с %s по %s",
                        WordUtils.uncapitalize(params.getType().getValue()),
                        DateTimeUtil.formatDateIntoViewFormat(params.getBeginDate()),
                        DateTimeUtil.formatDateIntoViewFormat(params.getEndDate())
                )
        );

        return stringBuilder.toString();
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Vacation params) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(params));

        return table;
    }
}
