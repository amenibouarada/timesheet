package com.aplana.timesheet.reports.monthreports;


/**
 * Created by AAfanasyev on 16.06.2015.
 */

public class MonthXLSReport extends BaseMonthReport {

    public static final String jrName = "monthreport_xls";

    public static final String jrNameFile = "MonthReport";

    protected Integer division;

    protected Integer manager;

    protected String regions;

    protected String roles;

    public Integer getDivision() {
        return division;
    }

    public void setDivision(Integer division) {
        this.division = division;
    }

    public Integer getManager() {
        return manager;
    }

    public void setManager(Integer manager) {
        this.manager = manager;
    }

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getJRName() {
        return jrName;
    }

    public String getJRNameFile() {
        return jrNameFile;
    }
}
