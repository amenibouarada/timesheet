package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.CalendarDAO;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Holiday;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.exception.TSRuntimeException;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import com.aplana.timesheet.util.DateNumbers;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.*;

@Service
public class CalendarService {
    @Autowired
    private CalendarDAO calendarDAO;

    /**
     * Ищет в базе данных запись соответсвующую указанной дате в виде строки.
     *
     * @param String date Дата в виде строки.
     * @return объект типа Calendar, либо null, если объект не найден.
     */
    @Transactional(readOnly = true)
    public Calendar find(String date) {
        return calendarDAO.find(DateTimeUtil.stringToTimestamp(date, DateTimeUtil.DATE_PATTERN));
    }

    /**
     * Ищет в базе данных запись соответсвующую указанной дате в миллисекундах.
     *
     * @param long date Дата в миллисекундах.
     * @return объект типа Calendar, либо null, если объект не найден.
     */
    @Transactional(readOnly = true)
    public Calendar find(long date) {
        return calendarDAO.find(new Timestamp(date));
    }

    /**
     * Ищет в базе данных запись соответсвующую указанной дате в Timestamp.
     *
     * @param Timestamp date Дата.
     * @return объект типа Calendar, либо null, если объект не найден.
     */
    @Transactional(readOnly = true)
    public Calendar find(Timestamp date) {
        return calendarDAO.find(date);
    }

    /**
     * Формирует список годов, которые есть в таблице списания занятости.
     *
     * @return список List Calendar.
     */
    public List<Calendar> getYearsList() {
        Calendar minYear = calendarDAO.getMinDateList();
        int maxYear = calendarDAO.getMaxDateList().getYear();

        List<Calendar> yearList = new ArrayList<Calendar>();
        yearList.add(minYear);
        if (minYear.getYear() != maxYear) {
            for (int i = minYear.getYear() + 1; i <= maxYear; i++) {
                yearList.add(new Calendar(i, 1, calendarDAO.getMonthTxt(1)));
            }
        }
        return yearList;
    }

    /**
     * Формирует список месяцев, соответствующие годам, которые есть в системе.
     *
     * @return список List Calendar.
     */
    public List<Calendar> getMonthList(Integer year) {
        List<Calendar> monthList = new ArrayList<Calendar>();
        List<Integer> tempList = calendarDAO.getMonth(year);
        for (Integer aTempList : tempList) {
            monthList.add(new Calendar(year, aTempList, calendarDAO.getMonthTxt(aTempList)));
        }
        return monthList;
    }

    /**
     * Формирует список дней для одного месяца.
     *
     * @param year
     * @param month
     * @return
     */
    @Transactional(readOnly = true)
    public List<Calendar> getDateList(Integer year, Integer month) {
        return calendarDAO.getDateList(year, month);
    }

    /**
     * Проверяет год на наличие в системе
     * param year
     * return true если год существует в системе
     * return false если год не существует в системе
     */
    public boolean yearValid(Integer year) {
        return calendarDAO.yearValid(year);
    }

    /**
     * Проверяет месяц на наличие в системе
     *
     * @param year
     * @param month
     * @return
     */
    public boolean monthValid(Integer year, Integer month) {
        return calendarDAO.monthValid(year, month);
    }

    public Calendar getLastWorkDay(Calendar day) {
        return calendarDAO.getLastWorkDay(day);
    }

    public Calendar getNextWorkDay(Calendar day, Region region) {
        return calendarDAO.getNextWorkDay(day, region);
    }

    /**
     * Возвращает количество выходных дней за выбранный период для конкретного региона
     */
    public Integer getHolidaysCountForRegion(Date beginDate, Date endDate, Region region) {
        return calendarDAO.getHolidaysCountForRegion(beginDate, endDate, region);
    }

