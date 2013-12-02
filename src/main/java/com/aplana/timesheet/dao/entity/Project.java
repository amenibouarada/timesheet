package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "project", uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class Project {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq")
	@SequenceGenerator(name = "project_seq", sequenceName = "project_seq", allocationSize = 10)
	@Column(nullable = false)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Column
	private String projectId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager")
	@ForeignKey(name = "FK_PROJECT_MANAGER")
	private Employee manager;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "state", nullable = false)
	@ForeignKey(name = "FK_PROJECT_STATE")
	private DictionaryItem state;

	@Column(name = "cq_required", columnDefinition = "bool not null default false")
	private boolean cqRequired;

	@Column(columnDefinition = "bool not null default true")
	private boolean active;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = CascadeType.ALL)
	private Set<ProjectTask> projectTasks;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = CascadeType.ALL)
    private Set<TimeSheetDetail> timeSheetDetail;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "division_project",
            joinColumns = {
                    @JoinColumn(name = "project_id", nullable = false) },
            inverseJoinColumns = {
                    @JoinColumn(name = "division_id", nullable = false) })
	private Set<Division> divisions;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<EmployeeProjectBillable> employeeProjectBillables;

    @Column(name = "start_date", columnDefinition = "date")
    private Date startDate;

    @Column(name = "end_date", columnDefinition = "date")
    private Date endDate;

    @Column(name = "jira_project_key")
    private String jiraProjectKey;

    @Column(name = "act_type")
    private String activityType;

    @Column(name = "passport")
    private String passport;

    @Column(name = "work_type")
    private String workType;

    @Column(name = "customer")
    private String customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division", nullable = true) //todo Определиться обязательно ли поле к заполнению. Пока таблица заполняется отключил.
    @ForeignKey(name = "FK_PROJECT_DIVISION")
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_type", nullable = true) //todo Определиться обязательно ли поле к заполнению. Пока таблица заполняется отключил.
    @ForeignKey(name = "FK_PROJECT_FUNDING_TYPE")
    private DictionaryItem fundingType;

    public Set<Division> getDivisions() {
		return divisions;
	}

	public void setDivisions(Set<Division> divisions) {
		this.divisions = divisions;
	}

	public Set<ProjectTask> getProjectTasks() {
		return projectTasks;
	}

	public void setProjectTasks(Set<ProjectTask> projectTasks) {
		this.projectTasks = projectTasks;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isCqRequired() {
		return cqRequired;
	}

	public void setCqRequired(boolean cqRequired) {
		this.cqRequired = cqRequired;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Employee getManager() {
		return manager;
	}

	public DictionaryItem getState() {
		return state;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public void setState(DictionaryItem state) {
		this.state = state;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
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

    public String getJiraProjectKey() {
        return jiraProjectKey;
    }

    public void setJiraProjectKey(String jiraProjectKey) {
        this.jiraProjectKey = jiraProjectKey;
    }

    public Set<TimeSheetDetail> getTimeSheetDetail() {
        return timeSheetDetail;
    }

    public void setTimeSheetDetail(Set<TimeSheetDetail> timeSheetDetail) {
        this.timeSheetDetail = timeSheetDetail;
    }

    public Set<EmployeeProjectBillable> getEmployeeProjectBillables() {
        return employeeProjectBillables;
    }

    public void setEmployeeProjectBillables(Set<EmployeeProjectBillable> employeeProjectBillables) {
        this.employeeProjectBillables = employeeProjectBillables;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public DictionaryItem getFundingType() {
        return fundingType;
    }

    public void setFundingType(DictionaryItem fundingType) {
        this.fundingType = fundingType;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(" id=").append(id)
                .append(" name=").append(name)
                .append(" manager [").append(manager).append("]")
                .append(" projectid=").append(projectId)
                .toString();
    }
}