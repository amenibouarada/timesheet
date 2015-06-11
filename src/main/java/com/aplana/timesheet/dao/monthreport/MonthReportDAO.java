package com.aplana.timesheet.dao.monthreport;

import com.aplana.timesheet.dao.entity.monthreport.MonthReportData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class MonthReportDAO {
    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(MonthReportDAO.class);

    public List<MonthReportData> getMonthReportData(
//            int year, int month, Integer divisionId, Integer managerId, List<Integer> regions, List<Integer> roles
    ){
        String queryString = "FROM MonthReportData";
        Query query = entityManager.createQuery(queryString);
        return query.getResultList();
    }

}
