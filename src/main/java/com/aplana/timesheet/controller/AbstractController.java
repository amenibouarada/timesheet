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
        DateFormat dateFormat = new SimpleDateFormat(DateTimeUtil.DB_DATE_PATTERN);
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

}
