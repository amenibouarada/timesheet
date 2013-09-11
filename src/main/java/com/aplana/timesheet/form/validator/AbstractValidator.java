package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.sql.Timestamp;
import java.util.regex.Pattern;

/**
 * @author eshangareev
 * @version 1.0
 * Позволяет валидировать формы унаследованные от CommonAbstractForm
 */
public abstract class AbstractValidator implements Validator{

    @Autowired
    private CalendarService calendarService;

    private static final String WRONG_YEAR_ERROR_MESSAGE = "Календарь на %i год еще не заполнен, " +
            "выполните запрос позже или обратитесь в службу поддержки системы";

    private static Pattern emailPattern =
            Pattern.compile(
                    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
                            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\" +
                            "[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                            "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\" +
                            "[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|" +
                            "[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]" +
                            "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])" );


    public static boolean validateEmail( String email ) {
        return emailPattern.matcher( email ).matches();
    }

    public static boolean isNotChoosed( Integer id ) {
        return id == null || id == 0;
    }

    private boolean isYearValid(Integer year) {
        return calendarService.yearValid(year);
    }

    public void validateYear(Integer year, Errors errors) {
        // Год не выбран.
        if ( isNotChoosed( year ) ) {
            errors.rejectValue( "year",
                    "error.tsform.year.required",
                    "Не выбран год." );
        }
        // Неверный год
        else if ( ! isYearValid( year ) ) {
            errors.rejectValue( "year",
                    "error.tsform.year.invalid",
                    "Выбран неверный год." );
        }
    }

    public void validateMonth(Integer year, Integer month, Errors errors) {
        // Месяц не выбран.
        if ( isNotChoosed( month ) ) {
            errors.rejectValue( "month",
                    "error.tsform.month.required",
                    "Не выбран месяц." );
        }

        // Неверный месяц
        else if ( ! calendarService.monthValid(year, month) ) {
            errors.rejectValue( "month",
                    "error.tsform.month.required",
                    "Выбран неверный месяц." );
        }
    }

    public void validatePeriod(Timestamp fromDate, Timestamp toDate, Errors errors){ // используйте DateTimeUtil для перевода в Timestamp
        if (fromDate.after(toDate)) {
            errors.rejectValue(
                    "calToDate",
                    "error.form.period",
                    "Дата окончания отпуска не может быть больше даты начала"
            );
        }
    }

    public void validateDateExistsInCalendar(Timestamp date, Errors errors){
        if (calendarService.find(date) == null){
            errors.rejectValue(
                    "calToDate",
                    "error.createVacation.wrongyear",
                    new Object[]{DateTimeUtil.getYear(date)+1900},
                    WRONG_YEAR_ERROR_MESSAGE
            );
        }
    }

    public void validateDatesIsNotEmpty(String fromDate, String toDate, Errors errors){
        if (fromDate == null || fromDate.length() <= 0) {
            errors.rejectValue(
                    "calFromDate",
                    "error.form.datefrom",
                    "Не указана дата начала периода"
            );
        }

        if (toDate == null || toDate.length() <= 0) {
            errors.rejectValue(
                    "calToDate",
                    "error.form.dateto",
                    "Не указана дата окончания периода"
            );
        }
    }

    public void validateEmployee(Employee employee, Errors errors){
        validateEmployeeId(employee.getId(), errors);
    }

    public void validateDivision(Division division, Errors errors){
        validateDivisionId(division.getId(), errors);
    }

    public void validateEmployeeId(Integer employeeId, Errors errors){
        if (employeeId == null || employeeId < 0){
            errors.rejectValue(
                    "employeeId",
                    "error.form.employeeid",
                    "Не указан сотрудник"
            );
        }

    }

     public void validateDivisionId(Integer divisionId, Errors errors){
        if (divisionId == null || divisionId < 0){
            errors.rejectValue(
                    "divisionId",
                    "error.form.divisionid",
                    "Не указано подраздлеление"
            );
        }
    }

}
