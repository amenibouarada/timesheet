package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.ProjectTask;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.dao.entity.TimeSheetDetail;
import com.aplana.timesheet.enums.*;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.service.OvertimeCauseService;
import com.aplana.timesheet.service.ReportService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.*;

import static com.aplana.timesheet.enums.DictionaryEnum.UNDERTIME_CAUSE;

public class TimeSheetSender extends MailSender<TimeSheetForm> {

    public static final String WORK_PLACE = "workPlace";
    public static final String ACT_TYPE = "actType";
    public static final String PROJECT_NAME = "projectName";
    public static final String CATEGORY_OF_ACTIVITY = "categoryOfActivity";
    public static final String TASK_NAME = "taskName";
    public static final String DURATION = "duration";
    public static final String DESCRIPTION_STRINGS = "descriptionStrings";
    public static final String PROBLEM_STRINGS = "problemStrings";
    public static final String PLAN_STRINGS = "planStrings";
    public static final String REASON = "reason";
    public static final String BEGIN_LONG_DATE = "beginLongDate";
    public static final String END_LONG_DATE = "endLongDate";
    public static final String SENDER_NAME = "senderName";
    public static final String OVERTIME_CAUSE = "overtimeCause";
    public static final String OVERTIME_COMMENT = "overtimeComment";
    public static final String OVERTIME_CAUSE_ID = "overtimeCauseId";
    public static final String TYPE_OF_COMPENSATION = "typeOfCompensation";
    public static final String EFFORT_IN_NEXTDAY = "effortInNextDay";
    public static final String SUMM_DURATION = "summDuration";
    private String employeeEmail;
    private ReportService reportService;
    public TimeSheetSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, OvertimeCauseService overtimeCauseService, ReportService reportService) {
        super(sendMailService, propertyProvider, overtimeCauseService);
        this.reportService = reportService;
        logger.info("Run sending message for: {}", getName());
    }

    String getName() {
        return String.format(" Оповещение о новом отчете о списании занятости (%s)", this.getClass().getSimpleName());
    }

    @Override
    @VisibleForTesting
    InternetAddress initFromAddresses(Mail mail) {
        String fromEmail = employeeEmail != null ? employeeEmail : mail.getFromEmail();
        logger.debug("From Address = {}", fromEmail);
        try {
            return new InternetAddress(fromEmail);
        } catch (MessagingException e) {
            throw new IllegalArgumentException(String.format("Email address %s has wrong format.", fromEmail), e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        Map model = new HashMap();

        model.put("paramsForGenerateBody", mail.getParamsForGenerateBody());
        model.put("undertimeDictId", UNDERTIME_CAUSE.getId());
        model.put("overtimeDictId", DictionaryEnum.OVERTIME_CAUSE.getId());

        logger.info("follows initialization output from velocity");
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "velocity/sendmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(reportService.modifyURL(messageBody), "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(TimeSheetForm params) {
        logger.info("Performing timesheet mailing.");
        Mail mail = new TimeSheetMail();

        /* правила для установки важности письма */
        mail.setPriority(calcPriority(params));

        mail.setToEmails(getToEmails(params));
        mail.setSubject(getSubject(params));
        mail.setParamsForGenerateBody(getBody(params));

        return Arrays.asList(mail);
    }

    private MailPriorityEnum calcPriority(TimeSheetForm params) {
        /* «Моя оценка моего объема работ на следующий рабочий день» = У меня будет мало работы или Я буду перегружен(а) */
        if ( params.getEffortInNextDay() == EffortInNextDayEnum.OVERLOADED.getId() || params.getEffortInNextDay() == EffortInNextDayEnum.UNDERLOADED.getId() ) {
            return MailPriorityEnum.HIGH;
        }
        /* указана переработка по часам */
        if (overtimeCauseService.isOvertimeDuration(params)) {
            return MailPriorityEnum.HIGH;
        }

        if (params.getTimeSheetTablePart() != null) {
            for (TimeSheetTableRowForm form : params.getTimeSheetTablePart()) {
                if (form.getProblem() != null && !form.getProblem().isEmpty()) {
                    return MailPriorityEnum.HIGH;
                }
            }
        }

        return MailPriorityEnum.NORMAL;
    }

    private String getSubject(TimeSheetForm params) {
        return  propertyProvider.getTimesheetMailMarker()+ //APLANATS-571
                " Отчет за " + params.getCalDate();
    }

    private Collection<String> getToEmails(TimeSheetForm params) {
        final Integer employeeId = params.getEmployeeId();

        /*Set<String> toEmails = Sets.newHashSet(Iterables.transform(
                sendMailService.getRegionManagerList(employeeId),
                new Function<Employee, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Employee params) {
                        return params.getEmail();
                    }
                }));*/

        Set<String> toEmails = new HashSet<String>();

        employeeEmail = sendMailService.getEmployeeEmail(employeeId);

        toEmails.add(employeeEmail);
        toEmails.add(sendMailService.getEmployeesManagersEmails(employeeId));
        toEmails.add(sendMailService.getEmployeesAdditionalManagerEmail(employeeId));
        toEmails.add(sendMailService.getProjectsManagersEmails(params));
        toEmails.add(sendMailService.getProjectManagersEmails(params));

        return toEmails;
    }

    private Table<Integer, String, String> getBody(TimeSheetForm tsForm) {
        Table<Integer, String, String> result = HashBasedTable.create();
        int FIRST = 0;

        List<TimeSheetTableRowForm> tsRows = tsForm.getTimeSheetTablePart();

        result.put(FIRST, SENDER_NAME, sendMailService.getSecurityPrincipal().getEmployee().getName());

        if (tsRows != null) {
            for (int i = 0; i < tsRows.size(); i++) {
                TimeSheetTableRowForm tsRow = tsRows.get(i);

                WorkPlacesEnum workPlace = tsRow.getWorkplaceId() != null ? WorkPlacesEnum.getById(tsRow.getWorkplaceId()) : null;
                result.put(i, WORK_PLACE, workPlace != null ? workPlace.getName() : "Неизвестно");

                Integer actTypeId = tsRow.getActivityTypeId();
                result.put(i, ACT_TYPE, TypesOfActivityEnum.getById(actTypeId).getName());

                String projectName = null;
                if ((actTypeId.equals(TypesOfActivityEnum.PROJECT.getId()))
                        ||(actTypeId.equals(TypesOfActivityEnum.PROJECT_PRESALE.getId()))
                        ||(actTypeId.equals(TypesOfActivityEnum.PRESALE.getId()))){
                    Integer projectId = tsRow.getProjectId();
                    if (projectId != null) {
                        result.put(i, PROJECT_NAME, (projectName = sendMailService.getProjectName(projectId)));
                    }
                }
                Integer actCatId = tsRow.getActivityCategoryId();
                if (actCatId != null && actCatId > 0) {
                    result.put(i, CATEGORY_OF_ACTIVITY, CategoriesOfActivityEnum.getById(actCatId).getName());
                }

                ProjectTask projectTask = sendMailService.getProjectTaskService().find(tsRow.getProjectTaskId());
                putIfIsNotBlank(i, result, TASK_NAME, projectTask != null ? projectTask.getTaskName() : null);
                putIfIsNotBlank(i, result, DURATION, tsRow.getDuration());
                putIfIsNotBlank(i, result, DESCRIPTION_STRINGS, tsRow.getDescription());
                putIfIsNotBlank(i, result, PROBLEM_STRINGS, tsRow.getProblem());

            }
        }

        putIfIsNotBlank(FIRST, result, PLAN_STRINGS, tsForm.getPlan());
        putIfIsNotBlank(FIRST, result, OVERTIME_CAUSE, sendMailService.getOvertimeCause(tsForm));
        putIfIsNotBlank(FIRST, result, OVERTIME_COMMENT, StringUtils.isNotBlank(tsForm.getOvertimeCauseComment()) ? tsForm.getOvertimeCauseComment() : null);
        Integer overtimeCauseId = sendMailService.getOverUnderTimeDictId(tsForm.getOvertimeCause());
        putIfIsNotBlank(FIRST, result, OVERTIME_CAUSE_ID, overtimeCauseId != null ? overtimeCauseId.toString() : null);
        putIfIsNotBlank(FIRST, result, TYPE_OF_COMPENSATION, sendMailService.getTypeOfCompensation(tsForm));
        putIfIsNotBlank(FIRST, result, EFFORT_IN_NEXTDAY, sendMailService.getEffort(tsForm));

        TimeSheet timeSheet = sendMailService.findForDateAndEmployee(tsForm.getCalDate(), tsForm.getEmployeeId());
        Iterator<TimeSheetDetail> iteratorTSD = timeSheet.getTimeSheetDetails().iterator();
        BigDecimal summDuration = BigDecimal.ZERO;
        while (iteratorTSD.hasNext()) {
            Double duration = iteratorTSD.next().getDuration();
            if (duration != null)
                summDuration = summDuration.add(BigDecimal.valueOf(duration)); //todo переделать на bigdecimal
        }

        String duration = summDuration.setScale(isIntegerValue(summDuration) ? 0 : 1, BigDecimal.ROUND_HALF_UP).toString();
        putIfIsNotBlank(FIRST, result, SUMM_DURATION, duration);

        return result;
    }

    private void putIfIsNotBlank(int i, Table<Integer,String,String> result, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            result.put(i, key, value);
        }
    }

    private boolean isIntegerValue(BigDecimal bd) {
        return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
    }
}
