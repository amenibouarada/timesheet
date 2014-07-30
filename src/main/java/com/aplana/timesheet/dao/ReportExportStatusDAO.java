package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportExportStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by abayanov
 * Date: 30.07.14
 */
@Repository
public class ReportExportStatusDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public ReportExportStatus find(Employee employee, String reportName, Integer hash) {
        Query query = entityManager.createQuery(""
                + "select re "
                + "from ReportExportStatus as re " +
                "where re.employee= :employee  and re.reportName=:reportName and re.hashForm = :hash").
                setParameter("employee", employee).
                setParameter("reportName", reportName).
                setParameter("hash", hash);

        List<ReportExportStatus> result = query.getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public void delete(ReportExportStatus reportExportStatus) {
        entityManager.remove(reportExportStatus);
    }

    public void save(ReportExportStatus reportExportStatus) {
        entityManager.merge(reportExportStatus);
    }

    public ReportExportStatus find(Integer id) {
        return entityManager.find(ReportExportStatus.class, id);
    }

    public List findUserIncompleteReports(Employee employee) {
        Query query = entityManager.createQuery(""
                + "select re "
                + "from ReportExportStatus as re " +
                "where re.employee= :employee and re.complete = true").
                setParameter("employee", employee);

        return query.getResultList();
    }
}
