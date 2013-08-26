package com.aplana.timesheet.form;

public class TimeSheetTableRowForm {
	private Integer projectTaskId;
	private String description;
	private String duration;
	private String problem;
	private Integer projectId;
	private Integer activityTypeId;
	private Integer activityCategoryId;
	private Integer projectRoleId;
    private Integer workplaceId;

	public Integer getProjectRoleId() {
		return projectRoleId;
	}

	public void setProjectRoleId(Integer projectRoleId) {
		this.projectRoleId = projectRoleId;
	}

	public Integer getProjectTaskId() {
		return projectTaskId;
	}

	public String getDescription() {
		return description;
	}

	public String getDuration() {
		return duration;
	}

	public String getProblem() {
		return problem;
	}

	public void setProjectTaskId(Integer projectTaskId) {
		this.projectTaskId = projectTaskId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public Integer getActivityTypeId() {
		return activityTypeId;
	}

	public Integer getActivityCategoryId() {
		return activityCategoryId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public void setActivityTypeId(Integer activityTypeId) {
		this.activityTypeId = activityTypeId;
	}

	public void setActivityCategoryId(Integer activityCategoryId) {
		this.activityCategoryId = activityCategoryId;
	}

    public Integer getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(Integer workplaceId) {
        this.workplaceId = workplaceId;
    }
}