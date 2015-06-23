package com.aplana.timesheet.dao;

import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.AbstractReport;
import com.aplana.timesheet.util.HibernateQueryResultDataSource;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AAfanasyev on 23.06.2015.
 */
@Repository
public abstract class AbstractReportDAO {

    @PersistenceContext
    public EntityManager entityManager;

    public static Map<Class, String[]> fieldsMap = new HashMap<Class, String[]>(6);

    public HibernateQueryResultDataSource getReportData(AbstractReport report) throws JReportBuildError{

        if (getResultList(report) != null && !getResultList(report).isEmpty()) {
            return new HibernateQueryResultDataSource(getResultList(report), fieldsMap.get(report.getClass()));
        } else {
            return null;
        }
    }

    public abstract List getResultList(AbstractReport abstractReport) throws JReportBuildError;
}
