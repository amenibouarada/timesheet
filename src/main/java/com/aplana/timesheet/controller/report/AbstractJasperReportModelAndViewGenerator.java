package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.controller.JasperReportModelAndViewGenerator;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.reports.TSJasperReport;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.EmployeeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */
public abstract class AbstractJasperReportModelAndViewGenerator implements JasperReportModelAndViewGenerator {

    @Autowired
    private RegionService regionService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EmployeeHelper employeeHelper;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected EmployeeService employeeService;

    @Autowired
    private SecurityService securityService;

    @Override
    public final ModelAndView getModelAndViewForReport( TSJasperReport form ) throws JReportBuildError {
        ModelAndView mav = new ModelAndView(getViewName());
        TSJasperReport result = null;
        if (form != null) {
            result = form;
        } else {
            result = getForm();
            result.setDivisionOwnerId(securityService.getSecurityPrincipal().getEmployee().getDivision().getId());
        }
        mav.addObject("reportForm", result);
        mav.addObject("regionList", regionService.getRegions());
        mav.addObject("defaultDivisionId", securityService.getSecurityPrincipal().getEmployee().getDivision().getId());

        fillSpecificProperties( mav );

        return mav;
    }

    protected abstract void fillSpecificProperties( final ModelAndView mav );

    protected abstract TSJasperReport getForm();

    protected abstract String getViewName();

    protected void fillDivisionList( final ModelAndView mav ){
        fillDivisionList( mav, false, false , false);
    }

    protected void fillDivisionList(
            final ModelAndView mav, boolean fillProjectListJson, boolean fillEmployeeListJson,
            boolean fillProjectListWithOwnerDivisionJson
    ){
        List<Division> divisions = divisionService.getDivisions();
        mav.addObject("divisionList", divisions );
        if( fillProjectListJson )
            mav.addObject("projectListJson", projectService.getProjectListJson(divisions));
        if ( fillProjectListWithOwnerDivisionJson )
            mav.addObject("projectListWithOwnerDivisionJson", projectService.getProjectListWithOwnerDivisionJson());
        if( fillEmployeeListJson )
            mav.addObject("employeeListJson", employeeHelper.getEmployeeListWithDivisionAndRegionAndActiveFlagJson(divisions, null));
    }

    protected void fillWithAllProjects( final ModelAndView mav ) {
        mav.addObject("projectList", projectService.getAllProjects());
    }

    protected void fillFullProjectListJson( final ModelAndView mav ){
        mav.addObject("fullProjectListJson", projectService.getProjectListJson());
    }

    protected void fillWithProjects( final ModelAndView mav ) {
        mav.addObject("projectList", projectService.getProjects());
    }
}
