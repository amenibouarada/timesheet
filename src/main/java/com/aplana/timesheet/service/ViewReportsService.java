package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.form.ReportsViewDeleteForm;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

import static com.aplana.timesheet.form.VacationsForm.VIEW_TABLE;

/**
 * Created by abayanov
 * Date: 14.08.14
 */
@Service
public class ViewReportsService {

    protected static final Logger logger = LoggerFactory.getLogger(ViewReportsService.class);

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private TimeSheetService timeSheetService;

    public void addVacationsForm(ModelAndView modelAndView) {

        VacationsForm vacationsForm = new VacationsForm();
        vacationsForm.setVacationType(0);
        vacationsForm.setRegions(new ArrayList<Integer>());
        vacationsForm.getRegions().add(VacationsForm.ALL_VALUE);
        vacationsForm.setViewMode(VIEW_TABLE);

        modelAndView.addObject("vacationsForm", vacationsForm);
    }

    public void deleteReports(ReportsViewDeleteForm tsDeleteForm, TimeSheetUser securityUser) {
        Integer[] ids = tsDeleteForm.getIds();
        for (Integer i = 0; i < ids.length; i++) {
            Integer id = ids[i];
            TimeSheet timeSheet = timeSheetService.find(id);
            logger.info("Удаляется отчет " + timeSheet + ". Инициатор: " + securityUser.getEmployee().getName());
            timeSheetService.delete(timeSheet);
            sendMailService.performTimeSheetDeletedMailing(timeSheet);
        }
    }
}
