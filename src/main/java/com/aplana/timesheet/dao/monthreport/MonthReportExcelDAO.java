package com.aplana.timesheet.dao.monthreport;

import com.aplana.timesheet.reports.monthreports.BaseMonthReport;
import com.aplana.timesheet.reports.monthreports.MonthXLSReport;
import com.aplana.timesheet.reports.monthreports.OvertimeReport;
import com.aplana.timesheet.util.HibernateQueryResultDataSource;
import com.aplana.timesheet.exception.JReportBuildError;
import org.springframework.beans.factory.annotation.Autowired;
import com.aplana.timesheet.util.StringUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.util.List;

/**
 * Created by AAfanasyev on 16.06.2015.
 */

@Repository
public class MonthReportExcelDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MonthReportDAO monthReportDAO;

    public HibernateQueryResultDataSource getReportData(BaseMonthReport baseMonthReport) throws JReportBuildError {

        if (baseMonthReport instanceof OvertimeReport) {
            return getOvertimeReportData((OvertimeReport) baseMonthReport);
        } else if (baseMonthReport instanceof MonthXLSReport) {
            return getMonthReportData((MonthXLSReport) baseMonthReport);
        }
        throw new IllegalArgumentException();
    }

    private HibernateQueryResultDataSource getOvertimeReportData(OvertimeReport report) throws JReportBuildError {

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

        return checkResultSetAndGetData(query.getResultList(), new String[] {
                "employee", "division", "region", "project", "overtime", "premium", "comment"});
    }

    private HibernateQueryResultDataSource getMonthReportData(MonthXLSReport report) throws JReportBuildError {
        List resultList = monthReportDAO.getMonthReportData(
                report.getDivision(),
                report.getManager(),
                report.getRegions(),
                report.getRoles(),
                report.getYear(),
                report.getMonth(),
                true);

        return checkResultSetAndGetData(resultList, new String[] {
                    "year", "month", "employee_id", "employee_name",
                    "region_id", "region_name", "division_id", "division_name",
                    "job_id", "manager_id", "ts_worked_calculated", "ts_worked",
                    "ts_vacation", "ts_illness", "ts_illness_calculated", "ts_all_paid",
                    "ts_over_val_fin_comp_calc", "ts_over_val_fin_comp", "ts_over_accounted", "ts_premium",
                    "ts_all_over_accounted", "ts_over_done", "ts_over_not_done", "ts_over_remain",
                    "ts_vacation_avail", "calc_worked_plan", "calc_worked_fact", "calc_vacation",
                    "calc_vacation_with", "calc_vacation_without", "calc_vacation_hol_paid", "calc_illness",
                    "calc_illness_with", "calc_illness_without", "calc_over", "calc_over_hol",
                    "calc_over_hol_paid", "calc_over_work", "calc_worked_ill", "calc_worked_vac"});
    }

    private HibernateQueryResultDataSource checkResultSetAndGetData(List resultList, String[] fields) throws JReportBuildError {
        if (resultList == null) {
            throw new JReportBuildError("Во время выполнения запроса к БД произошла ошибка.");
        }
        if (resultList.isEmpty()){
            throw new JReportBuildError("Нет данных для отображения.");
        }
        return new HibernateQueryResultDataSource(resultList, fields);
    }
}
