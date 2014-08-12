package com.aplana.timesheet;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Dictionary;
import com.aplana.timesheet.enums.*;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.security.core.GrantedAuthority;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;

/**
 * User: iziyangirov
 * Date: 11.08.14
 */
public class TestUtils {

    public static Division createDivision(){
        Division division = new Division();
        division.setId(1);
        division.setName("Дивизион №1");
        return division;
    }

    public static Employee createEmployee(){
        return createEmployee(createDivision());
    }

    public static Employee createEmployee(Division division){
        Employee employee = new Employee();
        employee.setId(1);
        employee.setDivision(division);
        employee.setName("User");
        employee.setJob(createProjectRole());

        return employee;
    }

    public static Employee createManager(){
        Employee manager = createEmployee();
        manager.setJob(createProjectRole("DR"));
        return manager;
    }

    private static ProjectRole createProjectRole(){
        return createProjectRole("DV");
    }

    private static ProjectRole createProjectRole(String jobCode){
        ProjectRole projectRole = new ProjectRole();
        projectRole.setActive(true);
        projectRole.setCode(jobCode);
        return projectRole;
    }

    public static ProjectTask createProjectTask(Project project){
        ProjectTask projectTask = new ProjectTask();
        projectTask.setActive(true);
        projectTask.setId(1);
        projectTask.setProject(project);
        projectTask.setTaskName("Task");
        return projectTask;
    }

    public static Project createProject(){
        Project project = new Project();
        project.setActive(true);
        project.setName("Project");
        project.setManager(createManager());
        Set<ProjectTask> projectTasks = new HashSet<ProjectTask>();
        projectTasks.add(createProjectTask(project));
        project.setProjectTasks(projectTasks);
        return project;
    }

    private static DictionaryItem createDictionaryItem(TSEnum enumValue){
        DictionaryItem dictionaryItem = new DictionaryItem();
        dictionaryItem.setId(enumValue.getId());
        dictionaryItem.setValue(enumValue.getName());
        return dictionaryItem;
    }

    private static DictionaryItem createActCat(){
        return createActCat(1);
    }

    private static DictionaryItem createActCat(int enumId){
        return createDictionaryItem(CategoriesOfActivityEnum.getById(enumId));
    }

    private static DictionaryItem createActType(){
        return createActType(12);
    }

    private static DictionaryItem createActType(int enumId){
        return createDictionaryItem(TypesOfActivityEnum.getById(enumId));
    }

    private static DictionaryItem createWorkplace(){
        return createWorkplace(49);
    }

    private static DictionaryItem createWorkplace(int enumId){
        return createDictionaryItem(WorkPlacesEnum.getById(enumId));
    }

    private static DictionaryItem createEffortInNextDay(){
        return createEffortInNextDay(125);
    }

    private static DictionaryItem createEffortInNextDay(int enumId){
        return createDictionaryItem(EffortInNextDayEnum.getById(enumId));
    }

    private static DictionaryItem createTypeOfTimesheetReport(){
        return createTypeOfTimesheetReport(131);
    }

    private static DictionaryItem createTypeOfTimesheetReport(int enumId){
        return createDictionaryItem(TypesOfTimeSheetEnum.getById(enumId));
    }

    private static DictionaryItem createVacationType(){
        return createVacationType(62);
    }

    private static DictionaryItem createVacationType(int enumId){
        return createDictionaryItem(VacationTypesEnum.getById(enumId));
    }

    private static DictionaryItem createVacationStatus(){
        return createVacationStatus(57);
    }

    private static DictionaryItem createVacationStatus(int enumId){
        return createDictionaryItem(VacationStatusEnum.getById(enumId));
    }

    public static TimeSheetDetail createTimeSheetDetail(TimeSheet timeSheet){
        TimeSheetDetail timeSheetDetail = new TimeSheetDetail();

        timeSheetDetail.setProject(createProject());
        timeSheetDetail.setActCat(createActCat());
        timeSheetDetail.setWorkplace(createWorkplace());
        timeSheetDetail.setActType(createActType());
        timeSheetDetail.setDuration(8d);
        timeSheetDetail.setDescription("unit test description | lololo ");
        timeSheetDetail.setProblem("unit test problem");
        timeSheetDetail.setProjectRole(createProjectRole());
        timeSheetDetail.setProjectTask(createProjectTask(createProject()));
        timeSheetDetail.setTimeSheet(timeSheet);

        return timeSheetDetail;
    }

    public static TimeSheet createTimeSheet(){
        TimeSheet timeSheet = new TimeSheet();
        Set<TimeSheetDetail> timeSheetDetails = new HashSet<TimeSheetDetail>();
        timeSheetDetails.add(createTimeSheetDetail(timeSheet));

        timeSheet.setTimeSheetDetails(timeSheetDetails);
        timeSheet.setCreationDate(new Date());
        timeSheet.setCalDate(createCalendar());
        timeSheet.setEmployee(createEmployee());
        timeSheet.setPlan("unit test plan");
        timeSheet.setEffortInNextDay(createEffortInNextDay());
        timeSheet.setOvertimeCause(timeSheet.getOvertimeCause());
        timeSheet.setState(null);
        timeSheet.setType(createTypeOfTimesheetReport());

        return timeSheet;
    }

    private static Calendar createCalendar(){
        Calendar calendar = new Calendar();
        Date date = new Date();
        calendar.setCalDate(new Timestamp(date.getTime()));
        return calendar;
    }

    public static Vacation createVacation() {
        java.util.Calendar calendar2 = java.util.Calendar.getInstance();
        Date dateCreate = calendar2.getTime();
        calendar2.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        Date dateBegin = calendar2.getTime();
        calendar2.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        Date dateEnd = calendar2.getTime();

        Vacation vacation = new Vacation();
        vacation.setComment("unittest comment");
        vacation.setCreationDate(dateCreate);
        vacation.setBeginDate(dateBegin);
        vacation.setEndDate(dateEnd);
        vacation.setAuthor(createEmployee());
        vacation.setStatus(vacation.getStatus());
        vacation.setType(createVacationType());
        vacation.setStatus(createVacationStatus());
        vacation.setEmployee(createEmployee());
        vacation.setRemind(false);

        return vacation;
    }

}
