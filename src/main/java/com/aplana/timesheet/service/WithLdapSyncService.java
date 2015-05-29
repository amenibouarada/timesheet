package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import com.aplana.timesheet.enums.RegionsEnum;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author eshangareev
 * @version 1.0
 */
@Service
@Transactional(noRollbackFor = DataAccessException.class)
public class WithLdapSyncService {
    private static final Logger logger = LoggerFactory.getLogger(WithLdapSyncService.class);

    @Autowired
    LdapDAO ldapDAO;

    @Autowired
    TSPropertyProvider propertyProvider;

    @Autowired
    DivisionDAO dao;

    @Autowired
    EmployeeDAO employeeDAO;

    @Autowired
    EmployeeService employeeService;

    @Autowired
    ProjectRoleDAO projectRoleDAO;

    @Autowired
    RegionDAO regionDAO;

    @Autowired
    ProjectRolePermissionsDAO projectRolePermissionsDAO;
    @Autowired
    private ProjectRoleService projectRoleService;

    public void syncWithLdap() {
        List<Integer> syncedEmployees = Lists.newArrayList();
        //запросом в LDAP получать список отделов
        logger.info("=====Starting synchronization user with LDAP…");
        syncDivisionsInLdap(syncedEmployees);
        logger.info("Active division (fromLDAP) users synched employees – {}", syncedEmployees.size());
        syncDivisionsNotInLdap(syncedEmployees);
        logger.info("Total active division users synched employees – {}", syncedEmployees.size());
        logger.info("Starting synchronization other users");
        syncOtherEmployees(employeeDAO.getActiveEmployeesNotInList(syncedEmployees), syncedEmployees);
        logger.info("Synchronization other users finished");
        logger.info("Total synched employees – {}", syncedEmployees.size());
    }

    private void syncDivisionsNotInLdap(List<Integer> syncedEmployees) {
        logger.info("Starting synchronization other division …");
        Iterable<Division> divisions = Iterables.filter(dao.getAllDivisions(), new Predicate<Division>() {
            @Override public boolean apply(@Nullable Division input) {
                return input.getNotToSyncWithLdap() && input.getSyncEmployee();
            } });
        logger.info("Count division for sync — {}", Iterables.size(divisions));
        for (Division division : divisions) {
            List<EmployeeLdap> employees = ldapDAO.getEmployeesByDepartmentNameFromDb(division.getDepartmentName());
            syncEmployees(employees, syncedEmployees);
        }
    }

    private void syncDivisionsInLdap(List<Integer> syncedEmployees) {
        logger.info("Starting synchronization division from LDAP…");
        logger.info("Getting all division from LDAP…");
        List<Map> divisions = ldapDAO.getDivisions();
        logger.info("…in LDAP {} divisions.", divisions.size());
        logger.info("Gettinng all division from DB…");
        List<Division> divisionsFromDB = dao.getAllDivisions();
        logger.info("…in DB {} divisions.", divisionsFromDB.size());

        for (Map division : divisions) {
            logger.info("Searching for division \"{}\" from LDAP according one in DB…", division.get(propertyProvider.getLdapFieldForDivisionName()));
            Division dbDivision = findDbDivision(divisionsFromDB, LdapUtils.convertBinarySidToString((byte[]) division.get(propertyProvider.getLdapFieldForSID())));
            logger.info("…finded division has {} id.", dbDivision == null ? null : dbDivision.getId());
            //Затем для каждого проверять существует ли данный отдел в системе
            if (dbDivision == null) {
                logger.info("According division not founded in DB.");
                logger.info("Creating new division in DB started...");
                //Если не удалось найти отдел - добавить новый отдел
                dbDivision = createNewDivision(division);
                dao.save(dbDivision);

                setLeaderIfNeed(division, dbDivision);
                logger.info("…created division saved.");
            //Если отдел уже есть и active = false - перейти к анализу следующего отдела
            } else if (!dbDivision.isActive() || dbDivision.getNotToSyncWithLdap()) {
                logger.info(
                        "Division \"{}\" is {}. Go to next division.",
                        dbDivision.getLdapName(),
                        dbDivision.getNotToSyncWithLdap() ? "marked as not to sync" : "not active"
                );
                continue;
            //Если отдел уже есть и active = true - проверить/обновить поля ldap_name, leader
            } else {
                setLeaderIfNeed(division, dbDivision);
                Employee employeeByObjectSid = employeeDAO.findByObjectSid((String) division.get(propertyProvider.getLdapFieldForLeader()));
                if (employeeByObjectSid != null && !employeeByObjectSid.getName().equals(dbDivision.getLeader())) {
                    logger.info("Divisions in LDAP and DB is not same.");
                    logger.info("Updating started...");
                    dbDivision.setLeader(employeeByObjectSid.getName());
                    dbDivision.setLeaderId(employeeByObjectSid);
                    logger.info("Setting new leader (name={})", employeeByObjectSid.getName());
                    dao.save(dbDivision);
                    logger.info("..udpating finished");
                } else {
                    logger.info("Divisions in LDAP and DB is same.");
                }
            }
            logger.info("Starting synchronization active division users");
            syncActiveEmployees(division, syncedEmployees, dbDivision.getDepartmentName());
            logger.info("Synchronization active division users finished");
        }
        logger.info("Synchronization division from LDAP was");
    }

