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
@Table(name = "business_trip")
public class BusinessTrip implements Cloneable, Periodical, Identifiable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "business_trip_seq")
    @SequenceGenerator(name = "business_trip_seq", sequenceName = "business_trip_seq", allocationSize = 10)
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
    @JoinColumn(name = "type_id")
    @ForeignKey(name = "fk_business_trip_type")
    private DictionaryItem type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ForeignKey(name = "fk_project")
    private Project project;

    @Column(name = "comment")
    private String comment;

    @Transient
    private Long workingDays = 0L;

    @Transient
    private Long calendarDays = 0L;

    @Override
    public BusinessTrip clone() throws CloneNotSupportedException {
        return (BusinessTrip)super.clone();
    }

    public boolean contain(Date date){
        return date.compareTo(beginDate) >= 0 && date.compareTo(endDate) <= 0;
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

    public DictionaryItem getType() {
        return type;
    }

    public void setType(DictionaryItem type) {
        this.type = type;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setWorkingDays(Long workingDays) {
        this.workingDays = workingDays;
    }

    public Long getWorkingDays() {
        return workingDays;
    }

    public void setCalendarDays(Long calendarDays) {
        this.calendarDays = calendarDays;
    }

    public Long getCalendarDays() {
        return calendarDays;
    }
}
