package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author iziyangirov
 */

public class VacationApprovalAcceptanceSender extends AbstractSenderWithCcAddress<VacationApproval>
        implements MailWithCcAddresses<VacationApproval>{

    final String DATE_FORMAT = "dd.MM.yyyy";
    final String MAIL_ACCEPT_SUBJECT = "Согласован отпуск %s - %s";
    final String MAIL_ACCEPT_BODY = "%s согласовал(а) \"%s\" сотрудника %s из г. %s на период с %s - %s.";
    final String MAIL_REFUSE_SUBJECT = "Согласован отпуск %s - %s";
    final String MAIL_REFUSE_BODY = "%s не согласовал(а) \"%s\" сотрудника %s из г. %s на период с %s - %s.";

    public VacationApprovalAcceptanceSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    public List<Mail> getMainMailList(VacationApproval vacationApproval) {
        Mail mail = new TimeSheetMail();

        Integer vacationId = vacationApproval.getVacation().getId();
        String matchingFIO = vacationApproval.getManager().getName();
        String vacationType = vacationApproval.getVacation().getType().getValue();
        String employeeFIO = vacationApproval.getVacation().getEmployee().getName();
        String region = vacationApproval.getVacation().getEmployee().getRegion().getName();
        String dateBegin = DateFormatUtils.format(vacationApproval.getVacation().getBeginDate(), DATE_FORMAT);
        String dateEnd = DateFormatUtils.format(vacationApproval.getVacation().getEndDate(), DATE_FORMAT);
        Boolean result = vacationApproval.getResult();

        String subject = result ? String.format(MAIL_ACCEPT_SUBJECT, dateBegin, dateEnd) :
                String.format(MAIL_REFUSE_SUBJECT, dateBegin, dateEnd);
        String text = result ? String.format(MAIL_ACCEPT_BODY, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd) :
                String.format(MAIL_REFUSE_BODY, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd);

        mail.setSubject(subject);
        mail.setPreconstructedMessageBody(text);
        mail.setToEmails( Arrays.asList(vacationApproval.getVacation().getEmployee().getEmail()) );
        mail.setCcEmails(sendMailService.getVacationApprovalEmailList(vacationId));

        return Arrays.asList(mail);
    }

    @Override
    final public String getCcEmail(VacationApproval vacationApproval) {
        Vacation vacation = vacationApproval.getVacation();
        return (vacation.getEmployee().getId().equals(vacation.getAuthor().getId())) ? StringUtils.EMPTY : vacation.getAuthor().getEmail();
    }

    @Override
    protected String getSubjectFormat() {
        return propertyProvider.getVacationMailMarker() + " %s";
    }
}
