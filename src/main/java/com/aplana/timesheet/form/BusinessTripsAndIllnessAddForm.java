package com.aplana.timesheet.form;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.BusinessTripTypesEnum;
import com.aplana.timesheet.enums.IllnessTypesEnum;
import com.aplana.timesheet.enums.QuickReportTypesEnum;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 23.01.13
 */
public class BusinessTripsAndIllnessAddForm {
    private Integer reportId = null;
    private Employee employee;
    private Integer employeeId;
    private Date beginDate = new Date();
    private Date endDate;
    private String comment;
    private Integer reason = IllnessTypesEnum.ILLNESS.getId();
    private Integer reportType = QuickReportTypesEnum.ILLNESS.getId();
    private List<QuickReportTypesEnum> reportTypes = Arrays.asList(QuickReportTypesEnum.values());
    private List<IllnessTypesEnum> illnessTypes = Arrays.asList(IllnessTypesEnum.values());
    private Integer businessTripType = BusinessTripTypesEnum.NOT_PROJECT.getId();
    private List<BusinessTripTypesEnum> businessTripTypes = Arrays.asList(BusinessTripTypesEnum.values());
    private Integer projectId;

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<BusinessTripTypesEnum> getBusinessTripTypes() {
        return businessTripTypes;
    }

    public void setBusinessTripTypes(List<BusinessTripTypesEnum> businessTripTypes) {
        this.businessTripTypes = businessTripTypes;
    }

    public Integer getBusinessTripType() {
        return businessTripType;
    }

    public void setBusinessTripType(Integer businessTripType) {
        this.businessTripType = businessTripType;
    }

    public List<QuickReportTypesEnum> getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(List<QuickReportTypesEnum> reportTypes) {
        this.reportTypes = reportTypes;
    }

    public List<IllnessTypesEnum> getIllnessTypes() {
        return illnessTypes;
    }

    public void setIllnessTypes(List<IllnessTypesEnum> illnessTypes) {
        this.illnessTypes = illnessTypes;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getReason() {
        return reason;
    }

    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }
}
