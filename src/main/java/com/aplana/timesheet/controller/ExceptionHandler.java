package com.aplana.timesheet.controller;

import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.system.security.SecurityService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.aplana.timesheet.util.ExceptionUtils.getLastCause;

@Controller
public class ExceptionHandler implements HandlerExceptionResolver {

    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EmployeeService employeeService;

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

        try {
            if (!tsPropertyProvider.getExceptionsIgnoreClassNames().
                    contains(getLastCause(exception).getClass().getName())
            ) {
                model.put("errors", "Unexpected error: " + exception.getMessage());
                // получим ФИО пользователя
                String FIO = "<не определен>";
                TimeSheetUser securityUser = securityService.getSecurityPrincipal();
                if (securityUser != null) {
                    int employeeId = securityUser.getEmployee().getId();
                    FIO = employeeService.find(employeeId).getName();
                }
                // Отправим сообщение админам
                StringBuilder sb = new StringBuilder();
                sb.append("У пользователя ").append(FIO).append(" произошла следующая ошибка:<br>");
                sb.append(exception.getMessage() != null ? exception.getMessage() : getLastCause(exception).getClass().getName());
                sb.append("<br><br>");

                StringBuilder sbtemp = new StringBuilder();
                Map parameterMap = request.getParameterMap();
                Iterator iterator = parameterMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry) iterator.next();
                    sbtemp.append(mapEntry.getKey() + " : " + Arrays.toString((String[])mapEntry.getValue()) + "<br>");
                }

                if (sbtemp.length()>0) {
                   sb.append("<br><br>");
                   sb.append("Параметры запроса: ");
                   sb.append(sbtemp.toString());
                }

                sb.append("Stack trace: <br>");
                sb.append(Arrays.toString(exception.getStackTrace()));
                sendMailService.performExceptionSender(sb.toString());
            }
        } finally {
            // Выведем в лог
            logger.error("Произошла неожиданная ошибка:", exception);
        }

        return new ModelAndView("exception", model);
    }

}
