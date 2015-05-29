package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.IllnessDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessAddException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessAddForm;
import com.aplana.timesheet.system.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 27.01.13
 */
@Service
public class IllnessService {

    public static final String ERROR_ILLNESS_EDIT = "Ошибка при редактировании больничного!";
    public static final String ERROR_ILLNESS_SAVE = "Ошибка при сохранении больничного!";

    private static final Logger logger = LoggerFactory.getLogger(IllnessService.class);

    @Autowired
    IllnessDAO illnessDAO;

    @Autowired
    DictionaryItemService dictionaryItemService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private IllnessMailService illnessMailService;

    @Transactional(readOnly = true)
    public List<Illness> getEmployeeIllness(Employee employee){
        return illnessDAO.getEmployeeIllness(employee);
    }

    @Transactional
    public void setIllness(Illness ilness){
        illnessDAO.setIllness(ilness);
    }

    @Transactional
    public void deleteIllness(Illness illness) {
        illnessDAO.deleteIllness(illness);
    }

    @Transactional
    public void deleteIllnessById(Integer reportId) {
        illnessDAO.deleteIllnessById(reportId);
    }

    @Transactional
    public Illness find(Integer reportId) {
        return illnessDAO.find(reportId);
    }

    public List<Illness> getEmployeeIllnessByDates(Employee employee, Date beginDate, Date endDate) {
        return illnessDAO.getEmployeeIllnessByDates(employee, beginDate, endDate);
    }

    public int getIllnessWorkdaysCount(Employee employee, Integer year, Integer month) {
        return illnessDAO.getIllnessWorkdaysCount(employee, year, month);
    }

    /**
     * сохраняем больничный и возвращаем форму с табличкой по больничным и сообщением о результатах сохранения
     */
    @Transactional
    public Illness saveIllness(BusinessTripsAndIllnessAddForm tsForm, Integer reportId) throws BusinessTripsAndIllnessAddException {
        try {
            Illness illness = find(reportId);
            illness.setBeginDate(tsForm.getBeginDate());
            illness.setEndDate(tsForm.getEndDate());
            illness.setReason(dictionaryItemService.find(tsForm.getReason()));
            illness.setComment(tsForm.getComment());
            illness.setAuthor(securityService.getSecurityPrincipal().getEmployee());
            illness.setEditionDate(new Date());
            setIllness(illness);
            illnessMailService.sendEditMail(illness);
            return illness;
        } catch (Exception e) {
            logger.error(ERROR_ILLNESS_EDIT, e);
            throw new BusinessTripsAndIllnessAddException(ERROR_ILLNESS_EDIT, e);
        }
    }

    /**
     * создаем новый больничный, сохраняем в базу. возвращаем форму с табличкой по больничным и результатом сохранения
     */
    @Transactional
    public Illness addIllness(BusinessTripsAndIllnessAddForm tsForm) throws BusinessTripsAndIllnessAddException {
        try {
            Illness illness = new Illness();
            illness.setEmployee(tsForm.getEmployee());
            illness.setBeginDate(tsForm.getBeginDate());
            illness.setEndDate(tsForm.getEndDate());
            illness.setComment(tsForm.getComment());
            illness.setReason(dictionaryItemService.find(tsForm.getReason()));
            illness.setAuthor(securityService.getSecurityPrincipal().getEmployee());
            illness.setEditionDate(new Date());
            setIllness(illness);
            illnessMailService.sendCreateMail(illness);
            return illness;
        } catch (Exception e) {
            logger.error(ERROR_ILLNESS_SAVE, e);
            throw new BusinessTripsAndIllnessAddException(ERROR_ILLNESS_SAVE, e);
        }
    }
}
