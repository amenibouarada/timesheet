package com.aplana.timesheet.enums;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public enum EmployeePlanType implements TSEnum {

    WORK_FOR_OTHER_DIVISIONS(111, "Работа на другие центры"),
    NON_PROJECT(112, "Непроектная"),
    ILLNESS(113, "Болезнь"),
    VACATION(114, "Отпуск");

    private final int id;
    private final String name;

    private EmployeePlanType(int id, String name) {
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
}
