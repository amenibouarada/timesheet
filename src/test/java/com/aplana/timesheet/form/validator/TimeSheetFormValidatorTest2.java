package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/* попытка сделать функциональный тест */

public class TimeSheetFormValidatorTest2 extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetFormValidatorTest2.class);

    private TimeSheetForm timeSheetForm = new TimeSheetForm();
    private Errors errors; // ошибки валидатора
    private StringBuilder errorsText; // текстовый вид ошибок

    @Autowired
    TimeSheetFormValidator timeSheetFormValidator;
    @Autowired
    ProjectRoleService projectRoleService;
    @Autowired
    DivisionService divisionService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    DictionaryItemService dictionaryItemService;
    @Autowired
    ProjectService projectService;
    @Autowired
    ProjectTaskService projectTaskService;
    @Autowired
    TimeSheetService timeSheetService;

    /* вспомогательная функция для формирования текста ошибок */
    private void fillErrorsText() {
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        errorsText = resultStr;
    }

    @Before
    public void setUp() {
    /*--- правильный отчёт ---*/

        // Разработчик
        ProjectRole projectRole = projectRoleService.find(16);

        // Центр заказной разработки
        Division division = divisionService.find(1);

        // Лебедев Н.А. (не лидер дивизиона)
        Employee employee = employeeService.find(690);

        // Дата отчёта (рабочий день) за который нет отчёта
        String calDate = DateTimeUtil.dateToString(timeSheetService.getLastWorkdayWithoutTimesheet(employee.getId()));

        // Нагрузка на завтра (Всё хорошо)
        DictionaryItem effort = dictionaryItemService.find(125);

        // Тип активности (Проект)
        DictionaryItem actType = dictionaryItemService.find(12);

        // Категория активности (ПО)
        DictionaryItem actCat = dictionaryItemService.find(47);

        //АпланаЦР - Система списания занятости
        Project project = projectService.find(23);

        // Задача (Развитие и поддержка TimeSheet)
        ProjectTask projectTask = projectTaskService.find(276);

        // Место работы
        DictionaryItem workplace = dictionaryItemService.find(49);

        TimeSheetTableRowForm tsRow1 = new TimeSheetTableRowForm();
        tsRow1.setProjectTaskId(projectTask.getId()); // project_task - Развитие и поддержка TimeSheet
        tsRow1.setDescription("Описание строки 1");
        tsRow1.setDuration("8.0");
//        tsRow1.setProblem("Проблема строки 1");
//        tsRow1.setOther("Прочее строки 1");
        tsRow1.setProjectId(project.getId());
        tsRow1.setActivityTypeId(actType.getId());
        tsRow1.setActivityCategoryId(actCat.getId());
        tsRow1.setProjectRoleId(projectRole.getId());
        tsRow1.setWorkplaceId(workplace.getId());

        timeSheetForm.setDivisionId(division.getId());
        timeSheetForm.setEmployeeId(employee.getId());
        List<TimeSheetTableRowForm> tsRowList = new ArrayList<TimeSheetTableRowForm>();
        tsRowList.add(tsRow1);
        timeSheetForm.setTimeSheetTablePart(tsRowList);
        timeSheetForm.setCalDate(calDate);
        timeSheetForm.setPlan("План работ формы 1");
//        timeSheetForm.setOvertimeCause(107);
//        timeSheetForm.setOvertimeCauseComment("Я трудоголик и работаю без выходных");
//        timeSheetForm.setTypeOfCompensation(117);
        timeSheetForm.setEffortInNextDay(effort.getId());

        /* инициализация внутренних инфо полей */
        errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");
        errorsText = new StringBuilder();
    }

