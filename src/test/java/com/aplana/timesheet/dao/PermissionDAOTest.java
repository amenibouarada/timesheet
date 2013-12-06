package com.aplana.timesheet.dao;

import com.aplana.timesheet.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author pmakarov
 * @see <a href="">Аналитика</a>
 *      creation date: 04.12.13
 */
public class PermissionDAOTest extends AbstractTest{

    @Autowired
    PermissionDAO permissionDAO;

    @Test
    public void findTest(){
        permissionDAO.find(1);
    }
}
