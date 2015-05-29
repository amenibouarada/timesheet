package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeProjectBillableDAO;
import com.aplana.timesheet.dao.entity.EmployeeProjectBillable;
import com.aplana.timesheet.dao.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
     * Возвращает запись о возможности списания занятости по её идентификатору в БД.
     * @param id Идентификатор в базе данных
     * @return Запись о возможности списания занятости
     */
    @Transactional(readOnly = true)
    public EmployeeProjectBillable find(Integer id) {
        return employeeProjectBillableDAO.find(id);
    }

    /**
     * Возвращает все записи о возможности списания занятости по проекту.
     * @param project Проект
     * @return Список записей
     */
    @Transactional(readOnly = true)
    public List<EmployeeProjectBillable> findByProject(Project project) {
        return employeeProjectBillableDAO.findByProject(project);
    }

    /**
     * Удаляет из базы запись о возможности списания занятости.
     * @param projectBillable Запись о возможности списания занятости
     */
    @Transactional(propagation = Propagation.NESTED)
    public void delete(EmployeeProjectBillable projectBillable) {
        employeeProjectBillableDAO.delete(projectBillable);
    }
}