/*----------------------------------------------------------------------------------------------------------------------
*   Блок 0
*   заведомо правильный отчёт
*---------------------------------------------------------------------------------------------------------------------*/
    /* Заведомо правильный отчёт */
    @Test
    public void testValidate0_0() throws Exception {
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

/*----------------------------------------------------------------------------------------------------------------------
*   Блок №1
*   производится тестирование каждого поля формы TimeSheetFrom на значение null и заведомо "плохие" (пример -1, ""...)
*---------------------------------------------------------------------------------------------------------------------*/
    /* Зануляем подразделение */
    @Test
    public void testValidate1_01() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setDivisionId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Подраздел") != -1);
        assertTrue(errorsText.indexOf("не выбран") != -1);
    }

    /* Неверное подразделение */
    @Test
    public void testValidate1_02() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setDivisionId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("неверн") != -1);
        assertTrue(errorsText.indexOf("подраздел") != -1);
    }

    /* Зануляем человека */
    @Test
    public void testValidate1_03() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setEmployeeId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("не выбран") != -1);
    }

    /* Неверный человек */
    @Test
    public void testValidate1_04() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setEmployeeId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = " + errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Неверные данные") != -1);
    }

    /* Зануляем строки отчёта */
    @Test
    public void testValidate1_05() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setTimeSheetTablePart(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = " + errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Пустой массив строк отчёта */
    @Test
    public void testValidate1_06() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setTimeSheetTablePart(new ArrayList<TimeSheetTableRowForm>());
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = " + errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зануляем дату отчёта */
    @Test
    public void testValidate1_07() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setCalDate(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("дат") != -1);
    }

    /* Пустая дата отчёта */
    @Test
    public void testValidate1_08() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setCalDate("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("дат") != -1);
    }

    /* Неверная дата отчёта */
    @Test
    public void testValidate1_09() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setCalDate("ха ха ха");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = " + errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("дат") != -1);
    }

    /* Зануляем план на следуюший день */
    @Test
    public void testValidate1_10() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setPlan(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("планы") != -1);
    }

    /* план на следуюший день пуст */
    @Test
    public void testValidate1_11() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setPlan("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("планы") != -1);
    }

    /* план на следуюший день состоит из одного слова */
    @Test
    public void testValidate1_12() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setPlan("Однослово");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("не могут быть менее 2х слов") != -1);
    }

    /* Зануляем причину пере/недоработки (тесты на правильность пере/недоработки будут ниже) */
    @Test
    public void testValidate1_13() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setOvertimeCause(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Причины нет в базе (тесты на правильность пере/недоработки будут ниже) */
    @Test
    public void testValidate1_14() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setOvertimeCause(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зануляем комментарий к причине пере/недоработки (тесты на правильность пере/недоработки будут ниже) */
    @Test
    public void testValidate1_15() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setOvertimeCauseComment(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зачищаем комментарий к причине пере/недоработки (тесты на правильность пере/недоработки будут ниже) */
    @Test
    public void testValidate1_16() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setOvertimeCauseComment("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зануляем тип компенсации (тесты на пере/недоработку будут ниже) */
    @Test
    public void testValidate1_17() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setTypeOfCompensation(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Тип компенсации не найден в базе (тесты на пере/недоработку будут ниже) */
    @Test
    public void testValidate1_18() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setTypeOfCompensation(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зануляем оценку загруженности на след день */
    @Test
    public void testValidate1_19() throws Exception {
        timeSheetForm.setEffortInNextDay(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(1, errors.getAllErrors().size());
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("оценк") != -1);
    }

    /* Ошибочная оценка загруженности на след день */
    @Test
    public void testValidate1_20() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setEffortInNextDay(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("оценк") != -1);
    }

/*----------------------------------------------------------------------------------------------------------------------
*   Блок №2, тесты комбинаций входящих значений
*   проверка 8 часов в день с указанием причин переработки недоработки и работы в выходные/праздничные дни
* --------------------------------------------------------------------------------------------------------------------*/
    /* часов в отчёте меньше 8(+1-3) и нет причины */
    @Test
    public void testValidate2_01() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("4.0");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("причин") != -1);
        assertTrue(errorsText.indexOf("недораб") != -1);
    }

    /* часов в отчёте больше 8(+1-3) и нет причины */
    @Test
    public void testValidate2_02() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("10.0");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("причин") != -1);
        assertTrue(errorsText.indexOf("перераб") != -1);
    }

    /* часов в отчёте меньше 8(+1-3) и есть одна из неверных причин */
    @Test
    public void testValidate2_03() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("4.0");
        timeSheetForm.setOvertimeCause(107);//Моя собственная инициатива
        timeSheetForm.setOvertimeCauseComment("Я трудоголик и работаю без выходных");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("неверн") != -1);
        assertTrue(errorsText.indexOf("причин") != -1);
        assertTrue(errorsText.indexOf("недораб") != -1);
    }

    /* часов в отчёте меньше 8(+1-3) и есть одна из верных причин */
    @Test
    public void testValidate2_04() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("4.0");
        timeSheetForm.setOvertimeCause(102);//Опоздал
        timeSheetForm.setOvertimeCauseComment("Переводил бабушек через дорогу");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* часов в отчёте больше 8(+1-3) и есть одна из неверных причин */
    @Test
    public void testValidate2_05() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("10.0");
        timeSheetForm.setOvertimeCause(102);//Опоздал
        timeSheetForm.setOvertimeCauseComment("Переводил бабушек через дорогу");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("неверн") != -1);
        assertTrue(errorsText.indexOf("причин") != -1);
        assertTrue(errorsText.indexOf("перераб") != -1);
    }

    /* часов в отчёте больше 8(+1-3) и есть одна из верных причин */
    @Test
    public void testValidate2_06() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("11.0");
        timeSheetForm.setOvertimeCause(107);//Моя собственная инициатива
        timeSheetForm.setOvertimeCauseComment("Я трудоголик и работаю без выходных");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* работа в выходной день без причины и без типа компенсации*/
    @Test
    public void testValidate2_07() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setCalDate("2013-01-01");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(2, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необход") != -1);
        assertTrue(errorsText.indexOf("причин") != -1);
        assertTrue(errorsText.indexOf("выход") != -1);
        assertTrue(errorsText.indexOf("Не указан") != -1);
        assertTrue(errorsText.indexOf("компен") != -1);
    }

    /* работа в выходной день с неверной причиной и без компенсации*/
    @Test
    public void testValidate2_08() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setCalDate("2013-01-01");
        timeSheetForm.setOvertimeCause(104);//В офисе произошло ЧП
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(2, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("неверн") != -1);
        assertTrue(errorsText.indexOf("причин") != -1);
        assertTrue(errorsText.indexOf("выход") != -1);
        assertTrue(errorsText.indexOf("Не указан") != -1);
        assertTrue(errorsText.indexOf("компен") != -1);
    }

    /* работа в выходной день с верной причиной и c компенсацией */
    @Test
    public void testValidate2_09() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setCalDate("2013-01-01");
        timeSheetForm.setOvertimeCause(120);//Просьба руководителя
        timeSheetForm.setTypeOfCompensation(116);//отгул
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* работа в выходной день с верной причиной и без компенсацией */
    @Test
    public void testValidate2_10() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setCalDate("2013-01-01");
        timeSheetForm.setOvertimeCause(120);//Просьба руководителя
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Не указан") != -1);
        assertTrue(errorsText.indexOf("компен") != -1);
    }

