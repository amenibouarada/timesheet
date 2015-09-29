package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.DeleteTimeSheetApproval;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang3.StringUtils;
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
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format("Оповещение о просьбе для удалении или отправки в черновик отчета (%s)", this.getClass().getSimpleName());
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

        Employee employee = timeSheet.getEmployee();
        String employeeName = employee.getName();
        String employeeEmail = employee.getEmail();
        String date = DateTimeUtil.formatDateIntoViewFormat(timeSheet.getCalDate().getCalDate());
        String fio = Padeg.getFIOPadegFS(employeeName, employee.getSex(), PadegConstants.Roditelnyy);

        mail.setToEmails(Arrays.asList(propertyProvider.getMailProblemAndProposalsToAddress()));
        mail.setCcEmails(Arrays.asList(employeeEmail));

        DeleteTimeSheetApproval deleteTimeSheetApproval = timeSheet.getDeleteTimeSheetApproval();

        String subject = String.format(" Запрос на %s отчета за %s от %s",
                deleteTimeSheetApproval.getReportSendApprovalType().getName(),
                date,
                fio);

        mail.setSubject(propertyProvider.getDeleteOrSetDraftApprovalMarker() + subject);

        String comment = deleteTimeSheetApproval.getDeleteSendApprovalComment();
        mail.setPreconstructedMessageBody(getMessageBody(fio, employeeEmail, comment, employee.getDivision() == null ? null : employee.getDivision().getName()));

        return Arrays.asList(mail);
    }

    private String getMessageBody(String name, String email, String comment, String employeeDivision) {
        final StringBuilder bodyTxt = new StringBuilder();

        bodyTxt.append(String.format("Комментарий: %s", comment));

        if (StringUtils.isNotBlank(name)) {
            bodyTxt.append("\n\nПришло от: ").append(name);
        }

        if (StringUtils.isNotBlank(employeeDivision)) {
            bodyTxt.append(" (").append(employeeDivision).append(")");
        }

        if (StringUtils.isNotBlank(email)) {
            bodyTxt.append("\nС адреса: ").append(email);
        }

        bodyTxt.append("\nТип сообщения: ").append("Другое");

        return bodyTxt.toString().replace("\n", "<br>");
    }
}
