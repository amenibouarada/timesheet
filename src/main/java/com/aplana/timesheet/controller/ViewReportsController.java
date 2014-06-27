package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.enums.TypesOfTimeSheetEnum;
import com.aplana.timesheet.form.ReportsViewDeleteForm;
import com.aplana.timesheet.form.VacationsForm;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.aplana.timesheet.form.VacationsForm.VIEW_TABLE;

@Controller
public class ViewReportsController extends AbstractControllerForEmployeeWithYears {

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
    private SendMailService sendMailService;

    @RequestMapping(value = "/viewreports", method = RequestMethod.GET)
    public String sendViewReports() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("redirect:/viewreports/%s/%s/%s/%s", securityService.getSecurityPrincipal().getEmployee().getDivision().getId(), securityService.getSecurityPrincipal().getEmployee().getId(), calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1);
    }

    @PostConstruct
    public void initPadeg() {
        if (Padeg.setDictionary(propertyProvider.getPathLibraryPadeg())) {
            Padeg.updateExceptions();
        } else
            logger.error("Cannot load exceptions for padeg module");
    }

    @RequestMapping(value = "/viewreports/{divisionId}/{employeeId}/{year}/{month}")
    public ModelAndView showDates(
            @PathVariable("divisionId") Integer divisionId, 
            @PathVariable("employeeId") Integer employeeId, 
            @PathVariable("year") Integer year, 
            @PathVariable("month") Integer month, 
            @ModelAttribute("viewReportsForm") ViewReportsForm tsForm, 
            @ModelAttribute("deleteReportsForm") ReportsViewDeleteForm tsDeleteForm,
            BindingResult result
    ) {
        logger.info("year {}, month {}", year, month);
        tsFormValidator.validate(tsForm, result);

        ModelAndView mav = createMAVForEmployeeWithDivision("viewreports", employeeId, divisionId);

        Employee employee = (Employee) mav.getModel().get(EMPLOYEE);

        mav.addObject("year", year);
        mav.addObject("month", month);
        mav.addObject("monthList", DateTimeUtil.getMonthListJson((List<Calendar>) mav.getModel().get(YEARS_LIST), calendarService));
        List<DayTimeSheet> dayTimeSheets = timeSheetService.findDatesAndReportsForEmployee(employee, year, month);
        mav.addObject("reports", dayTimeSheets);
        mav.addObject("employeeName",Padeg.getFIOPadegFS(employee.getName(),true,PadegConstants.Roditelnyy));
        BigDecimal durationFact = BigDecimal.ZERO;
        for (Iterator<DayTimeSheet> iterator = dayTimeSheets.iterator(); iterator.hasNext(); ) {
            DayTimeSheet next = iterator.next();
            BigDecimal vacationDuration = next.getVacationDay() && next.getWorkDay() ? new BigDecimal(8) : BigDecimal.ZERO;
            durationFact = durationFact.add(vacationDuration);
            //если это не черновик
            if(!next.getStatusHaveDraft())
                durationFact = durationFact.add(next.getDuration());
            next.setDuration(next.getDuration().add(vacationDuration).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        durationFact = durationFact.setScale(2, BigDecimal.ROUND_HALF_UP);
        mav.addObject("durationFact", durationFact/*.doubleValue()*/);
        mav.addObject(
                "durationPlan",
                BigDecimal.valueOf( //todo переделать когда все числа будут BigDecimal
                        (calendarService.getEmployeeRegionWorkDaysCount(
                                employee,
                                year,
                                month
                        ) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate())
                ).setScale(2, BigDecimal.ROUND_HALF_UP)
        );
        Date toDate = new Date();
        Integer curYear = calendarService.getYearFromDate(toDate);
        Integer curMonth = calendarService.getMonthFromDate(toDate);

        if ((year != curYear) && (month != curMonth)) {
           toDate = calendarService.getMaxDateMonth(year,month);
        }

        mav.addObject(
                "durationPlanToCurrDate",(toDate.after(new Date())) ? 0 :
                BigDecimal.valueOf(//todo переделать когда все числа будут BigDecimal
                        (calendarService.getCountWorkDayPriorDate(
                            employee.getRegion(),
                            year,
                            month,
                            toDate
                    ) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate())
                ).setScale(2, BigDecimal.ROUND_HALF_UP)
        );

        mav.addObject("reportsDetail", employeeReportService.getMonthReport(employeeId, year, month));
        addVacationsForm(mav); //костыль, из за того что адрес для первоначальной загрузки формы отпусков и после применения фильтра один и тот же
        return mav;
    }

    private void addVacationsForm(ModelAndView modelAndView) {

        VacationsForm vacationsForm = new VacationsForm();
        vacationsForm.setVacationType(0);
        vacationsForm.setRegions(new ArrayList<Integer>());
        vacationsForm.getRegions().add(VacationsForm.ALL_VALUE);
        vacationsForm.setViewMode(VIEW_TABLE);

        modelAndView.addObject("vacationsForm", vacationsForm);
    }


    @RequestMapping(value = "/deleteReports", method = RequestMethod.POST)
    public String deleteReports(
            @ModelAttribute("deleteReportsForm") ReportsViewDeleteForm tsDeleteForm) {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new SecurityException("Не найден пользователь в контексте безопасности.");
        }
        Integer[] ids = tsDeleteForm.getIds();
        for (Integer i = 0; i < ids.length; i++) {
            Integer id = ids[i];
            TimeSheet timeSheet = timeSheetService.find(id);
            logger.info("Удаляется отчет " + timeSheet + ". Инициатор: " + securityUser.getEmployee().getName());
            timeSheetService.delete(timeSheet);
            sendMailService.performTimeSheetDeletedMailing(timeSheet);
        }
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

}