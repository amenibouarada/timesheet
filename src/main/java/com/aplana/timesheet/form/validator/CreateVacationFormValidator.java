package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.form.CreateVacationForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class CreateVacationFormValidator extends AbstractValidator {

    public static final int TYPE_WITH_REQUIRED_COMMENT = VacationTypesEnum.WITH_NEXT_WORKING.getId();
    private static final int MAX_COMMENT_LENGTH = 600;
    private static final String MAX_LENGTH_ERROR_MESSAGE =
            String.format("Длина комментария превышает допустимые %d символов", MAX_COMMENT_LENGTH);
    private static final String WRONG_YEAR_ERROR_MESSAGE = "Календарь на %i год еще не заполнен, " +
            "оформите заявление позже или обратитесь в службу поддержки системы";
    private static final String WRONG_PLANNED_TODATE_ERROR_MESSAGE = "Дата начала планируемого отпуска должна быть позже %s";

    @Autowired
    private VacationService vacationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    protected TSPropertyProvider propertyProvider;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(CreateVacationForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        validate((CreateVacationForm) o, errors, false);
    }

    public void validate(CreateVacationForm createVacationForm, Errors errors, boolean approved) {
        final String calFromDate = createVacationForm.getCalFromDate();
        final String calToDate = createVacationForm.getCalToDate();

        final boolean isPlannedVacation = createVacationForm.getVacationType().equals(VacationTypesEnum.PLANNED.getId());
        final Integer createTrashold = propertyProvider.getPlannedVacationCreateThreshold();

        DictionaryItem planned = dictionaryItemService.find(VacationTypesEnum.PLANNED.getId());

        final boolean calFromDateIsNotEmpty = StringUtils.length(calFromDate) > 0;
        final boolean calToDateIsNotEmpty = StringUtils.length(calToDate) > 0;

        if (calFromDateIsNotEmpty && calToDateIsNotEmpty) {
            final Timestamp fromDate = DateTimeUtil.stringToTimestamp(calFromDate);
            final Timestamp toDate = DateTimeUtil.stringToTimestamp(calToDate);

            if (!(
                    approved &&
                    employeeService.isEmployeeAdmin(securityService.getSecurityPrincipal().getEmployee().getId())
            )) {
                final Date currentDate = new Date();
                Date allowedDate = DateUtils.addDays(currentDate, createTrashold);

                if (!fromDate.after(currentDate)) {
                    errors.rejectValue(
                            "calFromDate",
                            "error.createVacation.fromdate.wrong",
                            "Дата начала отпуска должна быть больше текущей даты"
                    );
                }

                if (!toDate.after(currentDate)) {
                    errors.rejectValue(
                            "calToDate",
                            "error.createVacation.todate.wrong",
                            "Дата окончания отпуска должна быть больше текущей даты"
                    );
                }

                if (isPlannedVacation && !fromDate.after(allowedDate)) {
                    errors.rejectValue(
                            "calToDate",
                            "error.createVacation.planned.todate.wrong",
                            new Object[]{new SimpleDateFormat(DATE_FORMAT).format(allowedDate)},
                            WRONG_PLANNED_TODATE_ERROR_MESSAGE
                    );
                }

            }
            long intersectVacationsCount = 0;
            if (!isPlannedVacation) {
                intersectVacationsCount = vacationService.getIntersectVacationsCount(
                        createVacationForm.getEmployeeId(),
                        fromDate,
                        toDate,
                        planned
                );
            } else {
                intersectVacationsCount = vacationService.getIntersectPlannedVacationsCount(
                        createVacationForm.getEmployeeId(),
                        fromDate,
                        toDate,
                        planned
                );
            }

            if (intersectVacationsCount > 0) {
                errors.reject(
                        "error.createVacation.wrongperiod",
                        "Указанный период частично или полностью приходится на период отпуска в уже существующем заявлении"
                );
            }

            if (fromDate.after(toDate)) {
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.wrongtodate",
                        "Дата окончания отпуска не может быть больше даты начала"
                );
            }
            if (calendarService.find(toDate) == null){
                Integer year = toDate.getYear() + 1900;
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.wrongyear",
                        new Object[]{year.toString()},
                        WRONG_YEAR_ERROR_MESSAGE
                );
            }
        } else {
            if (!calFromDateIsNotEmpty) {
                errors.rejectValue(
                        "calFromDate",
                        "error.createVacation.fromdate.required",
                        "Не указана дата начала отпуска"
                );
            }

            if (!calToDateIsNotEmpty) {
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.todate.required",
                        "Не указана дата окончания отпуска"
                );
            }
        }

        if (createVacationForm.getVacationType() == 0) {
            errors.reject(
                    "error.createVacation.type.required",
                    "Не указан тип отпуска"
            );
        }

        final int commentLength = StringUtils.length(createVacationForm.getComment());

        if (
                createVacationForm.getVacationType().equals(TYPE_WITH_REQUIRED_COMMENT) &&
                        commentLength == 0
        ) {
            errors.reject(
                    "error.createVacation.comment.required",
                    "Не указан комментарий"
            );
        }

        if (commentLength > MAX_COMMENT_LENGTH) {
            errors.rejectValue(
                    "comment",
                    "error.createVacation.comment.maxlength",
                    new Object[]{ MAX_COMMENT_LENGTH },
                    MAX_LENGTH_ERROR_MESSAGE
            );
        }
    }

}
