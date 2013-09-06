package com.aplana.timesheet.form;

/**
 * @author: iziyangirov
 */
public abstract class CommonAbstractForm extends AbstractFormForEmployee {

    private String calFromDate;
    private String calToDate;

    public String getCalFromDate() {
        return calFromDate;
    }

    public void setCalFromDate(String calFromDate) {
        this.calFromDate = calFromDate;
    }

    public String getCalToDate() {
        return calToDate;
    }

    public void setCalToDate(String calToDate) {
        this.calToDate = calToDate;
    }
}
