package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationApprovalResultDAO;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.dao.entity.VacationApprovalResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User: vsergeev
 * Date: 08.02.13
 */
@Service
public class VacationApprovalResultService {

    @Autowired
    private VacationApprovalResultDAO vacationApprovalResultDAO;

    @Transactional
    public VacationApprovalResult store(VacationApprovalResult vacationApprovalResult) {
        return vacationApprovalResultDAO.store(vacationApprovalResult);
    }

    /**
     * Возвращает объект класса VacationApprovalResult по заданному VacationApproval
     * @param vacationApproval
     * @return
     */
    public List<VacationApprovalResult> getVacationApprovalResultByManager(VacationApproval vacationApproval){
        return vacationApprovalResultDAO.getVacationApprovalResultByManager(vacationApproval);
    }
}
