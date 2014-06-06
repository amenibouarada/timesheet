package com.aplana.timesheet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by arozhkov on 02.06.2014.
 */
@Controller
public class PingController {

    @RequestMapping(value = "/ping", headers = "Accept=application/json;Charset=UTF-8")
    @ResponseBody
    public String pingReport() {
        return "Ok";
    }
}
