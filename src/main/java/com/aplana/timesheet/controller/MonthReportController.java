package com.aplana.timesheet.controller;

import com.aplana.timesheet.enums.MonthReportStatusEnum;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.MonthReportForm;
import com.aplana.timesheet.service.monthreport.MonthReportExcelService;
import com.aplana.timesheet.service.monthreport.MonthReportService;
import com.aplana.timesheet.service.monthreport.MutualWorkService;
import com.aplana.timesheet.service.monthreport.OvertimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(value = "/monthreport*")
public class MonthReportController extends AbstractControllerForEmployee {

    private static final Logger logger = LoggerFactory.getLogger(MonthReportController.class);

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
            // ToDo поправить обработку исключений. Вынеся отдельные повторяющиеся сообщения в константы
            logger.error("Во время запроса данных для табеля произошла ошибка!", e);
            return "[]";
        }
    }

    @RequestMapping(value = "/saveMonthReportTable", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveMonthReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("jsonData") String jsonData
    ) {
        try {
            if (employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                monthReportService.saveMonthReportTable(year, month, jsonData);
            }else{
                return NO_PERMISSION_MESSAGE;
            }
        }catch (Exception exc){
            logger.error("Во время сохранения табеля произошла ошибка!", exc);
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
            if (employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                overtimeService.saveOvertimeTable(year, month, jsonData);
            }else{
                return NO_PERMISSION_MESSAGE;
            }
        }catch (Exception exc){
            logger.error("Во время сохранения таблицы переработок произошла ошибка!", exc);
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
            if (employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                overtimeService.deleteOvertimes(jsonData);
            }else{
                return NO_PERMISSION_MESSAGE;
            }
        }catch (Exception exc){
            logger.error("Во время удаления данных из таблицы переработок произошла ошибка!", exc);
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
            logger.error("Во время запроса данных для таблицы переработок произошла ошибка!", e);
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
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return NO_PERMISSION_MESSAGE;
            }
            return mutualWorkService.getMutualWorkData(year, month, regions, divisionOwner, divisionEmployee, projectId);
        } catch (IOException e) {
            logger.error("Во время запроса данных для таблицы 'Взаимная занятость' произошла ошибка!", e);
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
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return NO_PERMISSION_MESSAGE;
            }
            mutualWorkService.saveMutualWorkTable(year, month, jsonData);
        }catch (Exception exc){
            logger.error("Во время сохранения данных таблицы 'Взаимная занятость' произошла ошибка!", exc);
            return String.format(COMMON_ERROR_MESSAGE, "сохранения");
        }
        return "Сохранено успешно.";
    }

    @RequestMapping(value = "/prepareReport3Data/{divisionOwner}/{divisionEmployee}/{region}/{employeeId}/{projectId}/{beginDate}/{endDate}")
    public ModelAndView prepareReport3Data(
            @PathVariable("beginDate") String beginDate,
            @PathVariable("endDate") String endDate,
            @PathVariable("region") Integer region,
            @PathVariable("divisionOwner") Integer divisionOwner,
            @PathVariable("divisionEmployee") Integer divisionEmployee,
            @PathVariable("projectId") Integer projectId,
            @PathVariable("employeeId") Integer employeeId,
            HttpServletResponse response,
            HttpServletRequest request
    )throws JReportBuildError, IOException {
        ModelAndView mav = new ModelAndView("/monthreport/monthreport");
        if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
            ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
            errorMav.addObject("cause", NO_PERMISSION_MESSAGE);
            return errorMav;
        }
        try{
            mutualWorkService.prepareReport3Data(beginDate, endDate, region, divisionOwner, divisionEmployee, projectId, employeeId, response, request);
        } catch (Exception exc){
            logger.error("Во время подготовки отчёта 'Отчет №3. Сводный отчет затраченного времени по проекту с детализацией' произошла ошибка!", exc);
            ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
            errorMav.addObject("cause", String.format(COMMON_ERROR_MESSAGE, "подготовки отчёта 'Отчет №3. Сводный отчет затраченного времени по проекту с детализацией'"));
            return errorMav;
        }
        return mav;
    }

    /**************************/
    /*    Блок отчеты Excel   */
    /**************************/

    @RequestMapping(value = "/makeOvertimeReport/{year}/{month}/{divisionOwner}/{divisionEmployee}")
    public ModelAndView makeOvertimeReport(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            @PathVariable("divisionOwner") Integer divisionOwner,
            @PathVariable("divisionEmployee") Integer divisionEmployee,
            HttpServletResponse response,
            HttpServletRequest request) throws JReportBuildError, IOException {

            ModelAndView mav = new ModelAndView("/monthreport/monthreport");
        try{
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
                errorMav.addObject("cause", NO_PERMISSION_MESSAGE);
                return errorMav;
            }
            monthReportExcelService.makeOvertimeReport(year, month, divisionOwner, divisionEmployee, request, response);
        }catch(Exception exc){
            ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
            errorMav.addObject("cause", handleExcelReportError(exc));
            return errorMav;
        }
        return mav;
    }

    @RequestMapping(value = "/makeMonthReport/{division}/{manager}/{regions}/{roles}/{year}/{month}")
    public ModelAndView makeMonthReport(
            @PathVariable("division") Integer division,
            @PathVariable("manager") Integer manager,
            @PathVariable("regions") String regions,
            @PathVariable("roles") String roles,
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            HttpServletResponse response,
            HttpServletRequest request) throws JReportBuildError, IOException {

        ModelAndView mav = new ModelAndView("/monthreport/monthreport");
        try {
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())) {
                ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
                errorMav.addObject("cause", NO_PERMISSION_MESSAGE);
                return errorMav;
            }
            monthReportExcelService.makeMonthReport(division, manager, regions, roles, year, month, request, response);
        } catch (Exception exc) {
            ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
            errorMav.addObject("cause", handleExcelReportError(exc));
            return errorMav;
        }
        return mav;
    }

    @RequestMapping(value = "/makeMutualWorkReport/{year}/{month}/{regions}/{divisionOwner}/{divisionEmployee}/{projectId}")
    public ModelAndView makeMutualWorkReport(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            @PathVariable("regions") String regions,
            @PathVariable("divisionOwner") Integer divisionOwner,
            @PathVariable("divisionEmployee") Integer divisionEmployee,
            @PathVariable("projectId") Integer projectId,
            HttpServletResponse response,
            HttpServletRequest request) throws JReportBuildError, IOException {

            ModelAndView mav = new ModelAndView("/monthreport/monthreport");
        try{
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
                errorMav.addObject("cause", NO_PERMISSION_MESSAGE);
                return errorMav;
            }
            monthReportExcelService.makeMutualWorkReport(year, month, regions, divisionOwner, divisionEmployee, projectId, response, request);
        }catch(Exception exc){
            ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
            errorMav.addObject("cause", handleExcelReportError(exc));
            return errorMav;
        }
        return mav;
    }

    private String handleExcelReportError(Exception exc){
        logger.error("Во время создания отчёта произошла ошибка: ", exc);
        return String.format(COMMON_ERROR_MESSAGE, "создания отчёта") + "\nОписание проблемы: " + exc.getMessage();
    }

    /**************************/
    /*      Статус табеля     */
    /**************************/
    @RequestMapping(value = "/getStatus", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getMonthReportStatus(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        Integer status;
        try {
            status = monthReportService.getMonthReportStatus(year, month).getId();
        } catch (Exception e) {
            // ToDo поправить обработку исключений. Вынеся отдельные повторяющиеся сообщения в константы
            logger.error("Во время запроса данных для табеля произошла ошибка!", e);
            status = MonthReportStatusEnum.NOT_CREATED.getId();
        }
        return status.toString();
    }

    @RequestMapping(value = "/closeMonthReport", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String closeMonthReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        try {
            if (monthReportService.closeMonthReport(year, month)){
                return "Табель успешно закрыт";
            }else{
                return "НЕТ!!!";
            }
        } catch (Exception e) {
            // ToDo поправить обработку исключений. Вынеся отдельные повторяющиеся сообщения в константы
            logger.error("Во время запроса данных для табеля произошла ошибка!", e);
            return "[]";
        }
    }

    @RequestMapping(value = "/openMonthReport", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String openMonthReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        try {
            if (monthReportService.openMonthReport(year, month)){
                return "Табель успешно открыт";
            }else{
                return "НЕТ!!!";
            }
        } catch (Exception e) {
            // ToDo поправить обработку исключений. Вынеся отдельные повторяющиеся сообщения в константы
            logger.error("Во время запроса данных для табеля произошла ошибка!", e);
            return "[]";
        }
    }

    @RequestMapping(value = "/getMonthReportStatusesForYear", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getMonthReportStatusesForYear(@RequestParam("year") Integer year) {
        try {
            return monthReportService.getMonthReportStatusesForYear(year);
        } catch (Exception e) {
            // ToDo поправить обработку исключений. Вынеся отдельные повторяющиеся сообщения в константы
            logger.error("Во время запроса данных для табеля произошла ошибка!", e);
            return "[]";
        }
    }

}
