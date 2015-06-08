package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.OvertimeDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Overtime;
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

        List<Map<String, String>> overtimes = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, String> overtimeMap : overtimes){
            Overtime overtime = new Overtime();
            String idString = overtimeMap.get("id");
            if (idString != ""){
                overtime.setId(Long.parseLong(idString));
            }
            overtime.setEmployee(employeeDAO.find(Integer.parseInt(overtimeMap.get("employeeId"))));
            overtime.setProject(projectDAO.find(Integer.parseInt(overtimeMap.get("projectId"))));
            overtime.setYear(year);
            overtime.setMonth(month);
            String overtimeString = overtimeMap.get("overtime");
            overtime.setOvertime(overtimeString == "" ? 0.0 : Double.parseDouble(overtimeString));
            String premiumString = overtimeMap.get("premium");
            overtime.setPremium(premiumString == "" ? 0.0 : Double.parseDouble(premiumString));
            overtime.setComment(overtimeMap.get("comment"));
            overtimeDAO.save(overtime);
        }

        return true;
    }

    public boolean deleteOvertimes(String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
        List<Map<String, String>> overtimes = mapper.readValue(jsonData, mapCollectionType);
        List<Long> idsToDelete = new ArrayList<Long>();
        for (Map<String, String> overtimeMap : overtimes){
            String idString = overtimeMap.get("id");
            if (idString != ""){
                idsToDelete.add(Long.parseLong(idString));
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
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, String>> overtimeList = new ArrayList<Map<String, String>>(overtimes.size());
        for (Overtime overtime : overtimes){
            HashMap<String, String> overtimeMap = new HashMap<String, String>();
            overtimeMap.put("id", new Long(overtime.getId()).toString());
            overtimeMap.put("employee", overtime.getEmployee().getName());
            overtimeMap.put("employeeId", overtime.getEmployee().getId().toString());
            overtimeMap.put("division", overtime.getEmployee().getDivision().getName());
            overtimeMap.put("divisionId", overtime.getEmployee().getDivision().getId().toString());
            overtimeMap.put("region", overtime.getEmployee().getRegion().getName());
            overtimeMap.put("regionId", overtime.getEmployee().getRegion().getId().toString());
            overtimeMap.put("type", overtime.getProject().getState().getValue());
            overtimeMap.put("typeId", overtime.getProject().getState().getId().toString());
            overtimeMap.put("project", overtime.getProject().getName());
            overtimeMap.put("projectId", overtime.getProject().getId().toString());
            overtimeMap.put("overtime", overtime.getOvertime().toString());
            overtimeMap.put("premium", overtime.getPremium().toString());
            overtimeMap.put("comment", overtime.getComment().toString());
            overtimeList.add(overtimeMap);
        }

        return mapper.writeValueAsString(overtimeList);

    }

    public List<Calendar> getYearsList(){
        return DateTimeUtil.getYearsList(calendarService);
    }


}
