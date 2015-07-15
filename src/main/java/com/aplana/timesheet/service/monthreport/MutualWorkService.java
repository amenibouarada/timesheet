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
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.util.NumberUtils;
import com.aplana.timesheet.util.StringUtil;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MutualWorkService.class);

    @Autowired private MutualWorkDAO    mutualWorkDAO;
    @Autowired private JasperReportDAO  reportDAO;
    @Autowired private EmployeeDAO      employeeDAO;
    @Autowired private ProjectDAO       projectDAO;

    @Autowired
    private JasperReportService jasperReportService;

    public boolean deleteMutualWorks(String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
        List<Map<String, Object>> mutualWorks = mapper.readValue(jsonData, mapCollectionType);
        List<Integer> idsToDelete = new ArrayList<Integer>();
        for (Map<String, Object> mutualWorkMap : mutualWorks){
            Integer id = (Integer)mutualWorkMap.get("mutual_work_id");
            if (id != null){
                idsToDelete.add(id);
            }
        }
        mutualWorkDAO.delete(idsToDelete);

        return true;
    }

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
            mutualWorkMap.put("mutual_work_id", mutualWorkData.getMutualWorkId());
            mutualWorkMap.put("division_owner_id", mutualWorkData.getDivision_owner_id());
            mutualWorkMap.put("division_owner_name", mutualWorkData.getDivision_owner_name());
            mutualWorkMap.put("project_id", mutualWorkData.getProject_id());
            mutualWorkMap.put("project_name", mutualWorkData.getProject_name());
            mutualWorkMap.put("project_type_id", mutualWorkData.getProject_type_id());
            mutualWorkMap.put("project_type_name", mutualWorkData.getProject_type_name());
            mutualWorkMap.put("employee_id", mutualWorkData.getEmployee_id());
            mutualWorkMap.put("employee_name", mutualWorkData.getEmployee_name());
            mutualWorkMap.put("division_employee_id", mutualWorkData.getDivision_employee_id());
            mutualWorkMap.put("division_employee_name", mutualWorkData.getDivision_employee_name());
            mutualWorkMap.put("region_id", mutualWorkData.getRegion_id());
            mutualWorkMap.put("region_name", mutualWorkData.getRegion_name());
            mutualWorkMap.put("work_days", mutualWorkData.getWork_days());
            mutualWorkMap.put("overtimes", mutualWorkData.getOvertimes());
            mutualWorkMap.put("coefficient", mutualWorkData.getCoefficient());
            mutualWorkMap.put("coefficient_calculated", mutualWorkData.getCoefficient_calculated());
            mutualWorkMap.put("work_days_calculated", mutualWorkData.getWork_days_calculated());
            mutualWorkMap.put("overtimes_calculated", mutualWorkData.getOvertimes_calculated());
            mutualWorkMap.put("comment", mutualWorkData.getComment());
            mutualWorkList.add(mutualWorkMap);
        }

        return mapper.writeValueAsString(mutualWorkList);
    }

    public boolean saveMutualWorkTable(int year, int month, int divisionOwner, String jsonData) throws IOException {
        logger.debug("Старт сохранения таблицы 'Взаимная занятость'");
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> mutualWorks = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, Object> mutualWorkMap : mutualWorks) {
            Project project = projectDAO.find((Integer) mutualWorkMap.get("project_id"));
            Employee employee = employeeDAO.find((Integer)mutualWorkMap.get("employee_id"));
            MutualWork mutualWork = mutualWorkDAO.findOrCreateMutualWork(employee, project, year, month, divisionOwner);

            mutualWork.setWork_days(     NumberUtils.getDoubleValue(mutualWorkMap.get("work_days")));
            mutualWork.setOvertimes(NumberUtils.getDoubleValue(mutualWorkMap.get("overtimes")));
            mutualWork.setCoefficient(NumberUtils.getDoubleValue(mutualWorkMap.get("coefficient")));
            mutualWork.setComment((String) mutualWorkMap.get("comment"));
            logger.debug("Сохранение записи в таблицу mutual_work: " + mutualWork.toString());
            mutualWorkDAO.save(mutualWork);
        }
        logger.debug("Завершение сохранения таблицы 'Взаимная занятость'");
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
