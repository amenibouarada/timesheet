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

        fieldsMap.put(OvertimeReport.class, new String[]{  "employee", "division", "region", "project", "overtime", "premium", "comment"});

        fieldsMap.put(MonthXLSReport.class, new String[]{  "year", "month", "employee_id", "employee_name",
                                                           "region_id", "region_name", "division_id", "division_name",
                                                           "job_id", "manager_id", "ts_worked_calculated", "ts_worked",
                                                           "ts_vacation", "ts_illness", "ts_illness_calculated", "ts_all_paid",
                                                           "ts_over_val_fin_comp_calc", "ts_over_val_fin_comp", "ts_over_accounted", "ts_premium",
                                                           "ts_all_over_accounted", "ts_over_done", "ts_over_not_done", "ts_over_remain",
                                                           "ts_vacation_avail", "calc_worked_plan", "calc_worked_fact", "calc_vacation",
                                                           "calc_vacation_with", "calc_vacation_without", "calc_vacation_hol_paid", "calc_illness",
                                                           "calc_illness_with", "calc_illness_without", "calc_over", "calc_over_hol",
                                                           "calc_over_hol_paid", "calc_over_work", "calc_worked_ill", "calc_worked_vac"});

        fieldsMap.put(MutualWorkReport.class, new String[]{"identifier", "year", "month", "division_owner_id", "division_owner_name",
                                                           "project_id", "project_name", "project_type_id", "project_type_name",
                                                           "employee_id", "employee_name", "division_employee_id", "division_employee_name",
                                                           "region_id", "region_name", "work_days", "overtimes",
                                                           "coefficient", "work_days_calc", "overtimes_calc", "comment"});
    }

    private static final Logger logger = LoggerFactory.getLogger(MonthReportExcelDAO.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MonthReportDAO monthReportDAO;

    @Autowired
    private MutualWorkDAO mutualWorkDAO;

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

    private List getOvertimeReportData(OvertimeReport report) throws JReportBuildError {

        String queryString = "SELECT " +
                "employee.name AS employee," +
                "division.name AS division," +
                "region.name AS region," +
                "project.name AS project," +
                "overtime.overtime AS overtime," +
                "overtime.premium AS premium," +
                "overtime.comment AS comment " +
                "FROM overtime " +
                "INNER JOIN employee ON (overtime.employee_id = employee.id) " +
                "INNER JOIN region ON (employee.region = region.id) " +
                "INNER JOIN division ON (employee.division = division.id) " +
                "INNER JOIN project ON (overtime.project_id = project.id) " +
                "WHERE year = :year and month = :month";

        boolean ownerDivSet = false;
        boolean employeeDivSet = false;

        if (report.getDivisionOwner() != null && report.getDivisionOwner() > 0) {
            queryString += " AND (overtime.project_id IS NULL OR division.id = :divisionOwner) ";
            ownerDivSet = true;
        }
        if (report.getDivisionEmployee() != null && report.getDivisionEmployee() > 0) {
            queryString += " AND division.id = :divisionEmployee ";
            employeeDivSet = true;
        }
        queryString += " ORDER BY employee.name";

        Query query = entityManager.createNativeQuery(queryString);

        query.setParameter("year", report.getYear());
        query.setParameter("month", report.getMonth());

        if (ownerDivSet) {
            query.setParameter("divisionOwner", report.getDivisionOwner());
        }
        if (employeeDivSet) {
            query.setParameter("divisionEmployee", report.getDivisionEmployee());
        }
        logger.debug("getOvertimeReportData result size = {}", query.getResultList().size());
        return query.getResultList();
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
