package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationApprovalDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.service.vacationapproveprocess.VacationApprovalProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author iziyangirov
 */
@Service
public class VacationApprovalService {

    @Autowired
    private VacationApprovalDAO vacationApprovalDAO;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private VacationApprovalProcessService vacationApprovalProcessService;

    public VacationApproval getVacationApproval(String uid){
        return vacationApprovalDAO.findVacationApproval(uid);
    }

    public VacationApproval find(Integer id){
        return vacationApprovalDAO.find(id);
    }

    @Transactional
    public VacationApproval store(VacationApproval vacationApproval){
        return vacationApprovalDAO.store(vacationApproval);
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId){
        return vacationApprovalDAO.getVacationApprovalEmailList(vacationId);
    }

    public List<VacationApproval> getAllApprovalsForVacation(Vacation vacation) {
        return vacationApprovalDAO.getAllApprovalsForVacation(vacation);
    }

    public List<VacationApproval> getProjectManagerApprovalsForVacationByProject(Vacation vacation, Project project) {
        return vacationApprovalDAO.getProjectManagerApprovalsForVacationByProject(vacation, project);
    }

    public VacationApproval tryGetManagerApproval(Vacation vacation, Employee manager) {
        return vacationApprovalDAO.tryGetManagerApproval(vacation, manager);
    }

    @Transactional
    public void deleteVacationApprovalByIdAndCheckIsApproved(Integer approvalId) throws VacationApprovalServiceException {
        VacationApproval vacationApproval = find(approvalId);
        final Employee employee = securityService.getSecurityPrincipal().getEmployee();
        final boolean isAdmin = employeeService.isEmployeeAdmin(employee.getId());
        if (isAdmin) {
            if (vacationApproval != null) {
               deleteVacationApproval(vacationApproval);
            } else {
                throw new VacationApprovalServiceException("Ошибка при удалении, данные отсутствуют");
            }
        } else {
            throw new VacationApprovalServiceException("Ошибка доступа");
        }

        Vacation vacation = vacationApproval.getVacation();
        if (vacation == null) {
            throw new VacationApprovalServiceException("Ошибка при удалении, данные отсутствуют");
        }
        vacationApprovalProcessService.checkVacationIsApproved(vacation);
    }

    public void deleteVacationApproval(VacationApproval vacationApproval) {
        vacationApprovalDAO.deleteVacationApproval(vacationApproval);
    }
}
