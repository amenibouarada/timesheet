package com.aplana.timesheet.reports;

public class Report05 extends BaseReport {

    public static final String jrName = "report05";

    public static final String jrNameFile="Отчет №5. Строки отчетов за день со всеми полями";

    private Boolean showInactiveEmployees = false;

    private String comment;

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

	private Integer employeeId;

	public Integer getEmployeeId() {
		return employeeId;
	}

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Boolean getShowInactiveEmployees() {
        return showInactiveEmployees;
    }

    public void setShowInactiveEmployees(Boolean showInactiveEmployees) {
        this.showInactiveEmployees = showInactiveEmployees;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
