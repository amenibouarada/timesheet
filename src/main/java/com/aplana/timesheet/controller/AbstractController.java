package com.aplana.timesheet.controller;

import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: vsergeev
 * Date: 31.01.13
 */
public abstract class AbstractController {

    @Autowired
    protected HttpServletRequest request;

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

}
