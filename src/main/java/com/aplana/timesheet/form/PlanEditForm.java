package com.aplana.timesheet.form;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class PlanEditForm {

    public static final int ALL_VALUE = 0;

    public static final String FORM = "planEditForm";

    public static final String DIVISION_ID = "divisionId";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String MANAGER = "manager";
    public static final String REGIONS = "regions";
    public static final String PROJECT_ROLES = "projectRoles";
    public static final String SHOW_PLANS = "showPlans";
    public static final String SHOW_FACTS = "showFacts";
    public static final String SHOW_PROJECTS = "showProjects";
    public static final String SHOW_PRESALES = "showPresales";
    public static final String SHOW_SUM_PROJECTS_PRESALES = "showSumProjectsPresales";
    public static final String SHOW_SUM_FUNDING_TYPE = "showSumFundingType";
    public static final String JSON_DATA = "jsonData";

    private Integer divisionId;
    private Integer year;
    private Integer month;
    private List<Integer> regions;
    private List<Integer> projectRoles;
    private Integer manager;
    private Boolean showPlans;
    private Boolean showFacts;
    private Boolean showProjects;
    private Boolean showPresales;
    private Boolean showSumProjectsPresales;
    private Boolean showSumFundingType;
    private String jsonData;

    public PlanEditForm() {
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
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

    public List<Integer> getRegions() {
        return regions;
    }

    public void setRegions(List<Integer> regions) {
        this.regions = regions;
    }

    public List<Integer> getProjectRoles() {
        return projectRoles;
    }

    public void setProjectRoles(List<Integer> projectRoles) {
        this.projectRoles = projectRoles;
    }

    public Boolean getShowPlans() {
        return showPlans;
    }

    public void setShowPlans(Boolean showPlans) {
        this.showPlans = showPlans;
    }

    public Boolean getShowFacts() {
        return showFacts;
    }

    public void setShowFacts(Boolean showFacts) {
        this.showFacts = showFacts;
    }

    public Boolean getShowProjects() {
        return showProjects;
    }

    public void setShowProjects(Boolean showProjects) {
        this.showProjects = showProjects;
    }

    public Boolean getShowPresales() {
        return showPresales;
    }

    public void setShowPresales(Boolean showPresales) {
        this.showPresales = showPresales;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public Integer getManager() {
        return manager;
    }

    public void setManager(Integer manager) {
        this.manager = manager;
    }

    public Boolean getShowSumProjectsPresales() {
        return showSumProjectsPresales;
    }

    public void setShowSumProjectsPresales(Boolean showSumProjectsPresales) {
        this.showSumProjectsPresales = showSumProjectsPresales;
    }

    public Boolean getShowSumFundingType() {
        return showSumFundingType;
    }

    public void setShowSumFundingType(Boolean showSumFundingType) {
        this.showSumFundingType = showSumFundingType;
    }
}
