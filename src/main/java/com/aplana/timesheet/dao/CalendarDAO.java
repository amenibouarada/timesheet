package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Holiday;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository
public class CalendarDAO {
	private static final Logger logger = LoggerFactory.getLogger(CalendarDAO.class);

    private static final String BEGIN_DATE = "beginDate";
    private static final String END_DATE = "endDate";
    private static final String REGION = "region";

    private static final String HOLIDAYS_FOR_REGION_BETWEEN_DATES = String.format(
            "from Holiday as h where ((h.calDate.calDate between :%s and :%s) and (h.region is null or h.region = :%s))",
            BEGIN_DATE,
            END_DATE,
            REGION
    );

    private static final String HOLIDAYS_ONLY_FOR_REGION_BETWEEN_DATES = String.format(
            "from Holiday as h where ((h.calDate.calDate between :%s and :%s) and h.region = :%s)",
            BEGIN_DATE,
            END_DATE,
            REGION
    );

    @PersistenceContext
	private EntityManager entityManager;

	public Calendar find(Timestamp date) {
		Query query = entityManager.createQuery(
                "select c from Calendar as c where c.calDate=:calDate"
        ).setParameter( "calDate", date );

		try {
            return (Calendar) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Date '{}' not found in Calendar.", date.toString());
            return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Calendar getMinDateList() {
        Calendar min = ( Calendar ) entityManager.createQuery("SELECT min(c) as min FROM Calendar as c").getResultList().get( 0 );
        logger.info( "getMinMaxYearsList MIN {}", min.toString() );
        return min;
    }

	@SuppressWarnings("unchecked")
	public Calendar getMaxDateList() {
		Calendar max = ( Calendar ) entityManager.createQuery("SELECT max(c) as max FROM Calendar as c").getResultList().get( 0 );
        logger.info( "getMinMaxYearsList MAX {}", max.toString() );
		return max;
	}

	public String getMonthTxt (Integer month){
		Query query = entityManager.createQuery(
                "select distinct(c.monthTxt) from Calendar as c where month=:monthPar"
        ).setParameter( "monthPar", month );

        return query.getResultList().get(0).toString();
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getMonth(Integer year){
		Query query = entityManager.createQuery(
                "select distinct(c.month) from Calendar as c " +
                        "where month in " +
                            "(select distinct(c.month) from Calendar as c " +
                                    "where year=:yearPar) order by c.month asc"
        ).setParameter( "yearPar", year );

        return ( ( List<Integer> ) query.getResultList() );
	}
	/**
	 * Возвращает все даты
	 */
	@SuppressWarnings("unchecked")
	public List<Calendar> getDateList(Integer year, Integer month) {
		Query query = entityManager.createQuery(
                "from Calendar as c where c.year=:yearPar and c.month=:monthPar order by c.calDate asc"
        ).setParameter("yearPar", year).setParameter("monthPar", month);

		return query.getResultList();
	}
	
	/**
	 * Проверяет наличие года в системе
	 * param year
	 * return true если год существует в системе
	 * return false если год не существует в системе
	 */
	public boolean yearValid(Integer year){
		Query query = entityManager.createQuery(
                "select distinct(c.year) from Calendar as c where c.year =:yearPar"
        ).setParameter("yearPar", year);

        return query.getResultList() != null;
	}

	public boolean monthValid(Integer year, Integer month) {
		Query query = entityManager.createQuery(
                "select c.calDate from Calendar as c where c.year =:yearPar and c.month =:monthPar"
        ).setParameter("yearPar", year).setParameter("monthPar", month);

        return query.getResultList() != null;
	}

    /**
     * Возвращает последний рабочий день месяца для переданной даты (включая саму дату).
     *
     * @param day
     * @return Calendar
     */
    public Calendar getLastWorkDay(Calendar day) {
        Date monthLastDay = DateTimeUtil.stringToTimestamp(DateTimeUtil.getLastDayOfMonth(day.getCalDate()));

        Query query = entityManager.createQuery(
                "select max(c) from Calendar as c " +
                        "left outer join c.holidays as h with h.region.id is null " +
                        "where c.calDate<=:calDatePar and h.id is null "
        ).setParameter("calDatePar", monthLastDay);

        return (Calendar) query.getSingleResult();
    }

    /**
     * Возвращает следующий рабочий день месяца для переданной даты.
     * @param day
     * @param region
     * @return Calendar
     */
    public Calendar getNextWorkDay(Calendar day, Region region) {
        Query query = entityManager.createQuery(
                "select c " +
                        "from Calendar as c " +
                        "left outer join c.holidays as h with h.region.id is null or h.region.id=:region " +
                        "where c.calDate>:calDatePar and h.id is null " +
                        "order by c.calDate asc " +
                        "limit 1")
                .setParameter("calDatePar", new Date(day.getCalDate().getTime()))
                .setParameter("region", region.getId());

        return ( Calendar ) query.getResultList().get( 0 );
    }

    /**
     * Возвращает количество выходных дней за выбранный период для конкретного региона
     */
    public Integer getHolidaysCountForRegion(Date beginDate, Date endDate, Region region){
        Query query = entityManager.createQuery("select count (*) " + HOLIDAYS_FOR_REGION_BETWEEN_DATES);

        setParametersForHolidaysQuery(beginDate, endDate, region, query);

        return ((Long) query.getSingleResult()).intValue();
    }

    public List<Holiday> getHolidaysForRegion(Date beginDate, Date endDate, Region region) {
        final Query query = entityManager.createQuery(HOLIDAYS_FOR_REGION_BETWEEN_DATES);

        setParametersForHolidaysQuery(beginDate, endDate, region, query);

        return query.getResultList();
    }

    /**
     * Возвращает список праздничных дней только для указанного региона
     * @param dateFrom
     * @param dateTo
     * @param region
     * @return
     */
    public List<Holiday> getHolidaysOnlyForRegion(Date dateFrom, Date dateTo, Region region) {
        final Query query = entityManager.createQuery(HOLIDAYS_ONLY_FOR_REGION_BETWEEN_DATES);

        setParametersForHolidaysQuery(dateFrom, dateTo, region, query);

        return query.getResultList();
    }

    private void setParametersForHolidaysQuery(Date beginDate, Date endDate, Region region, Query query) {
        query.setParameter(BEGIN_DATE, beginDate)
             .setParameter(END_DATE, endDate)
             .setParameter(REGION, region);
    }

    // возвращает выходные дни без региональных
    public List<Holiday> getHolidaysInInterval(Date beginDate, Date endDate){
        Query query = entityManager.createQuery(
                "select h from Holiday as h where h.calDate.calDate between :beginDate AND :endDate AND h.region is null")
                .setParameter("beginDate", beginDate)
                .setParameter("endDate", endDate);

        return query.getResultList();
    }

    // возвращает все выходные дни без региональных
    public List<Holiday> getAllHolidaysInInterval(Date beginDate, Date endDate){
        Query query = entityManager.createQuery(
                "select h from Holiday as h where h.calDate.calDate between :beginDate AND :endDate")
                .setParameter("beginDate", beginDate)
                .setParameter("endDate", endDate);

        return query.getResultList();
    }

    public int getWorkDaysCountForRegion(Region region, Integer year, Integer month, @NotNull Date fromDate) {
        final Query query = entityManager.createQuery(
                "select count(c) - count(h)" +
                    " from Calendar c" +
                    " left outer join c.holidays h with (h.region.id is null or h.region.id = :regionId)" +
                    " where YEAR(c.calDate) = :year and MONTH(c.calDate) = :month" +
                    " and c.calDate >= :from_date")
                .setParameter("regionId", region.getId())
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("from_date", fromDate);

        return ((Long) query.getSingleResult()).intValue();
    }

    public int getWorkDaysCountForRegion(Region region, Integer year, Integer month, @Nullable Date fromDate,
                                         @Nullable Date toDate) {
        final Date qFromDate = (fromDate != null) ? fromDate : DateTimeUtil.parseStringToDateForDB(DateTimeUtil.MIN_DATE);
        final Date qToDate = (toDate != null) ? toDate : DateTimeUtil.parseStringToDateForDB(DateTimeUtil.MAX_DATE);

        final Query query = entityManager.createQuery(
                        " select count(c)-count(h)" +
                        " from Calendar c" +
                        " left join c.holidays h with (h.region.id is null or h.region.id = :regionId)" +
                        " where YEAR(c.calDate) = :year and MONTH(c.calDate) = :month" +
                        " and c.calDate between :dateBeg and :dateEnd")
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("regionId", region.getId())
                .setParameter("dateBeg", qFromDate)
                .setParameter("dateEnd", qToDate);

        return ((Long) query.getSingleResult()).intValue();
    }

    public Date tryGetMaxDateMonth(Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "select MAX(calDate)" +
                        " from Calendar c" +
                        " where YEAR(c.calDate) = :year and MONTH(c.calDate) = :month"
        ).setParameter("year", year).setParameter("month", month);
        Date result;
        try {
            result = (Date) query.getSingleResult();
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    public Date tryGetMinDateMonth(Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "select MIN(calDate)" +
                        " from Calendar c" +
                        " where YEAR(c.calDate) = :year and MONTH(c.calDate) = :month"
        ).setParameter("year", year).setParameter("month", month);
        Date result;
        try {
            result = (Date) query.getSingleResult();
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    public int getCountWorkDayPriorDate(Region region, Integer year, Integer month, Date startDate, @NotNull Date toDate) {
        final Query query = entityManager.createQuery(
                "select count(c) - count(h)" +
                        " from Calendar c" +
                        " left outer join c.holidays h with (h.region.id is null or h.region.id = :regionId)" +
                        " where YEAR(c.calDate) = :year and MONTH(c.calDate) = :month" +
                        " and c.calDate >= :startDate and c.calDate <= :toDate")
                .setParameter("regionId", region.getId())
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("startDate", startDate)
                .setParameter("toDate", toDate);

        return ((Long) query.getSingleResult()).intValue();
    }

    public Integer getCountWorkDaysForPeriodForRegion(Date begin, Date end, Region region) {
        final Query query = entityManager.createQuery(
                "select count(c) " +
                        " from Calendar c" +
                        " left join c.holidays h " +
                        " where c.calDate >= :begin and c.calDate <= :endDate " +
                        " and h is null" +
                        " and (h.region.id is null or h.region = :region)");
        query.setParameter("begin",begin);
        query.setParameter("endDate",end);
        query.setParameter("region", region);
        return ((Long) query.getSingleResult()).intValue();
    }
    public Integer getCountDaysForPeriodForRegionExConsiderHolidays(Date begin, Date end, Region region) {
        final Query query = entityManager.createQuery(
                "select count(c) " +
                        " from Calendar c" +
                        " left join c.holidays h " +
                        " where c.calDate >= :begin and c.calDate <= :endDate " +
                        " and (h is null or h.consider = true) " +
                        " and (h.region is null or h.region = :region)");
        query.setParameter("begin",begin);
        query.setParameter("endDate",end);
        query.setParameter("region", region);
        return ((Long) query.getSingleResult()).intValue();
    }
}