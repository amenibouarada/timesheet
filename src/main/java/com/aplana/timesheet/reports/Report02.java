package com.aplana.timesheet.reports;

public class Report02 extends BaseReport {

    public static final String jrName = "report02";

    public static final String jrNameFile = "Отчет №2. Сводный отчет затраченного времени по проекту";

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    private Integer projectId;

    private Integer emplDivisionId;

    private Integer employeeId;

    private Boolean showInactiveProjects = false;

    private Boolean showInactiveEmployees = false;

    private Boolean showNonBillable=true;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getEmplDivisionId() {
        return emplDivisionId;
    }

    public void setEmplDivisionId(Integer emplDivisionId) {
        this.emplDivisionId = emplDivisionId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Boolean getShowInactiveProjects() {
        return showInactiveProjects;
    }

    public void setShowInactiveProjects(Boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }

    public Boolean getShowNonBillable() {
        return showNonBillable;
    }

    public void setShowNonBillable(Boolean showNonBillable) {
        this.showNonBillable = showNonBillable;
    }

    public Boolean getShowInactiveEmployees() {
        return showInactiveEmployees;
    }

    public void setShowInactiveEmployees(Boolean showInactiveEmployees) {
        this.showInactiveEmployees = showInactiveEmployees;
    }
}
