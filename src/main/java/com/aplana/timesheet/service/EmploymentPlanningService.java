package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static argo.jdom.JsonNodeBuilders.*;
import static argo.jdom.JsonNodeBuilders.aStringBuilder;

/**
 * Created by abayanov
 * Date: 13.08.14
 */
@Service
public class EmploymentPlanningService {

    private static final Integer ALL = -1;

    @Autowired
    private CalendarService calendarService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private EmployeeService employeeService;

    private List<Calendar> getYearList() {
        return DateTimeUtil.getYearsList(calendarService);
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
        modelAndView.addObject("divisionList", divisionService.getDivisions());
        modelAndView.addObject("managerList", employeeService.getManagerListJson());
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
    public List<Project> getProjects(Integer divisionId, Date date) {
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

}
