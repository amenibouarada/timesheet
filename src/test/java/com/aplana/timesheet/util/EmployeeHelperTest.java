package com.aplana.timesheet.util;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.helper.EmployeeHelper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class EmployeeHelperTest extends AbstractJsonTest {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeHelperTest.class);

    @Autowired
    private DivisionDAO divisionDAO;

    @Autowired
    private EmployeeHelper employeeHelper;

    @Autowired
    private EmployeeService employeeService;

    final Boolean filterFired = Boolean.FALSE;
    private Employee employee;
    private Map<String, String> employeeFromJson;
    private List<Division> divisions;

    @Before
    public void setUp() throws Exception {
        divisions = divisionDAO.getActiveDivisions();
        employee = getRandomEmployee();
        employeeFromJson = getEmployeeFromJsonList(employee.getId(), employee.getDivision().getId());
    }

    private Employee getRandomEmployee() {
        Random rn = new Random();
        int rnDivision = rn.nextInt(divisions.size());
        List<Employee> employees = employeeService.getEmployees(divisions.get(rnDivision), filterFired);
        int rnEmployee = rn.nextInt(employees.size());
        Employee employee = employees.get(rnEmployee);

        return employee;
    }

    private static final JdomParser JDOM_PARSER = new JdomParser();

    private Map<String, String> getEmployeeFromJsonList(int employee_id, int division_id) {
        String jsonText =""; //employeeHelper.getEmployeeListWithLastWorkdayJson(divisions, filterFired, true);
        try {
            JsonRootNode jsonn = JDOM_PARSER.parse(jsonText);
            List<JsonNode> divisionList= jsonn.getElements();
            for (JsonNode item : divisionList) {
                if ( item.getStringValue("divId").equals(String.valueOf(division_id)) ) {
                    List <JsonNode> employeeList = item.getArrayNode("divEmps");
                    for (JsonNode empl :employeeList) {
                        if ( empl.getStringValue("id").equals(String.valueOf(employee_id)) ) {
                            Map<String, String> result = new HashMap<String, String>();
                            result.put("id", empl.getStringValue("id"));
                            /* в имя в JSONе может быть добавлена строка увольнения */
                            String employeeName = empl.getStringValue("value");
                            if (employeeName.contains("(уволен:")) {
                                employeeName = (employeeName.substring(0, employeeName.indexOf("уволен:"))).trim();
                            }
                            result.put("name", employeeName);

                            result.put("manager", empl.getStringValue("manId"));
                            result.put("region", empl.getStringValue("regId"));
                            result.put("job", empl.getStringValue("jobId"));
                            result.put("startDate", empl.getStringValue("firstWorkDate"));
                            result.put("dateByDefault", empl.getStringValue("dateByDefault"));
                            return result;
                        }
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            logger.error("Parsing error", e);
        }
        return null;
    }

    @Test
    public void testGetEmployeeListJson() throws Exception {
        assertEmployeeFieldsEquals(employee, employeeFromJson);
    }
}
