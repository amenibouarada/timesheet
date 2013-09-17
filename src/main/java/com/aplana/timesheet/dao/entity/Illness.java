package com.aplana.timesheet.dao.entity;


import com.aplana.timesheet.dao.Identifiable;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;

/**
 * User: vsergeev
 * Date: 17.01.13
 */
@Entity
@Table(name = "illness")
public class Illness implements Cloneable, Periodical, Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "illness_seq")
    @SequenceGenerator(name = "illness_seq", sequenceName = "illness_seq", allocationSize = 10)
    @Column(nullable = false)
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

    @Column(name = "edition_date")
    private Date editionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @ForeignKey(name = "fk_author")
    private Employee author;

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

    public Date getEditionDate() {
        return editionDate;
    }

    public void setEditionDate(Date editionDate) {
        this.editionDate = editionDate;
    }

    public Employee getAuthor() {
        return author;
    }

    public void setAuthor(Employee author) {
        this.author = author;
    }
}
