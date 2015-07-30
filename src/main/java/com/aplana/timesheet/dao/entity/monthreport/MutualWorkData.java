package com.aplana.timesheet.dao.entity.monthreport;

import javax.persistence.*;

/**
 * Created by AAfanasyev on 17.06.2015.
 */

@Entity
@Table(name = "mutual_work_data") // на самом деле это view
public class MutualWorkData {

    @Id
    @Column(name = "identifier")
    private String identifier;

    @Column(name = "mutual_work_id")
    private Integer mutualWorkId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "division_owner_id")
    private Integer division_owner_id;

    @Column(name = "division_owner_name")
    private String division_owner_name;

    @Column(name = "project_id")
    private Integer project_id;

    @Column(name = "project_name")
    private String project_name;

    @Column(name = "project_type_id")
    private Integer project_type_id;

    @Column(name = "project_type_name")
    private String project_type_name;

    @Column(name = "employee_id")
    private Integer employee_id;

    @Column(name = "employee_name")
    private String employee_name;

    @Column(name = "division_employee_id")
    private Integer division_employee_id;

    @Column(name = "division_employee_name")
    private String division_employee_name;

    @Column(name = "region_id")
    private Integer region_id;

    @Column(name = "region_name")
    private String region_name;

    @Column(name = "work_days")
    private Double work_days;

    @Column(name = "overtimes")
    private Double overtimes;

    @Column(name = "coefficient")
    private Double coefficient;

    @Column(name = "coefficient_calculated")
    private Double coefficient_calculated;

    @Column(name = "work_days_calculated")
    private Double work_days_calculated;

    @Column(name = "overtimes_calculated")
    private Double overtimes_calculated;

    @Column(name = "comment")
    private String comment;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getMutualWorkId() {
        return mutualWorkId;
    }

    public void setMutualWorkId(Integer mutualWorkId) {
        this.mutualWorkId = mutualWorkId;
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

    public Integer getDivision_owner_id() {
        return division_owner_id;
    }

    public void setDivision_owner_id(Integer division_owner_id) {
        this.division_owner_id = division_owner_id;
    }

    public String getDivision_owner_name() {
        return division_owner_name;
    }

    public void setDivision_owner_name(String division_owner_name) {
        this.division_owner_name = division_owner_name;
    }

    public Integer getProject_id() {
        return project_id;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public Integer getProject_type_id() {
        return project_type_id;
    }

    public void setProject_type_id(Integer project_type_id) {
        this.project_type_id = project_type_id;
    }

    public String getProject_type_name() {
        return project_type_name;
    }

    public void setProject_type_name(String project_type_name) {
        this.project_type_name = project_type_name;
    }

    public Integer getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(Integer employee_id) {
        this.employee_id = employee_id;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public Integer getDivision_employee_id() {
        return division_employee_id;
    }

    public void setDivision_employee_id(Integer division_employee_id) {
        this.division_employee_id = division_employee_id;
    }

    public String getDivision_employee_name() {
        return division_employee_name;
    }

    public void setDivision_employee_name(String division_employee_name) {
        this.division_employee_name = division_employee_name;
    }

    public Integer getRegion_id() {
        return region_id;
    }

    public void setRegion_id(Integer region_id) {
        this.region_id = region_id;
    }

    public String getRegion_name() {
        return region_name;
    }

    public void setRegion_name(String region_name) {
        this.region_name = region_name;
    }

    public Double getWork_days() {
        return work_days;
    }

    public void setWork_days(Double work_days) {
        this.work_days = work_days;
    }

    public Double getOvertimes() {
        return overtimes;
    }

    public void setOvertimes(Double overtimes) {
        this.overtimes = overtimes;
    }

    public Double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(Double coefficient) {
        this.coefficient = coefficient;
    }

    public Double getCoefficient_calculated() {
        return coefficient_calculated;
    }

    public void setCoefficient_calculated(Double coefficient_calculated) {
        this.coefficient_calculated = coefficient_calculated;
    }

    public Double getWork_days_calculated() {
        return work_days_calculated;
    }

    public void setWork_days_calculated(Double work_days_calculated) {
        this.work_days_calculated = work_days_calculated;
    }

    public Double getOvertimes_calculated() {
        return overtimes_calculated;
    }

    public void setOvertimes_calculated(Double overtimes_calculated) {
        this.overtimes_calculated = overtimes_calculated;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
