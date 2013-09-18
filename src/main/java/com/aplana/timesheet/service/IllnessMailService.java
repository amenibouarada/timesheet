package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class IllnessMailService {

    private static final Logger logger = LoggerFactory.getLogger(IllnessMailService.class);

    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private IllnessService illnessService;

    /* запускаем оповещение о создании болезни */
    public void sendCreateMail(Illness illness) {
        if (illness != null) {
            /* проверим галочку информирования у подразделения */
            if (illness.getEmployee().getDivision().getTrackingIllness()) {
                Date curDate = DateTimeUtil.stringToDateForDB(DateTimeUtil.currentDay()); // кручу верчу обмануть хочу :-) (можно просто скинуть в дате время)
                Date reportDate = illness.getBeginDate();
                /* проверим что больничный не задним числом */
                if (curDate.compareTo(reportDate) != 1) {
                    sendMailService.performIllnessCreateMailing(illness);
                }
            }
        }
    }

    /* запускаем оповещение об изменении болезни */
    public void sendEditMail(Illness illness) {
        if (illness != null) {
            /* проверим галочку информирования у подразделения */
            if (illness.getEmployee().getDivision().getTrackingIllness()) {
                sendMailService.performIllnessEditMailing(illness);
            }
        }
    }

    /* запускаем оповещение об удалении болезни */
    public void sendDeleteMail(Integer illnessId) {
        if (illnessId != null) {
            sendDeleteMail(illnessService.find(illnessId));
        }
    }
    /* запускаем оповещение об удалении болезни */
    public void sendDeleteMail(Illness illness) {
        if (illness != null) {
            /* проверим галочку информирования у подразделения */
            if (illness.getEmployee().getDivision().getTrackingIllness()) {
                sendMailService.performIllnessDeleteMailing(illness);
            }
        }
    }

}
