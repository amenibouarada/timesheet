package com.aplana.timesheet.controller;

import argo.jdom.*;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ProjectFundingTypeEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import com.aplana.timesheet.form.PlanEditForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.Calendar;

import static argo.jdom.JsonNodeBuilders.*;

@Controller
public class EmploymentPlanningController{
    private static final Logger logger = LoggerFactory.getLogger(EmploymentPlanningController.class);
    private static final Integer ALL = -1;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProjectService projectService;

    private List<com.aplana.timesheet.dao.entity.Calendar> getYearList() {
        return DateTimeUtil.getYearsList(calendarService);
    }

    /* страница по умолчанию */
    @RequestMapping("/employmentPlanning")
    public ModelAndView showForm(@ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form) {
        final ModelAndView modelAndView = new ModelAndView("employmentPlanning");
        fillDefaultModelAndView(form, modelAndView);
        fillDefaultForm(form);
        modelAndView.addObject("form", form);

        return modelAndView;
    }


    /* страница с запрошенными данными */
    @RequestMapping(value = "/employmentPlanning", method = RequestMethod.POST)
    public ModelAndView showTable( @ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form) {
        final ModelAndView modelAndView = new ModelAndView("employmentPlanning");
        fillDefaultModelAndView(form, modelAndView);
        modelAndView.addObject("form", form);

        return modelAndView;
    }

    /* Возвращает JSON для грида занятости сотрудников на проекте */
    @RequestMapping(value="/employmentPlanning/getProjectPlanAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showProjectPlan(@ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form) {
        List<EmployeePercentPlan> projectPlanList = employeeProjectPlanService.getProjectPlan(form);
        String projectPlanAsJSON = getProjectPlanAsJSON(projectPlanList);

        return projectPlanAsJSON;
    }

    /* Возвращает JSON для грида занятости сотрудника на проектах*/
    @RequestMapping(value="/employmentPlanning/getEmployeePlanAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showEmployeePlan(
            @ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form,
            @RequestParam("employeeId") Integer employeeId
    ) {
        List<ProjectPercentPlan> planList = employeeProjectPlanService.getEmployeePlan(employeeId, form.getYearBeg(), form.getMonthBeg(), form.getYearEnd(), form.getMonthEnd());
        String employeePlanAsJSON = getEmployeePlanAsJSON(planList);

        return employeePlanAsJSON;
    }


    /* Возвращает JSON для форме выбора сотрудников */
    @RequestMapping(value="/employmentPlanning/getAddEmployeeListAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showAddEmployeeList(@ModelAttribute(AddEmployeeForm.ADD_FORM) AddEmployeeForm form) {
        List<Employee> employeeList = employeeService.getEmployeeByDivisionManagerRoleRegion(form.getDivisionId(), form.getManagerId(), form.getProjectRoleListId(), form.getRegionListId());
        String employeeListAsJSON = getEmployeeListAsJson(employeeList);

        return employeeListAsJSON;
    }

    /* Возвращает JSON для форме выбора сотрудников */
    @RequestMapping(value="/employmentPlanning/getProjectByDivisionAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showAddEmployeeList(
            @RequestParam("divisionId") String divisionId,
            @RequestParam("monthBegin") Integer monthBegin,
            @RequestParam("yearBegin") Integer yearBegin) {
        Date date = DateTimeUtil.createDate(yearBegin, monthBegin);
        List<Project> projectList = getProjects(Integer.parseInt(divisionId), date);
        String projectListAsJson = getProjectListAsJson(projectList);

        return projectListAsJson;
    }

    /* Сохраняем данные план по сотрудникам по проекту*/
    @RequestMapping(value="/employmentPlanning/setEmployeeProjectAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveEmployeeData(@ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form,
                                   @RequestParam("jsonData") String jsonData) throws InvalidSyntaxException {
        JdomParser jdomParser = new JdomParser();
        JsonRootNode rootNode = jdomParser.parse(jsonData);
        List<JsonField> jsonFieldList = rootNode.getFieldList();
        JsonField jsonField = jsonFieldList.get(0);
        JsonNode jsonNode = jsonField.getValue();
        List<JsonNode> jsonNodes = jsonNode.getElements();

        for(JsonNode node : jsonNodes){
            Integer employeeId = Integer.parseInt(node.getNumberValue("employee_id"));
            String plan = node.getStringValue("plan");
            if (plan!=null && !"".equals(plan)){
                try{
                    Double value = Double.parseDouble(plan);
                    employeeProjectPlanService.updateEmployeeProjectPlan(employeeId, form, value);
                }
                catch(NumberFormatException nfe){
                    //TODO something
                    new RuntimeException(nfe);
                }
            }
        }

        return "+OK";
    }


