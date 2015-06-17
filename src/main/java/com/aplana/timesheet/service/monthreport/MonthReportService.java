package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.monthreport.MonthReport;
import com.aplana.timesheet.dao.entity.monthreport.MonthReportData;
import com.aplana.timesheet.dao.entity.monthreport.MonthReportDetail;
import com.aplana.timesheet.dao.monthreport.MonthReportDAO;
import com.aplana.timesheet.service.EmployeeService;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aplana.timesheet.util.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonthReportService {

    @Autowired
    private MonthReportDAO monthReportDAO;
    @Autowired
    private EmployeeDAO employeeDAO;
    @Autowired
    private EmployeeService employeeService;

    public String getMonthReportData(Employee currentUser,
            Integer division, Integer manager, String regions, String roles, Integer year, Integer month) throws IOException {
        List<MonthReportData> result;
        if (employeeService.isEmployeeAdmin(currentUser.getId())){
            result = monthReportDAO.getMonthReportData(
                    division, manager,
                    StringUtil.stringToList(regions),
                    StringUtil.stringToList(roles),
                    year, month, false);
        }else{
            result = monthReportDAO.getSingleMonthReportData(currentUser, year, month);
        }
        return getMonthReportDataJSON(result);
    }

    private String getMonthReportDataJSON(List<MonthReportData> monthReportData) throws IOException {
        if (monthReportData == null) {
            return "[]";
        }
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> monthReportDataList = new ArrayList<Map<String, Object>>(monthReportData.size());
        for (MonthReportData detail : monthReportData) {
            HashMap<String, Object> detailMap = new HashMap<String, Object>();
            detailMap.put("employeeId", detail.getEmployeeId());
            detailMap.put("employee",   detail.getEmployeeName());
            detailMap.put("divisionId", detail.getDivisionId());
            detailMap.put("division",   detail.getDivisionName());
            detailMap.put("regionId",   detail.getRegionId());
            detailMap.put("region",     detail.getRegionName());

            detailMap.put("ts_worked_calculated",               detail.getTs_worked_calculated());
            detailMap.put("ts_worked",                          detail.getTs_worked());
            detailMap.put("ts_vacation",                        detail.getTs_vacation());
            detailMap.put("ts_illness_calculated",              detail.getTs_illness_calculated());
            detailMap.put("ts_illness",                         detail.getTs_illness());
            detailMap.put("ts_all_paid",                        detail.getTs_all_paid());
            detailMap.put("ts_over_val_fin_comp_calculated",    detail.getTs_over_val_fin_comp_calculated());
            detailMap.put("ts_over_val_fin_comp",               detail.getTs_over_val_fin_comp());
            detailMap.put("ts_over_accounted",                  detail.getTs_all_over_accounted());
            detailMap.put("ts_premium",                         detail.getTs_premium());
            detailMap.put("ts_all_over_accounted",              detail.getTs_all_over_accounted());
            detailMap.put("ts_over_done",                       detail.getTs_over_done());
            detailMap.put("ts_over_not_done",                   detail.getTs_over_not_done());
            detailMap.put("ts_over_remain",                     detail.getTs_over_remain());
            detailMap.put("ts_vacation_avail",                  detail.getTs_vacation_avail());

            detailMap.put("calc_worked_plan",       detail.getCalc_worked_plan());
            detailMap.put("calc_worked_fact",       detail.getCalc_worked_fact());
            detailMap.put("calc_vacation",          detail.getCalc_vacation());
            detailMap.put("calc_vacation_with",     detail.getCalc_vacation_with());
            detailMap.put("calc_vacation_without",  detail.getCalc_vacation_without());
            detailMap.put("calc_vacation_hol_paid", detail.getCalc_vacation_hol_paid());
            detailMap.put("calc_illness",           detail.getCalc_illness());
            detailMap.put("calc_illness_with",      detail.getCalc_illness_with());
            detailMap.put("calc_illness_without",   detail.getCalc_illness_without());
            detailMap.put("calc_over",              detail.getCalc_over());
            detailMap.put("calc_over_hol",          detail.getCalc_over_hol());
            detailMap.put("calc_over_hol_paid",     detail.getCalc_over_hol_paid());
            detailMap.put("calc_over_work",         detail.getCalc_over_work());
            detailMap.put("calc_worked_ill",        detail.getCalc_worked_ill());
            detailMap.put("calc_worked_vac",        detail.getCalc_worked_vac());

            monthReportDataList.add(detailMap);
        }

        return mapper.writeValueAsString(monthReportDataList);
    }

    public boolean saveMonthReportTable(int year, int month, String jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> monthReportData = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, Object> monthReportMap : monthReportData){
            Integer division = (Integer)monthReportMap.get("divisionId");
            MonthReport monthReport = monthReportDAO.findOrCreateMonthReport(year, month, division);
            Employee employee = employeeDAO.find((Integer)monthReportMap.get("employeeId"));

            MonthReportDetail monthReportDetail = monthReportDAO.findOrCreateMonthReportDetail(monthReport, employee);
            monthReportDetail.setMonthReport(monthReport);
            monthReportDetail.setEmployee(employee);
            // ToDo упростить
            monthReportDetail.setTsWorked(
                    (monthReportMap.get("ts_worked") instanceof Integer) ? new Double((Integer)monthReportMap.get("ts_worked")) : (Double)monthReportMap.get("ts_worked")
            );
            monthReportDetail.setTsIllness(
                    (monthReportMap.get("ts_illness") instanceof Integer) ? new Double((Integer)monthReportMap.get("ts_illness")) : (Double)monthReportMap.get("ts_illness")
            );
            monthReportDetail.setTsOverValFinComp(
                    (monthReportMap.get("ts_over_val_fin_comp") instanceof Integer) ? new Double((Integer) monthReportMap.get("ts_over_val_fin_comp")) : (Double) monthReportMap.get("ts_over_val_fin_comp")
            );
            monthReportDetail.setTsVacationAvail(
                    (monthReportMap.get("ts_vacation_avail") instanceof Integer) ? new Double((Integer)monthReportMap.get("ts_vacation_avail")) : (Double)monthReportMap.get("ts_vacation_avail")
            );

            monthReportDAO.save(monthReportDetail);
        }

        return true;
    }

}
