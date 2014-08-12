package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.dao.Identifiable;
import org.hibernate.annotations.ForeignKey;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Set;

@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "job", "division"}))
public class Employee implements Identifiable, Comparable{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq")
    @SequenceGenerator(name = "emp_seq", sequenceName = "emp_seq", allocationSize = 10)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String patronymic;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "start_date", columnDefinition = "date not null")
    private Timestamp startDate;

	@Column
	private String ldap;

    @Column(name="end_date")
    private Timestamp endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager")
    @ForeignKey(name = "FK_EMP_MANAGER")
    private Employee manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager2")
    @ForeignKey(name = "FK_MANAGER2")
    private Employee manager2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division", nullable = false)
    @ForeignKey(name = "FK_EMP_DIVISION")
    private Division division;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job", nullable = false)
    @ForeignKey(name = "FK_EMP_JOB")
    private ProjectRole job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region")
    @ForeignKey(name = "FK_EMP_REGION")
    private Region region;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "employee_permissions",
               joinColumns = {
                       @JoinColumn(name = "employee_id", nullable = false) },
               inverseJoinColumns = {
                       @JoinColumn(name = "permission_id", nullable = false) })
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Vacation> vacations;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Illness> illnesses;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<EmployeeProjectBillable> employeeProjectBillables;

    @Column(name = "not_to_sync", columnDefinition = "bool not null default false")
    private boolean notToSync;

    @Column(length = 50, name = "ldap_object_sid")
    private String objectSid;

    @Column(name = "job_rate", columnDefinition = "float default 1", nullable = false)
    private Float jobRate = 1.0f;

    @Column(nullable = false, columnDefinition = "bool not null default true")
    private boolean billable = true;

    @Column(nullable = false, name = "jira_name")
    private String jiraName;


    public Employee getManager2() {
        return manager2;
    }

    public void setManager2(Employee manager2) {
        this.manager2 = manager2;
    }

    public Set<Vacation> getVacations() {
        return vacations;
    }

    public void setVacations(Set<Vacation> vacations) {
        this.vacations = vacations;
    }

    public Set<Illness> getIllnesses() {
        return illnesses;
    }

    public void setIllnesses(Set<Illness> illnesses) {
        this.illnesses = illnesses;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean isNotToSync() {
        return notToSync;
    }

    public void setNotToSync(boolean notToSync) {
        this.notToSync = notToSync;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public ProjectRole getJob() {
        return job;
    }

    public void setJob(ProjectRole job) {
        this.job = job;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(@Nullable Employee manager) {
        this.manager = manager;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

	public String getLdap() {
		return ldap;
	}

	public void setLdap(String ldap) {
		this.ldap = ldap;
	}

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getObjectSid() {
        return objectSid;
    }

    public void setObjectSid(String objectSid) {
        this.objectSid = objectSid;
    }

    public Float getJobRate() { //todo переделать в BigDecimal
        return jobRate;
    }

    public void setJobRate(Float jobRate) {
        this.jobRate = jobRate;
    }

    public boolean isBillable() {
        return billable;
    }

    public void setBillable(boolean billable) {
        this.billable = billable;
    }

    public String getJiraName() {
        return jiraName;
    }

    public void setJiraName(String jiraName) {
        this.jiraName = jiraName;
    }

    //проверяем уволенный ли сотрудник
    //ts==null сравниваем с текущей датой
    public boolean isDisabled(Timestamp ts) {
        if(endDate!=null) {
            if(ts==null) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                ts = new Timestamp(c.getTimeInMillis());
            }
            return ts.compareTo(endDate)>0;
        }
        else return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Employee employee = (Employee) o;

        final Integer employeeId = employee.getId();

        return (getId() != null ? getId().equals(employeeId) : employeeId == null);
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Employee");
        sb.append("{id=").append(getId());
        sb.append(", name='").append(getName()).append('\'');
        sb.append(", email='").append(getEmail()).append('\'');
        sb.append(", startDate=").append(getStartDate());
        sb.append(", ldap='").append(ldap).append('\'');
        sb.append(", endDate=").append(getEndDate());
        sb.append(", job=").append(getJob());
        sb.append(", notToSync=").append(isNotToSync());
        sb.append(", objectSid='").append(getObjectSid()).append('\'');
        sb.append(", jobRate=").append(getJobRate());
        sb.append('}');
        return sb.toString();
    }

    public Set<EmployeeProjectBillable> getEmployeeProjectBillables() {
        return employeeProjectBillables;
    }

    public void setEmployeeProjectBillables(Set<EmployeeProjectBillable> employeeProjectBillables) {
        this.employeeProjectBillables = employeeProjectBillables;
    }

    @Override
    public int compareTo(Object o) {
        if(equals(o))
            return 0;
        else
            return getName().compareTo(((Employee) o).getName());
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    /**
     *
     * @return true if male, false if female
     */
    public Boolean getSex(){
        if (getPatronymic() == null) {
            return true;
        }
        String lastChar = getPatronymic().substring(getPatronymic().length(),getPatronymic().length());
        if ("ч".equals(lastChar)) {
            return true;
        } else if ("а".equals(lastChar)) {
            return false;
        } else {
            return true;
        }
    }
}