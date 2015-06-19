package com.aplana.timesheet.util;

public class NumberUtils {

    public static Double getDoubleValue(Object value){
        if (value instanceof Integer){
            return new Double((Integer)value);
        }
        if (value instanceof String){
            if (value == "null"){
                return null;
            }
            return new Double((String)value);
        }
        return (Double)value;
    }
}
