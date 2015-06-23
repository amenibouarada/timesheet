package com.aplana.timesheet.reports;

import com.aplana.timesheet.dao.AbstractReportDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import net.sf.jasperreports.engine.JRDataSource;

import java.util.List;

public interface TSJasperReport {

    JRDataSource prepareDataSource() throws JReportBuildError;

    void checkParams();

    String getJRName();

    public void setDivisionOwnerId(Integer divisionOwnerId);

    void setReportDAO(AbstractReportDAO abstractReportDAO);

    String getJRNameFile();
}
