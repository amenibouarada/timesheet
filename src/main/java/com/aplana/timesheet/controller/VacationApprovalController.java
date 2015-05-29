package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.ApprovalResultModel;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.VacationApprovalForm;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.ManagerRoleNameService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import com.aplana.timesheet.service.vacationapproveprocess.VacationApprovalProcessService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author iziyangriov
 */

@Controller
public class VacationApprovalController {

    @Autowired
    private VacationApprovalService vacationApprovalService;
    @Autowired
    private VacationApprovalProcessService vacationApprovalProcessService;
    @Autowired
    private TSPropertyProvider propertyProvider;
    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private ManagerRoleNameService managerRoleNameService;

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalController.class);

    final String NOT_ACCEPTED_YET = "%s, просьба принять решение по \"%s\" сотрудника %s из г. %s на период с %s по %s. %s";
    final String ACCEPTANCE = "Запрос \"%s\" сотрудника %s из г. %s на период с %s по %s %s.";
    final String ACCEPTED = "согласован";
    final String REFUSED = "не согласован";
    final String BAD_REQUEST = "Неверный запрос!";
    final String LOGGER_MESSAGE = "\n    UserIP: %s\n";
    final String REFUSE_ANSWER = "%s, Вы не согласовали \"%s\" сотрудника %s из г. %s на период с %s по %s.";
    final String ACCEPT_ANSWER = "%s, Вы согласовали \"%s\" сотрудника %s из г. %s на период с %s по %s.";

    final String BUTTONS_INVISIBLE = "style=\"display: none\"";

    final private AtomicInteger GLOBAL_WRONG_REQUEST_COUNTER = new AtomicInteger(1);

    @RequestMapping(value = "/vacation_approval", method = RequestMethod.GET)
    public ModelAndView vacationApprovalShow(
            @RequestParam(value = "uid", required = true) String uid,
            @ModelAttribute("vacationApprovalForm") VacationApprovalForm vaForm,
            HttpServletRequest request
    ) {
        ModelAndView mav = new ModelAndView("vacation_approval");
        mav.addObject("NoPageFormat", "true");

        VacationApproval vacationApproval = vacationApprovalService.getVacationApproval(uid);

        if (vacationApproval == null){
            proceedBadRequest(vaForm, request);
            return mav;
        }

        String matchingFIO = vacationApproval.getManager().getName();
        String vacationType = vacationApproval.getVacation().getType().getValue();
        String employeeFIO = vacationApproval.getVacation().getEmployee().getName();
        String region = vacationApproval.getVacation().getEmployee().getRegion().getName();
        String dateBegin = DateTimeUtil.getOnlyDate(vacationApproval.getVacation().getBeginDate());
        String dateEnd = DateTimeUtil.getOnlyDate(vacationApproval.getVacation().getEndDate());
        String comment = vacationApproval.getVacation().getComment();
        Boolean result = vacationApproval.getResult();

        vaForm.setApprovalList(getApprovalList(vacationApproval));

        if (result == null){
            vaForm.setMessage(String.format(NOT_ACCEPTED_YET, matchingFIO, vacationType, employeeFIO, region, dateBegin,
                    dateEnd, comment == null ? "" : comment));
            vaForm.setButtonsVisible("");
        } else {
           vaForm.setMessage(String.format(ACCEPTANCE, vacationType, employeeFIO, region, dateBegin, dateEnd,
                   getResultString(result)));
           vaForm.setButtonsVisible(BUTTONS_INVISIBLE);
        }

        return mav;
    }

    @RequestMapping(value = "/vacation_approval/save/{uid}/{acceptance}")
    public ModelAndView vacationApprovalSaveResult(
            @PathVariable("uid") String uid,
            @PathVariable ("acceptance") Boolean acceptance,
            @ModelAttribute("vacationApprovalForm") VacationApprovalForm vaForm,
            HttpServletRequest request
    ) throws VacationApprovalServiceException {
        ModelAndView mav = new ModelAndView("vacation_approval");
        mav.addObject("NoPageFormat", "true");
        vaForm.setButtonsVisible(BUTTONS_INVISIBLE);

        VacationApproval vacationApproval = vacationApprovalService.getVacationApproval(uid);

        if (vacationApproval == null){
            proceedBadRequest(vaForm, request);
            return mav;
        }

        // отпуск уже согласовывали
        if (vacationApproval.getResult() != null){
            return vacationApprovalShow(uid, vaForm, request);
        }

        String matchingFIO = vacationApproval.getManager().getName();
        String vacationType = vacationApproval.getVacation().getType().getValue();
        String employeeFIO = vacationApproval.getVacation().getEmployee().getName();
        String region = vacationApproval.getVacation().getEmployee().getRegion().getName();
        String dateBegin = DateTimeUtil.getOnlyDate(vacationApproval.getVacation().getBeginDate());
        String dateEnd = DateTimeUtil.getOnlyDate(vacationApproval.getVacation().getEndDate());

        vacationApproval.setResult(acceptance);
        vacationApproval.setResponseDate(new Date());

        if (acceptance){
            vaForm.setMessage(String.format(ACCEPT_ANSWER, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd));
        } else{
            vaForm.setMessage(String.format(REFUSE_ANSWER, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd));
        }

        vacationApprovalService.store(vacationApproval);

        sendMailService.performVacationAcceptanceMailing(vacationApproval);

        vacationApprovalProcessService.checkVacationIsApproved(vacationApproval.getVacation());

        vaForm.setApprovalList(getApprovalList(vacationApproval));

        return mav;
    }

    private List<ApprovalResultModel> getApprovalList(VacationApproval vacationApproval){
        Vacation vacation = vacationApproval.getVacation();
        List<VacationApproval> otherApprovals = vacationApprovalService.getAllApprovalsForVacation(vacation);
        if (vacationApproval.getResult() == null){
            otherApprovals.remove(vacationApproval);
        }

        List<ApprovalResultModel> approvalList = new ArrayList<ApprovalResultModel>();

        for (VacationApproval va : otherApprovals){
            ApprovalResultModel arm = new ApprovalResultModel();
            arm.setRole(managerRoleNameService.getManagerRoleName(va));
            arm.setName(va.getManager().getName());
            arm.setResult(va.getResult() == null ? "На согласовании" : (va.getResult() ? "Согласовано" : "Не согласовано"));
            approvalList.add(arm);
        }

        return approvalList;

    }

    private void proceedBadRequest(VacationApprovalForm vaForm, HttpServletRequest request){
        vaForm.setMessage(BAD_REQUEST);
        vaForm.setButtonsVisible(BUTTONS_INVISIBLE);

        logger.warn("Somebody try to get vacation approval service by wrong UID number: {}",
                String.format(LOGGER_MESSAGE, request.getRemoteAddr()));

        // если счетчик неудачных попыток кратен лимиту из настроек
        if (GLOBAL_WRONG_REQUEST_COUNTER.getAndIncrement() % propertyProvider.getVacationApprovalErrorThreshold() == 0){
            // Отправим сообщение админам
            sendMailService.performVacationApprovalErrorThresholdMailing();
        }
    }

    public String getResultString(Boolean result){
        if (result) {
            return ACCEPTED;
        }
        return REFUSED;
    }


}
