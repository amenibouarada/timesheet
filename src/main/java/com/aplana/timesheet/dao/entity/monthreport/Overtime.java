package com.aplana.timesheet.dao.entity.monthreport;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "overtime")
public class Overtime {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "overtime_seq")
    @SequenceGenerator(name = "overtime_seq", sequenceName = "overtime_seq", allocationSize = 10)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @ForeignKey(name = "fk_employee")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ForeignKey(name = "fk_project")
    private Project project;

    @Column(name = "overtime")
    private Double overtime;

    @Column(name = "fin_compensated_overtime")
    private Double fin_compensated_overtime;

    @Column(name = "premium")
    private Double premium;

    @Column(name = "comment")
    private String comment;

    @Column(name = "division_owner_id")
    private Integer division_owner_id;

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

    public Double getOvertime() {
        return overtime;
    }

    public void setOvertime(Double overtime) {
        this.overtime = overtime;
    }

    public Double getFin_compensated_overtime() {
        return fin_compensated_overtime;
    }

    public void setFin_compensated_overtime(Double fin_compensated_overtime) {
        this.fin_compensated_overtime = fin_compensated_overtime;
    }

    public Double getPremium() {
        return premium;
    }

    public void setPremium(Double premium) {
        this.premium = premium;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getDivision_owner_id() {
        return division_owner_id;
    }

    public void setDivision_owner_id(Integer division_owner_id) {
        this.division_owner_id = division_owner_id;
    }
}
