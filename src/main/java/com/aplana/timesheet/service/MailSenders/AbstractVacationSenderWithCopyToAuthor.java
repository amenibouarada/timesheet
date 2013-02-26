package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * User: vsergeev
 * Date: 22.02.13
 */
public abstract class AbstractVacationSenderWithCopyToAuthor extends AbstractSenderWithCcAddress<Vacation>
        implements MailWithCcAddresses<Vacation>{

    public AbstractVacationSenderWithCopyToAuthor(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    final protected void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        try {
            message.setText(mail.getParamsForGenerateBody().get(FIRST, MAIL_BODY), "UTF-8", "plain");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    final public String getCcEmail(Vacation vacation) {
        return (vacation.getEmployee().getId().equals(vacation.getAuthor().getId())) ? StringUtils.EMPTY : vacation.getAuthor().getEmail();
    }
}
