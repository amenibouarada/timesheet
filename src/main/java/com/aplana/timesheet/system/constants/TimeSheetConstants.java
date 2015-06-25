package com.aplana.timesheet.system.constants;

/**
 * @author aimamutdinov
 */
public class TimeSheetConstants {

    /**
     * Имя куки для отоброжения всех пользователей в том числе и уволенных
     */
    public static final String COOKIE_SHOW_ALLUSER = "SHOW_ALLUSER";

    /**
     * Куки которая ставится для того чтобы запомнить человека
     */
    public static final String COOKIE_REMEMBER = "REMEMBER";
    public static final String POST_REMEMBER = "remember";
    public static final double WORK_DAY_DURATION = 8.0; //todo переделать в BigDecimal
    public static final String DOJO_PATH = "/resources/js/dojo-release-1.8.3";


    /**
     * Значение для параметра "Все"
     * Например, "Все сотрудники", "Все подразделения"
     */
    public static final Integer ALL_VALUES = 0;

    /**
     * Значение для параметра "Не выбрано"
     * Например, когда в Select-e (выпадашка) нет выбранных значений
     */
    public static final Integer NOT_CHOOSED = -1;

    /**
     * Значение по умолчанию для коэффициента "Взаимной занятости"
     */
    public static final double MUTUAL_WORK_COEFFICIENT = 1.15;
}
