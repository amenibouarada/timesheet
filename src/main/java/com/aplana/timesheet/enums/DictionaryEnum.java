package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum DictionaryEnum implements TSEnum {
    CATEGORY_OF_ACTIVITY(1, "Категория активности"),
    TYPES_OF_ACTIVITY(2, "Тип активности"),
    WORKPLACE(5, "Место работы"),
    VACATION_TYPE(9, "Тип отпуска"),
    UNDERTIME_CAUSE(10, "Причины недоработок"),
    OVERTIME_CAUSE(11, "Причины переработок"),
    TYPES_OF_COMPENSATION(13, "Типы компенсации"),
    WORK_ON_HOLIDAY_CAUSE(14, "Причины работы в выходной день"),
    VACATION_STATUS(8,"Статусы отпусков"),
    EFFORT_IN_NEXTDAY(15, "Оценка моего объема работ на следующий рабочий день"),
    PROJECT_FUNDING_TYPE(16, "Тип финансирования проекта");

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private DictionaryEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
