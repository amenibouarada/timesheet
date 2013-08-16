package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.MailSenders.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.util.TimeSheetUser;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.annotation.Nullable;
import java.util.*;

@Service
public class SendMailService{
    private static final Logger logger = LoggerFactory.getLogger(SendMailService.class);

    private final Predicate<ProjectActivityInfo> LEAVE_PRESALE_AND_PROJECTS_ONLY =
            Predicates.and(Predicates.notNull(), new Predicate<ProjectActivityInfo>() {
                @Override
                public boolean apply(@Nullable ProjectActivityInfo projectActivityInfo) {
                    TypesOfActivityEnum actType = projectActivityInfo.getTypeOfActivity();
                    return actType == TypesOfActivityEnum.PROJECT || actType == TypesOfActivityEnum.PRESALE;
                }
            });
    private final Function<ProjectManager, String> GET_EMAIL_FROM_MANAGER
            = new Function<ProjectManager, String>() {
        @Nullable @Override
        public String apply(@Nullable ProjectManager projectManager) {
            return projectManager.getEmployee().getEmail();
        }
    };

    private Function<ProjectActivityInfo,String> GET_EMAILS_OF_INTERESTED_MANAGERS_FROM_PROJECT_FOR_CURRENT_ROLE
            = new Function<ProjectActivityInfo, String>() {
        @Nullable @Override
        public String apply(@Nullable ProjectActivityInfo input) {
            final ProjectRolesEnum roleInCurrentProject = input.getProjectRole();

            final Project project = projectService.find(input.getProjectId());

            return Joiner.on(",").join(
                Iterables.transform(
                    Iterables.filter(
                        projectService.getManagers(projectService.find(input.getProjectId())),
                            new Predicate<ProjectManager>() {
                                @Override
                                public boolean apply(@Nullable ProjectManager manager) {
                                    if(manager == null || manager.getProjectRole()==null){
                                        return false;
                                    }
                                    if (manager.getEmployee().getId().equals(project.getManager().getId())){
                                        return true;
                                    }
                                    if(EnumsUtils.tryFindById(manager.getProjectRole().getId(),ProjectRolesEnum.class)==ProjectRolesEnum.HEAD){
                                        return true;
                                    }
                                    return (manager.getProjectRole().getId().equals(roleInCurrentProject.getId()));

                                } }),
                        GET_EMAIL_FROM_MANAGER
                )
        ); } };

    @Autowired
    public VelocityEngine velocityEngine;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private TSPropertyProvider propertyProvider;
    @Autowired
    private OvertimeCauseService overtimeCauseService;
    @Autowired
    private VacationApprovalService vacationApprovalService;
    @Autowired
    private ProjectManagerService projectManagerService;
    @Autowired
    private EmployeeAssistantService employeeAssistantService;
    @Autowired
    private ProjectTaskService projectTaskService;
    @Autowired
    private ManagerRoleNameService managerRoleNameService;


    /**
     * Возвращает строку с адресами линейных руководителей сотрудника
     * (непосредственного и всех вышестоящих) разделёнными запятой.
     * @param empId
     */
    public String getEmployeesManagersEmails(Integer empId) {
        Set<String> emailList = new HashSet<String>();

        final Employee employee = employeeService.find(empId);
        final Employee manager = employee.getManager();

        if (manager != null && !manager.getId().equals(empId)) {
            emailList.add(manager.getEmail());
            emailList.add(getEmployeesManagersEmails(manager.getId()));
        }

        return StringUtils.join(emailList, ',');
    }

    /**
     * Возвращает email сотрудника
     */
    public String getEmployeeEmail(Integer empId) {
        return empId == null ? null : employeeService.find(empId).getEmail();
    }

    /**
     * Возвращает ФИО сотрудника
     */
    public String getEmployeeFIO(Integer empId){
        return empId == null ? null : employeeService.find(empId).getName();
    }

    /**
     * Возвращает email сотрудника
     */
    public String getEmployeeDivision(Integer empId) {
        Division division = employeeService.find(empId).getDivision();
        return empId == null ? null : division == null ? null : division.getDepartmentName();
    }

