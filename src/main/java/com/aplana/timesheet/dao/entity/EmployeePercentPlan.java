package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.enums.TypesOfActivityEnum;

import javax.persistence.Entity;
import javax.persistence.NamedNativeQuery;

/**
 * @author pmakarov
 * @see <a href="">Аналитика</a>
 *      creation date: 28.11.13
 */

public class EmployeePercentPlan {
    private Integer employeeId;
    private String employeeName;
    private Integer year;
    private Integer month;
    private Double percent;

    public EmployeePercentPlan(Integer employeeId, String employeeName, Integer year, Integer month, Double percent) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.year = year;
        this.month = month;
        this.percent = percent;
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

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }
}
