package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: vsergeev
 * Date: 13.02.13
 */
public class VacationApprovedSender extends AbstractVacationSenderWithCopyToAuthor {

    protected static final Logger logger = LoggerFactory.getLogger(VacationApprovedSender.class);

    private final List<String> emails;

    public VacationApprovedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider,
                                  List<String> emails) {
        super(sendMailService, propertyProvider);
        this.emails = emails;
    }

    @Override
    public List<Mail> getMainMailList (Vacation vacation) {
        final Mail mail = new TimeSheetMail();
        final Employee employee = vacation.getEmployee();

        mail.setToEmails(emails);

        final Collection<String> ccEmails =
                new ArrayList<String>(getAdditionalEmailsForRegion(employee.getRegion()));

        ccEmails.add(getAssistantEmail(getManagersEmails(mail, employee)));

        mail.setCcEmails(getNotBlankEmails(ccEmails));

        if (vacation.getStatus().getId().equals(VacationStatusEnum.APPROVED.getId())) {
            addApprovedContent(vacation, mail);
        } else {
            addRejectedContent(vacation, mail);
        }

        return Arrays.asList(mail);
    }

    private Iterable<String> getNotBlankEmails(Collection<String> ccEmails) {
        return Iterables.filter(ccEmails, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String email) {
                return StringUtils.isNotBlank(email);
            }
        });
    }

    private void addRejectedContent(Vacation vacation, Mail mail) {
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getRejectedBody(vacation));
    }

    private Table<Integer, String, String> getRejectedBody(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        String messageBody = String.format("Отклонен %s сотрудника %s из г. %s на период с %s - %s",
                vacation.getType().getValue(), vacation.getEmployee().getName(), vacation.getEmployee().getRegion().getName(), beginDateStr, endDateStr);

        return getMessageBody(messageBody);
    }

    private void addApprovedContent(Vacation vacation, Mail mail) {
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getApprovedBody(vacation));
    }

    private Collection<String> getAdditionalEmailsForRegion(Region region) {
        String additionalEmails = region.getAdditionalEmails();

        return  (StringUtils.isNotBlank(additionalEmails)) ? Arrays.asList(additionalEmails.split("\\s*,\\s*")) : Arrays.asList(StringUtils.EMPTY);
    }

    private Table<Integer, String, String> getApprovedBody(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        String messageBody = String.format("Успешно согласован %s сотрудника %s из г. %s на период с %s - %s",
                vacation.getType().getValue(), vacation.getEmployee().getName(), vacation.getEmployee().getRegion().getName(), beginDateStr, endDateStr);

        return getMessageBody(messageBody);
    }

    private Table<Integer, String, String> getMessageBody(String messageBody) {
        final Table<Integer, String, String> table = HashBasedTable.create();
        table.put(FIRST, MAIL_BODY, messageBody);

        return table;
    }

    private String getSubject(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        return  String.format("Согласование %s сотрудника %s на период с %s - %s", vacation.getType().getValue(), vacation.getEmployee().getName(),
                        beginDateStr, endDateStr);
    }

}
