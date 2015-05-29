package com.aplana.timesheet.enums;

/**
 * User: vsergeev
 * Date: 25.01.13
 */
public enum QuickReportTypesEnum implements TSEnum{
    ILLNESS(6, "Больничный", "Справочник оснований болезни"),
    BUSINESS_TRIP(7, "Командировка", "Справочник типов командировок");

    private Integer id;
    /**
     * Отображать
     */
    private String name;
    /**
     * Название справочника
     */
    private String dictName;

    QuickReportTypesEnum(int id, String name, String dictName) {
        this.id = id;
        this.name = name;
        this.dictName = dictName;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDictName() {
        return dictName;
    }
}
