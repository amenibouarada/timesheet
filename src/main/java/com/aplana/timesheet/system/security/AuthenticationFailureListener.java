package com.aplana.timesheet.system.security;

import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user abayanov
 * Date: 12.07.13
 */
@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Autowired
    private SendMailService sendMailService;

    final private AtomicInteger GLOBAL_WRONG_REQUEST_COUNTER = new AtomicInteger(1);

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureListener.class);

    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent ev) {
        String username = ev.getAuthentication().getName();
        logger.info("Fialed login : "
                + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())
                + " " + username
                + " " + request.getRemoteAddr());

        if (GLOBAL_WRONG_REQUEST_COUNTER.get() % propertyProvider.getLoginErrorThreshold() == 0) {
            // Отправим сообщение админам
            sendMailService.loginFailureErrorThresholdMailing();
            GLOBAL_WRONG_REQUEST_COUNTER.set(1);
        }
        GLOBAL_WRONG_REQUEST_COUNTER.getAndIncrement();
    }
}