    /* Сохраняем данные по проектам для сотрудника*/
    @RequestMapping(value="/employmentPlanning/setProjectDataAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String saveProjectData(@RequestParam("employeeId") Integer employeeId,
                                   @RequestParam("jsonData") String jsonData) throws InvalidSyntaxException {
        JdomParser jdomParser = new JdomParser();
        JsonRootNode rootNode = jdomParser.parse(jsonData);
        List<JsonField> jsonFieldList = rootNode.getFieldList();
        JsonField jsonField = jsonFieldList.get(0);
        JsonNode jsonNode = jsonField.getValue();
        List<JsonNode> jsonNodes = jsonNode.getElements();

        for(JsonNode node : jsonNodes){
            Integer projectId = Integer.parseInt(node.getNumberValue("project_id"));
            String fields = node.getStringValue("fields");
            if (fields!=null && !"".equals(fields)){
                String[] yearMonthArray = fields.split(";");
                for(String key : yearMonthArray){
                    String[] yearMonth = key.split("_");
                    Double value = Double.parseDouble(node.getNumberValue(key));
                    Integer year = Integer.parseInt(yearMonth[0]);
                    Integer month = Integer.parseInt(yearMonth[1]);

                    EmploymentPlanningForm employmentPlanningForm = new EmploymentPlanningForm();
                    employmentPlanningForm.setMonthBeg(month);
                    employmentPlanningForm.setMonthEnd(month);
                    employmentPlanningForm.setYearBeg(year);
                    employmentPlanningForm.setYearEnd(year);

                    if (projectId > 0){
                        employmentPlanningForm.setProjectId(projectId);
                        employeeProjectPlanService.updateEmployeeProjectPlan(employeeId, employmentPlanningForm, value);
                    } else {
                        employmentPlanningForm.setProjectId(-projectId);
                        employeeProjectPlanService.updateEmployeeNotProjectPlan(employeeId, employmentPlanningForm, value);
                    }
                }
            }
        }

        return "+OK";
    }

    public void fillDefaultModelAndView(EmploymentPlanningForm form, ModelAndView modelAndView){
        final List<com.aplana.timesheet.dao.entity.Calendar> yearList = getYearList();
        Employee currentUser = securityService.getSecurityPrincipal().getEmployee();

        Date date;
        if (form.getYearBeg() != null && form.getMonthBeg() != null) {
            date = DateTimeUtil.createDate(form.getYearBeg(), form.getMonthBeg());
        } else {
            date = new Date();
        }

        modelAndView.addObject("yearList", yearList);
        modelAndView.addObject("monthList", calendarService.getMonthList(2013));
        modelAndView.addObject("projectList", getProjects(currentUser.getDivision().getId(), date));
        modelAndView.addObject("divisionList", divisionService.getActiveDivisions());
        modelAndView.addObject("managerList", managerService.getManagerList());
        modelAndView.addObject("projectRoleList", projectRoleService.getProjectRoles());
        modelAndView.addObject("regionList", regionService.getRegions());
        modelAndView.addObject("all", ALL);

        AddEmployeeForm addEmployeeForm = new AddEmployeeForm();
        addEmployeeForm.setDivisionId(1);

        modelAndView.addObject("addEmployeeForm", addEmployeeForm);
    }

    /**
     * Возвращает списко активных проектов на дату, для конкретного подразделения
     * @return
     */
    private List<Project> getProjects(Integer divisionId, Date date) {
        final List<Integer> projectStates = Arrays.asList(
                TypesOfActivityEnum.PROJECT.getId(),
                TypesOfActivityEnum.PRESALE.getId()
        );

        return projectService.getProjectsByStatesForDateAndDivisionId(
                projectStates,
                date,
                divisionId
        );
    }
    /**
     * Значения по умолчанию для формы ввода поиска сотрудников
     * @param form
     */
    public void fillDefaultForm(EmploymentPlanningForm form){
        Employee currentUser = securityService.getSecurityPrincipal().getEmployee();

        Date date = new Date();
        java.util.Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);

        Integer month = calendar.get(java.util.Calendar.MONTH) + 1;
        Integer year = calendar.get(java.util.Calendar.YEAR);
        Integer quarter = 1 + (month-1)/3;

