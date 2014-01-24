package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeePercentPlan;
import com.aplana.timesheet.dao.entity.EmployeeProjectPlan;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.enums.*;
import com.aplana.timesheet.form.EmploymentPlanningForm;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Repository
public class EmployeeProjectPlanDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<EmployeeProjectPlan> find(Employee employee, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "from EmployeeProjectPlan epp where epp.employee = :employee and epp.year = :year and epp.month = :month"
        ).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);

        return query.getResultList();
    }

    public void store(EmployeeProjectPlan employeeProjectPlan) {
        final EmployeeProjectPlan merged = entityManager.merge(employeeProjectPlan);

        employeeProjectPlan.setId(merged.getId());
    }

    public EmployeeProjectPlan find(Employee employee, Integer year, Integer month, Project project) {
        final Query query = entityManager.createQuery(
                "from EmployeeProjectPlan epp where epp.employee = :employee and epp.year = :year and epp.month = :month and epp.project = :project"
        ).setParameter("employee", employee).setParameter("year", year).
                setParameter("month", month).setParameter("project", project);

        return (EmployeeProjectPlan) query.getSingleResult();
    }

    public void remove(Employee employee, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "delete from EmployeeProjectPlan epp where epp.employee = :employee and epp.year = :year and epp.month = :month"
        ).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);

        query.executeUpdate();
    }

    public EmployeeProjectPlan tryFind(Employee employee, Integer year, Integer month, Project project) {
        try {
            return find(employee, year, month, project);
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void remove(EmployeeProjectPlan employeeProjectPlan) {
        if (employeeProjectPlan.getId() != null)
          entityManager.remove(employeeProjectPlan);
    }

    /**
     * Возвращает суммарную процентную занятость сотрудников по проекту.
     * @return List<Object[3]>:
     *         Object[0] - employee_id,
     *         Object[1] - employee_name,
     *         Object[2] - year,
     *         Object[3] - month
     *         Object[4] - percent(сумма, запланированных часов по всем проектам, делить на количество рабочих часов в месяце)
     *
     * NativeNamedQuery только с позиционными связными переменными неудобно использовать.
     */
    public List<Object[]> getProjectPlan(EmploymentPlanningForm employmentPlanningForm){
        Query query = entityManager.createNativeQuery(
            "with emp as " +
            "( " +
                "select " +
                    "epp.employee_id, e.region " +
                "from " +
                    "employee_project_plan epp, employee e " +
                "where " +
                    "epp.project_id = :projectId and epp.employee_id = e.id " +
                    "and epp.value > 0 " +
                    "and ( " +
                    "   (epp.year = :yearStart and epp.year = :yearEnd   and epp.month between :monthStart and :monthEnd) " +
                    "or (epp.year = :yearStart and epp.year < :yearEnd   and epp.month > :monthStart) " +
                    "or (epp.year = :yearEnd   and epp.year > :yearStart and epp.month < :monthEnd) " +
                    "or (epp.year > :yearStart and epp.year < :yearEnd) " +
                    ") " +
            "), " +
            "no_project_data(employee_id, month, year, value) as " +
            "( " +
                "select " +
                    "ep.employee_id, ep.month, ep.year, ep.value " +
                "from " +
                    "dictionary_item d " +
                    "left join employee_plan ep on " +
                    "(" +
                        "d.id = ep.item_id " +
                        "and ( " +
                        "   (ep.year = :yearStart and ep.year = :yearEnd   and ep.month between :monthStart and :monthEnd) " +
                        "or (ep.year = :yearStart and ep.year < :yearEnd   and ep.month > :monthStart) " +
                        "or (ep.year = :yearEnd   and ep.year > :yearStart and ep.month < :monthEnd) " +
                        "or (ep.year > :yearStart and ep.year < :yearEnd) " +
                        ") " +
                    ") " +
                "where " +
                    " d.id = "+EmployeePlanType.NON_PROJECT.getId() + " " +
            ") " +
            "select " +
                    //TODO в дне всегда 8 рабочих часов?
                " e.id, e.name, fct.year, fct.month, 100*fct.val/(8*wrk.val) " +
            "from " +
                "( " +
                    "select " +
                        "epp.employee_id, epp.month, epp.year, sum(epp.value) val " +
                    "from " +
                        "(" +
                            "select epp.employee_id, epp.month, epp.year, epp.value from employee_project_plan epp " +
                            "union all " +
                            "select ep.employee_id, ep.month, ep.year, ep.value from no_project_data ep " +
                        ") epp " +
                    "where " +
                        "epp.employee_id in (select employee_id from emp) " +
                        "and epp.value > 0 " +
                        "and ( " +
                        "   (epp.year = :yearStart and epp.year = :yearEnd   and epp.month between :monthStart and :monthEnd) " +
                        "or (epp.year = :yearStart and epp.year < :yearEnd   and epp.month > :monthStart) " +
                        "or (epp.year = :yearEnd   and epp.year > :yearStart and epp.month < :monthEnd) " +
                        "or (epp.year > :yearStart and epp.year < :yearEnd) " +
                        ") " +
                    "group by " +
                        "epp.employee_id, epp.month, epp.year " +
                ") fct, " +
                "( " +
                    "select " +
                        "c.year, c.month, r.id region, count(case when h.id is null then 1 end) val " +
                    "from " +
                        "region r " +
                        "cross join calendar c " +
                        "left join holiday h on (c.caldate = h.caldate and (r.id  = h.region or h.region is NULL)) " +
                    "where " +
                        "r.id in (select region from emp) " +
                        "and ( " +
                        "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                        "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                        "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                        "or (c.year > :yearStart and c.year < :yearEnd) " +
                        ") " +
                    "group by " +
                        "r.id, c.month, c.year " +
                ") wrk, " +
                "employee e " +
            "where " +
                "fct.month = wrk.month and fct.year = wrk.year and fct.employee_id = e.id and e.region = wrk.region " +
            "order by e.name ");

        query.setParameter("projectId", employmentPlanningForm.getProjectId());
        query.setParameter("monthStart", employmentPlanningForm.getMonthBeg());
        query.setParameter("yearStart", employmentPlanningForm.getYearBeg());
        query.setParameter("monthEnd", employmentPlanningForm.getMonthEnd());
        query.setParameter("yearEnd", employmentPlanningForm.getYearEnd());

        return query.getResultList();
    }

    /**
     * Обновляет планы по проектам за период. Merge для одного сотрудника
     * @param employeeId
     * @param employmentPlanningForm
     * @param plan
     */
    public void updateEmployeeProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm, Double plan){

        Query query = entityManager.createNativeQuery(
                "with workDay(year, month, cnt) as " +
                "( " +
                    "select " +
                        "c.year, c.month, count(case when h.id is null then 1 end) val " +
                    "from " +
                        "region r " +
                        "cross join calendar c " +
                        "left join holiday h on (c.caldate = h.caldate and (r.id  = h.region or h.region is NULL)) " +
                    "where " +
                        "r.id in (select e.region from employee e where e.id = :employeeId) " +
                        "and ( " +
                        "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                        "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                        "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                        "or (c.year > :yearStart and c.year < :yearEnd) " +
                        ") " +
                    "group by " +
                        "r.id, c.month, c.year " +
                "), " +
                "upsert as " +
                "( " +
                    "update " +
                        "employee_project_plan epp " +
                    "set " +
                        "value = 8*:plan*wrk.cnt/100 " +
                    "from " +
                        "(select w.year, w.month, w.cnt from workDay w) wrk " +
                    "where " +
                        "epp.project_id = :projectId and epp.employee_id = :employeeId " +
                        "and wrk.year = epp.year and wrk.month = epp.month " +
                    "returning epp.year, epp.month " +
                ") " +
                "insert into employee_project_plan" +
                    "(employee_id, project_id, month, year, value) " +
                "select " +
                    ":employeeId, :projectId, wrk.month, wrk.year, 8*:plan*wrk.cnt/100 " +
                "from " +
                    "workDay wrk " +
                "where " +
                    "(year, month) not in (select year, month from upsert)");

        query.setParameter("projectId", employmentPlanningForm.getProjectId());
        query.setParameter("monthStart", employmentPlanningForm.getMonthBeg());
        query.setParameter("yearStart", employmentPlanningForm.getYearBeg());
        query.setParameter("monthEnd", employmentPlanningForm.getMonthEnd());
        query.setParameter("yearEnd", employmentPlanningForm.getYearEnd());

        query.setParameter("employeeId", employeeId);
        query.setParameter("plan", plan);

        query.executeUpdate();
    }

    /**
     * Обновляет планы по не-проектам за период. Merge для одного сотрудника
     * @param employeeId
     * @param employmentPlanningForm
     * @param plan
     */
    public void updateEmployeeNotProjectPlan(Integer employeeId, EmploymentPlanningForm employmentPlanningForm, Double plan){

        Query query = entityManager.createNativeQuery(
                "with workDay(year, month, cnt) as " +
                "( " +
                    "select " +
                        "c.year, c.month, count(case when h.id is null then 1 end) val " +
                    "from " +
                        "region r " +
                        "cross join calendar c " +
                        "left join holiday h on (c.caldate = h.caldate and (r.id  = h.region or h.region is NULL)) " +
                    "where " +
                        "r.id in (select e.region from employee e where e.id = :employeeId) " +
                        "and ( " +
                        "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                        "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                        "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                        "or (c.year > :yearStart and c.year < :yearEnd) " +
                        ") " +
                    "group by " +
                        "r.id, c.month, c.year " +
                "), " +
                "upsert as " +
                "( " +
                    "update " +
                        "employee_plan ep " +
                    "set " +
                        "value = 8*:plan*wrk.cnt/100 " +
                    "from " +
                        "(select w.year, w.month, w.cnt from workDay w) wrk " +
                    "where " +
                        "ep.item_id = :projectId and ep.employee_id = :employeeId " +
                        "and wrk.year = ep.year and wrk.month = ep.month " +
                        "returning ep.year, ep.month " +
                ") " +
                "insert into employee_plan" +
                    "(employee_id, item_id, month, year, value) " +
                "select " +
                    ":employeeId, :projectId, wrk.month, wrk.year, 8*:plan*wrk.cnt/100 " +
                "from " +
                    "workDay wrk " +
                "where " +
                    "(year, month) not in (select year, month from upsert)");

        query.setParameter("projectId", employmentPlanningForm.getProjectId());
        query.setParameter("monthStart", employmentPlanningForm.getMonthBeg());
        query.setParameter("yearStart", employmentPlanningForm.getYearBeg());
        query.setParameter("monthEnd", employmentPlanningForm.getMonthEnd());
        query.setParameter("yearEnd", employmentPlanningForm.getYearEnd());

        query.setParameter("employeeId", employeeId);
        query.setParameter("plan", plan);

        query.executeUpdate();
    }

    /**
     * Возвращает планы работника за период по проектам
     * @param employeeId - идентификатор работника
     * @return List<Object[6]>
     *     Object[0] - project_id
     *     Object[1] - project_name
     *     Object[2] - month
     *     Object[3] - year
     *     Object[4] - percent (сумма, запланированных часов по конкретному проекту, делить на количество рабочих часов в месяце)
     *     Object[5] - 0 - план, 1 - факт
     *
     *     NativeNamedQuery только с позиционными связными переменными неудобно использовать.
     */

    public List<Object[]> getEmployeePlan(Integer employeeId, Integer yearBeg, Integer monthBeg, Integer yearEnd, Integer monthEnd){
        Query query = entityManager.createNativeQuery(
            "with workDay(year, month, region, cnt) as " +
            "( " +
                "select " +
                    "c.year, c.month, r.id region, count(case when h.id is null then 1 end) val " +
                "from " +
                    "region r " +
                    "cross join calendar c " +
                    "left join holiday h on (c.caldate = h.caldate and (r.id  = h.region or h.region is NULL)) " +
                "where " +
                    "r.id in (select e.region from employee e where e.id = :employeeId) " +
                    "and ( " +
                    "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                    "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                    "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                    "or (c.year > :yearStart and c.year < :yearEnd) " +
            ") " +
            "group by " +
                "r.id, c.month, c.year " +
            "), " +
            "no_project_data(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                    "-d.id id, d.value nm, ep.month, ep.year, ep.value val " +
                "from " +
                    "dictionary_item d " +
                    "left join employee_plan ep on " +
                    "(" +
                        "d.id = ep.item_id and ep.employee_id = :employeeId " +
                        "and ( " +
                        "   (ep.year = :yearStart and ep.year = :yearEnd   and ep.month between :monthStart and :monthEnd) " +
                        "or (ep.year = :yearStart and ep.year < :yearEnd   and ep.month > :monthStart) " +
                        "or (ep.year = :yearEnd   and ep.year > :yearStart and ep.month < :monthEnd) " +
                        "or (ep.year > :yearStart and ep.year < :yearEnd) " +
                        ") " +
                    ") " +
                "where " +
                    " d.id = " + EmployeePlanType.NON_PROJECT.getId() + " " +
            "), " +
            "illness_data(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                "    -"+EmployeePlanType.ILLNESS.getId()+", cast('"+EmployeePlanType.ILLNESS.getName()+"' as text), c.month, c.year, count(1) val " +
                "from " +
                "    illness ill " +
                "    inner join calendar c on (c.caldate between ill.begin_date and ill.end_date) " +
                "where " +
                    "ill.employee_id = :employeeId " +
                    "and ( " +
                    "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                    "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                    "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                    "or (c.year > :yearStart and c.year < :yearEnd) " +
                    ") " +
                "group by " +
                "    ill.employee_id, c.month, c.year " +
            "), " +
            "vacation_plan_data(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                "    -"+EmployeePlanType.VACATION.getId()+", cast('"+EmployeePlanType.VACATION.getName()+"' as text), c.month, c.year, count(distinct c.caldate) val " +
                "from " +
                "    vacation vac " +
                "    inner join calendar c on (c.caldate between vac.begin_date and vac.end_date) " +
                "where " +
                    "vac.type_id in (" + VacationTypesEnum.WITH_NEXT_WORKING.getId() + ", " + VacationTypesEnum.PLANNED.getId() + ", "+ VacationTypesEnum.WITH_PAY.getId()+ ", " + VacationTypesEnum.WITHOUT_PAY.getId() +") " +
                    "and vac.status_id in ("+ VacationStatusEnum.APPROVED.getId() + ", " + VacationStatusEnum.APPROVEMENT_WITH_LM.getId() + ", "+ VacationStatusEnum.APPROVEMENT_WITH_PM.getId()+", "+ VacationStatusEnum.APPROVED_BY_PM.getId()+")" +
                    "and vac.employee_id = :employeeId " +
                    "and ( " +
                    "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                    "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                    "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                    "or (c.year > :yearStart and c.year < :yearEnd) " +
                    ") " +
                "group by " +
                "    vac.employee_id, c.month, c.year " +
            "), " +
            "project_data(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                    "epp.project_id, p.name, epp.month, epp.year, epp.value " +
                "from " +
                    "employee_project_plan epp " +
                    "inner join project p on (epp.project_id = p.id) " +
                "where " +
                    "epp.employee_id = :employeeId " +
                    "and epp.value > 0 " +
                    "and ( " +
                    "   (epp.year = :yearStart and epp.year = :yearEnd   and epp.month between :monthStart and :monthEnd) " +
                    "or (epp.year = :yearStart and epp.year < :yearEnd   and epp.month > :monthStart) " +
                    "or (epp.year = :yearEnd   and epp.year > :yearStart and epp.month < :monthEnd) " +
                    "or (epp.year > :yearStart and epp.year < :yearEnd) " +
                    ") " +
            "), " +
            "plan_data(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                    "project_id, name, month, year, val " +
                "from " +
                "( " +
                    "select " +
                    "   npd.project_id, npd.name, npd.month, npd.year, (100*npd.val)/(8*wd.cnt) val " +
                    "from " +
                    "   no_project_data npd " +
                    "   left join employee e on (e.id = :employeeId) " +
                    "   inner join workDay wd on (wd.region = e.region and npd.month = wd.month and npd.year = wd.year) " +
                    "union all " +
                    "select " +
                    "   pd.project_id, pd.name, pd.month, pd.year, (100*pd.val)/(8*wd.cnt) val " +
                    "from " +
                    "   project_data pd " +
                    "   left join employee e on (e.id = :employeeId) " +
                    "   inner join workDay wd on (wd.region = e.region and pd.month = wd.month and pd.year = wd.year) " +
                    "union all " +
                    "select " +
                    "   ild.project_id, ild.name, ild.month, ild.year, (100*ild.val)/(wd.cnt) val " +
                    "from " +
                    "   illness_data ild " +
                    "   left join employee e on (e.id = :employeeId) " +
                    "   inner join workDay wd on (wd.region = e.region and ild.month = wd.month and ild.year = wd.year) " +
                    "union all " +
                    "select " +
                    "   vac.project_id, vac.name, vac.month, vac.year, (100*vac.val)/(wd.cnt) val " +
                    "from " +
                    "   vacation_plan_data vac " +
                    "   left join employee e on (e.id = :employeeId) " +
                    "   inner join workDay wd on (wd.region = e.region and vac.month = wd.month and vac.year = wd.year) " +
                ") t " +
            "), " +
            "vacation_fact_data(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                "    -"+EmployeePlanType.VACATION.getId()+", cast('"+EmployeePlanType.VACATION.getName()+"' as text), c.month, c.year, count(distinct c.caldate) val " +
                "from " +
                "    vacation vac " +
                "    inner join calendar c on (c.caldate between vac.begin_date and vac.end_date) " +
                "where " +
                    "vac.type_id in (" + VacationTypesEnum.WITH_NEXT_WORKING.getId() + ", " + VacationTypesEnum.WITH_PAY.getId()+ ", " + VacationTypesEnum.WITHOUT_PAY.getId() +") " +
                    "and vac.status_id in ("+ VacationStatusEnum.APPROVED.getId()+")" +
                    "and vac.employee_id = :employeeId " +
                    "and ( " +
                    "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                    "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                    "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                    "or (c.year > :yearStart and c.year < :yearEnd) " +
                    ") " +
                "group by " +
                "    vac.employee_id, c.month, c.year " +
            "), " +
            "project_fact(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                    "tsd.proj_id, pr.name, c.month, c.year, sum(tsd.duration) " +
                "from " +
                    "time_sheet ts, " +
                    "time_sheet_detail tsd, " +
                    "calendar c, " +
                    "project pr " +
                "where " +
                    "ts.calDate = c.calDate " +
                    "and ts.ts_type_id = " + TypesOfTimeSheetEnum.REPORT.getId() +
                    "and tsd.proj_id = pr.id " +
                    "and ts.id = tsd.time_sheet_id " +
                    "and ts.emp_id = :employeeId " +
                    "and ( " +
                    "   (c.year = :yearStart and c.year = :yearEnd   and c.month between :monthStart and :monthEnd) " +
                    "or (c.year = :yearStart and c.year < :yearEnd   and c.month > :monthStart) " +
                    "or (c.year = :yearEnd   and c.year > :yearStart and c.month < :monthEnd) " +
                    "or (c.year > :yearStart and c.year < :yearEnd) " +
                    ") " +
                "group by " +
                    "c.month, c.year, tsd.proj_id, pr.name " +
            "), " +
            "fact_data(project_id, name, month, year, val) as " +
            "( " +
                "select " +
                    "pf.project_id, pf.name, pf.month, pf.year, 100*pf.val/(8*wd.cnt) prc " +
                "from " +
                    "project_fact pf " +
                    "left join employee e on (e.id = :employeeId) " +
                    "left join workDay wd on (wd.region = e.region and pf.month = wd.month and pf.year = wd.year) " +
                "union all " +
                "select " +
                "   ild.project_id, ild.name, ild.month, ild.year, (100*ild.val)/(wd.cnt) val " +
                "from " +
                "   illness_data ild " +
                "   left join employee e on (e.id = :employeeId) " +
                "   inner join workDay wd on (wd.region = e.region and ild.month = wd.month and ild.year = wd.year) " +
                "union all " +
                "select " +
                "   vac.project_id, vac.name, vac.month, vac.year, (100*vac.val)/(wd.cnt) val " +
                "from " +
                "   vacation_fact_data vac " +
                "   left join employee e on (e.id = :employeeId) " +
                "   inner join workDay wd on (wd.region = e.region and vac.month = wd.month and vac.year = wd.year) " +
            ") " +
            "select " +
                "project_id, name, month, year, val, isFact " +
            "from " +
            "( " +
                "select project_id, name, month, year, val, 0 isFact from plan_data " +
                    "union all " +
                "select project_id, name, month, year, val, 1 isFact from fact_data f " +
            ") t " +
            "order by case sign(t.project_id) when -1 then 0 when 1 then 1 when 0 then 2 end, name ");

        query.setParameter("employeeId", employeeId);
        query.setParameter("monthStart", monthBeg);
        query.setParameter("yearStart", yearBeg);
        query.setParameter("monthEnd", monthEnd);
        query.setParameter("yearEnd", yearEnd);

        return query.getResultList();
    }

}
