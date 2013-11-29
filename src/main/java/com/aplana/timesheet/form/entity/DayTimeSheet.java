package com.aplana.timesheet.form.entity;

import com.aplana.timesheet.dao.BusinessTripDAO;
import com.aplana.timesheet.dao.IllnessDAO;
import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/*
Самый дебильный класс во всем проекте!
 */
public class DayTimeSheet implements Comparable<DayTimeSheet> {

    private TimeSheetDAO timeSheetDAO;
    private IllnessDAO illnessDAO;
    private VacationDAO vacationDAO;
    private BusinessTripDAO businessTripDAO;
    private Employee emp;
    private Timestamp calDate;
    private Boolean workDay;
    private Integer id;
    /**
     * Отпуск или отгул или ещё что (не я это придумал так было до меня)
     */
    private Integer act_type;
    private Boolean isLoadDuration = false;
    private BigDecimal duration;
    private Boolean isLoadTimeSheet = false;
    private TimeSheet timeSheet;
    private boolean haveDraft;

    public DayTimeSheet(Timestamp calendarDate, Boolean isHoliday, Integer timeSheetId, Integer act_type, BigDecimal dur, Employee emp, boolean haveDraft) {
        this.setCalDate(calendarDate);
        this.setWorkDay(!isHoliday); // APLANATS-266. workday = true - выходной день, а false - рабочий!
        this.setId(timeSheetId);
        this.setAct_type(act_type);
        this.setDuration(dur);
        this.setEmp(emp);
        this.haveDraft = haveDraft;
    }

    public void setIllnessDAO(IllnessDAO illnessDAO) {
        this.illnessDAO = illnessDAO;
    }

    public void setTimeSheetDAO(TimeSheetDAO timeSheetDAO) {
        this.timeSheetDAO = timeSheetDAO;
    }

    public void setVacationDAO(VacationDAO vacationDAO) {
        this.vacationDAO = vacationDAO;
    }

    public void setBusinessTripDAO(BusinessTripDAO businessTripDAO) {
        this.businessTripDAO = businessTripDAO;
    }

    public Integer getAct_type() {
        return act_type;
    }

    public void setAct_type(Integer act_type) {
        this.act_type = act_type;
    }

    public BigDecimal getDuration() {
        return this.duration;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
        if (this.duration == null)
            this.duration = new BigDecimal(0);
    }

    public Timestamp getCalDate() {
        return calDate;
    }

    public void setCalDate(Timestamp calDate) {
        this.calDate = calDate;
    }

