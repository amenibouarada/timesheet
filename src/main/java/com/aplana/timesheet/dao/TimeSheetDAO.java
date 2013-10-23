package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static com.aplana.timesheet.enums.VacationStatusEnum.APPROVED;
import static com.aplana.timesheet.enums.VacationTypesEnum.PLANNED;

@Repository
public class TimeSheetDAO {

    @Autowired
    CalendarDAO calendarDAO;
    @Autowired
    IllnessDAO illnessDAO;
    @Autowired
    VacationDAO vacationDAO;
    @Autowired
    BusinessTripDAO businessTripDAO;
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetDAO.class);
    @PersistenceContext
    private EntityManager entityManager;

    public void storeTimeSheet(TimeSheet timeSheet) {
        if (timeSheet.getId() == null) {  //создается новый отчет, а не редактируется старый
            timeSheet.setCreationDate(new java.util.Date());
        }
        TimeSheet tsMerged = entityManager.merge(timeSheet);
        logger.info("timeSheet merged.");
        entityManager.flush();
        logger.info("Persistence context synchronized to the underlying database.");
        timeSheet.setId(tsMerged.getId());
        logger.debug("Flushed TimeSheet object id = {}", tsMerged.getId());
    }

    @SuppressWarnings("unchecked")
    public TimeSheet findForDateAndEmployee(Calendar date, Integer employeeId) {
        Query query = entityManager.createQuery(
                "select ts from TimeSheet as ts where ts.calDate = :calDate and ts.employee.id = :employeeId AND (ts.type = 0)"
        ).setParameter("calDate", date).setParameter("employeeId", employeeId);

        List<TimeSheet> result = query.getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Формирует список объектов, хранящих дату и работы по сотруднику за каждый день месяца
     *
     * @param year
     * @param month
     * @param employee
     * @return List<DayTimeSheet>
     */
    @SuppressWarnings("unchecked")
    public List<DayTimeSheet> findDatesAndReportsForEmployee(Integer year, Integer month, Integer region, Employee employee) {

        // Я не знаю как написать это на HQL, но на SQL пишется легко и непринужденно.
        Query query = entityManager.createNativeQuery(
                "select " +
                        "c.caldate caldate, " +
                        "h.id holiday_id, " +
                        "ts.id timesheet_id, " +
                        "SUM(tsd.duration), " +
                        "tsd.act_type "
                        + "from calendar c "
                        + "left outer join time_sheet as ts " +
                        "on ts.emp_id = :employeeId and ts.caldate=c.caldate "
                        + "left outer join holiday h " +
                        "on c.caldate=h.caldate and (h.region is null or h.region=:region) "
                        + "left outer join time_sheet_detail as tsd " +
                        "on ts.id=tsd.time_sheet_id "
                        + "where c.year=:yearPar " +
                        "and c.month=:monthPar "
                        + "group by " +
                        "c.caldate, " +
                        "h.id, " +
                        "ts.id, " +
                        "tsd.act_type "
                        + "order by c.calDate asc"
        ).setParameter("yearPar", year).setParameter("monthPar", month)
                .setParameter("region", region).setParameter("employeeId", employee.getId());

        List result = query.getResultList();

        List<DayTimeSheet> dayTSList = new ArrayList<DayTimeSheet>();

        HashMap<Long, DayTimeSheet> map = new HashMap<Long, DayTimeSheet>();
        for (Object object : result) {
            Object[] item = (Object[]) object;
            //дата в месяце
            Timestamp calDate = new Timestamp(((Date) item[0]).getTime());
            //если айдишник из таблицы календарь есть то это выходной
            Boolean holiday = item[1] != null;
            //айдишник в ts. нужен нам, чтобы отчет за один день суммировать
            Integer tsId = item[2] != null ? ((BigDecimal) item[2]).intValue() : null;
            //время за каждую деятельность(может быть несколько за один день)
            BigDecimal duration = item[3] != null ? ((BigDecimal) item[3]) : null;
            //по этому полю определяем отпуск\отгул и т.п.
            Integer actType = item[4] != null ? ((Integer) item[4]) : null;

            // Если нерабочая активность - сразу проставим в duration 0
            if (duration != null && !TypesOfActivityEnum.isEfficientActivity(actType)) {
                duration = BigDecimal.ZERO;
            }

            if (!map.containsKey(calDate.getTime())) {
                DayTimeSheet ds = new DayTimeSheet(calDate, holiday, tsId, actType, duration, employee);
                ds.setTimeSheetDAO(this);
                ds.setIllnessDAO(illnessDAO);
                ds.setVacationDAO(vacationDAO);
                ds.setBusinessTripDAO(businessTripDAO);
                map.put(calDate.getTime(), ds);
            } else {
                DayTimeSheet dts = map.get(calDate.getTime());
                dts.setDuration(dts.getDuration().add(duration));
            }
        }
        for (DayTimeSheet val : map.values()) {
            dayTSList.add(val);
        }

        Collections.sort(dayTSList);
        return dayTSList;
    }

    /**
     * Возвращает самый последний план относительно date
     *
     * @param date
     * @param employeeId
     * @return отчет И МОЖЕТ быть null
     */
    @SuppressWarnings("unchecked")
    public TimeSheet findLastTimeSheetBefore(Calendar date, Integer employeeId) {
        Query query = entityManager.createQuery(
                "select ts "
                        + "from TimeSheet as ts "
                        + "where ts.calDate <:calDate "
                        + "and ts.employee.id = :employeeId AND (ts.type = 0) "
                        + "order by ts.calDate desc"
        ).setParameter("calDate", date).setParameter("employeeId", employeeId);

        List<TimeSheet> result = query.getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Возвращает следующий план относительно date
     *
     * @param nextDate
     * @param employeeId
     * @return отчет
     */
    @SuppressWarnings("unchecked")
    public TimeSheet findNextTimeSheetAfter(Calendar nextDate, Integer employeeId) {
        Query query = entityManager.createQuery(""
                + "select ts "
                + "from TimeSheet as ts "
                + "where ts.calDate = :calDate "
                + "and ts.employee.id = :employeeId "
                + "AND (ts.type = 0) "
                + "order by ts.calDate asc"
        ).setParameter("calDate", nextDate).setParameter("employeeId", employeeId);

        List<TimeSheet> result = query.getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public TimeSheet find(Integer id) {
        return entityManager.find(TimeSheet.class, id);
    }

    // возвращает следующий рабочий день, после даты последнего списания занятости
    public Calendar getDateNextAfterLastDayWithTS(Employee employee) {
        Query query = entityManager.createQuery(
                "SELECT MAX(ts.calDate) FROM TimeSheet ts WHERE ts.employee = :employee and (ts.type = 0)"
        ).setParameter("employee", employee);

        if (!query.getResultList().isEmpty() && query.getSingleResult() != null) {
            return calendarDAO.getNextWorkDay((Calendar) query.getSingleResult(), employee.getRegion());
        } else {
            return null;
        }
    }

    // возвращает список следующих рабочих дней, после даты последнего списания занятости для всех сотрудников центра
    public Map<Integer, Date> getDateNextAfterLastDayWithTSMap(Division division) {

        /*
          На HQL это написать нельзя из-за строчки INNER JOIN calendar calnext ON calnext.caldate>tscal.maxcaldate
          При желании можно переписать на Criteria
         */

        final Query query = entityManager.createNativeQuery("SELECT tscal.empid, MIN(calnext.calDate)" +
                " FROM (SELECT emp.id empid, MAX(cal.calDate) maxcaldate" +
                "       FROM calendar cal" +
                "       INNER JOIN time_sheet ts on cal.caldate=ts.caldate" +
                "       INNER JOIN employee emp on emp.id=ts.emp_id" +
                "       GROUP BY emp.id" +
                "      ) tscal" +
                " INNER JOIN employee emp1 ON emp1.id=tscal.empid" +
                " INNER JOIN division d ON d.id=emp1.division" +
                " INNER JOIN region r ON r.id=emp1.region" +
                " INNER JOIN calendar calnext ON calnext.caldate>tscal.maxcaldate" +
                " WHERE d.id=:division " +
                " AND NOT EXISTS (" +
                "       SELECT i.employee_id,cal.caldate from calendar cal " +
                "       RIGHT JOIN illness i on cal.caldate between i.begin_date and i.end_date where " +
                "       calnext.caldate = cal.caldate and emp1.id= i.employee_id) AND " +
                " NOT EXISTS (" +
                "       SELECT v.employee_id,calv.caldate from calendar calv " +
                "       RIGHT JOIN vacation v on calv.caldate between v.begin_date and v.end_date " +
                "       WHERE calv.caldate = calnext.caldate AND v.employee_id = emp1.id AND v.status_id = :statusId AND v.type_id <> :typePlanned) " +
                " AND " +
                " NOT EXISTS (" +
                "       SELECT h.caldate from holiday h " +
                "       WHERE h.calDate=calnext.calDate and (h.region=r.id or h.region is null) )" +
                " GROUP BY 1" +
                " ORDER BY 1").setParameter("division", division).setParameter("statusId", APPROVED.getId()).setParameter("typePlanned", PLANNED.getId());

        final List resultList = query.getResultList();
        final Map<Integer, Date> resultMap = new HashMap<Integer, Date>(resultList.size());
        for (Object next : resultList) {
            Object[] item = (Object[]) next;
            resultMap.put((Integer) item[0], (Date) item[1]);
        }

        return resultMap;
    }

    public void delete(TimeSheet timeSheet) {
        entityManager.remove(timeSheet);
    }

    public List<TimeSheet> getTimeSheetsForEmployee(Employee employee, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "from TimeSheet ts where ts.employee = :employee and YEAR(ts.calDate.calDate) = :year and MONTH(ts.calDate.calDate) = :month and (ts.type = 0)"
        ).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);

        return query.getResultList();
    }

    public Boolean timeSheetTrouble(Integer id) {
        final Query query = entityManager.createQuery(
                "from TimeSheet ts inner join ts.timeSheetDetails tsd where ts.id = :id and tsd.problem <> '' and (ts.type = 0)"
        ).setParameter("id", id);
        return query.getResultList().size() != 0;
    }


}