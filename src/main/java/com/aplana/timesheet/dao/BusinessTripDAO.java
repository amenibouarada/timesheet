package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.BusinessTrip;
import com.aplana.timesheet.dao.entity.Employee;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 24.01.13
 */
@Repository
public class BusinessTripDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<BusinessTrip> getEmployeeBusinessTrips(Employee employee) {
        Query query = entityManager.createQuery("from BusinessTrip as bt where bt.employee = :employee order by bt.beginDate");
        query.setParameter("employee", employee);

        return (List<BusinessTrip>) query.getResultList();
    }

    public void setBusinessTrip(BusinessTrip businessTrip){
        BusinessTrip businessTripMerged = entityManager.merge(businessTrip);
        entityManager.flush();
    }

    public void deleteBusinessTrip(BusinessTrip businessTrip) {
        entityManager.remove(businessTrip);
    }

    public void deleteBusinessTripById(Integer reportId) {
        Query query = entityManager.createQuery("delete from BusinessTrip as bt where bt.id = :id");
        query.setParameter("id", reportId);

        query.executeUpdate();
    }

    public BusinessTrip find(Integer reportId) {
        Query query = entityManager.createQuery(
                "select bt from BusinessTrip as bt where bt.id = :id"
        ).setParameter( "id", reportId );

        return (BusinessTrip) query.getSingleResult();
    }

    public List<BusinessTrip> getEmployeeBusinessTripsByDates(Employee employee, Date beginDate, Date endDate) {
        return entityManager.createQuery("from BusinessTrip as bt " +
                "where bt.employee = :employee and bt.beginDate <= :endDate and bt.endDate >= :beginDate")
                .setParameter("employee", employee)
                .setParameter("beginDate", beginDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    /**
     * Командировки, которые имеют пересечения с периодом
     * @param employee
     * @param beginDate - начало периода
     * @param endDate - окончание периода
     * @return
     */
    public List<BusinessTrip> getEmployeeBusinessTripsIntersectionDates(Employee employee, Date beginDate, Date endDate) {
        return entityManager.createQuery(
                "from BusinessTrip as bt " +
                "where bt.employee = :employee and" +
                        "(bt.beginDate >= :beginDate and bt.beginDate <= :endDate " +
                        "or " +
                        ":beginDate >= bt.beginDate and :beginDate <= bt.endDate)")
                .setParameter("employee", employee)
                .setParameter("beginDate", beginDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }


    public Boolean isBusinessTripDay(Employee employee, Date date){
        Query query = entityManager.createQuery(
                "from BusinessTrip as bt where bt.employee = :employee and :date between bt.beginDate and bt.endDate"
        ).setParameter("employee", employee).setParameter("date", date);
        return !query.getResultList().isEmpty();
    }

}
