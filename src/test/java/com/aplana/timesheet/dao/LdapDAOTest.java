package com.aplana.timesheet.dao;

import com.aplana.timesheet.controller.TimeSheetController;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * @author eshangareev
 * @version 1.0
 */
/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})
public class LdapDAOTest {
    private static final Logger logger = LoggerFactory.getLogger(LdapDAOTest.class);

    @Autowired
    LdapDAO ldapDAO;


    @Test
    public void testGetEmployee() throws Exception {
        EmployeeLdap employee = ldapDAO.getEmployeeByEmail("Evgeniy.Sikachev@aplana.com");
        Assert.assertEquals(employee.getDepartment(), "Центр заказной разработки");
        Assert.assertEquals(employee.getDisplayName(), "Сикачёв Евгений");
        Assert.assertEquals(employee.getCity(), "Уфа");
    }

    @Test
    public void testGetEmployeeByName() throws Exception {
        EmployeeLdap employeeByName = ldapDAO.getEmployeeByLdapName("CN=Preobrazhensky Andrey,CN=Users,DC=aplana,DC=com");
        System.out.println(employeeByName);
        Assert.assertNotNull(employeeByName);
    }

    @Test
    public void testGetDivisionLeader() throws Exception {
        List<EmployeeLdap> divisionLeader = ldapDAO.getDivisionLeader("Сикачёв Евгений", "Центр заказной разработки");
        for (EmployeeLdap employeeLdap : divisionLeader) {
            Assert.assertNotNull(employeeLdap.getDisplayName());
            Assert.assertFalse(StringUtils.isBlank(employeeLdap.getDisplayName()));
        }
    }

    @Test
    public void testGetDivisions() throws Exception {
        List<Map> divisions = ldapDAO.getDivisions();
        Assert.assertNotNull(divisions);
    }

    @Test
    public void testGetEmployees() {
        List<EmployeeLdap> employees = ldapDAO.getEmployees("Центр заказной разработки");
        Assert.assertFalse(employees.isEmpty());
    }


}*/
