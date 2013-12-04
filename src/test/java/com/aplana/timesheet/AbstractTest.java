package com.aplana.timesheet;

import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.enums.TSEnum;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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


    @Autowired
    private DictionaryItemDAO dictionaryItemDAO;
    /**
     * Упрощенная версия enumTest для DictionaryItem
     * @param clazz - класс enum'а
     * @param <E> - enum
     */
    public <E extends Enum<E> & TSEnum> void enumDictionaryTest(Class<E> clazz){
        enumTest(clazz, new TSEnumHandler() {
            @Override
            public void handle(TSEnum tsEnum) {
                DictionaryItem dictionaryItem = dictionaryItemDAO.find(tsEnum.getId());
                assertNotNull(dictionaryItem);
                assertEquals(tsEnum.getName(), dictionaryItem.getValue());
            }
        });
    }
}
