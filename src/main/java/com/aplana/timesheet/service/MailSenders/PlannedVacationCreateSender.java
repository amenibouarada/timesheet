package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.constants.PadegConstants;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import padeg.lib.Padeg;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by user
 * User: abayanov
 * Date: 01.07.13
 * Time: 11:15
 */
public class PlannedVacationCreateSender extends AbstractVacationSenderWithCopyToAuthor {

    private static final Logger logger = LoggerFactory.getLogger(PlannedVacationCreateSender.class);

    private final List<String> emails;

    public PlannedVacationCreateSender(SendMailService sendMailService, TSPropertyProvider propertyProvider,
                                       List<String> emails) {
        super(sendMailService, propertyProvider);
        this.emails = emails;
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

        mail.setToEmails(emails);

        mail.setSubject(getSubject(vacation));

        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacation));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Vacation vacation) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(vacation));

        return table;
    }

    private String getBody(Vacation vacation) {
        String employeeNameStr = Padeg.getFIOPadegFS(vacation.getEmployee().getName(), true, PadegConstants.Roditelnyy);
        String regionNameStr = vacation.getEmployee().getRegion().getName();
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);
        String creationDate = DateFormatUtils.format(vacation.getCreationDate(), DATE_FORMAT);
        String authorVacation = Padeg.getFIOPadegFS(vacation.getAuthor().getName(), true, PadegConstants.Tvoritelnyy);
        String commentStr = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(vacation.getComment())) {
            commentStr = String.format("Комментарий: %s. ", vacation.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Информируем Вас о планируемом отпуске "));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s по %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Запись о планируемом отпуске создана %s. Дата создания %s.",authorVacation,creationDate));

        return stringBuilder.toString();
    }

    private String getSubject(Vacation vacation) {
        return  propertyProvider.getPlannedVacationCreateMailMarker()+
                String.format(" Планируемый отпуск %s", Padeg.getFIOPadegFS(vacation.getEmployee().getName(), true, PadegConstants.Roditelnyy));
    }
}
