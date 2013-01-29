package com.aplana.timesheet.reports;

import com.aplana.timesheet.util.DateTimeUtil;
import net.sf.jasperreports.engine.JRDataSource;

public class Report04 extends BaseReport {

    public static final String jrName = "report04";

    public static final String jrNameFile = "Отчет №4. Сотрудники, не отправившие отчет";

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    @Override
    public JRDataSource prepareDataSource() {
        return reportDAO.getReportData(this);
    }

    @Override
    public void checkParams() {
        this.beginDate = "".equals(this.beginDate) ? DateTimeUtil.MIN_DATE : this.beginDate;
        this.endDate = "".equals(this.endDate) ? DateTimeUtil.MAX_DATE : this.endDate;
        // Если конец интервала сегодня или в будущем, то переустанавливаем его на вчерашний день.
        // Таким образом исключаем из отчёта будущие неотправленные отчёты
        // Fix APLANATS-343
        String yesterday = DateTimeUtil.decreaseDay(DateTimeUtil.currentDay());
        if (DateTimeUtil.dayAfterDay(endDate, yesterday)) {
            endDate = yesterday;
        }
    }
}
