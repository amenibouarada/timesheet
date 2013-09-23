package com.aplana.timesheet.controller;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.VacationApprovalForm;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import com.aplana.timesheet.service.vacationapproveprocess.VacationApprovalProcessService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author: iziyangirov
 */
public class VacationsApprovalControllerTest extends AbstractTest{

    @Mock
    VacationApprovalService vacationApprovalService;

    @Mock
    VacationApprovalProcessService vacationApprovalProcessService;

    @Mock
    SendMailService sendMailService;

    @InjectMocks
    VacationApprovalController vacationApprovalController;

    HttpServletRequest request;

    final String UID = "c6a32c38-3649-44ce-aee7-c639a351e7e2";

    VacationApproval vacationApproval;
    Employee employee;
    Manager manager;
    Vacation vacation;
    DictionaryItem vacationType;

    VacationApprovalForm vacationApprovalForm;
    BindingResult errors;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        setDefaultData();

        errors = new BeanPropertyBindingResult(vacationApprovalForm, "vacationsForm");
        request = new MockHttpServletRequest();
        sendMailService = new SendMailService();
        vacationApprovalProcessService = new VacationApprovalProcessService();

        when(vacationApprovalService.getVacationApproval(UID)).thenReturn(vacationApproval);
    }

    private void setDefaultData(){
        // подготовка
        vacationApprovalForm = new VacationApprovalForm();

        Region region = new Region();
        region.setId(1);
        region.setName("Region name");

        employee = new Employee();
        employee.setId(55);
        employee.setName("User");
        employee.setRegion(region);

        manager = new Manager();
        manager.setEmployee(employee);

        vacationType = new DictionaryItem();
        vacationType.setId(1);
        vacationType.setValue("vacation type");

        vacation = new Vacation();
        vacation.setId(1);
        vacation.setEmployee(employee);
        vacation.setType(vacationType);
        vacation.setBeginDate(new Date());
        vacation.setEndDate(new Date());

        vacationApproval = new VacationApproval();
        vacationApproval.setManager(manager.getEmployee());
        vacationApproval.setVacation(vacation);
    }


    @Test
    public void testVacationApprovalShow1(){
        vacationApproval.setResult(null);

        ModelAndView result = vacationApprovalController.vacationApprovalShow(UID, vacationApprovalForm, request);

        assertNotNull(result);
        assertNotNull(vacationApprovalForm.getApprovalList());
        assertNotNull(vacationApprovalForm.getMessage());
        assertEquals(vacationApprovalForm.getButtonsVisible(), "");
    }
    @Test
    public void testVacationApprovalShow2(){
        vacationApproval.setResult(true);

        ModelAndView result = vacationApprovalController.vacationApprovalShow(UID, vacationApprovalForm, request);

        assertNotNull(result);
        assertNotNull(vacationApprovalForm.getApprovalList());
        assertNotNull(vacationApprovalForm.getMessage());
        assertTrue(vacationApprovalForm.getButtonsVisible().length() > 0);
    }

    @Test
    public void testVacationApprovalSaveResult(){
        try {
            ModelAndView result = vacationApprovalController.vacationApprovalSaveResult(UID, true, vacationApprovalForm, request);
            assertNotNull(result);
            assertNotNull(vacationApprovalForm.getApprovalList());
            assertNotNull(vacationApprovalForm.getMessage());
            assertTrue(vacationApprovalForm.getButtonsVisible().length() > 0);

            verify(vacationApprovalService, times(1)).store(vacationApproval);
            verify(sendMailService, times(1)).performVacationAcceptanceMailing(vacationApproval);
            verify(vacationApprovalProcessService, times(1)).checkVacationIsApproved(vacationApproval.getVacation());
        }catch (Exception exc){
        }
    }

}
