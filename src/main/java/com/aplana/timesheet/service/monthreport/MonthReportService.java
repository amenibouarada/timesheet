package com.aplana.timesheet.service.monthreport;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.monthreport.MonthReport;
import com.aplana.timesheet.dao.entity.monthreport.MonthReportData;
import com.aplana.timesheet.dao.entity.monthreport.MonthReportDetail;
import com.aplana.timesheet.dao.monthreport.MonthReportDAO;
import com.aplana.timesheet.enums.MonthReportStatusEnum;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.util.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MonthReportService.class);

    @Autowired
    private MonthReportDAO monthReportDAO;
    @Autowired
    private EmployeeDAO employeeDAO;
    @Autowired
    private EmployeeService employeeService;

    public String getMonthReportData(Employee currentUser,
            Integer division, Integer manager, String regions, String roles, Integer year, Integer month) throws IOException {
        List<MonthReportData> result;
        if (employeeService.isEmployeeHasPermissionsToMonthReportManage(currentUser)){
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
            detailMap.put("employeeId",                           detail.getEmployeeId());
            detailMap.put("employee",                             detail.getEmployeeName());
            detailMap.put("divisionId",                           detail.getDivisionId());
            detailMap.put("division",                             detail.getDivisionName());
            detailMap.put("regionId",                             detail.getRegionId());
            detailMap.put("region",                               detail.getRegionName());

            detailMap.put("ts_worked_calculated",                 detail.getTs_worked_calculated());
            detailMap.put("ts_worked",                            detail.getTs_worked());
            detailMap.put("overtimes_paid_current",               detail.getOvertimes_paid_current());
            detailMap.put("overtimes_paid_previous",              detail.getOvertimes_paid_previous());
            detailMap.put("calc_vacation_with",                   detail.getCalc_vacation_with());
            detailMap.put("calc_vacation_without",                detail.getCalc_vacation_without());
            detailMap.put("calc_vacation_hol_paid",               detail.getCalc_vacation_hol_paid());
            detailMap.put("ts_illness_calculated",                detail.getTs_illness_calculated());
            detailMap.put("ts_illness",                           detail.getTs_illness());
            detailMap.put("ts_all_paid",                          detail.getTs_all_paid());
            detailMap.put("ts_all_over_accounted",                detail.getTs_all_over_accounted());

            detailMap.put("ts_vacation_avail",                    detail.getTs_vacation_avail());
            detailMap.put("ts_over_remain_calculated",            detail.getTs_over_remain_calculated());
            detailMap.put("ts_over_remain",                       detail.getTs_over_remain());
            detailMap.put("calc_worked_vac",                      detail.getCalc_worked_vac());
            detailMap.put("ts_vacation",                          detail.getTs_vacation());
            detailMap.put("overtimes_acc_current",                detail.getOvertimes_acc_current());

            detailMap.put("calc_illness",                         detail.getCalc_illness());
            detailMap.put("calc_illness_with",                    detail.getCalc_illness_with());
            detailMap.put("calc_illness_without",                 detail.getCalc_illness_without());
            detailMap.put("calc_worked_ill",                      detail.getCalc_worked_ill());

            detailMap.put("calc_worked_plan",                     detail.getCalc_worked_plan());
            detailMap.put("calc_worked_fact",                     detail.getCalc_worked_fact());

            monthReportDataList.add(detailMap);
        }

        return mapper.writeValueAsString(monthReportDataList);
    }

    public boolean saveMonthReportTable(int year, int month, String jsonData) throws IOException {
        logger.debug("Старт сохранения таблицы 'Табель'");
        ObjectMapper mapper = new ObjectMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);

        List<Map<String, Object>> monthReportData = mapper.readValue(jsonData, mapCollectionType);

        for (Map<String, Object> monthReportMap : monthReportData){
            MonthReport monthReport = monthReportDAO.findOrCreateMonthReport(year, month);
            Employee employee = employeeDAO.find((Integer)monthReportMap.get("employeeId"));

            MonthReportDetail monthReportDetail = monthReportDAO.findOrCreateMonthReportDetail(monthReport, employee);
            monthReportDetail.setMonthReport(monthReport);
            monthReportDetail.setEmployee(employee);
            monthReportDetail.setTsWorked(               NumberUtils.getDoubleValue(monthReportMap.get("ts_worked")));
            monthReportDetail.setOvertimesPaidPrevious(  NumberUtils.getDoubleValue(monthReportMap.get("overtimes_paid_previous")));
            monthReportDetail.setTsIllness(              NumberUtils.getDoubleValue(monthReportMap.get("ts_illness")));
            monthReportDetail.setTsVacationAvail(        NumberUtils.getDoubleValue(monthReportMap.get("ts_vacation_avail")));
            monthReportDetail.setTsOverRemain(           NumberUtils.getDoubleValue(monthReportMap.get("ts_over_remain")));

            logger.debug("Сохранение записи в таблицу month_report_detail: " + monthReportDetail.toString());
            monthReportDAO.save(monthReportDetail);
        }

        logger.debug("Завершение сохранения таблицы 'Табель'");
        return true;
    }

    public MonthReportStatusEnum getMonthReportStatus(int year, int month){
        Integer status = monthReportDAO.getMonthReportStatus(year, month);
        return status == null ? MonthReportStatusEnum.NOT_CREATED : MonthReportStatusEnum.getById(status);
    }

    public boolean closeMonthReport(Integer year, Integer month) {
        return monthReportDAO.setMonthReportStatus(year, month, MonthReportStatusEnum.CLOSED.getId());
    }

    public boolean openMonthReport(Integer year, Integer month) {
        return monthReportDAO.setMonthReportStatus(year, month, MonthReportStatusEnum.OPEN.getId());
    }

    public String getMonthReportStatusesForYear(Integer year) throws IOException {
        List<Object> result = monthReportDAO.getMonthReportStatusesForYear(year);
        return new ObjectMapper().writeValueAsString(result);
    }

    public String getLastEnableYearAndMonth() throws IOException {
        List<Object> result = monthReportDAO.getLastEnableYearAndMonth();
        return new ObjectMapper().writeValueAsString(result);
    }
}
