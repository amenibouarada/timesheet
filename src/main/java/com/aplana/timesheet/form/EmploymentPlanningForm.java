package com.aplana.timesheet.form;

public class EmploymentPlanningForm {

    public static final String FORM = "employmentPlanningForm";

    private Integer yearBeg;
    private Integer monthBeg;
    private Integer yearEnd;
    private Integer monthEnd;
    private Integer projectId;
    private Integer selectDivisionId;

    public Integer getYearBeg() {
        return yearBeg;
    }

    public void setYearBeg(Integer yearBeg) {
        this.yearBeg = yearBeg;
    }

    public Integer getMonthBeg() {
        return monthBeg;
    }

    public void setMonthBeg(Integer monthBeg) {
        this.monthBeg = monthBeg;
    }

    public Integer getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(Integer yearEnd) {
        this.yearEnd = yearEnd;
    }

    public Integer getMonthEnd() {
        return monthEnd;
    }

    public void setMonthEnd(Integer monthEnd) {
        this.monthEnd = monthEnd;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public EmploymentPlanningForm() {
    }

    public Integer getSelectDivisionId() {
        return selectDivisionId;
    }

    public void setSelectDivisionId(Integer selectDivisionId) {
        this.selectDivisionId = selectDivisionId;
    }
}
