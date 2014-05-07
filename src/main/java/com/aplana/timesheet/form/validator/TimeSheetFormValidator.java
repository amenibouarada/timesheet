package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.enums.TSEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aplana.timesheet.enums.ProjectRolesEnum.*;

@Service
public class TimeSheetFormValidator extends AbstractValidator {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetFormValidator.class);

    @Autowired
    private TimeSheetService timeSheetService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private ProjectTaskService projectTaskService;
    @Autowired
    private TSPropertyProvider propertyProvider;
    @Autowired
    private VacationService vacationService;
    @Autowired
    private OvertimeCauseService overtimeCauseService;
    @Autowired
    private AvailableActivityCategoryService availableActivityCategoryService;
    @Autowired
    private BusinessTripService businessTripService;

    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(TimeSheetForm.class);
    }

    /**
     * Проверка корректности заполнения черновика
     * для единообразия вынесено на серверную часть
     *
     * @param tsForm
     * @param errors
     */
    public void validateDraft(TimeSheetForm tsForm, Errors errors) {
        // Для табличной части (по строчно).
        List<TimeSheetTableRowForm> tsTablePart = filterTable(tsForm);// удалим пустые строки
        tsForm.setTimeSheetTablePart(tsTablePart);
        logger.info("##### ");
        int notNullRowNumber = 0;
        if (tsTablePart != null && tsTablePart.size() != 0) {
            Integer selectedEmployeeId = tsForm.getEmployeeId();
            Employee employee = employeeService.find(selectedEmployeeId);
            ProjectRolesEnum emplJob = (employee != null)
                    ? getByCode(employee.getJob().getCode())
                    : NOT_DEFINED;
            checkForEffectiveActTypes(tsTablePart, tsForm.getEmployeeId(), errors);
            validateSelectedDate(tsForm, selectedEmployeeId, errors);
            for (TimeSheetTableRowForm formRow : tsTablePart) {
                Integer actTypeId = formRow.getActivityTypeId();
                if (actTypeId == null || actTypeId == 0) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                            "error.tsform.activity.category.required", getErrorMessageArgs(notNullRowNumber),
                            "Необходимо указать категорию активности в строке " + (notNullRowNumber + 1) + ".");
                }
            }
        } else {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                    "error.tsform.timesheet.requiredOne",
                    "Нельзя сохранить пустой черновик");
        }
    }

    public void validate(Object target, Errors errors) {
        TimeSheetForm tsForm = (TimeSheetForm) target;
        if (tsForm != null) {
            Integer selectedEmployeeId = tsForm.getEmployeeId();
            Employee employee = employeeService.find(selectedEmployeeId);
            ProjectRolesEnum emplJob = (employee != null)
                    ? getByCode(employee.getJob().getCode())
                    : NOT_DEFINED;

            if (emplJob == null) {
                logger.warn("emplJob is null");
            }

            validateDivision(tsForm, errors);
            validateEmployee(selectedEmployeeId, errors);
            validateSelectedDate(tsForm, selectedEmployeeId, errors);
            validateEffort(tsForm, errors);

            // Для табличной части (по строчно).
            List<TimeSheetTableRowForm> tsTablePart = filterTable(tsForm);// удалим пустые строки
            tsForm.setTimeSheetTablePart(tsTablePart);

            // Проверка рабочих часов на правильность формата
            validateDuration(tsForm, employee, errors, tsTablePart);
            // Проверка недоработок/переработок и  их причины
            validateCause(tsForm, employee, errors);

            // План на след. день нужен только если есть строки списания
            final boolean planNecessary = tsTablePart != null && !tsTablePart.isEmpty() && employee != null && employee.getDivision() != null && BooleanUtils.isTrue(employee.getDivision().getPlansRequired());
            validatePlan(tsForm, emplJob, planNecessary, errors);

            if (tsTablePart != null) {

                checkForEffectiveActTypes(tsTablePart, tsForm.getEmployeeId(), errors);

                logger.debug("TimeSheetForm table has {} lines.", tsTablePart.size());

                int notNullRowNumber = 0;

                for (TimeSheetTableRowForm formRow : tsTablePart) {
                    TypesOfActivityEnum actType = TypesOfActivityEnum.getById(formRow.getActivityTypeId());

                    validateProject(formRow, actType, notNullRowNumber, errors);
                    validateProjectAndDivision(tsForm.getDivisionId(), formRow, notNullRowNumber, errors);
                    validateWorkPlace(formRow, notNullRowNumber, errors);
                    validateProjectRole(formRow, notNullRowNumber, errors);
                    validateProjectRoleAndActivityType(formRow, notNullRowNumber, errors);
                    valdateCategoryOfActivity(formRow, emplJob, notNullRowNumber, errors);
                    validateProjectTask(formRow, notNullRowNumber, errors);
                    validateDescription(formRow, emplJob, notNullRowNumber, errors);

                    notNullRowNumber++;
                }

            /*
            Отчет может быть без записей
            if (tsTablePart.isEmpty()) {
                errors.reject("error.tsform.tablepart.required",
                        "В отчёте должны быть записи.");
            }*/
            } /*
        Отчет может быть без записей
        else {
            errors.reject("error.tsform.tablepart.required", "В отчёте должны быть записи.");
        }*/

        } else {
            errors.reject("error.tsform.isnull",
                    "Не удалось получить параметры формы");
            logger.error("Can not validate tsform, object is null");
        }
    }

    private void validateWorkPlace(TimeSheetTableRowForm formRow, int notNullRowNumber, Errors errors) {
        // Рабочее место не выбрано
        if (isNotChoosed(formRow.getWorkplaceId())) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].workplaceId",
                    "error.tsform.workplace.required", getErrorMessageArgs(notNullRowNumber),
                    "Необходимо указать место работы в строке " + (notNullRowNumber + 1) + ".");
            // Неверное мето работы
        } else if (!isWorkPlaceValid(formRow.getWorkplaceId())) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].workplaceId",
                    "error.tsform.workplace.wrongvalue", getErrorMessageArgs(notNullRowNumber),
                    "Выбрано недопустимое место работы в строке " + (notNullRowNumber + 1) + ".");
        }
    }

    private void checkForEffectiveActTypes(List<TimeSheetTableRowForm> timeSheetTablePart, Integer employeeId, Errors errors) {
        for (TimeSheetTableRowForm row : timeSheetTablePart) {
            if (!TypesOfActivityEnum.isEfficientActivity(row.getActivityTypeId())) {
                // Если есть хотя бы одна нерабочая активность - выведем ошибку. Нерабочие активности убраны из формы списания отчета
                errors.rejectValue("timeSheetTablePart", "error.tsform.tablepart.effectiveactivitytypesrequired", new Object[]{employeeId}, "error.tsform.tablepart.effectiveactivitytypesrequired");
                return;
            }
        }
    }

    /* удаляются все строки с типом активности не попадающем в список возможных */
    private List<TimeSheetTableRowForm> filterTable(TimeSheetForm tsForm) {
        List<TimeSheetTableRowForm> timeSheetTablePart = tsForm.getTimeSheetTablePart();
        if (timeSheetTablePart == null) {
            return null;
        }
        Iterable<TimeSheetTableRowForm> tsTablePart = Iterables.filter(timeSheetTablePart,
                new Predicate<TimeSheetTableRowForm>() {
                    @Override
                    public boolean apply(@Nullable TimeSheetTableRowForm timeSheetTableRowForm) {
                        // По каким-то неведомым причинам при нажатии на кнопку веб интерфейса
                        // "Удалить выбранные строки" (если выбраны промежуточные строки) они удаляются с формы, но
                        // в объект формы вместо них попадают null`ы. Мы эти строки удаляем из объекта формы. Если
                        // удалять последние строки (с конца табличной части формы), то все работает корректно.
                        // Также, если тип активности не выбран значит вся строка пустая, валидацию ее не проводим и удаляем
                        // UPD: теперь делаем фильтрацию
                        TypesOfActivityEnum actType =
                                TypesOfActivityEnum.getById(timeSheetTableRowForm.getActivityTypeId());
                        return actType != null;
                    }
                });

        return Lists.newArrayList(tsTablePart);
    }

    // <APLANATS-441> не менее 2х слов
    private final String inStringMoreThanTwoWordsRegex = "([^-\\p{LD}]+)?([-\\p{LD}]++([^-\\p{LD}]+)?+){2,}";

    private void validatePlan(TimeSheetForm tsForm, ProjectRolesEnum emplJob, boolean planNecessary, Errors errors) {

        String plan = tsForm.getPlan();
        // Планы на следующий рабочий день.
        if (planNecessary && emplJob != HEAD) {
            if (StringUtils.isBlank(plan)) {
                errors.rejectValue("plan",
                        "error.tsform.plan.required",
                        "Необходимо указать планы на следующий рабочий день.");
                return;
            }
            if (!plan.matches(inStringMoreThanTwoWordsRegex)) {
                errors.rejectValue("plan",
                        "error.tsform.plan.invalid",
                        "Планы на следующий день не могут быть менее 2х слов.");
            }
        }
    }

    private void validateDescription(TimeSheetTableRowForm formRow, ProjectRolesEnum emplJob, int notNullRowNumber, Errors errors) {
        String description = formRow.getDescription();
        // Необходимо указать комментарии
        logger.debug("Employee Job: {}", emplJob.getName());

        if (StringUtils.isBlank(description) && emplJob != HEAD) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].description",
                    "error.tsform.description.required", getErrorMessageArgs(notNullRowNumber),
                    "Необходимо указать комментарии в строке " + (notNullRowNumber + 1) + ".");
        }
    }

    private Object[] getErrorMessageArgs(int notNullRowNumber) {
        return new Object[]{"в строке №" + (notNullRowNumber + 1)};
    }

    @VisibleForTesting
    private void valdateCategoryOfActivity(TimeSheetTableRowForm formRow, ProjectRolesEnum emplJob, int notNullRowNumber, Errors errors) {
        Integer actCatId = formRow.getActivityCategoryId();
        Integer projectId = formRow.getProjectId();
        Integer typeActCatId = formRow.getActivityTypeId();

        // Не указана категория активности
        if ((isNotChoosed(actCatId) && (emplJob != HEAD))
                || (typeActCatId != TypesOfActivityEnum.NON_PROJECT.getId() && isNotChoosed(projectId) && isNotChoosed(actCatId))) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                    "error.tsform.activity.category.required", getErrorMessageArgs(notNullRowNumber),
                    "Необходимо указать категорию активности в строке " + (notNullRowNumber + 1) + ".");
            // Неверная категория активности
        } else if (!isActCatValid(actCatId, emplJob)) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                    "error.tsform.activity.category.invalid", getErrorMessageArgs(notNullRowNumber),
                    "Неверная категория активности в строке " + (notNullRowNumber + 1) + ".");
        }
    }

    private void validateProjectRole(TimeSheetTableRowForm formRow, int notNullRowNumber, Errors errors) {
        Integer actTypeId = formRow.getActivityTypeId();
        Integer projectRoleId = formRow.getProjectRoleId();

        // Не указана проектная роль
        if (TypesOfActivityEnum.isEfficientActivity(actTypeId)// APLANATS-276 Роль нужно указывать только для проектных видов деятельности
                && isNotChoosed(projectRoleId)
                ) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectRoleId",
                    "error.tsform.projectrole.required", getErrorMessageArgs(notNullRowNumber),
                    "Необходимо указать проектную роль в строке " + (notNullRowNumber + 1) + ".");
            // Неверная проектная роль
        } else if (!isProjectRoleValid(projectRoleId)) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectRoleId",
                    "error.tsform.projectrole.invalid", getErrorMessageArgs(notNullRowNumber),
                    "Неверная проектная роль в строке " + (notNullRowNumber + 1) + ".");
        }
    }

    private void validateProject(TimeSheetTableRowForm formRow, TypesOfActivityEnum actType, int notNullRowNumber, Errors errors) {
        Integer projectId = formRow.getProjectId();
        // Не указано название проекта
        // Не указано название пресейла
        if (actType != TypesOfActivityEnum.NON_PROJECT) {
            if ((actType == TypesOfActivityEnum.PROJECT || actType == TypesOfActivityEnum.PRESALE)
                    && isNotChoosed(projectId)
                    ) {
                errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectId",
                        actType == TypesOfActivityEnum.PRESALE
                                ? "error.tsform.presale.required"
                                : "error.tsform.project.required",
                        getErrorMessageArgs(notNullRowNumber),
                        "Необходимо указать название проекта в строке " + (notNullRowNumber + 1) + ".");
                // Неверный проект\пресейл
            } else if (!isProjectValid(projectId)) {
                errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectId",
                        "error.tsform.project.presale.invalid", getErrorMessageArgs(notNullRowNumber),
                        "Неверный проект\\пресейл в строке " + (notNullRowNumber + 1) + ".");
            }
        }
    }

    /* проверка на правильную комбинацию подразделения и проекта */
    private void validateProjectAndDivision(Integer divisionId, TimeSheetTableRowForm tsFromRows, int notNullRowNumber, Errors errors) {
        if (isDivisionValid(divisionId) && tsFromRows != null) {
            Integer projectId = tsFromRows.getProjectId();
            if (projectService.find(projectId) != null) {
                if (!divisionService.isValidDivisionProject(divisionId, projectId)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectId",
                            "error.tsform.division.project.presale.invalid", getErrorMessageArgs(notNullRowNumber),
                            "Для данного подразделения выбран неверный проект\\пресейл в строке " + (notNullRowNumber + 1) + ".");
                }
            }
        }
    }

    /* проверка на правильную комбинацию роли и активности */
    private void validateProjectRoleAndActivityType(TimeSheetTableRowForm tsFromRow, int notNullRowNumber, Errors errors) {
        DictionaryItem actType = dictionaryItemService.find(tsFromRow.getActivityTypeId());
        ProjectRole projectRole = projectRoleService.find(tsFromRow.getProjectRoleId());
        if (actType != null && projectRole != null) {
            List<AvailableActivityCategory> availableActivityCategoryList = availableActivityCategoryService.getAvailableActivityCategories(actType, projectRole);
            if (availableActivityCategoryList.isEmpty()) {
                errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityTypeId",
                        "error.tsform.projectrole.activitytype.invalid", getErrorMessageArgs(notNullRowNumber),
                        "Для данного типа активности выбрана неправильная проектная роль в строке " + (notNullRowNumber + 1) + ".");
            } else {
            /* заодно проверим категорию активности */
                if (tsFromRow.getActivityCategoryId() != null) {
                    DictionaryItem actCategory = dictionaryItemService.find(tsFromRow.getActivityCategoryId());
                    if (actCategory != null) {
                        Boolean finded = false;
                        for (AvailableActivityCategory item : availableActivityCategoryList) {
                            if (item.getActCat().equals(actCategory)) {
                                finded = true;
                                break;
                            }
                        }
                        if (!finded) {
                            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                                    "error.tsform.projectrole.activitytype.activitycategory.invalid", getErrorMessageArgs(notNullRowNumber),
                                    "Для данного типа активности и проектной роли указана неправильная активность в строке " + (notNullRowNumber + 1) + ".");
                        }
                    }
                }
            }
        }
    }

    private void validateProjectTask(TimeSheetTableRowForm formRow, int notNullRowNumber, Errors errors) {
        if (formRow.getActivityTypeId().equals(TypesOfActivityEnum.PROJECT_PRESALE.getId())) {
            return;
        }
        Integer projectId = formRow.getProjectId();
        Integer projectTaskId = formRow.getProjectTaskId();
        if (projectId != null) {
            Project project = projectService.find(projectId);
            // Необходимо указать проектную задачу
            if (project != null && project.isCqRequired()) {
                if (projectTaskId == null || projectTaskId.equals(0)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectTaskId",
                            "error.tsform.projectTask.required", getErrorMessageArgs(notNullRowNumber),
                            "Необходимо выбрать проектную задачу в строке " + (notNullRowNumber + 1) + ".");
                    // Неверная проектная задача
                } else if (!isProjectTaskValid(projectId, projectTaskId)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectTaskId",
                            "error.tsform.projectTask.invalid", getErrorMessageArgs(notNullRowNumber),
                            "Неверная проектная задача в строке " + (notNullRowNumber + 1) + ".");
                }
            }
        }
    }

    private void validateDivision(TimeSheetForm tsForm, Errors errors) {
        // Подразделение не выбрано.
        Integer division = tsForm.getDivisionId();
        if (isNotChoosed(division)) {
            errors.rejectValue("divisionId",
                    "error.tsform.division.required",
                    "Подразделение не выбрано.");
            // Неверное подразделение.
        } else if (!isDivisionValid(division)) {
            errors.rejectValue("divisionId",
                    "error.tsform.division.invalid",
                    "Выбрано неверное подразделение.");
        }
    }

    private void validateEmployee(Integer selectedEmployeeId, Errors errors) {
        // Сотрудник не выбран.
        if (isNotChoosed(selectedEmployeeId)) {
            errors.rejectValue("employeeId",
                    "error.tsform.employee.required",
                    "Сотрудник не выбран.");
            // Неверный сотрудник
        } else if (!isEmployeeValid(selectedEmployeeId)) {
            errors.rejectValue("employeeId",
                    "error.tsform.employee.invalid",
                    "Неверные данные сотрудника.");
        }
    }

    private void validateEffort(TimeSheetForm tsForm, Errors errors) {
        // Оценка объёма работ не выбрана
        if (isNotChoosed(tsForm.getEffortInNextDay())) {
            errors.rejectValue("effortInNextDay",
                    "error.tsform.effort.required",
                    "Не поставлена оценка моего объема работ на следующий рабочий день");
            // Неверная оценка
        } else if (!isEffortValid(tsForm.getEffortInNextDay())) {
            errors.rejectValue("effortInNextDay",
                    "error.tsform.effort.invalid",
                    "Неверные данные в поле 'Оценка объема работ на следующий рабочий день'");
        }
    }

    private void validateSelectedDate(TimeSheetForm tsForm, Integer selectedEmployeeId, Errors errors) {
        final String selectedDate = tsForm.getCalDate();
        // Дата не выбрана.
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,
                "calDate",
                "error.tsform.caldate.required",
                "Необходимо выбрать дату.");
        // Эти проверки не проводится, если дата не выбрана.
        if (StringUtils.isNotBlank(selectedDate)) {
            // Выбрана недопустимая дата (если сотрудник выбрал дату из
            // диапазона дат, которые еще не внесены в таблицу calendar.
            if (!isCaldateValid(selectedDate) || !DateTimeUtil.isDateValid(selectedDate)) {
                errors.rejectValue("calDate",
                        "error.tsform.caldate.invalid",
                        "Выбрана недопустимая дата.");
                // Сотрудник уже отправлял отчёт за выбранную дату.
            } else if (!isCaldateUniqueForCurrentEmployee(selectedDate, selectedEmployeeId)) {
                Object[] errorMessageArgs = {DateTimeUtil.formatDateString(selectedDate)};
                errors.rejectValue("calDate",
                        "error.tsform.caldate.notuniq",
                        errorMessageArgs,
                        "Вы уже списывали занятость за " + DateTimeUtil.formatDateString(selectedDate));
            }
        }
    }

    private void validateDuration(TimeSheetForm tsForm, Employee employee, Errors errors, List<TimeSheetTableRowForm> timeSheetTablePart) {
        double totalDuration = 0;  //todo переделать в BigDecimal
        int notNullRowNumber = 0;
        // Проверяем заполненность строк отчета только если они есть :)
        if (timeSheetTablePart != null) {
            for (TimeSheetTableRowForm rowForm : timeSheetTablePart) {
                String durationStr = rowForm.getDuration();
                // Необходимо указать часы
                ValidationUtils.rejectIfEmptyOrWhitespace(errors,
                        "timeSheetTablePart[" + notNullRowNumber + "].duration",
                        "error.tsform.duration.required", getErrorMessageArgs(notNullRowNumber),
                        "Необходимо указать часы в строке " + (notNullRowNumber + 1) + ".");
                // Часы должны быть указаны в правильном формате (1, 1.2, 5.52 и т.п.)
                // and may be 1,2; 2,3
                if (StringUtils.isNotBlank(durationStr)) {
                    /* все числа numeric (4, 2) */
                    Pattern p1 = Pattern.compile("^0*[1-9]?[0-9](?:[.,]\\d{1,2})?$");
                    Matcher m1 = p1.matcher(durationStr);
                    if (!m1.matches()) {
                        errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].duration",
                                "error.tsform.duration.format", getErrorMessageArgs(notNullRowNumber),
                                "Количество часов указано не верно в строке " + (notNullRowNumber + 1)
                                        + ". Примеры правильных значений (5, 3.5, 2.33 и т.п.).");
                    } else {
                        double duration = Double.parseDouble(durationStr.replace(",", "."));
                        // Количество часов должно быть больше нуля
                        if (duration <= 0) {
                            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].duration",
                                    "error.tsform.duration.length", getErrorMessageArgs(notNullRowNumber),
                                    "Количество часов должно быть больше нуля в строке " + (notNullRowNumber + 1) + ".");
                        }
                        // Считаем общее количество часов
                        totalDuration += duration;
                    }
                }

                notNullRowNumber++;

            }
        }

        logger.debug("Total duration is {}", totalDuration);
        // Проверять причины недоработки будем даже если нет записей в отчете
        //if (tsTablePart != null && !tsTablePart.isEmpty()) {
        boolean isHoliday = calendarService.isHoliday(
                DateTimeUtil.stringToDateForDB(tsForm.getCalDate()),
                employee
        );

        boolean isVacation = vacationService.isDayVacationWithoutPlanned(
                employee,
                DateTimeUtil.stringToDateForDB(tsForm.getCalDate())
        );

        boolean isBusinessTrip = businessTripService.isBusinessTripDay(
                employee,
                DateTimeUtil.stringToDateForDB(tsForm.getCalDate())
        );

        // Отчет за выходные без отработанных часов невозможен
        if ((isHoliday || isVacation) && totalDuration == 0 && !isBusinessTrip) {
            errors.rejectValue("overtimeCause", "error.tsform.workonholiday.zeroduration");
        }

        // Сумма часов превышает 24.
        if (totalDuration > 24) {
            errors.rejectValue("totalDuration",
                    "error.tsform.total.duration.max",
                    "Сумма часов не должна превышать 24.");
        }
    }

    private void checkTypeOfCompensation(TimeSheetForm tsForm, Errors errors) {
        final Integer typeOfCompensation = tsForm.getTypeOfCompensation();

        if (isNotChoosed(typeOfCompensation)) {
            errors.rejectValue(
                    "typeOfCompensation",
                    "error.tsform.typeOfCompensation.notChoosed",
                    "Не указан тип компенсации"
            );
        } else {
            if (dictionaryItemService.find(typeOfCompensation).getDictionary().getId() != DictionaryEnum.TYPES_OF_COMPENSATION.getId()) {
                errors.rejectValue(
                        "typeOfCompensation",
                        "error.tsform.typeOfCompensation.wrongvalue",
                        "Указан не правильный тип компенсации"
                );
            }
        }
    }

    /*
    * Возвращает false, если сотрудник уже отправлял отчет за выбранную дату.
    */
    private boolean isCaldateUniqueForCurrentEmployee(String calDate, Integer employeeId) {
        return timeSheetService.findForDateAndEmployee(calDate, employeeId) == null;
    }

    /*
    * Возвращает true, если введённая дата присутствует в таблице calendar и false, если нет.
    */
    private boolean isCaldateValid(String date) {
        return calendarService.find(date) != null;
    }

    private boolean isDivisionValid(Integer division) {
        return divisionService.find(division) != null;
    }

    private boolean isEmployeeValid(Integer employee) {
        return employeeService.find(employee) != null;
    }

    private boolean isEffortValid(Integer effort) {
        return dictionaryItemService.find(effort, DictionaryEnum.EFFORT_IN_NEXTDAY.getId()) != null;
    }

    private boolean isActCatValid(Integer actCat, ProjectRolesEnum emplJob) {
        return actCat == null || //У проектной роли "Руководитель центра" нет доступных категорий активности.
                (emplJob == HEAD && actCat == 0) || dictionaryItemService.find(actCat) != null;
    }

    private boolean isProjectValid(Integer project) {
        return project == null || projectService.findActive(project) != null;
    }

    private boolean isWorkPlaceValid(Integer workplaceId) {
        return workplaceId == null || dictionaryItemService.find(workplaceId, DictionaryEnum.WORKPLACE.getId()) != null;
    }

    private boolean isProjectRoleValid(Integer projectRole) {
        return projectRole == null || projectRoleService.findActive(projectRole) != null;
    }

    private boolean isProjectTaskValid(Integer project, Integer task) {
        return project == null && task == null || projectTaskService.find(project, task) != null;
    }

    /* вспомогательная функция для проверки одной из причин переработки */
    private void checkCause(TimeSheetForm tsForm, String causeTypeStr, TSEnum tsEnum, Errors errors) {
        final Integer overtimeCauseId = tsForm.getOvertimeCause();
        /* установлена ли причина недо/переработки или вых день*/
        if (isNotChoosed(overtimeCauseId)) {
            errors.rejectValue(
                    "overtimeCause",
                    "error.tsform.overtimecause.notchoosed",
                    "Необходимо выбрать причину " + causeTypeStr
            );
        } else {
            final DictionaryItem overtimeCause = dictionaryItemService.find(overtimeCauseId);
            /* проверим попадает ли причина в правильный словарь */
            if (overtimeCause.getDictionary().getId() != tsEnum.getId()) {
                errors.rejectValue(
                        "overtimeCause",
                        "error.tsform.overtimecause.wrongvalue",
                        "Указана неверная причина " + causeTypeStr
                );
            }
            /* проверим коментарии к причине */
            if (overtimeCause.getValue().equals("Другое")) {
                String overtimeCauseComment = tsForm.getOvertimeCauseComment();
                if (StringUtils.isBlank(overtimeCauseComment)
                        || !overtimeCauseComment.matches(inStringMoreThanTwoWordsRegex)) {
                    errors.rejectValue(
                            "overtimeCauseComment",
                            "error.tsform.overtimecause.wrongcommentformat",
                            "Комментарий должен содержать не менее двух слов"
                    );
                }
            }
        }
    }

    /* Проверка на правильность переработки недоработки или работы в выходной */
    private void validateCause(TimeSheetForm tsForm, Employee employee, Errors errors) {
        /* РЦК может всё (трудяга!) */
        if (!employeeService.isEmployeeDivisionLeader(employee.getId())) {
        /* проверим работу в выходные/праздничные дни */
            if (calendarService.isHoliday(DateTimeUtil.stringToDateForDB(tsForm.getCalDate()), employee)
                    || vacationService.isDayVacationWithoutPlanned(employee, DateTimeUtil.stringToDateForDB(tsForm.getCalDate()))) {
                checkCause(tsForm, "работы в праздничный/выходной день", DictionaryEnum.WORK_ON_HOLIDAY_CAUSE, errors);
                checkTypeOfCompensation(tsForm, errors);
            } else {
            /* проверяем есть ли переработка */
                if (overtimeCauseService.isOvertimeDuration(tsForm)) {
                    checkCause(tsForm, "переработки", DictionaryEnum.OVERTIME_CAUSE, errors);
                } else {
                /* проверим есть ли недоработка (для начальника подразделения недоработка не учитывается) */
                    if (overtimeCauseService.isUndertimeDuration(tsForm)) {
                        checkCause(tsForm, "недоработки", DictionaryEnum.UNDERTIME_CAUSE, errors);
                    }
                }
            }
        }
    }
}