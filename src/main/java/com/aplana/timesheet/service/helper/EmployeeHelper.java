package com.aplana.timesheet.service.helper;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.TimeSheetService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

import static argo.jdom.JsonNodeBuilders.anArrayBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static com.aplana.timesheet.util.DateTimeUtil.MAX_DATE;
import static com.aplana.timesheet.util.DateTimeUtil.dateToString;
import static com.aplana.timesheet.util.JsonUtil.aStringBuilder;

@Service
public class EmployeeHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeHelper.class);

    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String DIVISION_ID = "divId";
    private static final String REGION_ID = "regId";
    private static final String MANAGER_ID = "manId";
    private static final String PROJECTS = "projects";
    private static final String PROJECT_ID = "projectId";
    private static final String JOB_ID = "jobId";
    private static final String DIVISION_EMPLOYEES = "divEmps";
    private static final String DATE_BY_DEFAULT = "dateByDefault";
    private static final String FIRST_WORK_DATE = "firstWorkDate";
    private static final String LAST_WORK_DATE = "lastWorkDate";
    private static final String DEFAULT_MANAGER = "-1";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String ACTIVE_FLAG = "active";

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimeSheetService timeSheetService;

    @Transactional(readOnly = true)
    public String getEmployeeListWithLastWorkdayJson(List<Division> divisions, Boolean filterFired) {
        return getEmployeeListWithLastWorkdayJson(divisions, filterFired, false);
    }

    /* !!! медленная функция, ниже написаны ужатые функции */
    // ToDo разобраться почему функция медленная
    @Transactional(readOnly = true)
    public String getEmployeeListWithLastWorkdayJson(List<Division> divisions, Boolean filterFired, Boolean addDetails) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Division division : divisions) {
            final List<Employee> employees = employeeService.getEmployees(division, filterFired);
            Map<Integer, Date> lastWorkdays = new HashMap<Integer, Date>();
            if (addDetails) {
                lastWorkdays = timeSheetService.getLastWorkdayWithoutTimesheetMap(division);
            }
            final JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
            final JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();

            nodeBuilder.withField(DIVISION_ID, aStringBuilder(division.getId()));

            if (employees.isEmpty()) {
                employeesBuilder.withElement(
                        anObjectBuilder().
                                withField(ID, aStringBuilder(0)).
                                withField(VALUE, JsonNodeBuilders.aStringBuilder(StringUtils.EMPTY))
                );
            } else {
                for (Employee employee : employees) {
                    JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                            withField(ID, aStringBuilder(employee.getId())).
                            withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee))).
                            //добавил два поля из за того что на форме "командировки/болезни" все заточено под другую структуру данных
                            withField(MANAGER_ID, aStringBuilder(employee.getManager() == null ? null : employee.getManager().getId())).
                            withField(REGION_ID, aStringBuilder(employee.getRegion().getId()));
                    if (addDetails) {

                        Date defaultDate = lastWorkdays.get(employee.getId());
                        if (defaultDate == null)
                            defaultDate = employee.getStartDate();

                        Date curDate = new Date();
                        if (defaultDate.after(curDate)){
                            defaultDate = curDate;
                        }

                        if ((employee.getEndDate() != null && defaultDate.after(employee.getEndDate()))){
                            defaultDate = employee.getEndDate();
                        }

                        objectNodeBuilder.withField(JOB_ID, aStringBuilder(employee.getJob().getId())).
                                withField(DATE_BY_DEFAULT, JsonNodeBuilders.aStringBuilder(
                                        dateToString(defaultDate, DATE_FORMAT))).
                                withField(FIRST_WORK_DATE, JsonNodeBuilders.aStringBuilder(
                                        dateToString(employee.getStartDate(), DATE_FORMAT))).
                                withField(LAST_WORK_DATE, JsonNodeBuilders.aStringBuilder(
                                        employee.getEndDate() != null ? dateToString(employee.getEndDate(), DATE_FORMAT) : ""
                                ));
                    }
                    employeesBuilder.withElement(objectNodeBuilder);
                }
            }
            builder.withElement(nodeBuilder.withField(DIVISION_EMPLOYEES, employeesBuilder));
        }
        return JsonUtil.format(builder.build());
    }

    @Transactional(readOnly = true)
    public String getEmployeeListWithDivisionAndManagerAndRegionJson(List<Division> divisions, Boolean filterFired) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Division division : divisions) {
            final List<Employee> employees = employeeService.getEmployees(division, filterFired);
            final JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
            final JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();

            nodeBuilder.withField(DIVISION_ID, aStringBuilder(division.getId()));

            if (employees.isEmpty()) {
                employeesBuilder.withElement(
                        anObjectBuilder().
                                withField(ID, aStringBuilder(0)).
                                withField(VALUE, JsonNodeBuilders.aStringBuilder(StringUtils.EMPTY))
                );
            } else {
                for (Employee employee : employees) {
                    JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                            withField(ID, aStringBuilder(employee.getId())).
                            withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee))).
                            withField(MANAGER_ID, aStringBuilder(employee.getManager() == null ? null : employee.getManager().getId())).
                            withField(REGION_ID, aStringBuilder(employee.getRegion().getId()));
                    employeesBuilder.withElement(objectNodeBuilder);
                }
            }
            builder.withElement(nodeBuilder.withField(DIVISION_EMPLOYEES, employeesBuilder));
        }
        return JsonUtil.format(builder.build());
    }

    // ToDo удалить и внести в предыдущий метод, убрать копипасту
    @Transactional(readOnly = true)
    public String getEmployeeListWithDivisionAndRegionAndActiveFlagJson(List<Division> divisions, Date reportDate) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Division division : divisions) {
            final List<Employee> employees = employeeService.getEmployees(division, true);
            final JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
            final JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();

            nodeBuilder.withField(DIVISION_ID, aStringBuilder(division.getId()));

            if (employees.isEmpty()) {
                employeesBuilder.withElement(
                        anObjectBuilder().
                                withField(ID, aStringBuilder(0)).
                                withField(VALUE, JsonNodeBuilders.aStringBuilder(StringUtils.EMPTY))
                );
            } else {
                for (Employee employee : employees) {
                    JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                            withField(ID, aStringBuilder(employee.getId())).
                            withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee))).
                            withField(REGION_ID, aStringBuilder(employee.getRegion().getId())).
                            withField(ACTIVE_FLAG, aStringBuilder(isEmployeeActive(employee, reportDate)));

                    employeesBuilder.withElement(objectNodeBuilder);
                }
            }
            builder.withElement(nodeBuilder.withField(DIVISION_EMPLOYEES, employeesBuilder));
        }
        return JsonUtil.format(builder.build());
    }

    public Boolean isEmployeeActive(Employee employee, Date date) {
        if (employee != null) {
            Date beginDate = employee.getStartDate();
            Date curDate = (date != null) ? date : new Date();
            /* определим дату окончания работы
            *  если её нет то считаем бесконечно большой
            *  если есть то добавим день чтоб учесть последий рабочий день */
            Date endDate = (employee.getEndDate() != null) ?
                    DateUtils.addDays(employee.getEndDate(), 1) :
                    DateTimeUtil.parseStringToDateForDB(MAX_DATE);
            if (curDate.after(beginDate) && curDate.before(endDate)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    // ToDo эту копипасту тоже убрать
    @Transactional(readOnly = true)
    public String getEmployeeListWithDivisionJson(List<Division> divisions, Boolean filterFired) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Division division : divisions) {
            final List<Employee> employees = employeeService.getEmployees(division, filterFired);
            final JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
            final JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();

            nodeBuilder.withField(DIVISION_ID, aStringBuilder(division.getId()));

            if (employees.isEmpty()) {
                employeesBuilder.withElement(
                        anObjectBuilder().
                                withField(ID, aStringBuilder(0)).
                                withField(VALUE, JsonNodeBuilders.aStringBuilder(StringUtils.EMPTY))
                );
            } else {
                for (Employee employee : employees) {
                    JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                            withField(ID, aStringBuilder(employee.getId())).
                            withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee)));
                    employeesBuilder.withElement(objectNodeBuilder);
                }
            }
            builder.withElement(nodeBuilder.withField(DIVISION_EMPLOYEES, employeesBuilder));
        }
        return JsonUtil.format(builder.build());
    }

    // преобразует список сотрудников в JSON
    public String makeEmployeeListInJSON(List<Employee> employees){
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Employee employee : employees) {
            JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                    withField(ID, aStringBuilder(employee.getId())).
                    withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee)));
            builder.withElement(objectNodeBuilder);
        }
        return JsonUtil.format(builder.build());
    }

    @Transactional(readOnly = true)
    public String getManagerListJson(){
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        List<Employee> managerList = employeeService.getManagerListForAllEmployee();
        for (Employee e : managerList){
            JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
            nodeBuilder.withField(ID, aStringBuilder(e.getId()));
            nodeBuilder.withField(VALUE, JsonNodeBuilders.aStringBuilder(e.getName()));
            nodeBuilder.withField(DIVISION_ID, aStringBuilder(e.getDivision().getId()));
            builder.withElement(nodeBuilder);
        }
        return JsonUtil.format(builder.build());
    }


    private String getValue(Employee employee) {
        final StringBuilder sb = new StringBuilder(employee.getName());
        Timestamp endDate =  employee.getEndDate();

        if (null != endDate) {
            if (DateUtils.truncatedCompareTo(endDate, new Date(), Calendar.DAY_OF_MONTH) < 0) {
                sb.append(" (уволен: ").append(dateToString(employee.getEndDate(), DATE_FORMAT)).append(")");
            }
        }

        return sb.toString();
    }

}
