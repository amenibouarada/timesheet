package com.aplana.timesheet.controller;

import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.MonthReportForm;
import com.aplana.timesheet.service.monthreport.MonthReportService;
import com.aplana.timesheet.service.monthreport.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/monthreport*")
public class MonthReportController extends AbstractControllerForEmployee {

    @Autowired
    private MonthReportService monthReportService;
    @Autowired
    private OvertimeService overtimeService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm() {
        final ModelAndView modelAndView = new ModelAndView("monthreport/monthreport");

        fillMavForAddEmployeesForm(modelAndView);
        modelAndView.addObject(MonthReportForm.FORM, new MonthReportForm());
        modelAndView.addObject(AddEmployeeForm.ADD_FORM, new AddEmployeeForm());

        return modelAndView;
    }

    /**************************/
    /*     Блок "Табель"      */
    /**************************/
    @RequestMapping(value = "/getMonthReportData", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getMonthReport(
            @RequestParam("division") Integer division,
            @RequestParam("manager") Integer manager,
            @RequestParam("regions") String regions,
            @RequestParam("roles") String roles,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        try {
            return monthReportService.getMonthReportData(division, manager, regions, roles, year, month);
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    @RequestMapping(value = "/saveMonthReportTable", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getMonthReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("jsonData") String jsonData
    ) {
        try {
            monthReportService.saveMonthReportTable(year, month, jsonData);
        }catch (Exception exc){
            exc.printStackTrace();
            return "Во время сохранения произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.";
        }
        return "Сохранено успешно.";
    }


    /**************************/
    /*   Блок "Переработки"   */
    /**************************/
    @RequestMapping(value = "/saveOvertimeTable", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveOvertimeTable(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("jsonData") String jsonData
    ){
        try{
            overtimeService.saveOvertimeTable(year, month, jsonData);
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
            overtimeService.deleteOvertimes(jsonData);
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
            return overtimeService.getOvertimesJSON(year, month, divisionOwner, divisionEmployee);
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }


    /**************************/
    /*    Блок отчеты Excel   */
    /**************************/


}
