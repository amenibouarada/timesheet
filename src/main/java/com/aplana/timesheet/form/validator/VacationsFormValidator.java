package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.sql.Timestamp;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class VacationsFormValidator extends AbstractValidator {

    private static final String WRONG_YEAR_ERROR_MESSAGE = "Календарь на %i год еще не заполнен, " +
            "оформите заявление позже или обратитесь в службу поддержки системы";

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(VacationsForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        final VacationsForm vacationsForm = (VacationsForm) o;

        validateYear(vacationsForm.getYear(), errors);
    }

    public void validate(VacationsForm vacationsForm, Errors errors){
        final String calFromDate = vacationsForm.getCalFromDate();
        final String calToDate = vacationsForm.getCalToDate();

        final boolean calFromDateIsNotEmpty = StringUtils.length(calFromDate) > 0;
        final boolean calToDateIsNotEmpty = StringUtils.length(calToDate) > 0;

        validateDivisionId(vacationsForm.getDivisionId(), errors);
        validateEmployeeId(vacationsForm.getEmployeeId(), errors);

        if (calFromDateIsNotEmpty && calToDateIsNotEmpty) {
            final Timestamp fromDate = DateTimeUtil.stringToTimestamp(calFromDate);
            final Timestamp toDate = DateTimeUtil.stringToTimestamp(calToDate);

            validatePeriod(fromDate, toDate, errors);
            validateDateExistsInCalendar(toDate, errors);

        } else {
            validateDatesIsNotEmpty(calFromDate, calToDate, errors);
        }
    }
}
