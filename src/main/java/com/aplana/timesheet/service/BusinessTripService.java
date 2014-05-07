package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.BusinessTripDAO;
import com.aplana.timesheet.dao.entity.BusinessTrip;
import com.aplana.timesheet.dao.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 24.01.13
 */
@Service
public class BusinessTripService {

    @Autowired
    BusinessTripDAO businessTripDAO;

    @Transactional(readOnly = true)
    public List<BusinessTrip> getEmployeeBusinessTrips(Employee employee) {
        return businessTripDAO.getEmployeeBusinessTrips(employee);
    }

    @Transactional
    public void setBusinessTrip(BusinessTrip businessTrip) {
        businessTripDAO.setBusinessTrip(businessTrip);
    }

    @Transactional
    public void deleteBusinessTrip(BusinessTrip businessTrip) {
        businessTripDAO.deleteBusinessTrip(businessTrip);
    }

    @Transactional
    public void deleteBusinessTripById(Integer reportId) {
        businessTripDAO.deleteBusinessTripById(reportId);
    }

    public BusinessTrip find(Integer reportId) {
        return businessTripDAO.find(reportId);
    }

    public List<BusinessTrip> getEmployeeBusinessTripsByDates(Employee employee, Date beginDate, Date endDate) {
        return businessTripDAO.getEmployeeBusinessTripsByDates(employee, beginDate, endDate);
    }

    public boolean isBusinessTripDay(Employee employee, Date date) {
        return businessTripDAO.isBusinessTripDay(employee, date);
    }
}
