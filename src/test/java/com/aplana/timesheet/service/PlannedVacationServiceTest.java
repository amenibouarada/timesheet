package com.aplana.timesheet.service;
import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.*;

import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.util.DateTimeUtil;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: bsirazetdinov
 * Date: 07.08.13
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
public class PlannedVacationServiceTest extends AbstractJsonTest{

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Autowired
    ProjectManagerService projectManagerService;
    @Autowired
    private ProjectTaskService projectTaskService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private VacationService vacationService;
    @Autowired
    private EmployeeDAO employeeDAO;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private PlannedVacationService plannedVacationService;
    @Autowired
    private TimeSheetDAO timeSheetDAO;
    @Autowired
    private ProjectDAO projectDAO;
    @Autowired
    private VacationDAO vacationDAO;
    
    
    @Autowired 
    private ProjectService projectService;

    private Project project;
    private Employee employee;
    private TimeSheet timeSheet;
    private Vacation vacation;

    final static String JOB_CODE = "DV";
    final static private Date dateCreate;
    final static private Date dateBegin;
    final static private Date dateEnd;
    static {
        final Calendar calendar2 = Calendar.getInstance();
        dateCreate = calendar2.getTime();
        calendar2.add(Calendar.WEEK_OF_YEAR, 1);
        dateBegin = calendar2.getTime();
        calendar2.add(Calendar.WEEK_OF_YEAR, 1);
        dateEnd = calendar2.getTime();
    }
    static private Random random;

    @Before
    public void initData() {
        random = new Random();
        List<Employee> employees = employeeDAO.getEmployees();
        List<Employee> suitableEmployees = employeeDAO.getEmployees();
        for(Employee emp:employees) {
            if(emp.getJob().getCode().equals(JOB_CODE)) {               	
                suitableEmployees.add(emp);
            }
        }
        employee = suitableEmployees.get(random.nextInt(suitableEmployees.size()));
        
        List<Project> projects = projectDAO.getAll();
        List<Project> suitableProjects = new ArrayList<Project>();
        for(Project proj:projects) {
            if(proj.isActive() && !projectTaskService.getProjectTasks(proj.getId()).isEmpty() && !employeeDAO.getProjectManagers(proj).isEmpty()) {
                suitableProjects.add(proj);           
            }
        }

        project = suitableProjects.get(random.nextInt(suitableProjects.size()));       

        timeSheet = createTestTimeSheet(employee, project);
        timeSheetDAO.storeTimeSheet(timeSheet);

        vacation = createTestVacation(employee);
        vacationDAO.store(vacation);
    }

    private Vacation createTestVacation(Employee employee) {
        Vacation vacation = new Vacation();
        vacation.setComment("unittest comment");
        vacation.setCreationDate(dateCreate);
        vacation.setBeginDate(dateBegin);
        vacation.setEndDate(dateEnd);
        vacation.setAuthor(employee);
        vacation.setStatus(vacation.getStatus());
        vacation.setType(dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId()).get(0));
        vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVEMENT_WITH_PM.getId()));
        vacation.setEmployee(employee);

        return vacation;
    }

    private TimeSheet createTestTimeSheet(Employee employee, Project project) {
        TimeSheet timeSheet = new TimeSheet();
        Set<TimeSheetDetail> timeSheetDetails = new HashSet<TimeSheetDetail>();

        TimeSheetDetail timeSheetDetail = new TimeSheetDetail();

        timeSheetDetail.setProject(project);
        timeSheetDetail.setActCat(dictionaryItemService.getCategoryOfActivity().get(0));
        timeSheetDetail.setWorkplace(dictionaryItemService.getWorkplaces().get(0));
        timeSheetDetail.setActType(dictionaryItemService.getTypesOfActivity().get(0));
        timeSheetDetail.setDuration(8d);
        timeSheetDetail.setDescription("unit test description | lololo ");
        timeSheetDetail.setOther("unit test other");
        timeSheetDetail.setProblem("unit test problem");
        timeSheetDetail.setProjectRole(projectRoleService.getProjectRoles().get(0));
        timeSheetDetail.setProjectTask(projectTaskService.getProjectTasks(project.getId()).get(0));     //projectTaskService.getProjectTasks(project.getId()).get(0)
        timeSheetDetail.setTimeSheet(timeSheet);

        timeSheetDetails.add(timeSheetDetail);

        timeSheet.setTimeSheetDetails(timeSheetDetails);
        timeSheet.setCreationDate(new Date());
        timeSheet.setCalDate(calendarService.find(DateTimeUtil.currentDay()));
        timeSheet.setEmployee(employee);
        timeSheet.setPlan("unit test plan");
        timeSheet.setEffortInNextDay(dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.EFFORT_IN_NEXTDAY.getId()).get(0));
        timeSheet.setOvertimeCause(timeSheet.getOvertimeCause());
        timeSheet.setState(null);

        return timeSheet;
    }

    @Test
    public void testVacation() {

      
        Map<Employee, Set<Vacation>> employeeVacations = plannedVacationService.getManagerEmployeesVacation();
        
        
        boolean isConsist = false;
        for(Map.Entry<Employee, Set<Vacation>> entry : employeeVacations.entrySet()) {
            if (entry.getValue().contains(vacation)) isConsist = true;
        }       
        
        Assert.assertTrue(isConsist);
    }
}
