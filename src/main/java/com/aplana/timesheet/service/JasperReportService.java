package com.aplana.timesheet.service;

import argo.jdom.JsonNodeBuilders;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportExportStatus;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.BaseReport;
import com.aplana.timesheet.reports.TSJasperReport;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.JsonUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static com.aplana.timesheet.util.DateTimeUtil.SIMPLE_DATE_FORMAT;
import static com.aplana.timesheet.util.DateTimeUtil.SIMPLE_DATE_FORMAT_WITH_DASH;
import static com.aplana.timesheet.util.JsonUtil.aStringBuilder;

@Service
public class JasperReportService {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportService.class);

    private static final Properties propertiesFile = new Properties();

    public static final int REPORT_PRINTTYPE_HTML = 1;
    public static final int REPORT_PRINTTYPE_XLS = 2;
    public static final int REPORT_PRINTTYPE_PDF = 3;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ServletContext context;

    @Autowired
    private ReportExportStatusService reportExportStatusService;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    protected SecurityService securityService;

    @Autowired
    private TSPropertyProvider propertyProvider;

    private final HashMap<String, JasperReport> compiledReports = new HashMap<String, JasperReport>();

    private String toUTF8String(String s) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255 && !Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                byte[] b;
                b = Character.toString(c).getBytes("utf-8");
                for ( byte aB : b ) {
                    int k = aB;
                    if ( k < 0 ) k += 256;
                    sb.append( "%" ).append( Integer.toHexString( k ).toUpperCase() );
                }
            }
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public boolean makeReport(TSJasperReport report, int printtype, HttpServletResponse response, HttpServletRequest httpServletRequest) throws JReportBuildError {

        report.checkParams();

        String reportName = report.getJRName();
        Calendar calendar=new GregorianCalendar();
        String dateNorm=new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());

        String reportNameFile=report.getJRNameFile()+" "+dateNorm;
        try {
            JasperReport jasperReport = getReport(reportName + (printtype == REPORT_PRINTTYPE_XLS ? "_xls" : ""));

            Map params = new HashMap();
            params.put(JRParameter.IS_IGNORE_PAGINATION, (printtype != REPORT_PRINTTYPE_PDF));
            params.put("reportParams", report);

            JRDataSource jrDataSource = report.prepareDataSource();
            if (jrDataSource == null) {
            	return false;
            }
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, jrDataSource);

            String suffix = "";
            String contentType = "application/octet-stream";

            switch (printtype) {
                case REPORT_PRINTTYPE_HTML:
                    suffix = ".html";
                    contentType = "text/html; charset=UTF-8";
                    break;
                case REPORT_PRINTTYPE_PDF:
                    suffix = ".pdf";
                    contentType = "application/pdf";
                    break;
                case REPORT_PRINTTYPE_XLS:
                    suffix = ".xls";
                    contentType = "application/vnd.ms-excel";
                    break;
            }

            response.setContentType(contentType);
            if (printtype != REPORT_PRINTTYPE_HTML)
            {
                String agent = httpServletRequest.getHeader("user-agent");
                String contentDisposition = "attachment; filename=\"" + toUTF8String(reportNameFile+suffix) + "\"";
                if ( agent.contains( "Firefox" ) ) {
                    contentDisposition = "attachment; filename=\"" + MimeUtility.encodeText( reportNameFile + suffix, "UTF8", "B" ) + "\"";
                }

                response.setHeader("Content-Disposition",contentDisposition);
            }
            else {
                String agent = httpServletRequest.getHeader("user-agent");
                String contentDisposition = "filename=\"" + toUTF8String(reportNameFile+suffix) + "\"";
                if ( agent.contains( "Firefox" ) ) {
                    contentDisposition = "filename=\"" + MimeUtility.encodeText( reportNameFile + suffix, "UTF8", "B" ) + "\"";
                }

                response.setHeader("Content-Disposition",contentDisposition);
            }
            OutputStream outputStream = response.getOutputStream();

            switch (printtype) {
                case REPORT_PRINTTYPE_HTML: {

                    JRHtmlExporter jrHtmlExporter = new JRHtmlExporter();
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, jasperPrint);
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.CHARACTER_ENCODING, "UTF-8");
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
                    //  remove empty spaces
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, outputStream);
                    jrHtmlExporter.exportReport();

                    break;
                }

                case REPORT_PRINTTYPE_PDF: {

                    JRPdfExporter exporter = new JRPdfExporter();

                    exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

                    exporter.exportReport();

                    break;
                }
                case REPORT_PRINTTYPE_XLS: {

                    JRXlsExporter xlsExporter = new JRXlsExporter();
                    xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                    xlsExporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
                    xlsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                    xlsExporter.exportReport();

                    break;
                }
            }

            outputStream.close();

        } catch (JRException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        } catch (MalformedURLException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        } catch (IOException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        }
        return true;
    }

    @Transactional(readOnly = true)
    public String checkParamsReport04(final BaseReport report, Integer formHashCode)  throws JReportBuildError, ParseException {
        report.checkParams();
        // TODO предлагаю заменить на com.aplana.timesheet.util.DateTimeUtil.stringToDateForDB()
        Date beginDate = SIMPLE_DATE_FORMAT_WITH_DASH.parse(report.getBeginDate());
        Date endDate = SIMPLE_DATE_FORMAT_WITH_DASH.parse(report.getEndDate());

        String reportNameFile = String.format("%s (%s-%s)",
                report.getJRNameFile(),
                // TODO предлагаю заменить на com.aplana.timesheet.util.DateTimeUtil..stringToDateForView()
                SIMPLE_DATE_FORMAT.format(beginDate),
                SIMPLE_DATE_FORMAT.format(endDate));

        Employee employee = securityService.getSecurityPrincipal().getEmployee();

        ReportExportStatus reportExportStatusFinded = reportExportStatusService.find(employee, reportNameFile, formHashCode);

        if (reportExportStatusFinded != null) {
            if (!reportExportStatusFinded.getComplete()) {
                return jsonResponse(false, "Отчет с данными параметрами уже поставлен на выполнение");
            } else {
                return jsonResponse(false, "Отчет с данными параметрами готов, проверьте почту на наличие письма со ссылкой для скачивания");
            }
        }
        return jsonResponse(true);
    }

    /**
     * Метод асинхронно запускает задачу создания XLS отчета, затем посылает письмо со ссылкой для скачивания
     * @param report - Отчет №4. Сотрудники, не отправившие отчет
     * @return
     * @throws JReportBuildError
     */
    @Transactional
    public void makeReport04Async(final BaseReport report, final Integer formHashCode) throws JReportBuildError, ParseException {
        deleteUserReports();

        report.checkParams();

        final String reportName = report.getJRName();
        // TODO предлагаю заменить на com.aplana.timesheet.util.DateTimeUtil.stringToDateForDB()
        Date beginDate = SIMPLE_DATE_FORMAT_WITH_DASH.parse(report.getBeginDate());
        Date endDate = SIMPLE_DATE_FORMAT_WITH_DASH.parse(report.getEndDate());

        final String reportNameFile = String.format("%s (%s-%s)",
                report.getJRNameFile(),
                // TODO предлагаю заменить на com.aplana.timesheet.util.DateTimeUtil..stringToDateForView()
                SIMPLE_DATE_FORMAT.format(beginDate),
                SIMPLE_DATE_FORMAT.format(endDate));
        final String outputFile = propertyProvider.getPathReports() + reportNameFile + ".xls";

        final ReportExportStatus reportExportStatus = new ReportExportStatus();
        reportExportStatus.setComplete(false);
        reportExportStatus.setEmployee(securityService.getSecurityPrincipal().getEmployee());
        reportExportStatus.setReportName(reportNameFile);
        reportExportStatus.setHashForm(formHashCode);
        reportExportStatus.setPath(outputFile);
        reportExportStatusService.save(reportExportStatus);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JasperReport jasperReport = getReport(reportName + "_xls");
                    Map params = new HashMap() {{
                        put(JRParameter.IS_IGNORE_PAGINATION, true);
                        put("reportParams", report);
                    }};

                    JRDataSource jrDataSource = report.prepareDataSource();
                    if (jrDataSource != null) {
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, jrDataSource);

                        JRXlsExporter xlsExporter = new JRXlsExporter();
                        xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                        xlsExporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
                        xlsExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, context.getRealPath(outputFile));
                        xlsExporter.exportReport();
                    }
                    Employee employee = securityService.getSecurityPrincipal().getEmployee();
                    ReportExportStatus reportExportStatusFinded = reportExportStatusService.find(employee, reportNameFile, formHashCode);
                    sendMailService.performNotificationOnExportReportComplete(reportExportStatusFinded);
                } catch (JRException e) {
                    logger.error("Error forming report " + reportName);
                    logger.error(e.getMessage());
                } catch (MalformedURLException e) {
                    logger.error("Error forming report " + reportName);
                    logger.error(e.getMessage());
                }
            }
        }).run();
    }

    /**
     * Удаление предыдущих отчетов пользователя
     */
    public void deleteUserReports() {
        Employee employee = securityService.getSecurityPrincipal().getEmployee();
        List<ReportExportStatus> reportExportStatuses = reportExportStatusService.findUserIncompleteReports(employee);
        for (ReportExportStatus reportExportStatus : reportExportStatuses) {
            if (new File(context.getRealPath(reportExportStatus.getPath())).delete()) {
                reportExportStatusService.delete(reportExportStatus);
            }
        }
    }

    public String jsonResponse(Boolean result, String errorMessage) {
        return JsonUtil.format(anObjectBuilder().
                withField("result", aStringBuilder(result)).
                withField("errorMessage", JsonNodeBuilders.aStringBuilder(errorMessage)));
    }

    public String jsonResponse(Boolean result) {
        return JsonUtil.format(anObjectBuilder().
                withField("result", aStringBuilder(result)).
                withField("errorMessage", JsonNodeBuilders.aStringBuilder("")));
    }

    public JasperReport getReport(String reportName) throws MalformedURLException, JRException {
        JasperReport report;
        if (!compiledReports.containsKey(reportName)) {
            logger.info("Compiling jasper project " + reportName);

            report = JasperCompileManager.compileReport(context.getRealPath("/resources/reports/" + reportName + ".jrxml"));

            // кэширование бинарника отчета
            //compiledReports.put(reportName, report);
        } else {
            logger.info("Loading jasper project " + reportName + " from repository");
            report = compiledReports.get(reportName);
        }

        return report;
    }

    @Transactional
    public void downloadReport04(final Integer idReport, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ReportExportStatus reportExportStatus = reportExportStatusService.find(idReport);
        String suffix = ".xls";
        response.setContentType( "application/vnd.ms-excel");

        String agent = request.getHeader("user-agent");
        String contentDisposition = "attachment; filename=\"" + toUTF8String(reportExportStatus.getReportName() + suffix) + "\"";
        if (agent.contains("Firefox")) {
            contentDisposition = "attachment; filename=\"" + MimeUtility.encodeText(reportExportStatus.getReportName() + suffix, "UTF8", "B") + "\"";
        }

        response.setHeader("Content-Disposition", contentDisposition);
        InputStream is = new FileInputStream(context.getRealPath(reportExportStatus.getPath()));
        IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
        is.close();

        reportExportStatus.setComplete(true);
        reportExportStatusService.save(reportExportStatus);
    }
}
