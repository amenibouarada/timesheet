package com.aplana.timesheet.controller;

import com.aplana.timesheet.controller.report.JasperReportModelAndViewGeneratorsFactory;
import com.aplana.timesheet.controller.report.Report04ModelAndViewGenerator;
import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.exception.JReportBuildError;
import com.aplana.timesheet.form.validator.ReportFormValidator;
import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.service.JasperReportService;
import com.aplana.timesheet.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class JasperReportController {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportController.class);

    @Autowired
    private JasperReportService jasperReportService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private JasperReportDAO reportDAO;

    @Autowired
    private ReportFormValidator reportValidator;

    @Autowired
    private JasperReportModelAndViewGeneratorsFactory factory;

    public ModelAndView createReportMAV(Integer number) throws JReportBuildError {
        return createReportMAV(number, null);
    }

    public ModelAndView createReportMAV(Integer number, TSJasperReport form) throws JReportBuildError {
        JasperReportModelAndViewGenerator generator = factory.getJasperReportModelAndViewGeneratorById( number );
        return generator.getModelAndViewForReport(form);
    }

    private ModelAndView showReport (
            BaseReport report,
            BindingResult result,
            Integer printtype,
            int numberReport,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        fillRegionName(report);

        report.setReportDAO(reportDAO);
        reportValidator.validate(report, result);

        ModelAndView mav;

        if (result.hasErrors()) {
            return getModelAndViewForErrors( report, numberReport, result.getAllErrors() );
        }

        if (jasperReportService.makeReport(report, printtype, response, request)) {
	        if (printtype == JasperReportService.REPORT_PRINTTYPE_HTML) {
	            mav = new ModelAndView("empty");
	            mav.addObject("NoPageFormat", "true");
	            return mav;
	        } else {
	        	return null;
	        }
        } else {
            //Если необходимо конкретизировать ошибку для пустого отчета то можно сделать это здесь
            //для всех отчетов выводится error.reportform.nodata
            return getModelAndViewForErrors( report, numberReport,
                    Arrays.asList(new ObjectError(report.getJRName(), new String[]{"error.reportform.nodata"}, null, null)));
        }
    }

    private ModelAndView getModelAndViewForErrors(
            TSJasperReport report, int numberReport, List<ObjectError> errors
    ) throws JReportBuildError {
        ModelAndView mav = createReportMAV( numberReport, report );
        mav.addObject("errors", errors);

        return mav;
    }

    @RequestMapping(value = "/managertools/report/{number}", method = RequestMethod.GET)
    public ModelAndView reportGet(@PathVariable("number") Integer number) throws JReportBuildError {
        return createReportMAV(number);
    }

    @RequestMapping(value = "/managertools/report/1", method = RequestMethod.POST)
    public ModelAndView showReport01(
            @ModelAttribute("reportForm") Report01 report,
            BindingResult result,
            @RequestParam("printtype") Integer printtype,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        return showReport(report, result, printtype, 1, response, request);
    }

    @RequestMapping(value = "/managertools/report/2", method = RequestMethod.POST)
    public ModelAndView showReport02(
            @ModelAttribute("reportForm") Report02 report,
            BindingResult result,
            @RequestParam("printtype") Integer printtype,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        return showReport(report, result, printtype, 2, response, request);
    }

    @RequestMapping(value = "/managertools/report/3", method = RequestMethod.POST)
    public ModelAndView showReport03(
            @ModelAttribute("reportForm") Report03 report,
            BindingResult result,
            @RequestParam("printtype") Integer printtype,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        return showReport(report, result, printtype, 3, response, request);
    }

    @RequestMapping(value = "/managertools/report/4", method = RequestMethod.POST)
    public ModelAndView showReport04(
            @ModelAttribute("reportForm") Report04 report,
            BindingResult result,
            @RequestParam("printtype") Integer printtype,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        return showReport(report, result, printtype, 4, response, request);
    }

    @RequestMapping(value = "/managertools/report/make/4", method = RequestMethod.POST)
    public void makeReport04(@ModelAttribute("reportForm") Report04 report) throws Exception {
        fillRegionName(report);
        Integer formHashCode = report.hashCode();
        report.setReportDAO(reportDAO);
        jasperReportService.makeReport04Async(report, formHashCode);
    }

    @RequestMapping(value = "/managertools/report/checkParamsReport04", method = RequestMethod.POST, headers = "Accept=text/plain;Charset=UTF-8")
    @ResponseBody
    public String checkParamsReport04(@ModelAttribute("reportForm") Report04 report) throws Exception {
        fillRegionName(report);
        Integer formHashCode = report.hashCode();
        return jasperReportService.checkParamsReport04(report, formHashCode);
    }

    @RequestMapping(value = "/managertools/report/download/{idReport}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView downloadReport04(
            @PathVariable("idReport") Integer idReport,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        if (!jasperReportService.downloadReport04(idReport, request, response)) {
            ModelAndView reportNotFound = new ModelAndView("reportNotFound");
            Report04 report = new Report04();
            reportNotFound.addObject("errors",  Arrays.asList(
                    new ObjectError(report.getJRName(), new String[]{"error.reportform.file.deleted"}, null, null)
            ));
            return reportNotFound;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/managertools/report/5", method = RequestMethod.POST)
    public ModelAndView showReport05(
            @ModelAttribute("reportForm") Report05 report,
            BindingResult result,
            @RequestParam("printtype") Integer printtype,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        return showReport(report, result, printtype, 5, response, request);
    }

    @RequestMapping(value = "/managertools/report/6", method = RequestMethod.POST)
    public ModelAndView showReport06(
            @ModelAttribute("reportForm") Report06 report,
            BindingResult result,
            @RequestParam("printtype") Integer printtype,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        return showReport(report, result, printtype, 6, response, request);
    }
    @RequestMapping(value = "/managertools/report/7", method = RequestMethod.POST)
    public ModelAndView showReport07(
            @ModelAttribute("reportForm") Report07 report,
            BindingResult result,
            @RequestParam("printtype") Integer printtype,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws JReportBuildError {
        return showReport(report, result, printtype, 7, response, request);
    }

    // Нужно для отображения названия региона в сформированном отчете
    private void fillRegionName(BaseReport report) {
		List<Integer> regionIds = report.getRegionIds();
		if(regionIds != null && !regionIds.isEmpty()) {
			
			List<String> regionNames = new ArrayList<String>(regionIds.size());
            for ( Integer regionId : regionIds ) {
                Region region = regionService.find( regionId );
                String rName = "";
                if ( region != null ) {
                    rName = region.getName();
                }

                regionNames.add( rName );
            }
			
			report.setRegionNames(regionNames);
		}
    }
	
	/**
	 * Преобразует данные из указаных полей
	 * данные из поля "regionsId" приводит к виду List<Integer>
	 * 
	 * @param request
	 * @param binder
	 * @throws Exception 
	 */
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {

		binder.registerCustomEditor(List.class, "regionIds", new CustomCollectionEditor(List.class) {
			@Override
			protected Object convertElement(Object element) {
				if (element instanceof String) {
					String str = (String) element;
					Integer hold;
					try {
						hold = Integer.parseInt(str);
					} catch (NumberFormatException ex) {
						logger.error("Region Id convertion exception", ex);
						return null;
					}
					return hold;
				}
				return null;
			}
		});
	}
}
