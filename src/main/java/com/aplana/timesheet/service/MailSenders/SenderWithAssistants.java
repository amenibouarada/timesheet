package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;

/**
 * Created by arozhkov on 06.06.2014.
 */
public class SenderWithAssistants extends AbstractSenderWithAssistants {

    public SenderWithAssistants(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }
}
