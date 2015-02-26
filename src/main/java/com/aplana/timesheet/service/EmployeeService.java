package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNodeBuilder;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.RegionDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.PermissionsEnum;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static argo.jdom.JsonNodeBuilders.*;
import static argo.jdom.JsonNodeFactories.string;
import static com.aplana.timesheet.system.constants.RoleConstants.ROLE_ADMIN;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    public static final String EMPLOYEE_ID      = "employee_id";
    public static final String EMPLOYEE_NAME    = "employee_name";
    public static final String DIVISION_ID      = "divisionId";
    public static final String ID               = "id";
    public static final String MANAGER_ID       = "managerId";

    @Autowired
    public VelocityEngine velocityEngine;
    @Autowired
    TimeSheetService timeSheetService;
    @Autowired
    private EmployeeDAO employeeDAO;
    @Autowired
    private RegionService regionService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    RegionDAO regionDAO;
    @Autowired
    private VacationService vacationService;

    /**
     * Возвращает сотрудника по идентификатору.
     *
     * @param id идентификатор сотрудника
     * @return объект класса Employee либо null, если сотрудник
     *         с указанным id не найден.
     */
    @Transactional(readOnly = true)
    public Employee find(Integer id) {
        return employeeDAO.find(id);
    }

    public Boolean isShowAll(HttpServletRequest request) {
        if (!request.isUserInRole(ROLE_ADMIN))
            return false;

        Boolean isShowAll = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(TimeSheetConstants.COOKIE_SHOW_ALLUSER)) {
                    isShowAll = true;
                    break;
                }
            }
        }
        return isShowAll;
    }

    @Transactional(readOnly = true)
    public Employee findByEmail(String mail) {
        return employeeDAO.findByEmail(mail);
    }

    public Employee findByLdapName(String ldapName) {
        return employeeDAO.findByLdapName(ldapName);
    }

    /**
     * Возвращает сотрудника по имени.
     *
     * @param name имя сотрудника
     * @return объект класса Employee либо null, если сотрудник
     *         с указанным именем не найден.
     */
    @Transactional(readOnly = true)
    public Employee find(String name) {
        return employeeDAO.find(name);
    }

    /**
     * Возвращает список сотрудников
     *
     * @param division    Если null, то поиск осуществляется без учета подразделения,
     *                    иначе с учётом подразделения
     * @param filterFired Отоброжать ли уволенных сотрудников
     * @return список действующих сотрудников.
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployees(Division division, Boolean filterFired) {
        List<Employee> result;
        if (filterFired == true) {
            result = employeeDAO.getAllEmployeesDivision(division);
        } else {
            result = employeeDAO.getEmployees(division);
        }
        return result;
    }

    /**
     * Возвращает список сотрудников как json {id, name}
     * @param employeeList
     * @return
     */
    public String getEmployeeListAsJson(List<Employee> employeeList){
        JsonArrayNodeBuilder builder = anArrayBuilder();
        for(Employee employee : employeeList){
            builder.withElement(getEmployeeAsJSONBulder(employee));
        }
        return JsonUtil.format(builder.build());
    }

    public JsonObjectNodeBuilder getEmployeeAsJSONBulder(Employee employee){
        JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder();
        objectNodeBuilder.withField(EMPLOYEE_ID, aNumberBuilder(employee.getId().toString()));
        objectNodeBuilder.withField(EMPLOYEE_NAME, aStringBuilder(employee.getName()));
        return objectNodeBuilder;
    }

    public String getAllEmployeesJSON() {
        return getEmployeeListAsJson(employeeDAO.getAllEmployees());
    }

    /**
     * Возвращает список сотрудников
     *
     * @param divisions                    Если null, то поиск осуществляется по всем подразделениям (если 1 исп. функцию createDivisionList)
     * @param managers                     Если null, то поиск осуществляется по всем руководителям (если 1 исп. createManagerList)
     * @param regions                      Если null, то поиск осуществляется по всем регионам (см. также createRegionsList)
     * @param projects                     Если null, то поиск осуществляется по всем проектам (если 1 исп. createProjectList)
     * @param beginDate                    Интервал работы на проекте/проектах (если null - 01.01.1900)
     * @param endDate                      Интервал работы на проекте/проектах (если null - 01.01.2100)
     * @param lookPreviousTwoWeekTimesheet - посмотреть были ли на проектах в последнии две недели списания занятости,
     *                                     т.е. если за последние две недели (от beginDate) пользователь списывал занятость
     *                                     по указанным проектам (projects), то значит он будет считаться как на этом проекте,
     *                                     даже если в employee_project_plan записей нет
     * @return список сотрудников
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployees(List<Division> divisions, List<Employee> managers, List<Region> regions,
                                       List<Project> projects, Date beginDate, Date endDate,
                                       boolean lookPreviousTwoWeekTimesheet) {
        return employeeDAO.getEmployees(divisions, managers, regions, projects, beginDate, endDate, lookPreviousTwoWeekTimesheet);
    }

    // если первый 0 - вернет null => все регионы
    public List<Region> createRegionsList(List<Integer> regions) {
        if (regions == null || regions.get(0) <= 0) return null;
        List<Region> result = new ArrayList<Region>();
        for (Integer regionId : regions) {
            result.add(regionService.find(regionId));
        }
        return result;
    }

    // если 0 - вернет null => все подразделения
    public List<Division> createDivisionList(Integer division) {
        if (division == null || division <= 0) return null;
        List<Division> result = new ArrayList<Division>();
        result.add(divisionService.find(division));
        return result;
    }

    // если 0 - вернет null => все руководители
    public List<Employee> createManagerList(Integer manager) {
        if (manager == null || manager <= 0) return null;
        List<Employee> result = new ArrayList<Employee>();
        result.add(find(manager));
        return result;
    }

    // если 0 - вернет null => все проекты
    public List<Project> createProjectList(Integer project) {
        if (project == null || project <= 0) return null;
        List<Project> result = new ArrayList<Project>();
        result.add(projectService.find(project));
        return result;
    }

    /**
     * Возвращает список доступных для синхронизации с ldap сотрудников.
     *
     * @param division Если null, то поиск осуществляется без учета подразделения,
     *                 иначе с учётом подразделения
     * @return список сотрудников для синхронизации
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesForSync(Division division) {
        return employeeDAO.getEmployeesForSync(division);
    }

    /**
     * Сохраняет в базе нового сотрудника, либо обновляет данные уже
     * существующего сотрудника.
     *
     * @param employee
     */
    @Transactional
    public void setEmployee(Employee employee) {
        save(employee);
    }

    /**
     * Сохраняет в базе новых сотрудников, либо обновляет данные уже
     * существующих сотрудников.
     *
     * @param employees
     */
    @Transactional
    public StringBuffer setEmployees(List<Employee> employees) {
        // Если город не найден - "Другой район"
        Region defaultCity = regionDAO.find(1);
        StringBuffer trace = new StringBuffer();

        for (Employee emp : employees) {
            if (emp.getRegion() == null) {
                emp.setRegion(defaultCity);
            }
            try {
                Employee result = employeeDAO.findByObjectSid(emp.getObjectSid());
                if ( result != null  && !result.isNotToSync() && result.getEndDate() == null) {
                    trace.append(String.format(
                            "%s user: %s %s\n", emp.getId() != null ? "Updated" : "Added", emp.getEmail(), emp.getName()
                    ));
                    save(emp);
                } else if (result == null || result.isNotToSync()) {
                    trace.append(String.format(
                            "\nUser: %s %s marked not_to_sync.(Need update)\n%s\n\n",
                            emp.getEmail(), emp.getName(), emp.toString()));
                } else if (result.getEndDate() != null) {
                    trace.append(String.format(
                            "\nUser: %s %s is dismiss \n%s\n\n",
                            emp.getEmail(), emp.getName(), emp.toString()));
                }
            } catch (Exception e) {
                trace.append("exception: " + e);
                logger.debug("update user failed", e);
            }
        }
        trace.append("\n\n");
        return trace;
    }

    /**
     * Сохраняет в базе нового сотрудника, либо обновляет данные уже
     * существующего сотрудника.
     *
     * @param employee
     */
    public Employee save(Employee employee) {
        Employee empDb = employeeDAO.getEmployee(employee.getEmail());
        //если в базе есть дата увольнения, и дата не совпадает с лдапом, то дату в базе не меняем
        if (empDb != null && empDb.getEndDate() != null && !empDb.getEndDate().equals(employee.getEndDate())) {
            employee.setEndDate(empDb.getEndDate());
            empDb = employeeDAO.save(employee);
            logger.debug("Final date not equal in ldap and database for Employee object id = {}", empDb.getId());
        } else {
            empDb = employeeDAO.save(employee);
        }
        return empDb;
    }

    public Double getWorkDaysOnIllnessWorked(Employee employee, Date beginDate, Date endDate) {
        return employeeDAO.getWorkDaysOnIllnessWorked(employee, beginDate, endDate);
    }

    public boolean isEmployeeAdmin(Integer employeeId) {
        return isEmployeeHasPermissions(employeeId, PermissionsEnum.ADMIN_PERMISSION);
    }

    public boolean isEmployeeHasPermissions(Integer employeeId, final PermissionsEnum permissions) {
        final Employee employee = find(employeeId);

        return Iterables.any(employee.getPermissions(), new Predicate<Permission>() {
            @Override
            public boolean apply(@Nullable Permission permission) {
                return permission.getId().equals(permissions.getId());
            }
        });
    }

    public List<Employee> getDivisionEmployeesByManager(Integer divisionId, Date date, List<Integer> regionIds, List<Integer> projectRoleIds, Integer managerId) {
        List<Employee> employees =
                getSubManagersEmployee(divisionId, date, regionIds, projectRoleIds, managerId);

        Collections.sort(employees, new Comparator<Employee>() {
            @Override
            public int compare(Employee o1, Employee o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return employees;
    }

    // возвращает список сотрудников по переданным параметром,
    // а также рекурсивно всех подчиненных у найденных сотрудников
    public List<Employee> getSubManagersEmployee(
            Integer divisionId, Date date, List<Integer> regionIds, List<Integer> projectRoleIds, Integer managerId){

        Set<Employee> employeesByManager = new HashSet<Employee>(employeeDAO.getDivisionEmployeesByManager(divisionId, date, regionIds, projectRoleIds, managerId));
        Set<Employee> employeesBySubManagers = new HashSet<Employee>();
        for (Employee employee : employeesByManager) {
            if (!(employeesBySubManagers.contains(employee))) {
                employeesBySubManagers.add(employee);
                employeesBySubManagers.addAll(getSubManagersEmployee(divisionId, date, regionIds, projectRoleIds, employee.getId()));
            }
        }
        employeesByManager.addAll(employeesBySubManagers);
        return new ArrayList<Employee>(employeesByManager);
    };

    public List<Employee> getEmployees() {
        return employeeDAO.getEmployees();
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public Employee getEmployeeFromBusinessTrip(Integer reportId) {
        return employeeDAO.tryGetEmployeeFromBusinessTrip(reportId);
    }

    public Employee getEmployeeFromIllness(Integer reportId) {
        return employeeDAO.tryGetEmployeeFromIllness(reportId);
    }

    public Boolean isLineManager(Employee employee) {
        return employeeDAO.isLineManager(employee);
    }


    public Boolean isEmployeeDivisionLeader(Integer employeeID) {
        return employeeDAO.isEmployeeDivisionLeader(employeeID);
    }

    /**
     * Получаем список менеджеров, которые еще не приняли решение по отпуску
     */
    public List<Employee> getProjectManagersThatDoesntApproveVacation(Project project, Vacation vacation) {
        return employeeDAO.getProjectManagersThatDoesntApproveVacation(project, vacation);
    }

    /**
     * Возвращает JSON-строку, содержащую список всех главных руководителей проектов и связанные с ними проекты.
     *
     * @return Главные руководители и связанные проекты
     */
    public String getMainProjectManagersJSON() {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        // Заполнение списка руководителей проектов для варианта "Все подразделения"
        JsonArrayNodeBuilder allDivisionsBuilder = anArrayBuilder();
        for (Employee manager : employeeDAO.getMainProjectManagers()) {
            allDivisionsBuilder.withElement(
                    anObjectBuilder()
                            .withField(MANAGER_ID, aStringBuilder(manager.getId().toString()))
                            .withField("name", aStringBuilder(manager.getName()))
            );
        }
        builder.withElement(
                anObjectBuilder()
                        .withField(DIVISION_ID, aStringBuilder("-1"))
                        .withField("managers", allDivisionsBuilder)
        );

        // Заполнение списка руководителей проектов для варианта "Не проставлен (null)"
        JsonArrayNodeBuilder nullDivisionBuilder = anArrayBuilder();
        for (Employee manager : employeeDAO.getMainProjectManagers(null)) {
            nullDivisionBuilder.withElement(
                    anObjectBuilder()
                            .withField(MANAGER_ID, aStringBuilder(manager.getId().toString()))
                            .withField("name", aStringBuilder(manager.getName()))
            );
        }
        builder.withElement(
                anObjectBuilder()
                        .withField(DIVISION_ID, aStringBuilder("0"))
                        .withField("managers", nullDivisionBuilder)
        );

        // Заполнение списков руководителей проектов для подразделений
        for (Division division : divisionService.getAllDivisions()) {
            JsonArrayNodeBuilder divisionBuilder = anArrayBuilder();
            for (Employee manager : employeeDAO.getMainProjectManagers(division)) {
                divisionBuilder.withElement(
                        anObjectBuilder()
                                .withField(MANAGER_ID, aStringBuilder(manager.getId().toString()))
                                .withField("name", aStringBuilder(manager.getName()))
                );
            }
            builder.withElement(
                    anObjectBuilder()
                            .withField(DIVISION_ID, aStringBuilder(division.getId().toString()))
                            .withField("managers", divisionBuilder)
            );
        }

        return JsonUtil.format(builder);
    }

    private JsonNodeBuilder getEmployeesBuilder(List<Employee> employees) {
        JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();
        for (Employee employee : employees) {
            employeesBuilder.withElement(
                    anObjectBuilder()
                            .withField("employeeId", aStringBuilder(employee.getId().toString()))
                            .withField("name", aStringBuilder(employee.getName()))
                            .withField("birthday", aStringBuilder(DateTimeUtil.getDayMonthFromDate(employee.getBirthday())))
            );
        }
        return employeesBuilder;
    }

    /**
     * Возвращает JSON-строку, содержащую список всех подразделений и их сотрудников, включая неактивные подразделения
     * и уволенных сотрудников.
     *
     * @return Главные руководители и связанные проекты
     */
    public String getDivisionsEmployeesJSON() {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        // Заполнение списка сотрудников для варианта "Все подразделения"
        builder.withElement(
                anObjectBuilder()
                        .withField(DIVISION_ID, aStringBuilder("-1"))
                        .withField("employees", getEmployeesBuilder(employeeDAO.getAllEmployees()))
        );

        // Заполнение списков сотрудников для подразделений
        Map<Division, List<Employee>> divisionsEmployees =
                employeeDAO.getAllEmployees((ArrayList<Division>) divisionService.getAllDivisions());

        for (Division division : divisionsEmployees.keySet()) {
            builder.withElement(
                    anObjectBuilder()
                            .withField(DIVISION_ID, aStringBuilder(division.getId().toString()))
                            .withField("active", aStringBuilder(String.valueOf(division.isActive())))
                            .withField("employees", getEmployeesBuilder(divisionsEmployees.get(division)))
            );
        }

        return JsonUtil.format(builder);
    }

    /**
     * Получаем младших менеджеров проекта (тимлиды, ведущие аналитики) той же специальности (роли)
     */
    public List<Employee> getProjectManagersSameRole(Project project, Employee employee) {
        return employeeDAO.getProjectManagersSameRole(project, employee);
    }

    /* ToDo нельзя ли объединить метод getJuniorProjectManagersAndProjects для vacation и illness
    * скорее всего, когда проект перейдет на Java 8 можно будет это реализовать, когда можно будет
    * передавать функции в качестве аргументов.
    */
    /**
     * получаем список младших (тимлиды, ведущие аналитики) руководителей проектов, на которых сотрудник планирует свою занятость в даты болезни.
     */
    public Map<Employee, List<Project>> getJuniorProjectManagersAndProjects(List<Project> employeeProjects, Illness illness) {
        Map<Employee, List<Project>> managersAndProjects = new HashMap<Employee, List<Project>>();
        for (Project project : employeeProjects) {
            if (!illness.getEmployee().getId().equals(project.getManager().getId())) {        //если оформляющий отпуск - руководитель этого проекта, то по этому проекту писем не рассылаем
                List<Employee> managers = getProjectManagersSameRole(project, illness.getEmployee());
                for (Employee manager : managers) {
                    if (!manager.getId().equals(illness.getEmployee().getId())) {       //отсеиваем сотрудника, если он сам руководитель
                        if (managersAndProjects.containsKey(manager)) {
                            List<Project> projects = managersAndProjects.get(manager);
                            projects.add(project);
                        } else {
                            ArrayList<Project> projectArrayList = new ArrayList<Project>(1);
                            projectArrayList.add(project);
                            managersAndProjects.put(manager, projectArrayList);
                        }
                    }
                }
            }
        }

        return managersAndProjects;
    }

    /**
     * получаем список младших (тимлиды, ведущие аналитики) руководителей проектов, на которых сотрудник планирует свою занятость в даты отпуска.
     */
    public Map<Employee, List<Project>> getJuniorProjectManagersAndProjects(List<Project> employeeProjects, final Vacation vacation) {
        Map<Employee, List<Project>> managersAndProjects = new HashMap<Employee, List<Project>>();
        for (Project project : employeeProjects) {
            if (!vacation.getEmployee().getId().equals(project.getManager().getId())) {        //если оформляющий отпуск - руководитель этого проекта, то по этому проекту писем не рассылаем
                List<Employee> managers = getProjectManagersThatDoesntApproveVacation(project, vacation);
                for (Employee manager : managers) {
                    if (!manager.getId().equals(vacation.getEmployee().getId())) {       //отсеиваем сотрудника, если он сам руководитель
                        if (managersAndProjects.containsKey(manager)) {
                            List<Project> projects = managersAndProjects.get(manager);
                            projects.add(project);
                        } else {
                            ArrayList<Project> projectArrayList = new ArrayList<Project>(1);
                            projectArrayList.add(project);
                            managersAndProjects.put(manager, projectArrayList);
                        }
                    }
                }
            }
        }

        return managersAndProjects;
    }

    public List<Employee> getEmployeesForSync() {
        return employeeDAO.getEmployeesForSync();
    }

    public List<Employee> getManagerListForAllEmployee() {
        return employeeDAO.getManagerListForAllEmployee();
    }

    public String getManagerListJson() {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        List<Employee> managers = employeeDAO.getManagerListForAllEmployee();
        builder.withElement(
                anObjectBuilder().
                        withField("number", JsonUtil.aNumberBuilder(0)).
                        withField("value", string(StringUtils.EMPTY))
        );
        for (Employee manager : managers) {
            final JsonArrayNodeBuilder regionBuilder = anArrayBuilder();
            for (Integer region : employeeDAO.getRegionsWhereManager(manager.getId())) {
                regionBuilder.withElement(
                        anObjectBuilder().withField(ID, aStringBuilder(region.toString())));                // ToDo заменить на regionId
            }
            builder.withElement(
                    anObjectBuilder().
                            withField(ID, JsonUtil.aStringBuilder(manager.getId())).                        // ToDo заменить на managerId
                            withField("name", aStringBuilder(manager.getName())).
                            withField("division", aStringBuilder(manager.getDivision().getId().toString())).  // ToDo заменить на divisionId
                            withField("regionWhereMan", regionBuilder)
            );
        }
        return JsonUtil.format(builder);
    }

    /**
     * Получаем список линейных руководителей
     *
     * @param employee
     * @return
     */
    public List<Employee> getLinearEmployees(Employee employee) {
        List<Employee> employees = new ArrayList<Employee>();
        Employee manager = employee.getManager();
        if (manager != null && !employees.contains(manager)) {
            employees.add(manager);
            employees.addAll(getLinearEmployees(manager));
        }
        return employees;
    }

    /**
     * Ищет сотрудника по LdapCN
     */
    public Employee findByLdapCN(String LdapCN) {
        return employeeDAO.findByLdapCN(LdapCN);
    }

    /**
     * Ищет сотрудника по SID
     */
    public Employee findByLdapSID(String LdapCN) {
        return employeeDAO.findByLdapSID(LdapCN);
    }

    public List<Employee> getEmployeeByRegionAndManagerRecursiveAndDivision(List<Integer> regions, Integer divisionId, Integer manager) {
        return employeeDAO.getEmployeeByRegionAndManagerRecursiveAndDivision(regions, divisionId, manager);
    }

    public String checkDayIsVacation(Integer employeeId, String reportDate) {
        Date date = DateTimeUtil.parseStringToDateForDB(reportDate);
        Employee user = employeeDAO.find(employeeId);
        Boolean isVacationDay = vacationService.isDayVacationWithoutPlanned(user, date);
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                withField("isVacationDay", JsonNodeBuilders.aStringBuilder(isVacationDay.toString()));
        builder.withElement(objectNodeBuilder);

        return JsonUtil.format(builder.build());
    }

    /*
     * Проверяет, что сотрудник не начал работать на указанную дату
     *
     * @return true - не начал.
     */
    public Boolean checkNotStartWorkByDate(Integer employeeId, String reportDate) {
        Date date = DateTimeUtil.parseStringToDateForDB(reportDate);
        Employee employee = employeeDAO.find(employeeId);
        if (employee.getStartDate().after(date)) {
            return true;
        }
        return false;
    }

    /*
     * Возвращает список сотрудников выбранного подразделения, у которых день рождения в указанный месяц
     *
     * @param division
     * @param birthdayMonth - месяц на который ищутся дни рождения (нумерация с 0)
     *
     * @return List<Employee>
     */
    public List<Employee> getEmployeesForDivisionWithBirthdayMonth(Division division, Integer birthdayMonth) {
        return employeeDAO.getEmployeesForDivisionWithBirthdayMonth(division, birthdayMonth);
    }

    public Employee tryFindByFioRegionDivision(String fio, Region region, Division division) {
        String[] split = fio.split("\\s+");
        if (split.length == 3) {
            String name = split[0].concat(" ").concat(split[1]);
            return employeeDAO.tryFindByFioRegionDivision(name, split[3], region, division);
        } else if (split.length == 2) {
            String name = split[0].concat(" ").concat(split[1]);
            return employeeDAO.tryFindByFioRegionDivision(name, null, region, division);
        }

        return null;
    }
}