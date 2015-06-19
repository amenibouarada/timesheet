package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.monthreport.MutualWork;
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

    public String getMutualWorks(int year, int month, String regions, Integer divisionOwner, Integer divisionEmployee, Integer projectId) throws IOException {
        List<MutualWork> result;
        result = mutualWorkDAO.getMutualWorks(year, month, StringUtil.stringToList(regions), divisionOwner, divisionEmployee, projectId, false);
        return createMutualWorksJSON(result);
    }

    private String createMutualWorksJSON(List<MutualWork> mutualWorks) throws IOException {
        if (mutualWorks == null) {
            return "[]";
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> mutualWorkList = new ArrayList<Map<String, Object>>(mutualWorks.size());
        for (MutualWork mutualWork : mutualWorks) {
            HashMap<String, Object> mutualWorkMap = new HashMap<String, Object>();
            mutualWorkMap.put("identifier", mutualWork.getIdentifier());
            mutualWorkMap.put("divisionOwnerId", mutualWork.getDivisionOwnerId());
            mutualWorkMap.put("divisionOwnerName", mutualWork.getDivisionOwnerName());
            mutualWorkMap.put("projectId", mutualWork.getProjectId());
            mutualWorkMap.put("projectName", mutualWork.getProjectName());
            mutualWorkMap.put("projectTypeId", mutualWork.getProjectTypeId());
            mutualWorkMap.put("projectTypeName", mutualWork.getProjectTypeName());
            mutualWorkMap.put("employeeId", mutualWork.getEmployeeId());
            mutualWorkMap.put("employeeName", mutualWork.getEmployeeName());
            mutualWorkMap.put("divisionEmployeeId", mutualWork.getDivisionEmployeeId());
            mutualWorkMap.put("divisionEmployeeName", mutualWork.getDivisionEmployeeName());
            mutualWorkMap.put("regionId", mutualWork.getRegionId());
            mutualWorkMap.put("regionName", mutualWork.getRegionName());
            mutualWorkMap.put("workDays", mutualWork.getWorkDays());
            mutualWorkMap.put("overtimes", mutualWork.getOvertimes());
            mutualWorkMap.put("coefficient", mutualWork.getCoefficient());
            mutualWorkMap.put("workDaysCalc", mutualWork.getWorkDaysCalc());
            mutualWorkMap.put("overtimesCalc", mutualWork.getOvertimesCalc());
            mutualWorkMap.put("comment", mutualWork.getComment());
            mutualWorkList.add(mutualWorkMap);
        }

        return mapper.writeValueAsString(mutualWorkList);
    }

    public boolean saveMutualWorkTable(int year, int month, String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> mutualWorks = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, Object> mutualWorkMap : mutualWorks) {
            MutualWork mutualWork = new MutualWork();
            mutualWork.setYear(year);
            mutualWork.setMonth(month);
            mutualWork.setDivisionEmployeeId((Integer) mutualWorkMap.get("employeeId"));
            mutualWork.setDivisionEmployeeName((String) mutualWorkMap.get("employeeName"));
            mutualWork.setDivisionOwnerId((Integer) mutualWorkMap.get("divisionOwnerId"));
            mutualWork.setDivisionOwnerName((String) mutualWorkMap.get("divisionOwnerName"));
            mutualWork.setProjectId((Integer) mutualWorkMap.get("projectId"));
            mutualWork.setProjectName((String) mutualWorkMap.get("projectName"));
            mutualWork.setProjectTypeId((Integer) mutualWorkMap.get("projectTypeId"));
            mutualWork.setProjectTypeName((String) mutualWorkMap.get("projectTypeName"));
            mutualWork.setRegionId((Integer) mutualWorkMap.get("regionId"));
            mutualWork.setRegionName((String) mutualWorkMap.get("regionName"));
            mutualWork.setWorkDays((mutualWorkMap.get("workDays") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("workDays")) : (Double) mutualWorkMap.get("workDays"));
            mutualWork.setOvertimes((mutualWorkMap.get("overtimes") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("overtimes")) : (Double) mutualWorkMap.get("overtimes"));
            mutualWork.setCoefficient((mutualWorkMap.get("coefficient") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("coefficient")) : (Double) mutualWorkMap.get("coefficient"));
            mutualWork.setWorkDaysCalc((mutualWorkMap.get("workDaysCalc") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("workDaysCalc")) : (Double) mutualWorkMap.get("workDaysCalc"));
            mutualWork.setOvertimesCalc((mutualWorkMap.get("overtimesCalc") instanceof Integer) ?
                    new Double((Integer) mutualWorkMap.get("overtimesCalc")) : (Double) mutualWorkMap.get("overtimesCalc"));
            mutualWork.setComment((String) mutualWorkMap.get("comment"));

            mutualWorkDAO.save(mutualWork);
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
