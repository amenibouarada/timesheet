package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * User: bsirazetdinov
 * Date: 22.07.13
 * Time: 16:16
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = DataAccessException.class)
//из-за режима распространения SUPPORTS новая транзакция не начинается, в тестах данные становятся доступны
public class PlannedVacationService {
    private static final Logger logger = LoggerFactory.getLogger(PlannedVacationService.class);

    @Autowired
    private VacationDAO vacationDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    protected ProjectService projectService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Autowired
    private TSPropertyProvider tsPropertyProvider;

    private Date dateCurrent;
    private Date dateAfter;
    private Date dateBefore;

    public PlannedVacationService() {}

    /**
     * Получаем руководителей сотрудников
     */

    public  Map<Employee, Set<Employee>> getEmployeeManagers(List<Employee> employees) {
        Map<Employee, List<Project>> employeeProjects = new HashMap<Employee, List<Project>>();
        for(Employee employee:employees) {
            List<Project> projects = new ArrayList<Project>();
            projects.addAll(projectService.getEmployeeProjectsFromTimeSheetByDates(dateBefore, dateCurrent, employee));
            projects.addAll(projectService.getEmployeeProjectPlanByDates(dateCurrent, dateAfter, employee));

            employeeProjects.put(employee, projects);
        }

        Map<Employee, Set<Employee>> employeeManagers = new HashMap<Employee, Set<Employee>>();

        for(Map.Entry<Employee, List<Project>> entry : employeeProjects.entrySet()) {
            Set<Employee> managers = new HashSet<Employee>();


            Employee manager = entry.getKey();
            while ((manager = manager.getManager()) != null) {
                managers.add(manager);
            }
            if (entry.getKey().getManager2() != null){
                managers.add(entry.getKey().getManager2());
            }

            for (Project project:entry.getValue()) {
                managers.addAll(employeeDAO.getProjectManagers(project));
            }
            employeeManagers.put(entry.getKey(), managers);
        }
             
        return employeeManagers;
    }

    /**
     * Переворачиваем мапу
     */
    public  Map<Employee, Set<Employee>> reverseEmployeeManagersToManagerEmployees(Map<Employee, Set<Employee>> employeeManagers) {
        Map<Employee, Set<Employee>> managerEmployees = new HashMap<Employee, Set<Employee>>();

        for(Map.Entry<Employee, Set<Employee>> entry : employeeManagers.entrySet()) {  // проходим по сотрудник - его менеджеры
            for (Employee manager:entry.getValue()) {                                   // просматриваем его менеджеров
                if(manager.equals(entry.getKey()))continue;
                if(managerEmployees.containsKey(manager)) {                             // если в мапе менеджер - сотрудники есть такой менеджер
                    if(!managerEmployees.get(manager).contains(entry.getKey()))         // если у этого менеджера нет этого сотрудника
                        managerEmployees.get(manager).add(entry.getKey());              // то добавляем
                } else {
                    Set<Employee> employee = new HashSet<Employee>();                // если в мапе менеджер - сотрудники нет такого менеджера
                    employee.add(entry.getKey());                                       // добавляем нового с единственным сотрудником
                    managerEmployees.put(manager, employee);
                }
            }
        }

        return managerEmployees;
    }


    private Employee getManager(Employee e) {
        return e.getManager();
    }

    /**
    * Получаем руководителей чьи "близкие" подчиненые планируют отпуска в ближайшие 2 недели
    */

    public Map<Employee, Set<Vacation>> getManagerEmployeesVacation() {
        final List<Employee> employees = employeeDAO.getEmployeeWithPlannedVacation(dateCurrent, dateAfter);

        Map<Employee, Set<Employee>> employeeManagers = getEmployeeManagers(employees);

        Map<Employee, Set<Employee>> managerEmployees = reverseEmployeeManagersToManagerEmployees(employeeManagers);

        Map<Employee, Set<Vacation>> managerEmployeesVacation = new HashMap<Employee, Set<Vacation>>();

        DictionaryItem approved = dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()); //статус - Утвержденно

        for(Map.Entry<Employee, Set<Employee>> entry : managerEmployees.entrySet()) {
            Set<Vacation> vacations = new TreeSet<Vacation>();

            for (Employee employee:entry.getValue()) {
                //добавление утвержденных отпусков
                vacations.addAll(vacationDAO.findVacationsByStatus(employee.getId(), dateCurrent, dateAfter, approved));
            }

            if (vacations.size() > 0) {
                managerEmployeesVacation.put(entry.getKey(), new TreeSet<Vacation>(vacations));
            }
        }
        
        return managerEmployeesVacation;
    }

    @Transactional
    public void service() {
        setupDates();
        logger.info("Start sending mail to managers! Current dates! dateCurrent {} dateAfter {} dateBefore {}", Arrays.asList(dateCurrent, dateAfter, dateBefore));
        sendMailService.plannedVacationInfoMailing(getManagerEmployeesVacation());
    }

    private void setupDates(){
        final Calendar calendar2 = Calendar.getInstance();
        dateCurrent = calendar2.getTime();
        calendar2.add(Calendar.WEEK_OF_YEAR, 3);
        dateAfter = calendar2.getTime();
        calendar2.add(Calendar.WEEK_OF_YEAR, -4);
        dateBefore = calendar2.getTime();
    }

    public void remindDeletePlannedVacation() {
        // Напоминаем, что планируемый отпуск будет удален
        Integer remindPeriod = tsPropertyProvider.getPlannedVacationDeleteReminderThreshold();
        List<Vacation> remindVacationList = getVacationListByCurrentDayPeriod(remindPeriod);

        for (Vacation vacation : remindVacationList) {
            sendMailService.performPlannedRemind(vacation);
        }

        // Удаляем планируемые отпуска, о которых уже предупреждали
        Integer deletePeriod = tsPropertyProvider.getPlannedVacationDeleteThreshold();
        List<Vacation> deleteVacationList = getVacationListByCurrentDayPeriod(deletePeriod);

        for (Vacation vacation : deleteVacationList) {
            vacationDAO.delete(vacation);
            sendMailService.performPlannedRemove(vacation);
        }
    }

    private List<Vacation> getVacationListByCurrentDayPeriod(Integer period) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        Date date;

        calendar.setTime(currentDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DATE, period);
        date = calendar.getTime();

        List<Vacation> vacationList = vacationDAO.getPlannedVacationByBeginDate(date);

        return vacationList;
    }
}
