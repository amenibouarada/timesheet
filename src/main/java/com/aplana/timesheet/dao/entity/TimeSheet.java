package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "time_sheet", uniqueConstraints = @UniqueConstraint(columnNames = {"caldate", "emp_id"}))
public class TimeSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "time_sheet_seq")
    @SequenceGenerator(name = "time_sheet_seq", sequenceName = "time_sheet_seq", allocationSize = 10)
    @Column(columnDefinition = "decimal(10,0) not null")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caldate", columnDefinition = "date not null")
    @ForeignKey(name = "FK_CALENDAR")
    private Calendar calDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    @ForeignKey(name = "FK_EMPLOYEE")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state")
    @ForeignKey(name = "FK_TIME_SHEET_STATE")
    private DictionaryItem state;

    @Column(columnDefinition = "text null")
    private String plan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effort_id")
    @ForeignKey(name = "FK_TIME_SHEET_EFFORT")
    private DictionaryItem effortInNextDay;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ts_type_id")
    @ForeignKey(name = "FK_TIME_SHEET_TS_TYPE")
    private DictionaryItem type;

    @OneToMany(mappedBy = "timeSheet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("id asc")
    private Set<TimeSheetDetail> timeSheetDetails;

    @OneToOne(mappedBy = "timeSheet", fetch = FetchType.LAZY)
    private OvertimeCause overtimeCause;

    @Column(name = "creation_date")
    private Date creationDate;

    public DictionaryItem getType() {
        return type;
    }

    public void setType(DictionaryItem type) {
        this.type = type;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Calendar getCalDate() {
        return calDate;
    }

    public void setCalDate(Calendar calDate) {
        this.calDate = calDate;
    }

    public Set<TimeSheetDetail> getTimeSheetDetails() {
        return timeSheetDetails;
    }

    public void setTimeSheetDetails(Set<TimeSheetDetail> timeSheetDetails) {
        this.timeSheetDetails = timeSheetDetails;
    }

    public Integer getId() {
        return id;
    }


    public Employee getEmployee() {
        return employee;
    }

    public DictionaryItem getState() {
        return state;
    }

    public String getPlan() {
        return plan;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setEmployee(Employee emp) {
        this.employee = emp;
    }

    public void setState(DictionaryItem state) {
        this.state = state;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public OvertimeCause getOvertimeCause() {
        return overtimeCause;
    }

    public void setOvertimeCause(OvertimeCause overtimeCause) {
        this.overtimeCause = overtimeCause;
    }

    public DictionaryItem getEffortInNextDay() {
        return effortInNextDay;
    }

    public void setEffortInNextDay(DictionaryItem effortInNextDay) {
        this.effortInNextDay = effortInNextDay;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(" id=").append(id)
                .append(" calDate [").append(calDate).append("]")
                .append(" employee [").append(employee).append("]")
                .append(" state [").append(state).append("]")
                .append(" plan=").append(plan)
                .append(" typeReport=").append(type)
                .toString();
    }
}