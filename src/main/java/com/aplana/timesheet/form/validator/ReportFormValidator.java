package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.enums.Report07PeriodTypeEnum;
import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;

@Service
public class ReportFormValidator implements Validator {
    private static final Logger logger = LoggerFactory.getLogger(ReportFormValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(TSJasperReport.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        //TODO переделать этот ад
        if (o.getClass().isAssignableFrom(Report06.class)) {
            validateReport06(o, errors);
        } else if (o.getClass().isAssignableFrom(Report01.class)) {
            validateReport01( o, errors );
        } else if (o.getClass().isAssignableFrom(Report07.class)) {
            validateReport07(o, errors);
        }

        validateReport( ( BaseReport ) o, errors );
    }

    private void validateReport01(Object o, Errors errors) {
        Report01 form = (Report01) o;

		List<Integer> regionIds = form.getRegionIds();		
		// ничего не выбрано и не поставлена галка "Все регионы"
		if ((regionIds == null || regionIds.isEmpty()) && !form.isAllRegions()) {
			errors.rejectValue("regionIds", "error.reportform.noregion");
		}
    }

    private void validateReport06(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectId", "error.reportform.noproject");
    }

    private void validateReport07(Object o, Errors errors) {
        Report07 report = (Report07) o;

        if (report.getDivisionOwner() == null) {
            errors.rejectValue("filterDivisionOwner", "error.reportform.noprojectdivision");
        }

        if (report.getDivisionEmployee() == null) {
            errors.rejectValue("divisionEmployee", "error.reportform.noemplyeedivision");
        }

        final Report07PeriodTypeEnum periodType =
                Report07PeriodTypeEnum.tryGetByMonthsCount(report.getPeriodType());

        if (!ArrayUtils.contains(Report07PeriodTypeEnum.values(), periodType)) {
            errors.rejectValue("periodType", "error.reportform.wrongperiodtype");
        }
    }


    private void validateReport( BaseReport form, Errors errors ) {
        String beginDate = form.getBeginDate();
        if ( StringUtils.isBlank( beginDate ) && ! DateTimeUtil.isDateValid( beginDate )) {
            errors.rejectValue("beginDate", "error.reportform.wrongbegindate");
        }

        String endDate = form.getEndDate();
        if ( StringUtils.isBlank( endDate ) && !DateTimeUtil.isDateValid(endDate)) {
            errors.rejectValue("endDate", "error.reportform.wrongenddate");
        }

        if (!DateTimeUtil.isPeriodValid(beginDate, endDate)) {
            errors.rejectValue("beginDate", "error.reportform.wrongperiod");
        }
    }
}
