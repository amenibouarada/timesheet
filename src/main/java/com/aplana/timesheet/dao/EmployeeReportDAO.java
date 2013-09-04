package com.aplana.timesheet.dao;

import com.aplana.timesheet.enums.TypesOfActivityEnum;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/* класс для запросов для таблички детализации месячных данных по проектам */
@Repository
public class EmployeeReportDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /* получаем список типов активности и суммы времени по каждому*/
    public List<Object[]> getEmployeeMonthData(Integer employee_id, Integer year, Integer month) {
        List<Integer> projectAndProjectPresale = new ArrayList<Integer>();
        projectAndProjectPresale.add(TypesOfActivityEnum.PROJECT.getId());
        projectAndProjectPresale.add(TypesOfActivityEnum.PROJECT_PRESALE.getId());

        final Query queryPPS = entityManager.createQuery(
                " select min(ac)," +
                "        p," +
                "        sum(tsd.duration)" +
                " from Calendar c" +
                " inner join c.timeSheets ts" +
                " inner join ts.timeSheetDetails tsd" +
                " inner join tsd.actType ac" +
                " left outer join tsd.project p" +
                " where c.year = :year and c.month = :month" +
                " and ts.employee.id = :employee" +
                " and ac.id in :activityType" +
                " group by p" +
                " order by 1 asc, p.name asc")
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("employee", employee_id)
                .setParameter("activityType", projectAndProjectPresale);

        final Query queryOther = entityManager.createQuery(
                " select ac," +
                "        p," +
                "        sum(tsd.duration)" +
                " from Calendar c" +
                " inner join c.timeSheets ts" +
                " inner join ts.timeSheetDetails tsd" +
                " inner join tsd.actType ac" +
                " left outer join tsd.project p" +
                " where c.year = :year and c.month = :month" +
                " and ts.employee.id = :employee" +
                " and ac.id not in :activityType" +
                " group by ac, p" +
                " order by ac asc, p.name asc")
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("employee", employee_id)
                .setParameter("activityType", projectAndProjectPresale);

        List<Object[]> result = new ArrayList<Object[]>();
        result.addAll(queryPPS.getResultList());
        result.addAll(queryOther.getResultList());
        return result;
    }
}
