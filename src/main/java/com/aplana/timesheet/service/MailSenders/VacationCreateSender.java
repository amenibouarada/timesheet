package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.ApprovalResultModel;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.ManagerRoleNameService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.*;

/**
 * User: bkinzyabulatov
 */
public class VacationCreateSender extends AbstractVacationSender<Vacation> {

    public VacationCreateSender(SendMailService sendMailService, TSPropertyProvider propertyProvider,
                                VacationApprovalService vacationApprovalService, ManagerRoleNameService managerRoleNameService) {
        super(sendMailService, propertyProvider,vacationApprovalService,managerRoleNameService);
    }

    @Override
    public List<Mail> getMailList(Vacation vacation) {
        final Mail mail = new TimeSheetMail();

        if(vacation.getEmployee().getDivision().getVacationEmail()!=null && !vacation.getEmployee().getDivision().getVacationEmail().isEmpty()){
            ArrayList<String> toEmails = Lists.newArrayList(vacation.getEmployee().getDivision().getVacationEmail());
            mail.setToEmails(toEmails);
            mail.setSubject(getSubject(vacation));
            mail.setParamsForGenerateBody(getParamsForGenerateBody(vacation));
        }

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Vacation vacationApproval) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(vacationApproval));

        return table;
    }

    private String getBody(Vacation vacation) {

        String vacationTypeStr = vacation.getType().getValue();
        String employeeNameStr = vacation.getEmployee().getName();
        String regionNameStr = vacation.getEmployee().getRegion().getName();
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);
        String creationDate = DateFormatUtils.format(vacation.getCreationDate(), DATE_FORMAT);
        String commentStr = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(vacation.getComment())) {
            commentStr = String.format("Комментарий: %s. ", vacation.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Создан \"%s\" ", vacationTypeStr));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s по %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Дата создания отпуска %s.",creationDate));

        List<VacationApproval> approvals = vacationApprovalService.getAllApprovalsForVacation(vacation);

        List<ApprovalResultModel> approvalList = new ArrayList<ApprovalResultModel>();

        for (VacationApproval va : approvals){
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
                    sendMailService.velocityEngine, "velocity/vacationapprovals.vm", model);
            logger.debug("Message Body: {}", messageBody);
            stringBuilder.append(messageBody);
        }
        return stringBuilder.toString();
    }

    private String getSubject(Vacation vacation) {
        return  String.format("Создание отпуска %s", vacation.getEmployee().getName());
    }

    @Override
    protected String getSubjectFormat() {
        return propertyProvider.getVacationCreateMailMarker() + " %s";
    }
}
