package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import padeg.lib.Padeg;

import java.util.Date;

public class IllnessDeleteSender extends AbstractIllnessSender {
    public IllnessDeleteSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, ProjectService projectService, EmployeeService employeeService) {
        super(sendMailService, propertyProvider, projectService, employeeService);
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format(" Оповещения об удалении больничного (%s)", this.getClass().getSimpleName());
    }

    final Employee curUser = sendMailService.getSecurityPrincipal().getEmployee();

    @Override
    protected String getSubject(Illness illness) {
        return propertyProvider.getIllnessMailMarker() +
                String.format(" Удален больничный %s", Padeg.getFIOPadegFS(illness.getEmployee().getName(), illness.getEmployee().getSex(), PadegConstants.Roditelnyy));
    }

    @Override
    protected String getBody(Illness illness) {
        String employeeNameStr = Padeg.getFIOPadegFS(illness.getEmployee().getName(), illness.getEmployee().getSex(), PadegConstants.Roditelnyy);
        String regionNameStr = illness.getEmployee().getRegion().getName();
        String beginDateStr = DateTimeUtil.formatDateIntoViewFormat(illness.getBeginDate());
        String endDateStr = DateTimeUtil.formatDateIntoViewFormat(illness.getEndDate());
        String deletedDate = DateTimeUtil.formatDateIntoViewFormat(new Date());
        String authorVacation = Padeg.getFIOPadegFS(curUser.getName(), curUser.getSex(), PadegConstants.Tvoritelnyy);
        String commentStr = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(illness.getComment())) {
            commentStr = String.format("Комментарий: %s. ", illness.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Информируем Вас об удалении больничного "));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s по %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Запись о больничном удалена %s. ", authorVacation));
        stringBuilder.append(String.format("Дата удаления %s.", deletedDate));

        return stringBuilder.toString();
    }
}
