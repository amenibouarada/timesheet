package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.IllnessDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
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

    @Autowired
    IllnessDAO illnessDAO;

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
}
