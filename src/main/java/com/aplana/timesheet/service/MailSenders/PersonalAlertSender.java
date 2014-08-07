package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class PersonalAlertSender extends AbstractSenderWithAssistants<List<ReportCheck>> {

    public PersonalAlertSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
        logger.info("Run sending message for: {}", getName());
    }

    String getName() {
        return String.format(" Оповещение о несписанной занятости (%s)", this.getClass().getSimpleName());
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        Map model = new HashMap();
        model.put("passedDays", mail.getPassedDays().get(null));
        model.put("employee", Iterables.getFirst(mail.getEmployeeList(), null));
        model.put("timesheeturl",propertyProvider.getTimeSheetURL());
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "velocity/alertpersonalmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(List<ReportCheck> params) {
        logger.info("Performing personal mailing.");

        List<Mail> mails = new ArrayList<Mail>();

        for ( ReportCheck currentReportCheck : params ) {
            Mail mail = new TimeSheetMail();
            final Employee employee = currentReportCheck.getEmployee();
            mail.setToEmails(Arrays.asList(employee.getEmail()));
            mail.setSubject(getSubject(currentReportCheck));
            mail.setEmployeeList(Arrays.asList(employee));

            mail.getPassedDays().put(null, currentReportCheck.getPassedDays());
            mails.add(mail);
        }
        return mails;
    }

    private String getSubject(ReportCheck currentReportCheck) {
        return  propertyProvider.getTimesheetMailMarker() +
                " Отчитаться за " + Joiner.on(", ").join(
                Sets.newHashSet(Iterables.transform(currentReportCheck.getPassedDays(), new Function<String, String>() {
                    @Nullable @Override
                    public String apply(@Nullable String input) {
                        return DateTimeUtil.getMonthTxt(input);
                    }
                }))
        );
    }
}
