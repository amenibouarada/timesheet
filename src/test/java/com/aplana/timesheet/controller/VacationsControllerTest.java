package com.aplana.timesheet.controller;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EmployeeHelper;
import com.aplana.timesheet.util.TimeSheetUser;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.time.DateUtils;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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

    final String projectListJSON = "JSON project list";
    final String employeeListJSON = "JSON employee list";
    final String vacationsListJSON = "JSON vacation list";
    final String holidayListJSON = "JSON holiday list";
    final String approvalPart = "approvalPart";
    final Integer vacationsNeedsApprovalCount = 10;

    HttpSession session;
    Division division;
    Employee employee;
    TimeSheetUser timeSheetUser;
    List<Employee> employeeList;
    List<Division> divisions;
    List<Region> regionList;
    List<Integer> regionListIntegers;
    List<Holiday> holidayList;
    List<DictionaryItem> vacationTypes;
    DictionaryItem vacationType;
    DictionaryItem approval;
    Vacation vacation;
    List<Vacation> vacationList;
    Project project;
    Manager manager;
    Date fromDate;
    Date toDate;

    VacationsForm vacationsForm;
    BindingResult errors;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        setDefaultData();

        errors = new BeanPropertyBindingResult(vacationsForm, "vacationsForm");

        session = new MockHttpSession();
        session.setAttribute("employeeId", employee.getId());

        vacationsFormValidator = new VacationsFormValidator();

        when(messageSource.getMessage(anyString(), (Object[]) any(), (Locale) any())).
                                                                        thenReturn(approvalPart);
        when(request.getSession(false)).                                thenReturn(session);
        when(securityService.getSecurityPrincipal()).                   thenReturn(timeSheetUser);
        when(divisionService.getDivisions()).                           thenReturn(divisions);
        when(projectService.getProjectListJson(divisions)).             thenReturn(projectListJSON);
        when(employeeService.getEmployees((List) any(), (List) any(), (List) any(), (List) any(), (Date) any(), (Date) any(), anyBoolean())).
                                                                        thenReturn(employeeList);
        when(employeeService.find(employee.getId())).                   thenReturn(employee);
        when(employeeService.isShowAll((HttpServletRequest) any())).    thenReturn(false);
        when(employeeHelper.getEmployeeListWithDivisionAndManagerAndRegionJson(divisions, employeeService.isShowAll(request))).
                                                                        thenReturn(employeeListJSON);
        when(employeeHelper.makeEmployeeListInJSON(employeeList)).      thenReturn(employeeListJSON);
        when(regionService.getRegions()).                               thenReturn(regionList);
        when(calendarService.getHolidaysForRegion((Date) any(), (Date) any(), (Region) any())).
                                                                        thenReturn(holidayList);
        when(dictionaryItemService.find(vacationType.getId())).         thenReturn(vacationType);
        when(dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId())).
                                                                        thenReturn(vacationTypes);
        when(vacationService.getVacationListByRegionJSON((List < Vacation >) any())).
                                                                        thenReturn(vacationsListJSON);
        when(vacationService.getHolidayListJSON(resetHours(fromDate), resetHours(toDate))).
                                                                        thenReturn(holidayListJSON);
        when(vacationService.findVacations(employee.getId(), resetHours(fromDate), resetHours(toDate), vacationType)).
                                                                        thenReturn(vacationList);
        when(vacationService.findVacationsNeedsApprovalCount(employee.getId())).
                                                                        thenReturn(vacationsNeedsApprovalCount);
        when(vacationService.findVacationsNeedsApproval(employee.getId())).
                                                                        thenReturn(vacationList);
    }

    private void setDefaultData(){
        // подготовка
        vacationsForm = new VacationsForm();

        division = new Division();
        division.setId(1);
        division.setName("Дивизион №1");
        divisions = new ArrayList<Division>();
        divisions.add(division);

        Region region = new Region();
        region.setId(1);
        region.setName("Region name");
        regionList = new ArrayList<Region>();
        regionList.add(region);

        regionListIntegers = new ArrayList<Integer>();
        regionListIntegers.add(1);

        employee = new Employee();
        employee.setId(55);
        employee.setDivision(division);
        employee.setName("User");
        employee.setRegion(region);
        timeSheetUser = new TimeSheetUser(employee, new ArrayList<GrantedAuthority>());
        employeeList = new ArrayList<Employee>();
        employeeList.add(employee);

        Holiday holiday = new Holiday();
        Calendar calendar = new Calendar(2013, 8, "September");
        calendar.setCalDate(new Timestamp(System.currentTimeMillis()));
        holiday.setCalDate(calendar);
        holidayList = new ArrayList<Holiday>();
        holidayList.add(holiday);

        vacationTypes = new ArrayList<DictionaryItem>();
        vacationType = new DictionaryItem();
        vacationType.setId(1);
        vacationType.setValue("vacation type");
        vacationTypes.add(vacationType);

        vacation = new Vacation();
        vacation.setId(1);
        vacation.setEmployee(employee);
        vacationList = new ArrayList<Vacation>();
        vacationList.add(vacation);

        project = new Project();
        project.setId(1);
        project.setName("Project");

        manager = new Manager();
        manager.setEmployee(employee);

        approval = new DictionaryItem();
        approval.setId(1);
        approval.setValue("approval");

        fromDate = new Date();
        toDate = DateUtils.addDays(new Date(), 1);
    }

    private Date resetHours(Date date){
        return DateTimeUtil.stringToDate(DateTimeUtil.dateToString(date), DateTimeUtil.DATE_PATTERN);
    }

    @Test
    public void testPrepareToShowVacations(){
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

        vacationsForm.setDivisionId(division.getId());
        vacationsForm.setEmployeeId(employee.getId());
        vacationsForm.setCalFromDate(DateTimeUtil.dateToString(fromDate));
        vacationsForm.setCalToDate(DateTimeUtil.dateToString(toDate));
        vacationsForm.setProjectId(project.getId());
        vacationsForm.setManagerId(manager.getEmployee().getId());
        vacationsForm.setRegions(regionListIntegers);
        vacationsForm.setVacationId(vacation.getId());
        vacationsForm.setApprovalId(approval.getId());
        vacationsForm.setRegions(regionListIntegers);
        vacationsForm.setVacationType(1);

        ModelAndView result = vacationsController.showVacations(vacationsForm, errors);

        // проверка
        assertNotNull(result);
        Map<String,Object> resultMap = result.getModel();
        assertNotNull(resultMap);

        assertEquals("Сравниваем проект", project.getId(), resultMap.get("projectId"));
        assertEquals("Сравниваем список регионов", regionList.toString(), resultMap.get("regionList").toString());
        assertEquals("Сравниваем установленную дату начала периода",
                DateTimeUtil.dateToString(fromDate), DateTimeUtil.dateToString((Date)resultMap.get("calFromDate")));
        assertEquals("Сравниваем установленную дату конца периода",
                DateTimeUtil.dateToString(toDate), DateTimeUtil.dateToString((Date)resultMap.get("calToDate")));
        assertEquals("Сравниваем список отпусков", Lists.reverse(vacationList), resultMap.get("vacationsList"));
        assertEquals("Сравниваем список отпусков сгруппированных по регионам", vacationsListJSON, resultMap.get("vacationListByRegionJSON"));
        assertEquals("Сравниваем количество дней в отпусках", (new HashMap<Vacation,Integer>()), resultMap.get("calDays"));
        assertEquals("Сравниваем количество рабочих дней в отпусках", (new HashMap<Vacation,Integer>()), resultMap.get("workDays"));
        assertEquals("Сравниваем список выходных", holidayListJSON, resultMap.get("holidayList"));
        assertEquals("Сравниваем сравниваем типы отпусков", vacationTypes, resultMap.get("vacationTypes"));
        assertEquals("Сравниваем количество отпусков нуждающихся в подтверждении", 10, resultMap.get("vacationNeedsApprovalCount"));
        assertEquals("Сравниваем сравниваем сообщение о подтверждении отпусков", approvalPart, resultMap.get("approvalPart"));
        assertEquals("Сравниваем года", DateTimeUtil.getYear(toDate) - DateTimeUtil.getYear(fromDate) + 1, resultMap.get("years"));
        assertEquals("Сравниваем суммарное количество подтвержденных отпусков", 0, resultMap.get("summaryApproved"));
        assertEquals("Сравниваем суммарное количество отклоненных отпусков", 0, resultMap.get("summaryRejected"));
        assertEquals("Сравниваем текущего пользователя", employee, resultMap.get("curEmployee"));
        assertEquals("Сравниваем количество календарных дней в периоде", new ArrayList<VacationInYear>(), resultMap.get("calDaysCount"));
        assertEquals("Сравниваем руководителя", manager.getEmployee().getId(), resultMap.get(VacationsForm.MANAGER_ID));
        assertEquals("Сравниваем сервисы", vacationService, resultMap.get("vacationService"));
    }

    @Test
    public void testShowVacationsNeedsApproval(){

        vacationsForm.setVacationId(vacation.getId());

        ModelAndView result = vacationsController.showVacationsNeedsApproval(vacationsForm, errors);

        // проверка
        assertNotNull(result);
        Map<String,Object> resultMap = result.getModel();
        assertNotNull(resultMap);

        verify(vacationService, times(1)).deleteVacation(vacation.getId());
        assertEquals("Проверка, что id отпуска обнулилось", vacationsForm.getVacationId(), null);

        assertEquals("Сравниваем текущего пользователя", employee, resultMap.get("curEmployee"));
        assertEquals("Сравниваем список отпусков", Lists.reverse(vacationList), resultMap.get("vacationsList"));
        assertEquals("Сравниваем количество дней в отпусках", (new HashMap<Vacation, Integer>()), resultMap.get("calDays"));
        assertEquals("Сравниваем количество рабочих дней в отпусках", (new HashMap<Vacation, Integer>()), resultMap.get("workDays"));
    }

    @Test
    public void testGetVacationsCount(){
        vacationsForm.setVacationId(vacation.getId());
        String result = vacationsController.getVacationsCount();
        assertEquals("(" + vacationsNeedsApprovalCount + ")", result);
    }

    @Test
    public void testGetEmployeeList(){
        String result = vacationsController.getEmployeeList(vacationsForm);
        assertEquals(result, employeeListJSON);
    }
}

