package com.aplana.timesheet.dao.entity.monthreport;

import com.aplana.timesheet.dao.entity.Division;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "month_report")
public class MonthReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "month_report_seq")
    @SequenceGenerator(name = "month_report_seq", sequenceName = "month_report_seq", allocationSize = 10)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    @ForeignKey(name = "month_report_fk_division")
    private Division division;

    @Column(name = "status")
    private Integer status;

    public MonthReport() { }

    public MonthReport(Integer year, Integer month, Division division) {
        this.year = year;
        this.month = month;
        this.division = division;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

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

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }
}