package com.aplana.timesheet.dao.entity.monthreport;

import javax.persistence.*;

@Entity
@Table(name = "month_report_data") // на самом деле это view
public class MonthReportData {

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Id
    @Column(name="employee_id")
    private Integer employeeId;

    @Column(name="employee_name")
    private String employeeName;

    @Column(name="region_id")
    private Integer regionId;

    @Column(name="region_name")
    private String regionName;

    @Column(name="division_id")
    private Integer divisionId;

    @Column(name="division_name")
    private String divisionName;

    @Column(name="job_id")
    private Integer jobId;

    @Column(name="manager_id")
    private Integer managerId;

    @Column(name="ts_worked_calculated")
    private Double ts_worked_calculated;

    @Column(name="ts_worked")
    private Double ts_worked;

    @Column(name="overtimes_paid_current")
    private Double overtimes_paid_current;

    @Column(name="overtimes_paid_previous")
    private Double overtimes_paid_previous;

    @Column(name="calc_vacation_with")
    private Double calc_vacation_with;

    @Column(name="calc_vacation_without")
    private Double calc_vacation_without;

    @Column(name="calc_vacation_hol_paid")
    private Double calc_vacation_hol_paid;

    @Column(name="ts_illness_calculated")
    private Double ts_illness_calculated;

    @Column(name="ts_illness")
    private Double ts_illness;

    @Column(name="ts_all_paid")
    private Double ts_all_paid;

    @Column(name="ts_all_over_accounted")
    private Double ts_all_over_accounted;

    @Column(name="ts_vacation_avail")
    private Double ts_vacation_avail;

    @Column(name="ts_over_remain_calculated")
    private Double ts_over_remain_calculated;

    @Column(name="ts_over_remain")
    private Double ts_over_remain;

    @Column(name="calc_worked_vac")
    private Double calc_worked_vac;

    @Column(name="ts_vacation")
    private Double ts_vacation;

    @Column(name="overtimes_acc_current")
    private Double overtimes_acc_current;

    @Column(name="calc_illness")
    private Double calc_illness;

    @Column(name="calc_illness_with")
    private Double calc_illness_with;

    @Column(name="calc_illness_without")
    private Double calc_illness_without;

    @Column(name="calc_worked_ill")
    private Double calc_worked_ill;

    @Column(name="calc_worked_plan")
    private Double calc_worked_plan;

    @Column(name="calc_worked_fact")
    private Double calc_worked_fact;

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

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Double getTs_worked_calculated() {
        return ts_worked_calculated;
    }

    public void setTs_worked_calculated(Double ts_worked_calculated) {
        this.ts_worked_calculated = ts_worked_calculated;
    }

    public Double getTs_worked() {
        return ts_worked;
    }

    public void setTs_worked(Double ts_worked) {
        this.ts_worked = ts_worked;
    }

    public Double getOvertimes_paid_current() {
        return overtimes_paid_current;
    }

    public void setOvertimes_paid_current(Double overtimes_paid_current) {
        this.overtimes_paid_current = overtimes_paid_current;
    }

    public Double getOvertimes_paid_previous() {
        return overtimes_paid_previous;
    }

    public void setOvertimes_paid_previous(Double overtimes_paid_previous) {
        this.overtimes_paid_previous = overtimes_paid_previous;
    }

    public Double getCalc_vacation_with() {
        return calc_vacation_with;
    }

    public void setCalc_vacation_with(Double calc_vacation_with) {
        this.calc_vacation_with = calc_vacation_with;
    }

    public Double getCalc_vacation_without() {
        return calc_vacation_without;
    }

    public void setCalc_vacation_without(Double calc_vacation_without) {
        this.calc_vacation_without = calc_vacation_without;
    }

    public Double getCalc_vacation_hol_paid() {
        return calc_vacation_hol_paid;
    }

    public void setCalc_vacation_hol_paid(Double calc_vacation_hol_paid) {
        this.calc_vacation_hol_paid = calc_vacation_hol_paid;
    }

    public Double getTs_illness_calculated() {
        return ts_illness_calculated;
    }

    public void setTs_illness_calculated(Double ts_illness_calculated) {
        this.ts_illness_calculated = ts_illness_calculated;
    }

    public Double getTs_illness() {
        return ts_illness;
    }

    public void setTs_illness(Double ts_illness) {
        this.ts_illness = ts_illness;
    }

    public Double getTs_all_paid() {
        return ts_all_paid;
    }

    public void setTs_all_paid(Double ts_all_paid) {
        this.ts_all_paid = ts_all_paid;
    }

    public Double getTs_all_over_accounted() {
        return ts_all_over_accounted;
    }

    public void setTs_all_over_accounted(Double ts_all_over_accounted) {
        this.ts_all_over_accounted = ts_all_over_accounted;
    }

    public Double getTs_vacation_avail() {
        return ts_vacation_avail;
    }

    public void setTs_vacation_avail(Double ts_vacation_avail) {
        this.ts_vacation_avail = ts_vacation_avail;
    }

    public Double getTs_over_remain_calculated() {
        return ts_over_remain_calculated;
    }

    public void setTs_over_remain_calculated(Double ts_over_remain_calculated) {
        this.ts_over_remain_calculated = ts_over_remain_calculated;
    }

    public Double getTs_over_remain() {
        return ts_over_remain;
    }

    public void setTs_over_remain(Double ts_over_remain) {
        this.ts_over_remain = ts_over_remain;
    }

    public Double getCalc_worked_vac() {
        return calc_worked_vac;
    }

    public void setCalc_worked_vac(Double calc_worked_vac) {
        this.calc_worked_vac = calc_worked_vac;
    }

    public Double getTs_vacation() {
        return ts_vacation;
    }

    public void setTs_vacation(Double ts_vacation) {
        this.ts_vacation = ts_vacation;
    }

    public Double getOvertimes_acc_current() {
        return overtimes_acc_current;
    }

    public void setOvertimes_acc_current(Double overtimes_acc_current) {
        this.overtimes_acc_current = overtimes_acc_current;
    }

    public Double getCalc_illness() {
        return calc_illness;
    }

    public void setCalc_illness(Double calc_illness) {
        this.calc_illness = calc_illness;
    }

    public Double getCalc_illness_with() {
        return calc_illness_with;
    }

    public void setCalc_illness_with(Double calc_illness_with) {
        this.calc_illness_with = calc_illness_with;
    }

    public Double getCalc_illness_without() {
        return calc_illness_without;
    }

    public void setCalc_illness_without(Double calc_illness_without) {
        this.calc_illness_without = calc_illness_without;
    }

    public Double getCalc_worked_ill() {
        return calc_worked_ill;
    }

    public void setCalc_worked_ill(Double calc_worked_ill) {
        this.calc_worked_ill = calc_worked_ill;
    }

    public Double getCalc_worked_plan() {
        return calc_worked_plan;
    }

    public void setCalc_worked_plan(Double calc_worked_plan) {
        this.calc_worked_plan = calc_worked_plan;
    }

    public Double getCalc_worked_fact() {
        return calc_worked_fact;
    }

    public void setCalc_worked_fact(Double calc_worked_fact) {
        this.calc_worked_fact = calc_worked_fact;
    }
}
