package com.aplana.timesheet.reports.monthreport;

import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.reports.TSJasperReport;
import net.sf.jasperreports.engine.JRDataSource;

import static com.aplana.timesheet.util.DateTimeUtil.*;

public class OvertimeReport implements TSJasperReport {

    public static final String jrName = "overtimereport";

    public static final String jrNameFile = "Overtime";

    protected JasperReportDAO reportDAO;

    @Override
    public void setReportDAO(JasperReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    @Override
    public void checkParams() {

    }

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    protected Integer year = getCurrentYear();

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getDivisionOwnerId() {
        return divisionOwnerId;
    }

    @Override
    public void setDivisionOwnerId(Integer divisionOwnerId) {
        this.divisionOwnerId = divisionOwnerId;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    protected Integer month = getCurrentMonth();

    private Integer divisionOwnerId;

    private Integer divisionId;

    @Override
    public JRDataSource prepareDataSource() {
        return reportDAO.getOvertimeReportData(this);
    }
}
