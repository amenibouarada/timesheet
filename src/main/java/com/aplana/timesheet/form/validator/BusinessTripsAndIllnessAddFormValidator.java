package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.Identifiable;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.QuickReportTypesEnum;
import com.aplana.timesheet.form.BusinessTripsAndIllnessAddForm;
import com.aplana.timesheet.service.BusinessTripService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.IllnessService;
import com.aplana.timesheet.util.EnumsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.List;

/**
 * User: vsergeev
 * Date: 28.01.13
 */
@Service
public class BusinessTripsAndIllnessAddFormValidator extends AbstractValidator {

    private static final int MAX_COMMENT_LENGTH = 600;

    @Autowired
    private BusinessTripService businessTripService;

    @Autowired
    private IllnessService illnessService;

    @Autowired
    private EmployeeService employeeService;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(BusinessTripsAndIllnessAddForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        BusinessTripsAndIllnessAddForm form = (BusinessTripsAndIllnessAddForm) o;

        QuickReportTypesEnum reportType = EnumsUtils.getEnumById(form.getReportType(), QuickReportTypesEnum.class);
        List<? extends Identifiable> reports = null;

        if (form.getEmployee() == null) {
            if (form.getReportId() != null) {
                form.setEmployee(getEmployeeFromQuickReport(reportType, form.getReportId()));
            }
        }

        if (form.getEmployee() == null) {
            errors.rejectValue("employee", "error.businesstripsandilnessaddform.employee.wrong", "Сотрудник не определен!");
        } else {
            switch (reportType){
                case BUSINESS_TRIP: {
                    reports = businessTripService.getEmployeeBusinessTripsByDates(form.getEmployee(), form.getBeginDate(), form.getEndDate());
                    break;
                }
                case ILLNESS: {
                    reports = illnessService.getEmployeeIllnessByDates(form.getEmployee(), form.getBeginDate(), form.getEndDate());
                    break;
                }
                default: {
                    errors.rejectValue("reportType", "error.businesstripsandilnessaddform.reporttype.wrong", "Тип отчета не определен!");
                }
            }

            if (! reports.isEmpty()) {
                if (! isEditingReport(form.getReportId(), reports)) {
                    errors.rejectValue("beginDate", "error.businesstripsandilnessaddform.beginDate.wrong", String.format("Выбранный период частично или полностью попадает на период %s %s!", getExistingString(reportType), getReportName(reportType)));
                }
            }
        }

        if (form.getBeginDate().after(form.getEndDate())){
            errors.rejectValue("beginDate", "error.businesstripsandilnessaddform.begindate.wrong", "Дата окончания " + getReportName(reportType) + " не может быть раньше даты начала!");
        }

        form.setComment(form.getComment().trim());

        if (form.getComment() != null && form.getComment().length() > MAX_COMMENT_LENGTH) {
            errors.rejectValue("beginDate", "error.businesstripsandilnessaddform.comment.wrong", "Комментарий выходит за рамки допустимого количества символов - " + MAX_COMMENT_LENGTH + "!");
        }

    }

    private String getExistingString(QuickReportTypesEnum reportType) {
        switch (reportType) {
            case BUSINESS_TRIP: {
                return "существующей";
            }
            case ILLNESS: {
                return "существующего";
            }
            default: return null;
        }
    }

    private Employee getEmployeeFromQuickReport(QuickReportTypesEnum reportType, Integer reportId) {
        switch (reportType) {
            case BUSINESS_TRIP: {
                return employeeService.getEmployeeFromBusinessTrip(reportId);
            }
            case ILLNESS: {
                return employeeService.getEmployeeFromIllness(reportId);
            }
            default: return null;
        }
    }

    private boolean isEditingReport(Integer reportId, List<? extends Identifiable> reports) {
        if (reports.size() == 1) {
            if (reports.get(0).getId().equals(reportId)) {
                return true;
            }
        }

        return false;
    }

    private String getReportName(QuickReportTypesEnum reportType) {
        switch (reportType) {
            case BUSINESS_TRIP: return "командировки";
            case ILLNESS: return "больничного";
            default: return "неизвестно";
        }
    }
}
