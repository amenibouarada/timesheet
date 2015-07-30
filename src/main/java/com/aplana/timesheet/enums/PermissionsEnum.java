package com.aplana.timesheet.enums;

/**
 * @author iziyangirov
 * @version 1.0
 */
public enum PermissionsEnum implements TSEnum {

    PLAIN_PERMISSION(1, "Обычные права сотрудника"),
    REPORTS_PERMISSION(2, "Формирование отчетов"),
    ADMIN_PERMISSION(3, "Администрирование системы"),
    VIEW_ILLNESS_BUSINESS_TRIP(4, "Просмотр болезней и командировок всех сотрудников"),
    CHANGE_ILLNESS_BUSINESS_TRIP(5, "Изменение болезней и командировок всех сотрудников"),
    VIEW_PLANS(6, "Просмотр планов"),
    EDIT_PLANS(7, "Изменение планов"),
    MONTH_REPORT_PERMISSION(8, "Открытие и закрытие табеля");

    private int id;
    private String name;

    private PermissionsEnum(int id, String name) {
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

}
