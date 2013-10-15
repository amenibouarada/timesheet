package com.aplana.timesheet.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})

public class ReporServiceTest {
    /**
     http://jira.aplana.com/browse/ITIASMK-398
     APLANATS-780 - Формирование отчетов. Сообщение "Не найдены отчеты, удовлетворяющие заданным параметрам" не исчезает
     http://jira.aplana.com/secure/ContactAdministrators!default.jspa
     http://conf.aplana.com/dashboard.action"
     */
    private static final String TEST_STRING0 = "APLANATS-780 - Формирование отчетов. Сообщение \"Не найдены отчеты, удовлетворяющие заданным параметрам\" не исчезает";

    private static final String TEST_STRING1 = "http://jira.aplana.com/browse/ITIASMK-398";
    private static final String TEST_STRING2 = "http://conf.aplana.com/dashboard.action";
    private static final String TEST_STRING3 = "http://jira.aplana.com/secure/ContactAdministrators!default.jspa";

    @Autowired
    ReportService reportService;

    @Test
    public void replaceJiraLink(){
        String resultTrue = "<a href='http://jira.aplana.com/browse/APLANATS-780'>APLANATS-780</a> - Формирование отчетов. Сообщение \"Не найдены отчеты, удовлетворяющие заданным параметрам\" не исчезает";
        String resultTest = reportService.replaceJiraLink(TEST_STRING0);

        assertEquals(resultTest, resultTrue);
    }


    @Test
    public void shortLinkJiraLink(){
        String resultTrue = "ITIASMK-398";
        String resultTest = reportService.shorterLink(TEST_STRING1);
        assertEquals(resultTest, resultTrue);
    }

    @Test
    public void shortLinkConfLink(){
        String resultTrue = "<a href='http://conf.aplana.com/dashboard.action'>CONF</a>";
        String resultTest = reportService.shorterLink(TEST_STRING2);
        assertEquals(resultTest, resultTrue);
    }

    @Test
    public void shortLinkJiraLinkWithoutTask(){
        String resultTrue = "<a href='http://jira.aplana.com/secure/ContactAdministrators!default.jspa'>JIRA</a>";
        String resultTest = reportService.shorterLink(TEST_STRING3);
        assertEquals(resultTest, resultTrue);
    }
}
