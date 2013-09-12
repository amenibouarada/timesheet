package com.aplana.timesheet.form;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class CreateVacationForm extends CommonAbstractForm{

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    private String comment;
    private Integer vacationType;

    public CreateVacationForm() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getVacationType() {
        return vacationType;
    }

    public void setVacationType(Integer vacationType) {
        this.vacationType = vacationType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CreateVacationForm");
        sb.append("{employeeId=").append(getEmployeeId());
        sb.append(", calFromDate='").append(getCalFromDate()).append('\'');
        sb.append(", calToDate='").append(getCalToDate()).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", vacationType=").append(vacationType);
        sb.append('}');
        return sb.toString();
    }

}
