package com.aplana.timesheet.form;

import com.aplana.timesheet.dao.entity.Employee;

import java.util.Date;

/**
 * @author dsysterov
 * @version 1.0
 */
public class AdminProjectBillableForm {
    private Integer id;
    private Integer employee;
    private Boolean billable;
    private String startDate;
    private String endDate;
    private String comment;
    private String toDelete;

    public AdminProjectBillableForm() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmployee() {
        return employee;
    }

    public void setEmployee(Integer employee) {
        this.employee = employee;
    }

    public Boolean getBillable() {
        return billable;
    }

    public void setBillable(Boolean billable) {
        this.billable = billable;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getToDelete() {
        return toDelete;
    }

    public void setToDelete(String toDelete) {
        this.toDelete = toDelete;
    }
}
