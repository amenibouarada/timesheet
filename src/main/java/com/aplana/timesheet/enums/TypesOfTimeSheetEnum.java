package com.aplana.timesheet.enums;

/**
 * @author <a href="mailto:Evgeniy.Yaroslavtsev@aplana.com">Evgeniy Yaroslavtsev</a>
 * @see <a href="http://conf.aplana.com/pages/viewpage.action?pageId=1874744">Аналитика</a>
 */
public enum TypesOfTimeSheetEnum {
    REPORT(0, "Отчет"),
    DRAFT(1, "Черновик");

    private int id;
    private String name;

    private TypesOfTimeSheetEnum(int id, String name) {
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
