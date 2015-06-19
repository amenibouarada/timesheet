package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.dao.entity.monthreport.MutualWorkData;
import com.aplana.timesheet.dao.monthreport.MutualWorkDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.Report03;
import com.aplana.timesheet.service.JasperReportService;
import com.aplana.timesheet.util.StringUtil;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AAfanasyev on 17.06.2015.
 */

@Service
public class MutualWorkService {

    @Autowired
    private MutualWorkDAO mutualWorkDAO;

    @Autowired
    private JasperReportDAO reportDAO;

    @Autowired
    private JasperReportService jasperReportService;

    public String getMutualWorkData(int year, int month, String regions, Integer divisionOwner, Integer divisionEmployee, Integer projectId) throws IOException {
        List<MutualWorkData> result;
        result = mutualWorkDAO.getMutualWorkData(year, month, StringUtil.stringToList(regions), divisionOwner, divisionEmployee, projectId, false);
        return createMutualWorkDataJSON(result);
    }

    private String createMutualWorkDataJSON(List<MutualWorkData> mutualWorkDataList) throws IOException {
        if (mutualWorkDataList == null) {
            return "[]";
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> mutualWorkList = new ArrayList<Map<String, Object>>(mutualWorkDataList.size());
        for (MutualWorkData mutualWorkData : mutualWorkDataList) {
            HashMap<String, Object> mutualWorkMap = new HashMap<String, Object>();
            mutualWorkMap.put("identifier", mutualWorkData.getIdentifier());
            mutualWorkMap.put("divisionOwnerId", mutualWorkData.getDivisionOwnerId());
            mutualWorkMap.put("divisionOwnerName", mutualWorkData.getDivisionOwnerName());
            mutualWorkMap.put("projectId", mutualWorkData.getProjectId());
            mutualWorkMap.put("projectName", mutualWorkData.getProjectName());
            mutualWorkMap.put("projectTypeId", mutualWorkData.getProjectTypeId());
            mutualWorkMap.put("projectTypeName", mutualWorkData.getProjectTypeName());
            mutualWorkMap.put("employeeId", mutualWorkData.getEmployeeId());
            mutualWorkMap.put("employeeName", mutualWorkData.getEmployeeName());
            mutualWorkMap.put("divisionEmployeeId", mutualWorkData.getDivisionEmployeeId());
            mutualWorkMap.put("divisionEmployeeName", mutualWorkData.getDivisionEmployeeName());
            mutualWorkMap.put("regionId", mutualWorkData.getRegionId());
            mutualWorkMap.put("regionName", mutualWorkData.getRegionName());
            mutualWorkMap.put("workDays", mutualWorkData.getWorkDays());
            mutualWorkMap.put("overtimes", mutualWorkData.getOvertimes());
            mutualWorkMap.put("coefficient", mutualWorkData.getCoefficient());
            mutualWorkMap.put("workDaysCalc", mutualWorkData.getWorkDaysCalc());
            mutualWorkMap.put("overtimesCalc", mutualWorkData.getOvertimesCalc());
            mutualWorkMap.put("comment", mutualWorkData.getComment());
            mutualWorkList.add(mutualWorkMap);
        }

        return mapper.writeValueAsString(mutualWorkList);
    }

    public boolean saveMutualWorkTable(int year, int month, String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> mutualWorks = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, Object> mutualWorkMap : mutualWorks) {
            MutualWorkData mutualWorkData = new MutualWorkData();
            mutualWorkData.setYear(year);
            mutualWorkData.setMonth(month);
            mutualWorkData.setDivisionEmployeeId((Integer) mutualWorkMap.get("employeeId"));
            mutualWorkData.setDivisionEmployeeName((String) mutualWorkMap.get("employeeName"));
            mutualWorkData.setDivisionOwnerId((Integer) mutualWorkMap.get("divisionOwnerId"));
            mutualWorkData.setDivisionOwnerName((String) mutualWorkMap.get("divisionOwnerName"));
            mutualWorkData.setProjectId((Integer) mutualWorkMap.get("projectId"));
            mutualWorkData.setProjectName((String) mutualWorkMap.get("projectName"));
            mutualWorkData.setProjectTypeId((Integer) mutualWorkMap.get("projectTypeId"));
            mutualWorkData.setProjectTypeName((String) mutualWorkMap.get("projectTypeName"));
            mutualWorkData.setRegionId((Integer) mutualWorkMap.get("regionId"));
            mutualWorkData.setRegionName((String) mutualWorkMap.get("regionName"));
            mutualWorkData.setWorkDays((mutualWorkMap.get("workDays") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("workDays")) : (Double) mutualWorkMap.get("workDays"));
            mutualWorkData.setOvertimes((mutualWorkMap.get("overtimes") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("overtimes")) : (Double) mutualWorkMap.get("overtimes"));
            mutualWorkData.setCoefficient((mutualWorkMap.get("coefficient") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("coefficient")) : (Double) mutualWorkMap.get("coefficient"));
            mutualWorkData.setWorkDaysCalc((mutualWorkMap.get("workDaysCalc") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("workDaysCalc")) : (Double) mutualWorkMap.get("workDaysCalc"));
            mutualWorkData.setOvertimesCalc((mutualWorkMap.get("overtimesCalc") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("overtimesCalc")) : (Double) mutualWorkMap.get("overtimesCalc"));
            mutualWorkData.setComment((String) mutualWorkMap.get("comment"));

            mutualWorkDAO.save(mutualWorkData);
        }

        return true;
    }

    public void prepareReport3Data(
            String beginDate,
            String endDate,
            Integer region,
            Integer divisionOwner,
            Integer divisionEmployee,
            Integer projectId,
            Integer employeeId,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException, JReportBuildError {

        List<Integer> regionIds = new ArrayList<Integer>();

        regionIds.add(region);

        Report03 report = new Report03();
        report.setDivisionOwnerId(divisionOwner);
        report.setEmplDivisionId(divisionEmployee);
        report.setProjectId(projectId);
        report.setEmployeeId(employeeId);
        report.setRegionIds(regionIds);
        report.setBeginDate(beginDate);
        report.setEndDate(endDate);
        report.setReportDAO(reportDAO);

        jasperReportService.makeReport(report, 2, true, response, request);
    }
}
