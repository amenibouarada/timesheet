package com.aplana.timesheet.dao;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author pmakarov
 *      creation date: 29.11.13
 */
public class EmployeeProjectPlanTest extends AbstractTest{

    @Autowired
    EmployeeProjectPlanDAO employeeProjectPlanDAO;

    private EmploymentPlanningForm employmentPlanningForm;
    private Integer projectId;
    private Integer employeeId;
    private Integer monthBegin;
    private Integer monthEnd;
    private Integer yearBegin;
    private Integer yearEnd;
    private Double value;

    @Before
    public void init(){
        employmentPlanningForm = new EmploymentPlanningForm();

        projectId = -1;
        employeeId = -1;
        monthBegin = 1;
        monthEnd = 12;
        yearBegin = 2000;
        yearEnd = 3000;
        value = -1.0d;

        employmentPlanningForm.setSelectDivisionId(0);
        employmentPlanningForm.setYearBeg(yearBegin);
        employmentPlanningForm.setMonthBeg(monthBegin);
        employmentPlanningForm.setYearEnd(yearEnd);
        employmentPlanningForm.setMonthEnd(monthEnd);
        employmentPlanningForm.setProjectId(projectId);
    }

    @Test
    public void getProjectPlan(){
        employeeProjectPlanDAO.getProjectPlan(employmentPlanningForm);
    }

    @Test
    public void updateEmployeeProjectPlan(){
        employeeProjectPlanDAO.updateEmployeeProjectPlan(employeeId, employmentPlanningForm, value);
    }

    @Test
    public void updateEmployeeNotProjectPlan(){
        employeeProjectPlanDAO.updateEmployeeNotProjectPlan(employeeId, employmentPlanningForm, value);
    }

    @Test
    public void getEmployeePlan(){
        employeeProjectPlanDAO.getEmployeePlan(employeeId, yearBegin, monthBegin, yearEnd, monthEnd);
    }
}
