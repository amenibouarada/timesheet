package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeProjectBillableDAO;
import com.aplana.timesheet.dao.entity.EmployeeProjectBillable;
import com.aplana.timesheet.dao.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author dsysterov
 * @version 1.0
 */
@Service
public class EmployeeProjectBillableService {

    @Autowired
    private EmployeeProjectBillableDAO employeeProjectBillableDAO;

    /**
     * Возвращает все отметки о возможности списания занятости по проекту.
     * @param project Проект
     * @return Список отметок
     */
    @Transactional(readOnly = true)
    public List<EmployeeProjectBillable> findByProject(Project project) {
        return employeeProjectBillableDAO.findByProject(project);
    }
}