    /**
     * Сообщает нам выходной ли день или рабочий
     *
     * @return
     */
    public Boolean getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Boolean workDay) {
        this.workDay = workDay;
    }

    public Integer getId() {
        return id;
    }

    /**
     * @return TimeSheet или null
     */
    public TimeSheet getTimeSheet() {
        if (this.getId() != null) {
            if (!this.isLoadTimeSheet) {
                this.timeSheet = this.timeSheetDAO.find(this.getId());
                this.isLoadTimeSheet = true;
            }
            return this.timeSheet;
        }
        return null;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Employee getEmp() {
        return emp;
    }

    public void setEmp(Employee emp) {
        this.emp = emp;
    }

    @Override
    public int compareTo(DayTimeSheet o) {
        if (getCalDate().getTime() < o.getCalDate().getTime()) {
            return -1;
        } else if (getCalDate().getTime() == o.getCalDate().getTime()) {
            return 0;
        } else {
            return 1;
        }
    }

    private Timestamp getCurrent() {
        return new Timestamp(new Date().getTime());
    }

    /**
     * Сообщает что работнег ещё не приступил к исполнению обязанностей и день учитывать не стоит
     *
     * @return
     */
    public Boolean getStatusNotStart() {
        return this.getWorkDay() && this.getEmp().getStartDate().after(this.getCalDate());
    }

    /**
     * Этот день ещё не настал (больше чем текущая дата)
     *
     * @return
     */
    public Boolean getStatusNotCome() {
        return !this.getStatusHoliday() && getIsDayNotCome() && this.getTimeSheet() == null;
    }

    /*
     * Имя метода соответсвует тому что он выполняет
     * Этот день ещё не настал (больше чем текущая дата)
     */
    public Boolean getIsDayNotCome(){
        return getCurrent().before(getCalDate());
    }

    /*
     * Этот день ещё не настал (больше чем текущая дата)
     * Имя метода соответсвует тому что он выполняет
    */
    public Boolean getIsCalDateLongAgo(){
        long threeMonthsAgo = getCurrent().getTime() - DateTimeUtil.THREE_MONTHS_IN_MILLS;
        return threeMonthsAgo > getCalDate().getTime();
    }

    /**
     * Отработал и есть отчёт
     *
     * @return
     */
    public Boolean getStatusNormalDay() {
        return this.getWorkDay() && this.getTimeSheet() != null && !haveDraft;
    }

    /**
     * Рабочий день и у человека нет отчёта
     *
     * @return
     */
    public Boolean getStatusNoReport() {
        return !this.getStatusNotCome() && this.getWorkDay() && this.getTimeSheet() == null && !this.getStatusNotStart();
    }

    /**
     * Выходной день и у человека есть отчёт(скорей всего работал в выходные)
     *
     * @return
     */
    public Boolean getStatusWorkOnHoliday() {
        return !this.getWorkDay() && this.getTimeSheet() != null && !haveDraft;
    }

    /**
     * Выходной день и человек не работал, отдых
     *
     * @return
     */
    public Boolean getStatusHoliday() {
        return !this.getWorkDay() && this.getTimeSheet() == null;
    }

    /**
     * У человека есть черновик на этот день, и нет отчета
     *
     * @return
     */
    public Boolean getStatusHaveDraft() {
        return !getStatusWorkOnHoliday() && !getStatusNormalDay() && haveDraft;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DayTimeSheet)) return false;

        DayTimeSheet that = (DayTimeSheet) o;

        if (!act_type.equals(that.act_type)) return false;
        if (!calDate.equals(that.calDate)) return false;
        if (!getCurrent().equals(that.getCurrent())) return false;
        if (!duration.equals(that.duration)) return false;
        if (!emp.equals(that.emp)) return false;
        if (!id.equals(that.id)) return false;
        if (!isLoadDuration.equals(that.isLoadDuration)) return false;
        if (!isLoadTimeSheet.equals(that.isLoadTimeSheet)) return false;
        if (!timeSheet.equals(that.timeSheet)) return false;
        if (!timeSheetDAO.equals(that.timeSheetDAO)) return false;
        if (!workDay.equals(that.workDay)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timeSheetDAO.hashCode();
        result = 31 * result + emp.hashCode();
        result = 31 * result + calDate.hashCode();
        result = 31 * result + getCurrent().hashCode();
        result = 31 * result + workDay.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + act_type.hashCode();
        result = 31 * result + isLoadDuration.hashCode();
        result = 31 * result + duration.hashCode();
        result = 31 * result + isLoadTimeSheet.hashCode();
        result = 31 * result + timeSheet.hashCode();
        return result;
    }

    // является данный день больничным или нет
    @Transactional(readOnly = true)
    public Boolean getIllnessDay() {
        return illnessDAO.isDayIllness(emp, new Date(calDate.getTime()));
    }

    // является данный день отпуском или нет, без учета планируемых отпусков
    @Transactional(readOnly = true)
    public Boolean getVacationDay() {
        return vacationDAO.isDayVacationWithoutPlanned(emp, new Date(calDate.getTime()));
    }

    @Transactional(readOnly = true)
    public Boolean getTrouble() {
        if (this.timeSheet != null)
            return timeSheetDAO.timeSheetTrouble(this.timeSheet.getId());
        else
            return null;
    }

    public String getEffort() {
        if (this.timeSheet != null)
            return this.timeSheet.getEffortInNextDay().getValue();
        else
            return null;
    }

    // является данный день командировкой
    @Transactional(readOnly = true)
    public Boolean getBusinessTripDay() {
        return businessTripDAO.isDayBusinessTrip(emp, new Date(calDate.getTime()));
    }

}
