package com.aplana.timesheet;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Transactional(propagation = Propagation.REQUIRED)
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@ContextConfiguration(locations = {"classpath:/WEB-INF/spring/testApplicationContext.xml"})
public abstract class AbstractTest {
}
