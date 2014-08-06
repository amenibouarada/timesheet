package com.aplana.timesheet.service.helper;

import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.HolidayDAO;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.exception.service.NotDataForYearInCalendarException;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;

@Service
public class ViewReportHelper {

    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    VacationService vacationService;

    @Autowired
    CalendarService calendarService;

    @Autowired
    HolidayDAO holidayDAO;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    private static final Logger logger = LoggerFactory.getLogger(ViewReportHelper.class);

    final private Integer PLANNED_VACATION_MARK = 4;
    final private Integer VACATION_MARK = 3;
    final private Integer CROSS_VACATION_MARK = 5;
    final private Integer PREVIOUS_DAY_MARK = 1;
    final private Integer HOLIDAY_MARK = 2;
    final private Integer TYPICAL_DAY_MARK = 0;

    @Transactional
    public String getDateReportsListJson(Integer year, Integer month, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();
        final List<DayTimeSheet> calTSList = timeSheetService.findDatesAndReportsForEmployee(year, month, employeeId);

        for (DayTimeSheet queryResult : calTSList) {
            final String day = DateTimeUtil.formatDateIntoDBFormat(queryResult.getCalDate());

            Integer value = 0; //если нет отчета
            if ((queryResult.getId() != null) || (queryResult.getVacationDay()) || (queryResult.getIllnessDay())) {
                value = 1;   //если есть отчет
                if(queryResult.getStatusHaveDraft()) {
                    value = 3;
                }
            }
            else if (!queryResult.getWorkDay() && !queryResult.getBusinessTripDay()){
                value = 2;   //если выходной или праздничный день
            }

            builder.withField(day, aStringBuilder(value.toString()));
        }

        return JsonUtil.format(builder.build());
    }

    /**
     * В мапу добавляются все дни месяца с учетом прошедших дней, выходных и праздников
     * @param year
     * @param month
     * @param employeeId
     * @param vacationDates
     */
    private void addMonthDays(Integer year, Integer month, Integer employeeId, Map<Date, Integer> vacationDates) throws NotDataForYearInCalendarException {
        Date currentDate = new Date();
        Employee emp = employeeService.find(employeeId);
        List<Calendar> monthDays = calendarService.getDateList(year, month);
        if (monthDays == null || monthDays.size() == 0)
            throw new NotDataForYearInCalendarException(String.format("Календарь на %s год еще не заполнен, " +
                    "оформите заявление позже или обратитесь в службу поддержки системы", year.toString()));
        for (Calendar day : monthDays) {
            if (!holidayDAO.isWorkDay(day.getCalDate().toString(), emp.getRegion())) {
                vacationDates.put(day.getCalDate(), HOLIDAY_MARK);  //если выходной или праздничный день
            } else if (day.getCalDate().before(currentDate)) {
                vacationDates.put(day.getCalDate(), PREVIOUS_DAY_MARK);  //если это прошедший день
            } else {
                vacationDates.put(day.getCalDate(), TYPICAL_DAY_MARK);
            }
        }
    }

