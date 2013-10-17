package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;

import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.List;

/**
 * @author iziyangirov
 */

public class ExceptionSender extends MailSender<String> {

    public ExceptionSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) {
        try {
            Multipart multiPart = new MimeMultipart();
            MimeBodyPart messageText = new MimeBodyPart();
            messageText.setText(mail.getPreconstructedMessageBody(), "UTF-8", "html");
            multiPart.addBodyPart(messageText);

            message.setContent(multiPart);
        } catch (Exception e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(String problem) {
        Mail mail = new TimeSheetMail();

        mail.setSubject(propertyProvider.getTimesheetMailMarker() + " У одного из пользователей во время работы произошла ошибка. Подробности в письме.");
        mail.setPreconstructedMessageBody(problem);
        mail.setToEmails(Arrays.asList(propertyProvider.getMailProblemsAndProposalsCoaddress(0)));
        return Arrays.asList(mail);
    }

}
