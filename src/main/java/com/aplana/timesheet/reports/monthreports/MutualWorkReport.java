package com.aplana.timesheet.reports.monthreports;

import java.util.List;

/**
 * Created by AAfanasyev on 17.06.2015.
 */
public class MutualWorkReport extends BaseMonthReport{

    public static final String jrName = "/monthreports/mutualworkreport";

    public static final String jrNameFile = "MutualWorkReport";

    protected Integer divisionOwner;

    protected Integer divisionEmployee;

    protected Integer projectId;

    protected List<Integer> regions;

    public Integer getDivisionOwner() {
        return divisionOwner;
    }

    @Override
    public void setDivisionOwnerId (Integer divisionOwner) {
        this.divisionOwner = divisionOwner;
    }

    public Integer getDivisionEmployee() {
        return divisionEmployee;
    }

    public List<Integer> getRegions() {
        return regions;
    }

    public void setRegions(List<Integer> regions) {
        this.regions = regions;
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

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @Override
    public void checkParams() {

    }

}
