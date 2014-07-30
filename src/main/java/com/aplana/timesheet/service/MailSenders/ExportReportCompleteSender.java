package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.ReportExportStatus;

import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.List;

/**
 * Created by abayanov
 * Date: 29.07.14
 */
public class ExportReportCompleteSender extends MailSender<ReportExportStatus> {

    public ExportReportCompleteSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
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
    protected List<Mail> getMailList(ReportExportStatus reportExportStatus) {
        Mail mail = new TimeSheetMail();

        mail.setToEmails(Arrays.asList(reportExportStatus.getEmployee().getEmail()));

        mail.setSubject(propertyProvider.getTimesheetMailMarker() + " " + reportExportStatus.getReportName());
        String body = String.format("Отчет \"%s\" готов. Ссылка для скачивания: %s",
                reportExportStatus.getReportName(),
                propertyProvider.getTimeSheetURL() + "/managertools/report/download/" + reportExportStatus.getId()
        );

        mail.setPreconstructedMessageBody(body);

        return Arrays.asList(mail);
    }
}
