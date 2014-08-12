package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.system.constants.PadegConstants;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import padeg.lib.Padeg;

public class IllnessEditSender extends AbstractIllnessSender{
    public IllnessEditSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, ProjectService projectService, EmployeeService employeeService) {
        super(sendMailService, propertyProvider, projectService, employeeService);
        logger.info("Run sending message for: {}", getName());
    }

    String getName() {
        return String.format(" Оповещения о редактировании больничного (%s)", this.getClass().getSimpleName());
    }

    @Override
    protected String getSubject(Illness illness) {
        return propertyProvider.getIllnessMailMarker() +
                String.format(" Отредактирован больничный %s", Padeg.getFIOPadegFS(illness.getEmployee().getName(), illness.getEmployee().getSex(), PadegConstants.Roditelnyy));
    }

    @Override
    protected String getBody(Illness illness) {
        String employeeNameStr = Padeg.getFIOPadegFS(illness.getEmployee().getName(), illness.getEmployee().getSex(), PadegConstants.Roditelnyy);
        String regionNameStr = illness.getEmployee().getRegion().getName();
        String beginDateStr = DateTimeUtil.formatDateIntoViewFormat(illness.getBeginDate());
        String endDateStr = DateTimeUtil.formatDateIntoViewFormat(illness.getEndDate());
        String editionDate = DateTimeUtil.formatDateIntoViewFormat(illness.getEditionDate());
        String authorVacation = Padeg.getFIOPadegFS(illness.getAuthor().getName(), illness.getAuthor().getSex(), PadegConstants.Tvoritelnyy);
        String commentStr = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(illness.getComment())) {
            commentStr = String.format("Комментарий: %s. ", illness.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Информируем Вас о редактировании больничного "));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s по %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Запись о больничном отредактирована %s. ", authorVacation));
        stringBuilder.append(String.format("Дата редактирования %s.", editionDate));

        return stringBuilder.toString();
    }
}
