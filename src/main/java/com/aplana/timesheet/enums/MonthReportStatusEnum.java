package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

public enum MonthReportStatusEnum implements TSEnum{

    NOT_CREATED(0, "Не создан"),
    OPEN(1, "Открыт"),
    CLOSED(2, "Закрыт"),
    IN_WORK(3, "В работе");

    private Integer id;
    private String name;

    MonthReportStatusEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public static MonthReportStatusEnum getById(final int id) {
        return Iterables.find(Arrays.asList(MonthReportStatusEnum.values()), new Predicate<MonthReportStatusEnum>() {
            public boolean apply(MonthReportStatusEnum input) {
                return input.getId() == id;
            }
        });
    }

}
