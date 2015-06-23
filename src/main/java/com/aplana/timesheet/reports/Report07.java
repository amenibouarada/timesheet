package com.aplana.timesheet.reports;

import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.enums.Report07PeriodTypeEnum;
import net.sf.jasperreports.engine.JRDataSource;

public class Report07 extends BaseReport{

    public static final String jrName = "report07";

    public static final String jrNameFile = "Отчёт №7. Относительная активность по проектам";

    @Override
    public JRDataSource prepareDataSource() {
        return new JasperReportDAO().getReport07Data(this);
    }

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    private Integer divisionOwner;
    private Integer divisionEmployee;
    private Integer periodType = Report07PeriodTypeEnum.PERIOD_TYPE_MONTH.getMonthsCount();

    public Integer getDivisionOwner() {
        return divisionOwner;
    }

    public void setDivisionOwner(Integer divisionOwner) {
        this.divisionOwner = divisionOwner;
    }

    public Integer getDivisionEmployee() {
        return divisionEmployee;
    }

    public void setDivisionEmployee(Integer divisionEmployee) {
        this.divisionEmployee = divisionEmployee;
    }

    public Integer getPeriodType() {
        return periodType;
    }

    public void setPeriodType(Integer periodType) {
        this.periodType = periodType;
    }
}
