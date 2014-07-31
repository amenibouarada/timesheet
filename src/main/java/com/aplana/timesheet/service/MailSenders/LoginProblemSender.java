package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;

import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.List;

/**
 * User: eyaroslavtsev
 * Date: 03.08.12
 * Time: 14:13
 */
public class LoginProblemSender extends MailSender<AdminMessageForm> {

    public LoginProblemSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) {
        try {
            MimeBodyPart messageText = new MimeBodyPart();
            Multipart multiPart = new MimeMultipart();

            messageText.setText(mail.getPreconstructedMessageBody(), "UTF-8", "html");
            multiPart.addBodyPart(messageText);

            message.setContent(multiPart);
        } catch (Exception e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(AdminMessageForm params) {
        logger.info("Login problem mailing.");
        Mail mail = new TimeSheetMail();

        StringBuilder bodyTxt = new StringBuilder();

        mail.setToEmails(Arrays.asList(propertyProvider.getMailProblemsAndProposalsCoaddress(5)));
        mail.setSubject(propertyProvider.getAccessMarker() + " Ошибка авторизации");

        bodyTxt.append("Логин: ").append(params.getName()).append("\n");
        bodyTxt.append("Указаный адрес: ").append(params.getEmail()).append("\n");
        bodyTxt.append("Ошибка: ").append(params.getError()).append("\n");
        bodyTxt.append("Время: ").append(params.getDate()).append("\n");
        bodyTxt.append("Описание пользователя: ").append(params.getDescription()).append("\n");
        bodyTxt.append(params.getDescription());

        logger.info(params.toString());

        mail.setPreconstructedMessageBody(bodyTxt.toString());

        return Arrays.asList(mail);
    }
}
