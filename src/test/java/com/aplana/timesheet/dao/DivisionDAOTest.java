package com.aplana.timesheet.dao;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Division;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */

public class DivisionDAOTest extends AbstractTest {
    @Autowired
    DivisionDAO divisionDAO;

    @Test
    public void testGetDivisions() throws Exception {
        List<Division> divisionsForSync = divisionDAO.getActiveDivisions();
        Assert.assertFalse(divisionsForSync.isEmpty());
    }

    @Test
    public void testGetByDepartmentName() {
        String departmentName = "Центр заказной разработки";
        Division division = divisionDAO.findByDepartmentName(departmentName);
        Assert.assertNotNull(division);
        Assert.assertTrue(division.getDepartmentName().contains(departmentName));
    }
}
