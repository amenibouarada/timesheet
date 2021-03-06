package com.aplana.timesheet.dao.monthreport;


import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.monthreport.Overtime;
import com.aplana.timesheet.dao.entity.monthreport.OvertimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class OvertimeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(OvertimeDAO.class);

    @Transactional
    public void save(Overtime overtime)  {
        entityManager.merge(overtime);
        entityManager.flush();
        logger.debug("Flushed overtime object id = {}", overtime.getId());
    }

    @Transactional
    // ToDo переделать на один запрос
    public void delete(List<Integer> ids)  {
        for (Integer id : ids){
            entityManager.remove(entityManager.find(Overtime.class, id));
        }
        entityManager.flush();
    }

    public List<OvertimeData> getOvertimes(int year, int month, Integer divisionOwner, Integer divisionEmployee, boolean typeListObject){
        String queryString = typeListObject ?
                "SELECT * FROM overtime_data where year = :year and month = :month" :
                "FROM OvertimeData WHERE year = :year AND month = :month ";
        boolean ownerDivSet = false;
        boolean employeeDivSet = false;
        if (divisionOwner != null && divisionOwner > 0){
            queryString += " AND division_owner_id = :divisionOwner ";
            ownerDivSet = true;
        }
        if (divisionEmployee != null && divisionEmployee > 0){
            queryString += " AND division_employee_id = :divisionEmployee ";
            employeeDivSet = true;
        }
        queryString +=  " ORDER BY employee_name";
        Query query = typeListObject ?
                entityManager.createNativeQuery(queryString).setParameter("year", year).setParameter("month", month) :
                entityManager.createQuery(queryString).setParameter("year", year).setParameter("month", month);
        if (ownerDivSet) { query.setParameter("divisionOwner", divisionOwner); }
        if (employeeDivSet) { query.setParameter("divisionEmployee", divisionEmployee); }
        List<OvertimeData> result = query.getResultList();
        logger.debug("getOvertimes List<Overtime> result size = {}", result.size());
        return result;
    }

    public List<OvertimeData> getSingleOvertime(Employee currentUser, int year, int month) {
        Query query = entityManager.
                createQuery("FROM OvertimeData WHERE year = :year AND month = :month AND employee_id = :employee_id")
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("employee_id", currentUser.getId());
        List<OvertimeData> result = query.getResultList();
        logger.debug("getSingleOvertime List<Overtime> result size = {}", result.size());
        return result;
    }

    @Transactional
    public Overtime findOrCreateOvertime(Employee employee, Project project, Integer year, Integer month, Integer divisionOwner) {
        String queryString = "FROM Overtime WHERE employee = :employee AND year = :year AND month = :month";
        boolean projectSet = false;
        if (project != null) {
            queryString += " AND project = :project";
            projectSet = true;
           // Если выбрана непроектная занятость
        } else {
            queryString += " AND project is null";
        }
        Query query = entityManager.createQuery(queryString).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);
        if (projectSet) {query.setParameter("project", project);}
        List result = query.getResultList();
        if (result.size() == 0) { // создадим новый
            Overtime newOvertime = new Overtime();
            newOvertime.setEmployee(employee);
            newOvertime.setProject(project);
            newOvertime.setYear(year);
            newOvertime.setMonth(month);
            newOvertime.setDivision_owner_id(divisionOwner);
            entityManager.persist(newOvertime);
            logger.debug("findOrCreateOvertime Overtime id = {}", newOvertime.getId());
            logger.info("findOrCreateOvertime created newOvertime");
            return newOvertime;
        } else {
            logger.debug("findOrCreateOvertime List<Overtime> result size = {}", result.size());
            return (Overtime) result.get(0);
        }
    }

    public List<Overtime> getOvertimesForCloseOperation(int year, int month) {
        Query query = entityManager.createQuery("SELECT NEW Overtime(em AS employee, p AS project, " +
                                                "COALESCE(otd.overtime, otd.overtime_calculated) AS overtime, " +
                                                "COALESCE(otd.fin_compensated_overtime, otd.fin_compensated_overtime_calculated) AS fin_compensated_overtime) " +
                                                "FROM OvertimeData otd, Employee em, Project p " +
                                                "WHERE otd.year = :year AND otd.month = :month AND em.id = otd.employee_id AND p.id = otd.project_id")
                .setParameter("year", year)
                .setParameter("month", month);
        return query.getResultList();
    }
}
