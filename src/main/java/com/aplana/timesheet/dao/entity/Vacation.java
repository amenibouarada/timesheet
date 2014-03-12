package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Entity
@Table(name = "vacation")
public class Vacation implements Comparable{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vacation_seq")
    @SequenceGenerator(name = "vacation_seq", sequenceName = "vacation_seq", allocationSize = 10)
    @Column(columnDefinition = "integer not null")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @ForeignKey(name = "fk_employee")
    private Employee employee;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "begin_date")
    private Date beginDate;

    @Column(name = "end_date")
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    @ForeignKey(name = "fk_vacation_type")
    private DictionaryItem type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    @ForeignKey(name = "fk_vacation_status")
    private DictionaryItem status;

    @Column(name = "comment")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @ForeignKey(name = "fk_author")
    private Employee author;

    @OneToMany(mappedBy = "vacation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("requestDate, responseDate ASC")
    private Set<VacationApproval> vacationApprovals;

    @Column(name = "remind", columnDefinition = "bool not null default false")
    private Boolean remind;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public DictionaryItem getType() {
        return type;
    }

    public void setType(DictionaryItem type) {
        this.type = type;
    }

    public DictionaryItem getStatus() {
        return status;
    }

    public void setStatus(DictionaryItem status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Employee getAuthor() {
        return author;
    }

    public void setAuthor(Employee author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<VacationApproval> getVacationApprovals() {
        return vacationApprovals;
    }

    public void setVacationApprovals(Set<VacationApproval> vacationApprovals) {
        this.vacationApprovals = vacationApprovals;
    }

    public Boolean getRemind() {
        return remind;
    }

    public void setRemind(Boolean remind) {
        this.remind = remind;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Vacation");
        sb.append("{id=").append(id);
        sb.append(", employee=").append(employee);
        sb.append(", beginDate=").append(beginDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", type=").append(type);
        sb.append(", status=").append(status);
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", author=").append(author);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vacation)) return false;

        Vacation vacation = (Vacation) o;

        if (id != null ? !id.equals(vacation.id) : vacation.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Object obj) {
        if (this == obj) return 0;
        if (obj == null) return -1;
        Vacation vacation = (Vacation) obj;
        if(this.id == vacation.getId()) return 0;
        int result = this.getBeginDate().compareTo(vacation.getBeginDate());
        return (result==0)?1:result;
    }

}
