package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.monthreport.MonthReportExcelDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.monthreports.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.StringUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by AAfanasyev on 16.06.2015.
 */

@Service
public class MonthReportExcelService {

    @Autowired
    MonthReportExcelDAO monthReportExcelDAO;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ServletContext context;

    private final HashMap<String, JasperReport> compiledReports = new HashMap<String, JasperReport>();

    public String[] makeMonthReport(Integer division, Integer manager, String regions, String roles, Integer year, Integer month)
            throws JReportBuildError, IOException
    {
        MonthXLSReport monthXLSReport = new MonthXLSReport();
        monthXLSReport.setDivision(division);
        monthXLSReport.setManager(manager);
        monthXLSReport.setRoles(StringUtil.stringToList(roles));
        monthXLSReport.setRegions(StringUtil.stringToList(regions));
        monthXLSReport.setYear(year);
        monthXLSReport.setMonth(month);
        monthXLSReport.setReportDAO(monthReportExcelDAO);
        String[] headers = makeMonthExcelReport(monthXLSReport);

        return headers;
    }

    public String[] makeOvertimeReport(Integer year, Integer month, Integer divisionOwner, Integer divisionEmployee)
            throws JReportBuildError, IOException
    {
        OvertimeReport overtimeReport = new OvertimeReport();
        overtimeReport.setYear(year);
        overtimeReport.setMonth(month);
        overtimeReport.setDivisionOwner(divisionOwner);
        overtimeReport.setDivisionEmployee(divisionEmployee);
        overtimeReport.setReportDAO(monthReportExcelDAO);
        String[] headers = makeMonthExcelReport(overtimeReport);

        return headers;
    }

    public String[] makeMutualWorkReport(
            Integer year,
            Integer month,
            String regions,
            Integer divisionOwner,
            Integer divisionEmployee,
            Integer projectId) throws JReportBuildError, IOException
    {
        MutualWorkReport mutualWorkReport = new MutualWorkReport();
        mutualWorkReport.setYear(year);
        mutualWorkReport.setMonth(month);
        mutualWorkReport.setRegions(StringUtil.stringToList(regions));
        mutualWorkReport.setDivisionOwner(divisionOwner);
        mutualWorkReport.setDivisionEmployee(divisionEmployee);
        mutualWorkReport.setProjectId(projectId);

        mutualWorkReport.setReportDAO(monthReportExcelDAO);
        String[] headers = makeMonthExcelReport(mutualWorkReport);

        return headers;
    }

    @Transactional(readOnly = true)
    private String[] makeMonthExcelReport(BaseMonthReport report) throws JReportBuildError, IOException {

        String[] headers;
        String reportName = report.getJRName();
        Calendar calendar = new GregorianCalendar();
        String dateNorm = DateTimeUtil.formatDateIntoViewFormat(calendar.getTime());

        String reportNameFile = report.getJRNameFile() + "_" + dateNorm + ".xls";
        final String outputFile = context.getRealPath("/resources/reports/generatedReports/" + reportNameFile);

        // ToDo решить в какой момент удалять файлы
        for (File reportFile : new File(context.getRealPath("/resources/reports/generatedReports/")).listFiles()) {
            if (reportFile.isFile()) {
                reportFile.delete();
            }
        }

        try {
            JasperReport jasperReport = getReport(reportName);

            Map params = new HashMap();
            params.put(JRParameter.IS_IGNORE_PAGINATION, true);
            params.put("reportParams", report);

            JRDataSource jrDataSource = report.prepareDataSource();
            if (jrDataSource == null) {
                return null;
            }
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, jrDataSource);

            JRXlsExporter xlsExporter = new JRXlsExporter();
            xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            xlsExporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
            xlsExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFile);
            xlsExporter.exportReport();

            String url = "/resources/reports/generatedReports/" + reportNameFile;
            String contentType = "application/vnd.ms-excel";
            String contentDisposition = "attachment; filename=\"" + StringUtil.toUTF8String(reportNameFile) + "\"";

            headers = new String[]{contentType, contentDisposition, url};

        } catch (JReportBuildError e){
            throw e;
        } catch (JRException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        } catch (MalformedURLException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        } catch (IOException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        }
        return headers;
    }

    public JasperReport getReport(String reportName) throws MalformedURLException, JRException {
        JasperReport report;
        if (!compiledReports.containsKey(reportName)) {
            report = JasperCompileManager.compileReport(
                    context.getRealPath("/resources/reports/monthreports/" + reportName + ".jrxml"));
        } else {
            report = compiledReports.get(reportName);
        }
        return report;
    }
}
