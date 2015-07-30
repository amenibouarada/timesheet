package com.aplana.timesheet.dao.entity.monthreport;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.enums.MonthReportStatusEnum;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;

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

    @Column(name = "status")
    private Integer status;

    @Column(name = "open_date")
    private Date open_date;

    @Column(name = "close_date")
    private Date close_date;

    public MonthReport() { }

    public MonthReport(Integer year, Integer month) {
        this.year = year;
        this.month = month;
        this.setStatus(MonthReportStatusEnum.OPEN.getId());
        this.setOpen_date(new Date());
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getOpen_date() {
        return open_date;
    }

    public void setOpen_date(Date open_date) {
        this.open_date = open_date;
    }

    public Date getClose_date() {
        return close_date;
    }

    public void setClose_date(Date close_date) {
        this.close_date = close_date;
    }
}
