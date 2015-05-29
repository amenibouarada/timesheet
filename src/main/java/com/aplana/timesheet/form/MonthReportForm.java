package com.aplana.timesheet.form;

import java.util.List;

public class MonthReportForm extends MonthlyRequestParameters{
    public static final String FORM = "monthReportForm";

    private Integer divisionId;
    private Integer year;
    private Integer month;
    private List<Integer> regions;
    private List<Integer> projectRoles;
    private Integer manager;

    public static String getForm() {
        return FORM;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public List<Integer> getRegions() {
        return regions;
    }

    public void setRegions(List<Integer> regions) {
        this.regions = regions;
    }

    public List<Integer> getProjectRoles() {
        return projectRoles;
    }

    public void setProjectRoles(List<Integer> projectRoles) {
        this.projectRoles = projectRoles;
    }

    public Integer getManager() {
        return manager;
    }

    public void setManager(Integer manager) {
        this.manager = manager;
    }
}
