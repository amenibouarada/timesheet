package com.aplana.timesheet.service;

import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.dao.EmployeeProjectPlanDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class EmployeeProjectPlanService {

    @Autowired
    private EmployeeProjectPlanDAO employeeProjectPlanDAO;

    public List<EmployeeProjectPlan> find(Employee employee, Integer year, Integer month) {
        return employeeProjectPlanDAO.find(employee, year, month);
    }

    @Transactional
    public void mergeEmployeeProjectPlans(List<EmployeeProjectPlan> employeeProjectPlans) {
        for (EmployeeProjectPlan employeeProjectPlan : employeeProjectPlans) {
            if (employeeProjectPlan.getValue() == null || employeeProjectPlan.getValue() == 0)
                employeeProjectPlanDAO.remove(employeeProjectPlan);
            else
                employeeProjectPlanDAO.store(employeeProjectPlan);
        }

    }

    @Transactional
    public void store(EmployeeProjectPlan employeeProjectPlan) {
        employeeProjectPlanDAO.store(employeeProjectPlan);
    }

    public EmployeeProjectPlan tryFind(Employee employee, Integer year, Integer month, Project project) {
        return employeeProjectPlanDAO.tryFind(employee, year, month, project);
    }

    @Transactional
    public void remove(Employee employee, Integer year, Integer month) {
        employeeProjectPlanDAO.remove(employee, year, month);
    }

    @Transactional
    public void updateEmployeeProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm, Double plan){
        employeeProjectPlanDAO.updateEmployeeProjectPlan(employeeId, employmentPlanningForm, plan);
    }

    @Transactional
    public void updateEmployeeNotProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm, Double plan){
        employeeProjectPlanDAO.updateEmployeeNotProjectPlan(employeeId, employmentPlanningForm, plan);
    }

    @Transactional(readOnly = true)
    public List<EmployeePercentPlan> getProjectPlan(EmploymentPlanningForm employmentPlanningForm){
        List<Object[]> projectPlan = employeeProjectPlanDAO.getProjectPlan(employmentPlanningForm);
        List<EmployeePercentPlan> percentPlanList = new ArrayList<EmployeePercentPlan>(projectPlan.size());

        for (Object[] objectPlan : projectPlan){
            Integer employeeId = (Integer)objectPlan[0];
            String  employeeName = (String)objectPlan[1];
            Integer year = (Integer)objectPlan[2];
            Integer month = (Integer)objectPlan[3];
            Double  value = (Double)objectPlan[4];

            EmployeePercentPlan employeePercentPlan = new EmployeePercentPlan(employeeId, employeeName, year, month, value);
            percentPlanList.add(employeePercentPlan);
        }

        return percentPlanList;
    }

    @Transactional(readOnly = true)
    public List<ProjectPercentPlan> getEmployeePlan(Integer employeeId, Integer yearBeg, Integer monthBeg, Integer yearEnd, Integer monthEnd){
        List<Object[]> employeePlan =  employeeProjectPlanDAO.getEmployeePlan(employeeId, yearBeg, monthBeg, yearEnd, monthEnd);
        List<ProjectPercentPlan> percentPlanList = new ArrayList<ProjectPercentPlan>(employeePlan.size());

        for (Object[] projectPlan : employeePlan){
            Integer projectId = (Integer)projectPlan[0];
            String  projectName = (String)projectPlan[1];
            Integer month = (Integer)projectPlan[2];
            Integer year = (Integer)projectPlan[3];
            Double  value = (Double)projectPlan[4];
            Integer isFact = (Integer) projectPlan[5];

            ProjectPercentPlan projectPercentPlan = new ProjectPercentPlan(projectId, projectName, month, year, value, isFact);
            percentPlanList.add(projectPercentPlan);
        }

        return percentPlanList;
    }

    public List<Employee> getEmployesWhoWillWorkOnProject(Project project, Date beginDate, Date endDate, List<Integer> excludeIds){
       return employeeProjectPlanDAO.getEmployesWhoWillWorkOnProject(project, beginDate, endDate, excludeIds);
    }

    public void saveProjectData(String jsonData, Integer employeeId) throws InvalidSyntaxException {
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
                        updateEmployeeProjectPlan(employeeId, employmentPlanningForm, value);
                    } else {
                        employmentPlanningForm.setProjectId(-projectId);
                        updateEmployeeNotProjectPlan(employeeId, employmentPlanningForm, value);
                    }
                }
            }
        }

    }
}
