package com.aplana.timesheet.service;

import com.aplana.timesheet.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.EmployeeReportDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.EmployeePlanType;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.entity.EmployeeMonthReportDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;


/* класс для работы с табличкой детализации работ по проекта в месяц */
@Service
public class EmployeeReportService {

    @Autowired
    private EmployeeReportDAO employeeMonthReportDAO;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    private EmployeePlanService employeePlanService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private VacationService vacationService;

    @Autowired
    private IllnessService illnessService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    public List<EmployeeMonthReportDetail> getMonthReport(Integer employee_id, Integer year, Integer month) {
        Employee employee = employeeService.find(employee_id);
        List<Object[]> detailList = employeeMonthReportDAO.getEmployeeMonthData(employee_id, year, month);
        List<EmployeeMonthReportDetail> result = new ArrayList<EmployeeMonthReportDetail>();
        /* кол-во часов по плану в месяц */
        BigDecimal workDurationPlan = new BigDecimal(calendarService.getEmployeeRegionWorkDaysCount(employee, year, month) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate(), MathContext.DECIMAL64);
        /* Итогошные значения */
        BigDecimal sumPlanH = BigDecimal.ZERO;
        BigDecimal sumFactH = BigDecimal.ZERO;
        for (Object[] item : detailList) {
            DictionaryItem dictionaryItem = (DictionaryItem) item[0];
            /* подменим активность "проектный пресейл" -> "проектом" */
            if (dictionaryItem.getId().equals(TypesOfActivityEnum.PROJECT_PRESALE.getId())) {
                dictionaryItem = dictionaryItemService.find(TypesOfActivityEnum.PROJECT.getId());
            }
            Project project = (Project) item[1];
            BigDecimal workPlanH = BigDecimal.ZERO;

            /* считаем плановое кол-во часов (для непроектной свой расчёт) */
            if (dictionaryItem.getId() == EmployeePlanType.NON_PROJECT.getId()) {
                EmployeePlan employeePlan = employeePlanService.tryFind(employee, year, month, dictionaryItem);
                if (employeePlan != null) {
                    workPlanH = BigDecimal.valueOf(employeePlan.getValue() * employee.getJobRate());
                }
            } else {
                EmployeeProjectPlan projectPlan = employeeProjectPlanService.tryFind(employee, year, month, project);
                if (projectPlan != null) {
                    workPlanH = BigDecimal.valueOf(projectPlan.getValue() * employee.getJobRate());
                }
            }
            BigDecimal workFactH = new BigDecimal((Double) item[2], MathContext.DECIMAL64);
            /* складываем в Итого */
            sumPlanH = sumPlanH.add(workPlanH);
            sumFactH = sumFactH.add(workFactH);
            result.add(new EmployeeMonthReportDetail(dictionaryItem, project, workPlanH, workFactH, workDurationPlan));
        }

        /* проверяем отпуска */
        BigDecimal vacationPlanH = BigDecimal.ZERO;
        BigDecimal vacationFactH = new BigDecimal(vacationService.getVacationsWorkdaysCount(employee, year, month, null) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate(), MathContext.DECIMAL64);
        DictionaryItem vacationDic = dictionaryItemService.find(EmployeePlanType.VACATION.getId());
        EmployeePlan vacationPlan = employeePlanService.tryFind(employee, year, month, vacationDic);
        if (vacationPlan != null) {
            vacationPlanH = BigDecimal.valueOf(vacationPlan.getValue() * employee.getJobRate());
        }
        /* фильтруем пустую строку */
        if (!vacationPlanH.equals(BigDecimal.ZERO) || !vacationFactH.equals(BigDecimal.ZERO)) {
            sumPlanH = sumPlanH.add(vacationPlanH);
            sumFactH = sumFactH.add(vacationFactH);
            result.add(new EmployeeMonthReportDetail(vacationDic, new Project(), vacationPlanH, vacationFactH, workDurationPlan));
        }

        /* проверим болезни */
        BigDecimal illnessPlanH = BigDecimal.ZERO;
        BigDecimal illnessFactH = new BigDecimal(illnessService.getIllnessWorkdaysCount(employee, year, month)* TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate(), MathContext.DECIMAL64);
        DictionaryItem illnessDic = dictionaryItemService.find(EmployeePlanType.ILLNESS.getId());
        EmployeePlan illnessPlan = employeePlanService.tryFind(employee, year, month, illnessDic);
        if (illnessPlan != null) {
            illnessPlanH = BigDecimal.valueOf(illnessPlan.getValue() * employee.getJobRate());
        }
        /* фильтруем пустую строку */
        if (!illnessPlanH.equals(BigDecimal.ZERO) || !illnessFactH.equals(BigDecimal.ZERO)) {
            sumPlanH = sumPlanH.add(illnessPlanH);
            sumFactH = sumFactH.add(illnessFactH);
            result.add(new EmployeeMonthReportDetail(illnessDic, new Project(), illnessPlanH, illnessFactH, workDurationPlan));
        }

        /* считаем итоговую строку */
        if (result.size() != 0) {
            DictionaryItem itogoDI = new DictionaryItem();
            itogoDI.setValue("Итого");
            Project itogoP = new Project();
            result.add(new EmployeeMonthReportDetail(itogoDI, itogoP, sumPlanH, sumFactH, workDurationPlan));
        }

        return result;
    }
}