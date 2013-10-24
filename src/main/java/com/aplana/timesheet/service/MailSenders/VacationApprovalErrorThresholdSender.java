package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;

import java.util.Arrays;
import java.util.List;

/**
 * @author iziyangirov
 */

public class VacationApprovalErrorThresholdSender extends MailSender<String> {

    public VacationApprovalErrorThresholdSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected List<Mail> getMailList(String str) {
        Mail mail = new TimeSheetMail();

        mail.setSubject("Попытка подбора guid для сервиса согласования отпусков.");
        mail.setPreconstructedMessageBody("Обнаружена попытка подбора guid для сервиса согласования отпусков, подробности в логах сервера.");
        mail.setToEmails(Arrays.asList(propertyProvider.getMailProblemsAndProposalsCoaddress(0)));
        return Arrays.asList(mail);
    }

}
