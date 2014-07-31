package com.aplana.timesheet.util;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.*;
import argo.saj.InvalidSyntaxException;

import java.util.List;
import java.util.Locale;
import java.text.*;

import static argo.jdom.JsonNodeBuilders.anArrayBuilder;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class JsonUtil {

    public static final String NUMBER_FORMAT = "%.2f";

    private final static JsonFormatter JSON_FORMATTER = new CompactJsonFormatter();
    private final static JdomParser JDOM_PARSER = new JdomParser();

    public static String format(JsonNodeBuilder<JsonRootNode> builder) {
        return format(builder.build());
    }

    public static String format(JsonRootNode rootNode) {
        return JSON_FORMATTER.format(rootNode).replace("\\\"", "&quot;");
    }


    public static JsonRootNode parse(String json) throws InvalidSyntaxException {
        return JDOM_PARSER.parse(json);
    }

    public static JsonNodeBuilder aNumberBuilder(Number i) {
        return JsonNodeBuilders.aNumberBuilder(formatNumber(i));
    }

    private static String formatNumber(Number i) {
        if (i.getClass() == Double.class || i.getClass() == Float.class) {
            return String.format(Locale.ROOT, NUMBER_FORMAT, i.doubleValue());
        }

        return String.valueOf(i);
    }

    public static String toJsonIntegerArray(List<Integer> list) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Integer integer : list) {
            builder.withElement(aNumberBuilder(integer));
        }

        return format(builder);
    }

    public static Integer getDecNumberValue(JsonNode jsonNode, String field) {
        return Integer.valueOf(jsonNode.getNumberValue(field));
    }

    public static Double getFloatNumberValue(JsonNode jsonNode, String field) {
        return Double.valueOf(jsonNode.getNode(field).getText().replace(',', '.'));
    }

    public static JsonNodeBuilder aStringBuilder(Number n) {
        return argo.jdom.JsonNodeBuilders.aStringBuilder(
                n != null ? n.toString() : "0"
        );
    }

    public static JsonNodeBuilder aStringBuilder(Boolean b) {
        return argo.jdom.JsonNodeBuilders.aStringBuilder(
                b != null ? b.toString() : ""
        );
    }

    /**
     * Возвращает строку ы виде "n/m"
     * @param n
     * @param m
     * @return "n / m"
     */
    public static JsonNodeBuilder aNumberBuilder(Double n, Double m) {
        return argo.jdom.JsonNodeBuilders.aStringBuilder(formatDouble(n) +"/"+ formatDouble(m));
    }

    public static String formatDouble(Double d){
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        return format.format(d);
    }

}
