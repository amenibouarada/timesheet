package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by abayanov
 * Date: 29.07.14
 */
@Entity
@Table(name = "report_export_status")
public class ReportExportStatus {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "integer not null")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emp_id", nullable = false)
    @ForeignKey(name = "FK_EMPLOYEE")
    private Employee employee;

    @Column(nullable = false)
    private Boolean complete;

    @Column(nullable = false)
    private String reportName;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Integer hashForm;

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Integer getHashForm() {
        return hashForm;
    }

    public void setHashForm(Integer hashForm) {
        this.hashForm = hashForm;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
