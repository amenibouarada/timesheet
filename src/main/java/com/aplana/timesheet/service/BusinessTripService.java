package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.BusinessTripDAO;
import com.aplana.timesheet.dao.entity.BusinessTrip;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.BusinessTripTypesEnum;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessAddException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessAddForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(BusinessTripService.class);

    public static final String ERROR_BUSINESS_TRIP_SAVE = "Ошибка при сохранении командировки!";
    public static final String ERROR_BUSINESS_TRIP_EDIT = "Ошибка при редактировании командировки!";

    @Autowired
    BusinessTripDAO businessTripDAO;

    @Autowired
    DictionaryItemService dictionaryItemService;

    @Autowired
    ProjectService projectService;

    @Transactional(readOnly = true)
    public List<BusinessTrip> getEmployeeBusinessTrips(Employee employee) {
        return businessTripDAO.getEmployeeBusinessTrips(employee);
    }

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

    /**
     * создаем новую командировку, сохраняем в базу. возвращаем форму с табличкой по командировкам и результатом сохранения
     */
    public BusinessTrip addBusinessTrip (BusinessTripsAndIllnessAddForm tsForm) throws BusinessTripsAndIllnessAddException {
        try {
            BusinessTrip businessTrip = new BusinessTrip();
            businessTrip.setEmployee(tsForm.getEmployee());
            businessTrip.setBeginDate(tsForm.getBeginDate());
            businessTrip.setEndDate(tsForm.getEndDate());
            businessTrip.setComment(tsForm.getComment());
            businessTrip.setType(dictionaryItemService.find(tsForm.getBusinessTripType()));
            businessTrip.setProject(projectService.find(tsForm.getProjectId()));
            setBusinessTrip(businessTrip);
            return businessTrip;
        } catch (Exception e){
            logger.error(ERROR_BUSINESS_TRIP_SAVE, e);
            throw new BusinessTripsAndIllnessAddException(ERROR_BUSINESS_TRIP_SAVE, e);
        }
    }

    /**
     * сохраняем командировку и возвращаем форму с табличкой по командировкам и сообщением о результатах сохранения
     */
    public BusinessTrip saveBusinessTrip(BusinessTripsAndIllnessAddForm tsForm, Integer reportId) throws BusinessTripsAndIllnessAddException {
        try {
            BusinessTrip businessTrip = find(reportId);
            businessTrip.setBeginDate(tsForm.getBeginDate());
            businessTrip.setEndDate(tsForm.getEndDate());
            businessTrip.setType(dictionaryItemService.find(tsForm.getBusinessTripType()));
            if (tsForm.getBusinessTripType().equals(BusinessTripTypesEnum.PROJECT.getId())){
                businessTrip.setProject(projectService.find(tsForm.getProjectId()));
            } else {
                businessTrip.setProject(null);
            }
            businessTrip.setComment(tsForm.getComment());

            setBusinessTrip(businessTrip);
            return businessTrip;
        } catch (Exception e) {
            logger.error(ERROR_BUSINESS_TRIP_EDIT, e);
            throw new BusinessTripsAndIllnessAddException(ERROR_BUSINESS_TRIP_EDIT, e);
        }
    }
}
