package com.aplana.timesheet.dao.monthreport;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.monthreport.MutualWork;
import com.aplana.timesheet.dao.entity.monthreport.MutualWorkData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by AAfanasyev on 17.06.2015.
 */

@Repository
public class MutualWorkDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(MutualWorkDAO.class);

    @Transactional
    public void save(MutualWork mutualWork) {
        entityManager.merge(mutualWork);
        entityManager.flush();
        logger.debug("Flushed mutualWork object id = {}", mutualWork.getId());
    }

    @Transactional
    public void delete(List<Integer> ids)  {
        for (Integer id : ids){
            entityManager.remove(entityManager.find(MutualWork.class, id));
        }
        entityManager.flush();
    }

    public List<MutualWorkData> getMutualWorkData(int year, int month, List<Integer> regions, Integer divisionOwner, Integer divisionEmployee, Integer projectId, boolean typeListObject) {

        String queryString = typeListObject ?
                "SELECT * FROM mutual_work_data WHERE year = :year AND month = :month " :
                "FROM MutualWorkData WHERE year = :year AND month = :month ";

        boolean ownerDivSet = false;
        boolean employeeDivSet = false;
        boolean projectSet = false;
        boolean regionSet = false;

        if (divisionOwner != null && divisionOwner > 0) {
            queryString += typeListObject ? " AND division_owner_id = :divisionOwner " :
                    " AND divisionOwnerId = :divisionOwner ";
            ownerDivSet = true;
        }
        if (divisionEmployee != null && divisionEmployee > 0) {
            queryString += typeListObject ? " AND division_employee_id = :divisionEmployee " :
                    " AND divisionEmployeeId = :divisionEmployee ";
            employeeDivSet = true;
        }
        if (projectId != null && projectId > 0) {
            queryString += typeListObject ? " AND project_id = :projectId " :
                    " AND projectId = :projectId ";
            projectSet = true;
        }
        if (regions.size() > 0 && regions.get(0) > 0) {
            queryString += typeListObject ? " AND region_id in :regions " :
                    " AND regionId in :regions ";
            regionSet = true;
        }

        queryString += typeListObject ? " ORDER BY employee_name" : " ORDER BY employeeName";
        Query query = typeListObject ?
                entityManager.createNativeQuery(queryString).setParameter("year", year).setParameter("month", month) :
                entityManager.createQuery(queryString).setParameter("year", year).setParameter("month", month);
        if (ownerDivSet) {
            query.setParameter("divisionOwner", divisionOwner);
        }
        if (employeeDivSet) {
            query.setParameter("divisionEmployee", divisionEmployee);
        }
        if (projectSet) {
            query.setParameter("projectId", projectId);
        }
        if (regionSet) {
            query.setParameter("regions", regions);
        }
        logger.debug("getMutualWorkData List<MutualWorkData> result size = {}", query.getResultList().size());
        return query.getResultList();
    }

    @Transactional
    public MutualWork findOrCreateMutualWork(Employee employee, Project project, Integer year, Integer month){
        Query query = entityManager.
                createQuery("FROM MutualWork WHERE employee = :employee AND project = :project AND year = :year AND month = :month")
                .setParameter("employee", employee)
                .setParameter("project", project)
                .setParameter("year", year)
                .setParameter("month", month);
        List result = query.getResultList();
        if (result.size() == 0){ // создадим новый
            MutualWork newMutualWork = new MutualWork();
            newMutualWork.setEmployee(employee);
            newMutualWork.setProject(project);
            newMutualWork.setYear(year);
            newMutualWork.setMonth(month);
            entityManager.persist(newMutualWork);
            logger.debug("findOrCreateMutualWork MutualWork id = {}", newMutualWork.getId());
            logger.info("findOrCreateMutualWork created newMutualWork");
            return newMutualWork;
        }else{
            logger.debug("findOrCreateMutualWork List<MutualWork> result size = {}", result.size());
            return (MutualWork)result.get(0);
        }
    }

}