    private void setLeaderIfNeed(Map division, Division dbDivision) {
        if (dbDivision.getLeaderId() == null) {
            String employeeLdap = (String) division.get(propertyProvider.getLdapFieldForLeader());
            Employee leader = employeeDAO.findByLdapName(employeeLdap);
            if (leader == null) {
                leader = employeeService.save(createUser(ldapDAO.getEmployeeByLdapName(employeeLdap), true));
            }

            dbDivision.setLeaderId(leader);
            dbDivision.setLeader(leader.getName());
            dao.save(dbDivision);
        }
    }

    private void syncOtherEmployees(List<Employee> activeEmployeesNotInList, List<Integer> syncedEmployees) {
        for (Employee employee : activeEmployeesNotInList) {
            employee.setEndDate(new Timestamp(DateUtils.addDays(new Date(), 1).getTime()));
            employeeService.save(employee);
            syncedEmployees.add(employee.getId());
        }
    }

    private void syncActiveEmployees(Map division, List<Integer> syncedEmployees, String departmentName) {

        List<EmployeeLdap> totalEmployees = ldapDAO.getEmployeesByDepartmentNameFromDb(departmentName);
        logger.info("Total employees is {}", totalEmployees.size());
        Iterable<EmployeeLdap> employyes = Iterables.filter(
                totalEmployees,
                new Predicate<EmployeeLdap>() {
                    @Override
                    public boolean apply(@Nullable EmployeeLdap input) {
                        return !input.getLdapCn().contains("OU=Disabled Users");
                    }
                });
        logger.info("Active employees is {}", Iterables.size(employyes));
        //Проверить что лидеры отделов принадлежат отделу
        logger.info("Start checking of leader...");
        if( checkThatLeaderInMembers(division, employyes) ){
            // то остановить синхронизацию и написать письмо с логом синхронизации
            //TODO
            logger.error("...leader is not same division that he leads.");
            return;
        } else {
            logger.info("...leader is in same division that he leads.");
        }

        syncEmployees(employyes, syncedEmployees);
    }

    private void syncEmployees(Iterable<EmployeeLdap> employyes, List<Integer> syncedEmployees) {
        for (EmployeeLdap employee : employyes) {
            logger.info("Sync employee from LDAP with DB (ldapName = {})", employee.getDisplayName());
            Employee employeeFromDb   = employeeDAO.findByObjectSid(employee.getObjectSid());

            if(employeeFromDb != null){
                logger.info("According employee was founded");
                if(employeeFromDb.isNotToSync()){
                    logger.info("Employee markes as not to sync. Go to next employee.");
                    continue;
                }

                Employee employeeFromLdap = createUser(employee);

                if (!compareEmployees(employeeFromDb, employeeFromLdap)) {
                    logger.info("Employee in LDAP was changed. Update according employee in DB.");
                    mergeEmployees(employeeFromDb, employeeFromLdap);
                    employeeService.save(employeeFromDb);
                } else {
                    logger.info("Employees are same. Go to next employee.");
                }
            } else if (StringUtils.isBlank(employee.getEmail())) {
                logger.info("Employee {} is deactivated(empty email)", employee.getDisplayName());
                continue;
            } else {

                logger.info("According employee was not founded");
                logger.info("Creating new Employee in DB...");
                employeeFromDb = employeeService.save(createUser(employee));
                logger.info("…creating employee completed.");
            }
            syncedEmployees.add(employeeFromDb.getId());
        }
    }

    private void mergeEmployees(Employee employeeFromDb, Employee employeeFromLdap) {
        employeeFromDb.setLdap      (employeeFromLdap.getLdap());
        employeeFromDb.setName      (employeeFromLdap.getName());
        employeeFromDb.setRegion    (employeeFromLdap.getRegion());
        employeeFromDb.setDivision  (employeeFromLdap.getDivision());
        employeeFromDb.setJob       (employeeFromLdap.getJob());

        // У руководителей отделений не должны быть прописаны руководители
        Division division = dao.find(employeeFromDb.getDivision().getId());

        if(division.getLeaderId() != null && division.getLeaderId().equals(employeeFromDb)){
            employeeFromDb.setManager(null);
        } else {
            employeeFromDb.setManager(employeeFromLdap.getManager());
        }
        if (StringUtils.isBlank(employeeFromLdap.getEmail())) {
            logger.info("Employee {} is deactivated(empty email)", employeeFromDb.getName());
            employeeFromDb.setEndDate(employeeFromLdap.getEndDate());
        } else {
            employeeFromDb.setEmail(employeeFromLdap.getEmail());
        }
    }

