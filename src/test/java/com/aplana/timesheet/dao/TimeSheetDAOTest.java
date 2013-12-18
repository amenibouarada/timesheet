package com.aplana.timesheet.dao;

import com.aplana.timesheet.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author pmakarov
 * @see <a href="">Аналитика</a>
 *      creation date: 17.12.13
 */
public class TimeSheetDAOTest extends AbstractTest {
    @Autowired
    TimeSheetDAO timeSheetDAO;

    @Test
    public void getT(){
        timeSheetDAO.getOverdueTimesheet(1L, new Date(), new Date());
    }
}
