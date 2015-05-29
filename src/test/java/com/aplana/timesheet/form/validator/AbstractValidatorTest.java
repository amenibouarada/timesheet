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
    // создана не абстарактная форма для тестирования
    private class AbstractForm extends CommonAbstractForm{
        Integer year;
        Integer month;

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
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
        when(calendarServiceMock.monthValid((Integer) any(), (Integer) any())).thenReturn(true);
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
    public void testValidateYear1(){
        abstractValidator.validateYear(2013, errors);
        assertEquals(0, errors.getErrorCount());
    }
    @Test
    public void testValidateYear2(){
        abstractValidator.validateYear(null, errors);
        assertEquals(1, errors.getErrorCount());
    }
    @Test
    public void testValidateYear3(){
        when(calendarServiceMock.yearValid((Integer) any())).thenReturn(false);
        abstractValidator.validateYear(2013, errors);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testValidatePeriod1(){
        Timestamp date = new Timestamp((new Date()).getTime());
        abstractValidator.validatePeriod(date, date, errors);
        assertEquals(0, errors.getErrorCount());
    }
    @Test
    public void testValidatePeriod2(){
        Timestamp fromDate = new Timestamp((new Date()).getTime() + 1);
        Timestamp toDate = new Timestamp((new Date()).getTime());
        abstractValidator.validatePeriod(fromDate, toDate, errors);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testValidateDateExistsInCalendar1(){
        Timestamp date = new Timestamp((new Date()).getTime());
        abstractValidator.validateDateExistsInCalendar(date, errors);
        assertEquals(0, errors.getErrorCount());
    }
    @Test
    public void testValidateDateExistsInCalendar2(){
        Timestamp date = new Timestamp((new Date()).getTime());
        when(calendarServiceMock.find((Timestamp) any())).thenReturn(null);
        abstractValidator.validateDateExistsInCalendar(date, errors);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testValidateDatesIsNotEmpty1(){
        String fromDate = "21-03-2013";
        String toDate = "22-04-2013";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);
        assertEquals(0, errors.getErrorCount());
    }
    @Test
    public void testValidateDatesIsNotEmpty2(){
        String fromDate = "";
        String toDate = "22-04-2013";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);
        assertEquals(1, errors.getErrorCount());
    }
    @Test
    public void testValidateDatesIsNotEmpty3(){
        String fromDate = "21-03-2013";
        String toDate = "";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);
        assertEquals(1, errors.getErrorCount());
    }
    @Test
    public void testValidateDatesIsNotEmpty4(){
        String fromDate = "";
        String toDate = "";
        abstractValidator.validateDatesIsNotEmpty(fromDate, toDate, errors);
        assertEquals(2, errors.getErrorCount());
    }

    @Test
    public void testValidateMonth1(){
        abstractValidator.validateMonth(2013, 1, errors);
        assertEquals(0, errors.getErrorCount());
    }
    @Test
    public void testValidateMonth2(){
        abstractValidator.validateMonth(2013, null, errors);
        assertEquals(1, errors.getErrorCount());
    }
    @Test
    public void testValidateMonth3(){
        when(calendarServiceMock.monthValid((Integer) any(), (Integer) any())).thenReturn(false);
        abstractValidator.validateMonth(2014, 14, errors);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testValidateEmployee1(){
        abstractValidator.validateEmployeeId(12, errors);
        assertEquals(0, errors.getErrorCount());
    }
    @Test
    public void testValidateEmployee2(){
        abstractValidator.validateEmployeeId(null, errors);
        assertEquals(1, errors.getErrorCount());
    }
    @Test
    public void testValidateEmployee3(){
        abstractValidator.validateEmployeeId(-12, errors);
        assertEquals(1, errors.getErrorCount());
    }


    @Test
    public void testValidateDivision1(){
        abstractValidator.validateDivisionId(12, errors);
        assertEquals(0, errors.getErrorCount());
    }
    @Test
    public void testValidateDivision2(){
        abstractValidator.validateDivisionId(null, errors);
        assertEquals(1, errors.getErrorCount());
    }
    @Test
    public void testValidateDivision3(){
         abstractValidator.validateDivisionId(-12, errors);
        assertEquals(1, errors.getErrorCount());
    }





}
