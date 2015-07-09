package com.aplana.timesheet.controller;

import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectPercentPlan;
import com.aplana.timesheet.form.AddEmployeeForm;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import com.aplana.timesheet.service.EmployeeProjectPlanService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.EmploymentPlanningService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.timesheet.service.ProjectService.*;

@Controller
public class EmploymentPlanningController {
    private static final Logger logger = LoggerFactory.getLogger(EmploymentPlanningController.class);

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    EmploymentPlanningService employmentPlanningService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProjectService projectService;

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

    /* Возвращает JSON список планов сотрудников, которые участвуют на выбранном проекте в указанные временные рамки */
    @RequestMapping(value="/employmentPlanning/getEmployeesPlanAsJSON", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String showProjectPlan(@ModelAttribute(EmploymentPlanningForm.FORM) EmploymentPlanningForm form) {
        List<Employee> employees = employeeProjectPlanService.getEmployeesWhoWillWorkOnProject(
                form.getProjectId(),
                form.getMonthBeg(), form.getYearBeg(),
                form.getMonthEnd(), form.getYearEnd()
        );

        Map<Employee, List<ProjectPercentPlan>> planList =
                employeeProjectPlanService.getEmployeesPlan(
                        employees, form.getYearBeg(), form.getMonthBeg(), form.getYearEnd(), form.getMonthEnd()
                );

        return employmentPlanningService.getEmployeesPlanAsJSON(planList);
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

        return projectService.getProjectListAsJson(projectList, new String[]{PROJECT_ID, PROJECT_NAME});
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
