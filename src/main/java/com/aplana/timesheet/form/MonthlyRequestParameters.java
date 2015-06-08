package com.aplana.timesheet.form;

import java.util.List;

public class MonthlyRequestParameters {

    public static final int ALL_VALUE = 0;

    public static final String DIVISION_ID      = "divisionId";
    public static final String YEAR             = "year";
    public static final String MONTH            = "month";
    public static final String MANAGER          = "manager";
    public static final String REGIONS          = "regions";
    public static final String PROJECT_ROLES    = "projectRoles";


    private Integer divisionId;
    private Integer year;
    private Integer month;
    private Integer manager;
    private List<Integer> regions;
    private List<Integer> projectRoles;


    public static int getAllValue() {
        return ALL_VALUE;
    }

    public static String getDivisionId() {
        return DIVISION_ID;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public static String getYear() {
        return YEAR;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public static String getMonth() {
        return MONTH;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public static String getManager() {
        return MANAGER;
    }

    public void setManager(Integer manager) {
        this.manager = manager;
    }

    public static String getRegions() {
        return REGIONS;
    }

    public void setRegions(List<Integer> regions) {
        this.regions = regions;
    }

    public static String getProjectRoles() {
        return PROJECT_ROLES;
    }

    public void setProjectRoles(List<Integer> projectRoles) {
        this.projectRoles = projectRoles;
    }
}
