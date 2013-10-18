package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Manager;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.form.AssignmentLeadersForm;
import com.aplana.timesheet.form.AssignmentLeadersTableRowForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author iziyangirov
 */
@Controller
public class AssignmentLeadersController {

    @Autowired
    private DivisionService divisionService;
    @Autowired
    private ManagerService managerService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private EmployeeService employeeService;


    @RequestMapping(value = "/admin/update/assignmentleaders", method = RequestMethod.GET)
    public String leadersAssignment() {
        return String.format("redirect:/admin/update/assignmentleaders/%s/%s",
                securityService.getSecurityPrincipal().getEmployee().getDivision().getId(), "view");
    }

    @RequestMapping(value = "/admin/update/assignmentleaders/{divisionId}/{editable}")
    public ModelAndView leadersAssignmentWithFilter(
            @PathVariable("divisionId") Integer filterDivisionId,
            @PathVariable("editable") String editable,
            @ModelAttribute("assignmentLeadersForm") AssignmentLeadersForm alForm
    ) {
        ModelAndView mav = new ModelAndView("assignmentLeaders");
        alForm.setTableRows(getFilledTableRows(filterDivisionId));
        mav.addObject("divisionList", divisionService.getDivisions());
        mav.addObject("currentUserDivisionId", filterDivisionId);
        if (editable.equals("edit")){
            mav.addObject("editable", true);
        }else{
            mav.addObject("editable", false);
        }

        return mav;
    }

    @RequestMapping(value = "/admin/update/assignmentleaders/save/{divisionId}", method = RequestMethod.POST)
    public ModelAndView leadersAssignmentSave(
            @PathVariable("divisionId") Integer filterDivisionId,
            @ModelAttribute("assignmentLeadersForm") AssignmentLeadersForm alForm,
            HttpServletRequest request,
            BindingResult result
    ) {

        for (AssignmentLeadersTableRowForm tableRow : alForm.getTableRows()) {
            Integer divisionId = tableRow.getDivisionId();
            Integer regionId = tableRow.getRegionId();
            Integer leaderId = tableRow.getLeaderId();
            Manager manager = managerService.getManagerByDivisionAndRegion(divisionId, regionId);

            // если такой записи нет, и руководитель не был назначен - переходим к следующей записи
            if (manager == null && leaderId == 0) {
                continue;
            }

            // если такая запись была, а сейчас руководитель удален - удаляем запись
            if (  manager != null && leaderId == 0 ) {
                managerService.deleteManager(manager);
                continue;
            }

            // если записи нет, а руководитель сейчас появился - то запишем изменения
            if (manager == null && leaderId != 0 ){
                manager = new Manager();
                manager.setDivision(divisionService.find(divisionId));
                manager.setRegion(regionService.find(regionId));
                manager.setEmployee(employeeService.find(leaderId));
                managerService.insertManager(manager);
                continue;
            }

            // если руководитель изменился - меняем руководителя и сохраняем
            if (!manager.getEmployee().getId().equals(leaderId)){
                manager.setEmployee(employeeService.find(leaderId));
                managerService.updateManager(manager);
            }
        }

        // вызовем уже имеющийся контроллер и отобразим форму заново с сохраненными изменениями
        return leadersAssignmentWithFilter( filterDivisionId, "view", alForm );
    }

    private List<AssignmentLeadersTableRowForm> getFilledTableRows(Integer filterDivisionId){

        List<Division> divisions = divisionService.getDivisions();
        List<Region> regions = regionService.getRegions();
        List<Employee> employees = employeeService.getEmployees(null, false);

        List <AssignmentLeadersTableRowForm> tableRows = new ArrayList<AssignmentLeadersTableRowForm>(divisions.size());
        for (Division division : divisions){
            if (filterDivisionId == 0 || division.getId().equals(filterDivisionId)) {
                for (Region region : regions){
                    AssignmentLeadersTableRowForm tableRow =
                            new AssignmentLeadersTableRowForm(division.getId(), division.getName(), region.getId(), region.getName());

                    tableRow.setRegionDivisionEmployees(getRegionDivisionEmployees(employees, region, division));

                    Manager manager = managerService.getManagerByDivisionAndRegion(division, region);
                    if (manager != null){
                        tableRow.setLeaderId(manager.getEmployee().getId());
                        tableRow.setLeader(manager.getEmployee().getName());
                    }

                    if (tableRow.getRegionDivisionEmployees().size() != 0){
                        tableRows.add(tableRow);
                    }
                }
            }
        }

        return tableRows;
    }

    private List<Employee> getRegionDivisionEmployees(List<Employee> employees, Region region, Division division){
        List<Employee> result = new ArrayList<Employee>();
        for (Employee employee : employees) {
            if (employee.getRegion().getId().equals(region.getId()) &&
                    employee.getDivision().getId().equals(division.getId())){
                result.add(employee);
            }
        }
        return result;
    }

}
