package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.form.CommonAbstractForm;
import com.aplana.timesheet.service.CalendarService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author eshangareev
 * @version 1.0
 */
public class AbstractValidatorTest {

    // создан не абстрактный класс для тестирования
    private class ValidatorAbstract extends AbstractValidator{
        @Override
        public boolean supports(Class<?> aClass) {
            return false;
        }
        @Override
        public void validate(Object o, Errors errors) {
        }
    }
    // создана не абстарактна форма для тестирования
    private class AbstractForm extends CommonAbstractForm{
        Integer year;

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }
    }

    @Mock
    CalendarService calendarServiceMock;

    @InjectMocks
    ValidatorAbstract abstractValidator = new ValidatorAbstract();

    AbstractForm commAbstractForm = new AbstractForm();
    Errors errors;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);

        when(calendarServiceMock.yearValid((Integer) any())).thenReturn(true);
        when(calendarServiceMock.find((Timestamp) any())).thenReturn(new Calendar(2000, 10, "month"));
        errors = new BeanPropertyBindingResult(commAbstractForm, "vacationsForm");
    }

    @Test
    public void testValidate() throws Exception {
        Assert.assertTrue(AbstractValidator.validateEmail("eshangareev@aplana.com"));
        Assert.assertTrue(AbstractValidator.validateEmail("eshangareev.some@aplana.com"));
        Assert.assertTrue(AbstractValidator.validateEmail("eshangareev@aplana.some.com"));
        Assert.assertTrue( AbstractValidator.validateEmail( "eshangareev.some@aplana.some.com" ) );
        Assert.assertFalse( AbstractValidator.validateEmail("esh..@aplana.com"));
    }

    @Test
    public void testValidateYear(){
        abstractValidator.validateYear(2013, errors); // нет ошибок
        assertEquals(0, errors.getErrorCount());
        abstractValidator.validateYear(null, errors); // +1 ошибка
        assertEquals(1, errors.getErrorCount());
        when(calendarServiceMock.yearValid((Integer) any())).thenReturn(false);
        abstractValidator.validateYear(2013, errors); // +1 ошибка
        assertEquals(2, errors.getErrorCount());
    }

    @Test
    public void testValidatePeriod(){
        Timestamp fromDate = new Timestamp((new Date()).getTime() + 1);
        Timestamp toDate = new Timestamp((new Date()).getTime());
        abstractValidator.validatePeriod(fromDate, fromDate, errors); // нет ошибок
        assertEquals(0, errors.getErrorCount());
        abstractValidator.validatePeriod(fromDate, toDate, errors); // +1 ошибка
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testValidateDateExistsInCalendar(){
        Timestamp date = new Timestamp((new Date()).getTime());
        abstractValidator.validateDateExistsInCalendar(date, errors);  // нет ошибок
        assertEquals(0, errors.getErrorCount());
        when(calendarServiceMock.find((Timestamp) any())).thenReturn(null);
        abstractValidator.validateDateExistsInCalendar(date, errors);  // +1 ошибка
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testValidateDatesIsNotEmpty(){
        String fromDate = "21-03-2013";
        String toDate = "22-04-2013";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);    // нет ошибок
        fromDate = "";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);    // +1 ошибка
        assertEquals(1, errors.getErrorCount());
        toDate = "";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);    // +2 ошибка
        assertEquals(3, errors.getErrorCount());
        fromDate = "21-03-2013";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);    // +1 ошибка
        assertEquals(4, errors.getErrorCount());

        /*
        void validateDatesIsNotEmpty(String fromDate, String toDate, Errors errors){
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
        */
    }

    @Test
    public void testValidateMonth(){
/*
    protected void validateMonth(Integer year, Integer month, Errors errors) {
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
  */
    }

    @Test
    public void testValidateEmployee(){
    /*
    void validateEmployee(Employee employee, Errors errors){
        validateEmployeeId(employee.getId(), errors);
    }
    */
    }

    @Test
    public void testValidateDivision(){
    /*
    void validateDivision(Division division, Errors errors){
        validateDivisionId(division.getId(), errors);
    }
    */
    }


}
