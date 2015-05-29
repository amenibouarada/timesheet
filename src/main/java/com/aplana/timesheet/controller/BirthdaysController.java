package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.AdminProjectsForm;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * User: iziyangirov
 * Date: 13.08.14
 */
@Controller
public class BirthdaysController extends AbstractControllerForEmployee {

    private ModelAndView createMAV(Division division, Integer month){
        ModelAndView mav = new ModelAndView("birthdays");
        mav.addObject("currentMonth", month);
        mav.addObject("currentDivision", division.getId());
        mav.addObject("divisionList", divisionService.getDivisions());
        mav.addObject("employeesForSelectedMonth",
                employeeService.getEmployeesForDivisionWithBirthdayMonth(division, month));
        return mav;
    }

    @RequestMapping(value = "/birthdays")
    public ModelAndView showBirthdaysDefault() {
        Division currEmpDivision = getCurrentUser().getDivision();
        Integer month = DateTimeUtil.getMonth(new Date()) + 1;
        return createMAV(currEmpDivision, month);
    }

    @RequestMapping(value = "/birthdays/{divisionId}/{month}")
    public ModelAndView showBirthdays(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("month") Integer month
    ) {
        Division division = divisionService.find(divisionId);
        return createMAV(division, month);
    }
}
