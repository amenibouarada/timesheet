package com.aplana.timesheet.service.json;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.util.JsonUtil;

import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;

/**
 * Класс для построения json-объектов
 */
public class EmployeeJSONBuilder {

    public static final String EMPLOYEE_ID          = "employee_id";
    public static final String EMPLOYEE_NAME        = "employee_name";
    public static final String DIVISION             = "division";
    public static final String DIVISION_ID          = "divisionId";
    public static final String DIVISION_NAME        = "divisionName";
    public static final String REGION               = "region";
    public static final String REGION_ID            = "regionId";
    public static final String REGION_NAME          = "regionName";

    // ToDo возможно стоит добавить кэш, чтоб не формировать один и тот же объект несколько раз
    // ToDo и, возможно объединить методы
    public JsonObjectNodeBuilder getDivisionAsJSONBulder(Division division){
        JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder();
        objectNodeBuilder.withField(DIVISION_ID, aNumberBuilder(division.getId().toString()));
        objectNodeBuilder.withField(DIVISION_NAME, aStringBuilder(division.getName()));
        return objectNodeBuilder;
    }
    public JsonObjectNodeBuilder getRegionAsJSONBulder(Region region){
        JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder();
        objectNodeBuilder.withField(REGION_ID, aNumberBuilder(region.getId().toString()));
        objectNodeBuilder.withField(REGION_NAME, aStringBuilder(region.getName()));
        return objectNodeBuilder;
    }

    public JsonObjectNodeBuilder getEmployeeAsJSONBulder(Employee employee, Boolean additionalFields){
        JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder();
        objectNodeBuilder.withField(EMPLOYEE_ID, aNumberBuilder(employee.getId().toString()));
        objectNodeBuilder.withField(EMPLOYEE_NAME, aStringBuilder(employee.getName()));
        if (additionalFields != null && additionalFields == true){
            objectNodeBuilder.withField(DIVISION, getDivisionAsJSONBulder(employee.getDivision()));
            objectNodeBuilder.withField(REGION, getRegionAsJSONBulder(employee.getRegion()));
        }
        return objectNodeBuilder;
    }

    /**
     * Возвращает список сотрудников как json {id, name}
     * @param employeeList
     * @return
     */
    public String getEmployeeListAsJson(List<Employee> employeeList, Boolean additionalFields){
        JsonArrayNodeBuilder builder = anArrayBuilder();
        for(Employee employee : employeeList){
            builder.withElement(getEmployeeAsJSONBulder(employee, additionalFields));
        }
        return JsonUtil.format(builder.build());
    }
}
