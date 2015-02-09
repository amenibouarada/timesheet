package com.aplana.timesheet.util;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.exception.TSRuntimeException;
import com.aplana.timesheet.service.CalendarService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;
import static argo.jdom.JsonNodeFactories.string;

public class DateTimeUtil {
    public static final long DAY_IN_MILLS = 86400000;
    public static final long THREE_MONTHS_IN_MILLS = 90 * DAY_IN_MILLS;
    public static final String DB_DATE_PATTERN = "yyyy-MM-dd";
    public static final String VIEW_DATE_PATTERN = "dd.MM.yyyy";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String VIEW_DATE_TIME_PATTERN = VIEW_DATE_PATTERN.concat(" ").concat(TIME_PATTERN);
    public static final String MIN_DATE = "1900-01-01";
    public static final String MAX_DATE = "2999-12-31";
    private static final SimpleDateFormat VIEW_SIMPLE_DATE_FORMAT = new SimpleDateFormat(VIEW_DATE_PATTERN);
    private static final SimpleDateFormat DB_SIMPLE_DATE_FORMAT = new SimpleDateFormat(DB_DATE_PATTERN);

    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

    /**
     * Преобразует строку даты в указанном формате в объект класса {@link Date}.
     *
     * @param date       - строка даты.
     * @param dateFormat - формат даты ({@link SimpleDateFormat})
     * @return объект класса {@link Date}.
     */
    public static Date stringToDate(String date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        if (date == null || date.isEmpty()) {
            logger.error("Error while parsing empty string into date");
            return null;
        }
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            logger.error("Error while parsing date in string format.", e);
        }
        return null;
    }

    /**
     * Преобразует строку дату формата БД
     *
     * @param date - строка даты.
     * @return объект класса {@link Date}.
     */
    public static Date parseStringToDateForDB(String date) {
        if (date == null || date.isEmpty()) {
            logger.error("Error while parsing empty string into date");
            throw new TSRuntimeException(new Exception("Date is null"));
        }
        try {
            return DB_SIMPLE_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            logger.error("Error while parsing date in string format.", e);
            throw new TSRuntimeException(e);
        }
    }

    /**
     * Преобразует строку дату формата просмотра
     *
     * @param date - строка даты.
     * @return объект класса {@link Date}.
     */
    public static Date parseStringToDateForView(String date) {
        if (date == null || date.isEmpty()) {
            logger.error("Error while parsing empty string into date");
            throw new TSRuntimeException(new Exception("Date is null"));
        }
        try {
            return VIEW_SIMPLE_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            logger.error("Error while parsing date in string format.", e);
            throw new TSRuntimeException(e);
        }
    }



    public static String formatDateIntoDBFormat(Timestamp timestamp) {
        return DB_SIMPLE_DATE_FORMAT.format(timestamp);
    }

    public static String formatDateIntoDBFormat(Date date) {
        return DB_SIMPLE_DATE_FORMAT.format(date);
    }

    public static String formatDateIntoViewFormat(Timestamp timestamp) {
        return VIEW_SIMPLE_DATE_FORMAT.format(timestamp);
    }

    public static String formatDateIntoViewFormat(Date date) {
        return VIEW_SIMPLE_DATE_FORMAT.format(date);
    }

    /**
     * Преобразует строку даты в указанном формате в объект класса {@link Timestamp}.
     *
     * @param date       - строка даты.
     * @param dateFormat - формат даты ({@link SimpleDateFormat})
     * @return объект класса {@link Timestamp}.
     */
    public static Timestamp stringToTimestamp(String date, String dateFormat) {
        return new Timestamp(stringToDate(date, dateFormat).getTime());
    }

    /**
     * Преобразует строку даты из формата ldap в Timestamp
     *
     * @param ldapDate строка даты в формате ldap (yyyymmdd)
     * @return датa в формате Timestamp
     */
    public static Timestamp ldapDateToTimestamp(String ldapDate) {
        String year = ldapDate.substring(0, 4);
        String month = ldapDate.substring(4, 6);
        String day = ldapDate.substring(6, 8);
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("-").append(month).append("-").append(day);
        return stringToTimestamp(sb.toString(), DB_DATE_PATTERN);
    }

    /**
     * Преобразует строку даты из формата yyyy-MM-dd в формат dd.MM.yyyy
     *
     * @param d строка даты в формате yyyy-MM-dd
     * @return строка даты в формате dd.MM.yyyy
     */
    public static String formatDateString(String d) {
        String[] date = StringUtils.split(d, "-");
        return new StringBuilder()
                .append(date[2])
                .append(".")
                .append(date[1])
                .append(".")
                .append(date[0])
                .toString();
    }

    /**
     * Преобразует дату из строки в Timestamp
     *
     * @param date
     * @return Timestamp
     */
    public static Timestamp stringToTimestamp(String date) {
        return new Timestamp(stringToTimestamp(date, DB_DATE_PATTERN).getTime());
    }

    /**
     * Возвращает первый день текущего месяца
     *
     * @return String
     */
    public static String currentMonthFirstDay() {
        return dateToString(currentMonthFirstDayDate());
    }

    public static Date currentMonthFirstDayDate() {
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * Возвращает последний день текущего года
     *
     * @return String
     */
    public static String currentYearLastDay() {
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        return DB_SIMPLE_DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * Возвращает первый день прошлого месяца
     *
     * @return String
     */
    public static String previousMonthFirstDay() {
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return DB_SIMPLE_DATE_FORMAT.format(calendar.getTime());
    }

    public static Date previousMonthFirstDayDate(Date targetDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * Возвращает текущий день
     *
     * @return String
     */
    public static String currentDay() {
        return dateToString(new Date());
    }

    /**
     * Возвращает последнее воскресенье
     *
     * @return String
     */
    public static String lastSunday() {
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            } else
                calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        String format = DB_SIMPLE_DATE_FORMAT.format(calendar.getTime());
        logger.debug("Last Sunday " + format);
        return format;
    }

    public static Date lastSundayDate(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            } else
                calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        return calendar.getTime();
    }

    public static Date previousMonthLastDayDate(Date targetDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        while (calendar.get(Calendar.DAY_OF_MONTH) != calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            } else
                calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        return calendar.getTime();
    }

    /**
     * Возвращает конец текущего месяца
     *
     * @return String
     */
    public static String getLastDayOfMonth(Timestamp day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        String format = DB_SIMPLE_DATE_FORMAT.format(calendar.getTime());
        logger.debug("End current month day " + format);
        return format;
    }

    public static Date currentMonthLastDayDate(Date targetDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * Возвращает название текущего месяца в виде строки
     *
     * @param dateString
     * @return String
     */
    public static String getMonthTxt(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
        Date date = new Date();
        date.setTime(DateTimeUtil.stringToTimestamp(dateString).getTime());
        return sdf.format(date);
    }

    /**
     * Возвращает номер месяца в переданной дате
     *
     * @param date
     * @return
     */
    public static Integer getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    /**
     * Возвращает год в переданной дате
     *
     * @param date
     * @return
     */
    public static Integer getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * Cравнивает даты, true если первая дата позже
     *
     * @param firstDate
     * @param secondDate
     * @return
     */
    public static boolean dayAfterDay(String firstDate, String secondDate) {
        return stringToTimestamp(firstDate).after(stringToTimestamp(secondDate));
    }

    public static String decreaseDay(String day) {
        return addDaysToStringDate(day, -1);
    }

    public static String increaseDay(String day) {
        return addDaysToStringDate(day, 1);
    }

    private static String addDaysToStringDate(String dateStr, int daysCount) {
        try {
            Date date = DB_SIMPLE_DATE_FORMAT.parse(dateStr);
            date = DateUtils.addDays(date, daysCount);

            return DB_SIMPLE_DATE_FORMAT.format(date);

        } catch (ParseException ex) {
            throw new TSRuntimeException(ex);
        }
    }

    public static Boolean isDateValid(String date) {
        try {
            DB_SIMPLE_DATE_FORMAT.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    public static Boolean isPeriodValid(String strDateBegin, String strDateEnd) {
        try {
            Date dateBeg = DB_SIMPLE_DATE_FORMAT.parse(strDateBegin);
            Date dateEnd = DB_SIMPLE_DATE_FORMAT.parse(strDateEnd);
            return dateBeg.before(dateEnd) || dateBeg.equals(dateEnd);
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Возвращает List годов, существующих в системе
     *
     * @return List<Calendar>
     */
    public static List<com.aplana.timesheet.dao.entity.Calendar> getYearsList(CalendarService calendarService) {
        List<com.aplana.timesheet.dao.entity.Calendar> yearsList = calendarService.getYearsList();
        logger.info(yearsList.toString());
        return yearsList;
    }

    /**
     * Возвращает List месяцев, существующих в системе
     *
     * @param years
     * @return String
     */
    public static String getMonthListJson(List<com.aplana.timesheet.dao.entity.Calendar> years, CalendarService calendarService) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (com.aplana.timesheet.dao.entity.Calendar year : years) {
            List<com.aplana.timesheet.dao.entity.Calendar> months = calendarService.getMonthList(year.getYear());

            final JsonArrayNodeBuilder monthsBuilder = anArrayBuilder();

            if (months.isEmpty()) {
                monthsBuilder.withElement(
                        anObjectBuilder().
                                withField("number", JsonUtil.aNumberBuilder(0)).
                                withField("value", string(StringUtils.EMPTY))
                );
            } else {
                for (com.aplana.timesheet.dao.entity.Calendar month : months) {
                    monthsBuilder.withElement(
                            anObjectBuilder().
                                    withField("number", JsonUtil.aNumberBuilder(month.getMonth())).
                                    withField("name", aStringBuilder(month.getMonthTxt()))
                    );
                }

            }

            builder.withElement(
                    anObjectBuilder().
                            withField("year", JsonUtil.aNumberBuilder(year.getYear())).
                            withField("months", monthsBuilder)
            );
        }

        return JsonUtil.format(builder);
    }

    public static String dateToString(Date date) {
        return DB_SIMPLE_DATE_FORMAT.format(date);
    }

    public static String dateToString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static Date createDate(int year, int month) {
        final Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.YEAR, year);

        return calendar.getTime();
    }

    /**
     * Возвращает количество дней за период
     */
    public static Long getAllDaysCount(Date beginDate, Date endDate) {
        return (endDate.getTime() - beginDate.getTime()) / DAY_IN_MILLS + 1;
    }

    /**
     * @param year
     * @param month (from 1)
     * @return
     */
    public static Calendar getCalendar(int year, int month) {
        final Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        return calendar;
    }

    public static String getOnlyDate(Date date) {
        return VIEW_SIMPLE_DATE_FORMAT.format(date);
    }


    public static List<String> dateListToStringList(List<Date> dateList) {
        List<String> stringList = new ArrayList<String>(dateList.size());
        for (Date date : dateList) {
            stringList.add(dateToString(date, DB_DATE_PATTERN));
        }
        return stringList;
    }


    public static int getDiffInDays(Date beginDate, Date endDate) {
        return (int) ((endDate.getTime() - beginDate.getTime()) / (24 * 3600 * 1000) + 1);
    }

    public static int getDiffInMonths(Date beginDate, Date endDate){
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(beginDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        return diffMonth;
    }

    private static String[] months = new String[] {"января", "февраля", "марта", "апреля", "мая", "июня", "июля",
            "августа", "сентября", "октября", "ноября", "декабря"};

    /**
     * Возвращает дату в виде дня и месяца (пример: 1 октября)
     * @param timestamp
     * @return String
     */
    public static String getDayMonthFromDate(Timestamp timestamp){
        if (timestamp == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        return String.format("%s %s",calendar.get(Calendar.DATE), months[calendar.get(Calendar.MONTH)]);
    }
}