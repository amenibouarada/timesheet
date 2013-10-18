package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.OvertimeCauseDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.OvertimeCause;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.util.EnumsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.aplana.timesheet.system.constants.TimeSheetConstants.WORK_DAY_DURATION;

/**
 * @author eshangareev
 * @version 1.0
 */
@Service("overtimeCauseService")
public class OvertimeCauseService {

    private static final Logger logger = LoggerFactory.getLogger(OvertimeCauseService.class);

    @Autowired
    private OvertimeCauseDAO dao;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private TSPropertyProvider propertyProvider;


    @Transactional
    public void store(TimeSheet timeSheet, TimeSheetForm tsForm) {
        //if (!isOvertimeCauseNeeeded(tsForm, calculateTotalDuration(tsForm))) return; Непонятно зачем тут то проверять? Уже прислали - значит надо записать

        if (tsForm.getOvertimeCause() == null) return;

        OvertimeCause overtimeCause = new OvertimeCause();
        overtimeCause.setOvertimeCause( dictionaryItemService.find(tsForm.getOvertimeCause()) );
        overtimeCause.setTimeSheet(timeSheet);
        overtimeCause.setComment(tsForm.getOvertimeCauseComment());
        overtimeCause.setCompensation(dictionaryItemService.find(tsForm.getTypeOfCompensation()));

        dao.store(overtimeCause);
    }

    private BigDecimal calculateTotalDuration(TimeSheetForm tsForm) {
        BigDecimal totalDuration = BigDecimal.ZERO;
        BigDecimal currentDuration;
        for (TimeSheetTableRowForm tableRowForm : tsForm.getTimeSheetTablePart()) {
            try{
                currentDuration = new BigDecimal(tableRowForm.getDuration().replace(",", "."));
            }catch (NumberFormatException e){
                logger.error("Error parsing duration on empl_id="+tsForm.getEmployeeId()+" on date='"+tsForm.getCalDate()+"'", e);
                currentDuration = BigDecimal.ZERO;
            }catch (NullPointerException e){
                logger.error("Error parsing duration (isNull) on empl_id="+tsForm.getEmployeeId()+" on date='"+tsForm.getCalDate()+"'", e);
                currentDuration = BigDecimal.ZERO;
            }

            totalDuration = totalDuration.add(currentDuration);
        }

        return totalDuration;
    }

    public String getCauseName(TimeSheetForm tsForm) {
        final Integer overtimeCauseId = tsForm.getOvertimeCause();
        if (overtimeCauseId == null) return null;
        return dictionaryItemService.find(overtimeCauseId).getValue();
    }

    public boolean isOvertimeCauseNeeded(TimeSheetForm tsForm, BigDecimal totalDuration) {
        for (TimeSheetTableRowForm rowForm : tsForm.getTimeSheetTablePart()) {
            if (
                    TypesOfActivityEnum.isNotCheckableForOvertime(
                        EnumsUtils.tryFindById(
                                rowForm.getActivityTypeId(),
                                TypesOfActivityEnum.class
                        )
                    )
            ) {
                return false;
            }
        }

        return totalDuration.subtract(BigDecimal.valueOf(WORK_DAY_DURATION)).compareTo(
                    BigDecimal.valueOf(propertyProvider.getOvertimeThreshold())) > 0
                && BigDecimal.valueOf(WORK_DAY_DURATION).subtract(totalDuration).compareTo(
                    BigDecimal.valueOf(propertyProvider.getUndertimeThreshold())) > 0;
    }

    public boolean isOvertimeDuration(TimeSheetForm tsForm) {
        final List<TimeSheetTableRowForm> tsTableRowList = tsForm.getTimeSheetTablePart();
        if (tsTableRowList != null && tsTableRowList.size() != 0) {
            for (TimeSheetTableRowForm rowForm : tsTableRowList) {
                if (TypesOfActivityEnum.isNotCheckableForOvertime(
                        EnumsUtils.tryFindById(rowForm.getActivityTypeId(),TypesOfActivityEnum.class))) {
                    return false;
                }
            }
            BigDecimal totalDuration = calculateTotalDuration(tsForm);
            return totalDuration.subtract(BigDecimal.valueOf(WORK_DAY_DURATION)).compareTo(
                    BigDecimal.valueOf(propertyProvider.getOvertimeThreshold())) > 0;
        } else {
            return false;
        }
    }

    public boolean isUndertimeDuration(TimeSheetForm tsForm) {
        final List<TimeSheetTableRowForm> tsTableRowList = tsForm.getTimeSheetTablePart();
        if (tsTableRowList != null && tsTableRowList.size() != 0) {
            for (TimeSheetTableRowForm rowForm : tsForm.getTimeSheetTablePart()) {
                if (TypesOfActivityEnum.isNotCheckableForOvertime(
                        EnumsUtils.tryFindById(rowForm.getActivityTypeId(),TypesOfActivityEnum.class))) {
                    return false;
                }
            }
            BigDecimal totalDuration = calculateTotalDuration(tsForm);
            return BigDecimal.valueOf(WORK_DAY_DURATION).subtract(totalDuration).compareTo(
                    BigDecimal.valueOf(propertyProvider.getUndertimeThreshold())) > 0;
        } else {
            return false;
        }
    }

    public Integer getDictId(Integer overtimeCauseId) {
        DictionaryItem overtimeCause = dictionaryItemService.find(overtimeCauseId);
        return overtimeCause != null ? overtimeCause.getDictionary().getId() : null;
    }
}
