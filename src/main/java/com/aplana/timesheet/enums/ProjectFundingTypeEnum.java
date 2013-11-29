package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

/*
Типы источников финансирования проектов
 */
public enum ProjectFundingTypeEnum implements TSEnum {
    COMMERCIAL_PROJECT(128, "Из средств заказчика"),
    INVESTMENT_PROJECT(129, "Инвестиции"),
    INTERNAL_PROJECT(130, "Внутренние");

    private int id;
    private String name;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    private ProjectFundingTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static ProjectFundingTypeEnum getById(final int id) {
        return Iterables.find(Arrays.asList(ProjectFundingTypeEnum.values()), new Predicate<ProjectFundingTypeEnum>() {
            @Override
            public boolean apply(ProjectFundingTypeEnum input) {
                return input.getId() == id;
            }
        });
    }
}
