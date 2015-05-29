package com.aplana.timesheet.dao.entity;

/**
 * @author pmakarov
 * @see <a href="">Аналитика</a>
 *      creation date: 29.11.13
 */
public class ProjectPercentPlan {
    private Integer projectId;
    private String projectName;
    private Integer month;
    private Integer year;
    private Double percent;
    private Integer isFact;

    public ProjectPercentPlan(Integer projectId, String projectName, Integer month, Integer year, Double percent, Integer fact) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.month = month;
        this.year = year;
        this.percent = percent;
        isFact = fact;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Integer getFact() {
        return isFact;
    }

    public void setFact(Integer fact) {
        isFact = fact;
    }
}
