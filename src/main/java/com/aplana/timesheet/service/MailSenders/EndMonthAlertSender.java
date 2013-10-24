package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.aplana.timesheet.util.DateTimeUtil.currentDay;
import static com.aplana.timesheet.util.DateTimeUtil.getMonthTxt;

public class EndMonthAlertSender extends MailSender<List<ReportCheck>> {

    public EndMonthAlertSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "velocity/alertendmonthmail.vm", new HashMap());
        logger.debug("Message Body: {}", messageBody);

        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(List<ReportCheck> params) {
        Mail mail = new TimeSheetMail();
        
        mail.setSubject(String.format("Не забудьте списать занятость за %s", getMonthTxt(currentDay())));
        mail.setToEmails(getToEmails(params));
        
        return Lists.newArrayList(mail);
    }

    private Iterable<String> getToEmails(List<ReportCheck> params) {
        final List<String> emails = new ArrayList<String>(params.size());
        Division division;

        for (ReportCheck param : params) {
            division = param.getDivision();

            if (division != null && division.isCheck() && StringUtils.isNotBlank(division.getEmail())) {
                emails.add(division.getEmail());
            }
        }

        return emails;
    }
}
