package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.OvertimeDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.monthreport.Overtime;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.util.DateTimeUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonthReportService {

    @Autowired
    private OvertimeDAO overtimeDAO;
    @Autowired
    private EmployeeDAO employeeDAO;
    @Autowired
    private ProjectDAO projectDAO;
    @Autowired
    private CalendarService calendarService;

    public boolean saveOvertimeTable(int year, int month, String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> overtimes = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, Object> overtimeMap : overtimes){
            Overtime overtime = new Overtime();
            overtime.setId((Integer)overtimeMap.get("id"));
            overtime.setEmployee(employeeDAO.find((Integer)overtimeMap.get("employeeId")));
            overtime.setProject(projectDAO.find((Integer)overtimeMap.get("projectId")));
            overtime.setYear(year);
            overtime.setMonth(month);
            // ToDo можно ли упростить?
            overtime.setOvertime(
                    (overtimeMap.get("overtime") instanceof Integer) ? new Double((Integer)overtimeMap.get("overtime")) : (Double)overtimeMap.get("overtime"));
            overtime.setPremium(
                    (overtimeMap.get("premium") instanceof Integer) ? new Double((Integer)overtimeMap.get("premium")) : (Double)overtimeMap.get("premium"));
            overtime.setComment((String)overtimeMap.get("comment"));
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

    public String getOvertimesJSON(int year, int month, Integer divisionOwner, Integer divisionEmployee) throws IOException {
        return createOvertimesJSON(
                overtimeDAO.getOvertimes(year, month, divisionOwner, divisionEmployee));
    }

    private String createOvertimesJSON(List<Overtime> overtimes) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> overtimeList = new ArrayList<Map<String, Object>>(overtimes.size());
        for (Overtime overtime : overtimes){
            HashMap<String, Object> overtimeMap = new HashMap<String, Object>();
            overtimeMap.put("id", overtime.getId());
            overtimeMap.put("employee", overtime.getEmployee().getName());
            overtimeMap.put("employeeId", overtime.getEmployee().getId());
            overtimeMap.put("division", overtime.getEmployee().getDivision().getName());
            overtimeMap.put("divisionId", overtime.getEmployee().getDivision().getId());
            overtimeMap.put("region", overtime.getEmployee().getRegion().getName());
            overtimeMap.put("regionId", overtime.getEmployee().getRegion().getId());
            if (overtime.getProject() != null){
                overtimeMap.put("type", overtime.getProject().getState().getValue());
                overtimeMap.put("typeId", overtime.getProject().getState().getId());
                overtimeMap.put("project", overtime.getProject().getName());
                overtimeMap.put("projectId", overtime.getProject().getId());
            }else{
                overtimeMap.put("type", TypesOfActivityEnum.NON_PROJECT.getName());
                overtimeMap.put("typeId", TypesOfActivityEnum.NON_PROJECT.getId());
                overtimeMap.put("project", "");
                overtimeMap.put("projectId", null);
            }
            overtimeMap.put("overtime", overtime.getOvertime());
            overtimeMap.put("premium", overtime.getPremium());
            overtimeMap.put("allAccountedOvertime", overtime.getOvertime() + overtime.getPremium());
            overtimeMap.put("comment", overtime.getComment().toString());
            overtimeList.add(overtimeMap);
        }

        return mapper.writeValueAsString(overtimeList);

    }

    public List<Calendar> getYearsList(){
        return DateTimeUtil.getYearsList(calendarService);
    }


}
