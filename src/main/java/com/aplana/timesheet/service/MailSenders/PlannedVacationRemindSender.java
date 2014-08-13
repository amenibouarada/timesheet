package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.LanguageUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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
        logger.info("Run sending message for: {}", getName());
    }

    final String getName() {
        return String.format("Оповещение о запланированном отпуске сотрудника (%s)", this.getClass().getSimpleName());
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
        String beginDateStr = DateTimeUtil.formatDateIntoViewFormat(vacation.getBeginDate());
        String endDateStr =DateTimeUtil.formatDateIntoViewFormat(vacation.getEndDate());

        // Количество дней от момента удаления до планируемого начала отпуска
        Integer deleteThreshold = propertyProvider.getPlannedVacationDeleteThreshold();

        // Дата удаления
        Calendar c = Calendar.getInstance();
        c.setTime(vacation.getBeginDate());
        c.add(Calendar.DATE, -deleteThreshold);
        Date deleteDate = c.getTime();
        String deleteDateStr = DateTimeUtil.formatDateIntoViewFormat(deleteDate);

        // Количество дней от текущего дня до начала планируемого отпуска
        c.setTime(new Date());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int hoursInDay = 24;
        int minInHour = 60;
        int secInHour = 60;
        int millisec = 1000;
        int deleteReminderThreshold = ((int)(vacation.getBeginDate().getTime() - c.getTime().getTime()))/(hoursInDay * minInHour * secInHour * millisec);

        // Количество дней от текущего дня до удаления
        Integer deletePeriod = deleteReminderThreshold - deleteThreshold;

        StringBuilder stringBuilder = new StringBuilder();

        String format = "Информируем Вас о том, что через %d %s у Вас запланирован отпуск ";
        int weekDayCount = 7;
        if (deleteReminderThreshold % weekDayCount == 0) {
            Integer weekReminderCount = deleteReminderThreshold / weekDayCount;
            stringBuilder.append(String.format(format, weekReminderCount, LanguageUtil.getCaseWeekAccusative(weekReminderCount)));
        } else {
            stringBuilder.append(String.format(format, deleteReminderThreshold, LanguageUtil.getCaseDayAccusative(deleteReminderThreshold)));
        }
        stringBuilder.append(String.format("на период %s - %s. ", beginDateStr, endDateStr));
        stringBuilder.append("Необходимо создать заявление об отпуске! ");
        stringBuilder.append("Если заявление об отпуске уже создано или Вы не хотите идти в отпуск, то просто удалите данный планируемый отпуск. ");

        String format1 = "По истечении %d %s (%s) планируемый отпуск автоматически будет удален. ";
        if (deletePeriod % weekDayCount == 0) {
            Integer weekDeleteCount = deletePeriod / weekDayCount;
            stringBuilder.append(String.format(format1, weekDeleteCount, LanguageUtil.getCaseWeekGenetive(weekDeleteCount), deleteDateStr));
        } else {
            stringBuilder.append(String.format(format1, deletePeriod, LanguageUtil.getCaseDayGenetive(deletePeriod), deleteDateStr));
        }

        return stringBuilder.toString();
    }

    private String getSubject(Vacation vacation) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Планируемый отпуск за период: ");
        stringBuilder.append(DateTimeUtil.formatDateIntoViewFormat(vacation.getBeginDate()));
        stringBuilder.append(" - ");
        stringBuilder.append(DateTimeUtil.formatDateIntoViewFormat(vacation.getEndDate()));

        return stringBuilder.toString();
    }
}
