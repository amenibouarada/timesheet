package com.aplana.timesheet.dao.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by abayanov
 * Date: 29.08.14
 * В таблице хранятся количество
 */
@Entity
@Table(name = "vacation_days")
public class VacationDays {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vacation_days_seq")
    @SequenceGenerator(name = "vacation_days_seq", sequenceName = "vacation_days_seq", allocationSize = 10)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true, nullable = true)
    private Employee employee;

    @Column(name = "actualization_date")
    private Date actualizationDate;

    @Column(name = "count_days")
    private Integer countDays;

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

    public Date getActualizationDate() {
        return actualizationDate;
    }

    public void setActualizationDate(Date actualizationDate) {
        this.actualizationDate = actualizationDate;
    }

    public Integer getCountDays() {
        return countDays;
    }

    public void setCountDays(Integer countDays) {
        this.countDays = countDays;
    }
}
