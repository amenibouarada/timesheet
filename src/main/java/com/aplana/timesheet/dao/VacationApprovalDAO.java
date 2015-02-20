package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author iziyangirov
 */

@Repository
public class VacationApprovalDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Согласование на отпуск по переданному uid
     * @param uid
     * @return
     */
    public VacationApproval findVacationApproval(String uid) {
        final Query query =
                entityManager.createQuery("from VacationApproval va where va.uid = :uid")
                        .setParameter("uid", uid);

        if (query.getResultList().isEmpty()){
            return null;
        }

        return (VacationApproval)query.getSingleResult();
    }

    public VacationApproval find(Integer id) {
        return entityManager.find(VacationApproval.class, id);
    }

    public VacationApproval store(VacationApproval vacationApproval){
        vacationApproval = entityManager.merge(vacationApproval);
        entityManager.flush();

        return vacationApproval;
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId) {
        final Query query = entityManager.createQuery("select va.manager.email from VacationApproval va where va.vacation.id = :vac_id")
                .setParameter("vac_id", vacationId);

        return query.getResultList();
    }

    /**
     * получаем данные о согласовании отпуска конкретным менеджером
     */
    public VacationApproval getManagerApproval(Vacation vacation, Employee manager) {
        try {
            Query query = entityManager.createQuery("from VacationApproval as va " +
                    "where va.manager = :manager and va.vacation = :vacation")
                    .setParameter("manager", manager)
                    .setParameter("vacation", vacation);

            return (VacationApproval) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public VacationApproval tryGetManagerApproval(Vacation vacation, Employee manager) {
        try {
            Query query = entityManager.createQuery("from VacationApproval as va " +
                    "where va.vacation = :vacation and va.manager = :manager")
                    .setParameter("vacation", vacation)
                    .setParameter("manager", manager);

            return (VacationApproval) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<VacationApproval> getProjectManagerApprovalsForVacationByProject(Vacation vacation, Project project) {
        Query query = entityManager.createQuery("select distinct va from VacationApproval as va " +
                "where (va.vacation = :vacation) and va.manager = :manager")
                  .setParameter("manager", project.getManager())
                .setParameter("vacation", vacation);

        return query.getResultList();
    }

    public List<VacationApproval> getAllApprovalsForVacation(Vacation vacation) {
        return entityManager.createQuery("from VacationApproval as va where va.vacation = :vacation")
                .setParameter("vacation", vacation).getResultList();
    }

    public void deleteVacationApproval(VacationApproval vacationApproval) {
        Hibernate.initialize(vacationApproval);
        entityManager.remove(vacationApproval);
    }
}
