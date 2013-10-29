package com.aplana.timesheet.dao;

import com.aplana.timesheet.enums.TypesOfActivityEnum;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/* класс для запросов для таблички детализации месячных данных по проектам */
@Repository
public class EmployeeReportDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /* возвращает список типов активности, проектов, плановых часов и фактических часов по отправленным отчётам
     * часы по отпускам болезням нужно посчитать отдельно, в данный запрос они не влезли */
    public List<Object[]> getEmployeeMonthData(Integer employee_id, Integer year, Integer month) {
        final Query query = entityManager.createNativeQuery(
                "select foo.type_id as type_id,\n" +
                "       p.id as project_id,\n" +
                "       sum(foo.dur_p) as dur_plan,\n" +
                "       sum(foo.dur_f) as dur_fact\n" +
                "from\n" +
                "(select case when di.id = " + TypesOfActivityEnum.PROJECT_PRESALE.getId() + " then " + TypesOfActivityEnum.PROJECT.getId() + " else di.id end as type_id,\n" +
                "       p.id as project_id,\n" +
                "       epp.value as dur_p,\n" +
                "       0 as dur_f\n" +
                "from employee_project_plan epp\n" +
                "inner join project p on p.id = epp.project_id\n" +
                "inner join dictionary_item di on di.id=p.state\n" +
                "where epp.employee_id = :employee_id\n" +
                "and   epp.year = :year\n" +
                "and   epp.month = :month\n" +
                "union all\n" +
                "select case when di.id = " + TypesOfActivityEnum.PROJECT_PRESALE.getId() + " then " + TypesOfActivityEnum.PROJECT.getId() + " else di.id end as type_id,\n" +
                "       p.id as project_id,\n" +
                "       0 as dur_p,\n" +
                "       tsd.duration as dur_f\n" +
                "from   calendar c\n" +
                "inner join time_sheet ts on ts.caldate=c.caldate\n" +
                "inner join time_sheet_detail tsd on tsd.time_sheet_id = ts.id\n" +
                "inner join dictionary_item di on tsd.act_type = di.id\n" +
                "left outer join project p on p.id=tsd.proj_id\n" +
                "where  c.year = :year\n" +
                "and ts.type = 0\n" +
                "and    c.month = :month\n" +
                "and    ts.emp_id = :employee_id\n" +
                ") as foo\n" +
                "left outer join project p on p.id=foo.project_id\n" +
                "group by foo.type_id, p.id\n" +
                "order by type_id, p.name asc")
                .setParameter("employee_id", employee_id)
                .setParameter("year", year)
                .setParameter("month", month);
        return query.getResultList();
    }
}