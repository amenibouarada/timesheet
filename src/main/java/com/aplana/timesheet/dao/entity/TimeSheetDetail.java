package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "time_sheet_detail")
public class TimeSheetDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "time_sheet_detail_seq")
	@SequenceGenerator(name = "time_sheet_detail_seq", sequenceName = "time_sheet_detail_seq", allocationSize = 10)
	@Column(columnDefinition = "decimal(10,0) not null")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "time_sheet_id")
	@ForeignKey(name = "FK_TIME_SHEET")
	private TimeSheet timeSheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_id")
    @ForeignKey(name = "FK_TIME_SHEET_WORKPLACE")
    private DictionaryItem workplace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_type")
	@ForeignKey(name = "FK_TIME_SHEET_TYPE")
	private DictionaryItem actType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_cat")
	@ForeignKey(name = "FK_TIME_SHEET_CAT")
	private DictionaryItem actCat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "proj_id")
	@ForeignKey(name = "FK_TIME_SHEET_PROJECT")
	private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@ForeignKey(name = "FK_TIME_SHEET_PROJECT_ROLE")
    private ProjectRole projectRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "FK_TIME_SHEET_PROJECT_TASK")
    @JoinColumn(name = "task_id")
    private ProjectTask projectTask;

	@Column(name = "name")
    /**
     * @deprecated
     */
	private String taskName;

	@Column(columnDefinition = "text null")
	private String description;

	@Column(columnDefinition = "decimal(4,2) null")
	private Double duration; //todo переделать в BigDecimal

	@Column(columnDefinition = "text null")
	private String problem;

	public TimeSheet getTimeSheet() {
		return timeSheet;
	}

	public DictionaryItem getActType() {
		return actType;
	}

	public DictionaryItem getActCat() {
		return actCat;
	}

	public Project getProject() {
		return project;
	}

	public void setTimeSheet(TimeSheet timeSheet) {
		this.timeSheet = timeSheet;
	}

	public void setActType(DictionaryItem actType) {
		this.actType = actType;
	}

	public void setActCat(DictionaryItem actCat) {
		this.actCat = actCat;
	}

	public void setProject(Project proj) {
		this.project = proj;
	}

	public Double getDuration() {
		return duration;
	}

    /**
     * @deprecated
     */
	public String getTaskName() {
		return taskName;
	}

	public String getDescription() {
		return description;
	}

	public String getProblem() {
		return problem;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

    /**
     * @deprecated
     */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

    public ProjectRole getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(ProjectRole projectRole) {
        this.projectRole = projectRole;
    }

    public ProjectTask getProjectTask() {
        return projectTask;
    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
    }

    public DictionaryItem getWorkplace() {
        return workplace;
    }

    public void setWorkplace(DictionaryItem workplace) {
        this.workplace = workplace;
    }

    @Override
	public String toString() {
		return new StringBuilder()
			.append(" id=").append(id)
			.append(" timeSheet [").append(timeSheet).append("]")
			.append(" actType [").append(actType).append("]")
			.append(" actCat [").append(actCat).append("]")
			.append(" project [").append(project).append("]")
			.append(" taskName=").append(taskName)
			.append(" description=").append(description)
			.append(" duration=").append(duration)
			.append(" problem=").append(problem)
		.toString();
	}
}