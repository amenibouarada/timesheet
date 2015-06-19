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
        return query.getResultList();
    }

    @Transactional
    public MutualWork findOrCreateMutualWork(Employee employee, Project project){
        Query query = entityManager.
                createQuery("FROM MutualWork WHERE employee = :employee AND project = :project")
                .setParameter("employee", employee)
                .setParameter("project", project);
        List result = query.getResultList();
        if (result.size() == 0){ // создадим новый
            MutualWork newMutualWork = new MutualWork();
            entityManager.persist(newMutualWork);
            return newMutualWork;
        }else{
            return (MutualWork)result.get(0);
        }
    }

}
