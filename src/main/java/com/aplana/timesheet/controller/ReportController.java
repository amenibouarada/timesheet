package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ReportController {
	private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);	 
		
	@Autowired
	CalendarService calendarService;
	@Autowired
	TimeSheetService timeSheetService;
	@Autowired
	EmployeeService employeeService;
	@Autowired
	TimeSheetDetailService timeSheetDetailService;
	@Autowired
	SendMailService sendMailService;
	@Autowired
	ProjectService projectService;  
	@Autowired
    ReportService reportService;

	@RequestMapping(value = "/report/{year}/{month}/{day}/{employeeId}", method = RequestMethod.GET)
	public ModelAndView sendViewReports ( @PathVariable("year") Integer year,@PathVariable("month") Integer month,@PathVariable("day") Integer day, @PathVariable("employeeId") Integer employeeId, @ModelAttribute("ReportForm") TimeSheetForm tsForm, BindingResult result) {		
		logger.info("Date for report: {}.{}", year, month);
		logger.info("Date for report: {}", day);
		ModelAndView mav = new ModelAndView("report");
		mav.addObject("ReportForm", tsForm);
		mav.addObject("year", Integer.toString(year));
		mav.addObject("month", month);
		mav.addObject("day", day);
		mav.addObject("employeeId", employeeId);

        final TimeSheet timeSheet = timeSheetService.findForDateAndEmployee(year.toString() + "-" + month.toString() + "-" + day.toString(), employeeId);

        mav.addObject("creationDate", (timeSheet != null && timeSheet.getCreationDate() != null) ?
                DateTimeUtil.dateToString(timeSheet.getCreationDate(), DateTimeUtil.VIEW_DATE_TIME_PATTERN) : "");
        mav.addObject("report", reportService.modifyURL(sendMailService.initMessageBodyForReport(timeSheet)));

        logger.info("<<<<<<<<< End of RequestMapping <<<<<<<<<<<<<<<<<<<<<<");
        return mav;
    }
}