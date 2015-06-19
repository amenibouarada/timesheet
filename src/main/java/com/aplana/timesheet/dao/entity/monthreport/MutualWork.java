package com.aplana.timesheet.dao.entity.monthreport;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "mutual_work")
public class MutualWork {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mutual_work_seq")
    @SequenceGenerator(name = "mutual_work_seq", sequenceName = "mutual_work_seq", allocationSize = 10)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ForeignKey(name = "mutual_work_fk_employee")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    @ForeignKey(name = "mutual_work_fk_project")
    private Project project;

    @Column(name = "work_days")
    private Double workDays;

    @Column(name = "overtimes")
    private Double overtimes;

    @Column(name = "coefficient")
    private Double coefficient;

    @Column(name = "comment")
    private String comment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Double getWorkDays() {
        return workDays;
    }

    public void setWorkDays(Double workDays) {
        this.workDays = workDays;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
