package com.aplana.timesheet.service;

import com.aplana.timesheet.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.EmployeeReportDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.EmployeePlanType;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.entity.EmployeeMonthReportDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Double workDurationPlan = calendarService.getEmployeeRegionWorkDaysCount(employee, year, month) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate();
        /* Итогошные значения */
        Double sumPlanH = Double.valueOf(0);
        Double sumFactH = Double.valueOf(0);
        for (Object[] item : detailList) {
            DictionaryItem dictionaryItem = (DictionaryItem) item[0];
            /* подменим активность "проектный пресейл" -> "проектом" */
            if (dictionaryItem.getId().equals(TypesOfActivityEnum.PROJECT_PRESALE.getId())) {
                dictionaryItem = dictionaryItemService.find(TypesOfActivityEnum.PROJECT.getId());
            }
            Project project = (Project) item[1];
            Double workPlanH = Double.valueOf(0);

            /* считаем плановое кол-во часов (для непроектной свой расчёт) */
            if (dictionaryItem.getId() == EmployeePlanType.NON_PROJECT.getId()) {
                EmployeePlan employeePlan = employeePlanService.tryFind(employee, year, month, dictionaryItem);
                if (employeePlan != null) {
                    workPlanH = employeePlan.getValue() * employee.getJobRate();
                }
            } else {
                EmployeeProjectPlan projectPlan = employeeProjectPlanService.tryFind(employee, year, month, project);
                if (projectPlan != null) {
                    workPlanH = projectPlan.getValue() * employee.getJobRate();
                }
            }
            Double workFactH = (Double) item[2];
            /* складываем в Итого */
            sumPlanH += workPlanH;
            sumFactH += workFactH;
            result.add(new EmployeeMonthReportDetail(dictionaryItem, project, workPlanH, workFactH, workDurationPlan));
        }

        /* проверяем отпуска */
        Double vacationPlanH = Double.valueOf(0);
        Double vacationFactH = vacationService.getVacationsWorkdaysCount(employee, year, month, null) * TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate();
        DictionaryItem vacationDic = dictionaryItemService.find(EmployeePlanType.VACATION.getId());
        EmployeePlan vacationPlan = employeePlanService.tryFind(employee, year, month, vacationDic);
        if (vacationPlan != null) {
            vacationPlanH = vacationPlan.getValue() * employee.getJobRate();
        }
        /* фильтруем пустую строку */
        if (vacationPlanH != 0 || vacationFactH != 0) {
            sumPlanH += vacationPlanH;
            sumFactH += vacationFactH;
            result.add(new EmployeeMonthReportDetail(vacationDic, new Project(), vacationPlanH, vacationFactH, workDurationPlan));
        }

        /* проверим болезни */
        Double illnessPlanH = Double.valueOf(0);
        Double illnessFactH = illnessService.getIllnessWorkdaysCount(employee, year, month)* TimeSheetConstants.WORK_DAY_DURATION * employee.getJobRate();
        DictionaryItem illnessDic = dictionaryItemService.find(EmployeePlanType.ILLNESS.getId());
        EmployeePlan illnessPlan = employeePlanService.tryFind(employee, year, month, illnessDic);
        if (illnessPlan != null) {
            illnessPlanH = illnessPlan.getValue() * employee.getJobRate();
        }
        /* фильтруем пустую строку */
        if (illnessPlanH != 0 || illnessFactH != 0) {
            sumPlanH += illnessPlanH;
            sumFactH += illnessFactH;
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
