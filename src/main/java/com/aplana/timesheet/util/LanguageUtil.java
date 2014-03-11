package com.aplana.timesheet.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author pmakarov
 * @see <a href="">Аналитика</a>
 *      creation date: 11.03.14
 */
public class LanguageUtil {
    private static final List<Integer> singDayList = Arrays.asList(1);
    private static final List<Integer> plurDayList = Arrays.asList(0, 5, 6, 7, 8, 9);
    private static final List<Integer> geniDayList = Arrays.asList(2, 3, 4);

    /**
     * Выбор между дней/день/дня
     * @param dayNumber
     * @return
     */
    public static String getCaseDay(Integer dayNumber) {

        if (dayNumber >= 10 && dayNumber <= 20) {
            return "дней";
        }

        int end = dayNumber % 10;

        if (singDayList.contains(end)) {
            return "день";
        }

        if (plurDayList.contains(end)) {
            return "дней";
        }


        if (geniDayList.contains(end)) {
            return "дня";
        }

        return "день";
    }

    private static final List<Integer> singWeekAccList = Arrays.asList(1);
    private static final List<Integer> plurWeekAccList = Arrays.asList(0, 5, 6, 7, 8, 9);
    private static final List<Integer> geniWeekAccList = Arrays.asList(2, 3, 4);
    /**
     * Выбор между неделю/недели/недель
     * "Через 1 неделю", "Через 2 недели", "Через 3 недели" ...
     * @param weekNumber
     * @return
     */
    public static String getCaseWeekAccusative(Integer weekNumber) {

        if (weekNumber >= 10 && weekNumber <= 20) {
            return "недель";
        }

        int end = weekNumber % 10;

        if (singWeekAccList.contains(end)) {
            return "неделю";
        }

        if (plurWeekAccList.contains(end)) {
            return "недель";
        }


        if (geniWeekAccList.contains(end)) {
            return "недели";
        }

        return "недель";
    }

    private static final List<Integer> singWeekGenList = Arrays.asList(1);
    private static final List<Integer> plurWeekGenList = Arrays.asList(0, 5, 6, 7, 8, 9);
    /**
     * Выбор между неделю/недели/недель
     * "По истечении 1 недели", "По истечении 2 недель", "По истечении 3 недель" ...
     * @param weekNumber
     * @return
     */
    public static String getCaseWeekGenetive(Integer weekNumber) {

        if (weekNumber.equals(Integer.valueOf(11))) {
            return "недель";
        }

        int end = weekNumber % 10;

        if (singWeekGenList.contains(end)) {
            return "недели";
        }

        if (plurWeekGenList.contains(end)) {
            return "недель";
        }

        return "недель";
    }
}
