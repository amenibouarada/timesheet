package com.aplana.timesheet.dao.entity;


import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.lang.Integer;

/**
 * User: vsergeev
 * Date: 17.01.13
 */
@Entity
@Table(name = "illness")
public class Illness implements Cloneable, Periodical {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "illness_seq")
    @SequenceGenerator(name = "illness_seq", sequenceName = "illness_seq", allocationSize = 10)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @ForeignKey(name = "fk_employee")
    private Employee employee;

    @Column(name = "begin_date")
    private Date beginDate;

    @Column(name = "end_date")
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id")
    @ForeignKey(name = "fk_illness_reason")
    private DictionaryItem reason;

    @Column(name = "comment")
    private String comment;

    @Transient
    private Long calendarDays = 0L;

    @Transient
    private Long workingDays = 0L;

    @Transient
    private double workDaysOnIllnessWorked;

    @Override
    public Illness clone() throws CloneNotSupportedException {
        return (Illness)super.clone();
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

    public void setEmployee(Employee employeeId) {
        this.employee = employeeId;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public DictionaryItem getReason() {
        return reason;
    }

    public void setReason(DictionaryItem reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getCalendarDays() {
        return calendarDays;
    }

    public void setCalendarDays(Long calendarDays) {
        this.calendarDays = calendarDays;
    }

    public Long getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Long workingDays) {
        this.workingDays = workingDays;
    }

    public double getWorkDaysOnIllnessWorked() {
        return workDaysOnIllnessWorked;
    }

    public void setWorkDaysOnIllnessWorked(double workDaysOnIllnessWorked) {
        this.workDaysOnIllnessWorked = workDaysOnIllnessWorked;
    }
}
