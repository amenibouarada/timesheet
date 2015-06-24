package com.aplana.timesheet.reports.monthreports;

/**
 * Created by AAfanasyev on 16.06.2015.
 */

public class OvertimeReport extends BaseMonthReport {

    public static final String jrName = "/monthreports/overtimereport";

    public static final String jrNameFile = "OvertimeReport";

    protected Integer divisionOwner;

    protected Integer divisionEmployee;

    public Integer getDivisionOwner() {
        return divisionOwner;
    }

    @Override
    public void setDivisionOwnerId(Integer divisionOwner) {
        this.divisionOwner = divisionOwner;
    }

    public Integer getDivisionEmployee() {
        return divisionEmployee;
    }

    public void setDivisionEmployee(Integer divisionEmployee) {
        this.divisionEmployee = divisionEmployee;
    }

    public String getJRName() {
        return jrName;
    }

    public String getJRNameFile() {
        return jrNameFile;
    }

    @Override
    public void checkParams() {

    }
}
