package com.aplana.timesheet.util;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.enums.RegionsEnum;
import com.aplana.timesheet.enums.TSEnum;

import java.util.NoSuchElementException;

/**
 * User: vsergeev
 * Date: 22.01.13
 */
public class EnumsUtils {

    public static <T extends TSEnum> T getEnumById(DictionaryItem dictionaryItem, Class<T> aClass) {
        return getEnumById(dictionaryItem.getId(), aClass);
    }

    public static <T extends TSEnum> T getEnumById(int id, Class<T> aClass) {
        T result = tryFindById(id, aClass);

        if (result == null) {
            throw new NoSuchElementException();
        }

        return result;
    }

    public static <T extends TSEnum>T tryFindById( int id, Class<T> aClass) {
        for (T enumValue : aClass.getEnumConstants()) {
            if (enumValue.getId() == id) {
                return enumValue;
            }
        }
        if (aClass == RegionsEnum.class) {
            return (T)RegionsEnum.OTHERS;
        }
        return null;
    }


}
