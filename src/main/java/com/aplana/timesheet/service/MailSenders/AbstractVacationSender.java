package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.ManagerRoleNameService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * User: vsergeev
 * Date: 15.02.13
 */
public abstract class AbstractVacationSender<T> extends AbstractSenderWithAssistants<T>{

    public AbstractVacationSender(SendMailService sendMailService, TSPropertyProvider propertyProvider,
                                  VacationApprovalService vacationApprovalService, ManagerRoleNameService managerRoleNameService) {
        super(sendMailService, propertyProvider, vacationApprovalService, managerRoleNameService);
    }

    @Override
    protected String getSubjectFormat() {
        String marker = propertyProvider.getVacationMailMarker();

        return marker + " %s";
    }

    @Override
    protected final void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        try {
            if (mail.getParamsForGenerateBody() != null) {
                message.setText(mail.getParamsForGenerateBody().get(FIRST, MAIL_BODY), "UTF-8", "html");
            }else{
                super.initMessageBody(mail, message);
            }
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }
}
