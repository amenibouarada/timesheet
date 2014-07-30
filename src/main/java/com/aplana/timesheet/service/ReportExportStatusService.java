package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ReportExportStatusDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportExportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by abayanov
 * Date: 29.07.14
 */
@Service
public class ReportExportStatusService {

    @Autowired
    ReportExportStatusDAO reportExportStatusDAO;

    public ReportExportStatus find(Employee employee, String reportName, Integer hash){
        return reportExportStatusDAO.find(employee,reportName, hash);
    }

    public ReportExportStatus find(Integer id){
        return reportExportStatusDAO.find(id);
    }

    public List<ReportExportStatus> findUserIncompleteReports(Employee employee) {
        return reportExportStatusDAO.findUserIncompleteReports(employee);
    }

    public void delete(ReportExportStatus reportExportStatus){
        reportExportStatusDAO.delete(reportExportStatus);
    }

    public void save(ReportExportStatus reportExportStatus) {
        reportExportStatusDAO.save(reportExportStatus);
    }
}
