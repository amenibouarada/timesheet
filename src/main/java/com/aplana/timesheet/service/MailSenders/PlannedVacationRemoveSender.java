package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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
public class PlannedVacationRemoveSender  extends  AbstractPlannedVacationSender {

    protected static final Logger logger = LoggerFactory.getLogger(VacationDeletedSender.class);

    public PlannedVacationRemoveSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, ProjectService projectService, EmployeeService employeeService) {
        super(sendMailService, propertyProvider);
        this.projectService = projectService;
        this.employeeService = employeeService;
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format(" Оповещение о удалении планирумого отпуска (%s)", this.getClass().getSimpleName());
    }

    @PostConstruct
    public void initPadeg() {
        if (Padeg.setDictionary(propertyProvider.getPathLibraryPadeg())) {
            Padeg.updateExceptions();
        } else
            logger.error("Cannot load exceptions for padeg module");
    }

    @Override
    String getBody(Vacation vacation) {
        StringBuilder stringBuilder = new StringBuilder();

        String employeeNameStr = Padeg.getFIOPadegFS(vacation.getEmployee().getName(), vacation.getEmployee().getSex(), PadegConstants.Roditelnyy);

        stringBuilder.append(String.format(
                "Информируем Вас о удалении планируемого отпуска %s из г. %s на период %s - %s.",
                employeeNameStr,
                vacation.getEmployee().getRegion().getName(),
                DateTimeUtil.formatDateIntoViewFormat(vacation.getBeginDate()),
                DateTimeUtil.formatDateIntoViewFormat(vacation.getEndDate())
        ));

        return stringBuilder.toString();
    }

    @Override
    String getSubject(Vacation vacation) {
        String employeeNameStr = Padeg.getFIOPadegFS(vacation.getEmployee().getName(), vacation.getEmployee().getSex(), PadegConstants.Roditelnyy);

        return String.format(
                "Планируемый отпуск %s за период %s - %s удален",
                employeeNameStr,
                DateTimeUtil.formatDateIntoViewFormat(vacation.getBeginDate()),
                DateTimeUtil.formatDateIntoViewFormat(vacation.getEndDate())
        );
    }
}
