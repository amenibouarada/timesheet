package com.aplana.timesheet.util;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class StringUtil {

    public static String toUTF8String(String s) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255 && !Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                byte[] b;
                b = Character.toString(c).getBytes("utf-8");
                for (byte aB : b) {
                    int k = aB;
                    if (k < 0) k += 256;
                    sb.append("%").append(Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    public static List<Integer> stringToList(String inputString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Integer> outputList = mapper.readValue(inputString, List.class);
        return outputList;
    }
}
