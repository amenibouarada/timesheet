package com.aplana.timesheet.reports.monthreports;

import com.aplana.timesheet.dao.monthreport.MonthReportExcelDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import net.sf.jasperreports.engine.JRDataSource;

import java.io.IOException;

/**
 * Created by AAfanasyev on 16.06.2015.
 */
public abstract class BaseMonthReport{

    protected MonthReportExcelDAO monthReportExcelDAO;

    protected Integer year;

    protected Integer month;

    public abstract String getJRName();

    public abstract String getJRNameFile();

    public JRDataSource prepareDataSource() throws JReportBuildError {
      return monthReportExcelDAO.getReportData(this);
    }

    public void setReportDAO(MonthReportExcelDAO monthReportExcelDAO) {
        this.monthReportExcelDAO = monthReportExcelDAO;
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

}
