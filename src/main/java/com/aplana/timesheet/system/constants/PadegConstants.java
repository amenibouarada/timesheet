package com.aplana.timesheet.system.constants;

/**
 * User: abayanov
 * Date: 20.06.13
 */
public class PadegConstants {
    public static final int Imenitelnyy = 1;
    public static final int Roditelnyy = 2;
    public static final int Datelnyy = 3;
    public static final int Vinitelnyy = 4;
    public static final int Tvoritelnyy = 5;
    public static final int Predlojnyy = 6;

    public static String padegToString(Integer padeg) {
        switch (padeg) {
            case Imenitelnyy:
                return "Imenitelnyy";
            case Roditelnyy:
                return "Roditelnyy";
            case Datelnyy:
                return "Datelnyy";
            case Vinitelnyy:
                return "Vinitelnyy";
            case Tvoritelnyy:
                return "Tvoritelnyy";
            case Predlojnyy:
                return "Predlojnyy";
            default:
                return null;
        }
    }
}
