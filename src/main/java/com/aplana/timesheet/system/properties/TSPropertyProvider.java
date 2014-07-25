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

    /**
     * Единый метод для загрузки почтовых настроек
     */
    public static Properties getProperties() {
        if (needUpdate || properties == null) {
            try {
                properties = new Properties();
                properties.load(new FileInputStream(System.getProperty("pathToTsProperties")));

                needUpdate = false;

                return properties;
            } catch (FileNotFoundException e1) {
                logger.error("File timesheet.properties not found.");
            } catch (InvalidPropertiesFormatException e) {
                logger.error("Invalid timesheet.properties file format.");
            } catch (IOException e) {
                logger.error("Input-output error.");
            }
            throw new IllegalStateException("System properties file not found!");
        } else {
            return properties;
        }
    }

    public static void updateProperties() {
        needUpdate = true;
        getProperties();
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

    public static String getMailFromAddress() {
        return getProperties().getProperty("mail.fromaddress", "timesheet@aplana.com");
    }

    public String getMailAdminsAddress() {
        return getProperties().getProperty("mail.ProblemsAndProposals.toAdminAddress");
    }

    public String getMailSendEnable() {
        return getProperties().getProperty("mail.send.enable");
    }

    public String getMailDebugAddress() {
        return getProperties().getProperty("mail.debug.address");
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
        return readIntProperty("quickreport.moskow.beginday", 1);
    }

    public Integer getQuickreportMoskowBeginMonth() {
        return readIntProperty("quickreport.moskow.beginmonth", 4);
    }

    public Integer getQuickreportRegionsBeginDay() {
        return readIntProperty("quickreport.regions.beginday", 1);
    }

    public Integer getQuickreportRegionsBeginMonth() {
        return readIntProperty("quickreport.regions.beginmonth", 9);
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

    public String getVacationMailMarker() {
        return getProperties().getProperty("mail.marker.vacation", "[VACATION REQUEST]");
    }

    public String getVacationCreateMailMarker() {
        return getProperties().getProperty("mail.marker.vacation.create", "[VACATION CREATE]");
    }

    public String getPlannedVacationCreateMailMarker() {
        return getProperties().getProperty("mail.marker.vacation.planned", "[VACATION PLAN]");
    }

    public String getIllnessMailMarker() {
        return getProperties().getProperty("mail.marker.illness", "[ILLNESS]");
    }

    public String getTimesheetMailMarker() {
        return getProperties().getProperty("mail.marker.ts", "[TIMESHEET]");
    }

    public String getFeedbackMarker() {
        return getProperties().getProperty("mail.marker.feedback", "[TS FEEDBACK]");
    }

    public String getDeleteOrSetDraftApprovalMarker() {
        return getProperties().getProperty("mail.marker.deleteOrSetDraft", "[TS DELETE OR SET DRAFT]");
    }

    public Integer getVacationApprovalErrorThreshold() {
        return readIntProperty("vacation.approval.error.threshold", 100);
    }

    /**
     * получаем количество дней, которое вычтем из даты создания заявления на отпуск и будем искать для утверждения
     * заявления на отпуск менеджеров проектов, по которым сотрудник списывал занятость в этом промежутке времени
     */
    public Integer getBeforeVacationDays() {
        return readIntProperty("vacations.before.vacation.days", 14);
    }

    public Integer getVacationCreateThreshold() {
        return readIntProperty("vacations.vacation.create.threshold", 7);
    }

    public Integer getPlannedVacationCreateThreshold() {
        return readIntProperty("planned.vacations.vacation.create.threshold", 7);
    }

    public Integer getVacationProjectManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.project.manager.override.threshold", 7);
    }

    public Integer getVacationUrgentProjectManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.urgent.project.manager.override.threshold", 3);
    }

    /**
     * получаем количество дней, за которые линейный руководитель должен согласовать заявление на отпуск
     * в обычном режиме
     */
    public Integer getVacationLineManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.line.manager.override.threshold", 5);
    }

    /**
     * получаем количество дней, за которые линейный руководитель должен согласовать заявление на отпуск
     * в ускоренном режиме
     */
    public Integer getVacationUrgentLineManagerOverrideThreshold() {
        return readIntProperty("vacations.vacation.urgent.manager.override.threshold", 2);
    }

    public String getTimeSheetURL() {
        return getProperties().getProperty("timesheet.url", "http://timesheet.aplana.com");
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

    public static String getProperiesFilePath() {
        return System.getProperty("pathToTsProperties");
    }

    public String getPathLibraryPadeg() {
        return getProperties().getProperty("path.library.padeg", StringUtils.EMPTY);
    }

    public Integer getLoginErrorThreshold() {
        return readIntProperty("application.login.threshold", 10);
    }

    /* Количество дней отпуска при показе информ сообщения "Отпуск необходимо оформлять с понедельника по воскресенье" */
    public Integer getVacantionFridayInformDays() {
        return readIntProperty("vacations.vacation.friday.warning.threshold", 5);
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

    private Integer readIntProperty(String keyName, Integer defaultValue) {
        try {
            return Integer.parseInt(getProperties().getProperty(keyName));
        } catch (NullPointerException ex) {
            return defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Количество дней перед удалением планируемого отпуска
     * @return
     */
    public Integer getPlannedVacationDeleteThreshold() {
        String str = getProperties().getProperty("planned.vacations.vacation.delete.threshold");
        return Integer.parseInt(str);
    }

    /**
     * Количество дней перед предупреждением, что планируемый отпуск будет удален
     * @return
     */
    public Integer getPlannedVacationDeleteReminderThreshold() {
        String str = getProperties().getProperty("planned.vacations.vacation.delete.reminder.threshold");
        return Integer.parseInt(str);
    }

    public Integer getReportsOverdueThreshold() {
        String str = getProperties().getProperty("reports.overdue.threshold");
        return Integer.parseInt(str);
    }

    /**
     * Имя поля, используемого для определения параметра Department сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForDivision() {
        return getProperties().getProperty("ldap.field.division", "department");
    }

    /**
     * Имя поля, используемого для определения параметра DisplayName сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForDisplayName() {
        return getProperties().getProperty("ldap.field.displayName", "displayName");
    }

    /**
     * Имя поля, используемого для определения параметра Email сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForEmail() {
        return getProperties().getProperty("ldap.field.email", "mail");
    }

    /**
     * Имя поля, используемого для определения параметра Manager сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForManager() {
        return getProperties().getProperty("ldap.field.manager", "manager");
    }

    /**
     * Имя поля, используемого для определения параметра Title сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForTitle() {
        return getProperties().getProperty("ldap.field.title", "title");
    }

    /**
     * Имя поля, используемого для определения параметра Title сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForWhenCreated() {
        return getProperties().getProperty("ldap.field.whenCreated", "whenCreated");
    }

    /**
     * Имя поля, используемого для определения параметра City сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForCity() {
        return getProperties().getProperty("ldap.field.city", "l");
    }

    /**
     * Имя поля, используемого для определения параметра MailNickname сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForMailNickname() {
        return getProperties().getProperty("ldap.field.mailNickname", "mailNickname");
    }

    /**
     * Имя поля, используемого для определения параметра LdapCn сотрудника
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForLdapCn() {
        return getProperties().getProperty("ldap.field.ldapCn", "distinguishedname");
    }

    /**
     * Имя поля, используемого для определения класса объекта
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForObjectClass() {
        return getProperties().getProperty("ldap.field.objectClass", "objectClass");
    }

    /**
     * Имя поля, используемого для определения параметра SID
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForSID() {
        return getProperties().getProperty("ldap.field.SID", "objectSid");
    }

    /**
     * Имя поля, используемого для определения параметра DivisionName
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForDivisionName() {
        return getProperties().getProperty("ldap.field.divisionName", "description");
    }

    /**
     * Имя поля, используемого для определения параметра Leader
     * при синхронизации с помощью LDAP.
     */
    public String getLdapFieldForLeader() {
        return getProperties().getProperty("ldap.field.leader", "managedBy");
    }

    /**
     * Класс объекта для нахождения активных сотрудников.
     */
    public String getLdapObjectClassEmployee() {
        return getProperties().getProperty("ldap.objectClass.employee", "user");
    }

    /**
     * Класс объекта для нахождения неактивных сотрудников.
     */
    public String getLdapObjectClassDisabledEmployee() {
        return getProperties().getProperty("ldap.objectClass.disabledEmployee", "person");
    }

    /**
     * Класс объекта для нахождения неактивных сотрудников.
     */
    public String getLdapObjectClassDivision() {
        return getProperties().getProperty("ldap.objectClass.division", "group");
    }

    /**
     * Значение OU для нахождения неактивных сотрудников.
     */
    public String getLdapOuDisabledEmployee() {
        return getProperties().getProperty("ldap.ou.disabledEmployee", "Disabled Users");
    }

    /**
     * Значение CN для нахождения подразделений.
     */
    public String getLdapCnDivision() {
        return getProperties().getProperty("ldap.cn.division", "_Project Center *");
    }
}
