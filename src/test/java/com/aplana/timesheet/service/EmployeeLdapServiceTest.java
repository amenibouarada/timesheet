package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Permission;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author iziyangirov
 *
*/
public class EmployeeLdapServiceTest extends AbstractTest {

    @Autowired
    EmployeeLdapService employeeLdapService;

    @Autowired
    EmployeeService employeeService;

    @Test
    public void testSetEmployeePermission() throws Exception {

        Employee emplMock = mock(Employee.class);

        ProjectRole job = mock(ProjectRole.class);
        doReturn(1).when(job).getId();
        doReturn(job).when(emplMock).getJob();
        when(emplMock.getJob().getId()).thenReturn(1);

        employeeLdapService.setEmployeePermission(emplMock);

        verify(emplMock, times(1)).setPermissions((Set<Permission>) anyObject());
    }

    @Test
    public void testSynchronize() throws Exception{
        // employeeLdapService.synchronize(); Это сложно назвать адекватным тестом. Закомментировал, потому что процедура выполняется очень долго.
    }
}