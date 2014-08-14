package com.aplana.timesheet.controller;

import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeePercentPlan;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectPercentPlan;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import com.aplana.timesheet.service.EmployeeProjectPlanService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.EmploymentPlanningService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@Controller
public class EmploymentPlanningController{
    private static final Logger logger = LoggerFactory.getLogger(EmploymentPlanningController.class);

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    EmploymentPlanningService employmentPlanningService;

    @Autowired
    private EmployeeService employeeService;

    /* страница по умолчанию */
    @RequestMapping("/employmentPlanning")
    public ModelAndView showForm(@ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form) {
        final ModelAndView modelAndView = new ModelAndView("employmentPlanning");
        employmentPlanningService.fillDefaultModelAndView(form, modelAndView);
        employmentPlanningService.fillDefaultForm(form);
        modelAndView.addObject("form", form);

        return modelAndView;
    }


    /* страница с запрошенными данными */
    @RequestMapping(value = "/employmentPlanning", method = RequestMethod.POST)
    public ModelAndView showTable( @ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form) {
        final ModelAndView modelAndView = new ModelAndView("employmentPlanning");
        employmentPlanningService.fillDefaultModelAndView(form, modelAndView);
        modelAndView.addObject("form", form);

        return modelAndView;
    }

    /* Возвращает JSON для грида занятости сотрудников на проекте */
    @RequestMapping(value="/employmentPlanning/getProjectPlanAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showProjectPlan(@ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form) {
        List<EmployeePercentPlan> projectPlanList = employeeProjectPlanService.getProjectPlan(form);

        return employmentPlanningService.getProjectPlanAsJSON(projectPlanList);
    }

    /* Возвращает JSON для грида занятости сотрудника на проектах*/
    @RequestMapping(value="/employmentPlanning/getEmployeePlanAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showEmployeePlan(
            @ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form,
            @RequestParam("employeeId") Integer employeeId
    ) {
        List<ProjectPercentPlan> planList = employeeProjectPlanService.getEmployeePlan(employeeId, form.getYearBeg(), form.getMonthBeg(), form.getYearEnd(), form.getMonthEnd());

        return employmentPlanningService.getEmployeePlanAsJSON(planList);
    }


    /* Возвращает JSON для форме выбора сотрудников */
    @RequestMapping(value="/employmentPlanning/getAddEmployeeListAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showAddEmployeeList(@ModelAttribute(AddEmployeeForm.ADD_FORM) AddEmployeeForm form) {
        List<Employee> employeeList = employeeService.getEmployeeByDivisionManagerRoleRegion(form.getDivisionId(), form.getManagerId(), form.getProjectRoleListId(), form.getRegionListId());

        return employmentPlanningService.getEmployeeListAsJson(employeeList);
    }

    /* Возвращает JSON для форме выбора сотрудников */
    @RequestMapping(value="/employmentPlanning/getProjectByDivisionAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showAddEmployeeList(
            @RequestParam("divisionId") String divisionId,
            @RequestParam("monthBegin") Integer monthBegin,
            @RequestParam("yearBegin") Integer yearBegin) {
        Date date = DateTimeUtil.createDate(yearBegin, monthBegin);
        List<Project> projectList = employmentPlanningService.getProjects(Integer.parseInt(divisionId), date);

        return employmentPlanningService.getProjectListAsJson(projectList);
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
                    throw new RuntimeException(nfe);
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
        employeeProjectPlanService.saveProjectData(jsonData, employeeId);
        return "+OK";
    }
}
