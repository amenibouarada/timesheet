package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class VacationDeletedSender extends  AbstractVacationSenderWithCopyToAuthor {

    protected static final Logger logger = LoggerFactory.getLogger(VacationDeletedSender.class);
    ProjectService projectService;
    EmployeeService employeeService;


    public VacationDeletedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, ProjectService projectService, EmployeeService employeeService) {
        super(sendMailService, propertyProvider);
        this.projectService = projectService;
        this.employeeService = employeeService;
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format(" Оповещение об удалении отпуска (%s)", this.getClass().getSimpleName());
    }

    @Override
    public List<Mail> getMainMailList(Vacation vacation) {
        final Mail mail = new TimeSheetMail();

        Collection<String> mailsTo = new ArrayList<String>();

        Iterable<String> mailIterator = getToEmails(vacation);
        for(String email : mailIterator){
            mailsTo.add(email);
        }

        mail.setToEmails(mailsTo);

        final Collection<String> ccEmails = new ArrayList<String>();
        Employee employee = vacation.getEmployee();

        // оповещаем отдел кадров подразделения
        if (employee.getDivision() != null && VacationStatusEnum.APPROVED.getId() == vacation.getStatus().getId()) {
            ccEmails.addAll(getAdditionalEmailsForRegion(employee.getRegion()));
        }

        ccEmails.addAll(getAssistantEmail(Sets.newHashSet(mail.getToEmails())));

        //оповещаем центр
        if (employee != null && employee.getDivision() != null) {
            ccEmails.add(employee.getDivision().getVacationEmail());
        }

        // оповещаем РЦК
        if (employee.getDivision() != null && employee.getDivision().getLeaderId() != null) {
            ccEmails.add(employee.getDivision().getLeaderId().getEmail());
        }

        // оповещаем второго линейного руководителя
        if (employee.getManager2() != null) {
            ccEmails.add(employee.getManager2().getEmail());
        }


        mail.setCcEmails(getNotBlankEmails(ccEmails));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacation));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Vacation params) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(params));

        return table;
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

    private String getSubject(Vacation params) {
        return  String.format(" Удален отпуск %s", params.getEmployee().getName());
    }

    private Iterable<String> getToEmails(Vacation params) {
        final List<String> vacationApprovalEmailList = sendMailService.getVacationApprovalEmailList(params.getId());

        vacationApprovalEmailList.add(params.getEmployee().getEmail());

        return vacationApprovalEmailList;
    }

}
