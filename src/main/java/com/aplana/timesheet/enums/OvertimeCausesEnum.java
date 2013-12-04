package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum OvertimeCausesEnum implements TSEnum {
    A_LOT_OF_WORK(106, "У меня большой объем задач - не успеваю в рабочее время"),
    OWN_INITIATIVE(107, "Моя собственная инициатива"),
    MANAGER_REQUEST(108, "Просьба руководителя"),
    WORKING_OFF(109, "Отрабатываю время за предыдущие (последующие) дни"),
    OTHER(110, "Другое");

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private OvertimeCausesEnum(int id, String name) {

        this.id = id;
        this.name = name;
    }
}
