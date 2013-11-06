package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeProjectPlanDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeProjectPlan;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class EmployeeProjectPlanService {

    @Autowired
    private EmployeeProjectPlanDAO employeeProjectPlanDAO;

    public List<EmployeeProjectPlan> find(Employee employee, Integer year, Integer month) {
        return employeeProjectPlanDAO.find(employee, year, month);
    }

    @Transactional
    public void mergeEmployeeProjectPlans(List<EmployeeProjectPlan> employeeProjectPlans) {
        for (EmployeeProjectPlan employeeProjectPlan : employeeProjectPlans) {
            if (employeeProjectPlan.getValue() == null || employeeProjectPlan.getValue() == 0)
                employeeProjectPlanDAO.remove(employeeProjectPlan);
            else
                employeeProjectPlanDAO.store(employeeProjectPlan);
        }

    }

    @Transactional
    public void store(EmployeeProjectPlan employeeProjectPlan) {
        employeeProjectPlanDAO.store(employeeProjectPlan);
    }

    public EmployeeProjectPlan tryFind(Employee employee, Integer year, Integer month, Project project) {
        return employeeProjectPlanDAO.tryFind(employee, year, month, project);
    }

    @Transactional
    public void remove(Employee employee, Integer year, Integer month) {
        employeeProjectPlanDAO.remove(employee, year, month);
    }

    @Transactional
    public void updateEmployeeProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm, Double plan){
        employeeProjectPlanDAO.updateEmployeeProjectPlan(employeeId, employmentPlanningForm, plan);
    }

    @Transactional
    public void updateEmployeeNotProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm, Double plan){
        employeeProjectPlanDAO.updateEmployeeNotProjectPlan(employeeId, employmentPlanningForm, plan);
    }
}
