package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.service.MailSenders.*;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

import static com.aplana.timesheet.enums.TypesOfActivityEnum.*;
import static com.aplana.timesheet.util.ExceptionUtils.getLastCause;

@Service
public class SendMailService {
    private static final Logger logger = LoggerFactory.getLogger(SendMailService.class);

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
    @Qualifier("employeeAssistantService")
    @Autowired
    private EmployeeAssistantService employeeAssistantService;
    @Autowired
    private ProjectTaskService projectTaskService;
    @Autowired
    private ManagerRoleNameService managerRoleNameService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private TSPropertyProvider tsPropertyProvider;
    @Autowired
    private TimeSheetService timeSheetService;

    private final Predicate<ProjectActivityInfo> LEAVE_PRESALE_AND_PROJECTS_ONLY =
            Predicates.and(Predicates.notNull(), new Predicate<ProjectActivityInfo>() {
                @Override
                public boolean apply(@Nullable ProjectActivityInfo projectActivityInfo) {
                    TypesOfActivityEnum actType = projectActivityInfo.getTypeOfActivity();
                    return actType == PROJECT || actType == PRESALE || actType == PROJECT_PRESALE;
                }
            });

    private final Function<ProjectActivityInfo, String> GET_EMAILS_OF_INTERESTED_MANAGERS_FROM_PROJECT_FOR_CURRENT_ROLE
            = new Function<ProjectActivityInfo, String>() {
        @Nullable @Override
        public String apply(@Nullable ProjectActivityInfo input) {
            ProjectRolesEnum roleInCurrentProject = input.getProjectRole();
            List<String> emails = new ArrayList<String>();
            List<ProjectManager> projectManagers = projectService.getManagers(projectService.find(input.getProjectId()));
            for (ProjectManager manager : projectManagers){
                if (manager == null || manager.getProjectRole() == null) {
                    continue;
                }
                // если текущий менеджер - руководитель
                if (manager.getProjectRole().getId().equals(ProjectRolesEnum.HEAD.getId())){
                    emails.add(manager.getEmployee().getEmail());
                }
                // Если роли совпали (менеджера и списывающего), и если менеджер главный (разрабаотчик, тестировщик и т.д.),
                // то добавляем в список адресов на рассылку, т.е.:
                // - если списывает разработчик - то отправляем тимлиду
                // - если списывает аналитик - то отправляем ведущему аналитику проекта
                // - аналогично: главный системный инженер, главный тестировщик
                if  (manager.isMaster() &&
                     manager.getProjectRole().getId().equals(roleInCurrentProject.getId()))
                {
                        emails.add(manager.getEmployee().getEmail());
                }
            }
            return Joiner.on(",").join(emails);
        }
    };

    /**
     * Возвращает строку с адресами линейных руководителей сотрудника
     * (непосредственного и всех вышестоящих) разделёнными запятой.
     *
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
    public String getEmployeeFIO(Integer empId) {
        return empId == null ? null : employeeService.find(empId).getName();
    }

    /**
     * Возвращает email сотрудника
     */
    public String getEmployeeDivision(Integer empId) {
        Division division = employeeService.find(empId).getDivision();
        return empId == null ? null : division == null ? null : division.getName();
    }

