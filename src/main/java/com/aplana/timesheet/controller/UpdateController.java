package com.aplana.timesheet.controller;

import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.LdapDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/admin*")
public class UpdateController {
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);

    @Autowired
    private EmployeeLdapService employeeLdapService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private LdapDAO ldapDAO;
    @Autowired
    private TSPropertyProvider propertyProvider;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeAssistantService employeeAssistantService;
    @Autowired
    private PlannedVacationService plannedVacationService;

    public void setEmployeeLdapService(EmployeeLdapService employeeLdapService) {
        this.employeeLdapService = employeeLdapService;
    }

    @Autowired
    @Qualifier("reportCheckService")
    private ReportCheckService reportCheckService;

    public void setReportCheckService(ReportCheckService reportCheckService) {
        this.reportCheckService = reportCheckService;
    }

    @Autowired
    OQProjectSyncService oqProjectSyncService;

    @RequestMapping(value = "/update/ldap")
    public String ldapUsersUpdate(Model model) {
        this.employeeLdapService.synchronize();
        model.addAttribute("trace", this.employeeLdapService.getTrace().replaceAll("\n", "<br/>"));
        return "updateLDAP";
    }

    @RequestMapping(value = "/update/siddisabledusersfromldap")
    public String updateSidDeletedUsersFromLdap(Model model) {
        this.employeeLdapService.updateSidDisableddUsersFromLdap();
        model.addAttribute("trace", this.employeeLdapService.getTrace().replaceAll("\n", "<br/>"));
        return "updateLDAP";
    }

    @RequestMapping(value = "/update/sidallusersfromldap")
    public String updateSidAllUsersFromLdap(Model model) {
        this.employeeLdapService.updateSidAllUsersFromLdap();
        model.addAttribute("trace", this.employeeLdapService.getTrace().replaceAll("\n", "<br/>"));
        return "updateLDAP";
    }

    @RequestMapping(value = "/update/jiranameallusersfromldap")
    public String updateJiraNameAllUsersFromLdap(Model model) {
        this.employeeLdapService.updateJiraNameAllUsersFromLdap();
        model.addAttribute("trace", this.employeeLdapService.getTrace().replaceAll("\n", "<br/>"));
        return "updateLDAP";
    }

    @RequestMapping(value = "/update/checkreport")
    public String checkReportUpdate(Model model) {
        this.reportCheckService.storeReportCheck();
        model.addAttribute("trace", this.reportCheckService.getTrace().replaceAll("\n", "<br/>"));
        return "checkEmails";
    }

    @RequestMapping(value = "/update/oqsync")
    public ModelAndView oqSyncUpdate(Model model) {
        oqProjectSyncService.sync();
        ModelAndView mav = new ModelAndView("oqSync");
        mav.addObject("trace", oqProjectSyncService.getTrace().replaceAll("\n", "<br/>"));
        return mav;
    }

    @RequestMapping(value = "/update/showalluser")
    public String showAllUser(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(TimeSheetConstants.COOKIE_SHOW_ALLUSER, "active");
        cookie.setPath("/");
        cookie.setMaxAge(99999999);
        response.addCookie(cookie);
        return "redirect:/admin";
    }

    @RequestMapping(value = "/update/hidealluser")
    public String hideAllUser(HttpServletRequest request, HttpServletResponse response) {
        if (employeeService.isShowAll(request)) {
            Cookie cookie = new Cookie(TimeSheetConstants.COOKIE_SHOW_ALLUSER, "deactive");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "redirect:/admin";
    }

    @RequestMapping(value = "/update/properties")
    public String updateProperties(HttpServletRequest request, HttpServletResponse response) {
        TSPropertyProvider.updateProperties();

        return "redirect:/admin";
    }

    @RequestMapping(value = "/update/propertiesAJAX", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String updatePropertiesAXAX() {
        TSPropertyProvider.updateProperties();
        return TSPropertyProvider.getProperiesFilePath();
    }

    @RequestMapping(value = "/update/objectSid")
    public String updateObjectSids() {
        Iterable<Division> divisionsFromDb = Iterables.filter(divisionService.getAllDivisions(), new Predicate<Division>() {
            @Override
            public boolean apply(@Nullable Division input) {
                return !input.getNotToSyncWithLdap();
            }
        });

        List<Map> divisions = ldapDAO.getDivisions();
        for (final Division division : divisionsFromDb) {

            logger.debug("Division – {}", division.getName());

            if (StringUtils.isBlank(division.getObjectSid())) {
                Map map = Iterables.find(divisions, new Predicate<Map>() {
                    @Override
                    public boolean apply(@Nullable Map input) {
                        return division.getLdapName().equalsIgnoreCase((String) input.get(propertyProvider.getLdapFieldForDivisionName()));
                    }
                });
                division.setObjectSid(LdapUtils.convertBinarySidToString((byte[]) map.get(propertyProvider.getLdapFieldForSID())));
                divisionService.setDivision(division);
            }
        }

        List<Employee> employeesForSync = employeeService.getEmployeesForSync();

        for (Employee employee : employeesForSync) {
            if (StringUtils.isBlank(employee.getObjectSid())) {
                EmployeeLdap employeeFromLdap = ldapDAO.getEmployeeByLdapName(employee.getLdap());
                if (employeeFromLdap == null) {
                    employeeFromLdap = ldapDAO.getEmployeeByDisplayName(employee.getName());
                    employee.setLdap(employeeFromLdap.getLdapCn());
                }

                employee.setObjectSid(employeeFromLdap.getObjectSid());
                employeeService.setEmployee(employee);
            }
        }

        return "redirect:/admin";
    }

    @RequestMapping(value = "/update/employeeassistantactivestatus")
    public String updateEmployeeAssistantActiveStatus() {
        employeeAssistantService.changeAssistantActivity();
        return "redirect:/admin";
    }

    /**
     * Запуск рассылки писем о ближайщих отпусках сотрудников
     * @return ошибку или удачу
     */
    @RequestMapping(value = "/update/schedulerplannedvacationcheck", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String startSchedulerPlannedVacationCheck() {
        try {
            plannedVacationService.service();
        } catch (Exception e) {
            return "Ошибка! " + e.getLocalizedMessage();
        }
        return "операция завершена успешно.";
    }
}
