package com.aplana.timesheet.controller;

import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.form.validator.FeedbackFormValidator;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

@Controller
public class FeedbackController {
	private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);

	@Autowired
	private FeedbackFormValidator fbFormValidator;
    @Autowired
    private SendMailService sendMailService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private MessageSource messageSource;
    @Autowired
    private TSPropertyProvider propertyProvider;


	//IDs подразделения и сотрудника по умолчанию
	private Integer divisionId = 0, employeeId = 0; //TODO зачем это нужно? контроллер — синглтон, они будут доступны всем

	// Эти коды должны соответствовать страничке feedback.jsp
	private static final String[] FEEDBACK_TYPE_NAME_KEYS = {
		null,
        "feedback.type.newproposal",
		"feedback.type.incorrectdata",
		"feedback.type.cantsendreport",
		"feedback.type.notfoundproject",
		"feedback.type.other",
        "feedback.type.deletevacation"
	};

    //Пользователь нажал на кнопку "Очистить" на странице feedback.jsp
	@RequestMapping(value = "/newFeedbackMessage", method = RequestMethod.POST)
	public String newFeedbackMessage() {
		return "redirect:feedback";
	}
	
	//Пользователь нажал "Отправить" на странице feedback.jsp
	@RequestMapping(value = "/feedback", method = RequestMethod.POST)
	public ModelAndView sendFeedback(
            @ModelAttribute("feedbackForm")
            FeedbackForm fbForm,
            BindingResult result,
            Locale locale
    ) {
		//Валидируем форму
		logger.info("Processing FeedbackForm validation for employee {}", fbForm.getEmployeeId());
		fbFormValidator.validate(fbForm, result);
		//Если ошибки есть
		if (result.hasErrors()) {
			logger.info("FeedbackForm for employee {} has errors.", fbForm.getEmployeeId());
			return new ModelAndView("feedback");
		}
		//Если ошибок нет
		fbForm.setFeedbackTypeName(messageSource.getMessage(FEEDBACK_TYPE_NAME_KEYS[ fbForm.getFeedbackType() ], null, locale));
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser != null) {
            divisionId = securityUser.getEmployee().getDivision().getId();
            employeeId = securityUser.getEmployee().getId();
        }
        fbForm.setDivisionId(divisionId);
        fbForm.setEmployeeId(employeeId);
		sendMailService.performFeedbackMailing(fbForm);
		ModelAndView mav = new ModelAndView("feedbackSent");
		//сохраняем ID подразделения и сотрудника для будущей формы
		divisionId = fbForm.getDivisionId();
		employeeId = fbForm.getEmployeeId();
		mav.addObject("feedbackForm", fbForm);
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		return mav;
	}

	//Пользователь нажал на кнопку "Отправить новое сообщение о проблеме" на странице feedbackSent.jsp
	@RequestMapping(value = "/sendNewFeedbackMessage", method = RequestMethod.POST)
	public String sendNewFeedbackMessage() {
		return "redirect:feedback";
	}
	
	//Основной метод GET
	@RequestMapping(value = "/feedback", method = RequestMethod.GET)
    public ModelAndView sendReportFeedback(@ModelAttribute("feedbackForm") FeedbackForm fbForm, BindingResult result, String messageText, String exceptionText) {
        String jiraIssueCreateUrl = propertyProvider.getJiraIssueCreateUrl();
        if (StringUtils.isBlank(jiraIssueCreateUrl)) {
            jiraIssueCreateUrl = null;
            logger.warn("In your properties not assign 'jira.issue.create.url', some functions will be disabled");
        }

        fbForm.setFeedbackDescription(messageText);

		ModelAndView mav = new ModelAndView("feedback");
        if (StringUtils.isNotBlank(exceptionText)) {
            String message =  messageSource.getMessage(exceptionText, new Object[]{}, null);
            result.rejectValue("feedbackDescription", message, "Размер вложенных файлов больше 8 Мб!");
        }
		mav.addObject("feedbackForm", fbForm);
		mav.addObject("jiraIssueCreateUrl", jiraIssueCreateUrl);

		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return mav;
	 }

}