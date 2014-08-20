package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.enums.ReportSendApprovalType;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by abayanov
 * Date: 20.08.14
 */
@Entity
@Table(name = "delete_timesheet_approval")
public class DeleteTimeSheetApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "del_ts_appr_seq")
    @SequenceGenerator(name = "del_ts_appr_seq", sequenceName = "del_ts_appr_seq", allocationSize = 10)
    @Column(name = "id", columnDefinition = "integer")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", unique = true, nullable = true)
    private TimeSheet timeSheet;

    @Column(name = "delete_approval_date", nullable = true)
    private Date deleteSendApprovalDate;

    @Column(name = "delete_approval_comment", nullable = true)
    private String deleteSendApprovalComment;

    @Column(name = "send_approval_type", nullable = true)
    @Enumerated(EnumType.ORDINAL)
    private ReportSendApprovalType reportSendApprovalType;

    public Date getDeleteSendApprovalDate() {
        return deleteSendApprovalDate;
    }

    public void setDeleteSendApprovalDate(Date deleteSendApprovalDate) {
        this.deleteSendApprovalDate = deleteSendApprovalDate;
    }

    public String getDeleteSendApprovalComment() {
        return deleteSendApprovalComment;
    }

    public void setDeleteSendApprovalComment(String deleteSendApprovalComment) {
        this.deleteSendApprovalComment = deleteSendApprovalComment;
    }

    public ReportSendApprovalType getReportSendApprovalType() {
        return reportSendApprovalType;
    }

    public void setReportSendApprovalType(ReportSendApprovalType reportSendApprovalType) {
        this.reportSendApprovalType = reportSendApprovalType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TimeSheet getTimeSheet() {
        return timeSheet;
    }

    public void setTimeSheet(TimeSheet timeSheet) {
        this.timeSheet = timeSheet;
    }
}
