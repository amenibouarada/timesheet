package com.aplana.timesheet.service.vacationapproveprocess;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 19.02.13
 */
@Service
public class VacationApprovalAutoProcessService extends AbstractVacationApprovalProcessService {

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalAutoProcessService.class);

    /**
     * запускаем проверку для всех несогласованных отпусков
     */
    public void checkAllVacations() throws VacationApprovalServiceException {
        logger.info("Start automatic vacations check");
        final TransactionStatus transactionStatus = getNewTransaction();
        try {
            List<Integer> vacations = vacationService.getAllNotApprovedVacationsIds();
            for (Integer vacationId : vacations) {
                final TransactionStatus internalTransactionStatus = getNewTransaction();

                try {
                    Vacation vacation = vacationService.findVacation(vacationId);
                    checkVacationIsApproved(vacation);

                    if (internalTransactionStatus != null) {
                        commit(internalTransactionStatus);
                    }
                } catch (Exception ex) {
                    if (internalTransactionStatus != null) {
                        rollback(internalTransactionStatus);
                    }
                    logger.error("Error occured ", ex);
                }
            }
            if (transactionStatus != null) {
                commit(transactionStatus);
            }
            logger.info("Finish automatic vacations check");

        } catch (Exception e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
        }
    }

    /**
     * проверяем, одобрено ли заявление на отпуск руководителями передаваемого проекта
     * т.к. это реализация автоматической проверки, то нужно еще смотреть за соблюдением сроков согласования
     */
    protected Boolean getManagersApproveResultForVacationByProject(Project project, Vacation vacation) throws VacationApprovalServiceException {
        List<VacationApproval> projectManagerApprovals = vacationApprovalService.getProjectManagerApprovalsForVacationByProject(vacation, project);
        boolean timeIsOver = checkTimeIsOverForProjectManagers(vacation);
        if (!timeIsOver) {
            return checkAllManagerApprovedVacation(projectManagerApprovals);
        } else {
            return checkOneManagerApprovedVacation(projectManagerApprovals, project);
        }
    }

    /**
     * проверяет, закончилось ли у руководителей проекта время для согласования отпуска
     */
    private boolean checkTimeIsOverForProjectManagers(Vacation vacation) throws VacationApprovalServiceException {
        Integer controlTimeForProjectManager = getControlTimeForProjectManager(vacation);
        Date date = new Date();
        return (date.after(DateUtils.addDays(vacation.getCreationDate(), controlTimeForProjectManager)));
    }

    /**
     * получаем максимальное количество дней, за которое руководители проекта должны согласовать заявление на отпуск
     */
    private Integer getControlTimeForProjectManager(Vacation vacation) throws VacationApprovalServiceException {
        Long daysForApprove = DateTimeUtil.getAllDaysCount(vacation.getCreationDate(), vacation.getBeginDate());
        Integer vacationTreshold = getVacationTreshold();
        if (daysForApprove >= vacationTreshold) {
            return getVacationProjectManagerOverrideThreshold();
        } else {
            return getVacationUrgentProjectManagerOverrideThreshold();
        }
    }

    /**
     * если сроки согласования для отпуска прошли, то для согласования заявления на отпуск достаточно того, что хотя бы один
     * из руководителей согласовал заявление. сперва смотрим решение РП, если он не согласовал - смотрим решения нижестоящих руководителей
     */
    private Boolean checkOneManagerApprovedVacation(List<VacationApproval> projectManagerApprovals, Project project) throws VacationApprovalServiceException {
        Boolean projectManagerApproveResult = getProjectManagerApproveResult(projectManagerApprovals, project);
        if (projectManagerApproveResult != null) {
            return projectManagerApproveResult;
        } else {
            return getJuniorProjectManagersApproveResults(projectManagerApprovals);
        }
    }

    /**
     * Проверяем, что хотя бы один из руководителей согласовал заявление на отпуск.
     */
    private Boolean getJuniorProjectManagersApproveResults(List<VacationApproval> projectManagerApprovals) {
        if (rejectionsExists(projectManagerApprovals)) {
            return false;
        }

        if (approvesExists(projectManagerApprovals)) {
            return true;
        }

        return null;
    }

    /**
     * проверяем, согласовал ли РП заявление на отпуск. Возвращаем его решение. если письмо руководителю проекта не посылалось, то
     * выбрасывается VacationApprovalServiceException
     */
    private Boolean getProjectManagerApproveResult(List<VacationApproval> projectManagerApprovals, Project project) throws VacationApprovalServiceException {
        if (project != null) {
            Employee manager = project.getManager();
            if (manager != null) {
                Integer projectManagerId = manager.getId();
                for (VacationApproval approval : projectManagerApprovals) {
                    Integer managerId = approval.getManager().getId();
                    if (managerId.equals(projectManagerId)) {
                        return (manager.getEndDate() != null  && projectManagerApprovals.size() == 1) ? true : approval.getResult();
                    }
                }

                Integer vacationId = projectManagerApprovals.get(0).getVacation().getId();
                throw new VacationApprovalServiceException(String.format("В БД не найдены данные о письме руководителю проекта %s для подтверждения отпуска №%s!",
                        project.getName(), vacationId));
            } else {
                throw new VacationApprovalServiceException(String.format("В БД не найдены данные о руководителе проекта %s", project.getName()));
            }
        } else {
            throw new VacationApprovalServiceException("В БД не найдены данные о проекте");
        }
    }

    /**
     * проверяем, согласован ли отпуск с линейным руководителем
     */
    protected Boolean vacationIsApprovedByLineManager(Vacation vacation) throws VacationApprovalServiceException {
        if (vacation.getStatus() != null && approvedByLineManager.contains(vacation.getStatus().getId())) {        //проверка уже утвержденного отпуска
            return true;
        }

        if (!employeeHasManager(vacation.getEmployee())) {      //если линейных нет или сам себе линейный - утверждаем без проверок
            setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, true);
            return true;
        }

        /* APLANATS-865
         Boolean manager2Result = getManager2Result(vacation);       //если второй линейный отказал - сразу возвращаем отказ
        if (BooleanUtils.isFalse(manager2Result)) {
            return manager2Result;
        }*/

        int lineManagerDaysToApprove = getControlTimeForLineManager(vacation);
        VacationApproval lineManagerApproval = getTopLineManagerApproval(vacation);

        if (lineManagerApproval.getResult() != null) {    //нашли результат у одного из линейных
            setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, lineManagerApproval.getResult());
            return lineManagerApproval.getResult();
        }

        if (lineManagerHasTimeToApproveVacation(lineManagerDaysToApprove, lineManagerApproval)) { //у линейного еще есть время подумать
            return lineManagerApproval.getResult();
        }

        /*if (manager2Result != null) {       //после проверки всех линейных, если они не отвечают и если второй линейный вынес решение - возвращаем его
            return manager2Result;
        }*/

        VacationApproval approvalResult = prepareApproveLetterForLineManagerOfEmployee(vacation, lineManagerApproval.getManager());
        if (approvalResult != null) {
            sendMailService.performVacationApproveRequestSender(approvalResult); //посылаем письмо с уведомлением следующему в иерархии линейному руководителю
        } else {
            if (!employeeService.isLineManager(vacation.getEmployee())) {     //проверяем, что сотрудник - не чей-то линейный
                setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, true);
                return true;
            }
        }

        return false;
    }

    /**
     * рекурсивно поднимаемся по руководителям (employee.manager.manager...) пока не найдем последнего,
     * кому отправлялся запрос согласования. (продолаем рекурсивно подниматься)
     */
    protected VacationApproval getTopLineManagerApprovalRecursive(VacationApproval vacationApproval) throws VacationApprovalServiceException {

        int lineManagerDaysToApprove = getControlTimeForLineManager(vacationApproval.getVacation());

        if (vacationApproval.getResult() != null) {        //линейный вынес решение об отпуске
            return vacationApproval;
        }

        if (lineManagerHasTimeToApproveVacation(lineManagerDaysToApprove, vacationApproval)) { //у линейного еще есть время подумать
            return vacationApproval;
        }

        Employee manager = vacationApproval.getManager();

        if (!employeeHasManager(manager)) {  //у линейного нет руководителя или он сам себе руководитель
            return vacationApproval;
        }

        Vacation vacation = vacationApproval.getVacation();
        VacationApproval managerOfManagerApproval = tryGetManagerApproval(vacation, manager.getManager());
        if (managerOfManagerApproval == null) {  //письмо линейному руководителю этого линейного еще не посылалось
            return vacationApproval;
        }

        return getTopLineManagerApprovalRecursive(managerOfManagerApproval);       //проверяем следующего по иерархии линейного руководителя
    }

    /**
     * получаем максимальное количество дней для согласования отпуска руководителем проекта в обычном режиме
     */
    public Integer getVacationProjectManagerOverrideThreshold() throws VacationApprovalServiceException {
        return propertyProvider.getVacationProjectManagerOverrideThreshold();
    }

    /**
     * получаем максимальное количество дней для согласования отпуска руководителем проекта в ускоренном режиме
     */
    private Integer getVacationUrgentProjectManagerOverrideThreshold() throws VacationApprovalServiceException {
        return propertyProvider.getVacationUrgentProjectManagerOverrideThreshold(); //kss 25.02.2013, похоже была опечатка (getVacationUrgentLineManagerOverrideThreshold)
    }

}
