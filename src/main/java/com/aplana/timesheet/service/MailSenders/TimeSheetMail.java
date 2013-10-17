package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.properties.TSPropertyProvider;

/**
 * User: vsergeev
 * Date: 22.02.13
 */
public class TimeSheetMail extends Mail {

    @Override
    public String getFromEmail() {
        return TSPropertyProvider.getMailFromAddress();
    }

}
