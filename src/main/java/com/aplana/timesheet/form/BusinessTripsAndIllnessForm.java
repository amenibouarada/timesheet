package com.aplana.timesheet.form;

import com.aplana.timesheet.enums.QuickReportTypesEnum;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 18.01.13
 */
public class BusinessTripsAndIllnessForm {

    public static final int ALL_VALUE = -1;

    private Integer divisionId;
    private Integer employeeId;
    private Date dateFrom;
    private Date dateTo;
    private List<QuickReportTypesEnum> reportTypes = Arrays.asList(QuickReportTypesEnum.values());
    private Integer reportType = QuickReportTypesEnum.ILLNESS.getId();
    private List<Integer> regions;
    private Integer managerId;

    public List<QuickReportTypesEnum> getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(List<QuickReportTypesEnum> reportTypes) {
        this.reportTypes = reportTypes;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public List<Integer> getRegions() {
        return regions;
    }

    public void setRegions(List<Integer> regions) {
        this.regions = regions;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }
}
