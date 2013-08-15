package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */
/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})
public class EmployeeDAOTest {
    @Autowired
    private EmployeeDAO employeeDAO;

    *//*@Test
    public void testFindByObjectSid() throws Exception {
        Employee byObjectSid = employeeDAO.findByObjectSid("S-1-5-21-725345543-1454471165-1801674531-8646");
        Assert.assertNotNull(byObjectSid);
    }*//*

    *//*@Test
    public void testGetActiveEmployeesNotInList() {
        List<Employee> employees = employeeDAO.getEmployees(null);

        int limitSize = 50;

        List<Integer> ids = Lists.newArrayList(Iterables.transform(
                Iterables.limit(employees, limitSize),
                new Function<Employee, Integer>() {
                    @Nullable @Override
                    public Integer apply(Employee input) {
                        return input.getId();
                    } }));

        List<Employee> activeEmployeesNotInList = employeeDAO.getActiveEmployeesNotInList(ids);
        Assert.assertEquals(activeEmployeesNotInList.size(), employees.size() - limitSize);
    }*//*
}*/
