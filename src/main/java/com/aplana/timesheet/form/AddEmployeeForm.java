package com.aplana.timesheet.form;

import java.util.List;

/**
 * Форма для добавления сотрудников на форме "Планирование занятости за период"
 * @author pmakarov
 * @see <a href="http://conf.aplana.com/pages/viewpage.action?pageId=10487278">Аналитика</a>
 * creation date: 18.10.13
 */
// ToDo сделать общую форму
public class AddEmployeeForm {
    private Integer year;
    private Integer month;
    private String monthTxt;
    private Integer divisionOwnerId;
    private Integer projectTypeId;
    private Integer projectId;
    private Integer divisionId;
    private Integer managerId;
    private List<Integer> projectRoleListId;
    private List<Integer> regionListId;

    public static final String ADD_FORM = "addEmployeeForm";

    public AddEmployeeForm() {
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public List<Integer> getProjectRoleListId() {
        return projectRoleListId;
    }

    public void setProjectRoleListId(List<Integer> projectRoleListId) {
        this.projectRoleListId = projectRoleListId;
    }

    public List<Integer> getRegionListId() {
        return regionListId;
    }

    public void setRegionListId(List<Integer> regionListId) {
        this.regionListId = regionListId;
    }

    public Integer getDivisionOwnerId() {
        return divisionOwnerId;
    }

    public void setDivisionOwnerId(Integer divisionOwnerId) {
        this.divisionOwnerId = divisionOwnerId;
    }

    public Integer getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(Integer projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getMonthTxt() {
        return monthTxt;
    }

    public void setMonthTxt(String monthTxt) {
        this.monthTxt = monthTxt;
    }
}