    /**
     * Метод отмечает дни обычного отпуска, полнового и их пересечения
     *
     * @param year
     * @param month
     * @param vacations
     * @param vacationDates мапа с днями и отмеченными выходными и праздниками
     * @param markValue     метка дня (обычный, плановый, пересечение отпусков)
     */
    private void checkVacationDay(Integer year, Integer month, List<Vacation> vacations, Map<Date, Integer> vacationDates, Integer markValue) {
        Date lastDayofMonth = calendarService.getMaxDateMonth(year, month);
        Date firstDayofMonth = calendarService.getMinDateMonth(year, month);
        if (vacationDates != null) {
            for (Vacation vacation : vacations) {
                Long cnt = DateTimeUtil.getAllDaysCount(vacation.getBeginDate(), vacation.getEndDate()) - 1;//количество дней в отпуске
                for (Long i = 0L; i <= cnt; i++) {
                    Date vacationDay = DateUtils.addDays(vacation.getBeginDate(), i.intValue());
                    if (!vacationDay.after(lastDayofMonth) && !vacationDay.before(firstDayofMonth)) {
                        if (vacationDates.get(vacationDay) != HOLIDAY_MARK) {
                            if (vacationDates.get(vacationDay) != null && (markValue != PLANNED_VACATION_MARK)) {
                                vacationDates.put(vacationDay, markValue);
                            } else {
                                if (vacationDates.get(vacationDay) == VACATION_MARK) {
                                    vacationDates.put(vacationDay, CROSS_VACATION_MARK);
                                } else {
                                    vacationDates.put(vacationDay, markValue);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public String getDateVacationWithPlannedListJson(Integer year, Integer month, Integer employeeId){
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        Map<Date, Integer> vacationDates = null;

        try {
            vacationDates = getVacationWithPlannedMap(year, month, employeeId, false);
        } catch (NotDataForYearInCalendarException e) {
            logger.error("Error in getDateVacationWithPlannedListJson : " + e.getMessage());
        }

        for (Map.Entry date : vacationDates.entrySet()) {
            final String sdate = DateTimeUtil.formatDateIntoDBFormat((Date)date.getKey());
            builder.withField(sdate, aStringBuilder(date.getValue().toString()));
        }

        String format = JsonUtil.format(builder.build());
        return format;
    }

    /**
     * Возвращает мапу с отмеченными обчными и планируемыми отпусками
     * @param year
     * @param month
     * @param employeeId
     * @param needForCalcCount
     * @return мапу с отмеченными обчными и планируемыми отпусками
     */
    private Map<Date, Integer> getVacationWithPlannedMap(Integer year, Integer month, Integer employeeId, Boolean needForCalcCount) throws NotDataForYearInCalendarException {
        Map<Date, Integer> vacationDates = new HashMap<Date, Integer>();

        addMonthDays(year, month, employeeId, vacationDates);

        List<DictionaryItem> typesVac = dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId());
        DictionaryItem planned = dictionaryItemService.find(VacationTypesEnum.PLANNED.getId());
        typesVac.remove(planned);

        final List<Vacation> vacations;

        // Если необходимо посчитать дни отпуска сотрудника в getCountVacationAndPlannedVacationDays за выбранный месяц
        // то считаем дни «Отпуска с сохранением содержания» ( утвержденные заявление и заявления на согласовании) +
        // дни «Отпуска без сохранения содержания»( утвержденные заявление и заявления на согласовании), +
        // дни «Планируемого отпуска»
        if (needForCalcCount) {
            //без учета отпусков с отработкой
            typesVac.remove(dictionaryItemService.find(VacationTypesEnum.WITH_NEXT_WORKING.getId()));

            List<DictionaryItem> statusVac = dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_STATUS.getId());
            //нужны только утвержденные отпуска
            statusVac.remove(dictionaryItemService.find(VacationStatusEnum.REJECTED.getId()));

            vacations = vacationService.findVacationsByTypesAndStatuses(year, month, employeeId, typesVac, statusVac);
        } else {
            vacations = vacationService.findVacationsByTypes(year, month, employeeId, typesVac);
        }

        checkVacationDay(year, month, vacations, vacationDates, VACATION_MARK);

        final List<Vacation> vacationsPlanned = vacationService.findVacationsByType(year, month, employeeId, planned);

        //Отмечаем плановые отпуска
        checkVacationDay(year, month, vacationsPlanned, vacationDates, PLANNED_VACATION_MARK);
        return vacationDates;
    }

    /**
     * Рекурсивный поиск даты выхода на работу
     * @param dayEndVacation дата окончания отпуска
     * @param employeeId
     * @param inVacationDates при вызове метода передавать null, мапа необходима для рекурсивного вызова
     * @return дату выхода на работу без учета
     */
    public Date getNextWorkDay(Date dayEndVacation, Integer employeeId, Map<Date, Integer> inVacationDates) throws NotDataForYearInCalendarException {
        java.util.Calendar mycal = java.util.Calendar.getInstance();
        mycal.setTime(dayEndVacation);
        Integer month = mycal.get(java.util.Calendar.MONTH) + 1;
        Integer year = mycal.get(java.util.Calendar.YEAR);
        if (inVacationDates == null) {
            inVacationDates = getVacationWithPlannedMap(year, month, employeeId, false);
        }
        Date nextDay = DateUtils.addDays(dayEndVacation, 1);
        mycal.setTime(nextDay);
        Integer nextMonth = mycal.get(java.util.Calendar.MONTH) + 1;
        if (inVacationDates.size() > 0) {
            if (inVacationDates.get(nextDay) != null && (inVacationDates.get(nextDay) == TYPICAL_DAY_MARK || inVacationDates.get(nextDay) == PREVIOUS_DAY_MARK)) {
                return nextDay;
            } else {
                return getNextWorkDay(nextDay, employeeId, nextMonth != month ? null : inVacationDates);
            }
        } else {
            return null;
        }

    }
}
