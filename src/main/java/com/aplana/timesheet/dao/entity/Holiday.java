package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "holiday")
public class Holiday implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "holiday_seq")
    @SequenceGenerator(name = "holiday_seq", sequenceName = "holiday_seq", allocationSize = 10)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caldate")
    @ForeignKey(name = "fk_calendar")
    private Calendar calDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region")
    @ForeignKey(name = "fk_region")
    private Region region;

    @Column(name = "consider", nullable = false)
    private Boolean consider;

    public Calendar getCalDate() {
        return calDate;
    }

    public void setCalDate(Calendar calDate) {
        this.calDate = calDate;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Boolean getConsider() {
        return consider;
    }

    public void setConsider(Boolean consider) {
        this.consider = consider;
    }
}
