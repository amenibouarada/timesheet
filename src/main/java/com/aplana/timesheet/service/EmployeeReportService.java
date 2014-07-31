package com.aplana.timesheet.service;

import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.EmployeeReportDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeePlan;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.enums.EmployeePlanType;
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

    @Autowired
    private ProjectService projectService;


    public List<EmployeeMonthReportDetail> getMonthReport(Integer employee_id, Integer year, Integer month) {
        Employee employee = employeeService.find(employee_id);
        List<Object[]> detailList = employeeMonthReportDAO.getEmployeeMonthData(employee_id, year, month);
        List<EmployeeMonthReportDetail> result = new ArrayList<EmployeeMonthReportDetail>();

        /* кол-во часов по плану в месяц */
        int employeeRegionWorkDaysCount = calendarService.getEmployeeRegionWorkDaysCount(employee, year, month);
        BigDecimal workDurationPlan = new BigDecimal(employeeRegionWorkDaysCount *
                TimeSheetConstants.WORK_DAY_DURATION *
                employee.getJobRate(),
                MathContext.DECIMAL64);

        /* Итогошные значения */
        BigDecimal sumPlanH = BigDecimal.ZERO;
        BigDecimal sumFactH = BigDecimal.ZERO;

        /* вычисляем итого факт (для вычисления процента по каждой задаче) */
        for (Object[] item : detailList) {
            sumFactH = sumFactH.add((BigDecimal) item[3]);
        }
        /* отпуска */
        int vacationWorkDaysCount = vacationService.getVacationsWorkdaysCount(employee, year, month, null);
        sumFactH = sumFactH.add(BigDecimal.valueOf(vacationWorkDaysCount * TimeSheetConstants.WORK_DAY_DURATION));
        /* болезни */
        int illnessWorkdaysCount = illnessService.getIllnessWorkdaysCount(employee, year, month);
        sumFactH = sumFactH.add(BigDecimal.valueOf(illnessWorkdaysCount * TimeSheetConstants.WORK_DAY_DURATION));

        /* идём по спроектам */
        for (Object[] item : detailList) {
            DictionaryItem actType = dictionaryItemService.find((Integer) item[0]);
            Project project = projectService.find((Integer) item[1]);
            BigDecimal workPlanH = new BigDecimal((Double) item[2], MathContext.DECIMAL64);
            BigDecimal workFactH = (BigDecimal) item[3];

            /* складываем в Итого план*/
            sumPlanH = sumPlanH.add(workPlanH);

            /* добавляем строку */
            result.add(new EmployeeMonthReportDetail(actType, project, workPlanH, workFactH, workDurationPlan, sumFactH));
        }

        /* проверяем отпуска */
        BigDecimal vacationPlanH = BigDecimal.ZERO;
        BigDecimal vacationFactH = new BigDecimal(vacationService.getVacationsWorkdaysCount(employee, year, month, null) * TimeSheetConstants.WORK_DAY_DURATION, MathContext.DECIMAL64);
        DictionaryItem vacationDic = dictionaryItemService.find(EmployeePlanType.VACATION.getId());
        EmployeePlan vacationPlan = employeePlanService.tryFind(employee, year, month, vacationDic);
        if (vacationPlan != null) {
            vacationPlanH = BigDecimal.valueOf(vacationPlan.getValue() * employee.getJobRate());
        }
        /* фильтруем пустую строку */
        if (!vacationPlanH.equals(BigDecimal.ZERO) || !vacationFactH.equals(BigDecimal.ZERO)) {
            sumPlanH = sumPlanH.add(vacationPlanH);
            result.add(new EmployeeMonthReportDetail(vacationDic, new Project(), vacationPlanH, vacationFactH, workDurationPlan, sumFactH));
        }

        /* проверим болезни */
        BigDecimal illnessPlanH = BigDecimal.ZERO;
        BigDecimal illnessFactH = new BigDecimal(illnessService.getIllnessWorkdaysCount(employee, year, month)* TimeSheetConstants.WORK_DAY_DURATION, MathContext.DECIMAL64);
        DictionaryItem illnessDic = dictionaryItemService.find(EmployeePlanType.ILLNESS.getId());
        EmployeePlan illnessPlan = employeePlanService.tryFind(employee, year, month, illnessDic);
        if (illnessPlan != null) {
            illnessPlanH = BigDecimal.valueOf(illnessPlan.getValue() * employee.getJobRate());
        }
        /* фильтруем пустую строку */
        if (!illnessPlanH.equals(BigDecimal.ZERO) || !illnessFactH.equals(BigDecimal.ZERO)) {
            sumPlanH = sumPlanH.add(illnessPlanH);
            result.add(new EmployeeMonthReportDetail(illnessDic, new Project(), illnessPlanH, illnessFactH, workDurationPlan, sumFactH));
        }

        /* считаем итоговую строку */
        if (result.size() != 0) {
            DictionaryItem itogoDI = new DictionaryItem();
            itogoDI.setValue(EmployeeMonthReportDetail.ITOGO);
            Project itogoP = new Project();
            result.add(new EmployeeMonthReportDetail(itogoDI, itogoP, sumPlanH, sumFactH, workDurationPlan, sumFactH));
        }

        return result;
    }
}