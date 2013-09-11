package com.aplana.timesheet.controller;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EmployeeHelper;
import com.aplana.timesheet.util.TimeSheetUser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author: iziyangirov
 */
public class VacationsControllerTest extends AbstractTest {

    @Mock
    HttpServletRequest request;
    @Mock
    SecurityService securityService;
    @Mock
    EmployeeService employeeService;
    @Mock
    DivisionService divisionService;
    @Mock
    ProjectService projectService;
    @Mock
    EmployeeHelper employeeHelper;
    @Mock
    RegionService regionService;
    @Mock
    CalendarService calendarService;
    @Mock
    MessageSource messageSource;
    @Mock
    DictionaryItemService dictionaryItemService;
    @Mock
    VacationsFormValidator vacationsFormValidator;
    @Mock
    VacationService vacationService;

    @InjectMocks
    VacationsController vacationsController;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }



    @Test
    public void testPrepareToShowVacations(){
        // подготовка
        VacationsForm vacationsForm = new VacationsForm();
        HttpSession session = new MockHttpSession();
        session.setAttribute("employeeId", 55);
        vacationsFormValidator = new VacationsFormValidator();
        vacationService = new VacationService();
        messageSource = new StaticWebApplicationContext();

        String projectListJSON = "JSON project list";
        String employeeListJson = "JSON employee list";

        Division division = new Division();
        division.setId(1);
        division.setName("Дивизион №1");
        List<Division> divisions = new ArrayList<Division>();
        divisions.add(division);

        Employee employee = new Employee();
        employee.setId(55);
        employee.setDivision(division);
        employee.setName("User");
        TimeSheetUser timeSheetUser = new TimeSheetUser(employee, new ArrayList<GrantedAuthority>());

        Region region = new Region();
        region.setId(1);
        region.setName("Region name");
        List<Region> regionList = new ArrayList<Region>();
        regionList.add(region);

        Holiday holiday = new Holiday();
        Calendar calendar = new Calendar(2013, 8, "September");
        calendar.setCalDate(new Timestamp(System.currentTimeMillis()));
        holiday.setCalDate(calendar);
        List<Holiday> holidayList = new ArrayList<Holiday>();
        holidayList.add(holiday);

        List<DictionaryItem> vacations = new ArrayList<DictionaryItem>();
        DictionaryItem vacation = new DictionaryItem();
        vacation.setId(1);
        vacation.setValue("vacation type");
        vacations.add(vacation);

        when(request.getSession(false)).thenReturn(session);
        when(securityService.getSecurityPrincipal()).thenReturn(timeSheetUser);
        when(employeeService.find(employee.getId())).thenReturn(employee);
        when(divisionService.getDivisions()).thenReturn(divisions);
        when(projectService.getProjectListJson(divisions)).thenReturn(projectListJSON);
        when(employeeService.isShowAll((HttpServletRequest) any())).thenReturn(false);
        when(employeeHelper.getEmployeeListWithDivisionAndManagerAndRegionJson(
                divisions, employeeService.isShowAll(request))).thenReturn(employeeListJson);
        when(regionService.getRegions()).thenReturn(regionList);
        when(calendarService.getHolidaysForRegion((Date) any(), (Date) any(), (Region) any())).thenReturn(holidayList);
        when(dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId())).thenReturn(vacations);

        // тест
        ModelAndView result = vacationsController.prepareToShowVacations(vacationsForm);

        // проверка
        assertNotNull(result);

        Map<String,Object> resultMap = result.getModel();
        assertNotNull(resultMap);

        assertEquals("Сравниваем проект", division.getId(), vacationsForm.getDivisionId());
        assertEquals("Сравниваем сотрудника", employee.getId(), vacationsForm.getEmployeeId());
        assertEquals("Сравниваем установленную дату начала периода", DateTimeUtil.currentMonthFirstDay(), vacationsForm.getCalFromDate());
        assertEquals("Сравниваем установленную дату конца периода", DateTimeUtil.currentYearLastDay(), vacationsForm.getCalToDate());
        assertEquals("Сравниваем тип отпуска", (Integer)0, vacationsForm.getVacationType());
        assertEquals("Сравниваем список регионов", (Integer)VacationsForm.ALL_VALUE, vacationsForm.getRegions().get(0));
        assertEquals("Сравниваем способ отображения", (Integer)(VacationsForm.VIEW_TABLE), vacationsForm.getViewMode());
    }

    @Test
    // ToDo неплохо было бы протестировать AbstractControllerForEmployee
    public void testShowVacations(){

        VacationsForm vacationsForm = new VacationsForm();
        vacationsForm.setVacationType(1);
        List<Integer> regionList = new ArrayList<Integer>();
        regionList.add(1);
        vacationsForm.setRegions(regionList);

        BindingResult errors = new BeanPropertyBindingResult(vacationsForm, "vacationsForm");

        Employee employee = new Employee();
        employee.setId(55);
        employee.setName("User");
        TimeSheetUser timeSheetUser = new TimeSheetUser(employee, new ArrayList<GrantedAuthority>());
        when(securityService.getSecurityPrincipal()).thenReturn(timeSheetUser);

        DictionaryItem vacationType = new DictionaryItem();
        vacationType.setId(1);
        vacationType.setValue("vacation type");
        when(dictionaryItemService.find((Integer) any())).thenReturn(vacationType);

        ModelAndView result = vacationsController.showVacations(vacationsForm, errors);
                 /*
        assertEquals("Сравниваем проект", );
        assertEquals("Сравниваем список регионов", );
        assertEquals("Сравниваем установленную дату начала периода", );
        assertEquals("Сравниваем установленную дату конца периода", );
        assertEquals("Сравниваем тип отпуска", );
        assertEquals("Сравниваем список отпусков", );
        assertEquals("Сравниваем список отпусков сгруппированных по регионам", );
        assertEquals("Сравниваем количество дней в отпусках", );
        assertEquals("Сравниваем количество рабочих дней в отпусках", );
        assertEquals("Сравниваем список выходных", );
        assertEquals("Сравниваем сравниваем типы отпусков", );
        assertEquals("Сравниваем количество отпусков нуждающихся в подтверждении", );
        assertEquals("Сравниваем сравниваем сообщение о подтверждении отпусков", );
        assertEquals("Сравниваем года", );
        assertEquals("Сравниваем суммарное количество подтвержденных отпусков", );
        assertEquals("Сравниваем суммарное количество отклоненных отпусков", );
        assertEquals("Сравниваем текущего пользователя", );
        assertEquals("Сравниваем количество календарных дней в периоде", );
        assertEquals("Сравниваем руководителя", );
        assertEquals("Сравниваем ", );

             */

        /*
       modelAndView.addObject("projectId", vacationsForm.getProjectId() == null ? 0 : vacationsForm.getProjectId());
       modelAndView.addObject("regionList", getRegionList());
       modelAndView.addObject("calFromDate", dateFrom);
       modelAndView.addObject("calToDate", dateTo);
       modelAndView.addObject("vacationsList", revertList(vacations));
       modelAndView.addObject("vacationListByRegionJSON", getVacationListByRegionJSON(vacations));
       modelAndView.addObject("calDays", calDays);
       modelAndView.addObject("workDays", workDays);
       modelAndView.addObject("holidayList", getHolidayListJSON(dateFrom, dateTo));
       modelAndView.addObject("vacationTypes",
       modelAndView.addObject("vacationNeedsApprovalCount", vacationsNeedsApprovalCount);
       modelAndView.addObject("approvalPart", approvalPart);
       modelAndView.addObject("years", lastYear-firstYear+1);
       modelAndView.addObject("summaryApproved", summaryApproved);
       modelAndView.addObject("summaryRejected", summaryRejected);
       modelAndView.addObject("curEmployee", securityService.getSecurityPrincipal().getEmployee());
       modelAndView.addObject("calDaysCount", calAndWorkDaysList);
       modelAndView.addObject(VacationsForm.MANAGER_ID, vacationsForm.getManagerId());
       modelAndView.addObject("vacationService", vacationService);
        */

    }

    @Test
    public void testShowVacationsNeedsApproval(){

    }

    @Test
    public void testGetVacationsCount(){

    }

    @Test
    public void testGetEmployeeList(){

    }
}

