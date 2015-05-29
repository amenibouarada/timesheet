package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import padeg.lib.Padeg;

public class IllnessCreateSender extends AbstractIllnessSender {

    public IllnessCreateSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, ProjectService projectService, EmployeeService employeeService) {
        super(sendMailService, propertyProvider, projectService, employeeService);
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format(" Оповещения о создании больничного (%s)", this.getClass().getSimpleName());
    }

    @Override
    protected String getSubject(Illness illness) {
        return propertyProvider.getIllnessMailMarker() +
               String.format(" Создан больничный %s", Padeg.getFIOPadegFS(illness.getEmployee().getName(), illness.getEmployee().getSex(), PadegConstants.Roditelnyy));
    }

    @Override
    protected String getBody(Illness illness) {
        String employeeNameStr = Padeg.getFIOPadegFS(illness.getEmployee().getName(), illness.getEmployee().getSex(), PadegConstants.Roditelnyy);
        String regionNameStr = illness.getEmployee().getRegion().getName();
        String beginDateStr = DateTimeUtil.formatDateIntoViewFormat(illness.getBeginDate());
        String endDateStr = DateTimeUtil.formatDateIntoViewFormat(illness.getEndDate());
        String creationDate = DateTimeUtil.formatDateIntoViewFormat(illness.getEditionDate());
        String authorVacation = Padeg.getFIOPadegFS(illness.getAuthor().getName(), illness.getAuthor().getSex(), PadegConstants.Tvoritelnyy);
        String commentStr = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(illness.getComment())) {
            commentStr = String.format("Комментарий: %s. ", illness.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Информируем Вас о больничном "));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s по %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Запись о больничном создана %s. ", authorVacation));
        stringBuilder.append(String.format("Дата создания %s.", creationDate));

        return stringBuilder.toString();
    }
}
