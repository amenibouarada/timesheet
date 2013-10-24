package com.aplana.timesheet.system.properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class TSPropertyProvider {

    private static final Logger logger = LoggerFactory.getLogger(TSPropertyProvider.class);

    private static boolean needUpdate = true;
    private static Properties properties;

    private Integer readIntProperty(String keyName, Integer defaultValue) {
        try {
            return Integer.parseInt(getProperties().getProperty(keyName));
        } catch (NullPointerException ex){
            return defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public String getJiraIssueCreateUrl() {
        return getProperties().getProperty("jira.issue.create.url");
    }

    public String getOqUrl() {
        return getProperties().getProperty("OQ.url");
    }

    public String getPentahoUrl() {
        return getProperties().getProperty("pentaho.url");
    }

    public String getMailTransportProtocol() {
        return getProperties().getProperty("mail.transport.protocol");
    }

    public String getMailProblemsAndProposalsCoaddress(Integer feedbackType) {
        return feedbackType < 6
                ? getProperties().getProperty("mail.ProblemsAndProposals.toaddress")
                : getProperties().getProperty("mail.ProblemsAndProposals.toAdminAddress");
    }

    private static final String DEFAULT_TIME_SHEET_MAIL_ADRESS = "timesheet@aplana.com";
    public static String getMailFromAddress() {
        return getProperties().getProperty("mail.fromaddress", DEFAULT_TIME_SHEET_MAIL_ADRESS);
    }

    public String getMailSendEnable() {
        return getProperties().getProperty("mail.send.enable");
    }

    public String getMailDebugAddress() {
        return  getProperties().getProperty("mail.debug.address");
    }

    public String getMailSmtpPort() {
        return getProperties().getProperty("mail.smtp.port");
    }

    public String getMailSmtpAuth() {
        return getProperties().getProperty("mail.smtp.auth");
    }

    public String getMailUsername() {
        return getProperties().getProperty("mail.username");
    }

    public String getMailPassword() {
        return getProperties().getProperty("mail.password");
    }

    public Integer getQuickreportMoskowBeginDay() {
        return Integer.parseInt(getProperties().getProperty("quickreport.moskow.beginday"));
    }

    public Integer getQuickreportMoskowBeginMonth() {
        return Integer.parseInt(getProperties().getProperty("quickreport.moskow.beginmonth"));
    }

    public Integer getQuickreportRegionsBeginDay() {
        return Integer.parseInt(getProperties().getProperty("quickreport.regions.beginday"));
    }

    public Integer getQuickreportRegionsBeginMonth() {
        return Integer.parseInt(getProperties().getProperty("quickreport.regions.beginmonth"));
    }

    public String getProjectRoleDeveloper() {
        return getProperties().getProperty("project.role.developer");
    }

    public String getProjectRoleRp() {
        return getProperties().getProperty("project.role.rp");
    }

    public String getProjectRoleTest() {
        return getProperties().getProperty("project.role.test");
    }

    public String getProjectRoleAnalyst() {
        return getProperties().getProperty("project.role.analyst");
    }

    public String getProjectRoleSystem() {
        return getProperties().getProperty("project.role.system");
    }

    public Double getOvertimeThreshold() {
        return new Double(getProperties().getProperty("overtime.threshold.overtime", "1"));
    }

    public Double getUndertimeThreshold() {
        return new Double(getProperties().getProperty("overtime.threshold.undertime", "3"));
    }

    public static final String DEFAULT_VACATION_MAIL_MARKER = "[VACATION REQUEST]";
    public String getVacationMailMarker() {
        return getProperties().getProperty("vacation.mail.marker", DEFAULT_VACATION_MAIL_MARKER);
    }

    public static final String DEFAULT_VACATION_CREATE_MAIL_MARKER = "[VACATION CREATE]";
    public String getVacationCreateMailMarker() {
        return getProperties().getProperty("vacation.create.mail.marker", DEFAULT_VACATION_CREATE_MAIL_MARKER);
    }

    public static final String DEFAULT_PLANNED_VACATION_CREATE_MAIL_MARKER = "[VACATION PLAN]";
    public String getPlannedVacationCreateMailMarker() {
        return getProperties().getProperty("vacation.planned.create.mail.marker", DEFAULT_PLANNED_VACATION_CREATE_MAIL_MARKER);
    }

    public static final String DEFAULT_ILLNESS_DEFAULT_MAIL_MARKER = "[ILLNESS]";
    public String getIllnessMailMarker() {
        return getProperties().getProperty("illness.default.mail.marker", DEFAULT_ILLNESS_DEFAULT_MAIL_MARKER);
    }

    final String DEFAULT_TIMESHEET_MAIL_MARKER = "[TIMESHEET]";
    public String getTimesheetMailMarker() {
        return getProperties().getProperty("ts.mail.marker", DEFAULT_TIMESHEET_MAIL_MARKER);
    }

    final Integer DEFAULT_VACATION_APPROVAL_ERROR_THRESHOLD = 100;
    public Integer getVacationApprovalErrorThreshold(){
        return readIntProperty("vacation.approval.error.threshold", DEFAULT_VACATION_APPROVAL_ERROR_THRESHOLD);
    }

    private static final Integer BEFORE_VACATION_DAYS_DEFAULT = 14;
    /**
     * получаем количество дней, которое вычтем из даты создания заявления на отпуск и будем искать для утверждения
     * заявления на отпуск менеджеров проектов, по которым сотрудник списывал занятость в этом промежутке времени
     */
    public Integer getBeforeVacationDays() {
        return readIntProperty("vacations.before.vacation.days", BEFORE_VACATION_DAYS_DEFAULT);
    }

    public static final int VACATION_CREATE_THRESHOLD = 14;
    public Integer getVacationCreateThreshold() {
        return readIntProperty("vacations.vacation.create.threshold", VACATION_CREATE_THRESHOLD);
    }

    public static final int PLANNED_VACATION_CREATE_THRESHOLD = 7;
    public Integer getPlannedVacationCreateThreshold() {
        return readIntProperty("planned.vacations.vacation.create.threshold", PLANNED_VACATION_CREATE_THRESHOLD);
    }

    public static final int VACATION_PROJECT_MANAGER_OVERRIDE_THRESHOLD = 7;
    public Integer getVacationProjectManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.project.manager.override.threshold", VACATION_PROJECT_MANAGER_OVERRIDE_THRESHOLD);
    }

    public static final int VACATION_URGENT_PROJECT_MANAGER_OVERRIDE_THRESHOLD = 3;
    public Integer getVacationUrgentProjectManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.urgent.project.manager.override.threshold", VACATION_URGENT_PROJECT_MANAGER_OVERRIDE_THRESHOLD);
    }

    private static final Integer VACATION_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT = 5;
    /**
     * получаем количество дней, за которые линейный руководитель должен согласовать заявление на отпуск
     * в обычном режиме
     */
    public Integer getVacationLineManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.line.manager.override.threshold", VACATION_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT);
    }

    private static final Integer VACATION_URGENT_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT = 2;
    /**
     * получаем количество дней, за которые линейный руководитель должен согласовать заявление на отпуск
     * в ускоренном режиме
     */
    public Integer getVacationUrgentLineManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.urgent.manager.override.threshold", VACATION_URGENT_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT);
    }

    public String getTimeSheetURL() {
        return getProperties().getProperty("timesheet.url", "http://timesheet.aplana.com");
    }

    public String getFeedbackMarker() {
        return getProperties().getProperty("feedback.mail.marker", "[TS FEEDBACK] Сообщение от пользователя системы списания занятости");
    }

    public List<String> getExceptionsIgnoreClassNames() {
        final String ignoreClassNames = getProperties().getProperty("exceptions.ignoreClassNames", StringUtils.EMPTY);

        return Arrays.asList(ignoreClassNames.split("\\s*,\\s*"));
    }

    public static String getFooterText() {
        return getProperties().getProperty("footer.text");
    }

    public static String getTimesheetHelpUrl() {
        return getProperties().getProperty("timesheet.help.url");
    }

    public static String getVacationRulesUrl() {
        return getProperties().getProperty("vacation.rules.url");
    }

    /**
     * Единый метод для загрузки почтовых настроек
     *
     */
    public static Properties getProperties() {
        if (needUpdate || properties == null) {
            try {
                properties = new Properties();
                properties.load(new FileInputStream( System.getProperty("pathToTsProperties") ));

                needUpdate = false;

                return properties;
            } catch (FileNotFoundException e1) {
                logger.error("File timesheet.properties not found.");
            } catch (InvalidPropertiesFormatException e) {
                logger.error("Invalid timesheet.properties file format.");
            } catch (IOException e) {
                logger.error("Input-output error.");
            }
            throw new IllegalStateException("File with system properties not founded!");
        } else {
            return properties;
        }
    }

    public static void updateProperties() {
        needUpdate = true;
        getProperties();
    }

    public static String getProperiesFilePath() {
        return System.getProperty("pathToTsProperties");
    }

    public String getPathLibraryPadeg() {
        return getProperties().getProperty("path.library.padeg", StringUtils.EMPTY);
    }

    public static final int LOGIN_THRESHOLD = 10;
    public Integer getLoginErrorThreshold() {
            return readIntProperty("application.login.threshold", LOGIN_THRESHOLD);

    }

    /* Количество дней отпуска при показе информ сообщения "Отпуск необходимо оформлять с понедельника по воскресенье" */
    public static final int VACANTION_FRIDAY_INFORM_DAYS = 5;
    public Integer getVacantionFridayInformDays() {
        return readIntProperty("vacations.vacation.friday.warning.threshold", VACANTION_FRIDAY_INFORM_DAYS);
    }

    /* Пользователь в JIRA для получения информации */
    public String getJiraUsername() {
        return getProperties().getProperty("jira.username");
    }

    public String getJiraPassword() {
        return getProperties().getProperty("jira.password");
    }

    public String getJiraServerUrl() {
        return getProperties().getProperty("jira.server.url");
    }

}
