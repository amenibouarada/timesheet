package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Entity
@Table(name = "employee_assistant")
public class EmployeeAssistant {

    @Id
    @GeneratedValue(generator = "employee_assistant_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "employee_assistant_seq", sequenceName = "employee_assistant_seq", allocationSize = 10)
    @Column(columnDefinition = "integer", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_employee")
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @OneToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_assistant")
    @JoinColumn(name = "assistant_id", nullable = false)
    private Employee assistant;

    @Column(columnDefinition = "bool")
    private Boolean active;

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

    public Employee getAssistant() {
        return assistant;
    }

    public void setAssistant(Employee assistant) {
        this.assistant = assistant;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
