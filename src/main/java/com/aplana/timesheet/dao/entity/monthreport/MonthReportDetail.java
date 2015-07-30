package com.aplana.timesheet.dao.entity.monthreport;

import com.aplana.timesheet.dao.entity.Employee;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "month_report_detail")
public class MonthReportDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "month_report_detail_seq")
    @SequenceGenerator(name = "month_report_detail_seq", sequenceName = "month_report_detail_seq", allocationSize = 10)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "month_report_id", nullable = false)
    @ForeignKey(name = "month_report_detail_fk_report_id")
    private MonthReport monthReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ForeignKey(name = "month_report_detail_fk_emp_id")
    private Employee employee;

    @Column(name = "ts_worked")
    private Double tsWorked;

    @Column(name = "overtimes_paid_previous")
    private Double overtimesPaidPrevious;

    @Column(name = "ts_illness")
    private Double tsIllness;

    @Column(name = "ts_vacation_avail")
    private Double tsVacationAvail;

    @Column(name = "ts_over_remain")
    private Double tsOverRemain;

    public MonthReportDetail() {}

    public MonthReportDetail(MonthReport monthReport, Employee employee) {
        this.monthReport = monthReport;
        this.employee = employee;
    }

    public MonthReportDetail(MonthReport monthReport, Employee employee, Double tsWorked, Double overtimesPaidPrevious,
                             Double tsIllness, Double tsVacationAvail, Double tsOverRemain)
    {
        this.monthReport = monthReport;
        this.employee = employee;
        this.tsWorked = tsWorked;
        this.overtimesPaidPrevious = overtimesPaidPrevious;
        this.tsIllness = tsIllness;
        this.tsVacationAvail = tsVacationAvail;
        this.tsOverRemain = tsOverRemain;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MonthReport getMonthReport() {
        return monthReport;
    }

    public void setMonthReport(MonthReport monthReport) {
        this.monthReport = monthReport;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Double getTsWorked() {
        return tsWorked;
    }

    public void setTsWorked(Double tsWorked) {
        this.tsWorked = tsWorked;
    }

    public Double getOvertimesPaidPrevious() {
        return overtimesPaidPrevious;
    }

    public void setOvertimesPaidPrevious(Double overtimesPaidPrevious) {
        this.overtimesPaidPrevious = overtimesPaidPrevious;
    }

    public Double getTsIllness() {
        return tsIllness;
    }

    public void setTsIllness(Double tsIllness) {
        this.tsIllness = tsIllness;
    }

    public Double getTsVacationAvail() {
        return tsVacationAvail;
    }

    public void setTsVacationAvail(Double tsVacationAvail) {
        this.tsVacationAvail = tsVacationAvail;
    }

    public Double getTsOverRemain() {
        return tsOverRemain;
    }

    public void setTsOverRemain(Double tsOverRemain) {
        this.tsOverRemain = tsOverRemain;
    }

    @Override
    public String toString() {
        return "MonthReportDetail{" +
                "id=" + id +
                ", monthReport=" + monthReport +
                ", employee=" + employee +
                ", tsWorked=" + tsWorked +
                ", overtimesPaidPrevious=" + overtimesPaidPrevious +
                ", tsIllness=" + tsIllness +
                ", tsVacationAvail=" + tsVacationAvail +
                ", tsOverRemain=" + tsOverRemain +
                '}';
    }
}
