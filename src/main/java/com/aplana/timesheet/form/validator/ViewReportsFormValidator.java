package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.controller.ViewReportsController;
import com.aplana.timesheet.form.ViewReportsForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;


@Service
public class ViewReportsFormValidator extends AbstractValidator {
	private static final Logger logger = LoggerFactory.getLogger(ViewReportsController.class);

    public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(ViewReportsForm.class);
	}

	public void validate(Object target, Errors errors) {
		ViewReportsForm tsForm = (ViewReportsForm) target;
		Integer year = tsForm.getYear();
		Integer month = tsForm.getMonth();
		logger.debug("Year = {} and Month = {}.", year, month);
		
		validateYear(year, errors);
		validateMonth(year, month, errors);
		
	}

}