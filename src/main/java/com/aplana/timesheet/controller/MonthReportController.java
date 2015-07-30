package com.aplana.timesheet.controller;

import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.service.monthreport.MonthReportExcelService;
import com.aplana.timesheet.service.monthreport.MonthReportService;
import com.aplana.timesheet.service.monthreport.MutualWorkService;
import com.aplana.timesheet.service.monthreport.OvertimeService;
import com.aplana.timesheet.util.DateTimeUtil;
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
    private static String SAVE_SUCCESS_MESSAGE = "Успешно сохранено";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm() {
        final ModelAndView modelAndView = new ModelAndView("monthreport/monthreport");

        fillMavForAddEmployeesForm(modelAndView);
        modelAndView.addObject(AddEmployeeForm.ADD_FORM, new AddEmployeeForm());
        try {
            modelAndView.addObject("lastEnableYearAndMonth", monthReportService.getLastEnableYearAndMonth());
        } catch (Exception exc) {
            // Если ошибка, то устанавливаем текущий год и первый месяц
            modelAndView.addObject("lastEnableYearAndMonth", DateTimeUtil.getCurrentYear().toString() + ", 3");
            logger.error("Во время получения максимальных доступных года и месяца произошла ошибка: ", exc);
        }

        return modelAndView;
    }

    private String handleCommonException(String procedure, Exception exc){
        String message = String.format(COMMON_ERROR_MESSAGE, procedure)  + "\nОписание проблемы: " + exc.getMessage();
        logger.error(message, exc);
        return message;
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
        } catch (IOException exc) {
            return handleCommonException("запроса данных для табеля", exc);
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
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return NO_PERMISSION_MESSAGE;
            }
            monthReportService.saveMonthReportTable(year, month, jsonData);
        }catch (Exception exc){
            return handleCommonException("сохранения табеля", exc);
        }
        return SAVE_SUCCESS_MESSAGE;
    }

    /**************************/
    /*   Блок "Переработки"   */
    /**************************/
    @RequestMapping(value = "/saveOvertimeTable", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveOvertimeTable(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("divisionOwner") Integer divisionOwner,
            @RequestParam("jsonData") String jsonData
    ){
        try{
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return NO_PERMISSION_MESSAGE;
            }
            overtimeService.saveOvertimeTable(year, month, divisionOwner, jsonData);
        }catch (Exception exc){
            return handleCommonException("сохранения таблицы переработок", exc);
        }
        return SAVE_SUCCESS_MESSAGE;
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
            return handleCommonException("удаления данных из таблицы переработок", exc);
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
        } catch (IOException exc) {
            return handleCommonException("запроса данных для таблицы переработок", exc);
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
        } catch (IOException exc) {
            return handleCommonException("запроса данных для таблицы 'Взаимная занятость'", exc);
        }
    }

    @RequestMapping(value = "/saveMutualWorkTable", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveMutualWorkTable(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("divisionOwner") Integer divisionOwner,
            @RequestParam("jsonData") String jsonData
    ){
        try{
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return NO_PERMISSION_MESSAGE;
            }
            mutualWorkService.saveMutualWorkTable(year, month, divisionOwner, jsonData);
        }catch (Exception exc){
            return handleCommonException("сохранения данных таблицы 'Взаимная занятость'", exc);
        }
        return SAVE_SUCCESS_MESSAGE;
    }

    @RequestMapping(value = "/deleteMutualWorks", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteMutualWorks(
            @RequestParam("jsonData") String jsonData
    ){
        try{
            if (employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                mutualWorkService.deleteMutualWorks(jsonData);
            }else{
                return NO_PERMISSION_MESSAGE;
            }
        }catch (Exception exc){
            return handleCommonException("удаления данных из таблицы 'Взаимная занятость'", exc);
        }
        return "Строки успешно удалены";
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
    )throws JReportBuildError, IOException
    {
        try{
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return getExcelReportNoPermissionMAV();
            }
            mutualWorkService.prepareReport3Data(beginDate, endDate, region, divisionOwner, divisionEmployee, projectId, employeeId, response, request);
        } catch (Exception exc){
            return createErrorMAV(handleCommonException("подготовки отчёта 'Отчет №3. Сводный отчет затраченного времени по проекту с детализацией'", exc));
        }
        return new ModelAndView("/monthreport/monthreport");
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
            HttpServletRequest request) throws JReportBuildError, IOException
    {
        try{
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return getExcelReportNoPermissionMAV();
            }
            monthReportExcelService.makeOvertimeReport(year, month, divisionOwner, divisionEmployee, request, response);
        }catch(Exception exc){
            return handleExcelReportError(exc);
        }
        return new ModelAndView("/monthreport/monthreport");
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
            HttpServletRequest request) throws JReportBuildError, IOException
    {
        try {
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())) {
                return getExcelReportNoPermissionMAV();
            }
            monthReportExcelService.makeMonthReport(division, manager, regions, roles, year, month, request, response);
        } catch (Exception exc) {
            return handleExcelReportError(exc);
        }
        return new ModelAndView("/monthreport/monthreport");
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
            HttpServletRequest request) throws JReportBuildError, IOException
    {
        try{
            if ( ! employeeService.isEmployeeHasPermissionsToMonthReportManage(getCurrentUser())){
                return getExcelReportNoPermissionMAV();
            }
            monthReportExcelService.makeMutualWorkReport(year, month, regions, divisionOwner, divisionEmployee, projectId, response, request);
        }catch(Exception exc){
            return handleExcelReportError(exc);
        }
        return new ModelAndView("/monthreport/monthreport");
    }

    private ModelAndView getExcelReportNoPermissionMAV(){
        return createErrorMAV(NO_PERMISSION_MESSAGE);
    }

    private ModelAndView handleExcelReportError(Exception exc){
        return createErrorMAV(handleCommonException("создания отчёта", exc));
    }

    private ModelAndView createErrorMAV(String cause){
        ModelAndView errorMav = new ModelAndView("/errors/commonErrors");
        errorMav.addObject("cause", cause);
        return errorMav;
    }

    /**************************/
    /*      Статус табеля     */
    /**************************/
    @RequestMapping(value = "/getStatus", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getMonthReportStatus(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ){
        try {
            return new Integer( monthReportService.getMonthReportStatus(year, month).getId()).toString();
        } catch (Exception exc) {
            return handleCommonException("получения статуса табеля", exc);
        }
    }

    @RequestMapping(value = "/getMonthReportStatusesForYear", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getMonthReportStatusesForYear(@RequestParam("year") Integer year) {
        try {
            return monthReportService.getMonthReportStatusesForYear(year);
        } catch (Exception exc) {
            return handleCommonException("получения статусов табелей по месяцам года", exc);
        }
    }

    @RequestMapping(value = "/closeMonthReport", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String closeMonthReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        try {
            if ( ! employeeService.isEmployeeHasPermissionsToCloseOpenMonthReport(getCurrentUser())){
                return NO_PERMISSION_MESSAGE;
            }
            monthReportService.closeMonthReport(year, month);
        } catch (Exception exc) {
            return handleCommonException("закрытия табеля", exc);
        }
        return "Табель успешно закрыт";
    }

    @RequestMapping(value = "/openMonthReport", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String openMonthReport(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        try {
            if ( ! employeeService.isEmployeeHasPermissionsToCloseOpenMonthReport(getCurrentUser())){
                return NO_PERMISSION_MESSAGE;
            }
            monthReportService.openMonthReport(year, month);
        } catch (Exception exc) {
            return handleCommonException("открытия табеля", exc);
        }
        return "Табель успешно открыт";
    }
}
