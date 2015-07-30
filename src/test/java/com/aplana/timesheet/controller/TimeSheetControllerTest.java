package com.aplana.timesheet.controller;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.TypesOfTimeSheetEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.form.validator.TimeSheetFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/* Получился модульный тест, все запросы 'налево' (в сторонние сервисы) возвращают нужные значения */
@RunWith(MockitoJUnitRunner.class)
public class TimeSheetControllerTest extends AbstractTest {

    @Mock
    SecurityService securityService;
    @Mock
    EmployeeService employeeService;
    @Mock
    TimeSheetService timeSheetService;
    @Mock
    DictionaryItemService dictionaryItemService;
    @Mock
    SendMailService sendMailService;
    @Mock
    JiraService jiraService;
    @Mock
    TSPropertyProvider propertyProvider;
    @Mock
    DivisionService divisionService;
    @Mock
    AvailableActivityCategoryService availableActivityCategoryService;
    @Mock
    ProjectService projectService;
    @Mock
    ProjectTaskService projectTaskService;
    @Mock
    ProjectRoleService projectRoleService;
    @Mock
    TimeSheetFormValidator tsFormValidator;
    @Mock
    OvertimeCauseService overtimeCauseService;
    @InjectMocks
    TimeSheetController timeSheetController;

    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testShowMainForm() throws Exception {
        /* входные данные */
        // Дата отчёта (далее считаем что это НЕ выходной)
        String calDate = "2013-04-01";
        String calDateJSON = "JSON date";

        // Центр заказной разработки
        Division division = new Division();
        division.setId(1);
        division.setName("Дивизион №1");
        List<Division> divisions = new ArrayList<Division>();
        divisions.add(division);

        // Лебедев Н.А. (не лидер дивизиона)
        Employee employee = new Employee();
        employee.setId(690);
        //employee.setJob(projectRole);
        employee.setDivision(division);
        employee.setName("User");
        TimeSheetUser timeSheetUser = new TimeSheetUser(employee, new ArrayList<GrantedAuthority>());
        String employeeListJSON = "List of best workers";

        // нагрузка на след день
        List<DictionaryItem> effortList = new ArrayList<DictionaryItem>();
        DictionaryItem effort = new DictionaryItem();
        effort.setId(1);
        effort.setValue("Very hard");
        effortList.add(effort);

        // типы активности
        List<DictionaryItem> typesOfActivity = new ArrayList<DictionaryItem>();
        DictionaryItem typeOfActivity = new DictionaryItem();
        typeOfActivity.setId(2);
        typeOfActivity.setValue("Test");
        typesOfActivity.add(typeOfActivity);
        String typesOfActivityJSON = "JSON type of activity";

        // рабочие места
        List<DictionaryItem> workPlaces = new ArrayList<DictionaryItem>();
        DictionaryItem workplace = new DictionaryItem();
        workplace.setId(3);
        workplace.setValue("Офис");
        workPlaces.add(workplace);
        String workPlacesJSON = "JSON workPlaces is Office, Home, ...";

        // причины переработки
        List<DictionaryItem> overtimeCauses = new ArrayList<DictionaryItem>();
        DictionaryItem overtimeCause = new DictionaryItem();
        overtimeCause.setId(4);
        overtimeCause.setValue("Трудоголик");
        overtimeCauses.add(overtimeCause);
        String overtimeCausesJSON = "JSON overtime causes";

        //
        List<DictionaryItem> unfinishedDayCauses = new ArrayList<DictionaryItem>();
        DictionaryItem unfinishedDayCause = new DictionaryItem();
        unfinishedDayCause.setId(5);
        unfinishedDayCause.setValue("Сопливил");
        unfinishedDayCauses.add(unfinishedDayCause);
        String unfinishedDayCausesJSON = "JSON unfinished days";

        // категория активности
        List<DictionaryItem> categorysOfActivity = new ArrayList<DictionaryItem>();
        DictionaryItem categoryOfActivity = new DictionaryItem();
        categoryOfActivity.setId(6);
        categoryOfActivity.setValue("Прятки");
        categorysOfActivity.add(categoryOfActivity);
        String categorysOfActivityJSON = "JSON category of activity list";
        String availableActCategoriesJSON = "JSON aviable category of activity";

        // Проекты
        String projectListJSON = "JSON project list";
        List<Project> projectsWithCq = new ArrayList<Project>();
        Project projectWithCq = new Project();
        projectWithCq.setId(7);
        projectWithCq.setName("Mega project!!!");
        projectsWithCq.add(projectWithCq);
        String projectsWithCqJSON = "JSON projects with CQ";

        // проектные роли
        List<ProjectRole> projectRoles = new ArrayList<ProjectRole>();
        ProjectRole projectRole = new ProjectRole();
        projectRole.setId(8);
        projectRole.setName("Lazy manager");
        projectRole.setCode("MG");
        projectRoles.add(projectRole);
        String projectRolesJSON = "List of lazy managers";

        String listOfActDescriptionJSON = "ListOfActDescription";

        // типы компенсации
        List<DictionaryItem> typesOfCompensation = new ArrayList<DictionaryItem>();
        DictionaryItem typeCompensation = new DictionaryItem();
        typeCompensation.setId(9);
        typeCompensation.setValue("Выдача товаром");
        typesOfCompensation.add(typeCompensation);

        // причины работы в выхи
        String workOnHolidayCausesJSON = "JSON rabota ne volk v les ne ubezhit";

        /* определяем поведение сервисов */
        when(securityService.getSecurityPrincipal()).thenReturn(timeSheetUser);
        when(employeeService.find(employee.getId())).thenReturn(employee);
        when(timeSheetService.getSelectedCalDateJson((TimeSheetForm) any())).thenReturn(calDateJSON);
        when(timeSheetService.getEffortList()).thenReturn(effortList);
        when(dictionaryItemService.getTypesOfActivity()).thenReturn(typesOfActivity);
        when(dictionaryItemService.getDictionaryItemsInJson(typesOfActivity)).thenReturn(typesOfActivityJSON);
        when(dictionaryItemService.getWorkplaces()).thenReturn(workPlaces);
        when(dictionaryItemService.getDictionaryItemsInJson(workPlaces)).thenReturn(workPlacesJSON);
        when(dictionaryItemService.getOvertimeCauses()).thenReturn(overtimeCauses);
        when(dictionaryItemService.getDictionaryItemsInJson(overtimeCauses)).thenReturn(overtimeCausesJSON);
        when(dictionaryItemService.getUnfinishedDayCauses()).thenReturn(unfinishedDayCauses);
        when(dictionaryItemService.getDictionaryItemsInJson(unfinishedDayCauses)).thenReturn(unfinishedDayCausesJSON);
        when(propertyProvider.getOvertimeThreshold()).thenReturn(3D);
        when(propertyProvider.getUndertimeThreshold()).thenReturn(1D);
        when(divisionService.getDivisions()).thenReturn(divisions);
        when(employeeService.isShowAll((HttpServletRequest) any())).thenReturn(Boolean.TRUE);
        //when(employeeHelper.getEmployeeListWithLastWorkdayJson(divisions, Boolean.TRUE, Boolean.TRUE)).thenReturn(employeeListJSON);
        when(dictionaryItemService.getCategoryOfActivity()).thenReturn(categorysOfActivity);
        when(dictionaryItemService.getDictionaryItemsInJson(categorysOfActivity)).thenReturn(categorysOfActivityJSON);
        when(availableActivityCategoryService.getAvailableActCategoriesJson()).thenReturn(availableActCategoriesJSON);
        when(projectService.getProjectListJson(divisions)).thenReturn(projectListJSON);
        when(projectService.getProjectsWithCq()).thenReturn(projectsWithCq);
        when(projectTaskService.getProjectTaskListJson(projectsWithCq)).thenReturn(projectsWithCqJSON);
        when(projectRoleService.getProjectRoles()).thenReturn(projectRoles);
        when(projectRoleService.getProjectRoleListJson(anyList())).thenReturn(projectRolesJSON);
        when(timeSheetService.getListOfActDescription()).thenReturn(listOfActDescriptionJSON);
        when(dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.TYPES_OF_COMPENSATION.getId()))
                .thenReturn(typesOfCompensation);
        when(dictionaryItemService.getDictionaryItemsInJson(DictionaryEnum.WORK_ON_HOLIDAY_CAUSE.getId()))
                .thenReturn(workOnHolidayCausesJSON);

