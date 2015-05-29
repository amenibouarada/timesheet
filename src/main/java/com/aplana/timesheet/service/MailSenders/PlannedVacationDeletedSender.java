package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PlannedVacationDeletedSender extends AbstractPlannedVacationSender {

    protected static final Logger logger = LoggerFactory.getLogger(PlannedVacationDeletedSender.class);

    public PlannedVacationDeletedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, EmployeeService employeeService, ProjectService projectService) {
        super(sendMailService, propertyProvider);
        this.employeeService = employeeService;
        this.projectService = projectService;
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format(" Оповещение об удаленном отпуске сотрудника (%s)", this.getClass().getSimpleName());
    }

    @Override
    String getSubject(Vacation params) {
        return  String.format(" Удален планируемый отпуск у сотрудника %s", params.getEmployee().getName());
    }

    @Override
    String getBody(Vacation params) {
        final StringBuilder stringBuilder = new StringBuilder(
                String.format("Сотрудник \"%s\" удалил ", sendMailService.getSecurityPrincipal().getEmployee().getName())
        );

        final Employee employee = params.getEmployee();
        final Employee curUser = sendMailService.getSecurityPrincipal().getEmployee();

        if (params.getEmployee().equals(curUser)) {
            stringBuilder.append("своё заявление");
        } else {
            stringBuilder.append(
                    String.format("заявление сотрудника \"%s\"", employee.getName())
            );
        }

        stringBuilder.append(
                String.format(
                        " на %s за период с %s по %s",
                        WordUtils.uncapitalize(params.getType().getValue()),
                        DateTimeUtil.formatDateIntoViewFormat(params.getBeginDate()),
                        DateTimeUtil.formatDateIntoViewFormat(params.getEndDate())
                )
        );

        return stringBuilder.toString();
    }
}
