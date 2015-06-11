package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.monthreport.MonthReportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonthReportService {

    @Autowired
    private MonthReportDAO monthReportDAO;

    public void getMonthReportData(){
        monthReportDAO.getMonthReportData();
    }

}
