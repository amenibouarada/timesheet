package com.aplana.timesheet.reports.monthreports;

import com.aplana.timesheet.dao.monthreport.MonthReportExcelDAO;
import net.sf.jasperreports.engine.JRDataSource;

import java.io.IOException;

/**
 * Created by AAfanasyev on 16.06.2015.
 */
public abstract class BaseMonthReport implements XLSJasperReport {

    protected MonthReportExcelDAO monthReportExcelDAO;

    @Override
    public void setReportDAO(MonthReportExcelDAO monthReportExcelDAO) {
        this.monthReportExcelDAO = monthReportExcelDAO;
    }

    protected Integer year;

    protected Integer month;

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

    @Override
    public JRDataSource prepareDataSource() throws IOException {
      return monthReportExcelDAO.getReportData(this);
    }
}
