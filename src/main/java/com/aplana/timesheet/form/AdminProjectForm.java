package com.aplana.timesheet.form;

import com.aplana.timesheet.dao.entity.*;
import org.springframework.util.AutoPopulatingList;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author dsysterov
 * @version 1.0
 */
public class AdminProjectForm {
    private String name;
    private Integer division;
    private Integer managerDivision;
    private Integer manager;
    private String customer;
    private Date startDate;
    private Date endDate;

    private Integer state;
    private Integer fundingType;

    private String jiraKey;
    private Boolean active;
    private Boolean cqRequired;

    private List<AdminProjectTaskForm> projectTasks;
    private List<AdminProjectManagerForm> projectManagers;
    private List<AdminProjectBillableForm> projectBillables;
    private List<Division> projectDivisions;

    private String passport;

    public AdminProjectForm() {
        projectTasks = new AutoPopulatingList<AdminProjectTaskForm>(AdminProjectTaskForm.class);
        projectManagers = new AutoPopulatingList<AdminProjectManagerForm>(AdminProjectManagerForm.class);
        projectBillables = new AutoPopulatingList<AdminProjectBillableForm>(AdminProjectBillableForm.class);
        projectDivisions = new AutoPopulatingList<Division>(Division.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDivision() {
        return division;
    }

    public void setDivision(Integer division) {
        this.division = division;
    }

    public Integer getManagerDivision() {
        return managerDivision;
    }

    public void setManagerDivision(Integer managerDivision) {
        this.managerDivision = managerDivision;
    }

    public Integer getManager() {
        return manager;
    }

    public void setManager(Integer manager) {
        this.manager = manager;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getFundingType() {
        return fundingType;
    }

    public void setFundingType(Integer fundingType) {
        this.fundingType = fundingType;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public void setJiraKey(String jiraKey) {
        this.jiraKey = jiraKey;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getCqRequired() {
        return cqRequired;
    }

    public void setCqRequired(Boolean cqRequired) {
        this.cqRequired = cqRequired;
    }

    public List<AdminProjectTaskForm> getProjectTasks() {
        return projectTasks;
    }

    public void setProjectTasks(List<AdminProjectTaskForm> projectTasks) {
        this.projectTasks = projectTasks;
    }

    public List<AdminProjectManagerForm> getProjectManagers() {
        return projectManagers;
    }

    public void setProjectManagers(List<AdminProjectManagerForm> projectManagers) {
        this.projectManagers = projectManagers;
    }

    public List<AdminProjectBillableForm> getProjectBillables() {
        return projectBillables;
    }

    public void setProjectBillables(List<AdminProjectBillableForm> projectBillables) {
        this.projectBillables = projectBillables;
    }

    public List<Division> getProjectDivisions() {
        return projectDivisions;
    }

    public void setProjectDivisions(List<Division> projectDivisions) {
        this.projectDivisions = projectDivisions;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }
}
