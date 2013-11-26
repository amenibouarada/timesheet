package com.aplana.timesheet.controller;

import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * User: vsergeev
 * Date: 31.01.13
 */
public abstract class AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractController.class);

    @Autowired
    protected HttpServletRequest request;


    @Autowired
    protected HttpSession session;

    /**
     * формат передачи даты между моделью и формой
     */

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.initDirectFieldAccess();
        DateFormat dateFormat = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN);
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

    /**
     * Сохраняет в сессии: URI и параметы GET/POST запроса
     * @param uniqueSavepointName - уникальное значение для идентификации
     */

    public void savepoint(String uniqueSavepointName){
        Map<String, Object> parameterMap = request.getParameterMap();
        Pair<String, Map> link = new Pair(request.getRequestURI(), parameterMap);
        session.setAttribute(uniqueSavepointName, link);

        logger.debug("savepoint["+uniqueSavepointName+"]" + link);
    }


    /**
     * Возвращает ModelAndView с редиректом на ранее сохраненную точку
     * @param uniqueSavepointName - уникальное значение для идентификации
     * @return - ModelAndView
     */
    public ModelAndView redirectTo(String uniqueSavepointName){
        Pair<String, Map> link = (Pair<String, Map>)session.getAttribute(uniqueSavepointName);
        String uri = link.getFirst();
        Map<String, Object> params = link.getSecond();

        ModelAndView modelAndView = new ModelAndView("redirect:" + uri);
        if (params!=null){
            modelAndView.addAllObjects(params);
        }

        logger.debug("redirectTo["+uniqueSavepointName+"]" + link);

        return modelAndView;
    }
}
