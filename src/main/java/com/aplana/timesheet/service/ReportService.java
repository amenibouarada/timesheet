package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pmakarov
 * creation date: 15.10.13
 */
@Service
public class ReportService {

    private static final String JIRA_URL = "http://jira.aplana.com/browse/";
    private static final Pattern PATTERN_TASK = Pattern.compile("\\b[a-zA-ZА-Яа-я0-9-/]+\\b");
    private static final Pattern PATTERN_JIRA_URL = Pattern.compile("(https?://)?([a-zA-ZА-Яа-я-]+).aplana.com((/browse/(\\w+-\\d+))|([a-zA-ZА-Яа-я-./!]+))?");

    @Autowired
    public ProjectDAO projectDAO;

    /**
     * Ищет в тексте URL-ы и придает им красивый вид:
     *      строка APLANATS-1242 заменяется на URL ведущий на jira
     *      ссылка на конфлуен http://conf.aplana.com превращается в кликабельный CONF
     *      ссылка на внутренний ресурс http://SOME_URL.aplana.com/ превращается в кликабельный SOME_URL
     * @param text
     * @return
     */
    @Transactional(readOnly = true)
    public String modifyURL(String text){
        String result;
        result = shorterLink(text);
        result = replaceJiraLink(result);
        return result;
    }
    /**
     * Обрабатывает входящую строку и добавляет в нее ссылку на JIRA'у
     *      Пример:
     *      Строка на вход: "APLANATS-1242 - При синхронизации не добавляется новый пользовтель"
     *      Строка на выход: "<a href='http://jira.aplana.com/browse/APLANATS-1242'>APLANATS-1242</a> - При синхронизации не добавляется новый пользовтель"
     *
     * @param text - строка комментариев
     * @return
     */

    @Transactional(readOnly = true)
    public String replaceJiraLink(String text){
        List<String> jiraKeyList = getJiraKey();
        Matcher matcher = PATTERN_TASK.matcher(text);

        // Важен порядок
        Map<Pair<Integer, Integer>, String> replaceMap = new LinkedHashMap<Pair<Integer, Integer>, String>();

        while(matcher.find()){
            String word = matcher.group(0);

            // Не заменять в уже существующих ссылках
            // TODO нормальную проверку на ссылки
            if (word.contains("/")){
                continue;
            }

            Integer indexStart = matcher.start();
            Integer indexEnd = matcher.end();

            for (String projectName : jiraKeyList){
                if (word.contains(projectName)){
                    StringBuffer link = new StringBuffer(128);

                    link.append("<a href='");
                    link.append(JIRA_URL);
                    link.append(word);
                    link.append("'>");
                    link.append(word);
                    link.append("</a>");

                    replaceMap.put(new Pair<Integer, Integer>(indexStart, indexEnd), link.toString());
                    break;
                }
            }
        }

        StringBuffer result = new StringBuffer(text);
        // При замене подстроки в строке, последующие индексы сдвигаются, offset - изменение
        Integer offset = 0;

        for(Map.Entry<Pair<Integer, Integer>, String> entry : replaceMap.entrySet()){
            Integer indexStart = entry.getKey().getFirst();
            Integer indexEnd = entry.getKey().getSecond();

            result.replace(offset + indexStart, offset + indexEnd, entry.getValue());

            offset += entry.getValue().length() - (indexEnd-indexStart);
        }

        return result.toString();
    }

    /**
     * Обрабатывает ссылки на внутренние ресурсы
     * "http://jira.aplana.com/browse/ITIASMK-398":
     *      преобразуется в -> "ITIASMK-398"
     * "http://conf.aplana.com/dashboard.action":
     *      преобразуется в -> "<a href='http://conf.aplana.com/dashboard.action'>CONF</a>":
     * "http://jira.aplana.com/secure/ContactAdministrators!default.jspa"
     *      преобразуется в -> "<a href='http://jira.aplana.com/secure/ContactAdministrators!default.jspa'>JIRA</a>"
     * @param text
     * @return
     */
    public String shorterLink(String text){
        Matcher matcher = PATTERN_JIRA_URL.matcher(text);

        // Важен порядок
        Map<Pair<Integer, Integer>, String> replaceMap = new LinkedHashMap<Pair<Integer, Integer>, String>();

        while(matcher.find()){
            // строка "бла бла http://jira.aplana.com/browse/SBRFNK-1584 бла бла"
            String url = matcher.group(0);      // "http://jira.aplana.com/browse/SBRFNK-1584"
            matcher.group(1);                   // "http://"
            String uri = matcher.group(2);      // "jira"
            matcher.group(3);                   // "/browse/SBRFNK-1584"
            matcher.group(4);                   // "/browse/SBRFNK-1584"
            String task = matcher.group(5);     // "ITIASMK-398"
            matcher.group(6);                   // null

            Integer indexStart = matcher.start();
            Integer indexEnd = matcher.end();

            StringBuffer word = new StringBuffer(128);
            if (task != null){
                word.append(task);
            } else {
                // Для конфлуенса или других внутренних ресурсов
                word.append("<a href='");
                word.append(url);
                word.append("'>");
                word.append(uri.toUpperCase());
                word.append("</a>");
            }

            replaceMap.put(new Pair<Integer, Integer>(indexStart, indexEnd), word.toString());
        }

        StringBuffer result = new StringBuffer(text);
        // При замене подстроки в строке, последующие индексы сдвигаются, offset - изменение
        Integer offset = 0;

        for(Map.Entry<Pair<Integer, Integer>, String> entry : replaceMap.entrySet()){
            Integer indexStart = entry.getKey().getFirst();
            Integer indexEnd = entry.getKey().getSecond();

            result.replace(offset + indexStart, offset + indexEnd, entry.getValue());

            offset += entry.getValue().length() - (indexEnd-indexStart);
        }

        return result.toString();
    }

    @Transactional(readOnly = true)
    public List<String> getJiraKey(){
        List<String> result = new ArrayList<String>();

        List<String> jiraKeyList = projectDAO.getJiraKeyList();
        for(String keys : jiraKeyList){
            String[] splitKey = keys.split(",");
            for(int i=0; i<splitKey.length; ++i){
                result.add(splitKey[i].trim());
            }
        }

        return result;
    }
}
