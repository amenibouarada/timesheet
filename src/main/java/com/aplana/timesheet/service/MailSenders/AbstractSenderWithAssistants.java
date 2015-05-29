package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import com.aplana.timesheet.service.ManagerRoleNameService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractSenderWithAssistants<T> extends MailSender<T> {

    public AbstractSenderWithAssistants(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    public AbstractSenderWithAssistants(SendMailService sendMailService, TSPropertyProvider propertyProvider,
                                        VacationApprovalService vacationApprovalService, ManagerRoleNameService managerRoleNameService) {
        super(sendMailService, propertyProvider, vacationApprovalService, managerRoleNameService);
    }

    protected final Set<String> getAssistantEmail(Set<String> managersEmails) {
        final Set<String> emails = new HashSet<String>();
        final List<EmployeeAssistant> employeeAssistantList = sendMailService.getEmployeeAssistant(managersEmails);
        if(employeeAssistantList == null) return null;
        for (EmployeeAssistant employeeAssistant : employeeAssistantList){
            if (employeeAssistant != null && (employeeAssistant.isActive() == null || employeeAssistant.isActive())){
                Employee assistant = employeeAssistant.getAssistant();
                if (assistant.getEndDate() == null){
                    emails.add(employeeAssistant.getAssistant().getEmail());
                }
            }
        }
        return emails;
    }

}
