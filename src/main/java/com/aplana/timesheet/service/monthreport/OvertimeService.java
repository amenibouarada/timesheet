package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.monthreport.Overtime;
import com.aplana.timesheet.dao.entity.monthreport.OvertimeData;
import com.aplana.timesheet.dao.monthreport.OvertimeDAO;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.util.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OvertimeService {
    @Autowired
    private OvertimeDAO overtimeDAO;
    @Autowired
    private EmployeeDAO employeeDAO;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ProjectDAO projectDAO;

    private static final Logger logger = LoggerFactory.getLogger(OvertimeService.class);

    public boolean saveOvertimeTable(int year, int month, String jsonData) throws IOException {
        logger.debug("Старт сохранения таблицы 'Переработки'");
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> overtimes = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, Object> overtimeMap : overtimes){
            Project project = projectDAO.find((Integer) overtimeMap.get("project_id"));
            Employee employee = employeeDAO.find((Integer)overtimeMap.get("employee_id"));
            Overtime overtime = overtimeDAO.findOrCreateOvertime(employee, project, year, month);

            overtime.setOvertime(NumberUtils.getDoubleValue(overtimeMap.get("overtime")));
            overtime.setPremium(NumberUtils.getDoubleValue(overtimeMap.get("premium")));
            overtime.setComment((String)overtimeMap.get("comment"));
            logger.debug("Сохранение записи в таблицу overtime: " + overtime.toString());
            overtimeDAO.save(overtime);
        }
        return true;
    }

    public boolean deleteOvertimes(String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
        List<Map<String, Object>> overtimes = mapper.readValue(jsonData, mapCollectionType);
        List<Integer> idsToDelete = new ArrayList<Integer>();
        for (Map<String, Object> overtimeMap : overtimes){
            Integer id = (Integer)overtimeMap.get("id");
            if (id != null){
                idsToDelete.add(id);
            }
        }
        overtimeDAO.delete(idsToDelete);

        return true;
    }

    public String getOvertimes(Employee currentUser,
                               int year, int month, Integer divisionOwner, Integer divisionEmployee) throws IOException {
        List<OvertimeData> result;
        if (employeeService.isEmployeeHasPermissionsToMonthReportManage(currentUser)){
            result = overtimeDAO.getOvertimes(year, month, divisionOwner, divisionEmployee, false);
        }else{
            result = overtimeDAO.getSingleOvertime(currentUser, year, month);
        }
        return createOvertimesJSON(result);
    }

    private String createOvertimesJSON(List<OvertimeData> overtimes) throws IOException {
        if (overtimes == null){
            return "[]";
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> overtimeList = new ArrayList<Map<String, Object>>(overtimes.size());
        for (OvertimeData overtimeData : overtimes){
            HashMap<String, Object> overtimeMap = new HashMap<String, Object>();
            overtimeMap.put("identifier", overtimeData.getIdentifier());
            overtimeMap.put("employee_id", overtimeData.getEmployee_id());
            overtimeMap.put("employee_name", overtimeData.getEmployee_name());
            overtimeMap.put("division_employee_id", overtimeData.getDivision_employee_id());
            overtimeMap.put("division_employee_name", overtimeData.getDivision_employee_name());
            overtimeMap.put("region_id", overtimeData.getRegion_id());
            overtimeMap.put("region_name", overtimeData.getRegion_name());
            if (overtimeData.getProject_id() != null){
                overtimeMap.put("project_type_id", overtimeData.getProject_type_id());
                overtimeMap.put("project_type_name", overtimeData.getProject_type_name());
                overtimeMap.put("project_id", overtimeData.getProject_id());
                overtimeMap.put("project_name", overtimeData.getProject_name());
            }else{
                overtimeMap.put("project_type_name", TypesOfActivityEnum.NON_PROJECT.getName());
                overtimeMap.put("project_type_id", TypesOfActivityEnum.NON_PROJECT.getId());
                overtimeMap.put("project_id", null);
                overtimeMap.put("project_name", "");
            }
            overtimeMap.put("overtime", overtimeData.getOvertime());
            overtimeMap.put("premium", overtimeData.getPremium());
            overtimeMap.put("total_accounted_overtime", overtimeData.getTotal_accounted_overtime());
            overtimeMap.put("comment", overtimeData.getComment());
            overtimeList.add(overtimeMap);
        }

        return mapper.writeValueAsString(overtimeList);

    }
}