/*----------------------------------------------------------------------------------------------------------------------
*   Блок №3
*   тестирование строки отчёта TimeSheetFrom -> TimeSheetTableRowForm на null и заведомо "плохие" значения
*---------------------------------------------------------------------------------------------------------------------*/
    /* Зануляем название задачи */
    @Test
    public void testValidate3_01() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectTaskId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо выбрать проектную задачу") != -1);
    }

    /* Ложная задача */
    @Test
    public void testValidate3_02() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectTaskId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Неверная проектная задача") != -1);
    }

    /* Зануление описания */
    @Test
    public void testValidate3_03() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDescription(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать комментарии") != -1);
    }

    /* Пустое описание */
    @Test
    public void testValidate3_04() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDescription("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать комментарии") != -1);
    }

    /* Зануление время работы */
    @Test
    public void testValidate3_05() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(2, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать часы") != -1);
        assertTrue(errorsText.indexOf("недоработки") != -1);
    }

    /* Пустая строка времени работы */
    @Test
    public void testValidate3_06() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(2, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать часы") != -1);
        assertTrue(errorsText.indexOf("недоработки") != -1);
    }

    /* Неконвертируемая строка время работы */
    @Test
    public void testValidate3_07() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("хрен проссыш");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(2, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("указано не верно") != -1);
        assertTrue(errorsText.indexOf("недоработки") != -1);
    }

    /* Использование запятой вместо точки */
    @Test
    public void testValidate3_07_1() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("8,55");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зануление строки Проблемы */
    @Test
    public void testValidate3_08() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProblem(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Пустая строка Проблема */
    @Test
    public void testValidate3_09() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProblem("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зануляем проект */
    @Test
    public void testValidate3_10() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(2, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать название проекта") != -1);
        assertTrue(errorsText.indexOf("Необходимо указать категорию активности") != -1);
    }

    /* Ложный проект */
    @Test
    public void testValidate3_11() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Неверный проект\\пресейл") != -1);
    }

    /* Зануляем тип активности (считается что вся строка пуста) */
    @Test
    public void testValidate3_12() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Ложный тип активности (считается что вся строка пуста если тип не в словаре) */
    @Test
    public void testValidate3_13() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Зануляем категорию активности */
    @Test
    public void testValidate3_14() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityCategoryId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать категорию активности") != -1);
    }

    /* Ложная категория активности */
    @Test
    public void testValidate3_15() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityCategoryId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Неверная категория активности") != -1);
    }

    /* Зануляем проектную роль */
    @Test
    public void testValidate3_16() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectRoleId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать проектную роль") != -1);
    }

    /* Ложная проектная роль */
    @Test
    public void testValidate3_17() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectRoleId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Неверная проектная роль") != -1);
    }

    /* Зануляем проектную роль */
    @Test
    public void testValidate3_18() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setWorkplaceId(null);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Необходимо указать место работы") != -1);
    }

    /* Ложная проектная роль */
    @Test
    public void testValidate3_19() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setWorkplaceId(-1);
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Выбрано недопустимое место работы") != -1);
    }

