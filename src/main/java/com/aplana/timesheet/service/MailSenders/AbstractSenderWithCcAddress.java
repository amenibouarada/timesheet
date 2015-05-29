package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: vsergeev
 * Date: 22.02.13
 */
public abstract class AbstractSenderWithCcAddress<T> extends AbstractSenderWithAssistants<T>
        implements MailWithCcAddresses<T>{

    public AbstractSenderWithCcAddress(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    protected final List<Mail> getMailList(T params) {
        List<Mail> mailList = getMainMailList(params);
        String ccEmail = getCcEmail(params);
        addAddressToCcEmails(mailList, ccEmail);

        return mailList;
    }

    protected final List<Mail> addAddressToCcEmails(List<Mail> mailList, String ccEmail) {
        for (Mail mail : mailList) {
            ArrayList<String> emails = Lists.newArrayList(mail.getCcEmails());
            emails.add(ccEmail);
            mail.setCcEmails(emails);
        }

        return mailList;
    }

    protected final Iterable<String> getNotBlankEmails(Collection<String> ccEmails) {
        return Iterables.filter(ccEmails, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String email) {
                return StringUtils.isNotBlank(email);
            }
        });
    }

}
