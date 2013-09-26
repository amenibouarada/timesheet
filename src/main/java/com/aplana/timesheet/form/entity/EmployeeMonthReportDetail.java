package com.aplana.timesheet.form.entity;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Project;

import java.math.BigDecimal;

/* класс - строка в таблице детализации месячной работы по проектам */
public class EmployeeMonthReportDetail implements Comparable<EmployeeMonthReportDetail> {
    private DictionaryItem act_type;
    private Project project;
    private BigDecimal planHours;
    private BigDecimal factHours;
    private BigDecimal planDuration;
    private BigDecimal factDuration;

    public DictionaryItem getAct_type() {
        return act_type;
    }

    public void setAct_type(DictionaryItem act_type) {
        this.act_type = act_type;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BigDecimal getPlanHours() {
        return planHours;
    }

    public void setPlanHours(BigDecimal planHours) {
        this.planHours = planHours;
    }

    public BigDecimal getFactHours() {
        return factHours;
    }

    public void setFactHours(BigDecimal factHours) {
        this.factHours = factHours;
    }

    public BigDecimal getPlanDuration() {
        return planDuration;
    }

    public void setPlanDuration(BigDecimal planDuration) {
        this.planDuration = planDuration;
    }

    public BigDecimal getFactDuration() {
        return factDuration;
    }

    public void setFactDuration(BigDecimal factDuration) {
        this.factDuration = factDuration;
    }

    public EmployeeMonthReportDetail(DictionaryItem act_type, Project project, BigDecimal planHours, BigDecimal factHours, BigDecimal planDuration, BigDecimal factDuration) {
        this.act_type = act_type;
        this.project = project;
        this.planHours = planHours.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.factHours = factHours.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.planDuration = planDuration.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.factDuration = factDuration.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public int compareTo(EmployeeMonthReportDetail o) {
        int result;
        result = getAct_type().getValue().compareTo(o.getAct_type().getValue());
        if (result == 0) {
            result = getProject().getName().compareTo(o.getProject().getName());
        }
        return result;
    }

    public Integer getPlanPercent() {
        if (planDuration != null && !(planDuration.doubleValue() == 0) && planHours != null) {
            BigDecimal l = planHours.divide(planDuration, BigDecimal.ROUND_HALF_EVEN).multiply(BigDecimal.valueOf(100));
            return l.intValue();
        } else {
            return 0;
        }
    }

    public Integer getFactPercent() {
        if (factDuration != null && !(factDuration.doubleValue() == 0) && factHours != null) {
            BigDecimal l = factHours.divide(factDuration, BigDecimal.ROUND_HALF_EVEN).multiply(BigDecimal.valueOf(100));
            return l.intValue();
        } else {
            return 0;
        }
    }
}
