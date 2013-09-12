package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.form.CreateVacationForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.TimeSheetUser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.util.ArrayList;

import static com.aplana.timesheet.enums.VacationTypesEnum.*;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author: iziyangirov
 */
public class CreateVacationFormValidatorTest  extends AbstractTest {

    public Errors errors; // ошибки валидатора
    CreateVacationForm createVacationForm;

    @Mock
    SecurityService securityServiceMock;
    @Mock
    TSPropertyProvider propertyProviderMock;
    @Mock
    DictionaryItemService dictionaryItemServiceMock;
    @Mock
    EmployeeService employeeServiceMock;
    @Mock
    VacationService vacationServiceMock;
    @Mock
    CalendarService calendarServiceMock;

    @InjectMocks
    CreateVacationFormValidator createVacationFormValidator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        Employee employee = new Employee();
        employee.setName("dhh");
        employee.setId(1);
        TimeSheetUser timeSheetUser = new TimeSheetUser(employee, new ArrayList<GrantedAuthority>());
        when(securityServiceMock.getSecurityPrincipal()).thenReturn(timeSheetUser);

        when(propertyProviderMock.getPlannedVacationCreateThreshold()).thenReturn(1);

        DictionaryItem dictItem = new DictionaryItem();
        dictItem.setId(65);
        dictItem.setValue("Планируемый отпуск");
        when(dictionaryItemServiceMock.find(VacationTypesEnum.PLANNED.getId())).thenReturn(dictItem);

        when(employeeServiceMock.isEmployeeAdmin((Integer) any())).thenReturn(true);

        when(vacationServiceMock.getIntersectVacationsCount(
                (Integer) any(), (Timestamp) any(), (Timestamp) any(), (DictionaryItem) any())).thenReturn(0L);

        when(vacationServiceMock.getIntersectPlannedVacationsCount(
                (Integer) any(), (Timestamp) any(), (Timestamp) any(), (DictionaryItem) any())).thenReturn(0L);

        when(calendarServiceMock.find((Timestamp) any())).thenReturn(new Calendar(2000, 10, "month"));

        createVacationForm = new CreateVacationForm();
        setDefaultFormData();
    }

    public void setDefaultFormData(){
        // очищаем список ошибок
        errors = new BeanPropertyBindingResult(createVacationForm, "vacationsForm");

        // правильная форма
        createVacationForm.setEmployeeId(1);
        createVacationForm.setDivisionId(1);
        createVacationForm.setCalFromDate("2100-10-10");
        createVacationForm.setCalToDate("2100-10-20");
        createVacationForm.setComment("some comment");
        createVacationForm.setVacationType(WITH_PAY.getId());
    }

    @Test
    public void testPeriod() {
        createVacationForm.setCalFromDate("2100-10-20");
        createVacationForm.setCalToDate("2100-10-10");
        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        when(vacationServiceMock.getIntersectVacationsCount(
                (Integer) any(), (Timestamp) any(), (Timestamp) any(), (DictionaryItem) any())).thenReturn(1L);
        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testVacationType() {
        createVacationForm.setVacationType(0);

        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testDivisionId() {
        createVacationForm.setDivisionId(-1);

        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testEmployeeId() {
        createVacationForm.setEmployeeId(-1);

        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testComment() {
        // 601 символ
        createVacationForm.setComment(" 123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        createVacationFormValidator.validate(createVacationForm, errors, false);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        createVacationForm.setComment("");
        createVacationForm.setVacationType(WITH_NEXT_WORKING.getId());
        createVacationFormValidator.validate(createVacationForm, errors, false);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testToDate() {
        createVacationForm.setCalToDate("2010-10-10");
        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        createVacationForm.setCalToDate("2010-10-10");
        createVacationForm.setVacationType(PLANNED.getId());
        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        createVacationForm.setCalToDate("");
        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void testFromDate(){
        createVacationForm.setCalFromDate("2010-10-10");
        createVacationFormValidator.validate(createVacationForm, errors, false);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        when(calendarServiceMock.find((Timestamp) any())).thenReturn(null);
        createVacationForm.setVacationType(PLANNED.getId());
        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());

        setDefaultFormData();
        createVacationForm.setCalFromDate("");
        createVacationFormValidator.validate(createVacationForm, errors, true);
        assertEquals(1, errors.getErrorCount());
    }


    @Test
    public void testZeroErrors(){
        createVacationFormValidator.validate(createVacationForm, errors, false);
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void testThreeErrors(){
        createVacationForm.setCalFromDate("2010-10-10");
        createVacationForm.setCalToDate("2010-10-20");
        createVacationForm.setComment("");
        createVacationForm.setVacationType(WITH_NEXT_WORKING.getId());

        createVacationFormValidator.validate(createVacationForm, errors, false);
        assertEquals(3, errors.getErrorCount());
    }

    @Test
    public void testSixErrors(){
        createVacationForm.setCalFromDate("2010-10-20");
        createVacationForm.setCalToDate("2010-10-10");
        createVacationForm.setComment("");
        createVacationForm.setVacationType(PLANNED.getId());
        when(vacationServiceMock.getIntersectPlannedVacationsCount(
                (Integer) any(), (Timestamp) any(), (Timestamp) any(), (DictionaryItem) any())).thenReturn(1L);
        when(calendarServiceMock.find((Timestamp) any())).thenReturn(null);

        createVacationFormValidator.validate(createVacationForm, errors, false);
        assertEquals(6, errors.getErrorCount());
    }

}
