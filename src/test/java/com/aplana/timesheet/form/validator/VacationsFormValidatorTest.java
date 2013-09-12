package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.service.CalendarService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.aplana.timesheet.enums.VacationTypesEnum.PLANNED;
import static com.aplana.timesheet.enums.VacationTypesEnum.WITH_PAY;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author: iziyangirov
 */
public class VacationsFormValidatorTest extends AbstractTest {


    public Errors errors; // ошибки валидатора
    VacationsForm vacationsForm;

    @Mock
    CalendarService calendarServiceMock;

    @InjectMocks
    VacationsFormValidator vacationsFormValidator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        when(calendarServiceMock.find((Timestamp) any())).thenReturn(new Calendar(2000, 10, "month"));

        vacationsForm = new VacationsForm();
        setDefaultFormData();
    }

    public void setDefaultFormData(){
        // очищаем список ошибок
        errors = new BeanPropertyBindingResult(vacationsForm, "vacationsForm");

        List<Integer> regions = new ArrayList<Integer>();
        regions.add(1);
        List<Integer> regionsIdList = new ArrayList<Integer>();
        regionsIdList.add(1);

        // правильная форма
        vacationsForm.setEmployeeId(1);
        vacationsForm.setDivisionId(1);
        vacationsForm.setYear(2013);
        vacationsForm.setVacationId(1);
        vacationsForm.setCalFromDate("2100-10-10");
        vacationsForm.setCalToDate("2100-10-20");
        vacationsForm.setVacationType(WITH_PAY.getId());
        vacationsForm.setManagerId(1);
        vacationsForm.setRegions(regions);
        vacationsForm.setVacationId(1);
        vacationsForm.setApprovalId(1);
        vacationsForm.setProjectId(1);
        vacationsForm.setViewMode(1);
    }

    @Test
    public void testFirst(){
        vacationsFormValidator.validate(vacationsForm, errors);
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void testToDate(){
        vacationsForm.setCalToDate("2010-10-10");
        vacationsFormValidator.validate(vacationsForm, errors);
        assertEquals(1, errors.getErrorCount());

        vacationsForm.setCalToDate("");
        vacationsFormValidator.validate(vacationsForm, errors);
        assertEquals(1, errors.getErrorCount());
    }


    @Test
    public void testFromDate(){
        vacationsForm.setCalFromDate("2010-10-10");
        vacationsFormValidator.validate(vacationsForm, errors);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        when(calendarServiceMock.find((Timestamp) any())).thenReturn(null);
        vacationsForm.setVacationType(PLANNED.getId());
        vacationsFormValidator.validate(vacationsForm, errors);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        vacationsForm.setCalFromDate("");
        vacationsFormValidator.validate(vacationsForm, errors);
        assertEquals(1, errors.getErrorCount());
    }

}