    /**
     * Возвращает строку с адресами менеджеров проектов/пресейлов
     * @param tsForm
     */
    public String getProjectsManagersEmails(TimeSheetForm tsForm) {
        List<TimeSheetTableRowForm> tsRows = tsForm.getTimeSheetTablePart();

        if (tsRows == null) {
            return "";
        } //Нет проектов\пресейлов, нет и менеджеров.

        return Joiner.on(",").join(Iterables.transform(
                Iterables.filter(transformTimeSheetTableRowForm(tsRows), LEAVE_PRESALE_AND_PROJECTS_ONLY),
                new Function<ProjectActivityInfo, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable ProjectActivityInfo input) {
                        return getEmployeeEmail(projectService.find(input.getProjectId()).getManager().getId());
                    }
                }));
    }

    /**
     * Возвращает строку с email адресами в соответствии с логикой
     * РП - все роли
     * Руководителю группы разработки - конструктор, разработчик, системный инженер, тестировщик.
     * Ведущему аналитику - аналитик и технический писатель.
     *
     * @return emails - строка с emailАМИ
     */
    public String getProjectManagersEmails(TimeSheetForm tsForm) {
        if (tsForm.getTimeSheetTablePart() == null) return ""; // Нет строк в отчете - нет и участников
        return getProjectManagersEmails(transformTimeSheetTableRowForm(tsForm.getTimeSheetTablePart()));
    }


    /**
     * Получает email адреса из всех проектов
     * @param ts
     * @return string строка содержащая email's которым относится данный timesheet
     */
    public String getProjectManagersEmails(TimeSheet ts) {
        return getProjectManagersEmails(transformTimeSheetDetail(ts.getTimeSheetDetails()));
    }

    private String getProjectManagersEmails(Iterable<ProjectActivityInfo> details) {
        return StringUtils.join(
                Lists.newArrayList(Iterables.transform(
                        Iterables.filter(details, LEAVE_PRESALE_AND_PROJECTS_ONLY),
                        GET_EMAILS_OF_INTERESTED_MANAGERS_FROM_PROJECT_FOR_CURRENT_ROLE))
                , ",");
    }

    public List<Employee> getEmployeesList(Division division){
        return employeeService.getEmployees(division, false);
    }

    public void performMailing(TimeSheetForm form) {
        new TimeSheetSender(this, propertyProvider, overtimeCauseService).sendMessage(form);
    }

    public void performFeedbackMailing(FeedbackForm form) {
        new FeedbackSender(this, propertyProvider).sendMessage(form);
    }

    public void performLoginProblemMailing(AdminMessageForm form) {
        new LoginProblemSender(this, propertyProvider).sendMessage(form);
    }

    public void performPersonalAlertMailing(List<ReportCheck> rCheckList) {
        new PersonalAlertSender(this, propertyProvider).sendMessage(rCheckList);
    }

    public void performManagerMailing(List<ReportCheck> rCheckList) {
        new ManagerAlertSender(this, propertyProvider).sendMessage(rCheckList);
    }

    public void performEndMonthMailing(List<ReportCheck> rCheckList) {
        new EndMonthAlertSender(this, propertyProvider).sendMessage(rCheckList);
    }

    public void performTimeSheetDeletedMailing(TimeSheet timeSheet) {
        new TimeSheetDeletedSender(this, propertyProvider).sendMessage(timeSheet);
    }

    public void performVacationDeletedMailing(Vacation vacation) {
        new VacationDeletedSender(this, propertyProvider).sendMessage(vacation);
    }

    public void performVacationApproveRequestSender(VacationApproval vacationApproval) {
        new VacationApproveRequestSender(this, propertyProvider, vacationApprovalService, managerRoleNameService).sendMessage(vacationApproval);
    }

    public void performPlannedVacationCreateRequestSender(Vacation vacation, List<String> emails) {
        new PlannedVacationCreateSender(this, propertyProvider, emails).sendMessage(vacation);
    }

    public void performVacationApprovedSender (Vacation vacation, List<String> emails) {
        new VacationApprovedSender(this, propertyProvider, emails).sendMessage(vacation);
    }

    public void performExceptionSender(String problem){
        new ExceptionSender(this, propertyProvider).sendMessage(problem);
    }

    public void performVacationApprovalErrorThresholdMailing(){
        new VacationApprovalErrorThresholdSender(this, propertyProvider).sendMessage("");
    }

    public void loginFailureErrorThresholdMailing(){
        new LoginFailureErrorThresholdSender(this, propertyProvider).sendMessage("");
    }

    public void performVacationAcceptanceMailing(VacationApproval vacationApproval){
        new VacationApprovalAcceptanceSender(this, propertyProvider).sendMessage(vacationApproval);
    }

    public void performVacationCreateMailing(Vacation vacation) {
        new VacationCreateSender(this,propertyProvider,vacationApprovalService,managerRoleNameService).sendMessage(vacation);
    }

    public void plannedVacationInfoMailing(Map<Employee, Set<Vacation>> managerEmployeesVacation) {
        new PlannedVacationInfoSender(this, propertyProvider).sendMessage(managerEmployeesVacation);
    }

    public String initMessageBodyForReport(TimeSheet timeSheet) {
        Map<String, Object> model1 = new HashMap<String, Object>();
        Iterator<TimeSheetDetail> iteratorTSD = timeSheet.getTimeSheetDetails().iterator();
        Double summDuration = 0D;
        while (iteratorTSD.hasNext()){
            summDuration = summDuration + iteratorTSD.next().getDuration();
        }
        model1.put("summDuration", summDuration);
        model1.put("dictionaryItemService", dictionaryItemService);
        model1.put("projectService", projectService);
        model1.put("DateTimeUtil", DateTimeUtil.class);
        model1.put("senderName",
                timeSheet == null
                        ? securityService.getSecurityPrincipal().getEmployee().getName()
                        : timeSheet.getEmployee().getName());
        Map<String, Object> model = model1;

        model.put("timeSheet", timeSheet);
        logger.info("follows initialization output from velocity");
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "report.vm", model);
    }

    public TimeSheetUser getSecurityPrincipal() {
        return securityService.getSecurityPrincipal();
    }

    public String getProjectName(int projectId) {
        return projectService.find(projectId).getName();
    }

    public String getOvertimeCause(TimeSheetForm tsForm) {
        return overtimeCauseService.getCauseName(tsForm);
    }

    public Integer getOverUnderTimeDictId(Integer overtimeCause){
        return overtimeCauseService.getDictId(overtimeCause);
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId) {
        return vacationApprovalService.getVacationApprovalEmailList(vacationId);
    }

    public Map<Employee, List<Project>> getJuniorProjectManagersAndProjects(Project project, Vacation vacation) {
        return employeeService.getJuniorProjectManagersAndProjects(Arrays.asList(project), vacation);
    }

    public EmployeeAssistant getEmployeeAssistant(Set<String> managersEmails) {
        return employeeAssistantService.tryFind(managersEmails);
    }

    public String getProjectsManagersEmails(TimeSheet timeSheet) {
        return StringUtils.join(getProjectsManagersEmails(timeSheet.getTimeSheetDetails()), ',');
    }

    private Collection getProjectsManagersEmails(Set<TimeSheetDetail> timeSheetDetails) {
        final Set<String> emails = new HashSet<String>();

        Project project;

        for (TimeSheetDetail timeSheetDetail : timeSheetDetails) {
            project = timeSheetDetail.getProject();

            if (project != null && project.getManager() != null  &&
                    TypesOfActivityEnum.isProjectOrPresale(
                            EnumsUtils.tryFindById(timeSheetDetail.getActType().getId(), TypesOfActivityEnum.class)
                    )
            ) {
                emails.add(project.getManager().getEmail());
            }
        }

        return emails;
    }

    public String getEmployeesAdditionalManagerEmail(Integer employeeId) {
        final Employee employee = employeeService.find(employeeId);
        final Employee manager2 = employee.getManager2();

        if (manager2 != null) {
            return manager2.getEmail();
        }

        return StringUtils.EMPTY;
    }

    public String getTypeOfCompensation(TimeSheetForm tsForm) {
        final DictionaryItem item = dictionaryItemService.find(tsForm.getTypeOfCompensation());

        return (item == null ? StringUtils.EMPTY : item.getValue());
    }

    public String getEffort(TimeSheetForm tsForm) {
        final DictionaryItem item = dictionaryItemService.find(tsForm.getEffortInNextDay());

        return (item == null ? StringUtils.EMPTY : item.getValue());
    }

    public ProjectTaskService getProjectTaskService() {
        return projectTaskService;
    }

    interface ProjectActivityInfo {
        TypesOfActivityEnum getTypeOfActivity();
        ProjectRolesEnum getProjectRole();
        Integer getProjectId();
    }

    public Iterable<ProjectActivityInfo> transformTimeSheetTableRowForm(Iterable<TimeSheetTableRowForm> tsRows) {
        return Iterables.transform(tsRows, new Function<TimeSheetTableRowForm, ProjectActivityInfo>() {
            @Nullable
            @Override
            public ProjectActivityInfo apply(@Nullable final TimeSheetTableRowForm input) {
                return new ProjectActivityInfo() {
                    @Override
                    public TypesOfActivityEnum getTypeOfActivity() {
                        return TypesOfActivityEnum.getById(input.getActivityTypeId());
                    }

                    @Override
                    public ProjectRolesEnum getProjectRole() {
                        return ProjectRolesEnum.getById(input.getProjectRoleId());
                    }

                    @Override
                    public Integer getProjectId() {
                        return input.getProjectId();
                    }
                };
            }
        });
    }

    public Iterable<ProjectActivityInfo> transformTimeSheetDetail(Iterable<TimeSheetDetail> details) {
        return Iterables.transform(details, new Function<TimeSheetDetail, ProjectActivityInfo>() {
            @Nullable
            @Override
            public ProjectActivityInfo apply(@Nullable final TimeSheetDetail input) {
                return new ProjectActivityInfo() {
                    @Override
                    public TypesOfActivityEnum getTypeOfActivity() {
                        return TypesOfActivityEnum.getById(input.getActType().getId());
                    }

                    @Override
                    public ProjectRolesEnum getProjectRole() {
                        if (input.getProjectRole() != null) {
                            return ProjectRolesEnum.getById(input.getProjectRole().getId());
                        }
                        return null;
                    }

                    @Override
                    public Integer getProjectId() {
                        return input.getProject().getId();
                    }
                };
            }
        });
    }

    /**
     * получаем проекты, по которым сотрудник списывал отчеты, по датам отчетов
     */
    public List<Project> getEmployeeProjectsByDates(Date beginDate, Date endDate, Employee employee) {
        return projectService.getEmployeeProjectsFromTimeSheetByDates(beginDate, endDate, employee);
    }
}