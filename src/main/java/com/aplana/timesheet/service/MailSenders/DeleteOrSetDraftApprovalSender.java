package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import padeg.lib.Padeg;

import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.List;

/**
 * Created by abayanov
 * Date: 14.07.14
 */
public class DeleteOrSetDraftApprovalSender extends MailSender<TimeSheet> {
    public DeleteOrSetDraftApprovalSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
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
    protected List<Mail> getMailList(TimeSheet timeSheet) {
        Mail mail = new TimeSheetMail();

        String employeeName = timeSheet.getEmployee().getName();
        String employeeEmail = timeSheet.getEmployee().getEmail();
        String date = DateTimeUtil.formatDateIntoViewFormat(timeSheet.getCalDate().getCalDate());
        String fio = Padeg.getOfficePadeg(employeeName, PadegConstants.Roditelnyy);

        mail.setToEmails(Arrays.asList(propertyProvider.getMailAdminsAddress()));
        mail.setCcEmails(Arrays.asList(employeeEmail));

        String subject = String.format(" Запрос на %s отчета за %s от %s",
                timeSheet.getReportSendApprovalType().getName(),
                date,
                fio);

        mail.setSubject(propertyProvider.getDeleteOrSetDraftApprovalMarker() + subject);

        String body = String.format("Необходимо выполнить %s отчета за %s сотрудника %s \r\n " +
                "Комментарий: %s",
                timeSheet.getReportSendApprovalType().getName(),
                date,
                fio,
                timeSheet.getDeleteSendApprovalComment());

        mail.setPreconstructedMessageBody(body);

        return Arrays.asList(mail);
    }
}
