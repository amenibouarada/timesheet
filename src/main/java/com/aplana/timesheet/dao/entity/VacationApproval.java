package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author rshamsutdinov
 * @version 1.0.0
 */
@Entity
@Table(name = "vacation_approval")
public class VacationApproval {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "INTEGER NOT NULL")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_id", nullable = false)
    @ForeignKey(name = "fk_vacation")
    private Vacation vacation;

    @Column(name = "request_datetime", columnDefinition = "date NOT NULL")
    private Date requestDate;

    @Column(name = "response_datetime", columnDefinition = "date")
    private Date responseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @ForeignKey(name = "fk_manager")
    private Employee manager;

    @Column(name = "uid", columnDefinition = "CHAR(36) NOT NULL")
    private String uid;

    @Column (name = "result")
    private Boolean result;

    @Column (nullable = true)
    private String comment;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Vacation getVacation() {
        return vacation;
    }

    public void setVacation(Vacation vacation) {
        this.vacation = vacation;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("VacationApproval");
        sb.append("{id=").append(id);
        sb.append(", vacation=").append(vacation);
        sb.append(", requestDate=").append(requestDate);
        sb.append(", responseDate=").append(responseDate);
        sb.append('}');
        return sb.toString();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