    private boolean compareEmployees(Employee employeeFromDb, Employee employeeFromLdap) {
        return     Objects.equal(employeeFromDb.getLdap()            ,  employeeFromLdap.getLdap())
                && Objects.equal(employeeFromDb.getName()            ,  employeeFromLdap.getName())
                && Objects.equal(getId(employeeFromDb.getDivision()) ,  getId(employeeFromLdap.getDivision()))
                && Objects.equal(employeeFromDb.getEmail()           ,  employeeFromLdap.getEmail())
                && Objects.equal(getId(employeeFromDb.getJob())      ,  getId(employeeFromLdap.getJob()))
                && Objects.equal(getId(employeeFromDb.getManager())  ,  getId(employeeFromLdap.getManager()))
                && Objects.equal(getId(employeeFromDb.getRegion())   ,  getId(employeeFromLdap.getRegion()));
    }

    private Integer getId(Identifiable identifiable) {
        return identifiable == null ? null : identifiable.getId();
    }

    private boolean checkThatLeaderInMembers(Map division, Iterable<EmployeeLdap> employyes) {
        final EmployeeLdap employeeByLdapName = ldapDAO.getEmployeeByLdapName((String) division.get(propertyProvider.getLdapFieldForLeader()));

        Optional<EmployeeLdap> employeeLdapOptional = Iterables.tryFind(employyes, new Predicate<EmployeeLdap>() {
            @Override
            public boolean apply(EmployeeLdap input) {
                return input.getObjectSid().equals(employeeByLdapName.getObjectSid());
            }
        });

        return ! employeeLdapOptional.isPresent();
    }

    @VisibleForTesting
    Division findDbDivision(List<Division> divisions, final String objectSid) {
        logger.debug("Try to find division in DB by objectSid = {}", objectSid);
        return Iterables.tryFind(divisions, new Predicate<Division>() {
            @Override
            public boolean apply(Division input) {
                return objectSid.equals(input.getObjectSid());
            }
        }).orNull();
    }

    @VisibleForTesting
    Employee createUser(final EmployeeLdap employeeFromLdap) {
        return createUser(employeeFromLdap, false);
    }

    @VisibleForTesting
    Employee createUser(final EmployeeLdap employeeFromLdap, boolean leader) {
        Employee employee = new Employee();

        logger.debug("Starting creating new employee(Email={})", employeeFromLdap.getEmail());
        employee.setLdap(employeeFromLdap.getLdapCn());
        employee.setObjectSid(employeeFromLdap.getObjectSid());
        employee.setEmail(employeeFromLdap.getEmail());

        employee.setDivision(dao.findByDepartmentName(employeeFromLdap.getDepartment()));
        employee.setNotToSync(false);
        employee.setName(employeeFromLdap.getDisplayName());

        employee.setStartDate(new Timestamp(DateUtils.addDays(new Date(), 1).getTime()));

        Region region = Iterables.tryFind(regionDAO.getRegions(), new Predicate<Region>() {
            @Override
            public boolean apply(@Nullable Region input) {
                return employeeFromLdap.getCity().toLowerCase().contains(input.getLdapCity());
            }
        }).or(regionDAO.find(RegionsEnum.OTHERS.getId()));

        employee.setRegion(region);

        employee.setJob(
                Optional.fromNullable(projectRoleDAO.find(employeeFromLdap.getTitle()))
                        .or(projectRoleService.getUndefinedRole()));

        employee.setPermissions(Sets.newHashSet(projectRolePermissionsDAO.getProjectRolePermission(employee.getJob())));

        if (StringUtils.isNotBlank(employeeFromLdap.getManager())) {
            logger.debug("Employee' division – {}, division leader {}", employee.getDivision().getLdapName(), employee.getDivision().getLeader());
            if(leader || employee.getDivision().getLeader().equals(employeeFromLdap.getDisplayName())){
                employee.setManager(null);
            } else {
                employee.setManager(employeeDAO.findByLdapName(employeeFromLdap.getManager()));
                if (employee.getManager() == null) {
                    employee.setManager(
                            employeeService.save(createUser(ldapDAO.getEmployeeByLdapName(employeeFromLdap.getManager()))));
                }
            }
        }

        return employee;
    }

    @VisibleForTesting
    Division createNewDivision(Map division) {
        Division dbDivision = new Division();

        dbDivision.setObjectSid(LdapUtils.convertBinarySidToString((byte[]) division.get(propertyProvider.getLdapFieldForSID())));
        logger.info("In field objectSid set \"()\" value.", dbDivision.getObjectSid());
        dbDivision.setActive(true);
        dbDivision.setNotToSyncWithLdap(false);
        dbDivision.setSyncEmployee(true);
        dbDivision.setLdapName((String) division.get(propertyProvider.getLdapFieldForDivisionName()));
        logger.info("In field ldapName set \"()\" value.", dbDivision.getLdapName());


        dbDivision.setName(dbDivision.getLdapName());
        dbDivision.setDepartmentName(dbDivision.getLdapName());
        dbDivision.setLeaderId(employeeDAO.findByLdapName((String) division.get(propertyProvider.getLdapFieldForLeader())));
        dbDivision.setLeader(dbDivision.getLeaderId() == null ? null : dbDivision.getLeaderId().getName());
        logger.info("In field leader set \"()\" value.", dbDivision.getLeader());

        return dbDivision;
    }
}
