package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.ProjectRoleDAO;
import com.aplana.timesheet.form.PlanEditForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.List;

import static com.aplana.timesheet.form.PlanEditForm.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class PlanEditFormValidator extends AbstractValidator {

    private static final String ERROR_PLANEDIT_FORM = "error.planedit.form.";

    @Autowired
    private ProjectRoleDAO projectRoleDAO;

    @Override
    public boolean supports(Class<?> clazz) {
        return (clazz.isAssignableFrom(PlanEditForm.class));
    }

    @Override
    public void validate(Object target, Errors errors) {
        final PlanEditForm form = (PlanEditForm) target;

        validateDivision(form.getDivisionId(), errors);
        validateYear(form.getYear(), errors);
        validateMonth(form.getYear(), form.getMonth(), errors);
        validateRegions(form.getRegions(), errors);
        validateProjectRoles(form.getProjectRoles(), errors);
        validateShowPlans(form.getShowPlans(), errors);
        validateShowFacts(form.getShowFacts(), errors);
        validateShowProjects(form.getShowProjects(), errors);
        validateShowPresales(form.getShowPresales(), errors);
    }

    private void validateDivision(Integer divisionId, Errors errors) {
        if (divisionId == null) {
            errors.rejectValue(DIVISION_ID, ERROR_PLANEDIT_FORM + "divisionid.required", "Не выбрано подразделение");
        }
    }

    private void validateRegions(List<Integer> regions, Errors errors) {
        if (regions == null || regions.isEmpty()) {
            errors.rejectValue(REGIONS, ERROR_PLANEDIT_FORM + "regions.required",
                    "Не выбран ни один регион");
        }
    }

    private void validateProjectRoles(List<Integer> projectRoles, Errors errors) {
        if (projectRoles == null || projectRoles.isEmpty()) {
            errors.rejectValue(PROJECT_ROLES, ERROR_PLANEDIT_FORM + "projectroles.required",
                    "Не выбран ни одна должность");
        }
    }

    private void validateShowPlans(Boolean showPlans, Errors errors) {
        validateShowFlagField(showPlans, SHOW_PLANS, "плановых показателей", errors);
    }

    private void validateShowFacts(Boolean showFacts, Errors errors) {
        validateShowFlagField(showFacts, SHOW_FACTS, "фактических показателей", errors);
    }

    private void validateShowProjects(Boolean showProjects, Errors errors) {
        validateShowFlagField(showProjects, SHOW_PROJECTS, "проектов", errors);
    }

    private void validateShowPresales(Boolean showPresales, Errors errors) {
        validateShowFlagField(showPresales, SHOW_PRESALES, "пресейлов", errors);
    }

    private void validateShowFlagField(Boolean fieldValue, String fieldName, String messageComponent, Errors errors) {
        if (fieldValue == null) {
            errors.rejectValue(
                    fieldName,
                    ERROR_PLANEDIT_FORM + "showflag.required",
                    new Object[]{ messageComponent },
                    String.format("Не указан флаг отображения %s", messageComponent)
            );
        }
    }
}