/*----------------------------------------------------------------------------------------------------------------------
*   Блок №4
*   Логические проверки условий из постановки http://conf.aplana.com/pages/viewpage.action?pageId=1874744
*---------------------------------------------------------------------------------------------------------------------*/

    /* Проверяем связку проекта и подразделения */
    @Test
    public void testValidate4_01() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setDivisionId(1); // Центр заказной разработки
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectId(38); // МТС-ТП
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectTaskId(10); // ДКР
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("выбран неверный проект") != -1);
    }

    /* Проверяем связку проектной роли и типа активности */
    @Test
    public void testValidate4_02() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectRoleId(10); // руководитель
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("неправильная проектная роль") != -1);
    }

    /* Проверяем связку проектной роли, типа активности и активности */
    @Test
    public void testValidate4_03() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectRoleId(19); // руководитель
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("неправильная активность") != -1);
    }

    /* Проверяем связку проекта и проектной задачи */
    @Test
    public void testValidate4_04() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectTaskId(259); // Пресейлы СБ РФ
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Неверная проектная задача") != -1);
    }

    /* Проверяем связку проекта и проектной задачи (не активной) */
    @Test
    public void testValidate4_05() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectTaskId(258); // Приемочные испытания
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(1, errors.getAllErrors().size());
        /* по наличию ошибок */
        assertTrue(errorsText.toString(), errors.hasErrors());
        /* по наличию ключевой фразы в ошибке */
        assertTrue(errorsText.indexOf("Неверная проектная задача") != -1);
    }

    /* Проверяем требование о наличие коментария для РП и РЦ*/
    @Test
    public void testValidate4_06() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setEmployeeId(1); // РЦ
        timeSheetForm.setCalDate(
                DateTimeUtil.dateToString(
                        timeSheetService.getLastWorkdayWithoutTimesheet(timeSheetForm.getEmployeeId()
                        )
                )
        );
        timeSheetForm.getTimeSheetTablePart().get(0).setDescription("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Проверяем требование о наличие коментария для РП и РЦ*/
    @Test
    public void testValidate4_07() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setEmployeeId(474); //РП
        timeSheetForm.setCalDate(
                DateTimeUtil.dateToString(
                        timeSheetService.getLastWorkdayWithoutTimesheet(timeSheetForm.getEmployeeId()
                        )
                )
        );
        timeSheetForm.getTimeSheetTablePart().get(0).setDescription("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        /* по кол-ву ошибок */
        assertEquals(0, errors.getAllErrors().size());
    }

    /* план на следуюший день пуст ошибки для РП и РЦ нет */
    @Test
    public void testValidate4_08() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setEmployeeId(1); // РЦ
        timeSheetForm.setCalDate(
                DateTimeUtil.dateToString(
                        timeSheetService.getLastWorkdayWithoutTimesheet(timeSheetForm.getEmployeeId()
                        )
                )
        );
        timeSheetForm.setPlan("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }

    /* план на следуюший день пуст ошибки для РП и РЦ нет */
    @Test
    public void testValidate4_09() throws Exception {
        /* дополнения "правильного" объекта новыми данными */
        timeSheetForm.setEmployeeId(474); // РП
        timeSheetForm.setCalDate(
                DateTimeUtil.dateToString(
                        timeSheetService.getLastWorkdayWithoutTimesheet(timeSheetForm.getEmployeeId()
                        )
                )
        );
        timeSheetForm.setPlan("");
        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);
        /* анализ результата */
        fillErrorsText();
        logger.info("Expected error = "+errorsText);
        assertEquals(0, errors.getAllErrors().size());
    }


}
