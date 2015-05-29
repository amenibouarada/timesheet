package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum TypesOfActivityEnum implements TSEnum {

    PROJECT(                12, "Проект",              1),
    PROJECT_PRESALE(        42, "Проектный пресейл",   2),
    PRESALE(                13, "Пресейл",             3),
    NON_PROJECT(            14, "Непроектная",         4),
    VACATION(               16, "Отпуск",              6),
    ILLNESS(                17, "Болезнь",             7),
    COMPENSATORY_HOLIDAY(   15, "Отгул",               9);

    private int id;
    private String name;
    private int order;

    private TypesOfActivityEnum(int id, String name, int order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Проверяет, входит ли тип активности в перечень тех, по которым нужно считать время работы
     *
     * @param id ид типа работы
     * @return
     */
    public static boolean isEfficientActivity(int id) {
        return isEfficientActivity(getById(id));
    }

    /**
     * Проверяет, входит ли тип активности в перечень тех, по которым нужно считать время работы
     *
     * @param typeOfActivity тип работы
     * @return
     */
    public static boolean isEfficientActivity(TypesOfActivityEnum typeOfActivity) {
        return typeOfActivity == NON_PROJECT
                || typeOfActivity == PRESALE
                || typeOfActivity == PROJECT
                || typeOfActivity == PROJECT_PRESALE;
    }

    public static boolean isProjectOrPresale(TypesOfActivityEnum typesOfActivityEnum) {
        return (typesOfActivityEnum == PROJECT || typesOfActivityEnum == PRESALE);
    }

    public static TypesOfActivityEnum getById(final Integer id) {
        if (id == null) {
            return null;
        }
        return Iterables.tryFind(Arrays.asList(TypesOfActivityEnum.values()), new Predicate<TypesOfActivityEnum>() {
            @Override
            public boolean apply(@Nullable TypesOfActivityEnum input) {
                return input.getId() == id;
            }
        }).orNull();
    }

    public static List<Integer> getProjectPresaleNonProjectActivityId() {
        List<Integer> result = new ArrayList<Integer>();
        result.add(PROJECT.getId());
        result.add(PRESALE.getId());
        result.add(NON_PROJECT.getId());
        return result;
    }

    public static boolean isNotCheckableForOvertime(TypesOfActivityEnum activityType) {
        return (
                activityType == VACATION || activityType == ILLNESS || activityType == COMPENSATORY_HOLIDAY
        );
    }

    /*
        Возвращает перечисление в виде строки через запятую id типов активности которые будут учитываться в отчетах
        42, 12, 13
     */
    public static String getOnlyWorkTypeString() {
        return String.valueOf(PROJECT.getId()) + ',' + PROJECT_PRESALE.getId() + ',' + PRESALE.getId();
    }
}
