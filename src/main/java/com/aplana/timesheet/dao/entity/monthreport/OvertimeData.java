package com.aplana.timesheet.dao.entity.monthreport;

import javax.persistence.*;

@Entity
@Table(name = "overtime_data") //Это view
public class OvertimeData {

    @Id
    @Column(name = "identifier")
    private String identifier;

    @Column(name = "overtime_id")
    private Integer overtime_id;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "employee_id")
    private Integer employee_id;

    @Column(name = "employee_name")
    private String employee_name;

    @Column(name = "region_id")
    private Integer region_id;

    @Column(name = "region_name")
    private String region_name;

    @Column(name = "division_employee_id")
    private Integer division_employee_id;

    @Column(name = "division_employee_name")
    private String division_employee_name;

    @Column(name = "project_id")
    private Integer project_id;

    @Column(name = "project_name")
    private String project_name;

    @Column(name = "project_type_id")
    private Integer project_type_id;

    @Column(name = "project_type_name")
    private String project_type_name;

    @Column(name = "division_owner_id")
    private Integer division_owner_id;

    @Column(name = "division_owner_name")
    private String division_owner_name;

    @Column(name = "overtime")
    private Double overtime;

    @Column(name = "overtime_calculated")
    private Double overtime_calculated;

    @Column(name = "premium")
    private Double premium;

    @Column(name = "total_accounted_overtime")
    private Double total_accounted_overtime;

    @Column(name = "fin_compensated_overtime")
    private Double fin_compensated_overtime;

    @Column(name = "fin_compensated_overtime_calculated")
    private Double fin_compensated_overtime_calculated;

    @Column(name = "comment")
    private String comment;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getOvertime_id() {
        return overtime_id;
    }

    public void setOvertime_id(Integer overtime_id) {
        this.overtime_id = overtime_id;
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

    public Double getOvertime() {
        return overtime;
    }

    public void setOvertime(Double overtime) {
        this.overtime = overtime;
    }

    public Double getOvertime_calculated() {
        return overtime_calculated;
    }

    public void setOvertime_calculated(Double overtime_calculated) {
        this.overtime_calculated = overtime_calculated;
    }

    public Double getPremium() {
        return premium;
    }

    public void setPremium(Double premium) {
        this.premium = premium;
    }

    public Double getTotal_accounted_overtime() {
        return total_accounted_overtime;
    }

    public void setTotal_accounted_overtime(Double total_accounted_overtime) {
        this.total_accounted_overtime = total_accounted_overtime;
    }

    public Double getFin_compensated_overtime() {
        return fin_compensated_overtime;
    }

    public void setFin_compensated_overtime(Double fin_compensated_overtime) {
        this.fin_compensated_overtime = fin_compensated_overtime;
    }

    public Double getFin_compensated_overtime_calculated() {
        return fin_compensated_overtime_calculated;
    }

    public void setFin_compensated_overtime_calculated(Double fin_compensated_overtime_calculated) {
        this.fin_compensated_overtime_calculated = fin_compensated_overtime_calculated;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
