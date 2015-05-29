package com.aplana.timesheet.controller;

import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.form.validator.AdminMessageFormValidator;
import com.aplana.timesheet.service.SendMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);

    @Autowired
    private AdminMessageFormValidator adminMessageFormValidator;

    @Autowired
    private SendMailService sendMailService;
    private static final SimpleDateFormat DATE_WITH_TIME_FORMAT = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginForm(ModelMap model) {
        return "login";
    }

    @RequestMapping(value="/loginfailed", method = RequestMethod.GET)
    public String loginError(ModelMap model, HttpServletRequest request) {
        model.addAttribute("error", "true");
        return "login";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logout(ModelMap model) {
        return "login";
    }

    //форма отпраки сообщения
    @RequestMapping(value="/adminMessage" , method = RequestMethod.GET)
    public ModelAndView sendMessage(HttpServletRequest request) {
        return new ModelAndView("adminMessage").addObject("adminMessageForm",new AdminMessageForm());
    }

    //по нажатию на кнопку отправить.
    @RequestMapping(value = "/adminMessage", method = RequestMethod.POST)
    public ModelAndView adminMessage(
            @ModelAttribute("adminMessageForm") AdminMessageForm adminMessageForm,
            BindingResult result,
            HttpServletRequest request
    ) {
        ModelAndView mav;

        adminMessageFormValidator.validate(adminMessageForm, result);

        if (result.hasErrors()) {
            mav=new ModelAndView("adminMessage");
            mav.addObject("errors",result.getAllErrors());
            return mav;
        }

        String dateString = DATE_WITH_TIME_FORMAT.format( new Date() );

        //берем последнюю ошибку
        HttpSession httpSession=request.getSession(false);
        String error=httpSession.getAttribute("SPRING_SECURITY_LAST_EXCEPTION").toString();
        //и имя юзера
        String name=httpSession.getAttribute("lastLogin").toString();

        //проверяем
        if(error.isEmpty()) error="user don't indicated error";
        if(name.isEmpty()) name="user don't indicated login";

        //цепляем дополнительные данные
        adminMessageForm.setDate(dateString);
        adminMessageForm.setError(error);
        adminMessageForm.setName(name);

        //отправка сообщения
        sendMailService.performLoginProblemMailing(adminMessageForm);

        mav=new ModelAndView("adminMessageSent");
        return mav;
    }
}
