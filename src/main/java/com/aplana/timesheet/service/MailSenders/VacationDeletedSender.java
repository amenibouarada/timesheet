package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class VacationDeletedSender extends  AbstractVacationSenderWithCopyToAuthor {

    protected static final Logger logger = LoggerFactory.getLogger(VacationDeletedSender.class);



    public VacationDeletedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    public List<Mail> getMainMailList(Vacation vacation) {
        final Mail mail = new TimeSheetMail();

        mail.setToEmails(getToEmails(vacation));

        final Collection<String> ccEmails = new ArrayList<String>();
        Employee employee = vacation.getEmployee();
        //оповещаем центр
        if (employee!=null && employee.getDivision()!=null) {
            ccEmails.add(employee.getDivision().getVacationEmail());
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
                        DateFormatUtils.format(params.getBeginDate(), DATE_FORMAT),
                        DateFormatUtils.format(params.getEndDate(), DATE_FORMAT)
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
