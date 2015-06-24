package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.monthreport.MonthReportExcelDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.monthreports.*;
import com.aplana.timesheet.service.JasperReportService;
import com.aplana.timesheet.util.StringUtil;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by AAfanasyev on 16.06.2015.
 */

@Service
public class MonthReportExcelService {

    @Autowired
    MonthReportExcelDAO monthReportExcelDAO;

    @Autowired
    JasperReportService jasperReportService;

    public void makeMonthReport(
            Integer division,
            Integer manager,
            String regions,
            String roles,
            Integer year,
            Integer month,
            HttpServletRequest request,
            HttpServletResponse response)
            throws JReportBuildError, IOException
    {
        MonthXLSReport monthXLSReport = new MonthXLSReport();
        monthXLSReport.setDivisionOwnerId(division);
        monthXLSReport.setManager(manager);
        monthXLSReport.setRoles(StringUtil.stringToList(roles));
        monthXLSReport.setRegions(StringUtil.stringToList(regions));
        monthXLSReport.setYear(year);
        monthXLSReport.setMonth(month);
        monthXLSReport.setReportDAO(monthReportExcelDAO);

        jasperReportService.makeReport(monthXLSReport, jasperReportService.REPORT_PRINTTYPE_XLS, response, request);
    }

    public void  makeOvertimeReport(
            Integer year,
            Integer month,
            Integer divisionOwner,
            Integer divisionEmployee,
            HttpServletRequest request,
            HttpServletResponse response) throws JReportBuildError, IOException
    {
        OvertimeReport overtimeReport = new OvertimeReport();
        overtimeReport.setYear(year);
        overtimeReport.setMonth(month);
        overtimeReport.setDivisionOwnerId(divisionOwner);
        overtimeReport.setDivisionEmployee(divisionEmployee);
        overtimeReport.setReportDAO(monthReportExcelDAO);

        jasperReportService.makeReport(overtimeReport, jasperReportService.REPORT_PRINTTYPE_XLS, response, request);
    }

    public void makeMutualWorkReport(
            Integer year,
            Integer month,
            String regions,
            Integer divisionOwner,
            Integer divisionEmployee,
            Integer projectId,
            HttpServletResponse response,
            HttpServletRequest request) throws JReportBuildError, IOException
    {
        MutualWorkReport mutualWorkReport = new MutualWorkReport();
        mutualWorkReport.setYear(year);
        mutualWorkReport.setMonth(month);
        mutualWorkReport.setRegions(StringUtil.stringToList(regions));
        mutualWorkReport.setDivisionOwnerId(divisionOwner);
        mutualWorkReport.setDivisionEmployee(divisionEmployee);
        mutualWorkReport.setProjectId(projectId);
        mutualWorkReport.setReportDAO(monthReportExcelDAO);

        jasperReportService.makeReport(mutualWorkReport, jasperReportService.REPORT_PRINTTYPE_XLS, response, request);
    }
}
