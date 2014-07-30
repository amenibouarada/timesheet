package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ReportExportStatusNewDAO;
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
    ReportExportStatusNewDAO reportExportStatusNewDAO;

    public ReportExportStatus find(Employee employee, String reportName, Integer hash){
        return reportExportStatusNewDAO.find(employee,reportName, hash);
    }

    public ReportExportStatus find(Integer id){
        return reportExportStatusNewDAO.find(id);
    }

    public List<ReportExportStatus> findUserIncompleteReports(Employee employee) {
        return reportExportStatusNewDAO.findUserIncompleteReports(employee);
    }

    public void delete(ReportExportStatus reportExportStatus){
        reportExportStatusNewDAO.delete(reportExportStatus);
    }

    public void save(ReportExportStatus reportExportStatus) {
        reportExportStatusNewDAO.save(reportExportStatus);
    }
}
