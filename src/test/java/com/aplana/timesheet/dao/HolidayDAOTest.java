package com.aplana.timesheet.dao;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Region;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class HolidayDAOTest extends AbstractTest {

    @Autowired
    HolidayDAO holidayDAO;

    @PersistenceContext
    private EntityManager entityManager;

    /* используем не самописные получение данных из базы */
    private Region find(String regionName) {
        Query query = entityManager.createQuery(
                "from Region as r where r.name =:regionName"
        ).setParameter("regionName", regionName);

        return (Region) query.getResultList().get(0);
    }


    @Test
    public void testIsWorkDay1() throws Exception {
        assertTrue(holidayDAO.isWorkDay("2013-08-08", find("Пермь")));
    }

    @Test
    public void testIsWorkDay2() throws Exception {
        assertFalse(holidayDAO.isWorkDay("2013-08-08", find("Уфа")));
    }

    @Test
    public void testIsWorkDay3() throws Exception {
        assertFalse(holidayDAO.isWorkDay("2013-06-01", find("Пермь")));
        assertFalse(holidayDAO.isWorkDay("2013-06-01", find("Уфа")));
    }

    @Test
    public void testIsWorkDay4() throws Exception {
        assertTrue(holidayDAO.isWorkDay("2013-06-06", find("Пермь")));
        assertTrue(holidayDAO.isWorkDay("2013-06-06", find("Уфа")));
    }

    @Test
    public void testIsWorkDay5() throws Exception {
        assertFalse(holidayDAO.isWorkDay("2013-01-01"));
        assertTrue(holidayDAO.isWorkDay("2013-06-06"));
    }


}
