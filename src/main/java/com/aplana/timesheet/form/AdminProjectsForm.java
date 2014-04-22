package com.aplana.timesheet.form;

/**
 * @author dsysterov
 * @version 1.0
 */
public class AdminProjectsForm {

    private Integer divisionId;
    private Integer managerId;
    private Boolean showActiveOnly;

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer employeeId) {
        this.managerId = employeeId;
    }

    public Boolean getShowActiveOnly() {
        return showActiveOnly;
    }

    public void setShowActiveOnly(Boolean showActiveOnly) {
        this.showActiveOnly = showActiveOnly;
    }
}
