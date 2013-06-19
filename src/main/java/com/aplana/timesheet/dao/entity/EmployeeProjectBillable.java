package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * User: bkinzyabulatov
 * Date: 17.06.13
 */
@Entity
@Table(
        name = "employee_project_billable",
        uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "project_id"})
)
public class EmployeeProjectBillable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_project_billable_seq")
    @SequenceGenerator(name = "employee_project_billable_seq", sequenceName = "employee_project_billable_seq", allocationSize = 10)
    @Column(name = "id", columnDefinition = "integer")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ForeignKey(name = "fk_employee")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ForeignKey(name = "fk_project")
    private Project project;

    @Column(nullable = false)
    private boolean billable;

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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean getBillable() {
        return billable;
    }

    public void setBillable(boolean billable) {
        this.billable = billable;
    }
}
