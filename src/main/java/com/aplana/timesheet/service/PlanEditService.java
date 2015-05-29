package com.aplana.timesheet.service;

import argo.jdom.*;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.*;
import com.aplana.timesheet.form.PlanEditForm;
import com.aplana.timesheet.form.validator.PlanEditFormValidator;
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.Calendar;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static argo.jdom.JsonNodeFactories.*;
import static argo.jdom.JsonNodeFactories.array;
import static com.aplana.timesheet.controller.PlanEditController.*;
import static com.aplana.timesheet.util.JsonUtil.aNumberBuilder;

import static com.aplana.timesheet.service.ProjectService.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class PlanEditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanEditService.class);

    private static final String COOKIE_DIVISION_ID = "cookie_division_id";

    private static final String COOKIE_REGIONS = "cookie_regions";
    private static final String COOKIE_PROJECT_ROLES = "cookie_project_roles";

    private static final String COOKIE_SHOW_PLANS = "cookie_show_plans";
    private static final String COOKIE_SHOW_FACTS = "cookie_show_facts";
    private static final String COOKIE_SHOW_PROJECTS = "cookie_show_projects";
    private static final String COOKIE_SHOW_PRESALES = "cookie_show_presales";
    private static final String COOKIE_SHOW_SUMMARY_PROJECTS_PRESALES = "cookie_show_summary_projects_presales";
    private static final String COOKIE_SHOW_SUMMARY_FUNDING = "cookie_show_summary_funding";
    private static final String COOKIE_MONTH = "cookie_month";
    private static final String COOKIE_MANAGER = "cookie_manager";
    private static final String SEPARATOR = "~";

    public static final String _PLAN = "_plan";
    public static final String _FACT = "_fact";

    public static final String SUMMARY = "summary";
    public static final String CENTER_PROJECTS = "center_projects";
    public static final String CENTER_PRESALES = "center_presales";
    public static final String OTHER_PROJECT = "other_project";
    public static final String OTHER_PRESALE = "other_presale";
    public static final String OTHER_PROJECTS_AND_PRESALES = "other_projects_and_presales";
    public static final String OTHER_INVEST_PROJECT = "other_invest_project";
    public static final String OTHER_COMERCIAL_PROJECT = "other_comercial_project";
    public static final String NON_PROJECT = "non_project";
    public static final String ILLNESS = "illness";
    public static final String VACATION = "vacation";
    public static final String EMPLOYEE = "employee";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String EMPLOYEE_DIVISION = "employee_division";
    public static final String PERCENT_OF_CHARGE = "percent_of_charge";

    public static final String SUMMARY_PROJECTS = "summary_projects";
    public static final String SUMMARY_PRESALES = "summary_presales";
    public static final String SUMMARY_INVESTMENT = "summary_investment";
    public static final String SUMMARY_COMMERCIAL = "summary_commercial";
    public static final String SUMMARY_PROJECTS_FACT = SUMMARY_PROJECTS + _FACT;
    public static final String SUMMARY_PRESALES_FACT = SUMMARY_PRESALES + _FACT;
    public static final String SUMMARY_INVESTMENT_FACT = SUMMARY_INVESTMENT + _FACT;
    public static final String SUMMARY_COMMERCIAL_FACT = SUMMARY_COMMERCIAL + _FACT;

    public static final String PROJECTS_PLANS = "projects_plans";

    public static final String SUMMARY_FACT = SUMMARY + _FACT;
    public static final String CENTER_PROJECTS_FACT = CENTER_PROJECTS + _FACT;
    public static final String CENTER_PRESALES_FACT = CENTER_PRESALES + _FACT;

    public static final String OTHER_PROJECT_PLAN = OTHER_PROJECT + _PLAN;
    public static final String OTHER_PRESALE_PLAN = OTHER_PRESALE + _PLAN;
    public static final String OTHER_INVEST_PROJECT_PLAN = OTHER_INVEST_PROJECT + _PLAN;
    public static final String OTHER_COMERCIAL_PROJECT_PLAN = OTHER_COMERCIAL_PROJECT + _PLAN;

    public static final String OTHER_PROJECTS_AND_PRESALES_PLAN = OTHER_PROJECTS_AND_PRESALES + _PLAN;
    public static final String OTHER_PROJECTS_AND_PRESALES_FACT = OTHER_PROJECTS_AND_PRESALES + _FACT;
    public static final String NON_PROJECT_PLAN = NON_PROJECT + _PLAN;
    public static final String NON_PROJECT_FACT = NON_PROJECT + _FACT;
    public static final String ILLNESS_PLAN = ILLNESS + _PLAN;
    public static final String ILLNESS_FACT = ILLNESS + _FACT;
    public static final String VACATION_PLAN = VACATION + _PLAN;
    public static final String VACATION_FACT = VACATION + _FACT;
    public static final String PERCENT_OF_CHARGE_FACT = PERCENT_OF_CHARGE + _FACT;
    public static final String MONTH_PLAN = "month_plan";

    public static final String JSON_DATA_ITEMS = "items";

    public  static final int    COOKIE_MAX_AGE = 999999999;

    public static final JsonStringNode PROJECTS_PLANS_FIELD = string(PROJECTS_PLANS);
    public static final JsonStringNode NON_PROJECT_PLAN_FIELD = string(NON_PROJECT_PLAN);
    public static final JsonStringNode ILLNESS_PLAN_FIELD = string(ILLNESS_PLAN);
    public static final JsonStringNode VACATION_PLAN_FIELD = string(VACATION_PLAN);
    public static final JsonStringNode OTHER_PROJECTS_AND_PRESALES_PLAN_FIELD =
            string(OTHER_PROJECTS_AND_PRESALES_PLAN);

    public static final Map<JsonStringNode, TSEnum> PLAN_TYPE_MAP = new HashMap<JsonStringNode, TSEnum>();

    static {
        PLAN_TYPE_MAP.put(NON_PROJECT_PLAN_FIELD, EmployeePlanType.NON_PROJECT);
        PLAN_TYPE_MAP.put(ILLNESS_PLAN_FIELD, EmployeePlanType.ILLNESS);
        PLAN_TYPE_MAP.put(VACATION_PLAN_FIELD, EmployeePlanType.VACATION);
        PLAN_TYPE_MAP.put(OTHER_PROJECTS_AND_PRESALES_PLAN_FIELD, EmployeePlanType.WORK_FOR_OTHER_DIVISIONS);
    }

    @Autowired
    private PlanEditFormValidator planEditFormValidator;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private VacationService vacationService;

    @Autowired
    private IllnessService illnessService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    private EmployeePlanService employeePlanService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePlans(JsonRootNode rootNode, Integer year, Integer month) {
        final Calendar calendar = DateTimeUtil.getCalendar(year, month);

        if (DateUtils.truncatedCompareTo(new Date(), calendar.getTime(), Calendar.MONTH) > 0) {
            throw new IllegalArgumentException("Редактирование планов за предыдущий месяц запрещено");
        }

        final List<EmployeePlan> employeePlans = new ArrayList<EmployeePlan>();
        final List<EmployeeProjectPlan> employeeProjectPlans = new ArrayList<EmployeeProjectPlan>();

        processJsonDataItems(employeePlans, employeeProjectPlans, rootNode, year, month);

        employeePlanService.mergeProjectPlans(employeePlans);
        employeeProjectPlanService.mergeEmployeeProjectPlans(employeeProjectPlans);
    }

    private void processJsonDataItems(
            List<EmployeePlan> employeePlans, List<EmployeeProjectPlan> employeeProjectPlans,
            JsonRootNode rootNode, Integer year, Integer month
    ) {
        Integer employeeId;

        for (JsonNode jsonNode : rootNode.getArrayNode(JSON_DATA_ITEMS)) {
            employeeId = JsonUtil.getDecNumberValue(jsonNode, EMPLOYEE_ID);

            final Employee employee = employeeService.find(employeeId);

            addEmployeePlans(employeePlans, year, month, employee, jsonNode);

            addEmployeeProjectPlans(employeeProjectPlans, year, month, employee, jsonNode);
        }
    }

    private void addEmployeeProjectPlans(List<EmployeeProjectPlan> employeeProjectPlans, Integer year, Integer month, Employee employee, JsonNode jsonNode) {
        if (jsonNode.getFields().containsKey(PROJECTS_PLANS_FIELD)) {
            for (JsonNode node : jsonNode.getArrayNode(PROJECTS_PLANS)) {
                employeeProjectPlans.add(createEmployeeProjectPlanIfNeed(year, month, employee, node));
            }
        }
    }

    private void addEmployeePlans(List<EmployeePlan> employeePlans, Integer year, Integer month, Employee employee, JsonNode jsonNode) {
        final Map<JsonStringNode, JsonNode> fields = jsonNode.getFields();

        JsonStringNode field;

        for (Map.Entry<JsonStringNode, TSEnum> entry : PLAN_TYPE_MAP.entrySet()) {
            field = entry.getKey();

            final EmployeePlan employeePlan = createEmployeePlanIfNeed(
                    year, month, employee,
                    dictionaryItemService.find(entry.getValue().getId())
            );

            if (fields.containsKey(field)) {
                employeePlan.setValue(JsonUtil.getFloatNumberValue(jsonNode, field.getText()));

                employeePlans.add(employeePlan);
            }
        }
    }

    private EmployeePlan createEmployeePlanIfNeed(Integer year, Integer month, Employee employee, DictionaryItem dictionaryItem) {
        EmployeePlan employeePlan = employeePlanService.tryFind(employee, year, month, dictionaryItem);

        if (employeePlan == null) {
            employeePlan = new EmployeePlan();

            employeePlan.setType(dictionaryItem);
            employeePlan.setEmployee(employee);
            employeePlan.setYear(year);
            employeePlan.setMonth(month);
        }

        return employeePlan;
    }

    private EmployeeProjectPlan createEmployeeProjectPlanIfNeed(Integer year,
                                                                Integer month,
                                                                Employee employee,
                                                                JsonNode node
    ) {
        final Project project = projectService.find(JsonUtil.getDecNumberValue(node, PROJECT_ID));

        EmployeeProjectPlan employeeProjectPlan = employeeProjectPlanService.tryFind(employee, year, month, project);

        if (employeeProjectPlan == null) {
            employeeProjectPlan = new EmployeeProjectPlan();

            employeeProjectPlan.setEmployee(employee);
            employeeProjectPlan.setProject(project);
            employeeProjectPlan.setYear(year);
            employeeProjectPlan.setMonth(month);
        }

        employeeProjectPlan.setValue(JsonUtil.getFloatNumberValue(node, _PLAN));

        return employeeProjectPlan;
    }

    private static double nilIfNull(Double value) {
        return (value == null) ? 0 : value;
    }

    private static boolean isPresale(Project project) {
        return (EnumsUtils.getEnumById(project.getState().getId(), TypesOfActivityEnum.class) == TypesOfActivityEnum.PRESALE);
    }

    private static boolean isProject(Project project) {
        return (EnumsUtils.getEnumById(project.getState().getId(), TypesOfActivityEnum.class) == TypesOfActivityEnum.PROJECT);
    }

    private static boolean isCommercialProject(Project project) {
        return (EnumsUtils.getEnumById(project.getFundingType().getId(), ProjectFundingTypeEnum.class) == ProjectFundingTypeEnum.COMMERCIAL_PROJECT);
    }

    private static <T> T defaultValue(T value, T defaultValue) {
        return (value == null) ? defaultValue : value;
    }

    private static Integer tryParseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Integer> tryParseIntegerListFromString(String string) {
        try {
            final List<Integer> list = new ArrayList<Integer>();

            for (String s : StringUtils.split(string, SEPARATOR)) {
                list.add(Integer.valueOf(s));
            }

            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean tryParseBoolean(String value) {
        try {
            return Boolean.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static void addCookie(HttpServletResponse response, String name, Object value) {
        final String valueStr = String.valueOf(value);

        final Cookie cookieToDelete = new Cookie(name, valueStr);
        final Cookie cookie = new Cookie(name, valueStr);

        cookieToDelete.setMaxAge(0);
        cookie.setMaxAge(COOKIE_MAX_AGE);

        response.addCookie(cookieToDelete);
        response.addCookie(cookie);
    }

    private static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookieToDelete = new Cookie(name, "");
        cookieToDelete.setMaxAge(0);

        response.addCookie(cookieToDelete);
    }

    public static Object getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     *
     * @param form
     * @return true, if form was init not from cookies
     */
    public void initForm(PlanEditForm form, HttpServletRequest request) {
        initDefaultForm(form);

        final Cookie[] cookies = request.getCookies();

        String name, value;

        for (Cookie cookie : cookies) {
            name = cookie.getName();
            value = cookie.getValue();
            if (COOKIE_DIVISION_ID.equals(name)) {
                form.setDivisionId(defaultValue(tryParseInt(value), form.getDivisionId()));
            } else if (COOKIE_REGIONS.equals(name)) {
                form.setRegions(defaultValue(tryParseIntegerListFromString(value), form.getRegions()));
            } else if (COOKIE_PROJECT_ROLES.equals(name)) {
                form.setProjectRoles(defaultValue(tryParseIntegerListFromString(value), form.getProjectRoles()));
            } else if (COOKIE_SHOW_PLANS.equals(name)) {
                form.setShowPlans(defaultValue(tryParseBoolean(value), form.getShowPlans()));
            } else if (COOKIE_SHOW_FACTS.equals(name)) {
                form.setShowFacts(defaultValue(tryParseBoolean(value), form.getShowFacts()));
            } else if (COOKIE_SHOW_PROJECTS.equals(name)) {
                form.setShowProjects(defaultValue(tryParseBoolean(value), form.getShowProjects()));
            } else if (COOKIE_SHOW_PRESALES.equals(name)) {
                form.setShowPresales(defaultValue(tryParseBoolean(value), form.getShowPresales()));
            } else if (COOKIE_MONTH.equals(name)) {
                form.setMonth(defaultValue(tryParseInt(value), form.getMonth()));
            } else if (COOKIE_MANAGER.equals(name)) {
                form.setManager(defaultValue(tryParseInt(value), form.getManager()));
            } else if (COOKIE_SHOW_SUMMARY_PROJECTS_PRESALES.equals(name)) {
                form.setShowSumProjectsPresales(defaultValue(tryParseBoolean(value), form.getShowSumProjectsPresales()));
            } else if (COOKIE_SHOW_SUMMARY_FUNDING.equals(name)) {
                form.setShowSumFundingType(defaultValue(tryParseBoolean(value), form.getShowSumFundingType()));
            }
        }
    }

    private void initDefaultForm(PlanEditForm form) {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);

        form.setDivisionId(securityService.getSecurityPrincipal().getEmployee().getDivision().getId());
        form.setYear(year);
        form.setMonth(calendar.get(Calendar.MONTH) + 1);
        form.setRegions(Arrays.asList(PlanEditForm.ALL_VALUE));
        form.setProjectRoles(Arrays.asList(PlanEditForm.ALL_VALUE));
        form.setShowPlans(Boolean.TRUE);
        form.setShowFacts(Boolean.TRUE);
        form.setShowProjects(Boolean.TRUE);
        form.setShowPresales(Boolean.TRUE);
        form.setShowSumProjectsPresales(Boolean.TRUE);
        form.setShowSumFundingType(Boolean.FALSE);
    }

    private List<Region> getRegionList() {
        return regionService.getRegions();
    }

    private List<Employee> getManagerList() {
        return employeeService.getManagerListForAllEmployee();
    }

    private String getManagerListJson() {
        return employeeService.getManagerListJson();
    }

    private List<ProjectRole> getProjectRoleList() {
        return projectRoleService.getProjectRoles();
    }

    private List<Division> getDivisionList() {
        return divisionService.getDivisions();
    }

    private List<com.aplana.timesheet.dao.entity.Calendar> getYearList() {
        return DateTimeUtil.getYearsList(calendarService);
    }

    private String getMonthMapAsJson(List<com.aplana.timesheet.dao.entity.Calendar> yearList) {
        return DateTimeUtil.getMonthListJson(yearList, calendarService);
    }

    public void saveCookie(PlanEditForm form, HttpServletResponse response) {
        addCookie(response, COOKIE_DIVISION_ID, form.getDivisionId());
        addCookie(response, COOKIE_SHOW_PLANS, form.getShowPlans());
        addCookie(response, COOKIE_SHOW_FACTS, form.getShowFacts());
        addCookie(response, COOKIE_SHOW_PROJECTS, form.getShowProjects());
        addCookie(response, COOKIE_SHOW_PRESALES, form.getShowPresales());
        addCookie(response, COOKIE_REGIONS, StringUtils.join(form.getRegions(), SEPARATOR));
        addCookie(response, COOKIE_PROJECT_ROLES, StringUtils.join(form.getProjectRoles(), SEPARATOR));
        addCookie(response, COOKIE_MONTH, form.getMonth());
        addCookie(response, COOKIE_MANAGER, form.getManager());
        addCookie(response, COOKIE_SHOW_SUMMARY_PROJECTS_PRESALES, form.getShowSumProjectsPresales());
        addCookie(response, COOKIE_SHOW_SUMMARY_FUNDING, form.getShowSumFundingType());
    }

    public void  deleteCookie(HttpServletResponse response){
        deleteCookie(response, COOKIE_SELECTION_ROW);
        deleteCookie(response, COOKIE_SCROLL_X);
        deleteCookie(response, COOKIE_SCROLL_Y);
    }

    public ModelAndView createModelAndView(PlanEditForm form, BindingResult bindingResult) {
        final ModelAndView modelAndView = new ModelAndView("planEdit");

        modelAndView.addObject("regionList", getRegionList());
        modelAndView.addObject("managerList", getManagerList());
        modelAndView.addObject("managerMapJson", getManagerListJson());
        modelAndView.addObject("projectRoleList", getProjectRoleList());
        modelAndView.addObject("divisionList", getDivisionList());

        final List<com.aplana.timesheet.dao.entity.Calendar> yearList = getYearList();

        modelAndView.addObject("yearList", yearList);
        modelAndView.addObject("monthMapJson", getMonthMapAsJson(yearList));

        planEditFormValidator.validate(form, bindingResult);

        Boolean editable = Boolean.FALSE;

        if (!bindingResult.hasErrors() && (form.getShowPlans() || form.getShowFacts())) {
            fillTableData(modelAndView, form);
            modelAndView.addObject("monthList", calendarService.getMonthList(form.getYear()));
            editable = isEditable(form);
        }

        modelAndView.addObject("editable", editable);

        return modelAndView;
    }

    private Boolean isEditable(PlanEditForm form) {
        final Calendar calendar = DateTimeUtil.getCalendar(form.getYear(), form.getMonth());

        return (DateUtils.truncatedCompareTo(new Date(), calendar.getTime(), Calendar.MONTH) <= 0);
    }

    private void fillTableData(ModelAndView modelAndView, PlanEditForm form) {
        final Date date = DateTimeUtil.createDate(form.getYear(), form.getMonth());
        List<Project> projectList = getProjects(form, date);

        modelAndView.addObject("projectListJson", projectService.getProjectListAsJson(projectList,
                new String[]{PROJECT_ID, PROJECT_NAME, PROJECT_DIVISION, PROJECT_TYPE, PROJECT_FUNDING_TYPE }));
        modelAndView.addObject("jsonDataToShow", getDataAsJson(form, date, projectList));
    }

    public List<Project> getProjects(PlanEditForm form, Date date) {
        final List<Integer> projectStates = getProjectStates(form);

        return projectStates.isEmpty()
                ? new ArrayList<Project>()
                : projectService.getProjectsByStatesForDateAndDivisionId(
                projectStates,
                date,
                form.getDivisionId()
        );
    }

    private List<Integer> getProjectStates(PlanEditForm form) {
        final List<Integer> states = new ArrayList<Integer>();

        if (form.getShowProjects()) {
            states.add(TypesOfActivityEnum.PROJECT.getId());
        }

        if (form.getShowPresales()) {
            states.add(TypesOfActivityEnum.PRESALE.getId());
        }

        return states;
    }

    public String getDataAsJson(PlanEditForm form, Date date, List<Project> projectList) {
        final Integer managerId = form.getManager();
        LOGGER.debug("manager = {}",managerId);
        final List<Employee> employees = employeeService.getDivisionEmployeesByManager(
                form.getDivisionId(),
                date,
                getRegionIds(form),
                getProjectRoleIds(form),
                managerId
        );
        if (managerId != null && managerId >= 1) {
            Employee manager = employeeService.find(managerId);
            if (!employees.contains(manager) && manager != null) {
                employees.add(manager);
                Collections.sort(employees);
            }
        }
        final ArrayList<JsonNode> nodes = new ArrayList<JsonNode>();

        final Integer year = form.getYear();
        final Integer month = form.getMonth();
        final boolean showPlans = form.getShowPlans();
        final boolean showFacts = form.getShowFacts();

        JsonObjectNodeBuilder builder;
        int workDaysCount;

        for (Employee employee : employees) {
            builder = anObjectBuilder().
                    withField(EMPLOYEE_ID, aNumberBuilder(employee.getId())).
                    withField(EMPLOYEE, aStringBuilder(employee.getName())).
                    withField(EMPLOYEE_DIVISION, aNumberBuilder(employee.getDivision().getId()));

            workDaysCount = calendarService.getEmployeeRegionWorkDaysCount(employee, year, month);

            final double summaryPlan = TimeSheetConstants.WORK_DAY_DURATION * workDaysCount * employee.getJobRate();

            if (showPlans) {
                appendToBuilder(builder, getPlans(employee, year, month, summaryPlan, form, projectList));
            }

            if (showFacts) {
                appendToBuilder(builder, getFacts(employee, year, month, summaryPlan, form));
            }

            nodes.add(builder.build());
        }

        return JsonUtil.format(array(nodes));
    }

    private void appendToBuilder(JsonObjectNodeBuilder builder, Map<String, JsonNodeBuilder> map) {
        for (Map.Entry<String, JsonNodeBuilder> entry : map.entrySet()) {
            builder.withField(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, JsonNodeBuilder> getPlans(Employee employee, Integer year, Integer month, Double summaryPlan, PlanEditForm form, List<Project> projectListToShow) {
        final Map<String, JsonNodeBuilder> map = Maps.newHashMap();

        Double otherProjectsPlan = 0.0;
        Double otherPresalePlan  = 0.0;
        Double otherInvestProjectPlan = 0.0;
        Double otherComercialProjectPlan = 0.0;

        Double vacationPlan = vacationService.getVacationsWorkdaysCount(employee, year, month) * TimeSheetConstants.WORK_DAY_DURATION;
        Double illnessPlan  = illnessService.getIllnessWorkdaysCount(employee, year, month) * TimeSheetConstants.WORK_DAY_DURATION;


        Double vacationPlanPercent = getPercent(vacationPlan, summaryPlan);
        Double illnessPlanPercent  = getPercent(illnessPlan, summaryPlan);

        for (EmployeeProjectPlan employeeProjectPlan : employeeProjectPlanService.find(employee, year, month)) {
            final Project project = employeeProjectPlan.getProject();
            double duration = nilIfNull(employeeProjectPlan.getValue());
            duration = getPercent(duration, summaryPlan); // так как отображается в процентах, то переводим
            if (project != null) {
                if ( !project.getDivisions().contains(employee.getDivision())) {
                    if (isPresale(project)){
                        otherPresalePlan  += duration;
                    }else{
                        otherProjectsPlan += duration;
                    }
                }

                Integer projectDivisionId = project.getDivision() != null ? project.getDivision().getId() : null;
                Integer employeeDivisionId = employee.getDivision() != null ? employee.getDivision().getId() : null;
                boolean isEqual = (projectDivisionId !=null && employeeDivisionId !=null && projectDivisionId.equals(employeeDivisionId));

                if ( ! projectListToShow.contains(project)){
                    if (isCommercialProject(project) || !isEqual || !isPresale(project)){
                        otherComercialProjectPlan += duration;
                    }
                    else {
                        otherInvestProjectPlan += duration;
                    }
                }
                appendNumberField(map, String.format("%d" + _PLAN, project.getId()), duration);
            }
        }

        for (EmployeePlan employeePlan : employeePlanService.find(employee, year, month)) {
            Double duration = nilIfNull(employeePlan.getValue());
            duration = getPercent(duration, summaryPlan);
            appendNumberField(map, getFieldNameForEmployeePlan(employeePlan), duration);
            if (employeePlan.getType().getId().equals(EmployeePlanType.WORK_FOR_OTHER_DIVISIONS.getId())) {
                otherInvestProjectPlan += duration;
            }
        }

        appendNumberField(map, OTHER_PROJECT_PLAN, otherProjectsPlan);
        appendNumberField(map, OTHER_PRESALE_PLAN, otherPresalePlan);
        appendNumberField(map, OTHER_PROJECTS_AND_PRESALES_PLAN, otherProjectsPlan + otherPresalePlan);
        appendNumberField(map, OTHER_COMERCIAL_PROJECT_PLAN, otherComercialProjectPlan);
        appendNumberField(map, OTHER_INVEST_PROJECT_PLAN, otherInvestProjectPlan);
        appendNumberField(map, ILLNESS_PLAN, illnessPlanPercent);
        appendNumberField(map, VACATION_PLAN, vacationPlanPercent);
        appendStringField(map, MONTH_PLAN, round(summaryPlan));

        return map;
    }

    private String round(Double value){
        return (Long.valueOf(Math.round(value))).toString();
    }

    private Double getPercent(Double partValue, Double total){
        return partValue / total * 100;
    }

    private Map<String, JsonNodeBuilder> getFacts(Employee employee, Integer year, Integer month, double summaryPlan, PlanEditForm form) {
        final Division division = employee.getDivision();
        final Map<Integer, Double> projectsFactMap = Maps.newHashMap();

        Double summaryFact        = 0.0;
        Double centerProjectsFact = 0.0;
        Double centerPresalesFact = 0.0;
        Double otherProjectsFact  = 0.0;
        Double nonProjectFact     = 0.0;
        Double sumProjectsFact    = 0.0;
        Double sumPresalesFact    = 0.0;
        Double sumInvestFact      = 0.0;
        Double sumCommerceFact    = 0.0;
        Double illnessFact = illnessService.getIllnessWorkdaysCount(employee, year, month) * TimeSheetConstants.WORK_DAY_DURATION;
        Double vacationFact = vacationService.getApprovedVacationsWorkdaysCount(employee, year, month) * TimeSheetConstants.WORK_DAY_DURATION;

        Double illnessFactPercent = getPercent(illnessFact, summaryPlan);
        Double vacationFactPercent = getPercent(vacationFact, summaryPlan);

        for (TimeSheet timeSheet : timeSheetService.getTimeSheetsForEmployee(employee, year, month)) {
            for (TimeSheetDetail timeSheetDetail : timeSheet.getTimeSheetDetails()) {
                if(timeSheetDetail.getActType() == null){
                    // пропускаем пустые отчеты
                    continue;
                }
                double duration = nilIfNull(timeSheetDetail.getDuration());
                duration = getPercent(duration, summaryPlan); // так как отображается в процентах, то переводим
                summaryFact += duration;

                if (timeSheetDetail.getActType().getId().equals(TypesOfActivityEnum.NON_PROJECT.getId())) {
                    nonProjectFact += duration;
                }

                final Project project = timeSheetDetail.getProject();

                if (project != null) {
                    Integer projectId = project.getId();

                    if (division.equals(project.getDivision())) {
                        if (isPresale(project)) {
                            centerPresalesFact += duration;
                        } else {
                            centerProjectsFact += duration;
                        }
                    } else {
                        otherProjectsFact += duration;
                    }

                    /* расчёт итого по проектам/пресейлам */
                    if (isProject(project)) {
                        sumProjectsFact += duration;
                    } else {
                        sumPresalesFact += duration;
                    }

                    Integer projectDivisionId = project.getDivision() != null ? project.getDivision().getId() : null;
                    Integer employeeDivisionId = employee.getDivision() != null ? employee.getDivision().getId() : null;
                    boolean isEqual = (projectDivisionId !=null && employeeDivisionId !=null && projectDivisionId.equals(employeeDivisionId));

                    /* расчёт итого по инвест/комерц проектам */
                    if (isCommercialProject(project) || !isEqual || !isPresale(project)) {
                        sumCommerceFact += duration;
                    }else{
                        sumInvestFact += duration;
                    }

                    projectsFactMap.put(projectId, nilIfNull(projectsFactMap.get(projectId)) + duration);
                }
            }
        }
        final Map<String, JsonNodeBuilder> map = Maps.newHashMap();

        for (Map.Entry<Integer, Double> entry : projectsFactMap.entrySet()) {
            appendNumberField(map, String.format("%d" + _FACT, entry.getKey()), entry.getValue());
        }

        summaryFact += vacationFact;
        summaryFact += illnessFact;

        appendNumberField(map, SUMMARY_FACT, summaryFact);
        appendStringField(map, PERCENT_OF_CHARGE_FACT, round(100 * summaryFact / summaryPlan));
        appendNumberField(map, CENTER_PROJECTS_FACT, centerProjectsFact);
        appendNumberField(map, CENTER_PRESALES_FACT, centerPresalesFact);
        appendNumberField(map, OTHER_PROJECTS_AND_PRESALES_FACT, otherProjectsFact);
        appendNumberField(map, NON_PROJECT_FACT, nonProjectFact);
        if (form.getShowSumProjectsPresales()) {
            appendNumberField(map, SUMMARY_PROJECTS_FACT, sumProjectsFact);
            appendNumberField(map, SUMMARY_PRESALES_FACT, sumPresalesFact);
        }
        if (form.getShowSumFundingType()) {
            appendNumberField(map, SUMMARY_INVESTMENT_FACT, sumInvestFact + nonProjectFact);
            appendNumberField(map, SUMMARY_COMMERCIAL_FACT, sumCommerceFact);
        }
        appendNumberField(map, ILLNESS_FACT, illnessFactPercent);
        appendNumberField(map, VACATION_FACT, vacationFactPercent);

        return map;
    }

    private String getFieldNameForEmployeePlan(EmployeePlan employeePlan) {
        for (Map.Entry<JsonStringNode, TSEnum> entry : PLAN_TYPE_MAP.entrySet()) {
            if (entry.getValue() == EnumsUtils.getEnumById(employeePlan.getType(), EmployeePlanType.class)) {
                return entry.getKey().getText();
            }
        }

        throw new IllegalArgumentException();
    }

    private void appendNumberField(Map<String, JsonNodeBuilder> map, String fieldName, Double value) {
        if (value != null) {
            map.put(fieldName, aNumberBuilder(Math.round(value)));
        }
    }

    private void appendStringField(Map<String, JsonNodeBuilder> map, String fieldName, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(fieldName, aStringBuilder(value));
        }
    }

    private List<Integer> getRegionIds(PlanEditForm form) {
        final List<Integer> regions = form.getRegions();

        if (regions.contains(PlanEditForm.ALL_VALUE)) {
            return Arrays.asList(EmployeeDAO.ALL_REGIONS);
        }

        return regions;
    }

    private List<Integer> getProjectRoleIds(PlanEditForm form) {
        final List<Integer> projectRoles = form.getProjectRoles();

        if (projectRoles.contains(PlanEditForm.ALL_VALUE)) {
            return Arrays.asList(EmployeeDAO.ALL_PROJECT_ROLES);
        }

        return projectRoles;
    }
}
