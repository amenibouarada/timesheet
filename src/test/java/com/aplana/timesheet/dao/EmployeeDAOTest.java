package com.aplana.timesheet.dao;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ProjectRole;
import com.aplana.timesheet.dao.entity.Region;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class EmployeeDAOTest extends AbstractTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    EmployeeDAO employeeDAO;

    private Employee expectedEmployee = new Employee();

    /* используем не самописные получение данных из базы */
    private Region getRegion(Integer regionId) {
        Query query = entityManager.createQuery(
                "from Region as r where r.id =:region_id"
        ).setParameter("region_id", regionId);

        return (Region) query.getResultList().get(0);
    }

    private Division getDivision(Integer divisionId) {
        Query query = entityManager.createQuery(
                "from Division as d where d.id =:division_id"
        ).setParameter("division_id", divisionId);

        return (Division) query.getResultList().get(0);
    }

    private ProjectRole getJob(Integer jobId) {
        Query query = entityManager.createQuery(
                "from ProjectRole as p where p.id =:job_id"
        ).setParameter("job_id", jobId);

        return (ProjectRole) query.getResultList().get(0);
    }

    private Employee getEmployee(Integer employeeId) {
        Query query = entityManager.createQuery(
                "from Employee as e where e.id =:employee_id"
        ).setParameter("employee_id", employeeId);

        return (Employee) query.getResultList().get(0);
    }


    private void fillTestEmployee(Employee empl) {
        empl.setId(1000001);
        empl.setEmail("test@test.com");
        empl.setName("Тестов Тестович");
        empl.setNotToSync(false);
        Date startD = new Date();
        try {
            startD = new SimpleDateFormat("yyyy-MM-dd").parse("2013-06-06");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp startDate = new java.sql.Timestamp(startD.getTime());
        empl.setStartDate(startDate);
        empl.setDivision(getDivision(1));
        empl.setJob(getJob(19));
        empl.setManager(getEmployee(1));
        empl.setRegion(getRegion(1));
        empl.setLdap("ldap name");
        empl.setEndDate(null);
        empl.setObjectSid("S-0-0-00-000000000-0000000000-0000000000-00000");
        empl.setJobRate(1f);
        empl.setManager2(getEmployee(1));
        empl.setBillable(true);
        empl.setJiraName("Ttest");
    }

    private void createTestEmployeeDB() throws Exception {
        fillTestEmployee(expectedEmployee);

        Query query = entityManager.createNativeQuery(
                "insert into employee (" +
                        " id," +
                        " email," +
                        " name," +
                        " not_to_sync," +
                        " start_date," +
                        " division," +
                        " job," +
                        " manager," +
                        " region," +
                        " ldap," +
                        " end_date," +
                        " ldap_object_sid," +
                        " job_rate," +
                        " manager2," +
                        " billable," +
                        " jira_name)" +
                        "values (" +
                        " :id," +
                        " :email," +
                        " :name," +
                        " :not_to_sync," +
                        " :start_date," +
                        " :division_id," +
                        " :job_id," +
                        " :manager_id," +
                        " :region_id," +
                        " :ldap," +
                        " :end_date," +
                        " :ldap_object_sid," +
                        " :job_rate," +
                        " :manager2_id," +
                        " :billable," +
                        " :jira_name)");
        query.setParameter("id", expectedEmployee.getId());
        query.setParameter("email", expectedEmployee.getEmail());
        query.setParameter("name", expectedEmployee.getName());
        query.setParameter("not_to_sync", expectedEmployee.isNotToSync());
        query.setParameter("start_date", expectedEmployee.getStartDate());
        query.setParameter("division_id", expectedEmployee.getDivision().getId());
        query.setParameter("job_id", expectedEmployee.getJob().getId());
        query.setParameter("manager_id", expectedEmployee.getManager().getId());
        query.setParameter("region_id", expectedEmployee.getRegion().getId());
        query.setParameter("ldap", expectedEmployee.getLdap());
        query.setParameter("end_date", expectedEmployee.getEndDate(), TemporalType.TIMESTAMP);
        query.setParameter("ldap_object_sid", expectedEmployee.getObjectSid());
        query.setParameter("job_rate", expectedEmployee.getJobRate());
        query.setParameter("manager2_id", expectedEmployee.getManager2().getId());
        query.setParameter("billable", expectedEmployee.isBillable());
        query.setParameter("jira_name", expectedEmployee.getJiraName());

        if (query.executeUpdate() != 1) {
            throw new Exception("Error in create test employee in DB");
        }
    }

    @Test
    public void testFind() throws Exception {
        createTestEmployeeDB();

        Employee result = employeeDAO.find(expectedEmployee.getId());

        assertNotNull(result);
        assertEquals(expectedEmployee.getId(), result.getId());
        assertEquals(expectedEmployee.getEmail(), result.getEmail());
        assertEquals(expectedEmployee.getName(), result.getName());
        assertEquals(expectedEmployee.isNotToSync(), result.isNotToSync());
        assertEquals(expectedEmployee.getStartDate(), result.getStartDate());
        assertEquals(expectedEmployee.getDivision(), result.getDivision());
        assertEquals(expectedEmployee.getJob(), result.getJob());
        assertEquals(expectedEmployee.getManager(), result.getManager());
        assertEquals(expectedEmployee.getRegion(), result.getRegion());
        assertEquals(expectedEmployee.getLdap(), result.getLdap());
        assertEquals(expectedEmployee.getEndDate(), result.getEndDate());
        assertEquals(expectedEmployee.getObjectSid(), result.getObjectSid());
        assertEquals(expectedEmployee.getJobRate(), result.getJobRate());
        assertEquals(expectedEmployee.getManager2(), result.getManager2());
        assertEquals(expectedEmployee.isBillable(), result.isBillable());
        assertEquals(expectedEmployee.getJiraName(), result.getJiraName());
    }

    @Test
    public void testSave() throws Exception {
        /* делаем копию т.к. при вызове save объект обновляется и сравнивать теряет смысл */
        Employee testedEmployee = new Employee();
        fillTestEmployee(testedEmployee);
        /* заполняем сотрудника на которого будем опираться */
        fillTestEmployee(expectedEmployee);

        /* выполняем тестируемый метод */
        Employee saveResult = employeeDAO.save(testedEmployee);

        /* получаем из базы по сгенерированному при сохранении id-шнику */
        Employee result = getEmployee(saveResult.getId());

        /* проверка по значениям */
        assertNotNull(saveResult);
        assertNotNull(result);
        /* id не сравниваем т.к. при save он присвоился автоматом */
        assertEquals(expectedEmployee.getEmail(), result.getEmail());
        assertEquals(expectedEmployee.getName(), result.getName());
        assertEquals(expectedEmployee.isNotToSync(), result.isNotToSync());
        assertEquals(expectedEmployee.getStartDate(), result.getStartDate());
        assertEquals(expectedEmployee.getDivision(), result.getDivision());
        assertEquals(expectedEmployee.getJob(), result.getJob());
        assertEquals(expectedEmployee.getManager(), result.getManager());
        assertEquals(expectedEmployee.getRegion(), result.getRegion());
        assertEquals(expectedEmployee.getLdap(), result.getLdap());
        assertEquals(expectedEmployee.getEndDate(), result.getEndDate());
        assertEquals(expectedEmployee.getObjectSid(), result.getObjectSid());
        assertEquals(expectedEmployee.getJobRate(), result.getJobRate());
        assertEquals(expectedEmployee.getManager2(), result.getManager2());
        assertEquals(expectedEmployee.isBillable(), result.isBillable());
        assertEquals(expectedEmployee.getJiraName(), result.getJiraName());
    }
}