        when(timeSheetService.getListsToMAV(request)).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, Object> result = new HashMap<String, Object>();

                List<DictionaryItem> typesOfActivity = dictionaryItemService.getTypesOfActivity();
                result.put("actTypeList", typesOfActivity);

                String typesOfActivityJson = dictionaryItemService.getDictionaryItemsInJson(typesOfActivity);
                result.put("actTypeJson", typesOfActivityJson);

                String workplacesJson = dictionaryItemService.getDictionaryItemsInJson(dictionaryItemService.getWorkplaces());
                result.put("workplaceJson", workplacesJson);

                result.put("overtimeCauseJson", dictionaryItemService.getDictionaryItemsInJson(dictionaryItemService
                        .getOvertimeCauses()));
                result.put("unfinishedDayCauseJson", dictionaryItemService.getDictionaryItemsInJson(dictionaryItemService
                        .getUnfinishedDayCauses()
                ));
                result.put("overtimeThreshold", propertyProvider.getOvertimeThreshold());
                result.put("undertimeThreshold", propertyProvider.getUndertimeThreshold());

                List<Division> divisions = divisionService.getDivisions();
                result.put("divisionList", divisions);

                //String employeeListJson = employeeHelper.getEmployeeListWithLastWorkdayJson(divisions, employeeService.isShowAll(request), true);
                //result.put("employeeListJson", employeeListJson);

