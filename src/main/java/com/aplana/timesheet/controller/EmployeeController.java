package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static argo.jdom.JsonNodeFactories.*;

/**
 * Created by abayanov
 * Date: 20.08.14
 */

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

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

    @RequestMapping(value = "/employee/employeeListWithLastWorkday/{divisionId}/{filterFired}/{addDetails}", headers = "Accept=application/json", method = RequestMethod.GET)
    @ResponseBody
    public String getEmployeeListWithLastWorkdayForDivisionJson(@PathVariable("divisionId") Integer divisionId,
                                                                @PathVariable("filterFired") Boolean filterFired,
                                                                @PathVariable("addDetails") Boolean addDetails) {
        return employeeService.getEmployeeListWithLastWorkdayForDivisionJson(divisionId,filterFired, addDetails);
    }

    /* Возвращает JSON для формы выбора сотрудников */
    @RequestMapping(value="/employee/getAddEmployeeListAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showAddEmployeeList(@ModelAttribute(AddEmployeeForm.ADD_FORM) AddEmployeeForm form) {
        Date date;
        if (form.getYear() == null || form.getMonth() == null) {
            date = new Date();
        } else {
            date = DateTimeUtil.getLastDayOfAnyMonth(form.getYear(), form.getMonth());
        }
        List<Employee> employeeList = employeeService.getDivisionEmployeesByManager(
                form.getDivisionId(),
                date,
                form.getRegionListId(),
                form.getProjectRoleListId(),
                form.getManagerId());

        return employeeService.getEmployeeListAsJson(employeeList, true);
    }
}
