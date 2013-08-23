package com.aplana.timesheet.form;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

public class TimeSheetForm {
    private Integer divisionId;
    private Integer employeeId;
    private List<TimeSheetTableRowForm> timeSheetTablePart;
    private String calDate;
    private String plan;
    /** Причина недоработок, переработок */
    private Integer overtimeCause;
    /** Комментария к причине надоработок/переработко */
    private String overtimeCauseComment;
    private Integer typeOfCompensation;
    /* оценка моего объема работ на следующий рабочий день */
    private Integer effortInNextDay;

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public List<TimeSheetTableRowForm> getTimeSheetTablePart() {
        return timeSheetTablePart;
    }

    public String getPlan() {
        return plan;
    }

    public void setTimeSheetTablePart(List<TimeSheetTableRowForm> timeSheetTablePart) {
        this.timeSheetTablePart = timeSheetTablePart;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getCalDate() {
        return calDate;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public void setCalDate(String calDate) {
        this.calDate = calDate;
    }

    public Integer getOvertimeCause() {
        return overtimeCause;
    }

    public void setOvertimeCause(Integer overtimeCause) {
        this.overtimeCause = overtimeCause;
    }

    public String getOvertimeCauseComment() {
        return overtimeCauseComment;
    }

    public void setOvertimeCauseComment(String overtimeCauseComment) {
        this.overtimeCauseComment = overtimeCauseComment;
    }

    public Integer getTypeOfCompensation() {
        return typeOfCompensation;
    }

    public void setTypeOfCompensation(Integer typeOfCompensation) {
        this.typeOfCompensation = typeOfCompensation;
    }

    public Integer getEffortInNextDay() {
        return effortInNextDay;
    }

    public void setEffortInNextDay(Integer effortInNextDay) {
        this.effortInNextDay = effortInNextDay;
    }

    public void unEscapeHTML() {
        plan = StringEscapeUtils.unescapeHtml4(plan);
        overtimeCauseComment = StringEscapeUtils.unescapeHtml4(overtimeCauseComment);
        for(TimeSheetTableRowForm part:timeSheetTablePart){
            if (part!=null) {
                part.setDescription(StringEscapeUtils.unescapeHtml4(part.getDescription()));
                part.setProblem(StringEscapeUtils.unescapeHtml4(part.getProblem()));
            }
        }
    }
}