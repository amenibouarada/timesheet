package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.MailPriorityEnum;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @author eshangareev
 * @version 1.0
 */
public abstract class Mail {

    private static Set<String> clearDuplicates(Iterable<String> emails) {
        if (emails == null) {
            return null;
        }

        final Set<String> uniqueEmails = Sets.newHashSet();

        for (String email : emails) {
            uniqueEmails.addAll(Arrays.asList(email.split("\\s*,\\s*")));
        }

        return uniqueEmails;
    }

    private Division division;
    private Iterable<String> ccEmails = new ArrayList<String>();
    private String subject;

    private Iterable<String> toEmails = new ArrayList<String>();

    private Iterable<Employee> employeeList;
    private Map<Employee, List<String>> passedDays = new HashMap<Employee, List<String>>();
    private Iterable<MultipartFile> filePahts;
    private String preconstructedMessageBody;
    private String date;

    private Table<Integer, String, String> paramsForGenerateBody;

    /* важность письма 1 = high, 3 = normal, 5 = low */
    private MailPriorityEnum priority = MailPriorityEnum.NORMAL;

    public abstract String getFromEmail();

    public Division getDivision() {
        return division;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Iterable<String> getCcEmails() {
        return ccEmails;
    }

    public void setCcEmails(Iterable<String> ccEmails) {
        if (ccEmails != null) {
            this.ccEmails = clearDuplicates(ccEmails); //удаляем дубликаты;
        }
    }

    public Iterable<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(Iterable<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public Map<Employee, List<String>> getPassedDays() {
        return passedDays;
    }

    public Iterable<MultipartFile> getFilePahts() {
        return filePahts;
    }

    public void setFilePahts(Iterable<MultipartFile> filePahts) {
        this.filePahts = filePahts;
    }

    public String getPreconstructedMessageBody() {
        return preconstructedMessageBody;
    }

    public void setPreconstructedMessageBody(String preconstructedMessageBody) {
        this.preconstructedMessageBody = preconstructedMessageBody;
    }

    public Iterable<String> getToEmails() {
        return toEmails;
    }

    public void setToEmails(Iterable<String> toEmails) {
        //удаляем дупликаты
        this.toEmails = clearDuplicates(toEmails);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setParamsForGenerateBody(Table<Integer, String, String> paramsForGenerateBody) {
        this.paramsForGenerateBody = paramsForGenerateBody;
    }

    public Table<Integer, String, String> getParamsForGenerateBody() {
        return paramsForGenerateBody;
    }

    public MailPriorityEnum getPriority() {
        return priority;
    }

    public void setPriority(MailPriorityEnum priority) {
        this.priority = priority;
    }
}
