package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.TestUtils;
import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.*;

import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.TypesOfTimeSheetEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.util.DateTimeUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Calendar;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * User: bsirazetdinov
 * Date: 07.08.13
 */
public class PlannedVacationServiceTest extends AbstractTest {

    @Mock
    private EmployeeDAO employeeDAO;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    PlannedVacationService plannedVacationService;

    private Project project;
    private List<Project> projects;
    private Employee employee;
    private List<Employee> employees;
    private Employee manager;
    private List<Employee> managers;

    @Before
    public void initData() {
        MockitoAnnotations.initMocks(this);

        employee = TestUtils.createEmployee();
        project = TestUtils.createProject();
        manager = TestUtils.createManager();

        employees = new ArrayList<Employee>();
        employees.add(employee);
        projects = new ArrayList<Project>();
        projects.add(project);
        managers = new ArrayList<Employee>();
        managers.add(manager);

        when(employeeDAO.getEmployeeWithPlannedVacation((Date) any(), (Date) any())).thenReturn(employees);
        when(employeeDAO.getProjectManagers(project)).thenReturn(managers);
        when(projectService.getEmployeeProjectsFromTimeSheetByDates((Date) any(), (Date) any(), (Employee) any())).thenReturn(projects);
        when(projectService.getEmployeeProjectPlanByDates((Date) any(), (Date) any(), (Employee) any())).thenReturn(projects);
    }

    @Test
    public void testVacation() {

        Map<Employee, Set<Employee>> result = plannedVacationService.getEmployeeManagers(employees);
        Assert.assertNotNull(result);

        /*
            Необходимо протестировать
            reverseEmployeeManagersToManagerEmployees
            getManagerEmployeesVacation
            remindDeletePlannedVacation
            getDateByCurrentDayPeriod
        */
    }
}
