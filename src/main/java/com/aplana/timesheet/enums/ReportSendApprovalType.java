package com.aplana.timesheet.enums;

/**
 * Created by abayanov
 * Date: 14.07.14
 */
public enum ReportSendApprovalType {

    DELETE(0, "удаление"),
    SET_DRAFT(1, "перевод в черновики");

    Integer id;
    String name;

    ReportSendApprovalType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    static public ReportSendApprovalType findById(Integer id) {
        for (ReportSendApprovalType reportSendApprovalType : values()) {
            if (reportSendApprovalType.getId() == id) {
                return reportSendApprovalType;
            }
        }
        return null;
    }
}
