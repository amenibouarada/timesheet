package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import org.junit.Before;
import org.junit.BeforeClass;
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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/* попытка сделать функциональный тест */

public class TimeSheetFormValidatorTest2 extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetFormValidatorTest2.class);

    private final TimeSheetForm timeSheetForm = new TimeSheetForm();

    @Autowired
    TimeSheetFormValidator timeSheetFormValidator;

    @Before
    public void setUp() {
        /* правильный отчёт */

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

        // Дата отчёта (рабочий день)
        String calDate = "2013-04-01";

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
        tsRow1.setDuration("8.0");
//        tsRow1.setProblem("Проблема строки 1");
//        tsRow1.setOther("Прочее строки 1");
        tsRow1.setProjectId(project.getId());
        tsRow1.setActivityTypeId(actType.getId());
        tsRow1.setActivityCategoryId(actCat.getId());
        tsRow1.setProjectRoleId(projectRole.getId());
        tsRow1.setWorkplaceId(49);


        timeSheetForm.setDivisionId(division.getId());
        timeSheetForm.setEmployeeId(employee.getId());
        List<TimeSheetTableRowForm> tsRowList = new ArrayList<TimeSheetTableRowForm>();
        tsRowList.add(tsRow1);
        timeSheetForm.setTimeSheetTablePart(tsRowList);
        timeSheetForm.setCalDate(calDate);
        timeSheetForm.setPlan("План работ формы 1");
        timeSheetForm.setTotalDuration(8);
//        timeSheetForm.setOvertimeCause(107);
//        timeSheetForm.setOvertimeCauseComment("Я трудоголик и работаю без выходных");
//        timeSheetForm.setTypeOfCompensation(117);
        timeSheetForm.setEffortInNextDay(effort.getId());
    }

    @Test
    public void testValidate1() throws Exception {

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        assertFalse(resultStr.toString(), errors.hasErrors());
    }

    /* часов в отчёте меньше 8(+1-3) и нет причины */
    @Test
    public void testValidate2() throws Exception {
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("4.0");

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());
    }

    /* часов в отчёте больше 8(+1-3) и нет причины */
    @Test
    public void testValidate3() throws Exception {
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("10.0");

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());
    }

    /* часов в отчёте меньше 8(+1-3) и есть одна из не верных причин */
    @Test
    public void testValidate4() throws Exception {
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("4.0");
        timeSheetForm.setOvertimeCause(107);//Моя собственная инициатива
        timeSheetForm.setOvertimeCauseComment("Я трудоголик и работаю без выходных");

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());
    }

    /* часов в отчёте меньше 8(+1-3) и есть одна из верных причин */
    @Test
    public void testValidate5() throws Exception {
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("4.0");
        timeSheetForm.setOvertimeCause(102);//Опоздал
        timeSheetForm.setOvertimeCauseComment("Переводил бабушек через дорогу");

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertFalse(resultStr.toString(), errors.hasErrors());
    }

    /* часов в отчёте больше 8(+1-3) и есть одна из не верных причин */
    @Test
    public void testValidate6() throws Exception {
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("10.0");
        timeSheetForm.setOvertimeCause(102);//Опоздал
        timeSheetForm.setOvertimeCauseComment("Переводил бабушек через дорогу");

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());
    }

    /* часов в отчёте больше 8(+1-3) и есть одна из верных причин */
    @Test
    public void testValidate7() throws Exception {
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("4.0");
        timeSheetForm.setOvertimeCause(107);//Моя собственная инициатива
        timeSheetForm.setOvertimeCauseComment("Я трудоголик и работаю без выходных");

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertFalse(resultStr.toString(), errors.hasErrors());
    }

    /* работа в выходной день без причины и без типа компенсации*/
    @Test
    public void testValidate8() throws Exception {
        timeSheetForm.setCalDate("2013-01-01");

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(2, errors.getAllErrors().size());
    }

    /* работа в выходной день с не верной причиной и без компенсации*/
    @Test
    public void testValidate9() throws Exception {
        timeSheetForm.setCalDate("2013-01-01");
        timeSheetForm.setOvertimeCause(104);//В офисе произошло ЧП

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(2, errors.getAllErrors().size());
    }

    /* работа в выходной день с верной причиной и c компенсацией */
    @Test
    public void testValidate10() throws Exception {
        timeSheetForm.setCalDate("2013-01-01");
        timeSheetForm.setOvertimeCause(108);//Просьба руководителя
        timeSheetForm.setTypeOfCompensation(116);//отгул

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertFalse(resultStr.toString(), errors.hasErrors());
    }

    /* работа в выходной день с верной причиной и без компенсацией */
    @Test
    public void testValidate11() throws Exception {
        timeSheetForm.setCalDate("2013-01-01");
        timeSheetForm.setOvertimeCause(108);//Просьба руководителя

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());
    }

    /* Для типов активностей Проект, Проектный присейл, Присейл заполнение полей «Название проекта/пресейла»,
    «Проектная роль», «Категория активности», «Часы» и «Комментарии»  является обязательным. */
    @Test
    public void testValidate12() throws Exception {

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(12);//Проект
        timeSheetFormValidator.validate(timeSheetForm, errors);
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(42);//Проектный присейл
        timeSheetFormValidator.validate(timeSheetForm, errors);
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(13);//Присейл
        timeSheetFormValidator.validate(timeSheetForm, errors);

        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertFalse(resultStr.toString(), errors.hasErrors());
        assertEquals(0, errors.getAllErrors().size());
    }

    /* Для типов активностей Проект, Проектный присейл, Присейл заполнение полей «Название проекта/пресейла»,
    «Проектная роль», «Категория активности», «Часы» и «Комментарии»  является обязательным. */
    @Test
    public void testValidate13() throws Exception {

        Errors errors = new BeanPropertyBindingResult(timeSheetForm, "timeSheetForm");

        /* тест */
        timeSheetForm.getTimeSheetTablePart().get(0).setTaskName(null);
        timeSheetForm.getTimeSheetTablePart().get(0).setProjectRoleId(null);
        timeSheetForm.getTimeSheetTablePart().get(0).setActivityCategoryId(null);
        timeSheetForm.getTimeSheetTablePart().get(0).setDuration("");
        timeSheetForm.getTimeSheetTablePart().get(0).setDescription(null);

        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(12);//Проект
        timeSheetFormValidator.validate(timeSheetForm, errors);

        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(42);//Проектный присейл
        timeSheetFormValidator.validate(timeSheetForm, errors);

        timeSheetForm.getTimeSheetTablePart().get(0).setActivityTypeId(13);//Присейл
        timeSheetFormValidator.validate(timeSheetForm, errors);


        /* анализ результата */
        StringBuilder resultStr = new StringBuilder("\n");
        if (errors.hasErrors()) {
            for (ObjectError item : errors.getAllErrors()) {
                resultStr.append(item.getDefaultMessage()).append("\n");
            }
        }
        logger.info("Expected error = "+resultStr);
        assertTrue(resultStr.toString(), errors.hasErrors());
        assertEquals(6*3, errors.getAllErrors().size());
    }


}