    /**
     * Возвращает строку с адресами менеджеров проектов/пресейлов
     *
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
     *
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

    public void performMailing(TimeSheetForm form) {
        new TimeSheetSender(this, propertyProvider, overtimeCauseService, reportService).sendMessage(form);
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
        new VacationDeletedSender(this, propertyProvider, projectService, employeeService).sendMessage(vacation);
    }

    public void performVacationApproveRequestSender(VacationApproval vacationApproval) {
        new VacationApproveRequestSender(this, propertyProvider, vacationApprovalService, managerRoleNameService).sendMessage(vacationApproval);
    }

    public void performPlannedVacationCreateRequestSender(Vacation vacation, List<String> emails) {
        new PlannedVacationCreateSender(this, propertyProvider, emails).sendMessage(vacation);
    }

    public void performPlannedVacationDeletedMailing(Vacation vacation) {
        new PlannedVacationDeletedSender(this, propertyProvider, employeeService, projectService).sendMessage(vacation);
    }

    public void performVacationApprovedSender(Vacation vacation, List<String> emails) {
        new VacationApprovedSender(this, propertyProvider, emails).sendMessage(vacation);
    }

    public void performExceptionSender(String problem) {
        new ExceptionSender(this, propertyProvider).sendMessage(problem);
    }

    public void performVacationApprovalErrorThresholdMailing() {
        new VacationApprovalErrorThresholdSender(this, propertyProvider).sendMessage("");
    }

    public void loginFailureErrorThresholdMailing() {
        new LoginFailureErrorThresholdSender(this, propertyProvider).sendMessage("");
    }

    public void performVacationAcceptanceMailing(VacationApproval vacationApproval) {
        new VacationApprovalAcceptanceSender(this, propertyProvider).sendMessage(vacationApproval);
    }

    public void performVacationCreateMailing(Vacation vacation) {
        new VacationCreateSender(this, propertyProvider, vacationApprovalService, managerRoleNameService).sendMessage(vacation);
    }

    public void plannedVacationInfoMailing(Map<Employee, Set<Vacation>> managerEmployeesVacation) {
        new PlannedVacationInfoSender(this, propertyProvider).sendMessage(managerEmployeesVacation);
    }

    public void performIllnessCreateMailing(Illness illness) {
        new IllnessCreateSender(this, propertyProvider, projectService, employeeService).sendMessage(illness);
    }

    public void performIllnessEditMailing(Illness illness) {
        new IllnessEditSender(this, propertyProvider, projectService, employeeService).sendMessage(illness);
    }

    public void performIllnessDeleteMailing(Illness illness) {
        new IllnessDeleteSender(this, propertyProvider, projectService, employeeService).sendMessage(illness);
    }

    public void performPlannedRemind(Vacation vacation) {
        new PlannedVacationRemindSender(this, propertyProvider).sendMessage(vacation);
    }

    public void performPlannedRemove(Vacation vacation){
        new PlannedVacationRemoveSender(this, propertyProvider, projectService, employeeService).sendMessage(vacation);
    }

    public void performDeleteOrSetDraftApproval(TimeSheet timeSheet){
        new DeleteOrSetDraftApprovalSender(this, propertyProvider).sendMessage(timeSheet);
    }

    public void performNotificationOnExportReportComplete(ReportExportStatus reportExportStatus){
        new ExportReportCompleteSender(this, propertyProvider).sendMessage(reportExportStatus);
    }

    public StringBuilder buildMailException(HttpServletRequest request, Exception exception){
        Map<String, Object> model = new HashMap<String, Object>();

        // При MaxUploadSizeExceededException не нужно отправлять письмо админу
        if (exception instanceof MaxUploadSizeExceededException) {
            return null;
        }

        try {
            if (!tsPropertyProvider.getExceptionsIgnoreClassNames().
                    contains(getLastCause(exception).getClass().getName())
                    ) {
                model.put("errors", "Unexpected error: " + exception.getMessage());
                // получим ФИО пользователя
                String FIO = "<не определен>";
                TimeSheetUser securityUser = null;
                try {
                    securityUser = securityService.getSecurityPrincipal();
                } catch (NullPointerException e) {}
                if (securityUser != null) {
                    int employeeId = securityUser.getEmployee().getId();
                    FIO = employeeService.find(employeeId).getName();
                }
                // Отправим сообщение админам
                StringBuilder sb = new StringBuilder();
                sb.append("У пользователя ").append(FIO).append(" произошла следующая ошибка:<br>");
                sb.append(exception.getMessage() != null ? exception.getMessage() : getLastCause(exception).getClass().getName());
                sb.append("<br><br>");

                StringBuilder sbtemp = new StringBuilder();
                Map parameterMap = request.getParameterMap();
                Iterator iterator = parameterMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry) iterator.next();
                    sbtemp.append(mapEntry.getKey() + " : " + Arrays.toString((String[])mapEntry.getValue()) + "<br>");
                }

                if (sbtemp.length()>0) {
                    sb.append("<br><br>");
                    sb.append("Параметры запроса: ");
                    sb.append(sbtemp.toString());
                }

                sb.append("Stack trace: <br>");
                sb.append(Arrays.toString(exception.getStackTrace()));
                return sb;
            }
        } finally {
            // Выведем в лог
            logger.error("Произошла неожиданная ошибка:", exception);
        }
        return null;
    }

    public String initMessageBodyForReport(TimeSheet timeSheet) {
        Map<String, Object> model1 = new HashMap<String, Object>();
        Iterator<TimeSheetDetail> iteratorTSD = timeSheet.getTimeSheetDetails().iterator();
        BigDecimal summDuration = BigDecimal.ZERO;
        while (iteratorTSD.hasNext()) {
            Double duration = iteratorTSD.next().getDuration();
            if (duration != null)
                summDuration = summDuration.add(BigDecimal.valueOf(duration)); //todo переделать на bigdecimal
        }
        model1.put("summDuration", summDuration.setScale(2, BigDecimal.ROUND_HALF_UP));
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
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "velocity/report.vm", model);
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

    public Integer getOverUnderTimeDictId(Integer overtimeCause) {
        return overtimeCauseService.getDictId(overtimeCause);
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId) {
        return vacationApprovalService.getVacationApprovalEmailList(vacationId);
    }

    public List<EmployeeAssistant> getEmployeeAssistant(Set<String> managersEmails) {
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

            if (project != null && project.getManager() != null &&
                    TypesOfActivityEnum.isProjectOrPresale(
                            EnumsUtils.tryFindById(timeSheetDetail.getActType().getId(), TypesOfActivityEnum.class)
                    )) {
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
                        if (input != null && input.getActivityTypeId() != null) {
                            return TypesOfActivityEnum.getById(input.getActivityTypeId());
                        }
                        return null;
                    }

                    @Override
                    public ProjectRolesEnum getProjectRole() {
                        if (input != null && input.getProjectRoleId() != null) {
                            return ProjectRolesEnum.getById(input.getProjectRoleId());
                        }
                        return null;
                    }

                    @Override
                    public Integer getProjectId() {
                        if (input != null && input.getProjectId() != null) {
                            return input.getProjectId();
                        }
                        return null;
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
                        if (input != null && input.getActType() != null) {
                            return TypesOfActivityEnum.getById(input.getActType().getId());
                        }
                        return null;
                    }

                    @Override
                    public ProjectRolesEnum getProjectRole() {
                        if (input != null && input.getProjectRole() != null) {
                            return ProjectRolesEnum.getById(input.getProjectRole().getId());
                        }
                        return null;
                    }

                    @Override
                    public Integer getProjectId() {
                        if (input != null && input.getProject() != null) {
                            return input.getProject().getId();
                        }
                        return null;
                    }
                };
            }
        });
    }

    @Transactional(readOnly = true)
    public TimeSheet findForDateAndEmployee(String calDate, Integer employeeId) {
        return timeSheetService.findForDateAndEmployee(calDate, employeeId);
    }

}