    /**
     * получаем мапу для периода
     * ключ - год в периоде
     * значения - месяцы, соответствующие году (ключу), попадающие в заданный период
     */
    public HashMap<Integer, Set<Integer>> getMonthsAndYearsNumbers(Date beginDate, Date endDate) {
        final DateNumbers startDateNumbers = new DateNumbers(beginDate);
        final DateNumbers endDateNumbers = new DateNumbers(endDate);
        HashMap<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();

        for (int year = startDateNumbers.getYear(); year <= endDateNumbers.getYear(); year++) {
            Set<Integer> monthsInYear = Sets.newHashSet(calendarDAO.getMonth(year));
            if (monthsInYear.isEmpty()) {
                throw new TSRuntimeException(new CalendarServiceException("Попытка получения месяцев из года, который еще не занесен в БД!"));
            }

            if (startDateNumbers.getYear() == year) {
                monthsInYear = Sets.newHashSet(Iterables.filter(monthsInYear, new Predicate<Integer>() {
                    @Override
                    public boolean apply(@Nullable Integer input) {
                        return input >= startDateNumbers.getDatabaseMonth();
                    }
                }));
            }
            if (endDateNumbers.getYear() == year) {
                monthsInYear = Sets.newHashSet(Iterables.filter(monthsInYear, new Predicate<Integer>() {
                    @Override
                    public boolean apply(@Nullable Integer input) {
                        return input <= endDateNumbers.getDatabaseMonth();
                    }
                }));
            }
            result.put(year, monthsInYear);
        }

        return result;
    }


    public int getWorkDaysCountForRegion(Region region, Integer year, Integer month, @NotNull Date fromDate) {
        return calendarDAO.getWorkDaysCountForRegion(region, year, month, fromDate);
    }

    public int getWorkDaysCountForRegion(Region region, Integer year, Integer month,
                                         @Nullable Date fromDate, @Nullable Date toDate) {
        return calendarDAO.getWorkDaysCountForRegion(region, year, month, fromDate, toDate);
    }

    public int getEmployeeRegionWorkDaysCount(Employee employee, Integer year, Integer month) {
        return getWorkDaysCountForRegion(employee.getRegion(), year, month, employee.getStartDate(), employee.getEndDate());
    }

    // если регион = нул - возвращает общий для всех
    public List<Holiday> getHolidaysForRegion(Date minDate, Date maxDate, Region region) {
        if (region == null){
            return calendarDAO.getHolidaysInInterval(minDate, maxDate);
        }
        return calendarDAO.getHolidaysForRegion(minDate, maxDate, region);
    }

    /**
     * Возвращает список праздничных дней только для указанного региона
     * @param dateFrom
     * @param dateTo
     * @param region
     * @return
     */
    public List<Holiday> getHolidaysOnlyForRegion(Date dateFrom, Date dateTo, Region region) {
        if (region == null){
            return calendarDAO.getHolidaysInInterval(dateFrom, dateTo);
        }
        return calendarDAO.getHolidaysOnlyForRegion(dateFrom, dateTo, region);
    }

    public boolean isHoliday(Date date, Employee employee) {
        return (getHolidaysCountForRegion(date, date, (employee == null) ? null : employee.getRegion()) > 0);
    }

    /**
     * Возвращает максимальную дату месяца
     * @param year
     * @param month
     * @return
     */
    public Date getMaxDateMonth(Integer year, Integer month) {
        return calendarDAO.tryGetMaxDateMonth(year,month);
    }

    /**
     * Возвращает минимальную дату месяца
     * @param year
     * @param month
     * @return
     */
    public Date getMinDateMonth(Integer year, Integer month) {
        return calendarDAO.tryGetMinDateMonth(year,month);
    }

    /**
     * Возвращает год из даты
     * @param date
     * @return
     */
    public Integer getYearFromDate(Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
        calendar.setTime(date);
        return calendar.get(java.util.Calendar.YEAR);
    }

    /**
     * Возвращает мясяц из даты
     * @param date
     * @return
     */
    public Integer getMonthFromDate(Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
        calendar.setTime(date);
        return calendar.get(java.util.Calendar.MONTH)+1;
    }


    public int getCountWorkDayPriorDate(Region region, Integer year, Integer month, @NotNull Date toDate) {
        return calendarDAO.getCountWorkDayPriorDate(region, year, month, toDate);
    }
}