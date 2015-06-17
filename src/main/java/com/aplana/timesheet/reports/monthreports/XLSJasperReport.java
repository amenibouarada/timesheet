package com.aplana.timesheet.reports.monthreports;

import com.aplana.timesheet.dao.monthreport.MonthReportExcelDAO;
import net.sf.jasperreports.engine.JRDataSource;

import java.io.IOException;

public interface XLSJasperReport {

    JRDataSource prepareDataSource() throws IOException;

    void setReportDAO(MonthReportExcelDAO monthReportExcelDAO);

    String getJRName();

    String getJRNameFile();
}
