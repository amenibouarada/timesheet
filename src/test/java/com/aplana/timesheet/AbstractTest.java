package com.aplana.timesheet;

import com.aplana.timesheet.enums.TSEnum;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Transactional(propagation = Propagation.REQUIRED)
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public abstract class AbstractTest {

    public interface TSEnumHandler{
        void handle(TSEnum tsEnum);
    }

    /**
     * Бежит по всем элементами enum'а и дергает метод TSEnumHandler.handle
     * @param clazz - класс enum'а
     * @param checkHandler - какой метод дергаем
     * @param <E> - enum
     */
    public <E extends Enum<E> & TSEnum> void enumTest(Class<E> clazz, TSEnumHandler checkHandler){
        for(E en : EnumSet.allOf(clazz)){
            checkHandler.handle(en);
        }
    }
}
