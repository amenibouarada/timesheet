package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.monthreport.MutualWork;
import com.aplana.timesheet.dao.entity.monthreport.MutualWorkData;
import com.aplana.timesheet.dao.monthreport.MutualWorkDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.Report03;
import com.aplana.timesheet.service.JasperReportService;
import com.aplana.timesheet.util.NumberUtils;
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

    @Autowired private MutualWorkDAO    mutualWorkDAO;
    @Autowired private JasperReportDAO  reportDAO;
    @Autowired private EmployeeDAO      employeeDAO;
    @Autowired private ProjectDAO       projectDAO;

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
            Project project = projectDAO.find((Integer) mutualWorkMap.get("projectId"));
            Employee employee = employeeDAO.find((Integer)mutualWorkMap.get("employeeId"));
            MutualWork mutualWork = mutualWorkDAO.findOrCreateMutualWork(employee, project);

            mutualWork.setYear(year);
            mutualWork.setMonth(month);
            mutualWork.setEmployee(employee);
            mutualWork.setProject(project);
            mutualWork.setWorkDays(     NumberUtils.getDoubleValue(mutualWorkMap.get("workDays")));
            mutualWork.setOvertimes(NumberUtils.getDoubleValue(mutualWorkMap.get("overtimes")));
            mutualWork.setCoefficient(NumberUtils.getDoubleValue(mutualWorkMap.get("coefficient")));
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

        jasperReportService.makeReport(report, jasperReportService.REPORT_PRINTTYPE_XLS, response, request);
    }
}
