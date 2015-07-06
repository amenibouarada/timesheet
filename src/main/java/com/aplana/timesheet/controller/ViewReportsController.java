package com.aplana.timesheet.controller;

import com.aplana.timesheet.enums.ReportSendApprovalType;
import com.aplana.timesheet.form.ReportsViewDeleteForm;
import com.aplana.timesheet.form.entity.EmployeeMonthReportDetail;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.ViewReportsForm;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.form.validator.ViewReportsFormValidator;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import padeg.lib.Padeg;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.aplana.timesheet.system.constants.RoleConstants.ROLE_ADMIN;

@Controller
public class ViewReportsController extends AbstractControllerForEmployee {

    @Autowired
    ViewReportsFormValidator tsFormValidator;

    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    CalendarService calendarService;

    @Autowired
    TSPropertyProvider propertyProvider;

    @Autowired
    EmployeeReportService employeeReportService;

    @Autowired
    DictionaryItemService dictionaryItemService;

    @Autowired
    ViewReportsService viewReportsService;

    @Autowired
    VacationService vacationService;

    @RequestMapping(value = "/viewreports", method = RequestMethod.GET)
    public String sendViewReports() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("redirect:/viewreports/%s/%s/%s", securityService.getSecurityPrincipal().getEmployee().getId(), calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1);
    }

    @PostConstruct
    public void initPadeg() {
        if (Padeg.setDictionary(propertyProvider.getPathLibraryPadeg())) {
            Padeg.updateExceptions();
        } else
            logger.error("Cannot load exceptions for padeg module");
    }


    @RequestMapping(value = "/viewreports/{employeeId}/{year}/{month}")
    public ModelAndView showDates(
            @PathVariable("employeeId") Integer employeeId, 
            @PathVariable("year") Integer year, 
            @PathVariable("month") Integer month, 
            @ModelAttribute("viewReportsForm") ViewReportsForm tsForm, 
            @ModelAttribute("deleteReportsForm") ReportsViewDeleteForm tsDeleteForm,
            BindingResult result
    ) {
        logger.info("year {}, month {}", year, month);

        Employee employee = employeeService.find(employeeId);

        tsForm.setDivisionId(employee.getDivision().getId());

        tsFormValidator.validate(tsForm, result);

        ModelAndView mav = createMAVForEmployeeWithDivision("viewreports", employeeId);
        mav.addObject("divisionsEmployeesJSON", employeeService.getDivisionsEmployeesJSON());

        mav.addObject("year", year);
        mav.addObject("month", month);
        mav.addObject("monthList", DateTimeUtil.getMonthListJson((List<Calendar>) mav.getModel().get(YEARS_LIST), calendarService));
        List<DayTimeSheet> dayTimeSheets = timeSheetService.findDatesAndReportsForEmployee(employee, year, month);
        mav.addObject("reports", dayTimeSheets);
        mav.addObject("durationFact", getDurationFact(dayTimeSheets));
        mav.addObject("employeeName", Padeg.getFIOPadegFS(employee.getName(), employee.getSex(), PadegConstants.Roditelnyy));
        mav.addObject(
                "durationPlan",
                BigDecimal.valueOf(
                        (calendarService.getEmployeeRegionWorkDaysCount(
                                employee,
                                year,
                                month
                        ) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate())
                ).setScale(2, BigDecimal.ROUND_HALF_UP)
        );
        Date toDate = new Date();
        mav.addObject("planVacDaysCount", vacationService.getPlanVacationDaysCount(employee, year, month));
        mav.addObject("factVacDaysCount", vacationService.getFactVacationDaysCount(employee, year, month));
        mav.addObject("vacActualizationDate", vacationService.getVacActualizationDate(employee, year, month));
        Integer curYear = calendarService.getYearFromDate(toDate);
        Integer curMonth = calendarService.getMonthFromDate(toDate);

        if ((year != curYear) && (month != curMonth)) {
            toDate = calendarService.getMaxDateMonth(year,month);
        }

        mav.addObject(
                "durationPlanToCurrDate",(toDate.after(new Date())) ? 0 :
                BigDecimal.valueOf(
                        (calendarService.getCountWorkDayPriorDate(
                                employee.getRegion(),
                                year,
                                month,
                                employee.getStartDate(),
                                toDate
                        ) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate())
                ).setScale(2, BigDecimal.ROUND_HALF_UP)
        );

        List<EmployeeMonthReportDetail> reportDetails = employeeReportService.getMonthReport(employeeId, year, month);

        int factToCurrentDate = 0;
        for (EmployeeMonthReportDetail reportDetail : reportDetails){
            // возьмем строку итого, чтобы получить фактическое отработанное время на текущую дату
            if (EmployeeMonthReportDetail.ITOGO.equals(reportDetail.getAct_type().getValue())){
                factToCurrentDate = reportDetail.getFactHours().intValue();
            }
        }

        mav.addObject("durationFactToCurrDate", BigDecimal.valueOf(factToCurrentDate).setScale(2, BigDecimal.ROUND_HALF_UP));
        mav.addObject("reportsDetail", reportDetails);

        viewReportsService.addVacationsForm(mav); //костыль, из за того что адрес для первоначальной загрузки формы отпусков и после применения фильтра один и тот же
        return mav;
    }

    private BigDecimal getDurationFact(List<DayTimeSheet> dayTimeSheets) {
        BigDecimal durationFact = BigDecimal.ZERO;
        for (Iterator<DayTimeSheet> iterator = dayTimeSheets.iterator(); iterator.hasNext(); ) {
            DayTimeSheet next = iterator.next();
            BigDecimal vacationDuration = next.getConsiderVacationDay() && next.getWorkDay() ? new BigDecimal(8) : BigDecimal.ZERO;
            durationFact = durationFact.add(vacationDuration);
            //если это не черновик
            if(!next.getStatusHaveDraft())
                durationFact = durationFact.add(next.getDuration());
            next.setDuration(next.getDuration().add(vacationDuration).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        durationFact = durationFact.setScale(2, BigDecimal.ROUND_HALF_UP);
        return durationFact;
    }

    @RequestMapping(value = "/deleteReports", method = RequestMethod.POST)
    public String deleteReports(
            @ModelAttribute("deleteReportsForm") ReportsViewDeleteForm tsDeleteForm) {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new SecurityException("Не найден пользователь в контексте безопасности.");
        }

        if (!request.isUserInRole(ROLE_ADMIN)) {
            throw new SecurityException("Недостаточно прав для вывполнения операции");
        }

        viewReportsService.deleteReports(tsDeleteForm, securityUser);

        return String.format("redirect:"+tsDeleteForm.getLink());
    }

    @RequestMapping(value = "/sendToRawReports", method = RequestMethod.POST)
    public String sendToRawReports(
            @ModelAttribute("deleteReportsForm") ReportsViewDeleteForm tsDeleteForm) {
        Integer[] ids = tsDeleteForm.getIds();

        for (Integer i = 0; i < ids.length; i++) {
            Integer id = ids[i];
            timeSheetService.setDraftTypeToTimeSheet(id);
        }

        return String.format("redirect:"+tsDeleteForm.getLink());
    }

    @RequestMapping(value = "/sendDeleteReportApproval", method = RequestMethod.POST)
    public String sendDeleteReportApproval(
            @ModelAttribute("deleteReportsForm") ReportsViewDeleteForm tsDeleteForm) {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new SecurityException("Не найден пользователь в контексте безопасности.");
        }
        timeSheetService.setReportApprovalData(tsDeleteForm.getReportId(), tsDeleteForm.getComment(), ReportSendApprovalType.DELETE);

        return String.format("redirect:"+tsDeleteForm.getLink());
    }


    @RequestMapping(value = "/setDraftReportApproval", method = RequestMethod.POST)
    public String setDraftReportApproval(
            @ModelAttribute("deleteReportsForm") ReportsViewDeleteForm tsDeleteForm) {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new SecurityException("Не найден пользователь в контексте безопасности.");
        }
        timeSheetService.setReportApprovalData(tsDeleteForm.getReportId(), tsDeleteForm.getComment() ,ReportSendApprovalType.SET_DRAFT);

        return String.format("redirect:"+tsDeleteForm.getLink());
    }
}