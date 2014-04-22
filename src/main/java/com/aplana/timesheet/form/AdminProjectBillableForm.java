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
    private Date startDate;
    private Date endDate;
    private String comment;

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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
