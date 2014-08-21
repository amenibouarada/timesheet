package com.aplana.timesheet.controller;

import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.helper.EmployeeHelper;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static argo.jdom.JsonNodeFactories.*;

/**
 * Created by abayanov
 * Date: 20.08.14
 */

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeHelper employeeHelper;

    @RequestMapping(value = "/employee/isDivisionLeader", headers = "Accept=application/json")
    @ResponseBody
    public String isDivisionLeader(@RequestParam("employeeId") Integer employeeId) {
        return JsonUtil.format(
                object(
                        field(
                                "isDivisionLeader",
                                employeeService.isEmployeeDivisionLeader(employeeId) ? trueNode() : falseNode())
                )
        );
    }

    @RequestMapping(value = "/employee/employeeListWithLastWorkday/{divisionId}", headers = "Accept=application/json", method = RequestMethod.GET)
    @ResponseBody
    public String getEmployeeListWithLastWorkdayForDivisionJson(@PathVariable("divisionId") Integer divisionId, HttpServletRequest request) {
        return employeeHelper.getEmployeeListWithLastWorkdayForDivisionJson(divisionId, employeeService.isShowAll(request), true);
    }
}
