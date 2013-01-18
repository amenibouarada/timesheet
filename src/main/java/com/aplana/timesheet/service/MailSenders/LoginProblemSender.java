package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * User: eyaroslavtsev
 * Date: 03.08.12
 * Time: 14:13
 */
public class LoginProblemSender extends MailSender {

    private AdminMessageForm adminMessageForm;

    public LoginProblemSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected void initMessageBody() {
        Multipart multiPart = new MimeMultipart();
        MimeBodyPart messageText = new MimeBodyPart();
        StringBuilder bodyTxt = new StringBuilder();

        bodyTxt.append("Логин: ").append(adminMessageForm.getName()).append("\n");
        bodyTxt.append("Указаный адрес: ").append(adminMessageForm.getEmail()).append("\n");
        bodyTxt.append("Ошибка: ").append(adminMessageForm.getError()).append("\n");
        bodyTxt.append("Время: ").append(adminMessageForm.getDate()).append("\n");
        bodyTxt.append("Описание пользователя: ").append(adminMessageForm.getDescription()).append("\n");
        bodyTxt.append(StringEscapeUtils.escapeHtml4(adminMessageForm.getDescription()));
        logger.info(adminMessageForm.toString());
        try {
            messageText.setText(bodyTxt.toString(), "UTF-8", "html");
            multiPart.addBodyPart(messageText);
            message.setContent(multiPart);
        } catch (Exception e) {
            logger.error("Error while init message body.", e);
        }
    }

    public void SendLoginProblem(AdminMessageForm form) {
        adminMessageForm = form;

        try {
            initSender();

            logger.info("Login problem mailing.");

            message = new MimeMessage(session);
            initMessageHead();
            initMessageBody();

            sendMessage();

        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.", propertyProvider.getMailTransportProtocol(), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        } finally {
            deInitSender();
        }
    }
}
