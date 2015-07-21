package com.aplana.timesheet.dao.monthreport;

import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.monthreport.MonthReport;
import com.aplana.timesheet.dao.entity.monthreport.MonthReportData;
import com.aplana.timesheet.dao.entity.monthreport.MonthReportDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Repository
public class MonthReportDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DivisionDAO divisionDAO;

    private static final Logger logger = LoggerFactory.getLogger(MonthReportDAO.class);

    public List<MonthReportData> getSingleMonthReportData(Employee employee, Integer year, Integer month){
        Query query = entityManager
                .createQuery("FROM MonthReportData WHERE year = :year AND month = :month AND employeeId = :employeeId")
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("employeeId", employee.getId());
        return query.getResultList();
    }

    /**
     * Используется для получения списка записей таблицы "Табель"
     * Последний параметр нужен для того, чтобы определить в каком виде необходимо получить результат
     *
     * @param division
     * @param manager
     * @param regions
     * @param roles
     * @param year
     * @param month
     * @param typeListObject - если true - то результат в виде List<Object[]>, иначе List<MonthReportData>
     * @return
     */
    public List<MonthReportData> getMonthReportData(
            Integer division,
            Integer manager,
            List<Integer> regions,
            List<Integer> roles,
            Integer year,
            Integer month,
            boolean typeListObject
    ) {
        String queryString = typeListObject ?
                "SELECT * FROM month_report_data WHERE year = :year AND month = :month " :
                "FROM MonthReportData WHERE year = :year AND month = :month ";
        boolean divisionSet = false;
        boolean managerSet = false;
        boolean regionSet = false;
        boolean rolesSet = false;
        if (division != null && division > 0) {
            queryString += " AND division_id = :division ";
            divisionSet = true;
        }
        if (manager != null && manager > 0) {
            queryString += " AND manager_id = :manager ";
            managerSet = true;
        }
        if (regions.size() > 0 && regions.get(0) > 0) {
            queryString += " AND region_id in :regions ";
            regionSet = true;
        }
        if (roles.size() > 0 && roles.get(0) > 0) {
            queryString += " AND job_id in :roles ";
            rolesSet = true;
        }
        Query query = typeListObject ?
                entityManager.createNativeQuery(queryString).setParameter("year", year).setParameter("month", month) :
                entityManager.createQuery(queryString).setParameter("year", year).setParameter("month", month);

        if (divisionSet){ query.setParameter("division", division); }
        if (managerSet) { query.setParameter("manager", manager);   }
        if (regionSet)  { query.setParameter("regions", regions);   }
        if (rolesSet)   { query.setParameter("roles", roles);       }
        logger.debug("getMonthReportData List<MonthReportData> result size = {}", query.getResultList().size());
        return query.getResultList();
    }

    private MonthReport findMonthReportYearMonth(Integer year, Integer month){
        Query query = entityManager.
                createQuery("FROM MonthReport WHERE year = :year AND month = :month")
                .setParameter("year", year)
                .setParameter("month", month);
        List result = query.getResultList();
        if (result.size() == 0){
            logger.debug("findMonthReportYearMonth List<MonthReport> result size = {}", query.getResultList().size());
            logger.info("findMonthReportYearMonth returned null");
            return null;
        }else{
            logger.debug("findMonthReportYearMonth List<MonthReport> result size = {}", query.getResultList().size());
            return (MonthReport)result.get(0);
        }
    }

    @Transactional
    public MonthReport findOrCreateMonthReport(Integer year, Integer month){
        MonthReport monthReport = findMonthReportYearMonth(year, month);
        if (monthReport == null){ // создадим новый
            MonthReport newMonthReport = new MonthReport(year, month);
            entityManager.persist(newMonthReport);
            logger.debug("findOrCreateMonthReport MonthReport id = {}", newMonthReport.getId());
            logger.info("findMonthReportYearMonth created newMonthReport");
            return newMonthReport;
        }else{
            logger.debug("findOrCreateMonthReport MonthReport id = {}", monthReport.getId());
            return monthReport;
        }
    }

    @Transactional
    public MonthReportDetail findOrCreateMonthReportDetail(MonthReport monthReport, Employee employee) {
        Query query = entityManager.
                createQuery("FROM MonthReportDetail WHERE monthReport = :monthReport AND employee = :employee")
                .setParameter("monthReport", monthReport)
                .setParameter("employee", employee);
        List result = query.getResultList();
        if (result.size() == 0){ // создадим новый
            MonthReportDetail newMonthReportDetail = new MonthReportDetail(monthReport, employee);
            entityManager.persist(newMonthReportDetail);
            logger.debug("findOrCreateMonthReportDetail List<MonthReportDetail> result size = {}", query.getResultList().size());
            logger.info("findOrCreateMonthReportDetail created newMonthReportDetail");
            return newMonthReportDetail;
        }else{
            logger.debug("findOrCreateMonthReportDetail List<MonthReportDetail> result size = {}", query.getResultList().size());
            return (MonthReportDetail)result.get(0);
        }
    }

    @Transactional
    public void save(MonthReportDetail monthReportDetail)  {
        entityManager.merge(monthReportDetail);
        entityManager.flush();
        logger.debug("Flushed monthReportDetail object id = {}", monthReportDetail.getId());
    }

    public Integer getMonthReportStatus(int year, int month){
        MonthReport monthReport = findMonthReportYearMonth(year, month);
        return monthReport == null ? null : monthReport.getStatus();
    }

    @Transactional
    public boolean setMonthReportStatus(Integer year, Integer month, Integer status) {
        MonthReport monthReport = findMonthReportYearMonth(year, month);
        monthReport.setStatus(status);
        monthReport.setClose_date(new Date());
        entityManager.merge(monthReport);
        entityManager.flush();
        logger.debug("Updated status monthReport object id = {}", monthReport.getId());
        return true;
    }

    public List<Object> getMonthReportStatusesForYear(Integer year) {
        Query query = entityManager.
                createQuery("SELECT month, status, close_date FROM MonthReport WHERE year = :year ORDER BY year")
                .setParameter("year", year);
        logger.debug("getMonthReportStatusesForYear List<Object> result size = {}", query.getResultList().size());
        return query.getResultList();
    }
}
