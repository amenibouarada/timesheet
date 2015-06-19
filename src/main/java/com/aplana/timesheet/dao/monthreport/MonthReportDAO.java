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
        return query.getResultList();
    }

    @Transactional
    public MonthReport findOrCreateMonthReport(Integer year, Integer month, Integer division){
        Query query = entityManager.
                createQuery("FROM MonthReport WHERE year = :year AND month = :month AND division.id = :division")
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("division", division);
        List result = query.getResultList();
        if (result.size() == 0){ // создадим новый
            MonthReport newMonthReport = new MonthReport(year, month, divisionDAO.find(division));
            entityManager.persist(newMonthReport);
            return newMonthReport;
        }else{
            return (MonthReport)result.get(0);
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
            MonthReportDetail newMonthReportDetail = new MonthReportDetail();
            entityManager.persist(newMonthReportDetail);
            return newMonthReportDetail;
        }else{
            return (MonthReportDetail)result.get(0);
        }
    }

    @Transactional
    public void save(MonthReportDetail monthReportDetail)  {
        entityManager.merge(monthReportDetail);
        entityManager.flush();
        logger.debug("Flushed monthReportDetail object id = {}", monthReportDetail.getId());
    }

}
