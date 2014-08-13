package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Arrays;
import java.util.List;

public class FeedbackSender extends MailSender<FeedbackForm> {

    public FeedbackSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format(" Оповещения о фидбэке (%s)", this.getClass().getSimpleName());
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) {
        try {
            Multipart multiPart = new MimeMultipart();

            MimeBodyPart messageText = new MimeBodyPart();
            messageText.setText(mail.getPreconstructedMessageBody(), "UTF-8", "html");
            multiPart.addBodyPart(messageText);

            for ( MultipartFile path : mail.getFilePahts()) {
                if ( ( path != null ) && ( ! path.isEmpty() ) ) {
                    MimeBodyPart attach = new MimeBodyPart();
                    attach.setDataHandler( new DataHandler(new ByteArrayDataSource(path.getBytes(), path.getContentType())) );
                    attach.setFileName( path.getOriginalFilename() );
                    multiPart.addBodyPart( attach );
                }
            }
            message.setContent(multiPart);
        } catch (Exception e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(FeedbackForm params) {
        Mail mail = new TimeSheetMail();

        String employeeName = sendMailService.getEmployeeFIO(params.getEmployeeId());
        String employeeEmail = sendMailService.getEmployeeEmail(params.getEmployeeId());
        String employeeDivision = sendMailService.getEmployeeDivision(params.getEmployeeId());

        mail.setToEmails(Arrays.asList(propertyProvider.getMailProblemsAndProposalsCoaddress(params.getFeedbackType())));
        mail.setCcEmails(Arrays.asList(employeeEmail));
        mail.setSubject(propertyProvider.getFeedbackMarker() + " Сообщение от пользователя системы списания занятости");
        mail.setFilePahts(Arrays.asList(params.getFile1Path(), params.getFile2Path()));
        mail.setPreconstructedMessageBody(
                getMessageBody(employeeName, employeeEmail, params.getFeedbackDescription(), params.getFeedbackTypeName(), employeeDivision) );

        return Arrays.asList(mail);
    }

    private String getMessageBody(String name, String email, String description, String feedbackTypeName, String employeeDivision) {
        final StringBuilder bodyTxt = new StringBuilder();

        bodyTxt.append(description);

        if (StringUtils.isNotBlank(name)) {
            bodyTxt.append("\n\nПришло от: ").append(name);
        }

        if (StringUtils.isNotBlank(employeeDivision)) {
            bodyTxt.append(" (").append(employeeDivision).append(")");
        }

        if (StringUtils.isNotBlank(email)) {
            bodyTxt.append("\nС адреса: ").append(email);
        }

        if (StringUtils.isNotBlank(feedbackTypeName)) {
            bodyTxt.append("\nТип сообщения: ").append(feedbackTypeName);
        }

        return bodyTxt.toString().replace("\n", "<br>");
    }

}
