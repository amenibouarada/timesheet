package com.aplana.timesheet.controller;

import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.MonthReportForm;
import com.aplana.timesheet.service.monthreport.MonthReportExcelService;
import com.aplana.timesheet.service.monthreport.MonthReportService;
import com.aplana.timesheet.service.monthreport.MutualWorkService;
import com.aplana.timesheet.service.monthreport.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(value = "/monthreport*")
public class MonthReportController extends AbstractControllerForEmployee {

    @Autowired
    private MonthReportService monthReportService;
    @Autowired
    private MonthReportExcelService monthReportExcelService;
    @Autowired
    private OvertimeService overtimeService;
    @Autowired
    private MutualWorkService mutualWorkService;

    private static String COMMON_ERROR_MESSAGE =
            "Во время %s произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.";
    private static String NO_PERMISSION_MESSAGE = "У вас нет прав на это действие";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm() {
        final ModelAndView modelAndView = new ModelAndView("monthreport/monthreport");

        fillMavForAddEmployeesForm(modelAndView);
        modelAndView.addObject(MonthReportForm.FORM, new MonthReportForm());
        modelAndView.addObject(AddEmployeeForm.ADD_FORM, new AddEmployeeForm());

        return modelAndView;
    }

    private boolean checkUserPermission(){
        return employeeService.isEmployeeAdmin(getCurrentUser().getId());
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
            return monthReportService.getMonthReportData(getCurrentUser(), division, manager, regions, roles, year, month);
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
            if (checkUserPermission()){
                monthReportService.saveMonthReportTable(year, month, jsonData);
            }else{
                return NO_PERMISSION_MESSAGE;
            }
        }catch (Exception exc){
            exc.printStackTrace();
            return String.format(COMMON_ERROR_MESSAGE, "сохранения");
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
            if (checkUserPermission()){
                overtimeService.saveOvertimeTable(year, month, jsonData);
            }else{
                return NO_PERMISSION_MESSAGE;
            }
        }catch (Exception exc){
            exc.printStackTrace();
            return String.format(COMMON_ERROR_MESSAGE, "сохранения");
        }
        return "Сохранено успешно.";
    }

    @RequestMapping(value = "/deleteOvertimes", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteOvertimes(
            @RequestParam("jsonData") String jsonData
    ){
        try{
            if (checkUserPermission()){
                overtimeService.deleteOvertimes(jsonData);
            }else{
                return NO_PERMISSION_MESSAGE;
            }
        }catch (Exception exc){
            exc.printStackTrace();
            return String.format(COMMON_ERROR_MESSAGE, "удаления");
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
            return overtimeService.getOvertimes(getCurrentUser(), year, month, divisionOwner, divisionEmployee);
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    /*********************************/
    /*   Блок "Взаимная занятость"   */
    /********************************/

    @RequestMapping(value = "/getMutualWorks", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getMutualWorks(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("regions") String regions,
            @RequestParam("divisionOwner") Integer divisionOwner,
            @RequestParam("divisionEmployee") Integer divisionEmployee,
            @RequestParam("projectId") Integer projectId
    ) {
        try {
            if ( ! checkUserPermission()){
                return NO_PERMISSION_MESSAGE;
            }
            return mutualWorkService.getMutualWorkData(year, month, regions, divisionOwner, divisionEmployee, projectId);
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    @RequestMapping(value = "/saveMutualWorkTable", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveMutualWorkTable(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("jsonData") String jsonData
    ){
        try{
            if ( ! checkUserPermission()){
                return NO_PERMISSION_MESSAGE;
            }
            mutualWorkService.saveMutualWorkTable(year, month, jsonData);
        }catch (Exception exc){
            exc.printStackTrace();
            return String.format(COMMON_ERROR_MESSAGE, "сохранения");
        }
        return "Сохранено успешно.";
    }

    @RequestMapping(value = "/prepareReport3Data", produces = "application/octet-stream")
    @ResponseBody
    public String prepareReport3Data(
            @RequestParam("beginDate") String beginDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("region") Integer region,
            @RequestParam("divisionOwner") Integer divisionOwner,
            @RequestParam("divisionEmployee") Integer divisionEmployee,
            @RequestParam("projectId") Integer projectId,
            @RequestParam("employeeId") Integer employeeId,
            HttpServletResponse response,
            HttpServletRequest request
    )throws JReportBuildError, IOException {
        if ( ! checkUserPermission()){
            return NO_PERMISSION_MESSAGE;
        }
        try{
            mutualWorkService.prepareReport3Data(beginDate, endDate, region, divisionOwner, divisionEmployee, projectId, employeeId, response, request);
        } catch (Exception exc){
            // ToDo
        }
        return "";
    }

    /**************************/
    /*    Блок отчеты Excel   */
    /**************************/

    @RequestMapping(value = "/makeOvertimeReport", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String makeOvertimeReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("divisionOwner") Integer divisionOwner,
            @RequestParam("divisionEmployee") Integer divisionEmployee,
            HttpServletResponse response)
    {
        try{
            if ( ! checkUserPermission()){
                return NO_PERMISSION_MESSAGE;
            }
            String [] headers = monthReportExcelService.makeOvertimeReport(year, month, divisionOwner, divisionEmployee);
            response.setContentType(headers[0]);
            response.setHeader("Content-Disposition",headers[1]);
            response.setHeader("Location", headers[2]);
        }catch(Exception exc){
            return handleExcelReportError(exc);
        }
        return "";
    }

    @RequestMapping(value = "/makeMonthReport", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String makeMonthReport(
            @RequestParam("division") Integer division,
            @RequestParam("manager") Integer manager,
            @RequestParam("regions") String regions,
            @RequestParam("roles") String roles,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            HttpServletResponse response) throws JReportBuildError
    {
        try{
            if ( ! checkUserPermission()){
                return NO_PERMISSION_MESSAGE;
            }
            String [] headers = monthReportExcelService.makeMonthReport(division, manager, regions, roles, year, month);
            response.setContentType(headers[0]);
            response.setHeader("Content-Disposition",headers[1]);
            response.setHeader("Location", headers[2]);
        }catch(Exception exc){
            return handleExcelReportError(exc);
        }
        return "";
    }

    @RequestMapping(value = "/makeMutualWorkReport" , produces = "text/plain;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public String makeMutualWorkReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("regions") String regions,
            @RequestParam("divisionOwner") Integer divisionOwner,
            @RequestParam("divisionEmployee") Integer divisionEmployee,
            @RequestParam("projectId") Integer projectId,
            HttpServletResponse response) throws JReportBuildError
    {
        try{
            if ( ! checkUserPermission()){
                return NO_PERMISSION_MESSAGE;
            }
            String [] headers = monthReportExcelService.makeMutualWorkReport(year, month, regions, divisionOwner, divisionEmployee, projectId);
            response.setContentType(headers[0]);
            response.setHeader("Content-Disposition",headers[1]);
            response.setHeader("Location", headers[2]);
        }catch(Exception exc){
            handleExcelReportError(exc);
        }
        return "";
    }

    private String handleExcelReportError(Exception exc){
        logger.error("Во время создания отчёта произошла ошибка: ", exc);
        return String.format(COMMON_ERROR_MESSAGE, "создания отчёта") + "\nОписание проблемы: " + exc.getMessage();
    }
}
