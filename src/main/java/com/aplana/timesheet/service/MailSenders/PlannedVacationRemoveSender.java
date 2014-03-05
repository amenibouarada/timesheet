package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import padeg.lib.Padeg;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author pmakarov
 * @see <a href="http://conf.aplana.com/pages/viewpage.action?pageId=8783552">Алгоритм, выполняемый после создания планируемого отпуска - пункт 2</a>
 *      creation date: 04.03.14
 */
public class PlannedVacationRemoveSender  extends  AbstractVacationSenderWithCopyToAuthor {

    protected static final Logger logger = LoggerFactory.getLogger(VacationDeletedSender.class);
    ProjectService projectService;
    EmployeeService employeeService;

    public PlannedVacationRemoveSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, ProjectService projectService, EmployeeService employeeService) {
        super(sendMailService, propertyProvider);
        this.projectService = projectService;
        this.employeeService = employeeService;
    }

    @PostConstruct
    public void initPadeg() {
        if (Padeg.setDictionary(propertyProvider.getPathLibraryPadeg())) {
            Padeg.updateExceptions();
        } else
            logger.error("Cannot load exceptions for padeg module");
    }

    @Override
    public List<Mail> getMainMailList(Vacation vacation) {

        final Mail mail = new TimeSheetMail();
        final Set<String> emails = new HashSet<String>();
        final Set<String> ccEmails = new HashSet<String>();

        Employee employee = vacation.getEmployee();
        /* добавляем руководителей проектов */
        final List<Project> projects = projectService.getProjectsForVacation(vacation);
        for (Project project : projects) {
            emails.add(project.getManager().getEmail());
        }

        /* делаем список линейных рук для оповещения */
        final List<Employee> linearEmployees = employeeService.getLinearEmployees(employee);
        for (Employee item : linearEmployees) {
            emails.add(item.getEmail());
        }

        /* добавляем доп руководителя */
        Employee manager2 = vacation.getEmployee().getManager2();
        if (manager2 != null) {
            emails.add(manager2.getEmail());
        }

        /* оповещаем отпускника и удалителя */
        if (!emails.contains(vacation.getEmployee().getEmail())) {
            ccEmails.add(vacation.getEmployee().getEmail());
        }
        if (!emails.contains(vacation.getAuthor().getEmail())) {
            ccEmails.add(vacation.getAuthor().getEmail());
        }

        //оповещаем центр
        if (employee.getDivision()!=null) {
            ccEmails.add(employee.getDivision().getVacationEmail());
        }

        mail.setToEmails(getNotBlankEmails(emails));
        mail.setCcEmails(getNotBlankEmails(ccEmails));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacation));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Vacation params) {
        Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(params));

        return table;
    }

    private String getBody(Vacation vacation) {
        StringBuilder stringBuilder = new StringBuilder();

        String employeeNameStr = Padeg.getFIOPadegFS(vacation.getEmployee().getName(), true, PadegConstants.Roditelnyy);

        stringBuilder.append(String.format(
                "Информируем Вас о удалении планируемого отпуска %s из г. %s на период %s - %s.",
                employeeNameStr,
                vacation.getEmployee().getRegion().getName(),
                DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT),
                DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT)
        ));

        return stringBuilder.toString();
    }

    private String getSubject(Vacation vacation) {
        String employeeNameStr = Padeg.getFIOPadegFS(vacation.getEmployee().getName(), true, PadegConstants.Roditelnyy);

        return String.format(
                "Планируемый отпуск %s %s - %s удален",
                employeeNameStr,
                DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT),
                DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT)
        );
    }
}
