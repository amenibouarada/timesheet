package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/* Получился модульный тест, все запросы 'налево' (в сторонние сервисы) возвращают нужные значения */
@RunWith(MockitoJUnitRunner.class)
public class TimeSheetFormValidatorTest1 extends AbstractTest {

    @Mock
    TimeSheetService timeSheetServiceMock;
    @Mock
    ProjectService projectServiceMock;
    @Mock
    ProjectRoleService projectRoleServiceMock;
    @Mock
    EmployeeService employeeServiceMock;
    @Mock
    CalendarService calendarServiceMock;
    @Mock
    DivisionService divisionServiceMock;
    @Mock
    DictionaryItemService dictionaryItemServiceMock;
    @Mock
    ProjectTaskService projectTaskServiceMock;
    @Mock
    TSPropertyProvider propertyProviderMock;
    @Mock
    VacationService vacationServiceMock;
    @InjectMocks
    private TimeSheetFormValidator timeSheetFormValidator;

    @Test
    public void testValidate() throws Exception {
        /* подготавливаемся к тесту */

        // Разработчик
        ProjectRole projectRole = new ProjectRole();
        projectRole.setCode("DV");
        projectRole.setId(16);

        // Центр заказной разработки
        Division division = new Division();
        division.setId(1);

        // Лебедев Н.А. (не лидер дивизиона)
        Employee employee = new Employee();
        employee.setId(690);
        employee.setJob(projectRole);
        employee.setDivision(division);

        // Дата отчёта (далее считаем что это НЕ выходной)
        String calDate = "2013-04-01";
        Boolean isHoliday = false;

        // Нагрузка на завтра (Всё хорошо)
        DictionaryItem effort = new DictionaryItem();
        effort.setId(125);

        // Тип активности (Проект)
        DictionaryItem actType = new DictionaryItem();
        actType.setId(12);

        // Категория активности (ПО)
        DictionaryItem actCat = new DictionaryItem();
        actCat.setId(47);

        //АпланаЦР - Система списания занятости
        Project project = new Project();
        project.setId(23);
        project.setCqRequired(true);

        // Задача (Развитие и поддержка TimeSheet)
        ProjectTask projectTask = new ProjectTask();
        projectTask.setId(276);
        projectTask.setProject(project);

        TimeSheetTableRowForm tsRow1 = new TimeSheetTableRowForm();
        tsRow1.setTaskName(projectTask.getId()); // project_task - Развитие и поддержка TimeSheet
        tsRow1.setDescription("Описание строки 1");
        tsRow1.setDuration("2.0");
        tsRow1.setProblem("Проблема строки 1");
        tsRow1.setOther("Прочее строки 1");
        tsRow1.setProjectId(project.getId());
        tsRow1.setActivityTypeId(actType.getId());
        tsRow1.setActivityCategoryId(actCat.getId());
        tsRow1.setProjectRoleId(projectRole.getId());
        tsRow1.setWorkplaceId(49);

        TimeSheetForm testedForm1 = new TimeSheetForm();
        testedForm1.setDivisionId(division.getId());
        testedForm1.setEmployeeId(employee.getId());
        List<TimeSheetTableRowForm> tsRowList = new ArrayList<TimeSheetTableRowForm>();
        tsRowList.add(tsRow1);
        testedForm1.setTimeSheetTablePart(tsRowList);
        testedForm1.setCalDate(calDate);
        testedForm1.setPlan("План работ формы 1");
        testedForm1.setOvertimeCause(107);
        testedForm1.setOvertimeCauseComment("Я трудоголик и работаю без выходных");
        testedForm1.setTypeOfCompensation(117);
        testedForm1.setEffortInNextDay(effort.getId());

        Errors errors = new BeanPropertyBindingResult(testedForm1, "timeSheetForm");

        /* устанавливаем поведение вызова внешних модулей */
        when(employeeServiceMock.find(employee.getId())).thenReturn(employee);
        when(employeeServiceMock.isEmployeeDivisionLeader(employee.getId())).thenReturn(false);

        when(divisionServiceMock.find(division.getId())).thenReturn(division);

        when(calendarServiceMock.find(anyString())).thenReturn(new Calendar());// всегда есть день отчёта
        when(calendarServiceMock.isHoliday((Date) any(), (Employee) any())).thenReturn(isHoliday);

        when(dictionaryItemServiceMock.find(effort.getId(), DictionaryEnum.EFFORT_IN_NEXTDAY.getId())).thenReturn(effort);
        when(dictionaryItemServiceMock.find(actCat.getId())).thenReturn(actCat);

        when(vacationServiceMock.isDayVacationWithoutPlanned((Employee) any(), (Date) any())).thenReturn(false);// нет незапланированного отпуска

        when(propertyProviderMock.getOvertimeThreshold()).thenReturn(1d);
        when(propertyProviderMock.getUndertimeThreshold()).thenReturn(3d);

        when(projectServiceMock.find(project.getId())).thenReturn(project);
        when(projectServiceMock.findActive(project.getId())).thenReturn(project);

        when(projectRoleServiceMock.findActive(projectRole.getId())).thenReturn(projectRole);

        when(projectTaskServiceMock.find(project.getId(), projectTask.getId())).thenReturn(projectTask);

        /* тест */
        timeSheetFormValidator.validate(testedForm1, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        assertFalse(resultStr.toString(), errors.hasErrors());
    }


}
