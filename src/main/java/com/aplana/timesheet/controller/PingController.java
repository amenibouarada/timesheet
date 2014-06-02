package com.aplana.timesheet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by arozhkov on 02.06.2014.
 */
@Controller
public class PingController {

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public void pingReport() {

    }
}