                List<DictionaryItem> categoryOfActivity = dictionaryItemService.getCategoryOfActivity();
                result.put("actCategoryList", categoryOfActivity);

                String actCategoryListJson = dictionaryItemService.getDictionaryItemsInJson(categoryOfActivity);
                result.put("actCategoryListJson", actCategoryListJson);

                result.put("availableActCategoriesJson", availableActivityCategoryService.getAvailableActCategoriesJson());

                result.put("projectListJson", projectService.getProjectListJson(divisions));
                result.put(
                        "projectTaskListJson",
                        projectTaskService.getProjectTaskListJson(projectService.getProjectsWithCq())
                );

                List<ProjectRole> projectRoleList = projectRoleService.getProjectRoles();

                for (int i = 0; i < projectRoleList.size(); i++) {
                    if (projectRoleList.get(i).getCode().equals("ND")) {  // Убираем из списка роль "Не определена" APLANATS-270
                        projectRoleList.remove(i);
                        break;
                    }
                }

                result.put("projectRoleList", projectRoleList);
                result.put("projectRoleListJson", projectRoleService.getProjectRoleListJson(projectRoleList));

                result.put("listOfActDescriptionJson", timeSheetService.getListOfActDescription());
                result.put(
                        "typesOfCompensation",
                        dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.TYPES_OF_COMPENSATION.getId())
                );
                result.put(
                        "workOnHolidayCauseJson",
                        dictionaryItemService.getDictionaryItemsInJson(DictionaryEnum.WORK_ON_HOLIDAY_CAUSE.getId())
                );

                return result;
            }
        });
        /* тест */
        ModelAndView result = timeSheetController.showMainForm(calDate, employee.getId(), null);

        /* проверка вызовов */

        /* анализ результата */
        assertNotNull(result);

        Map<String, Object> resultMap = result.getModel();
        assertNotNull(resultMap);

        TimeSheetForm actualTS = (TimeSheetForm) resultMap.get("timeSheetForm");
        assertNotNull(actualTS);

        assertEquals("timesheet", result.getViewName());
        assertEquals("Сравниваем отправителя отчёта", employee.getId(), actualTS.getEmployeeId());
        assertEquals("Сравниваем подразделение", employee.getDivision().getId(), actualTS.getDivisionId());
        assertEquals("Сравниваем дату отчёта", calDateJSON, resultMap.get("selectedCalDateJson"));
        assertEquals("Сравниваем нагрузку", effortList, resultMap.get("effortList"));
        assertEquals("[{row:'0', role:''}]", resultMap.get("selectedProjectRolesJson"));
        assertEquals("[{row:'0', task:''}]", resultMap.get("selectedProjectTasksJson"));
        assertEquals("[{row:'0', project:''}]", resultMap.get("selectedProjectsJson"));
        assertEquals("[{row:'0', workplace:''}]", resultMap.get("selectedWorkplaceJson"));
        assertEquals("[{row:'0', actCat:''}]", resultMap.get("selectedActCategoriesJson"));
        assertEquals("Сравниваем список типов активности", typesOfActivity, resultMap.get("actTypeList"));
        assertEquals("Сравниваем список типов активности в JSON", typesOfActivityJSON, resultMap.get("actTypeJson"));
        assertEquals("Сравниваем мест работы", workPlacesJSON, resultMap.get("workplaceJson"));
        assertEquals("Сравниваем причины недоработок", overtimeCausesJSON, resultMap.get("overtimeCauseJson"));
        assertEquals("Сравниваем непонятные дни", unfinishedDayCausesJSON, resultMap.get("unfinishedDayCauseJson"));
        assertEquals("Сравниваем границу паники", 3D, resultMap.get("overtimeThreshold"));
        assertEquals("Сравниваем границу строгости", 1D, resultMap.get("undertimeThreshold"));
        assertEquals("Сравниваем список подразделений", divisions, resultMap.get("divisionList"));
        assertEquals("Сравниваем работников", employeeListJSON, resultMap.get("employeeListJson"));
        assertEquals("Сравниваем список категорий активности", categorysOfActivity, resultMap.get("actCategoryList"));
        assertEquals("Сравниваем список категорий активности в JSON",
                categorysOfActivityJSON, resultMap.get("actCategoryListJson"));
        assertEquals("Сравниваем список доступных в JSON",
                availableActCategoriesJSON, resultMap.get("availableActCategoriesJson"));
        assertEquals("Сравниваем список проектов в JSON", projectListJSON, resultMap.get("projectListJson"));
        assertEquals("Сравниваем проектных задач в JSON", projectsWithCqJSON, resultMap.get("projectTaskListJson"));
        assertEquals("Сравниваем список проектных ролей", projectRoles, resultMap.get("projectRoleList"));
        assertEquals("Сравниваем список проектных ролей в JSON", projectRolesJSON, resultMap.get("projectRoleListJson"));
        assertEquals("Сравниваем описание", listOfActDescriptionJSON, resultMap.get("listOfActDescriptionJson"));
        assertEquals("Сравниваем список компенсаций", typesOfCompensation, resultMap.get("typesOfCompensation"));
        assertEquals("Сравниваем причины в JSON", workOnHolidayCausesJSON, resultMap.get("workOnHolidayCauseJson"));
    }

    @Test
    public void testShowCqCodes() throws Exception {
        /* заполняем входящие параметры */
        String expected = "cqcodes";
        /* выполняем тест */
        String actual = timeSheetController.showCqCodes();
        /* анализ результатов */
        assertEquals(expected, actual);
    }

    @Test
    public void testIndex() throws Exception {
        /* заполняем входящие параметры */
        String expected = "redirect:timesheet";
        /* выполняем тест */
        String actual = timeSheetController.index();
        /* анализ результатов */
        assertEquals(expected, actual);
    }

    @Test
    public void testNewReport() throws Exception {
        /* заполняем входящие параметры */
        String expected = "redirect:timesheet";
        /* выполняем тест */
        String actual = timeSheetController.newReport();
        /* анализ результатов */
        assertEquals(expected, actual);
    }

    @Test
    public void testSendNewReport() throws Exception {
        /* заполняем входящие параметры */
        String expected = "redirect:timesheet";
        /* выполняем тест */
        String actual = timeSheetController.sendNewReport();
        /* анализ результатов */
        assertEquals(expected, actual);
    }

    @Test
    public void testDelTimeSheet_01() {
        /* заполняем входящие параметры */
        String requestResult = "Test";
        String expected = "redirect:" + requestResult;

        Integer tsId = 1;

        Employee employee = new Employee();
        employee.setName("User");
        TimeSheetUser timeSheetUser = new TimeSheetUser(employee, new ArrayList<GrantedAuthority>());

        TimeSheet timeSheet = new TimeSheet();

        /* определяем поведение сервисов */
        when(securityService.getSecurityPrincipal()).thenReturn(timeSheetUser);
        when(timeSheetService.find(tsId)).thenReturn(timeSheet);
        when(request.getHeader("Referer")).thenReturn(requestResult);

        /* выполняем тест */
        String actual = timeSheetController.delTimeSheet(tsId, request);

        /* проверка вызовов */
        verify(timeSheetService, times(1)).delete(timeSheet);
        verify(sendMailService, times(1)).performTimeSheetDeletedMailing(timeSheet);

        /* анализ результатов */
        assertEquals(expected, actual);
    }

    @Test
    public void testDelTimeSheet_02() {
        /* заполняем входящие параметры */
        /* определяем поведение сервисов */
        when(securityService.getSecurityPrincipal()).thenReturn(null);

        /* выполняем тест */
        try {
            String actual = timeSheetController.delTimeSheet(1, request);
        } catch (Exception e) {
            /* анализ результатов */
            assertEquals("Не найден пользователь в контексте безопасности.", e.getMessage());
        }
    }

    @Test
    public void testGetPlans() {
        /* входные данные */
        String date = "2013-01-01";
        Integer employeeId = 1;
        String expected = JsonUtil.format(anObjectBuilder().withField("isDraft", aStringBuilder("false")));

        /* определяем поведение сервисов */
//        when(timeSheetService.getPlansJson(date, employeeId)).thenReturn(expected);
        when(timeSheetService.getPlansJsonBuilder(date, employeeId)).thenReturn(anObjectBuilder());
        when(timeSheetService.findForDateAndEmployeeByTypes(date, employeeId, Arrays.asList(TypesOfTimeSheetEnum.DRAFT))).thenReturn(null);
        /* тест */
        String actual = timeSheetController.getPlans(date, employeeId);

        /* проверка вызовов */
        verify(timeSheetService).getPlansJsonBuilder(date, employeeId);

        /* анализ результата */
        assertEquals(expected, actual);
    }

    @Test
    public void testGetJiraIssuesStr() {
        /* входные данные */
        String date = "2013-01-01";
        Integer employeeId = 1;
        Integer projectId = 1;
        String expected = "В джире много интересного";

        /* определяем поведение сервисов */
        when(jiraService.getDayIssues(employeeId, date, projectId)).thenReturn(expected);

        /* тест */
        String actual = timeSheetController.getJiraIssuesStr(employeeId, date, projectId, request);

        /* проверка вызовов */
        verify(jiraService).getDayIssues(employeeId, date, projectId);

        /* анализ результата */
        assertEquals(expected, actual);
    }

    @Test
    public void testIsDivisionLeader() {
        /* входные данные */
        Integer employeeId = 1;
        String expected = "{\"isDivisionLeader\":true}";
        /* определяем поведение сервисов */
        when(employeeService.isEmployeeDivisionLeader(employeeId)).thenReturn(Boolean.TRUE);

        /* тест */
        String actual = "false"; //timeSheetController.isDivisionLeader(employeeId);

        /* проверка вызовов */
        verify(employeeService).isEmployeeDivisionLeader(employeeId);

        /* анализ результата */
        assertEquals(expected, actual);
    }

    @Test
    public void testSendTimeSheet_01() {
        /* входные данные */
        TimeSheetForm timeSheetForm = new TimeSheetForm();
        BindingResult errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");
        TimeSheet timeSheet = new TimeSheet();
        /* определяем поведение сервисов */
        when(timeSheetService.storeTimeSheet(timeSheetForm, TypesOfTimeSheetEnum.REPORT)).thenReturn(timeSheet);
        /* тест */
        ModelAndView result = timeSheetController.sendTimeSheet(timeSheetForm, errors, new Locale("ru"));
        /* проверка вызовов */
        verify(tsFormValidator).validate(timeSheetForm, errors);
        verify(overtimeCauseService, times(1)).store(timeSheet, timeSheetForm);
        verify(sendMailService, times(1)).performMailing(timeSheetForm);
        /* анализ результата */
        assertNotNull(result);
        assertNotNull(result.getModel());
        assertEquals("Сравниваем входную и выходную модели", timeSheetForm, result.getModel().get("timeSheetForm"));
    }

    /* форма с ошибками */
    @Test
    public void testSendTimeSheet_02() {
        /* входные данные */
        String selectedProjectsJSON = "JSON project selected";
        String selectedProjectRolesJSON = "JSON project role selected";
        String selectedProjectTasksJSON = "JSON project task selected";
        String selectedWorkplaceJSON = "JSON workplace selected";
        String selectedActCategoriesJSON = "JSON activity category selected";
        String selectedCalDateJSON = "JSON calendar date selected";
        // нагрузка на след день
        List<DictionaryItem> effortList = new ArrayList<DictionaryItem>();
        DictionaryItem effort = new DictionaryItem();
        effort.setId(1);
        effort.setValue("Very hard");
        effortList.add(effort);

        TimeSheetForm timeSheetForm = new TimeSheetForm();
        timeSheetForm.setPlan("");
        timeSheetForm.setOvertimeCauseComment("");

        List<TimeSheetTableRowForm> timeSheetTableRowFormList = new ArrayList<TimeSheetTableRowForm>();
        timeSheetTableRowFormList.add(new TimeSheetTableRowForm());

        timeSheetForm.setTimeSheetTablePart(timeSheetTableRowFormList);

        BindingResult errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");
        errors.rejectValue("employeeId",
                "error.tsform.isnull",
                "Не удалось получить параметры формы");
        TimeSheet timeSheet = new TimeSheet();

        /* определяем поведение сервисов */
        when(timeSheetService.storeTimeSheet(timeSheetForm, TypesOfTimeSheetEnum.REPORT)).thenReturn(timeSheet);
        when(timeSheetService.getSelectedProjectsJson(timeSheetForm)).thenReturn(selectedProjectsJSON);
        when(timeSheetService.getSelectedProjectRolesJson(timeSheetForm)).thenReturn(selectedProjectRolesJSON);
        when(timeSheetService.getSelectedProjectTasksJson(timeSheetForm)).thenReturn(selectedProjectTasksJSON);
        when(timeSheetService.getSelectedWorkplaceJson(timeSheetForm)).thenReturn(selectedWorkplaceJSON);
        when(timeSheetService.getSelectedActCategoriesJson(timeSheetForm)).thenReturn(selectedActCategoriesJSON);
        when(timeSheetService.getSelectedCalDateJson(timeSheetForm)).thenReturn(selectedCalDateJSON);
        when(timeSheetService.getEffortList()).thenReturn(effortList);

        /* тест */
        ModelAndView result = timeSheetController.sendTimeSheet(timeSheetForm, errors, new Locale("ru"));

        /* проверка вызовов */
        verify(tsFormValidator).validate(timeSheetForm, errors);
        verify(overtimeCauseService, never()).store(timeSheet, timeSheetForm);
        verify(sendMailService, never()).performMailing(timeSheetForm);
        /* анализ результата */
        assertNotNull(result);

        Map<String, Object> resultMap = result.getModel();
        assertNotNull(resultMap);

        TimeSheetForm actualTS = (TimeSheetForm) resultMap.get("timeSheetForm");
        assertNotNull(actualTS);

        assertEquals("Сравниваем ошибки", errors.getAllErrors(), resultMap.get("errors"));
        assertEquals("Сравниваем список проектов", selectedProjectsJSON, resultMap.get("selectedProjectsJson"));
        assertEquals("Сравниваем список проектных ролей",
                selectedProjectRolesJSON, resultMap.get("selectedProjectRolesJson"));
        assertEquals("Сравниваем список проектных задач",
                selectedProjectTasksJSON, resultMap.get("selectedProjectTasksJson"));
        assertEquals("Сравниваем мест работы", selectedWorkplaceJSON, resultMap.get("selectedWorkplaceJson"));
        assertEquals("Сравниваем список категорий активности",
                selectedActCategoriesJSON, resultMap.get("selectedActCategoriesJson"));
        assertEquals("Сравниваем дату отчёта", selectedCalDateJSON, resultMap.get("selectedCalDateJson"));
        assertEquals("Сравниваем нагрузку", effortList, resultMap.get("effortList"));
    }

}
