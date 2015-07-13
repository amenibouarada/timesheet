package com.aplana.timesheet.dao.monthreport;

import com.aplana.timesheet.dao.AbstractReportDAO;
import com.aplana.timesheet.reports.TSJasperReport;
import com.aplana.timesheet.reports.monthreports.MonthXLSReport;
import com.aplana.timesheet.reports.monthreports.OvertimeReport;
import com.aplana.timesheet.reports.monthreports.MutualWorkReport;
import com.aplana.timesheet.exception.JReportBuildError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by AAfanasyev on 16.06.2015.
 */

@Repository
public class MonthReportExcelDAO extends AbstractReportDAO {

    static {

        fieldsMap.put(OvertimeReport.class, new String[]{  "identifier", "overtime_id", "year", "month", "employee_id", "employee_name",
                                                           "region_id", "region_name", "division_employee_id", "division_employee_name",
                                                           "project_id", "project_name", "project_type_id", "division_owner_name",
                                                           "division_owner_id", "project_type_name", "overtime", "overtime_calculated",
                                                           "premium", "total_accounted_overtime", "fin_compensated_overtime",
                                                           "fin_compensated_overtime_calculated", "comment"});

        fieldsMap.put(MonthXLSReport.class, new String[]{  "year", "month", "employee_id", "employee_name",
                                                           "region_id", "region_name", "division_id", "division_name",
                                                           "job_id", "manager_id", "ts_worked_calculated", "ts_worked",
                                                           "overtimes_paid_current", "overtimes_paid_previous", "calc_vacation_with",
                                                           "calc_vacation_without", "calc_vacation_hol_paid", "ts_illness_calculated",
                                                           "ts_illness", "ts_all_paid", "ts_all_over_accounted", "ts_vacation_avail", "ts_over_remain_calculated",
                                                           "ts_over_remain", "calc_worked_vac", "ts_vacation", "overtimes_acc_current",
                                                           "calc_illness", "calc_illness_with", "calc_illness_without", "calc_worked_ill",
                                                           "calc_worked_plan", "calc_worked_fact"});

        fieldsMap.put(MutualWorkReport.class, new String[]{"identifier", "mutual_work_id", "year", "month", "division_owner_id", "division_owner_name",
                                                           "project_id", "project_name", "project_type_id", "project_type_name",
                                                           "employee_id", "employee_name", "division_employee_id", "division_employee_name",
                                                           "region_id", "region_name", "work_days", "work_days_calculated", "overtimes", "overtimes_calculated",
                                                           "coefficient", "coefficient_calculated", "comment"});
    }

    private static final Logger logger = LoggerFactory.getLogger(MonthReportExcelDAO.class);

    @Autowired
    private MonthReportDAO monthReportDAO;

    @Autowired
    private MutualWorkDAO mutualWorkDAO;

    @Autowired
    private OvertimeDAO overtimeDAO;

    public List getResultList(TSJasperReport baseMonthReport) throws JReportBuildError {
        if (baseMonthReport instanceof OvertimeReport) {
            OvertimeReport report = (OvertimeReport) baseMonthReport;
            return getOvertimeReportData(report);
        } else if (baseMonthReport instanceof MonthXLSReport) {
            MonthXLSReport report = (MonthXLSReport) baseMonthReport;
            return getMonthReportData(report);
        } else if (baseMonthReport instanceof MutualWorkReport) {
            MutualWorkReport report = (MutualWorkReport) baseMonthReport;
            return getMutualWorkReportData(report);
        }
        throw new IllegalArgumentException();
    }

    private List getOvertimeReportData (OvertimeReport report) throws JReportBuildError {
        List resultList = overtimeDAO.getOvertimes(
                report.getYear(),
                report.getMonth(),
                report.getDivisionOwner(),
                report.getDivisionEmployee(),
                true);
        logger.debug("getMonthReportData result size = {}", resultList.size());
        return resultList;
    }

    private List getMonthReportData(MonthXLSReport report) throws JReportBuildError {
        List resultList = monthReportDAO.getMonthReportData(
                report.getDivision(),
                report.getManager(),
                report.getRegions(),
                report.getRoles(),
                report.getYear(),
                report.getMonth(),
                true);
        logger.debug("getMonthReportData result size = {}", resultList.size());
        return resultList;
    }

    private List getMutualWorkReportData(MutualWorkReport report) throws JReportBuildError {
        List resultList = mutualWorkDAO.getMutualWorkData(
                report.getYear(),
                report.getMonth(),
                report.getRegions(),
                report.getDivisionOwner(),
                report.getDivisionEmployee(),
                report.getProjectId(),
                true);
        logger.debug("getMutualWorkReportData result size = {}", resultList.size());
        return resultList;
    }
}
