package com.aplana.timesheet.service.vacationapproveprocess;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * User: vsergeev
 * Date: 19.02.13
 */
public abstract class AbstractVacationApprovalProcessService extends AbstractServiceWithTransactionManagement {

    private List<Integer> approvedByProjectManager = Arrays.asList(VacationStatusEnum.APPROVED_BY_PM.getId(),
            VacationStatusEnum.APPROVEMENT_WITH_LM.getId(), VacationStatusEnum.APPROVED.getId(), VacationStatusEnum.REJECTED.getId());

    protected List<Integer> approvedByLineManager = Arrays.asList(VacationStatusEnum.APPROVED.getId(), VacationStatusEnum.REJECTED.getId());

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalService.class);

    private static final String VACATION_APPROVE_MAILS_SEND_FAILED_EXCEPTION_MESSAGE = "Отпуск создан, но рассылка о согласовании не произведена!";

    @Autowired
    protected ProjectService projectService;
    @Autowired
    protected TSPropertyProvider propertyProvider;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    protected VacationService vacationService;
    @Autowired
    protected SendMailService sendMailService;
    @Autowired
    private VacationApprovalResultService vacationApprovalResultService;
    @Autowired
    protected ProjectManagerService projectManagerService;
    @Autowired
    protected VacationApprovalService vacationApprovalService;
    @Autowired
    protected EmployeeService employeeService;

    /**
     * Рассылаем всем сотрудникам, которым высылались письма с просьбой согласовать отпуск, сообщения, что
     * отпуск согласован
     */
    public void sendVacationApprovedMessages (Vacation vacation) {
        List<VacationApproval> vacationApprovals = vacationApprovalService.getAllApprovalsForVacation(vacation);
        vacationApprovals.add(createNewVacationApproval(vacation, new Date(), vacation.getEmployee()));

        List<String> emails = prepareEmailsListForVacationApprovedMessage(vacationApprovals);
        sendMailService.performVacationApprovedSender(vacation, emails);
    }

    /**
     * формируем список адресов, на которые будет разослано сообщение об успешном согласовании
     */
    protected List<String> prepareEmailsListForVacationApprovedMessage(List<VacationApproval> vacationApprovals) {
        if (! vacationApprovals.isEmpty()) {
            List<String> emails = new ArrayList<String>();
            for (VacationApproval vacationApproval : vacationApprovals) {
                emails.add(vacationApproval.getManager().getEmail());
            }

            return emails;
        }
        return null;
    }

    /**
     * Проверяем, согласован ли отпуск
     */
    public Boolean checkVacationIsApproved(Vacation vacation) throws VacationApprovalServiceException {
        if (BooleanUtils.isTrue(vacationIsApprovedByProjectManagers(vacation))) {        //проверяем руководителей проектов
            Boolean lineManagersResult = vacationIsApprovedByLineManager(vacation);     //проверяем линейного
            return BooleanUtils.isTrue(lineManagersResult);
        }
        return false;
    }

    protected abstract Boolean vacationIsApprovedByLineManager(Vacation vacation) throws VacationApprovalServiceException;

    /**
     * поднимаемся по руководителям (employee.manager.manager...) пока не найдем последнего,
     * кому отправлялся запрос согласования. (начинаем подниматься...)
     * Вернет null, если ЛР еще не согласовывали
     */
    protected VacationApproval getTopLineManagerApproval(Vacation vacation) throws VacationApprovalServiceException {
        Employee manager = vacation.getEmployee().getManager();
        VacationApproval lineManagerOfEmployeeApproval = tryGetManagerApproval(vacation, manager);
        if (lineManagerOfEmployeeApproval == null) {
            return null;
        }

        return getTopLineManagerApprovalRecursive(lineManagerOfEmployeeApproval);
    }

    protected abstract VacationApproval getTopLineManagerApprovalRecursive(VacationApproval lineManagerOfEmployeeApproval) throws VacationApprovalServiceException;

    public VacationApproval tryGetManagerApproval(Vacation vacation, Employee manager) {
        return vacationApprovalService.tryGetManagerApproval(vacation, manager);
    }

    /**
     * проверяем, есть ли у сотрудника линейный или он последний в иерархии
     */
    protected boolean managerExists(Employee employee) {
        Employee manager = employee.getManager();

        return ! ( manager == null || manager.getId().equals(employee.getId()) );
    }

    /**
     * устанавливаем у отпуска статус "согласован" и рассылаем об этом уведомления
     */
    protected Boolean setFinalStatusForVacationAndSendVacationApprovedMessages(Vacation vacation, Boolean status) {
        if (status) {
            vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()));
        } else {
            vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.REJECTED.getId()));
        }
        vacationService.store(vacation);

        sendVacationApprovedMessages(vacation);  //делаем рассылку о том, что отпуск согласован

        return status;
    }

    /**
     * проверяет, согласован ли отпуск с руководителями проектов, которые получали уведомления
     * о согласовании переданного отпуска
     */
    private Boolean vacationIsApprovedByProjectManagers(Vacation vacation) throws VacationApprovalServiceException {

        if (approvedByProjectManager.contains(vacation.getStatus().getId())){   //проверяется уже утвержденный руководителями проектов отпуск
            return vacationIsNotRejected(vacation);
        }

        List<Project> projectsForVacation = projectService.getProjectsAssignedToVacation(vacation);
        Map<Project, Boolean> managerApproveResult = checkManagerApproveResultForVacationByProjects(vacation, projectsForVacation);

        if (managerApproveResult.values().contains(false)) {        //один из менеджеров отказал в отпуске! :(
            return setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, false);
        }

        if (managerApproveResult.values().contains(null)) {       //если отпуск по каким-то проектам еще ожидает решения
            return null;
        }

        setApprovementWithLineManagerStatusAndSendMessages(vacation);

        return true;
    }

    /**
     * Ставим отпуску статус "на согласовании с линейным руководителем" и делаем рассылку по линейным с просьбой согласовать отпуск
     */
    private void setApprovementWithLineManagerStatusAndSendMessages(Vacation vacation) throws VacationApprovalServiceException {

        if (! managerExists(vacation.getEmployee())) {      //если линейных нет или сам себе линейный - утверждаем без проверок
            setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, true);
            return;
        }

        vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVEMENT_WITH_LM.getId()));     //в БД отмечаем, что отпуск теперь на согласовании линейного руководителя
        vacationService.store(vacation);

        VacationApproval lineManagerApproval = getTopLineManagerApproval(vacation);   //проверяем, посылалось ли письмо линейным руководителям
        if (lineManagerApproval == null) {      //если не посылалось, посылаем
            /* APLANATS-865
            VacationApproval lineManager2VacationApproval = null;
            if (vacation.getEmployee().getManager2() != null) {
                lineManager2VacationApproval = vacationApprovalService.tryGetManagerApproval(vacation, vacation.getEmployee().getManager2());
                if (lineManager2VacationApproval != null && lineManager2VacationApproval.getResult() != null) {
                    setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, lineManager2VacationApproval.getResult());
                }
                if (lineManager2VacationApproval == null) {
                    lineManager2VacationApproval = createNewVacationApproval(vacation, new Date(), vacation.getEmployee().getManager2());
                    lineManager2VacationApproval = vacationApprovalService.store(lineManager2VacationApproval);
                }
            }*/

            lineManagerApproval = prepareApproveLetterForLineManagerOfEmployee(vacation, vacation.getEmployee());
            sendMailService.performVacationApproveRequestSender(lineManagerApproval);
            /*if (lineManager2VacationApproval != null) {
                sendMailService.performVacationApproveRequestSender(lineManager2VacationApproval);
            }*/
        }
    }

    protected boolean vacationIsNotRejected(Vacation vacation) {
        return ! vacation.getStatus().getId().equals(VacationStatusEnum.REJECTED.getId());
    }

    /**
     * заносим в БД данные о согласовании отпуска линейным руководителем
     */
    protected VacationApproval prepareApproveLetterForLineManagerOfEmployee(Vacation vacation, Employee employee) {
        Employee managerWithoutApproval = getFirstManagerWithoutApproval(vacation, employee);

        if (managerWithoutApproval == null) { //выше уже нет линейных
            return null;
        }

        VacationApproval vacationApproval = new VacationApproval();
        vacationApproval.setManager(managerWithoutApproval);
        vacationApproval.setVacation(vacation);
        vacationApproval.setUid(UUID.randomUUID().toString());
        vacationApproval.setRequestDate(new Date());

        vacationApproval = vacationApprovalService.store(vacationApproval);

        return vacationApproval;
    }

    /**
     * Получаем следующего в иерархии линейного руководителя, которому еще не рассылалось письмо с просьбой подтвердить отпуск
     */
    private Employee getFirstManagerWithoutApproval(Vacation vacation, Employee employee) {
        VacationApproval vacationApproval;
        do {
            if (! managerExists(employee)) {
                return null;
            }
            employee = employee.getManager();
            vacationApproval = tryGetManagerApproval(vacation, employee);
        } while (vacationApproval != null);

        return employee;
    }

    /**
     * проверяем, одобрено ли заявление на отпуск руководителями переданных проектов
     */
    private Map<Project, Boolean> checkManagerApproveResultForVacationByProjects(Vacation vacation, List<Project> projectsForVacation) throws VacationApprovalServiceException {
        Map<Project, Boolean> managerApproveResults = new HashMap<Project, Boolean>();
        for (Project project : projectsForVacation) {
            managerApproveResults.put(project, getManagersApproveResultForVacationByProject(project, vacation));
        }

        return managerApproveResults;
    }

    protected abstract Boolean getManagersApproveResultForVacationByProject(Project project, Vacation vacation) throws VacationApprovalServiceException;


    /**
     * проверяем, что все менеджеры дали решение по поводу отпуска
     */
    protected Boolean checkAllManagerApprovedVacation(List<VacationApproval> projectManagerApprovals) {
        if (rejectionsExists(projectManagerApprovals)) {
            return false;
        }
        if (delaysExists(projectManagerApprovals)) {
            return null;
        }

        return true;
    }

    /**
     * проверяем, есть ли еще письма, ожидающие решения руководителей проекта
     */
    private boolean delaysExists(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals) {
            if (approval.getResult() == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяем, есть ли отказы у руководителей проектов. если есть хотя бы один отказ - отпуск сразу отклоняется
     */
    protected boolean rejectionsExists(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals) {
            if (BooleanUtils.isFalse(approval.getResult())) {
                return true;
            }
        }

        return false;
    }

    /**
     * получаем количество дней, по которому в дальнейшем будет определяться,
     * согласовывать отпуск в обычном режиме или в ускоренном
     */
    public Integer getVacationTreshold() throws VacationApprovalServiceException {
        return propertyProvider.getVacationCreateThreshold();
    }

    /**
     * подготавливаем данные для рассылки писем об утверждении отпуска
     */
    public void sendVacationApproveRequestMessages(Vacation vacation) throws VacationApprovalServiceException {
        try {
            final List<Project> projects = projectService.getProjectsForVacation(vacation);
            final Employee employee = vacation.getEmployee();

            boolean isProjectManagerInAllProjects = true;

            for (Project project : projects) {
                isProjectManagerInAllProjects &= (employee.equals(project.getManager()));
            }

            if (isProjectManagerInAllProjects) {
                // если нет проектов ни в планах, ни в списаниях - сразу утверждаем у линейного
                setApprovementWithLineManagerStatusAndSendMessages(vacation);
            } else {
                Map<String, VacationApproval> juniorProjectManagersVacationApprovals = prepareJuniorProjectManagersVacationApprovals(projects, vacation);
                Map<String, VacationApproval> allManagerVacationApprovals = prepareProjectManagersVacationApprovals(juniorProjectManagersVacationApprovals, projects, vacation);
                // если список утверждающих менеджеров пустой - сразу утверждаем у линейного
                if (allManagerVacationApprovals == null || allManagerVacationApprovals.isEmpty()) {
                    setApprovementWithLineManagerStatusAndSendMessages(vacation);
                }
                for (VacationApproval vacationApproval : allManagerVacationApprovals.values()) {
                    sendMailService.performVacationApproveRequestSender(vacationApproval);
                }
            }

        } catch (CalendarServiceException ex) {
            throw new VacationApprovalServiceException(VACATION_APPROVE_MAILS_SEND_FAILED_EXCEPTION_MESSAGE, ex);
        }

    }

    public void sendNoticeForPlannedVacaton(Vacation vacation) throws VacationApprovalServiceException {
        final List<Project> projects = projectService.getProjectsForVacation(vacation);

        List<String> emails = new ArrayList<String>();
        Map<Employee, List<Project>> juniorProjectManagersAndProjects =
                employeeService.getJuniorProjectManagersAndProjects(projects, vacation);
        for (Map.Entry entry: juniorProjectManagersAndProjects.entrySet()) {
            emails.add(((Employee) entry.getKey()).getEmail());
        }

        for (Project project : projects) {
            Employee manager = project.getManager();
            String email = manager.getEmail();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }

        addLineManagers(emails, vacation);
        addSecondManager(emails, vacation);
        vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()));     //в БД отмечаем, что отпуск утвержден
        vacationService.store(vacation);
        sendMailService.performPlannedVacationCreateRequestSender(vacation, emails);
    }

    private void addSecondManager(List<String> emails, Vacation vacation) {
        Employee manager2 = vacation.getEmployee().getManager2();
        if (manager2 != null) {
            String email = manager2.getEmail();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }
    }

    /**
     * добавляем емейлы для уведомлений линейному руководителю сотрудника и всем руководителям руководителя сотрудника вплоть до РЦК включительно
     */
    private void addLineManagers(List<String> emails, Vacation vacation) throws VacationApprovalServiceException {
        Employee manager = vacation.getEmployee().getManager();
        String email = manager.getEmail();
        if (!emails.contains(email)) {
            emails.add(email);
        }
        addLineManagerRecursive(manager, emails);
    }

    private void addLineManagerRecursive(Employee manager, List<String> emails) throws VacationApprovalServiceException {
        Employee employeeManager = manager.getManager();
        if (employeeManager != null) {
            String email = employeeManager.getEmail();
            if (!emails.contains(email)) {
                emails.add(email);
            }
            addLineManagerRecursive(employeeManager, emails);
        }
    }

    /**
     * создаем заготовки для писем о согласовании отпуска руководителям проектов
     */
    private Map<String, VacationApproval> prepareProjectManagersVacationApprovals(Map<String, VacationApproval> approvals, List<Project> projects, Vacation vacation) {
        Date requestDate = new Date();

        for (Project project : projects) {
            Employee manager = project.getManager();
            tryAddNewManagerToApprovalResults(vacation, requestDate, approvals, manager, Lists.newArrayList(project));
        }

        return approvals;
    }

    /**
     * создаем заготовки для писем о согласовании отпуска для младших менеджеров на проекте (тимлид, старший аналитик, заместители...)
     */
    private Map<String, VacationApproval> prepareJuniorProjectManagersVacationApprovals(List<Project> projects, Vacation vacation) throws CalendarServiceException {
        Map<Employee, List<Project>> juniorProjectManagersAndProjects =
                employeeService.getJuniorProjectManagersAndProjects(projects, vacation);

        return createJuniorManagersVacationApprovals(juniorProjectManagersAndProjects, vacation);
    }

    /**
     * Создаем записи для утверждения отпусков младшими менеджерами в таблицах.
     */
    private Map<String, VacationApproval> createJuniorManagersVacationApprovals(Map<Employee, List<Project>> juniorProjectManagersAndProjects, Vacation vacation) {
        Date requestDate = new Date();
        Map<String, VacationApproval> approvals = new HashMap<String, VacationApproval>();

        for (Employee manager : juniorProjectManagersAndProjects.keySet()) {
            tryAddNewManagerToApprovalResults(vacation, requestDate, approvals, manager, juniorProjectManagersAndProjects.get(manager));
        }

        return approvals;
    }

    /**
     * пытаемся добавить сотрудника к рассылке. Если сотрудник уже есть в рассылке - не добавляем его. Если сотрудник является линейным менеджером, игнорируем его.
     */
    private void tryAddNewManagerToApprovalResults(Vacation vacation, Date requestDate, Map<String, VacationApproval> approvals,
                                                   Employee manager, List<Project> projects) {
        //Не согласуем у самого себя
        if(vacation.getEmployee().getId().equals(manager.getId()))
            return;
        //получаем список линейных руководителей
        List<Employee> linearManagers = employeeService.getLinearEmployees(vacation.getEmployee());
        for (Project project : projects) {
            if(linearManagers!=null && !linearManagers.contains(manager)){
                VacationApproval vacationApproval = (approvals.get(manager.getEmail()) != null) ?
                        approvals.get(manager.getEmail()) : addNewVacationApproval(approvals, vacation, requestDate, manager);

                VacationApprovalResult vacationApprovalResult = new VacationApprovalResult();
                vacationApprovalResult.setProject(project);
                vacationApprovalResult.setVacationApproval(vacationApproval);

                vacationApprovalResultService.store(vacationApprovalResult);
            }
        }
    }

    /**
     * добавляем в мапу с заготовками новую заготовку
     */
    private VacationApproval addNewVacationApproval(Map<String, VacationApproval> approvals, Vacation vacation, Date requestDate, Employee employee) {
        VacationApproval vacationApproval = createNewVacationApproval(vacation, requestDate, employee);
        vacationApproval = vacationApprovalService.store(vacationApproval);
        approvals.put(employee.getEmail(), vacationApproval);

        return vacationApproval;
    }

    /**
     * создаем новую заготовку
     */
    private VacationApproval createNewVacationApproval(Vacation vacation, Date requestDate, Employee manager) {
        VacationApproval vacationApproval = new VacationApproval();
        vacationApproval.setVacation(vacation);
        vacationApproval.setManager(manager);
        vacationApproval.setRequestDate(requestDate);
        vacationApproval.setUid(UUID.randomUUID().toString());

        return vacationApproval;
    }

    /**
     * Проверяем, есть ли среди писем с просьбой утвердить отпуск положительные ответы
     */
    protected boolean approvesExists(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals){
            if (BooleanUtils.isTrue(approval.getResult())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Получаем результат второго линейного
     */
    /* APLANATS-865
    protected Boolean getManager2Result(Vacation vacation) {
        if (vacation.getEmployee().getManager2() == null) {
            return null;
        }

        VacationApproval lineManager2Approval = vacationApprovalService.tryGetManagerApproval(vacation, vacation.getEmployee().getManager2());

        return (lineManager2Approval != null) ? lineManager2Approval.getResult() : null;
    }*/

    /**
     * получаем мексимальное количество дней, за которое линейный руководитель должен утвердить заявление на отпуск
     */
    protected Integer getControlTimeForLineManager(Vacation vacation) throws VacationApprovalServiceException {
        Long daysForApprove = DateTimeUtil.getAllDaysCount(vacation.getCreationDate(), vacation.getBeginDate());
        Integer vacationTreshold = getVacationTreshold();
        if (daysForApprove >= vacationTreshold) {
            return propertyProvider.getVacationLineManagerOverrideThreshold();
        } else {
            return propertyProvider.getVacationUrgentLineManagerOverrideThreshold();
        }
    }

    /**
     * Проверяем, успевает ли линейный руководитель вынести решение по заявлению на отпуск
     */
    protected boolean lineManagerHasTimeToApproveVacation(int lineManagerDaysToApprove, VacationApproval lineManagerApproval) {
        Date lastLineManagerApproveDate = lineManagerApproval.getRequestDate();
        return DateTimeUtil.getAllDaysCount(lastLineManagerApproveDate, new Date()) <= lineManagerDaysToApprove; // kss 05.03.2013. Было >=, исправил на <=
    }

}
