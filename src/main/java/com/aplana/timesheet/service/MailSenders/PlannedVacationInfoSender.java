package com.aplana.timesheet.service.MailSenders;


import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.*;



/**
 * Created with IntelliJ IDEA.
 * User: bsirazetdinov
 * Date: 22.07.13
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class PlannedVacationInfoSender extends AbstractSenderWithAssistants<Map <Employee, Set<Vacation>> > {

    private static final String EMPLOYEE_NAME = "employeeName";
    private static final String EMPLOYEE_JOB = "employeeJob";
    private static final String BEGIN_DATE = "beginDate";
    private static final String END_DATE = "endDate";
    private static final String VACATION_TYPE = "vacationType";



    public PlannedVacationInfoSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    private Table<Integer, String, String> getBody(Set<Vacation> managerEmployeesVacation) {
        Table<Integer, String, String> result = HashBasedTable.create();

            SimpleDateFormat format = new SimpleDateFormat(super.DATE_FORMAT);
            Integer i = 0;
            for(Vacation vacation:managerEmployeesVacation) {
                result.put(i, EMPLOYEE_NAME,  vacation.getEmployee().getName());
                result.put(i, EMPLOYEE_JOB,vacation.getEmployee().getJob().getName());
                result.put(i, BEGIN_DATE,  format.format(vacation.getBeginDate()));
                result.put(i, END_DATE, format.format(vacation.getEndDate()));
                result.put(i, VACATION_TYPE, vacation.getType().getValue());
                i++;
            }
        return result;
    }

    @Override
    public List<Mail> getMailList(Map <Employee, Set<Vacation>> managerEmployeesVacation) {
        final List<Mail> mails = new ArrayList<Mail>();
        for(Map.Entry<Employee, Set<Vacation>> entry : managerEmployeesVacation.entrySet()) {
            Mail mail = new TimeSheetMail();
            ArrayList<String> toEmails = Lists.newArrayList(entry.getKey().getEmail());
            mail.setToEmails(toEmails);
            mail.setSubject("Информация по планируемым отпускам сотрудников в ближайшие две недели.");
            mail.setParamsForGenerateBody(getBody(entry.getValue()));
            mail.setCcEmails(getAssistantEmail(getManagersEmails(mail, entry.getKey())));
            mails.add(mail);
        }

        return mails;
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        Map model = new HashMap();
        Calendar calendar = Calendar.getInstance();
        //SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        model.put("forDate", format.format(calendar.getTime()));
        model.put("paramsForGenerateBody", mail.getParamsForGenerateBody());

        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "plannedVacations.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }
}
