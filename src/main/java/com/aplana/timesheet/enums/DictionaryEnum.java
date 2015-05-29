package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum DictionaryEnum implements TSEnum {
    CATEGORY_OF_ACTIVITY(1, "Категория активности", "справочник активностей"),
    TYPES_OF_ACTIVITY(2, "Тип активности", "справочник типов активностей"),
    WORKPLACE(5, "Место работы", "Место работы"),
    VACATION_TYPE(9, "Тип отпуска", "Типы отпусков"),
    UNDERTIME_CAUSE(10, "Причины недоработок", "Причины недоработок"),
    OVERTIME_CAUSE(11, "Причины переработок", "Причины переработок"),
    TYPES_OF_COMPENSATION(13, "Типы компенсации", "Типы компенсации"),
    WORK_ON_HOLIDAY_CAUSE(14, "Причины работы в выходной день", "Причины работы в выходной день"),
    VACATION_STATUS(8,"Статусы отпусков", "Статусы согласования"),
    EFFORT_IN_NEXTDAY(15, "Оценка моего объема работ на следующий рабочий день", "Справочник оценок объёма работ на следующий день"),
    PROJECT_FUNDING_TYPE(16, "Тип финансирования проекта", "Тип финансирования проекта"),
    TIMESHEET_TYPE(17, "Вид списания", "Вид списания");

    private int id;
    /**
     * Отображать
     */
    private String name;
    /**
     * Название справочника
     */
    private String dictName;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private DictionaryEnum(int id, String name, String dictName) {
        this.id = id;
        this.name = name;
        this.dictName = dictName;
    }

    public String getDictName() {
        return dictName;
    }
}
