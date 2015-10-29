package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.TypesOfTimeSheetEnum;
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Calendar;
import java.util.*;

@Repository
public class EmployeeDAO {

    public static final int ALL_REGIONS = -1;
    public static final int ALL_PROJECT_ROLES = -1;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeDAO.class);

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Возвращает сотрудника по идентификатору.
	 * @param id идентификатор сотрудника
	 * @return объект класса Employee либо null, если сотрудник
	 *         с указанным id не найден.
	 */
	public Employee find(Integer id) {
		if (id == null) {
			logger.warn("For unknown reasons, the Employee ID is null.");
			return null;
		}
		return entityManager.find(Employee.class, id);
	}
	
	/**
	 * Возвращает сотрудника по имени.
	 * @param name имя сотрудника
	 * @return объект класса Employee либо null, если сотрудник
	 *         с указанным именем не найден.
	 */
	public Employee find(String name) {
		if (name == null || "".equals(name)) { return null; }

		Query query = entityManager.createQuery(
                "from Employee as e where e.name=:name"
        ).setParameter( "name", name );
		try {
            return (Employee) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Employee with name '{}' not found.", name);
		} catch (NonUniqueResultException e) {
			logger.warn("More than one employee with name '{}' was found.", name);
		}
        return null;
	}

    /**
     * Возвращает сотрудника по email
     * @param email сотрудника
     * @return объект класса Employee либо null, если сотрудник
     *         с указанным именем не найден.
     */

    public Employee findByEmail(String email) {
        if ("".equals(email)) { return null; }

        Query query = entityManager.createQuery(
                "select e from Employee as e where e.email=:email"
        ).setParameter( "email", email );
        try {
            Employee result = (Employee) query.getSingleResult();
            // Загружаем детализации, чтобы не было проблем из-за lazy загрузки.
            Hibernate.initialize(result);
            Hibernate.initialize(result.getDivision());
            Hibernate.initialize(result.getManager());
            return result;
        } catch (NoResultException e) {
            logger.warn("Employee with email '{}' not found.", email);
        } catch (NonUniqueResultException e) {
            logger.warn("More than one employee with email '{}' was found.", email);
        }
        return null;
    }


	public Employee findByLdapName(String ldap) {
		Query query = this.entityManager.createQuery(
                "select e from Employee e where e.ldap like :ldap"
        ).setMaxResults( 1 ).setParameter("ldap", "%" + ldap + "%");

		return (Employee) Iterables.getFirst(query.getResultList(), null);
	}

    public List<Employee> getEmployeesForSync() {
        return getEmployeesForSync(null);
    }

    /**
	 * Возвращает список доступных для синхронизации с ldap сотрудников.
	 * @param division Если null, то поиск осуществляется без учета подразделения,
	 * 				   иначе с учётом подразделения
	 * @return список сотрудников для синхронизации
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getEmployeesForSync(Division division) {
		Query query;
		if (division == null) {
			query = entityManager.createQuery(
                    "from Employee as e where e.notToSync=:notToSync and e.division.active = true"
            );
		} else {
			query = entityManager.createQuery(
                    "from Employee as e where e.notToSync=:notToSync and e.division=:division"
            );
			query.setParameter("division", division);
		}
		query.setParameter("notToSync", false);

        return query.getResultList();
	}

    @SuppressWarnings("unchecked")
    public List<Employee> getAllEmployeesDivision(Division division) {
        Query query;
        if (division == null) {
            query = entityManager.createQuery("FROM Employee");
        } else {
            query = entityManager.createQuery("FROM Employee AS e WHERE e.division=:division");
            query.setParameter("division", division);
        }

        return query.getResultList();
    }

    /**
     * Возвращает список всех работников у которых начала запланированного отпуска находится между <b>begin</b> и <b>end</b>
     * @param begin
     * @param end
     * @return List<Employee>
     */
    public List<Employee> getEmployeeWithPlannedVacation (Date begin, Date end) {
        Query query = this.entityManager.createQuery(
                "select emp from Employee as emp where emp.id in " +
                        "(select v.employee.id from Vacation as v where v.beginDate >= :begin and v.beginDate < :end)"
        ).setParameter("begin", begin)
         .setParameter("end", end);

        return query.getResultList();
    }

	/**
	 * Возвращает список действующих сотрудников указанного подразделения
	 * @param division подразделениe
	 * @return список сотрудников подразделения с идентификатором division
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getEmployees(Division division) {
		Query query;

        Date maxModDate = new Date();

		if (division == null) {
			query = entityManager.createQuery(
                    //действующий сотрудник-который на текущий момент либо не имеет endDate, либо endDate<=cuDate
                    "FROM Employee AS emp " +
                            "WHERE (emp.endDate IS NOT NULL " +
                                "AND emp.endDate >= :curDate) " +
                                "OR (emp.endDate IS NULL) " +
                            "ORDER BY emp.name"
            ).setParameter("curDate", maxModDate);
		} else {
			query = entityManager.createQuery(
                    "FROM Employee AS emp " +
                            "WHERE (emp.division=:division " +
                                "AND emp.endDate IS NOT NULL " +
                                "AND emp.endDate >= :curDate) " +
                                "OR (emp.division=:division " +
                                "AND emp.endDate IS NULL) " +
                            "ORDER BY emp.name"
            ).setParameter("curDate", maxModDate).setParameter("division", division);
		}

        return query.getResultList();
	}

	/**
	 * Сохраняет в базе нового сотрудника, либо обновляет данные уже
	 * существующего сотрудника.
	 * @param employee
	 */
    public Employee save(Employee employee) {
        Employee empMerged = entityManager.merge(employee);
        entityManager.flush();
        logger.info("Persistence context synchronized to the underlying database.");
        logger.debug("Flushed Employee object id = {}", empMerged.getId());

        employee.setId(empMerged.getId());

        return empMerged;
    }

    public Employee getEmployee(String email) {
        if(email!=null && !email.isEmpty()){
            Employee employee = (Employee) Iterables.getFirst(entityManager.createQuery(
                    "FROM Employee emp WHERE email = :email"
            ).setParameter("email", email).getResultList(), null);

            return employee;
        }
        return null;
    }

    public boolean isNotToSync(Employee employee) {
        Employee result = findByObjectSid(employee.getObjectSid());
        return result != null && result.isNotToSync();
    }

    public Double getWorkDaysOnIllnessWorked(Employee employee, Date beginDate, Date endDate) {
        Query query = entityManager.createQuery(
                "select sum(tsd.duration) from TimeSheetDetail tsd " +
                        "inner join tsd.timeSheet ts " +
                        "where (tsd.actType.id = 14 or tsd.actType.id = 12 or tsd.actType.id = 13 or tsd.actType.id = 42) " +
                        "and (ts.calDate.calDate between :beginDate and :endDate) " +
                        "and (ts.employee = :employee) " +
                        "and (ts.type = " + TypesOfTimeSheetEnum.REPORT.getId() + ")"
        );
        query.setParameter("beginDate", beginDate);
        query.setParameter("endDate", endDate);
        query.setParameter("employee", employee);

        if (query.getResultList().get(0) != null){
            return ((Double) query.getResultList().get(0));
        } else {
            return 0d;
        }

    }

    public Employee findByObjectSid(String objectSid) {
        Employee employee = (Employee) Iterables.getFirst(entityManager.createQuery(
                "FROM Employee emp WHERE objectSid = :objectSid"
        ).setParameter("objectSid", objectSid).getResultList(), null);

        if(employee == null) return null;

        Hibernate.initialize(employee.getDivision());
        Hibernate.initialize(employee.getDivision().getLeaderId());
        Hibernate.initialize(employee.getDivision().getLeader());
        Hibernate.initialize(employee.getJob());
        Hibernate.initialize(employee.getManager());
        Hibernate.initialize(employee.getRegion());

        return employee;
    }

    public List<Employee> getActiveEmployeesNotInList(List<Integer> syncedEmployees) {
        return entityManager.createQuery(
                "FROM Employee AS emp " +
                        "WHERE (emp.endDate IS NOT NULL " +
                        "AND emp.endDate >= :curDate) " +
                        "OR (emp.endDate IS NULL) AND emp.id NOT IN :ids"
        ).setParameter("curDate", new Date()).setParameter("ids", syncedEmployees).getResultList();
    }

    public List<Employee> getEmployees() {
        final Query query = entityManager.createQuery("from Employee e where e.endDate is null order by e.name");

        return query.getResultList();
    }

    /* возвращает список  */
    public List<Employee> getAllEmployees() {
        final Query query = entityManager.createQuery("from Employee e order by e.name");

        return query.getResultList();
    }

    public List<Employee> getAllEmployees(Division division) {
        Query query = entityManager.createQuery("select e from Employee e where e.division = :division")
                .setParameter("division", division);

        return query.getResultList();
    }

    public Map<Division, List<Employee>> getAllEmployees(List<Division> divisions) {
        Map<Division, List<Employee>> resultMap = new HashMap<Division, List<Employee>>();

        for (Division division : divisions) {
            resultMap.put(division, getAllEmployees(division));
        }

        return resultMap;
    }

    /**
     * Возвращает всех подчинённых сотрудников, являющихся руководителями.
     * @param employee Сотрудник, для которого необходимо получить всех подчинённых сотрудников-руководителей.
     * @return Подчиненные сотрудники-руководители.
     */
    public List<Employee> getAllSubordinateManagers(Employee employee) {
        final List<Employee> allManagers = entityManager.createQuery("select distinct e.manager from Employee e where e.manager.endDate is null").getResultList();
        final Map<Employee, HashSet<Employee>> allManagersMap = new HashMap<Employee, HashSet<Employee>>();

        // Построение карты вида Старший менеджер -> Младшие менеджеры
        for (Employee subordinate : allManagers) {
            if (!allManagersMap.containsKey(subordinate.getManager())) {
                allManagersMap.put(subordinate.getManager(), new HashSet<Employee>());
            }

            allManagersMap.get(subordinate.getManager()).add(subordinate);
        }

        final HashSet<Employee> subordinates = new HashSet<Employee>();

        getEmployeeSubordinateManagers(employee, subordinates, allManagersMap);

        return new ArrayList<Employee>(subordinates);
    }

    /**
     * Возвращает всех подчинённых сотрудников, являющихся руководителями.
     * @param employees Сотрудники, для которых необходимо получить всех подчинённых сотрудников-руководителей.
     * @return Подчиненные сотрудники-руководители.
     */
    public List<Employee> getAllSubordinateManagers(List<Employee> employees) {
        final List<Employee> allManagers = entityManager.createQuery("select distinct e.manager from Employee e where e.manager.endDate is null").getResultList();
        final Map<Employee, HashSet<Employee>> allManagersMap = new HashMap<Employee, HashSet<Employee>>();

        // Построение карты вида Старший менеджер -> Младшие менеджеры
        for (Employee subordinate : allManagers) {
            if (!allManagersMap.containsKey(subordinate.getManager())) {
                allManagersMap.put(subordinate.getManager(), new HashSet<Employee>());
            }

            allManagersMap.get(subordinate.getManager()).add(subordinate);
        }

        final HashSet<Employee> subordinates = new HashSet<Employee>();

        for (Employee employee : employees) {
            getEmployeeSubordinateManagers(employee, subordinates, allManagersMap);
        }

        return new ArrayList<Employee>(subordinates);
    }

    /**
     * Возвращает всех подчинённых, являющихся сотрудниками-руководителями.
     * @param employee Сотрудник, для которого возвращаются подчинённые сотрудники-руководители.
     * @param subordinates Заполняемый перечень сотрудников.
     * @param allManagersMap Карта вида "Старший менеджер -> Младшие менеджеры".
     * @return Подчинённые сотрудники-руководители.
     */
    public void getEmployeeSubordinateManagers(Employee employee, final HashSet<Employee> subordinates, Map<Employee, HashSet<Employee>> allManagersMap) {
        if (allManagersMap.containsKey(employee)) {
            subordinates.addAll(allManagersMap.get(employee));
            for (Employee subordinateEmployee : allManagersMap.get(employee)) {
                getEmployeeSubordinateManagers(subordinateEmployee, subordinates, allManagersMap);
            }
        }
    }

    public Employee tryGetEmployeeFromBusinessTrip(Integer reportId) {
        try {
            return (Employee) entityManager.createQuery("select bt.employee from BusinessTrip as bt " +
                    "where bt.id = :id")
                    .setParameter("id", reportId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Employee tryGetEmployeeFromIllness(Integer reportId) {
        try {
            return (Employee) entityManager.createQuery("select  i.employee from Illness as i " +
                    "where i.id = :id")
                    .setParameter("id", reportId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Boolean isLineManager(Employee employee) {
        Long slavesCount = (Long) entityManager.createQuery("select count (*) from Employee as e " +
                "where e.manager = :employee")
                .setParameter("employee", employee)
                .getSingleResult();
        return slavesCount > 0;
    }

    /**
     * Возвращает всех главных руководителей проектов.
     * @return Список главных руководителей
     */
    public List<Employee> getMainProjectManagers() {
        Query query = entityManager.createQuery("select distinct e from Employee e " +
                "where e in (select p.manager from Project p where p.manager is not null) order by e.name");

        return query.getResultList();
    }

    /**
     * Возвращает всех главных руководителей для проектов, принадлежащих указанному центру.
     * @param division Центр
     * @return Список главных руководителей
     */
    public List<Employee> getMainProjectManagers(Division division) {
        if (division == null) {
            Query query = entityManager.createQuery("select distinct e from Employee e " +
                    "where e in (select p.manager from Project p where p.division is null " +
                    "and p.manager is not null) order by e.name");

            return query.getResultList();
        } else {
            Query query = entityManager.createQuery("select distinct e from Employee e " +
                    "where e in (select p.manager from Project p where p.division in (:division) " +
                    "and p.manager is not null) order by e.name")
                    .setParameter("division", division);

            return query.getResultList();
        }
    }

    /**
     * Получаем младших менеджеров проекта (тимлиды, ведущие аналитики)
     */
    public List<Employee> getProjectManagers(Project project) {
        Query query = entityManager.createQuery("select pm.employee from ProjectManager as pm " +
                "where pm.project = :project and pm.active=:active")
                .setParameter("project", project)
                .setParameter("active", true);

        return query.getResultList();
    }

    /**
     * Получаем младших менеджеров проекта (тимлиды, ведущие аналитики) той же специальности (роли)
     */
    public List<Employee> getProjectManagersSameRole(Project project, Employee employee) {
        Query query = entityManager.createQuery("select pm.employee from ProjectManager as pm " +
                "where pm.project = :project and pm.active=:active and " +
                "pm.projectRole.id = :roleId")
                .setParameter("project", project)
                .setParameter("active", true)
                .setParameter("roleId", employee.getJob().getId());

        return query.getResultList();
    }

    /**
     * Получаем младших менеджеров проекта (тимлиды, ведущие аналитики), которые еще не ответили на письмо о согласовании отпуска
     */
    public List<Employee> getProjectManagersThatDoesntApproveVacation(Project project, Vacation vacation) {
        Query query = entityManager.createQuery("select pm.employee from ProjectManager as pm " +
                "where pm.project = :project and pm.active=:active and " +
                "pm.projectRole.id = :roleId and pm.employee not in " +
                "(select va.manager from VacationApproval as va where va.vacation = :vacation and va.result is not null)")
                .setParameter("project", project)
                .setParameter("active", true)
                .setParameter("roleId", vacation.getEmployee().getJob().getId())
                .setParameter("vacation", vacation);

        return query.getResultList();
    }

    public List<Employee> getManagerListForAllEmployee(){
        Query query= entityManager.createQuery("select distinct emp.manager as manager from Employee as emp where emp.endDate is null order by 1");
        return query.getResultList();
    }

    /**
     * множенственный выбор по подразделениям, руководителям подразделений, проектам и регионам
     * если параметр передан как null - то поиск по всем
     */
    public List<Employee> getEmployees(List<Division> divisions, List<Employee> managers, List<Region> regions,
                                       List<Project> projects, Date beginDate, Date endDate,
                                       boolean lookPreviousTwoWeekTimesheet){

        Integer beginDateMonth = 1;
        Integer beginDateYear = 1900;
        Integer endDateMonth = 1;
        Integer endDateYear = 2100;
        Date twoWeekEarlyDate = DateUtils.addDays(beginDate, -TimeSheetConstants.LOOKUP_DAYS); // получаем дату на 2 недели назад
        if (lookPreviousTwoWeekTimesheet){
            if (beginDate != null){
                beginDateMonth = DateTimeUtil.getMonth(beginDate) + 1; // в БД нумерация с 1
                beginDateYear = DateTimeUtil.getYear(beginDate);
            }
            if (endDate != null){
                endDateMonth = DateTimeUtil.getMonth(endDate) + 1; // в БД нумерация с 1
                endDateYear = DateTimeUtil.getYear(endDate);
            }
        }

        StringBuilder queryString = new StringBuilder("FROM Employee e ");
        queryString.append(" WHERE e.endDate is null ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        boolean hasCondition = false;

        if (divisions != null){ // если не все подразделения, а несколько
            queryString.append("AND (e.division IN (:divisions))");
            parameters.put("divisions", divisions);
            hasCondition = true;
        }
        if (managers != null){
            if (hasCondition) queryString.append(" AND "); hasCondition = true;
            queryString.append("(e.manager IN (:managers) OR e.manager2 IN (:managers))");
            managers.addAll(getAllSubordinateManagers(managers));
            parameters.put("managers", managers);
        }
        if (regions != null){
            if (hasCondition) queryString.append(" AND "); hasCondition = true;
            queryString.append("(e.region IN (:regions))");
            parameters.put("regions", regions);
        }
        if (projects != null){
            if (hasCondition) queryString.append(" AND ");
            queryString.append("((e.id IN (SELECT epp.employee FROM EmployeeProjectPlan epp WHERE " +
                    "(epp.project IN (:projects)) AND " +
                    "(epp.month <= :endDateMonth AND epp.month >= :beginDateMonth AND" +
                    " epp.year <= :endDateYear AND epp.year >= :beginDateYear)))");
            if (lookPreviousTwoWeekTimesheet){
                queryString.append(" OR (e.id IN (SELECT ts.employee FROM TimeSheet ts WHERE ts.id IN " +
                        "(SELECT tsd.timeSheet FROM TimeSheetDetail tsd WHERE tsd.project IN (:projects)) AND " +
                        "ts.calDate.calDate between :twoWeekEarlyDate AND :beginDate AND ts.type = "+TypesOfTimeSheetEnum.REPORT.getId()+"))");
                parameters.put("twoWeekEarlyDate", twoWeekEarlyDate);
                parameters.put("beginDate", beginDate);
            }
            queryString.append(")");
            parameters.put("projects", projects);
            parameters.put("endDateMonth", endDateMonth);
            parameters.put("endDateYear", endDateYear);
            parameters.put("beginDateMonth", beginDateMonth);
            parameters.put("beginDateYear", beginDateYear);
        }
        Query query = entityManager.createQuery(queryString.toString());
        for (Map.Entry entry : parameters.entrySet()){
            query.setParameter(entry.getKey().toString(), entry.getValue());
        }
        return query.getResultList();
    }

    // Если manager_Id == null или <1, то ищет без учета менеджера
    public List<Employee> getDivisionEmployeesByManager(
            Integer divisionId, Date date, List<Integer> regionIds, List<Integer> projectRoleIds, Integer managerId) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("div_id", divisionId);
        parameters.put("date", date);

        StringBuilder queryString = new StringBuilder(
                "from Employee e where e.division.id = :div_id" +
                " and (:date >= e.startDate and (e.endDate is null or :date <= e.endDate))"
        );

        if (regionIds != null && !regionIds.contains(ALL_REGIONS)){
            queryString.append(" and (e.region.id in (:region_ids))");
            parameters.put("region_ids", regionIds);
        }
        if (projectRoleIds != null && !projectRoleIds.contains(ALL_PROJECT_ROLES)){
            queryString.append(" and (e.job.id in (:project_role_ids))");
            parameters.put("project_role_ids", projectRoleIds);
        }
        if (managerId != null && managerId >= 1){
            queryString.append(" and (e.manager.id = :manager_Id or e.manager2.id = :manager_Id)");
            parameters.put("manager_Id",managerId);
        }
        queryString.append(" order by e.name");
        final Query query = entityManager.createQuery(queryString.toString());
        for (Map.Entry entry : parameters.entrySet()){
            query.setParameter(entry.getKey().toString(), entry.getValue());
        }

        return query.getResultList();
    }

    public Employee findByLdapCN(String ldapCN) {
        return (Employee) Iterables.getFirst(entityManager.createQuery(
                "FROM Employee emp WHERE ldap = :ldap"
        ).setParameter("ldap", ldapCN).getResultList(), null);
    }

    public Boolean isEmployeeDivisionLeader(Integer employeeID) {
        Long slavesCount = (Long) entityManager.createQuery("select count (*) from Division as e " +
                "where e.leaderId.id = :employeeID")
                .setParameter("employeeID", employeeID)
                .getSingleResult();
        return slavesCount > 0;
    }

    /**
     * Возвращает сотрудников по списку регионов, руководителю (включая всех нижестоящих руководителей) и центру.
     * @param regions Список регионов
     * @param divisionId Идентификатор центра
     * @param manager Идентификатор руководителя
     * @return Список сотрудников
     */
    public List<Employee> getEmployeeByRegionAndManagerRecursiveAndDivision(List<Integer> regions, Integer divisionId, Integer manager) {
        String qlString = "select emp from Employee as emp where emp.endDate is null";
        if (manager != null && manager >= 0 ) {
            qlString += " and emp.manager in (:managers) ";
        }
        if (regions != null && regions.size() > 0 && !regions.get(0).equals(-1)) {
            qlString += " and emp.region.id in :regionId  ";
        }
        if (divisionId != null && divisionId != 0 ) {
            qlString += " and emp.division.id = :divisionId ";
        }
        Query query = entityManager.createQuery(qlString);
        if ( manager != null && manager >= 0) {
            Employee managerEmployee = find(manager);
            ArrayList<Employee> managers = new ArrayList<Employee>(getAllSubordinateManagers(managerEmployee));
            managers.add(managerEmployee);
            query.setParameter("managers", managers);
        }
        if (regions != null && regions.size() > 0 && !regions.get(0).equals(-1)) {
            query.setParameter("regionId", regions);
        }
        if ( divisionId != null && divisionId != 0 ) {
            query.setParameter("divisionId", divisionId);

        }
        return query.getResultList();
    }

    public Employee findByLdapSID(String ldapSid) {
        return (Employee) Iterables.getFirst(entityManager.createQuery(
                "FROM Employee emp WHERE objectSid = :ldapSid"
        ).setParameter("ldapSid", ldapSid).getResultList(), null);
    }

    /**
     * Возвращает id регионов где имеются сотрудники у данного менеджера
     * @param id
     * @return
     */
    public List<Integer> getRegionsWhereManager(Integer id) {
        Query query = entityManager.createQuery("select emp.region.id from Employee emp where emp.endDate=null and emp.manager.id = :id group by emp.region").setParameter("id", id);
        return query.getResultList();
    }

    public List<Employee> getEmployeesForDivisionWithBirthdayMonth(Division division, Integer birthdayMonth){
        Query query = entityManager.createQuery(
                "from Employee where division = :division AND month(birthday) = :birthdayMonth AND endDate is null");
        query.setParameter("division", division);
        query.setParameter("birthdayMonth", birthdayMonth);
        return query.getResultList();
    }

    public Employee tryFindByFioRegionDivision(String name, String patronymic, Region region, Division division) {
        Query query = entityManager.createQuery(
                "select " +
                        "emp " +
                        "from " +
                        "Employee emp " +
                        "where " +
                        " emp.name = :name and " +
                        " emp.region = :region and " +
                        " emp.division = :division" +
                        ( patronymic != null ? " and emp.patronymic = :patronymic"  : "")
        );
        query.setParameter("name", name);
        query.setParameter("region", region);
        query.setParameter("division", division);
        if (patronymic != null) {
            query.setParameter("patronymic", patronymic);
        }

        return query.getResultList().size() == 1 ? (Employee) query.getResultList().get(0) : null;
    }

    /**
     * Получить идентификаторы проектных ролей сотрудника
     *
     * @param projectId  идентификатор проекта
     * @param employeeId идентификатор сотрудника
     * @return список идентификаторов проектных ролей
     */
    public List<Integer> getEmployeeProjectRoleIds(Integer projectId, Integer employeeId) {
        Query query = entityManager.createNativeQuery("SELECT project_role\n" +
                "FROM project_managers\n" +
                "WHERE project = :projectId AND employee = :employeeId")
                .setParameter("projectId", projectId)
                .setParameter("employeeId", employeeId);

        return query.getResultList();
    }
}