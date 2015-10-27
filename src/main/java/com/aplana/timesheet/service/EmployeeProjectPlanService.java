package com.aplana.timesheet.service;

import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.dao.EmployeeProjectPlanDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeProjectPlan;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectPercentPlan;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    public void updateEmployeeProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm,
                                          double plannedWorkingPercent) {
        Integer projectId = employmentPlanningForm.getProjectId();
        Integer year = employmentPlanningForm.getYearBeg();
        Integer month = employmentPlanningForm.getMonthBeg();

        double workingHours = getEmployeeWorkingHours(employeeId, year, month, plannedWorkingPercent);

        employeeProjectPlanDAO.updateEmployeeProjectPlan(projectId, employeeId, year, month, workingHours);
    }

    @Transactional
    public void updateEmployeeNotProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm, Double plan){
        employeeProjectPlanDAO.updateEmployeeNotProjectPlan(employeeId, employmentPlanningForm, plan);
    }

    @Transactional(readOnly = true)
    public Map<Employee, List<ProjectPercentPlan>> getEmployeesPlan(List<Employee> employeeIds, Integer yearBeg, Integer monthBeg, Integer yearEnd, Integer monthEnd){
        HashMap<Employee, List<ProjectPercentPlan>> result = new HashMap<Employee, List<ProjectPercentPlan>>(employeeIds.size());
        for (Employee employee : employeeIds){

            List<Object[]> employeePlan =  employeeProjectPlanDAO.getEmployeePlan(employee.getId(), yearBeg, monthBeg, yearEnd, monthEnd);
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

            result.put(employee, percentPlanList);
        }
        return result;
    }

    public List<Employee> getEmployeesWhoWillWorkOnProject(int projectId, int beginMonth, int beginYear, int endMonth, int endYear){
        return employeeProjectPlanDAO.getEmployeesWhoWillWorkOnProject(projectId, beginMonth, beginYear, endMonth, endYear);
    }

    public List<Employee> getEmployeesWhoWillWorkOnProject(int projectId, Date beginDate, Date endDate){
        Integer beginMonth = DateTimeUtil.getMonth(beginDate) + 1; // в БД нумерация с 1
        Integer beginYear = DateTimeUtil.getYear(beginDate);
        Integer endMonth = DateTimeUtil.getMonth(endDate) + 1; // в БД нумерация с 1
        Integer endYear = DateTimeUtil.getYear(endDate);

        return getEmployeesWhoWillWorkOnProject(projectId, beginMonth, beginYear, endMonth, endYear);
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

    /**
     * Рассчитать количество рабочих часов сотрудника за 1 месяц
     *
     * @param employeeId идентификатор сотрудника
     * @param year       год
     * @param month      меясц
     * @param plan       план в процентах
     * @return количество рабочих часов
     */
    private double getEmployeeWorkingHours(Integer employeeId, Integer year, Integer month, Double plan) {
        Integer workDays = employeeProjectPlanDAO.getEmployeeWorkingDaysCount(year, month, employeeId);

        return 8 * plan * workDays / 100;
    }
}
