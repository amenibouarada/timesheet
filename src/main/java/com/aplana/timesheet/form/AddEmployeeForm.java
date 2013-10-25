package com.aplana.timesheet.form;

import java.util.List;

/**
 * Форма для добавления сотрудников на форме "Планирование занятости за период"
 * @author pmakarov
 * @see <a href="http://conf.aplana.com/pages/viewpage.action?pageId=10487278">Аналитика</a>
 * creation date: 18.10.13
 */
public class AddEmployeeForm {
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
}
