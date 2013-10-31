package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeAssistantDAO;
import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class EmployeeAssistantService {

    @Autowired
    private EmployeeAssistantDAO employeeAssistantDAO;

    public EmployeeAssistant find(Set<String> managersEmails) {
        return employeeAssistantDAO.find(managersEmails);
    }

    public List<EmployeeAssistant> tryFind(Set<String> managersEmails) {
        return employeeAssistantDAO.tryFind(managersEmails);
    }

    public void changeAssistantActivity(){
        employeeAssistantDAO.updateEmployeeAssistantActiveStatus();
    }

}
