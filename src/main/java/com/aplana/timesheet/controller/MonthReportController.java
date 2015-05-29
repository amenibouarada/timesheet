package com.aplana.timesheet.controller;

import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.MonthReportForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/monthreport*")
public class MonthReportController extends AbstractControllerForEmployee {

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm() {
        final ModelAndView modelAndView = new ModelAndView("monthreport/monthreport");

        fillMavForAddEmployeesForm(modelAndView);
        modelAndView.addObject(MonthReportForm.FORM, new MonthReportForm());
        modelAndView.addObject(AddEmployeeForm.ADD_FORM, new AddEmployeeForm());
        //@ModelAttribute(MonthReportForm.FORM) MonthReportForm form

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView showTable(@ModelAttribute(MonthReportForm.FORM) MonthReportForm form) {
        ModelAndView mav = new ModelAndView("monthreport/monthreport");

        mav.addObject("here", "TEXT!!!");
        return mav;
    }

}
