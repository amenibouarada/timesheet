package com.aplana.timesheet.form;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class VacationsForm extends CommonAbstractForm {

    public static final int ALL_VALUE = 0;
    public static final String REGIONS = "regions";
    public static final String MANAGER_ID = "managerId";
    public static final String EMPLOYEE_ID = "employeeId";
    public static final String DIVISION_ID = "divisionId";
    public static final String PROJECT_ID = "projectId";
    public static final String VACATION_ID = "vacationId";
    public static final String VACATION_TYPE = "vacationType";
    public static final String CAL_FROM_DATE = "calFromDate";
    public static final String CAL_TO_DATE = "calToDate";
    public static final String APPROVAL_ID = "approvalId";
    public static final String VIEW_MODE = "viewMode";

    // отображение данных
    public static final int VIEW_TABLE = 1;
    public static final int VIEW_GRAPHIC_BY_DAY = 2;
    public static final int VIEW_GRAPHIC_BY_WEEK = 3;

    private Integer year;
    private Integer vacationId;
    private Integer vacationType;
    private Integer managerId;
    private List<Integer> regions;
    private Integer approvalId;
    private Integer projectId;
    private Integer viewMode; // нужно для отображения вкладок, переключателей

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getVacationId() {
        return vacationId;
    }

    public void setVacationId(Integer vacationId) {
        this.vacationId = vacationId;
    }

    public Integer getVacationType() {
        return vacationType;
    }

    public void setVacationType(Integer vacationType) {
        this.vacationType = vacationType;
    }

    public List<Integer> getRegions() {
        return regions;
    }

    public void setRegions(List<Integer> regions) {
        this.regions = regions;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Integer approverID) {
        this.approvalId = approverID;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getViewMode() {
        return viewMode;
    }

    public void setViewMode(Integer viewMode) {
        this.viewMode = viewMode;
    }
}

