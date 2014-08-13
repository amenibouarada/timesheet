package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;

import java.util.Arrays;
import java.util.List;

/**
 * Created user abayanov
 * Date: 12.07.13
 * Time: 17:11
 */
public class LoginFailureErrorThresholdSender extends MailSender<String> {
    public LoginFailureErrorThresholdSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format(" Оповещения о попытки подбора пароля (%s)", this.getClass().getSimpleName());
    }

    @Override
    protected List<Mail> getMailList(String str) {
        Mail mail = new TimeSheetMail();

        mail.setSubject("Попытка подбора пароля для доступа к системе.");
        mail.setPreconstructedMessageBody("Зафиксированы попытки подбора пароля для доступа к системе, подробности в логах сервера.");
        mail.setToEmails(Arrays.asList(propertyProvider.getMailProblemsAndProposalsCoaddress(0)));
        return Arrays.asList(mail);
    }
}



