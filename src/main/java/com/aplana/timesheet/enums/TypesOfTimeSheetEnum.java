package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Вид списания
 * @see com.aplana.timesheet.enums.DictionaryEnum#TIMESHEET_TYPE
 *
 * @author <a href="mailto:Evgeniy.Yaroslavtsev@aplana.com">Evgeniy Yaroslavtsev</a>
 * @see <a href="http://conf.aplana.com/pages/viewpage.action?pageId=1874744">Аналитика</a>
 */
public enum TypesOfTimeSheetEnum implements TSEnum {
    REPORT(131, "Отчет"),
    DRAFT(132, "Черновик отчета");

    private int id;
    private String name;

    private TypesOfTimeSheetEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TypesOfTimeSheetEnum getById( final Integer id ) {
        if (id == null) {return null;}
        return Iterables.tryFind(Arrays.asList(TypesOfTimeSheetEnum.values()), new Predicate<TypesOfTimeSheetEnum>() {
            @Override
            public boolean apply(@Nullable TypesOfTimeSheetEnum input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
