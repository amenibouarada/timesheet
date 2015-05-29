package com.aplana.timesheet.controller;

import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.aplana.timesheet.util.ExceptionUtils.getLastCause;

@Controller
public class ExceptionHandler implements HandlerExceptionResolver {

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private TSPropertyProvider tsPropertyProvider;

    protected static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception exception) {
        Map<String, Object> model = new HashMap<String, Object>();

        // При MaxUploadSizeExceededException не нужно отправлять письмо админу
        if (exception instanceof MaxUploadSizeExceededException) {
            model.put("exceptionText", "error.feedback.maxsize");
            return new ModelAndView("redirect:feedback", model);
        }

        StringBuilder sb = sendMailService.buildMailException(request, exception);
        sendMailService.performExceptionSender(sb.toString());

        try {
            if (!tsPropertyProvider.getExceptionsIgnoreClassNames().contains(getLastCause(exception).getClass().getName())
            ) {
                model.put("errors", "Unexpected error: " + exception.getMessage());
            }
        } finally {
            // Выведем в лог
            logger.error("Произошла неожиданная ошибка:", exception);
        }

        return new ModelAndView("exception", model);
    }

}
