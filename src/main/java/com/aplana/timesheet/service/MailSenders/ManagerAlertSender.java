package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.properties.TSPropertyProvider;
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

public class ManagerAlertSender extends AbstractSenderWithAssistants<List<ReportCheck>> {

    public ManagerAlertSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

     @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        Map model = new HashMap();
        model.put("division", mail.getDivision().getName());
        model.put("employeeList", mail.getEmployeeList());
        model.put("passedDays", mail.getPassedDays());
        //если это центр заказной разработки
        model.put("region", mail.getDivision().getId() == 1 ? "show" : "");

        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "alertmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(List<ReportCheck> params) {
        logger.info("Performing manager mailing.");
        // Формируем для каждого руководителя отдельный список отчетов его подчиненных
        Map<Employee, List> managerMap = new HashMap<Employee, List>();

        for ( ReportCheck reportCheck : params ) {
            addReportToManagerLists(reportCheck.getEmployee(), reportCheck, managerMap);
        }

        List<Mail> mails = new ArrayList<Mail>();

        // Для каждого руководителя формируем отдельное письмо со списком отчетов по его подчиненным
        for (Map.Entry<Employee, List> entry : managerMap.entrySet()) {
            Employee currentManager = entry.getKey();
            //если руководитель помечен как archived
            //не было реал. в storeReportCheck тк список руководителей формируется здесь(выше)
            if (currentManager.isDisabled(null))
                logger.info("Manager {} is disabled", currentManager.getName());
            else {
                Mail mail = new TimeSheetMail();

                List<ReportCheck> currentReportCheckList = entry.getValue();

                mail.setToEmails(Arrays.asList(currentManager.getEmail()));
                mail.setCcEmails(getAssistantEmail(getManagersEmails(mail, currentManager)));
                mail.setSubject(getSubject(currentReportCheckList));
                mail.setDivision(currentReportCheckList.get(0).getDivision());
                mail.setEmployeeList(getEmployeeList(currentManager, currentReportCheckList));
                mail.getPassedDays().putAll(getPassedDays(currentReportCheckList));

                mails.add(mail);
            }
        }
        return mails;
    }

    /**
     * Процедура рекурсивно проходит всех руководителей изначально переданного сотрудника
     * и добавляет для этих руководителей в спец. списки ссылки на reportCheck сотрудника о несписанной занятости
     *
     * @param employee
     * @param reportCheck
     */
    private Map<Employee, List> addReportToManagerLists(Employee employee, ReportCheck reportCheck, Map<Employee, List> managerMap) {

        Employee manager = employee.getManager();

        if (manager != null) {
            if (!managerMap.containsKey(manager)) {
                managerMap.put(manager, new ArrayList<ReportCheck>());
            }

            managerMap.get(manager).add(reportCheck);

            return addReportToManagerLists(manager, reportCheck, managerMap);
        }
        return managerMap;
    }

    private Map<Employee,List<String>> getPassedDays(List<ReportCheck> currentReportCheckList) {
        Map<Employee,List<String>> result = new HashMap<Employee, List<String>>();

        for (ReportCheck reportCheck : currentReportCheckList) {
            result.put(reportCheck.getEmployee(), reportCheck.getPassedDays());
        }
        return result;
    }

    private String getSubject(List<ReportCheck> currentReportCheckList) {
        Iterable<String> concat = Iterables.concat(Iterables.transform(currentReportCheckList, new Function<ReportCheck, Iterable<String>>() {
            @Nullable
            @Override
            public Iterable<String> apply(@Nullable ReportCheck params) {
                return params.getPassedDays();
            }
        }));

        return  propertyProvider.getTimesheetMailMarker() + // APLANATS-571
                " Не отчитались за " + Joiner.on(", ").join(
                Sets.newHashSet(Iterables.transform(concat, new Function<String, String>() {
                    @Nullable @Override
                    public String apply(@Nullable String params) {
                        return DateTimeUtil.getMonthTxt(params);
                    }
                }))
        );
    }

    private List<Employee> getEmployeeList(Employee manager, List<ReportCheck> reportCheckList){
        List<Employee> employeeList = new ArrayList<Employee>();
        for (ReportCheck reportCheck : reportCheckList){
            employeeList.add(reportCheck.getEmployee());
        }
        return employeeList;
    }

}
