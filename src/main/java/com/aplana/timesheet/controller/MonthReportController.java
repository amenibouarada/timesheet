package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.MonthReportForm;
import com.aplana.timesheet.service.MonthReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/monthreport*")
public class MonthReportController extends AbstractControllerForEmployee {

    @Autowired
    private MonthReportService monthReportService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm() {
        final ModelAndView modelAndView = new ModelAndView("monthreport/monthreport");

        fillMavForAddEmployeesForm(modelAndView);
        modelAndView.addObject(MonthReportForm.FORM, new MonthReportForm());
        modelAndView.addObject(AddEmployeeForm.ADD_FORM, new AddEmployeeForm());
        //@ModelAttribute(MonthReportForm.FORM) MonthReportForm form

        final List<Calendar> yearList = monthReportService.getYearsList();
        modelAndView.addObject("yearList", yearList);
        modelAndView.addObject("monthList", calendarService.getMonthList(2015));// ToDo


        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView showTable(@ModelAttribute(MonthReportForm.FORM) MonthReportForm form) {
        ModelAndView mav = new ModelAndView("monthreport/monthreport");

        mav.addObject("here", "TEXT!!!");
        return mav;
    }

    @RequestMapping(value = "/saveOvertimeTable", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveOvertimeTable(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("jsonData") String jsonData
    ){
        try{
            monthReportService.saveOvertimeTable(year, month, jsonData);
        }catch (Exception exc){
            exc.printStackTrace();
            return "Во время сохранения произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.";
        }
        return "Сохранено успешно.";
    }

    @RequestMapping(value = "/deleteOvertimes", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteOvertimes(
            @RequestParam("jsonData") String jsonData
    ){
        try{
            monthReportService.deleteOvertimes(jsonData);
        }catch (Exception exc){
            exc.printStackTrace();
            return "Во время удаления произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.";
        }
        return "Строки успешно удалены";
    }

    @RequestMapping(value = "/getOvertimes", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getOvertimes(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("divisionOwner") Integer divisionOwner,
            @RequestParam("divisionEmployee") Integer divisionEmployee
    ) {
        try {
            return monthReportService.getOvertimesJSON(year, month, divisionOwner, divisionEmployee);
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }



}
