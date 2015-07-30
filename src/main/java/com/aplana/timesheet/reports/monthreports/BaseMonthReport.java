package com.aplana.timesheet.reports.monthreports;

import com.aplana.timesheet.dao.AbstractReportDAO;
import com.aplana.timesheet.dao.monthreport.MonthReportExcelDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.TSJasperReport;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * Created by AAfanasyev on 16.06.2015.
 */
public abstract class BaseMonthReport implements TSJasperReport{

    protected MonthReportExcelDAO monthReportExcelDAO;

    protected Integer year;

    protected Integer month;

    @Autowired
    protected AbstractReportDAO reportDAO;

    public void setReportDAO(AbstractReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    @Override
    public JRDataSource prepareDataSource() throws JReportBuildError {
        return reportDAO.getReportData(this);
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
