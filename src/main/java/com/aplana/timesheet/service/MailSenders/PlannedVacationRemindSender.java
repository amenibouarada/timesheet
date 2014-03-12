package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.LanguageUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.time.DateFormatUtils;
import padeg.lib.Padeg;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author pmakarov
 * @see <a href="http://conf.aplana.com/pages/viewpage.action?pageId=8783552">Алгоритм, выполняемый после создания планируемого отпуска - пункт 1</a>
 */
public class PlannedVacationRemindSender extends AbstractVacationSenderWithCopyToAuthor {
    public PlannedVacationRemindSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
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
        Mail mail = new TimeSheetMail();

        mail.setToEmails(Arrays.asList(vacation.getEmployee().getEmail()));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacation));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Vacation vacation) {
        Table<Integer, String, String> table = HashBasedTable.create();
        table.put(FIRST, MAIL_BODY, getBody(vacation));

        return table;
    }

    private String getBody(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        Integer deleteThreshold = propertyProvider.getPlannedVacationDeleteThreshold();
        Integer deleteReminderThreshold = propertyProvider.getPlannedVacationDeleteReminderThreshold();
        Integer deletePeriod = deleteReminderThreshold - deleteThreshold;

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, deletePeriod);
        String deleteDate = DateFormatUtils.format(c.getTime(), DATE_FORMAT);

        Integer weekReminderCount = deleteReminderThreshold / 7;
        Integer weekDeleteCount = deletePeriod / 7;

        StringBuilder stringBuilder = new StringBuilder();

        if (deleteReminderThreshold % 7 == 0) {
            stringBuilder.append(String.format("Информируем Вас о том, что через %d %s у Вас запланирован отпуск ", weekReminderCount, LanguageUtil.getCaseWeekAccusative(weekReminderCount)));
        } else {
            stringBuilder.append(String.format("Информируем Вас о том, что через %d %s у Вас запланирован отпуск ", deleteReminderThreshold, LanguageUtil.getCaseDay(deleteReminderThreshold)));
        }
        stringBuilder.append(String.format("на период %s - %s. ", beginDateStr, endDateStr));
        stringBuilder.append("Необходимо создать заявление об отпуске! ");
        stringBuilder.append("Если заявление об отпуске уже создано или Вы не хотите идти в отпуск, то просто удалите данный планируемый отпуск. ");

        if (deletePeriod % 7 == 0) {
            stringBuilder.append(String.format("По истечении %d %s (%s) планируемый отпуск автоматически будет удален. ", weekDeleteCount, LanguageUtil.getCaseWeekGenetive(weekDeleteCount), deleteDate));
        } else {
            stringBuilder.append(String.format("По истечении %d %s (%s) планируемый отпуск автоматически будет удален. ", deletePeriod, LanguageUtil.getCaseDay(deletePeriod), deleteDate));
        }

        return stringBuilder.toString();
    }

    private String getSubject(Vacation vacation) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Планируемый отпуск за период: ");
        stringBuilder.append(DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT));
        stringBuilder.append(" - ");
        stringBuilder.append(DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT));

        return stringBuilder.toString();
    }
}
