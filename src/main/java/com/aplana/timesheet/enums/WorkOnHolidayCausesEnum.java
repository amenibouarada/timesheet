package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum WorkOnHolidayCausesEnum implements TSEnum {
    A_LOT_OF_WORK(118, "У меня большой объем задач, не успеваю справляться в рабочее время"),
    OWN_INITIATIVE(119, "Моя собственная инициатива"),
    MANAGER_REQUEST(120, "Просьба руководителя"),
    WORKING_OFF(121, "Отрабатываю время за предыдущие (последующие) дни"),
    OTHER(122, "Другое");

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private WorkOnHolidayCausesEnum(int id, String name) {

        this.id = id;
        this.name = name;
    }
}
