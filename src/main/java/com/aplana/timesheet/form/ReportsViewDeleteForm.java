package com.aplana.timesheet.form;


import java.util.Arrays;

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
        if(ids == null) {
            this.ids = new Integer[0];
        } else {
            this.ids = Arrays.copyOf(ids, ids.length);
        }
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