        form.setMonthBeg(month);
        form.setYearBeg(year);
        // Конец квартала
        form.setMonthEnd(3*quarter);
        form.setYearEnd(year);
        form.setProjectId(ALL);
        form.setSelectDivisionId(currentUser.getDivision().getId());
    }

    /**
     * Получает JSON для грида занятости сотрудников на проекте
     * @param planList
     * @return
     */
    public String getProjectPlanAsJSON(List<EmployeePercentPlan> planList){
        JsonArrayNodeBuilder builder = anArrayBuilder();
        Map<Integer, JsonObjectNodeBuilder> jsonMap = new LinkedHashMap<Integer, JsonObjectNodeBuilder>();

        for(EmployeePercentPlan result : planList){
            Integer employee_id = result.getEmployeeId();
            String  employee_name = result.getEmployeeName();
            Integer year = result.getYear();
            Integer month = result.getMonth();
            Double  value = result.getPercent();

            JsonObjectNodeBuilder objectNodeBuilder = jsonMap.get(employee_id);

            if (objectNodeBuilder != null){
                objectNodeBuilder.withField(year+"_"+month, aNumberBuilder(value.toString()));
            } else {
                objectNodeBuilder = anObjectBuilder();
                objectNodeBuilder.
                        withField("employee_id", aNumberBuilder(employee_id.toString())).
                        withField("employee_name", aStringBuilder(employee_name)).
                        withField(year+"_"+month, aNumberBuilder(value.toString()));
                jsonMap.put(employee_id, objectNodeBuilder);
            }
        }

        for(Map.Entry<Integer, JsonObjectNodeBuilder> entry : jsonMap.entrySet()){
            builder.withElement(entry.getValue());
        }

        return JsonUtil.format(builder.build());
    }

    public String getEmployeePlanAsJSON(List<ProjectPercentPlan> planList){
        JsonArrayNodeBuilder builder = anArrayBuilder();
        Map<Integer, JsonObjectNodeBuilder> jsonMap = new LinkedHashMap<Integer, JsonObjectNodeBuilder>();

        for(ProjectPercentPlan result : planList){
            Integer projectId = result.getProjectId();
            String  projectName = result.getProjectName();
            Integer month = result.getMonth();
            Integer year = result.getYear();
            Double  value = result.getPercent();
            Integer isFact = result.getFact();

            String key;
            if (isFact.equals(1)){
                key = year + "-" + month;
            } else {
                key = year + "_" + month;
            }

            if (value == null){
                value = -1d;
            }

            JsonObjectNodeBuilder objectNodeBuilder = jsonMap.get(projectId);

            if (objectNodeBuilder != null){
                objectNodeBuilder.withField(key, aNumberBuilder(value.toString()));
            } else {
                objectNodeBuilder = anObjectBuilder();
                objectNodeBuilder.withField("project_id", aNumberBuilder(projectId.toString()));
                objectNodeBuilder.withField("project_name", aStringBuilder(projectName));
                objectNodeBuilder.withField(key, aNumberBuilder(value.toString()));

                jsonMap.put(projectId, objectNodeBuilder);
            }
        }

        for(Map.Entry<Integer, JsonObjectNodeBuilder> entry : jsonMap.entrySet()){
            builder.withElement(entry.getValue());
        }

        return JsonUtil.format(builder.build());
    }


    /**
     * Возвращает список сотрудников как json {id, name}
     * @param employeeList
     * @return
     */
    public String getEmployeeListAsJson(List<Employee> employeeList){
        JsonArrayNodeBuilder builder = anArrayBuilder();

        for(Employee employee : employeeList){
            JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder();
            objectNodeBuilder.withField("employee_id", aNumberBuilder(employee.getId().toString()));
            objectNodeBuilder.withField("employee_name", aStringBuilder(employee.getName()));
            builder.withElement(objectNodeBuilder);
        }

        return JsonUtil.format(builder.build());
    }

    /**
     * Возвращает список проектов как json {id, name}
     * @param projectList
     * @return
     */
    public String getProjectListAsJson(List<Project> projectList){
        JsonArrayNodeBuilder builder = anArrayBuilder();

        for(Project project : projectList){
            JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder();
            objectNodeBuilder.withField("project_id", aNumberBuilder(project.getId().toString()));
            objectNodeBuilder.withField("project_name", aStringBuilder(project.getName()));
            builder.withElement(objectNodeBuilder);
        }

        return JsonUtil.format(builder.build());
    }
}
