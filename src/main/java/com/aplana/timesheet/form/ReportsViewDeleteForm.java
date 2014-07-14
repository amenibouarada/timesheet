package com.aplana.timesheet.form;


/**
 * Created by abayanov
 * Date: 06.06.14
 */
public class ReportsViewDeleteForm {

    private Integer ids[];
    private String link;
    private Integer reportId;
    private String comment;

    public Integer[] getIds() {
        return ids;
    }

    public void setIds(Integer[] ids) {
        this.ids = ids;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
