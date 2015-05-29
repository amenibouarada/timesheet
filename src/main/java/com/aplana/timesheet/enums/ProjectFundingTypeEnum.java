package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

/*
Типы источников финансирования проектов
 */
public enum ProjectFundingTypeEnum implements TSEnum {
    COMMERCIAL_PROJECT(128, "Из средств заказчика", "10"),
    INVESTMENT_PROJECT(129, "Инвестиции", "20"),
    INTERNAL_PROJECT(130, "Внутренние", "30");

    private int id;
    private String name;
    private String fromOQ;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFromOQ() {
        return fromOQ;
    }

    private ProjectFundingTypeEnum(int id, String name, String fromOQ) {
        this.id = id;
        this.name = name;
        this.fromOQ = fromOQ;
    }

    public static ProjectFundingTypeEnum getById(final int id) {
        return Iterables.find(Arrays.asList(ProjectFundingTypeEnum.values()), new Predicate<ProjectFundingTypeEnum>() {
            @Override
            public boolean apply(ProjectFundingTypeEnum input) {
                return input.getId() == id;
            }
        });
    }

    public static ProjectFundingTypeEnum getById(String fromOQ) {
        for(ProjectFundingTypeEnum item : ProjectFundingTypeEnum.values()){
            if (item.getFromOQ().equals(fromOQ)){
                return item;
            }
        }
        throw new IllegalArgumentException("Error in ProjectFundingTypeEnum.getById()");
    }
}
