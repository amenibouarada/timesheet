package com.aplana.timesheet.dao.entity.monthreport;

import javax.persistence.*;

/**
 * Created by AAfanasyev on 17.06.2015.
 */

@Entity
@Table(name = "outstaffing_data") // на самом деле это view
public class MutualWork {

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "division_owner_id")
    private Integer divisionOwnerId;

    @Column(name = "division_owner_name")
    private String divisionOwnerName;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_type_id")
    private Integer projectTypeId;

    @Column(name = "project_type_name")
    private String projectTypeName;

    @Id
    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "division_employee_id")
    private Integer divisionEmployeeId;

    @Column(name = "division_employee_name")
    private String divisionEmployeeName;

    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "work_days")
    private Double workDays;

    @Column(name = "overtimes")
    private Double overtimes;

    @Column(name = "coefficient")
    private Double coefficient;

    @Column(name = "work_days_calc")
    private Double workDaysCalc;

    @Column(name = "overtimes_calc")
    private Double overtimesCalc;

    @Column(name = "comment")
    private String comment;

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

    public String getDivisionOwnerName() {
        return divisionOwnerName;
    }

    public void setDivisionOwnerName(String divisionOwnerName) {
        this.divisionOwnerName = divisionOwnerName;
    }

    public Integer getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(Integer projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    public Double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(Double coefficient) {
        this.coefficient = coefficient;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getOvertimesCalc() {
        return overtimesCalc;
    }

    public void setOvertimesCalc(Double overtimesCalc) {
        this.overtimesCalc = overtimesCalc;
    }

    public Double getWorkDaysCalc() {
        return workDaysCalc;
    }

    public void setWorkDaysCalc(Double workDaysCalc) {
        this.workDaysCalc = workDaysCalc;
    }

    public Double getOvertimes() {
        return overtimes;
    }

    public void setOvertimes(Double overtimes) {
        this.overtimes = overtimes;
    }

    public Double getWorkDays() {
        return workDays;
    }

    public void setWorkDays(Double workDays) {
        this.workDays = workDays;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public String getDivisionEmployeeName() {
        return divisionEmployeeName;
    }

    public void setDivisionEmployeeName(String divisionEmployeeName) {
        this.divisionEmployeeName = divisionEmployeeName;
    }

    public Integer getDivisionEmployeeId() {
        return divisionEmployeeId;
    }

    public void setDivisionEmployeeId(Integer divisionEmployeeId) {
        this.divisionEmployeeId = divisionEmployeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getProjectTypeName() {
        return projectTypeName;
    }

    public void setProjectTypeName(String projectTypeName) {
        this.projectTypeName = projectTypeName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getDivisionOwnerId() {
        return divisionOwnerId;
    }

    public void setDivisionOwnerId(Integer divisionOwnerId) {
        this.divisionOwnerId = divisionOwnerId;
    }
}
