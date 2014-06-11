package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.HolidayDAO;
import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.OvertimeCausesEnum;
import com.aplana.timesheet.enums.TypesOfTimeSheetEnum;
import com.aplana.timesheet.enums.UndertimeCausesEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.service.helper.EmployeeHelper;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static argo.jdom.JsonNodeBuilders.*;
import static argo.jdom.JsonNodeFactories.string;
import static com.aplana.timesheet.enums.TypesOfActivityEnum.ILLNESS;
import static com.aplana.timesheet.enums.TypesOfActivityEnum.getById;

@Service
public class TimeSheetService {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetService.class);

    private static final String ROW = "row";
    private static final String PROJECT = "project";
    private static final String ROLE = "role";
    private static final String TASK = "task";
    private static final String WORKPLACE = "workplace";
    private static final String ACT_CAT = "actCat";

    @Autowired
    private TimeSheetDAO timeSheetDAO;

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    AvailableActivityCategoryDAO availableActivityCategoryDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    public ProjectRoleService projectRoleService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    public SecurityService securityService;

    @Autowired
    private EmployeeHelper employeeHelper;

    @Autowired
    private AvailableActivityCategoryService availableActivityCategoryService;

    @Autowired
    private BusinessTripService businessTripService;

    @Autowired
    private HolidayDAO holidayDAO;

    @Transactional
    public TimeSheet storeTimeSheet(TimeSheetForm tsForm, TypesOfTimeSheetEnum type) {
        TimeSheet timeSheet = new TimeSheet();
        logger.debug("Selected employee id = {}", tsForm.getEmployeeId());
        logger.debug("Selected calDate = {}", tsForm.getCalDate());
        timeSheet.setEmployee(employeeService.find(tsForm.getEmployeeId()));
        timeSheet.setCalDate(calendarService.find(tsForm.getCalDate()));
        timeSheet.setPlan(tsForm.getPlan());
        timeSheet.setEffortInNextDay(dictionaryItemService.find(tsForm.getEffortInNextDay()));

        List<TimeSheetTableRowForm> tsTablePart = new ArrayList<TimeSheetTableRowForm>();
        if (tsForm.getTimeSheetTablePart() != null) {
            tsTablePart.addAll(tsForm.getTimeSheetTablePart());
        }
        //очищаем от некорректных строк
        for (TimeSheetTableRowForm form : tsTablePart) {
            if (form.getActivityTypeId() == null) {
                tsTablePart.remove(form);
            }
        }

        Set<TimeSheetDetail> timeSheetDetails = new LinkedHashSet<TimeSheetDetail>();
        if (!tsTablePart.isEmpty()) { // Отчет может быть вообще без строк списания
            for (TimeSheetTableRowForm formRow : tsTablePart) {
                TimeSheetDetail timeSheetDetail = new TimeSheetDetail();
                timeSheetDetail.setTimeSheet(timeSheet);
                timeSheetDetail.setActType(dictionaryItemService.find(formRow.getActivityTypeId()));
                timeSheetDetail.setWorkplace(dictionaryItemService.find(formRow.getWorkplaceId()));
                if (formRow.getActivityCategoryId() != null) {
                    timeSheetDetail.setActCat(dictionaryItemService.find(formRow.getActivityCategoryId()));
                } else {
                    timeSheetDetail.setActCat(null);
                }
                Integer projectId = formRow.getProjectId();
                Double duration = null;
                String durationStr = formRow.getDuration();
                if (projectId != null) {
                    timeSheetDetail.setProject(projectService.find(projectId));
                    timeSheetDetail.setProjectTask(projectTaskService.find(projectId, formRow.getProjectTaskId()));
                }
                // Сохраняем часы только для тех полей, которые не disabled
                if (durationStr != null && !durationStr.isEmpty()) {
                    duration = Double.parseDouble(durationStr.replace(",", "."));
                }
                timeSheetDetail.setDuration(duration);
                timeSheetDetail.setDescription(formRow.getDescription());
                timeSheetDetail.setProblem(formRow.getProblem());
                timeSheetDetail.setProjectRole(projectRoleService.find(formRow.getProjectRoleId()));
                timeSheetDetails.add(timeSheetDetail);
            }
        } else {
            // Если записей в таблице нет то создаем пустую запись с 0 часами
            // Такое возможно если отправляют пустой отчет
            TimeSheetDetail timeSheetDetail = new TimeSheetDetail();
            timeSheetDetail.setDuration((double) 0);
            timeSheetDetail.setTimeSheet(timeSheet);
            String comment;
            if (businessTripService.isBusinessTripDay(timeSheet.getEmployee(), timeSheet.getCalDate().getCalDate())) {
                OvertimeCausesEnum overtimeCause =
                        EnumsUtils.getEnumById(tsForm.getOvertimeCause(), OvertimeCausesEnum.class);
                comment = overtimeCause.getName() + ":" + tsForm.getOvertimeCauseComment();
            } else {
                UndertimeCausesEnum undertimeCause =
                        EnumsUtils.getEnumById(tsForm.getOvertimeCause(), UndertimeCausesEnum.class);
                comment = undertimeCause.getName() + ":" + tsForm.getOvertimeCauseComment();
            }
            timeSheetDetail.setDescription(comment);
            timeSheetDetails.add(timeSheetDetail);
        }
        timeSheet.setTimeSheetDetails(timeSheetDetails);
        //сохраняем тип отчета
        timeSheet.setType(dictionaryItemService.find(type.getId()));

        //пытаемся узнать, может у нас есть уже черновик вне зависисмости от типа отчета
        //на случай если появится еще состояния
        if (TypesOfTimeSheetEnum.DRAFT == type || TypesOfTimeSheetEnum.REPORT == type) {
            Integer id = timeSheetDAO.findIdForDateAndEmployeeByTypes(
                    calendarService.find(tsForm.getCalDate()),
                    tsForm.getEmployeeId(),
                    Arrays.asList(TypesOfTimeSheetEnum.REPORT, TypesOfTimeSheetEnum.DRAFT)
            );

            if (id != null) {
                TimeSheet timeSheet2 = find(id);
                timeSheetDAO.deleteAndFlush(timeSheet2);
                logger.debug("Old TimeSheet object for employee {} ({}) deleted.", tsForm.getEmployeeId(), timeSheet.getCalDate());
                timeSheetDAO.storeTimeSheet(timeSheet);
                logger.debug("TimeSheet object for employee {} ({}) saved.", tsForm.getEmployeeId(), timeSheet.getCalDate());
                return timeSheet;
            }
        }

        timeSheetDAO.storeTimeSheet(timeSheet);
        logger.info("TimeSheet object for employee {} ({}) saved.", tsForm.getEmployeeId(), timeSheet.getCalDate());

        return timeSheet;
    }

    /**
     * Ищет в таблице timesheet запись соответсвующую date для сотрудника с
     * идентификатором employeeId, с определенным типом и возвращает объект типа Timesheet.
     *
     * @param calDate    Дата в виде строки.
     * @param employeeId Идентификатор сотрудника в базе данных.
     * @return объект типа Timesheet, либо null, если объект не найден.
     */
    @Transactional
    public TimeSheet findForDateAndEmployeeByTypes(String calDate, Integer employeeId, List<TypesOfTimeSheetEnum> types) {
        return timeSheetDAO.findForDateAndEmployeeByTypes(
                calendarService.find(calDate),
                employeeId,
                types
        );
    }

    /**
     * Ищет в таблице timesheet запись соответсвующую date для сотрудника с
     * идентификатором employeeId и возвращает объект типа Timesheet.
     *
     * @param calDate    Дата в виде строки.
     * @param employeeId Идентификатор сотрудника в базе данных.
     * @return объект типа Timesheet, либо null, если объект не найден.
     */
    @Transactional(readOnly = true)
    public TimeSheet findForDateAndEmployee(String calDate, Integer employeeId) {
        return timeSheetDAO
                .findForDateAndEmployee(calendarService.find(calDate), employeeId); // Котов. Убрал вызов employeeService.find, здесь достаточно Id, а работать будет на порядок быстрее
    }

    /**
     * Собирает из таблиц timesheet и calendar список дат и работ по сотруднику за месяц.
     *
     * @param employee
     * @param year
     * @param month
     * @return List DayTimeSheet объектов. Первое поле - дата, второе - рабочий/нерабочий день, третье - id работы, если есть.
     */
    @Transactional(readOnly = true)
    public List<DayTimeSheet> findDatesAndReportsForEmployee(Employee employee, Integer year, Integer month) {
        return timeSheetDAO
                .findDatesAndReportsForEmployee(year, month, employee.getRegion().getId(), employee);
    }

    /**
     * Переопределение. (Используется для последующей расскраски календаря)
     * Собирает из таблиц timesheet и calendar список дат и работ по сотруднику за месяц.
     *
     * @param year
     * @param month
     * @param employeeId
     * @return List DayTimeSheet объектов. Первое поле - дата, второе - рабочий/нерабочий день, третье - id работы, если есть.
     */
    @Transactional(readOnly = true)
    public List<DayTimeSheet> findDatesAndReportsForEmployee(Integer year, Integer month, Integer employeeId) {
        Employee emp = employeeService.find(employeeId);

        if (emp == null) {
            emp = new Employee();
            final Region region = new Region();

            region.setId(-1);

            emp.setId(employeeId);
            emp.setRegion(region);
        }

        return timeSheetDAO.findDatesAndReportsForEmployee(year, month, emp.getRegion().getId(), emp);
    }

    public TimeSheet find(Integer id) {
        return timeSheetDAO.find(id);
    }

    @Transactional
    public void delete(TimeSheet timeSheet) {
        timeSheetDAO.delete(timeSheet);
    }

    /**
     * Формирует JSON планов предыдущего дня и на следующего дня
     *
     * @param date
     * @param employeeId
     * @return jsonString
     */
    @Transactional(readOnly = true)
    public String getPlansJson(String date, Integer employeeId) {
        return JsonUtil.format(getPlansJsonBuilder(date, employeeId));
    }

    @Transactional(readOnly = true)
    public JsonObjectNodeBuilder getPlansJsonBuilder(String date, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        final TimeSheet lastTimeSheet = timeSheetDAO.findLastTimeSheetBefore(calendarService.find(date), employeeId);
        final Calendar nextWorkDay = calendarService.getNextWorkDay(
                calendarService.find(date),
                employeeService.find(employeeId).getRegion()
        );
        final TimeSheet nextTimeSheet = timeSheetDAO.findNextTimeSheetAfter(nextWorkDay, employeeId);

        if (lastTimeSheet != null) {
            builder.withField("prev", getPlanBuilder(lastTimeSheet, true));
        }

        if (nextTimeSheet != null &&
                !(ILLNESS == getById(
                        Lists.newArrayList(
                                nextTimeSheet.getTimeSheetDetails()).get(0).getActType().getId()))
                ) { // <APLANATS-458>
            builder.withField("next", getPlanBuilder(nextTimeSheet, false));
        }
        return builder;
    }

    /**
     * Формирует JSON, содержащий дату и план предыдущего рабочего дня, таблицу работ
     * и план для текущего дня, дату и сводку о выполненных работах следующего дня.
     *
     * @param date
     * @param employeeId
     * @return jsonString
     */
    @Transactional(readOnly = true)
    public JsonObjectNodeBuilder getDailyTimesheetJsonBuilder(String date, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        builder.withField("previousDayData", getDailyTimesheetPreviousDayJsonBuilder(date, employeeId));
        builder.withField("currentDayData", getDailyTimesheetCurrentDayJsonBuilder(date, employeeId));
        builder.withField("nextDayData", getDailyTimesheetNextDayJsonBuilder(date, employeeId));

        return builder;
    }

    /**
     * Формирует JSON, содержащий дату и план работ предыдущего рабочего дня.
     *
     * @param date
     * @param employeeId
     * @return jsonString
     */
    public JsonObjectNodeBuilder getDailyTimesheetPreviousDayJsonBuilder(String date, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        final TimeSheet lastTimeSheet = timeSheetDAO.findLastTimeSheetBefore(calendarService.find(date), employeeId);
        if (lastTimeSheet != null) {
            builder.withField("workDate", aStringBuilder(DateTimeUtil.formatDate(lastTimeSheet.getCalDate().getCalDate())));
            builder.withField("plan", aStringBuilder(getPlan(lastTimeSheet)));
        }

        return builder;
    }

    /**
     * Формирует JSON, содержащий факт утверждённого списания, нагрузку, таблицу работ и план для текущего дня.
     *
     * @param date
     * @param employeeId
     * @return jsonString
     */
    public JsonObjectNodeBuilder getDailyTimesheetCurrentDayJsonBuilder(String date, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        final TimeSheet currentTimeSheet = timeSheetDAO.findForDateAndEmployeeByTypes(calendarService.find(date), employeeId, Arrays.asList(TypesOfTimeSheetEnum.REPORT, TypesOfTimeSheetEnum.DRAFT));
        final Calendar nextWorkDateCalendar = calendarService.getNextWorkDay(calendarService.find(date), employeeService.find(employeeId).getRegion());
        builder.withField("nextWorkDate", aStringBuilder(DateTimeUtil.formatDate(nextWorkDateCalendar.getCalDate())));

        if (currentTimeSheet != null) {
            builder.withField("plan", aStringBuilder(getPlan(currentTimeSheet)));
            builder.withField("effort", aStringBuilder(currentTimeSheet.getEffortInNextDay().getId().toString()));
            builder.withField("isFinal", (currentTimeSheet.getType().getId() == TypesOfTimeSheetEnum.REPORT.getId()) ? aTrueBuilder() : aFalseBuilder());

            Set<TimeSheetDetail> timeSheetDetailSet = currentTimeSheet.getTimeSheetDetails();
            builder.withField("rows", aStringBuilder(String.valueOf(timeSheetDetailSet.size())));

            int i = 0;
            JsonArrayNodeBuilder detailsBuilder = anArrayBuilder();
            if (timeSheetDetailSet != null && timeSheetDetailSet.size() != 0) {
                for (TimeSheetDetail timeSheetDetail : timeSheetDetailSet) {
                    detailsBuilder.withElement(
                            anObjectBuilder().
                                    withField("row", JsonUtil.aStringBuilder(i++)).
                                    withField("activity_type_id", aStringBuilder(timeSheetDetail.getActType() != null ? timeSheetDetail.getActType().getId().toString() : "0")).
                                    withField("workplace_id", aStringBuilder(timeSheetDetail.getWorkplace() != null ? timeSheetDetail.getWorkplace().getId().toString() : "0")).
                                    withField("project_id", aStringBuilder(timeSheetDetail.getProject() != null ? timeSheetDetail.getProject().getId().toString() : "0")).
                                    withField("project_role_id", aStringBuilder(timeSheetDetail.getProjectRole() != null ? timeSheetDetail.getProjectRole().getId().toString() : "0")).
                                    withField("activity_category_id", aStringBuilder(timeSheetDetail.getActCat() != null ? timeSheetDetail.getActCat().getId().toString() : "0")).
                                    withField("projectTask_id", aStringBuilder(timeSheetDetail.getProjectTask() != null ? timeSheetDetail.getProjectTask().getId().toString() : "0")).
                                    withField("duration_id", aStringBuilder(timeSheetDetail.getDuration() != null ? timeSheetDetail.getDuration().toString() : "")).
                                    withField("description_id", aStringBuilder(timeSheetDetail.getDescription() != null ? timeSheetDetail.getDescription() : "")).
                                    withField("problem_id", aStringBuilder(timeSheetDetail.getProblem() != null ? timeSheetDetail.getProblem() : ""))
                    );
                }
            }
            builder.withField("data", detailsBuilder);
        } else {
            builder.withField("isFinal", aFalseBuilder());
        }

        return builder;
    }

    /**
     * Формирует JSON, содержащий дату следующего рабочего дня и комментарии об уже списанной в этом дне занятости.
     *
     * @param date
     * @param employeeId
     * @return
     */
    @Transactional(readOnly = true)
    private JsonObjectNodeBuilder getDailyTimesheetNextDayJsonBuilder(String date, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        final Calendar nextWorkDateCalendar = calendarService.getNextWorkDay(calendarService.find(date), employeeService.find(employeeId).getRegion());
        builder.withField("workDate", aStringBuilder(DateTimeUtil.formatDate(nextWorkDateCalendar.getCalDate())));

        final TimeSheet nextTimeSheet = timeSheetDAO.findForDateAndEmployeeByTypes(nextWorkDateCalendar, employeeId, Arrays.asList(TypesOfTimeSheetEnum.REPORT));

        if (nextTimeSheet != null) {
            Set<TimeSheetDetail> nextDayDetails = nextTimeSheet.getTimeSheetDetails();

            if (nextDayDetails != null && nextDayDetails.size() > 0) {
                builder.withField("workSummary", aStringBuilder(getStringTimeSheetDetails(nextTimeSheet)));
            }
        }

        return builder;
    }

    private JsonObjectNodeBuilder getPlanBuilder(TimeSheet timeSheet, Boolean nextOrPrev) {
        return anObjectBuilder().
                withField("dateStr", aStringBuilder(DateTimeUtil.formatDate(timeSheet.getCalDate().getCalDate()))).
                withField("plan", aStringBuilder(nextOrPrev ? getPlan(timeSheet) : getStringTimeSheetDetails(timeSheet)));
    }

    private String getPlan(TimeSheet timeSheet) {
        String lastPlan = timeSheet.getPlan();
        if (lastPlan != null) {
            lastPlan = lastPlan.replace("\r\n", "\n");
        } else {
            lastPlan = StringUtils.EMPTY;
        }
        return lastPlan;
    }

    /**
     * Формирует строку на подобие поля "Что было сделано" из отчета
     *
     * @param timeSheet
     * @return String
     */
    public String getStringTimeSheetDetails(TimeSheet timeSheet) {
        Set<TimeSheetDetail> timeSheetDetails = timeSheet.getTimeSheetDetails();
        StringBuilder sb;
        StringBuilder rezult = new StringBuilder();
        int i = 1;
        for (TimeSheetDetail detail : timeSheetDetails) {
            sb = new StringBuilder();
            sb.append(i).append(". ");
            sb.append(detail.getActType().getValue()).append(" - ");
            if (detail.getProject() != null)
                sb.append(detail.getProject().getName()).append(" : ");
            sb.append(detail.getDescription());
            rezult.append(sb.toString()).append("\n");
            i++;
        }
        return rezult.toString();
    }

    @Transactional(readOnly = true)
    public Date getLastWorkdayWithoutTimesheet(Integer employeeId) {
        Employee employee = employeeDAO.find(employeeId);
        Calendar calendar = timeSheetDAO.getDateNextAfterLastDayWithTS(employee);
        Date result = new Date();
        if (calendar == null) {
            return employee.getStartDate();
        } else {
            result.setTime(calendar.getCalDate().getTime());
            return result;
        }
    }

    /**
     * Возвращает Map по id сотрудников c указанием дат последних отправленных отчетов
     *
     * @param division Если null, то поиск осуществляется без учета подразделения,
     *                 иначе с учётом подразделения
     * @return Map по id сотрудников c указанием дат последних отправленных отчетов.
     */
    @Transactional(readOnly = true)
    public Map<Integer, Date> getLastWorkdayWithoutTimesheetMap(Division division) {
        return timeSheetDAO.getDateNextAfterLastDayWithTSMap(division);
    }

    public List<TimeSheet> getTimeSheetsForEmployee(Employee employee, Integer year, Integer month) {
        return timeSheetDAO.getTimeSheetsForEmployee(employee, year, month);
    }

    @Transactional(readOnly = true)
    public String getListOfActDescription() {
        List<AvailableActivityCategory> availableActivityCategories = availableActivityCategoryDAO.getAllAvailableActivityCategories();
        final JsonArrayNodeBuilder result = anArrayBuilder();
        for (AvailableActivityCategory activityCategory : availableActivityCategories) {
            result.withElement(
                    anObjectBuilder().
                            withField("actCat", JsonUtil.aNumberBuilder(activityCategory.getActCat().getId())).
                            withField("actType", JsonUtil.aNumberBuilder(activityCategory.getActType().getId())).
                            withField("projectRole", JsonUtil.aNumberBuilder(activityCategory.getProjectRole().getId())).
                            withField("description",
                                    activityCategory.getDescription() != null ?
                                            aStringBuilder(activityCategory.getDescription()) :
                                            string(StringUtils.EMPTY)
                            )
            );
        }
        result.withElement(
                anObjectBuilder().
                        withField("actCat", JsonUtil.aNumberBuilder(0)).
                        withField("actType", JsonUtil.aNumberBuilder(0)).
                        withField("projectRole", JsonUtil.aNumberBuilder(0)).
                        withField("description", string(StringUtils.EMPTY))
        );

        return JsonUtil.format(result);
    }

    public String getSelectedCalDateJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        String date = "";
        sb.append("'");
        if (DateTimeUtil.isDateValid(tsForm.getCalDate())) {
            date = DateTimeUtil.formatDateString(tsForm.getCalDate());
            sb.append(date);
        }
        sb.append("'");
        logger.debug("SelectedCalDateJson = {}", date);
        return sb.toString();
    }

    public String getSelectedActCategoriesJson(TimeSheetForm tsForm) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();

        if (tablePart != null) {
            for (int i = 0; i < tablePart.size(); i++) {
                builder.withElement(
                        anObjectBuilder().
                                withField(ROW, JsonUtil.aStringBuilder(i)).
                                withField(ACT_CAT, JsonUtil.aStringBuilder(tablePart.get(i).getActivityCategoryId()))
                );
            }
        } else {
            builder.withElement(
                    anObjectBuilder().
                            withField(ROW, JsonUtil.aStringBuilder(0)).
                            withField(ACT_CAT, aStringBuilder(StringUtils.EMPTY))
            );
        }

        return JsonUtil.format(builder);
    }

    public String getSelectedWorkplaceJson(TimeSheetForm tsForm) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            for (int i = 0; i < tablePart.size(); i++) {
                builder.withElement(
                        anObjectBuilder().
                                withField(ROW, JsonUtil.aStringBuilder(i)).
                                withField(WORKPLACE, JsonUtil.aStringBuilder(tablePart.get(i).getWorkplaceId()))
                );
            }
        } else {
            builder.withElement(
                    anObjectBuilder().
                            withField(ROW, JsonUtil.aStringBuilder(0)).
                            withField(WORKPLACE, aStringBuilder(StringUtils.EMPTY))
            );
        }

        return JsonUtil.format(builder);
    }

    public String getSelectedProjectTasksJson(TimeSheetForm tsForm) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();

        if (tablePart != null) {
            for (int i = 0; i < tablePart.size(); i++) {
                if (tablePart.get(i).getProjectTaskId() != null) {
                    builder.withElement(
                            anObjectBuilder().
                                    withField(ROW, JsonUtil.aStringBuilder(i)).
                                    withField(TASK, aStringBuilder(tablePart.get(i).getProjectTaskId().toString()))
                    );
                }
            }
        } else {
            builder.withElement(
                    anObjectBuilder().
                            withField(ROW, JsonUtil.aStringBuilder(0)).
                            withField(TASK, aStringBuilder(StringUtils.EMPTY))
            );
        }

        return JsonUtil.format(builder);
    }

    public String getSelectedProjectRolesJson(TimeSheetForm tsForm) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();

        if (tablePart != null) {
            for (int i = 0; i < tablePart.size(); i++) {
                if (tablePart.get(i).getProjectTaskId() != null) {
                    builder.withElement(
                            anObjectBuilder().
                                    withField(ROW, JsonUtil.aStringBuilder(i)).
                                    withField(ROLE, JsonUtil.aStringBuilder(tablePart.get(i).getProjectRoleId()))
                    );
                }
            }
        } else {
            builder.withElement(
                    anObjectBuilder().
                            withField(ROW, JsonUtil.aStringBuilder(0)).
                            withField(ROLE, aStringBuilder(StringUtils.EMPTY))
            );
        }

        return JsonUtil.format(builder);
    }

    public String getSelectedProjectsJson(TimeSheetForm tsForm) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();

        if (tablePart != null) {
            for (int i = 0; i < tablePart.size(); i++) {
                builder.withElement(
                        anObjectBuilder().
                                withField(ROW, JsonUtil.aStringBuilder(i)).
                                withField(PROJECT, JsonUtil.aStringBuilder(tablePart.get(i).getProjectId()))
                );
            }
        } else {
            builder.withElement(
                    anObjectBuilder().
                            withField(ROW, JsonUtil.aStringBuilder(0)).
                            withField(PROJECT, aStringBuilder(StringUtils.EMPTY))
            );
        }

        return JsonUtil.format(builder);
    }

    public List<DictionaryItem> getEffortList() {
        return dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.EFFORT_IN_NEXTDAY.getId());
    }

    /**
     * Возвращает Map со значениями для заполнения списков сотрудников,
     * проектов, пресейлов, проектных задач, типов и категорий активности на
     * форме приложения.
     *
     * @return
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getListsToMAV(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();

        List<DictionaryItem> typesOfActivity = dictionaryItemService.getTypesOfActivity();
        result.put("actTypeList", typesOfActivity);

        String typesOfActivityJson = dictionaryItemService.getDictionaryItemsInJson(typesOfActivity);
        result.put("actTypeJson", typesOfActivityJson);

        result.put("workplaceList", dictionaryItemService.getWorkplaces());

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

        String employeeListJson = employeeHelper.getEmployeeListWithLastWorkdayJson(divisions, employeeService.isShowAll(request), true);
        result.put("employeeListJson", employeeListJson);

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

        result.put("listOfActDescriptionJson", getListOfActDescription());
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

    public String getOverdueTimesheetEmployeesNames(Division division) {

        // Логика выбора дат перенесена из ReportCheckService,
        // по принципу "работает - не ломай".
        Date currentDay = new Date();

        Date startDate = DateTimeUtil.previousMonthFirstDayDate(currentDay);
        Date currentMonthLastDay = DateTimeUtil.currentMonthLastDayDate(currentDay);
        Date previousMonthLastDay = DateTimeUtil.previousMonthLastDayDate(currentDay);
        Date lastSunday = DateTimeUtil.lastSundayDate(currentDay);

        Date endDate = lastSunday;

        if (currentMonthLastDay.after(lastSunday))
            endDate = currentDay;
        if (previousMonthLastDay.after(lastSunday))
            endDate = previousMonthLastDay;
        if (endDate.equals(currentDay))
            endDate = DateUtils.addDays(endDate, -1);

        Integer threshold = propertyProvider.getReportsOverdueThreshold();
        List<String> names = timeSheetDAO.getOverdueTimesheetEmployeeNames(division, startDate, endDate, threshold);

        return Joiner.on(", ").join(names);
    }

    @Transactional
    public void setDraftTypeToTimeSheet(Integer id) {
        TimeSheet timeSheet = find(id);
        DictionaryItem draftItem = dictionaryItemService.find(TypesOfTimeSheetEnum.DRAFT.getId());
        timeSheet.setType(draftItem);
        timeSheetDAO.storeTimeSheet(timeSheet);
    }
}