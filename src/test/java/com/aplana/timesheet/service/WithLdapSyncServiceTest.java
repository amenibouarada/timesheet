package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * @author eshangareev
 * @version 1.0
 */
/*
public class WithLdapSyncServiceTest extends AbstractTest {
    @Autowired
    private WithLdapSyncService service;

    @Autowired
    LdapDAO ldapDAO;

    @Autowired
    DivisionDAO dao;

    @Autowired
    EmployeeDAO employeeDAO;

    @Autowired
    ProjectRoleDAO projectRoleDAO;

    @Autowired
    RegionDAO regionDAO;

    @Autowired
    ProjectRolePermissionsDAO projectRolePermissionsDAO;

    @Test
    public void testCreateUser() throws Exception {
        EmployeeLdap employeeByDisplayName = ldapDAO.getEmployeeByDisplayName("Шангареев Эдуард");
        Employee user = service.createUser(employeeByDisplayName);

        Assert.assertNotNull(user.getEmail());
        Assert.assertNotNull(user.getJob());
        Assert.assertNotNull(user.getManager());
        Assert.assertNotNull(user.getName());
        Assert.assertNotNull(user.getObjectSid());
        Assert.assertNotNull(user.getDivision());
        Assert.assertNotNull(user.getStartDate());
        Assert.assertNotNull(user.getLdap());
        Assert.assertFalse(user.getPermissions().isEmpty());
        Assert.assertNotNull(user.getRegion());
        Assert.assertNull(user.getEndDate());
        Assert.assertNull(user.getId());
    }

    @Test
    public void testCreateDivision(){
        List<Map> divisions = ldapDAO.getDivisions();
        for (Map division : divisions) {
            Division newDivision = service.createNewDivision(division);
            Assert.assertNotNull(newDivision.getName());
            Assert.assertNotNull(newDivision.getLdapName());
        }
    }

    @Test
    public void testSyncWithLdap(){
        service.syncWithLdap();
    }

    @Test
    public void testFindDbDivision() {
        Division dbDivision = service.findDbDivision(dao.getAllDivisions(), "S-1-5-21-725345543-1454471165-1801674531-8066");
        Assert.assertNotNull(dbDivision);

    }
} */
