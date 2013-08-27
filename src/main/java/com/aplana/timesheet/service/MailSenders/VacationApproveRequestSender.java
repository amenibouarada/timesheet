package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.ApprovalResultModel;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.ManagerRoleNameService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.*;

/**
 * User: vsergeev
 * Date: 04.02.13
 */
public class VacationApproveRequestSender extends AbstractVacationSender<VacationApproval> {

    protected static final Logger logger = LoggerFactory.getLogger(VacationApproveRequestSender.class);

    public VacationApproveRequestSender(SendMailService sendMailService, TSPropertyProvider propertyProvider,
                                        VacationApprovalService vacationApprovalService, ManagerRoleNameService managerRoleNameService) {
        super(sendMailService, propertyProvider, vacationApprovalService, managerRoleNameService);
    }

    @Override
    protected List<Mail> getMailList(VacationApproval vacationApproval) {
        final Mail mail = new TimeSheetMail();
        Vacation vacation = vacationApproval.getVacation();

        mail.setToEmails(Arrays.asList(vacationApproval.getManager().getEmail()));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacationApproval));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(VacationApproval vacationApproval) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(vacationApproval));

        return table;
    }

    private String getBody(VacationApproval vacationApproval) {
        Vacation vacation = vacationApproval.getVacation();

        String vacationTypeStr = vacation.getType().getValue();
        String employeeNameStr = vacation.getEmployee().getName();
        String regionNameStr = vacation.getEmployee().getRegion().getName();
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);
        String creationDate = DateFormatUtils.format(vacation.getCreationDate(), DATE_FORMAT);
        String commentStr = StringUtils.EMPTY;
        String approveURL = String.format("%s/vacation_approval?uid=%s", getTimeSheetURL(), vacationApproval.getUid());
        if (StringUtils.isNotBlank(vacation.getComment())) {
            commentStr = String.format("Комментарий: %s. ", vacation.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Просьба принять решение по \"%s\" ", vacationTypeStr));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s по %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Для регистрации Вашего решения нажмите на ссылку: %s . ", approveURL));
        stringBuilder.append(String.format("Дата создания отпуска %s.",creationDate));

        List<VacationApproval> otherApprovals = vacationApprovalService.getAllApprovalsForVacation(vacation);
        List<VacationApproval> cloneOtherApproval = new ArrayList<VacationApproval>(otherApprovals);
        for (VacationApproval va : cloneOtherApproval){
            if (va.getManager().getId().equals(vacationApproval.getManager().getId())){
                otherApprovals.remove(va);
            }
        }

        List<ApprovalResultModel> approvalList = new ArrayList<ApprovalResultModel>();

        for (VacationApproval va : otherApprovals){
            ApprovalResultModel arm = new ApprovalResultModel();
            arm.setRole(managerRoleNameService.getManagerRoleName(va));
            arm.setName(va.getManager().getName());
            arm.setResult(va.getResult() == null ? "На согласовании" : (va.getResult() ? "Согласовано" : "Не согласовано"));
            approvalList.add(arm);
        }

        if (approvalList.size() > 0){
            Map model = new HashMap();
            model.put("approvalList", approvalList.iterator());
            String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                    sendMailService.velocityEngine, "vacationapprovals.vm", model);
            logger.debug("Message Body: {}", messageBody);
            stringBuilder.append(messageBody);
        }
        return stringBuilder.toString();
    }

    private String getTimeSheetURL() {
        return propertyProvider.getTimeSheetURL();
    }

    private String getSubject(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        return  String.format("Запрос отпуска %s", vacation.getEmployee().getName());
//        return  String.format("%s %s %s - %s", vacation.getStatus().getValue(), vacation.getEmployee().getName(),
//                        beginDateStr, endDateStr);
    }

}
