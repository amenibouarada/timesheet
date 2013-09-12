package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.form.CreatePlanForPeriodForm;
import com.aplana.timesheet.service.ProjectService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.Calendar;
import java.util.Date;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class CreatePlanForPeriodFormValidator extends AbstractValidator {

    private static final String ERROR_CODE_PREFIX = "error.createPlanForPeriodForm.";

    @Autowired
    private ProjectService projectService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(CreatePlanForPeriodForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final CreatePlanForPeriodForm form = (CreatePlanForPeriodForm) target;

        if (isNotChoosed(form.getEmployeeId())) {
            errors.rejectValue("employeeId", ERROR_CODE_PREFIX + "employeeId.required", "Не выбран сотрудник");
        }

        final Date curDate = new Date();
        final Date fromDate = form.getFromDate();
        final Date toDate = form.getToDate();

        if (DateUtils.truncatedCompareTo(curDate, fromDate, Calendar.MONTH) > 0) {
            errors.rejectValue("fromDate", ERROR_CODE_PREFIX + "fromDate.invalid",
                    "Начало периода должно быть не раньше текущего месяца");
        }

        if (fromDate.after(toDate)) {
            errors.rejectValue("toDate", ERROR_CODE_PREFIX + "toDate.invalid",
                    "Конец периода не должны быть раньше начала");
        }

        final Integer projectId = form.getProjectId();

        if (isNotChoosed(projectId)) {
            errors.rejectValue("projectId", ERROR_CODE_PREFIX + "projectId.required", "Не выбран проект");
        } else {
            final Project project = projectService.find(projectId);

            final Date projectEndDate = project.getEndDate();

            if (project.getStartDate().after(fromDate) || projectEndDate != null && projectEndDate.before(toDate)) {
                errors.rejectValue("projectId", ERROR_CODE_PREFIX + "projectId.invalid",
                        "Период активности проекта не попадает в указанный период");
            }
        }

        final Byte percentOfCharge = form.getPercentOfCharge();

        if (percentOfCharge == null) {
            errors.rejectValue("percentOfCharge", ERROR_CODE_PREFIX + "percentOfCharge.required",
                    "Не указан процент загрузки");
        } else if (percentOfCharge < 0 || percentOfCharge > 100) {
            errors.rejectValue("percentOfCharge", ERROR_CODE_PREFIX + "percentOfCharge.invalid",
                    "Процент загрузки должен быть в диапозоне от 0 до 100");
        }
    }

}
