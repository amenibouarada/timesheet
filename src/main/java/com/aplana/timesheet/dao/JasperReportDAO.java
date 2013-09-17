package com.aplana.timesheet.dao;

import com.aplana.timesheet.enums.OvertimeCategory;
import com.aplana.timesheet.enums.Report07PeriodTypeEnum;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.HibernateQueryResultDataSource;
import com.aplana.timesheet.util.report.Report7Period;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.time.DateUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.timesheet.enums.TypesOfActivityEnum.getProjectPresaleNonProjectActivityId;
import static com.aplana.timesheet.enums.VacationStatusEnum.APPROVED;
import static com.aplana.timesheet.enums.IllnessTypesEnum.ILLNESS;

@Repository
public class JasperReportDAO {

    public static final String HOURS_WITH_PERCENTS = ", ч. (%)";
    private DecimalFormat doubleFormat = new DecimalFormat("#.##");

    private static Map<Class, String[]> fieldsMap = new HashMap<Class, String[]>( 6 );

    static {
        //может быть это вынести в сам класс Reports? kss - нет, это мапинг полей datasource для отчета, его логично делать там же, где формируются данные.
        fieldsMap.put( Report01.class, new String[] { "id", "name", "caldate", "projnames", "overtime", "duration",
                "holiday", "region", "projdetail", "durationdetail", "region_name", "project_role", "vacation", "illness",
                "billable", "overtime_cause", "comment", "compensation", "vacation_type" } );
        fieldsMap.put( Report02.class, new String[] { "name", "empldivision", "project",
                "taskname", "duration", "day_type", "region", "region_name", "project_role", "project_state", "billable", "vacation_type" } );
        fieldsMap.put( Report03.class, new String[] { "name", "empldivision", "project", "taskname",
                "caldate", "duration", "day_type", "region", "region_name", "project_role", "project_state", "billable", "vacation_type" } );
        fieldsMap.put( Report04.class, new String[] { "date", "name", "region_name", "role" } );
        fieldsMap.put( Report05.class, new String[] { "calDate", "name", "value", "pctName", "actType",
                "role", "taskName", "duration", "description", "problem", "region_name", "workplace", "project_role",
                "day_type", "billable", "billable_comment", "plan" } );
        fieldsMap.put( Report06.class, new String[] { "duration", "project_role", "name", "act_cat", "region_name", "role", "act_type", "begin_date", "end_date"} );
    }

    private static final Logger logger = LoggerFactory.getLogger(JasperReportDAO.class);

    private static final String DIVISION_CLAUSE = "d.id=:emplDivisionId AND ";
    private static final String EMPLOYEE_CLAUSE = "empl.id=:emplId AND ";
    private static final String REGION_CLAUSE   = "empl.region.id in :regionIds AND ";
    private static final String PROJECT_CLAUSE  = "tsd.project.id=:projectId AND ";
    private static final String DIVISION_SQL_CLAUSE = "division.id=:emplDivisionId AND ";
    private static final String EMPLOYEE_SQL_CLAUSE = "empl.id=:emplId AND ";
    private static final String REGION_SQL_CLAUSE   = "region.id in :regionIds AND ";
    private static final String PROJECT_SQL_CLAUSE  = "project.id=:projectId AND ";
    private static final String BILLABLE_CLAUSE = "(epbillable.billable = 'true' OR (epbillable.billable is null AND empl.billable = 'true')) AND ";
    private static final String HIDE_INACTIVE_PROJECTS_CLAUSE = "AND project.active = 'true' ";

    private static final String WITHOUT_CLAUSE  = "";

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TSPropertyProvider propertyProvider;


    public HibernateQueryResultDataSource getReportData(BaseReport report) {
        List resultList     = getResultList     ( report );

        if (resultList != null && !resultList.isEmpty()) {
            return new HibernateQueryResultDataSource(resultList, fieldsMap.get( report.getClass() ) );
        } else {
            return null;
        }
    }

    private List getResultList( BaseReport baseReport ) {
        //TODO выпилить это, заменить на иерархию классов
        if ( baseReport instanceof Report01 ) {
            Report01 report = ( Report01 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report02 ) {
            Report02 report = ( Report02 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report03 ) {
            Report03 report = ( Report03 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report04 ) {
            Report04 report = ( Report04 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report05 ) {
            Report05 report = ( Report05 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report06 ) {
            Report06 report = ( Report06 ) baseReport;
            return getResultList( report );
        }

        throw new IllegalArgumentException();
    }

    private Collection<String> getProjectNamesList( List projResultList, Integer emplId, Timestamp date ) {
        Set<String> projNamesList = new HashSet<String>();

        for ( Object aProjResultList : projResultList ) {
            Object[] o = ( Object[] ) aProjResultList;

            if ( o[ 0 ].equals( emplId ) && o[ 1 ].equals( date ) ) {
                String projName = ( String ) o[ 2 ];
                projNamesList.add( projName );
            }
        }
        return projNamesList;
    }

    private String getStringWithProjectNames( Collection<String> projNamesList ) {
        StringBuffer projNames = new StringBuffer();

        for ( Iterator<String> namesIterator = projNamesList.iterator(); namesIterator.hasNext(); ) {
            String nextName = namesIterator.next();
            /* пропускаем null (скорее всего это непроектная деятельность) */
            if (nextName == null)
                continue;
            projNames.append( nextName );
            if ( namesIterator.hasNext() )
                projNames.append( ", " );
        }
        return projNames.toString();
    }

    @VisibleForTesting
    List getProjResultList( Report01 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = ! report.getDivisionOwnerId().equals(0);
        // Запрос достанет для сотрудников наименования проектов по датам
        Query projQuery = entityManager.createNativeQuery(
                "select empl.id, calendar.calDate, project.name " +
                "from time_sheet_detail timesheet_details " +
                    "       INNER JOIN time_sheet timesheet ON timesheet_details.time_sheet_id=timesheet.id " +
                    "       INNER JOIN employee empl    ON timesheet.emp_id=empl.id " +
                    "       INNER JOIN region region        ON empl.region=region.id " +
                    "       INNER JOIN division division    ON empl.division=division.id " +
                    "       LEFT OUTER JOIN calendar calendar  ON timesheet.caldate=calendar.caldate " +
                    "       LEFT OUTER JOIN project project    ON timesheet_details.proj_id=project.id " +
                "where " +
                    (withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE) +
                    (withRegionClause ? REGION_SQL_CLAUSE : WITHOUT_CLAUSE ) +
                    "calendar.calDate between :beginDate and :endDate ");

        if (withRegionClause)
            projQuery.setParameter("regionIds", report.getRegionIds());
        if (withDivisionClause)
            projQuery.setParameter("emplDivisionId", report.getDivisionOwnerId());

        projQuery.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ))
                 .setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return projQuery.getResultList();
    }

    @VisibleForTesting
    List getResultList( Report01 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = ! report.getDivisionOwnerId().equals(0);

        String workDaySeparator = "";

        if(OvertimeCategory.HOLIDAY.equals(report.getCategory())) {
            workDaySeparator="holidays.calDate is not null AND";
        } else if(OvertimeCategory.SIMPLE.equals(report.getCategory())) {
            workDaySeparator="holidays.calDate is null AND";
        }

        // К сожалению HQL не может ворочить сложные запросы, пришлось писать native sql-запрос
        Query query = entityManager.createNativeQuery(
                "SELECT " +
                        "        empl.id AS col_0," +
                        "        empl.name AS col_1," +
                        "        timesheet.caldate AS col_2," +
                        "        cast('' AS varchar(255)) AS col_3," +
                        "        sum(timesheet_details.duration)-8 AS col_4," +
                        "        sum(timesheet_details.duration) AS col_5," +
                        "        holidays.id AS col_6," +
                        "        holidays.region AS col_7," +
                        "        CASE" +
                        "            WHEN (holidays.id is not null OR" +
                        "                  vacations.id is not null OR" +
                        "                  illnesses.id is not null) " +
                        "               THEN CASE" +
                        "                   WHEN project.id is not null THEN project.name " +
                        "                   ELSE cast('Внепроектная деятельность' AS varchar(255)) " +
                        "            END" +
                        "            ELSE cast('%NO_GROUPING%' AS varchar(255)) " +
                        "        END AS col_8," +
                        "        CASE" +
                        "            WHEN (holidays.id is not null OR" +
                        "                  vacations.id is not null OR " +
                        "                  illnesses.id is not null) " +
                        "               THEN SUM(timesheet_details.duration) " +
                        "            ELSE cast(-1 as float4) " +
                        "        END AS col_9," +
                        "        region.name AS col_10," +
                        "        project_role.name AS col_11," +
                        "        vacations.id AS col_12," +
                        "        illnesses.id AS col_13," +
                        "        CASE " +
                        "            WHEN (epbillable.billable is not null) THEN epbillable.billable" +
                        "            ELSE empl.billable" +
                        "        END AS billable, " +
                        "        over_cause.value AS col_15, " +
                        "        overtime.comment AS col_16, " +
                        "        compensation.value AS col_17, " +
                        "        vacation_type.value AS col_18, " +
                        "        CASE" +
                        "           WHEN (holidays.id is not null) " +
                        "               THEN 1 " +
                        "           ELSE " +
                        "               CASE " +
                        "                   WHEN (vacations.id is not null) " +
                        "                       THEN 2 " +
                        "                   ELSE " +
                        "                       CASE " +
                        "                           WHEN (illnesses.id is not null) " +
                        "                               THEN 3 " +
                        "                           ELSE 0" +
                        "                       END" +
                        "               END" +
                        "        END AS day_type " +
                        "FROM " +
                        "       time_sheet_detail timesheet_details " +
                        "       INNER JOIN time_sheet timesheet ON timesheet_details.time_sheet_id=timesheet.id " +
                        "       INNER JOIN employee empl    ON timesheet.emp_id=empl.id " +
                        "       INNER JOIN region region        ON empl.region=region.id " +
                        "       INNER JOIN division division    ON empl.division=division.id " +
                        "       LEFT OUTER JOIN calendar calendar  ON timesheet.caldate=calendar.caldate " +
                        "       LEFT OUTER JOIN holiday holidays   ON calendar.caldate=holidays.caldate " +
                        "       LEFT OUTER JOIN project project    ON timesheet_details.proj_id=project.id " +
                        "       LEFT OUTER JOIN employee_project_billable epbillable    ON project.id = epbillable.project_id AND empl.id = epbillable.employee_id " +
                        "                                                                  AND timesheet.caldate BETWEEN epbillable.start_date AND epbillable.end_date " +
                        "       LEFT OUTER JOIN overtime_cause overtime    ON overtime.timesheet_id=timesheet.id " +
                        "       LEFT OUTER JOIN dictionary_item over_cause    ON over_cause.id=overtime.overtime_cause_id " +
                        "       LEFT OUTER JOIN dictionary_item compensation    ON compensation.id=overtime.compensation_id " +
                        "       LEFT OUTER JOIN project_role project_role ON timesheet_details.projectrole_id=project_role.id " +
                        "       LEFT OUTER JOIN vacation vacations ON " +
                        "               empl.id=vacations.employee_id AND " +
                        "               timesheet.caldate BETWEEN vacations.begin_date AND vacations.end_date " +
                        "               AND vacations.status_id=:status " +
                        "       LEFT OUTER JOIN dictionary_item vacation_type    ON vacation_type.id=vacations.type_id " +
                        "       LEFT OUTER JOIN illness illnesses ON " +
                        "               empl.id=illnesses.employee_id AND " +
                        "               timesheet.caldate BETWEEN illnesses.begin_date AND illnesses.end_date " +
                        "WHERE " +
                                (withDivisionClause ? "division.id = :emplDivisionId AND " : "") +
                                (withRegionClause ? "region.id in :regionIds AND " : "") +
                                workDaySeparator +
                                (report.getShowNonBillable() ? WITHOUT_CLAUSE : BILLABLE_CLAUSE) +
                        "        timesheet_details.act_type in :actTypes AND " +
                        "        timesheet.caldate BETWEEN :beginDate AND :endDate AND " +
                        "        (holidays.region is null OR holidays.region=region.id) " +
                        "GROUP BY" +
                        "        empl.id ," +
                        "        empl.name ," +
                        "        timesheet.caldate ," +
                        "        holidays.id ," +
                        "        holidays.region ," +
                        "        col_8 ," +
                        "        region.name ," +
                        "        project_role.name, " +
                        "        vacations.id ," +
                        "        illnesses.id ," +
                        "        epbillable.billable," +
                        "        vacation_type.value, " +
                        "        over_cause.value, " +
                        "        overtime.comment, " +
                        "        compensation.value " +
                        "HAVING" +
                        "        sum(timesheet_details.duration) > 8 " +
                        "        OR holidays.id is not null " +
                        "        OR vacations.id is not null " +
                        "        OR illnesses.id is not null " +
                        "ORDER BY" +
                        "        empl.name," +
                        "        day_type," +
                        "        timesheet.caldate"
        );


        if (withRegionClause) {
            query.setParameter("regionIds", report.getRegionIds());
		}
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionOwnerId());
        }

        query   .setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()))
                .setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()))
                .setParameter("status", APPROVED.getId())
                .setParameter("actTypes", getProjectPresaleNonProjectActivityId()); // отчет только по этим типам активностей ( 12 , 13 , 14 )

        List resultList = query.getResultList();
        //похоже это нужно вынести в запрос, и не делать этого в Java - kss: это нельзя сделать в hql запросе - только в nativesql c использованием специфических для БД функций.
        List projResultList = getProjResultList ( report );

        // Пробежим весь список отчетов и заполним в них списки проектов, над которыми работали сотрудники
        for ( Object aResultList : resultList ) {
            Object[] next = ( Object[] ) aResultList;

            if ( next[ 6 ] != null ) continue;

            next[ 3 ] = getStringWithProjectNames(
                    getProjectNamesList( projResultList, ( Integer ) next[ 0 ], new Timestamp ( ((Date)next[ 2 ]).getTime() )
            ) );
        }
        return resultList;
    }

    @Language("SQL")
    private static final String report02QueryString =
                        "SELECT " +
                                "empl.name as col_0, " +
                                "division.name as col_1, " +
                                "project.name as col_2, " +
                                "project_task.name as col_3, " +
                                "sum(timesheet_details.duration) as col_4, " +
                                "CASE" +
                                "   WHEN (holidays.id is not null) " +
                                "       THEN 1 " +
                                "   ELSE " +
                                "       CASE " +
                                "           WHEN (vacations.id is not null) " +
                                "               THEN 2 " +
                                "           ELSE " +
                                "               CASE " +
                                "                   WHEN (illnesses.id is not null) " +
                                "                       THEN 3 " +
                                "                   ELSE 0 " +
                                "               END " +
                                "       END " +
                                "END as day_type, " +
                                "max(h_region.id) as col_6, " +
                                "region.name as col_7, " +
                                "project_role.name as col_8, " +
                                "project_state.value as col_9, " +
                                "CASE " +
                                "    WHEN (epbillable.billable is not null) THEN epbillable.billable " +
                                "    ELSE empl.billable " +
                                "END as col_10, " +
                                "vacation_type.value AS col_11 " +
                        "FROM time_sheet_detail AS timesheet_details " +
                                "INNER JOIN time_sheet timesheet ON timesheet_details.time_sheet_id=timesheet.id " +
                                "INNER JOIN employee empl    ON timesheet.emp_id=empl.id " +
                                "INNER JOIN calendar calendar  ON timesheet.caldate=calendar.caldate " +
                                "INNER JOIN division division    ON empl.division=division.id " +
                                "INNER JOIN project project    ON timesheet_details.proj_id=project.id " + "%s " +
                                "INNER JOIN region region        ON empl.region=region.id " +
                                "LEFT OUTER JOIN project_task project_task ON timesheet_details.task_id=project_task.id " +
                                "LEFT OUTER JOIN project_role project_role ON timesheet_details.projectrole_id=project_role.id " +
                                "LEFT OUTER JOIN holiday holidays   ON calendar.caldate=holidays.caldate " +
                                "LEFT OUTER JOIN region h_region   ON holidays.region=h_region.id " +
                                "LEFT OUTER JOIN employee_project_billable epbillable    ON project.id=epbillable.project_id AND empl.id=epbillable.employee_id AND " +
                                "                                                           timesheet.caldate BETWEEN epbillable.start_date AND  epbillable.end_date " +
                                "LEFT OUTER JOIN dictionary_item project_state    ON project.state=project_state.id " +
                                "LEFT OUTER JOIN vacation vacations ON " +
                                "        empl.id=vacations.employee_id AND " +
                                "        timesheet.caldate BETWEEN vacations.begin_date AND vacations.end_date " +
                                "        AND vacations.status_id=:status " +
                                "LEFT OUTER JOIN dictionary_item vacation_type    ON vacation_type.id=vacations.type_id " +
                                "LEFT OUTER JOIN illness illnesses ON " +
                                "        empl.id=illnesses.employee_id AND " +
                                "        timesheet.caldate BETWEEN illnesses.begin_date AND illnesses.end_date " +
                                "%s " +
                        "WHERE " +
                                "timesheet_details.duration > 0 AND " +
                                " %s %s %s %s %s " +
                                "calendar.calDate between :beginDate AND :endDate " +
                        "GROUP BY " +
                                "empl.name, " +
                                "division.name, " +
                                "region.name, " +
                                "project_role.name, " +
                                "project.name, " +
                                "project_task.name, " +
                                "project_task.sort_order, " +
                                "project_state.value, " +
                                "epbillable.billable, " +
                                "vacation_type.value, " +
                                "empl.billable, " +
                                "holidays.id, " +
                                "vacations.id," +
                                "illnesses.id " +
                        "ORDER BY " +
                                "empl.name, " +
                                "project_state.value, " +
                                "project.name, " +
                                "project_task.sort_order, " +
                                "day_type ";

    private List getResultList( Report02 report ) {
        boolean hasProject = report.getProjectId() != null && report.getProjectId() != 0;
        boolean hasDiv = report.getDivisionOwnerId() != null && report.getDivisionOwnerId() != 0;
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0;
        boolean withEmployeeClause = report.getEmployeeId()     != null && report.getEmployeeId    () != 0;

        Query query;
        if (hasProject) {
            // Выборка по конкретному проекту
            query = entityManager.createNativeQuery( String.format( report02QueryString,
                    !report.getShowInactiveProjects()? HIDE_INACTIVE_PROJECTS_CLAUSE :WITHOUT_CLAUSE,
                    "",
                    PROJECT_SQL_CLAUSE,
                    withEmployeeClause ? EMPLOYEE_SQL_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_SQL_CLAUSE   : WITHOUT_CLAUSE,
                    !report.getShowNonBillable()? BILLABLE_CLAUSE :WITHOUT_CLAUSE
            )).setParameter("projectId", report.getProjectId());
        } else if ( hasDiv ) {
            // Выборка по всем проектам центра
            query = entityManager.createNativeQuery( String.format( report02QueryString,
                    !report.getShowInactiveProjects()? HIDE_INACTIVE_PROJECTS_CLAUSE :WITHOUT_CLAUSE,
                    "LEFT OUTER JOIN division_project division_project ON division_project.project_id=project.id ",
                    "division_project.division_id=:divisionId AND ",
                    withEmployeeClause ? EMPLOYEE_SQL_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_SQL_CLAUSE   : WITHOUT_CLAUSE,
                    !report.getShowNonBillable()? BILLABLE_CLAUSE :WITHOUT_CLAUSE
            ) ).setParameter( "divisionId", report.getDivisionOwnerId() );
        } else {
            // Выборка по всем проектам всех центров
            query = entityManager.createNativeQuery( String.format( report02QueryString,
                    !report.getShowInactiveProjects()? HIDE_INACTIVE_PROJECTS_CLAUSE :WITHOUT_CLAUSE,
                    "",
                    "",
                    withEmployeeClause ? EMPLOYEE_SQL_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_SQL_CLAUSE   : WITHOUT_CLAUSE,
                    !report.getShowNonBillable()? BILLABLE_CLAUSE :WITHOUT_CLAUSE
            ) );
        }
        if ( withRegionClause )
            query.setParameter("regionIds", report.getRegionIds());
        if ( withEmployeeClause )
            query.setParameter("emplId", report.getEmployeeId());
        if ( withDivisionClause )
            query.setParameter("emplDivisionId", report.getEmplDivisionId());

        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));
        query.setParameter("status", APPROVED.getId());

        return query.getResultList();
    }

    @Language("SQL")
    private static final String report03QueryString =
            "SELECT " +
                    "empl.name as col_0, " +
                    "division.name as col_1, " +
                    "project.name as col_2, " +
                    "CASE" +
                    "   WHEN (project_task.name is not null) " +
                    "       THEN project_task.name " +
                    "   ELSE " +
                    "       'Задача не указана' " +
                    "END as col_3, " +
                    "calendar.caldate as col_4, " +
                    "sum(timesheet_details.duration) as col_5, " +
                    "CASE" +
                    "   WHEN (holidays.id is not null) " +
                    "       THEN 1 " +
                    "   ELSE " +
                    "       CASE " +
                    "           WHEN (vacations.id is not null) " +
                    "               THEN 2 " +
                    "           ELSE " +
                    "               CASE " +
                    "                   WHEN (trip.id is not null) " +
                    "                       THEN 5 " +
                    "                   ELSE " +
                    "                       CASE " +
                    "                           WHEN (illnesses.id is not null) " +
                    "                               THEN  " +
                    "                                   CASE " +
                    "                                       WHEN (illnesses.reason_id=:reasonable_illness) " +
                    "                                           THEN 3 " +
                    "                                   ELSE 4 " +
                    "                                   END " +
                    "                           ELSE 0 " +
                    "                       END " +
                    "               END " +
                    "       END " +
                    "END as col_6, " +
                    "h_region.id as col_7, " +
                    "region.name as col_8, " +
                    "project_role.name as col_9, " +
                    "project_state.value as col_10, " +
                    "CASE " +
                    "    WHEN (epbillable.billable is not null) THEN epbillable.billable " +
                    "    ELSE empl.billable " +
                    "END as col_11, " +
                    "vacation_type.value AS col_12 " +
            "FROM time_sheet_detail AS timesheet_details " +
                    "INNER JOIN time_sheet timesheet ON timesheet_details.time_sheet_id=timesheet.id " +
                    "INNER JOIN employee empl    ON timesheet.emp_id=empl.id " +
                    "INNER JOIN calendar calendar  ON timesheet.caldate=calendar.caldate " +
                    "INNER JOIN division division    ON empl.division=division.id " +
                    "INNER JOIN project project    ON timesheet_details.proj_id=project.id " + "%s " +
                    "INNER JOIN region region        ON empl.region=region.id " +
                    "LEFT OUTER JOIN project_task project_task ON timesheet_details.task_id=project_task.id " +
                    "LEFT OUTER JOIN project_role project_role ON timesheet_details.projectrole_id=project_role.id " +
                    "LEFT OUTER JOIN holiday holidays   ON calendar.caldate=holidays.caldate " +
                    "LEFT OUTER JOIN region h_region   ON holidays.region=h_region.id " +
                    "LEFT OUTER JOIN employee_project_billable epbillable    ON project.id=epbillable.project_id and empl.id=epbillable.employee_id AND " +
                    "                                                           timesheet.caldate BETWEEN epbillable.start_date AND  epbillable.end_date " +
                    "LEFT OUTER JOIN dictionary_item project_state    ON project.state=project_state.id " +
                    "LEFT OUTER JOIN vacation vacations ON " +
                    "        empl.id=vacations.employee_id AND " +
                    "        timesheet.caldate BETWEEN vacations.begin_date AND vacations.end_date " +
                    "        AND vacations.status_id=:status " +
                    "LEFT OUTER JOIN dictionary_item vacation_type    ON vacation_type.id=vacations.type_id " +
                    "LEFT OUTER JOIN illness illnesses ON " +
                    "        empl.id=illnesses.employee_id AND " +
                    "        timesheet.caldate BETWEEN illnesses.begin_date AND illnesses.end_date " +
                    "LEFT OUTER JOIN business_trip trip ON " +
                    "        empl.id=trip.employee_id AND " +
                    "        timesheet.caldate BETWEEN trip.begin_date AND trip.end_date " +
                    "%s " +
            "WHERE " +
                    "timesheet_details.duration > 0 AND " +
                    " %s %s %s %s %s " +
                    "calendar.caldate between :beginDate AND :endDate " +
            "GROUP BY " +
                    "empl.name, " +
                    "division.name, " +
                    "project.name, " +
                    "project_task.name, " +
                    "calendar.caldate, " +
                    "holidays.id, " +
                    "vacations.id, " +
                    "illnesses.id, " +
                    "trip.id, " +
                    "h_region.id, " +
                    "region.id, " +
                    "region.name, " +
                    "epbillable.billable, " +
                    "vacation_type.value, " +
                    "empl.billable, " +
                    "project_role.name, " +
                    "project_state.value " +
            "ORDER BY empl.name, project.name, col_3, calendar.caldate ";

    private List getResultList( Report03 report ) {
        boolean hasProject = report.getProjectId() != null && report.getProjectId() != 0;
        boolean hasDiv = report.getDivisionOwnerId() != null && report.getDivisionOwnerId() != 0;
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0;
        boolean withEmployeeClause = report.getEmployeeId()     != null && report.getEmployeeId    () != 0;

        Query query;

        if ( hasProject ) {
            // Выборка по конкретному проекту
            query = entityManager.createNativeQuery(
                    String.format( report03QueryString,
                            !report.getShowInactiveProjects()? HIDE_INACTIVE_PROJECTS_CLAUSE :WITHOUT_CLAUSE,
                            "",
                            PROJECT_SQL_CLAUSE,
                            withEmployeeClause ? EMPLOYEE_SQL_CLAUSE : WITHOUT_CLAUSE,
                            withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE,
                            withRegionClause   ? REGION_SQL_CLAUSE   : WITHOUT_CLAUSE,
                            !report.getShowNonBillable()? BILLABLE_CLAUSE :WITHOUT_CLAUSE
            ) );
			query.setParameter("projectId", report.getProjectId());
        } else if ( hasDiv ) {
            // Выборка по всем проектам центра
            query = entityManager.createNativeQuery(
                    String.format( report03QueryString,
                            !report.getShowInactiveProjects()? HIDE_INACTIVE_PROJECTS_CLAUSE :WITHOUT_CLAUSE,
                            "LEFT OUTER JOIN division_project division_project ON division_project.project_id=project.id ",
                            "division_project.division_id=:divisionId AND ",
                            withEmployeeClause ? EMPLOYEE_SQL_CLAUSE : WITHOUT_CLAUSE,
                            withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE,
                            withRegionClause   ? REGION_SQL_CLAUSE   : WITHOUT_CLAUSE,
                            !report.getShowNonBillable()? BILLABLE_CLAUSE :WITHOUT_CLAUSE
            ) );
            query.setParameter( "divisionId", report.getDivisionOwnerId() );
        } else {
            // Выборка по всем проектам всех центров
            query = entityManager.createNativeQuery(
                    String.format( report03QueryString,
                            !report.getShowInactiveProjects()? HIDE_INACTIVE_PROJECTS_CLAUSE :WITHOUT_CLAUSE,
                            "",
                            "",
                            withEmployeeClause ? EMPLOYEE_SQL_CLAUSE : WITHOUT_CLAUSE,
                            withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE,
                            withRegionClause   ? REGION_SQL_CLAUSE   : WITHOUT_CLAUSE,
                            !report.getShowNonBillable()? BILLABLE_CLAUSE :WITHOUT_CLAUSE
            ) );
        }

        if ( withRegionClause )
            query.setParameter("regionIds", report.getRegionIds());
        if (withEmployeeClause)
            query.setParameter("emplId", report.getEmployeeId());
        if (withDivisionClause)
            query.setParameter("emplDivisionId", report.getEmplDivisionId());

        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));
        query.setParameter("status", APPROVED.getId());
        query.setParameter("reasonable_illness", ILLNESS.getId());

        return query.getResultList();
    }

    private List getResultList( Report04 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getDivisionOwnerId() != null && report.getDivisionOwnerId() != 0;

        // К сожалению HQL не может ворочить сложные запросы, прищлось писать native sql-запрос
        Query query = entityManager.createNativeQuery(
            "SELECT " +
                    "        calendar.caldate AS col_0, " +
                    "        employee.name AS col_1, " +
                    "        region.name AS col_2, " +
                    "        project_role.name as col_3 " +
                    "FROM " +
                    "        calendar calendar  " +
                    "        CROSS JOIN employee employee  " +
                    "        INNER JOIN division division ON employee.division=division.id " +
                    "        INNER JOIN project_role project_role ON employee.job=project_role.id " +
                    "        LEFT JOIN illness illnesses ON  " +
                    "                employee.id=illnesses.employee_id AND  " +
                    "                (calendar.caldate BETWEEN illnesses.begin_date AND illnesses.end_date) AND  " +
                    "                (illnesses.begin_date <= :endDate AND illnesses.end_date >= :beginDate) " +
                    "        LEFT JOIN vacation vacations ON  " +
                    "                employee.id=vacations.employee_id AND  " +
                    "                (calendar.caldate BETWEEN vacations.begin_date AND vacations.end_date) AND  " +
                    "                (vacations.begin_date <= :endDate AND vacations.end_date >= :beginDate) AND  " +
                    "                (vacations.status_id = :statusId), " +
                    "        region region  " +
                    "WHERE  " +
                             ( withDivisionClause ? "        division.id=:emplDivisionId AND  " : "") +
                             ( withRegionClause   ? "        (employee.region IN (:regionIds)) AND  " : "" ) +
                    "        illnesses.id IS NULL AND " +
                    "        vacations.id IS NULL AND " +
                    "        employee.region=region.id AND  " +
                    "        (calendar.caldate BETWEEN :beginDate AND  " +
                    "                CASE WHEN employee.end_date IS NOT NULL THEN  " +
                    "                        CASE WHEN employee.end_date<:endDate THEN employee.end_date ELSE :endDate END  " +
                    "                ELSE :endDate end) AND " +
                    "        employee.not_to_sync=FALSE AND  " +
                    "        (employee.manager IS NOT NULL) AND  " +
                    "        (calendar.caldate NOT IN  " +
                    "                (SELECT timesheet.caldate FROM time_sheet timesheet WHERE timesheet.emp_id=employee.id)) AND  " +
                    "        (calendar.caldate NOT IN   " +
                    "                (SELECT holiday.caldate FROM holiday holiday WHERE holiday.region IS NULL OR holiday.region=employee.region)) AND  " +
                    "        employee.start_date<=calendar.caldate  " +
                    "ORDER BY " +
                    "        employee.name,  " +
                    "        calendar.caldate, " +
                    "        project_role.name "
        );

        if (withRegionClause) {
            query.setParameter("regionIds", report.getRegionIds());
		}
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionOwnerId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));
        query.setParameter("statusId", APPROVED.getId());

        return query.getResultList();
    }

    private List getResultList( Report05 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getDivisionOwnerId() != null && report.getDivisionOwnerId() != 0;
        boolean withEmployeeClause = report.getEmployeeId() != null && report.getEmployeeId    () != 0;

        Query query = entityManager.createNativeQuery(
                "select " +
                        "calendar.calDate as col_0, " +
                        "empl.name as col_1, " +
                        "act_type.value as col_2, " +
                        "project.name as col_3, " +
                        "act_cat.value as col_4, " +
                        "job.name as col_5, " +
                        "COALESCE(project_task.name, '') as col_6, " +
                        "timesheet_details.duration as col_7, " +
                        "timesheet_details.description as col_8, " +
                        "timesheet_details.problem as col_9, " +
                        "region.name as col_10, " +
                        "workplace.value as col_11," +
                        "project_role.name as col_12," +
                        "CASE" +
                        "   WHEN (holidays.id is not null) " +
                        "       THEN 1 " +
                        "   ELSE 0 " +
                        "END as col_13, " +
                        "CASE " +
                        "    WHEN (epbillable.billable is not null) THEN epbillable.billable " +
                        "    ELSE empl.billable " +
                        "END as col_14, " +
                        "epbillable.comment as col_15, " +
                        "timesheet.plan as col_16 " +
                "FROM time_sheet_detail AS timesheet_details " +
                        "INNER JOIN time_sheet timesheet ON timesheet_details.time_sheet_id=timesheet.id " +
                        "INNER JOIN employee empl    ON timesheet.emp_id=empl.id " +
                        "INNER JOIN calendar calendar  ON timesheet.caldate=calendar.caldate " +
                        "INNER JOIN project project    ON timesheet_details.proj_id=project.id " +
                        "INNER JOIN region region        ON empl.region=region.id " +
                        "INNER JOIN project_role project_role        ON timesheet_details.projectrole_id=project_role.id " +
                        "INNER JOIN project_role job        ON empl.job=job.id " +
                        "INNER JOIN division division    ON empl.division=division.id " +
                        "LEFT OUTER JOIN project_task project_task ON timesheet_details.task_id=project_task.id " +
                        "LEFT OUTER JOIN dictionary_item act_type    ON timesheet_details.act_type=act_type.id " +
                        "LEFT OUTER JOIN dictionary_item act_cat    ON timesheet_details.act_cat=act_cat.id " +
                        "LEFT OUTER JOIN dictionary_item workplace    ON timesheet_details.workplace_id=workplace.id " +
                        "LEFT OUTER JOIN holiday holidays   ON calendar.caldate=holidays.caldate " +
                        "LEFT OUTER JOIN employee_project_billable epbillable    ON project.id=epbillable.project_id and empl.id=epbillable.employee_id AND " +
                        "                                                           timesheet.caldate BETWEEN epbillable.start_date AND  epbillable.end_date " +
                "where " +
                        (withDivisionClause ? DIVISION_SQL_CLAUSE : WITHOUT_CLAUSE) +
                        (withRegionClause ? REGION_SQL_CLAUSE : WITHOUT_CLAUSE) +
                        (withEmployeeClause ? EMPLOYEE_SQL_CLAUSE : WITHOUT_CLAUSE) +
                        "calendar.calDate between :beginDate and :endDate " +
                "order by calendar.calDate, empl.name ");

        if (withRegionClause) {
			query.setParameter("regionIds", report.getRegionIds());
		}
        if (withEmployeeClause) {
            query.setParameter("emplId", report.getEmployeeId());
        }
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionOwnerId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }

    @VisibleForTesting
    List getResultList(Report06 report) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDatesClause   = !report.isAllDates();

        boolean withProjectClause = !report.getProjectId().equals(0);

        Query query = entityManager.createQuery(
                "SELECT " +
                        "sum(tsd.duration), " +
                        "tsd.projectRole.name, " +
                        "tsd.timeSheet.employee.name, " +
                        "tsd.actCat.value, " +
                        "r.name, " +
                        "tsd.timeSheet.employee.job.name, " +
                        "tsd.actType.value " +
                        (",min(tsd.timeSheet.calDate.calDate), max(tsd.timeSheet.calDate.calDate) ") +
                "FROM " +
                        "TimeSheetDetail tsd, " +
                        "AvailableActivityCategory act " +
                        "join tsd.timeSheet.employee empl " +
                        "join empl.region r " +
                "WHERE " +
                        "tsd.actType=act.actType AND " +
                        "tsd.actCat=act.actCat AND " +
                        (withRegionClause ? REGION_CLAUSE : WITHOUT_CLAUSE) +
                        (withProjectClause ? PROJECT_CLAUSE : WITHOUT_CLAUSE) +
                        (withDatesClause?"tsd.timeSheet.calDate.calDate between :beginDate AND :endDate AND ":WITHOUT_CLAUSE) +
                        "act.projectRole=tsd.projectRole " +
                "GROUP BY " +
                        "tsd.projectRole.name, " +
                        "act.projectRole.name, " +
                        "tsd.timeSheet.employee.name, " +
                        "tsd.actCat.value, " +
                        "r.name, " +
                        "tsd.timeSheet.employee.job.name, " +
                        "tsd.actType.value " +
                "ORDER BY tsd.timeSheet.employee.name asc");
        //нужен только для случая если период не указан
        Query datesQuery = null;
        if (!withDatesClause) {
            datesQuery = entityManager.createQuery(
                    "SELECT " +
                            "new map(min(tsd.timeSheet.calDate.calDate) as minDate, " +
                            "max(tsd.timeSheet.calDate.calDate) as maxDate) " +
                    "FROM " +
                            "TimeSheetDetail tsd " +
                            "join tsd.timeSheet.employee empl " +
                            "join empl.region r " +
                    "WHERE " +
                            (withRegionClause ? REGION_CLAUSE : WITHOUT_CLAUSE) +
                            (withProjectClause ? PROJECT_CLAUSE : WITHOUT_CLAUSE) +
                            (withDatesClause?"tsd.timeSheet.calDate.calDate between :beginDate AND :endDate AND":WITHOUT_CLAUSE)+
                            "true is true ");
        }

        if (withRegionClause){
            query.setParameter("regionIds", report.getRegionIds());
            if (!withDatesClause) {
                datesQuery.setParameter("regionIds", report.getRegionIds());
            }
        }
        if (withProjectClause){
            query.setParameter("projectId", report.getProjectId());
            if (!withDatesClause) {
                datesQuery.setParameter("projectId", report.getProjectId());
            }
        }
        if (withDatesClause) {
            query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
            query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));
        }
        if (!withDatesClause) {
            Map<String, Timestamp> dates= (Map<String, Timestamp>) datesQuery.getSingleResult();
            if(dates!=null){
                report.setBeginDate(DateTimeUtil.formatDate(dates.get("minDate")));
                report.setEndDate(DateTimeUtil.formatDate(dates.get("maxDate")));
            }
        }

        return query.getResultList();
    }

    public HibernateQueryResultDataSource getReport07Data(Report07 report) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            final Report07PeriodTypeEnum periodType =
                    Report07PeriodTypeEnum.getByMonthsCount(report.getPeriodType());

            ArrayList rolesDev = this.readRolesFromString(propertyProvider.getProjectRoleDeveloper());
            ArrayList rolesRp = this.readRolesFromString(propertyProvider.getProjectRoleRp());
            ArrayList rolesTest = this.readRolesFromString(propertyProvider.getProjectRoleTest());
            ArrayList rolesAnalyst = this.readRolesFromString(propertyProvider.getProjectRoleAnalyst());
            ArrayList rolesSystem = this.readRolesFromString(propertyProvider.getProjectRoleSystem());
            Date start = sdf.parse(report.getBeginDate());
            Date end = sdf.parse(report.getEndDate());
            HashMap<String, Object> itogo = new HashMap<String, Object>();
            Query query1;
            if (report.getDivisionOwner() != 0) {
                query1 = this.getReport07Query1(start, end, report.getDivisionEmployee(), report.getDivisionOwner());
            } else {
                query1 = this.getReport07Query1(start, end, report.getDivisionEmployee());
            }
            List dataSource = new ArrayList();
            Double temp = null;
            HashMap<String, HashMap<String, Double>> periodsDuration = new HashMap<String, HashMap<String, Double>>();
            HashMap<String, Double> durations = new HashMap<String, Double>();
            Report7Period itogoPeriod = new Report7Period("Итого");
            for (Object o : query1.getResultList()) {
                Object[] projects = (Object[]) o;
                Integer projectId = (Integer) projects[0];
                Integer projectDivision = (Integer) projects[2];
                Date periodStart = start;
                String projectName = null;
                Integer periodNumber = null;
                Date periodEnd = periodStart;
                Double periodByRP = 0D;
                Double periodByAnalyst = 0D;
                Double periodByDev = 0D;
                Double periodBySystem = 0D;
                Double periodByTest = 0D;
                Double periodByCenterOwner = 0D;
                Double periodByCenterEtc = 0D;
                HashMap<String, Double> periodRegions = new HashMap<String, Double>();
                for (periodNumber = 1; periodEnd.before(end); periodNumber = periodNumber + 1) {
                    final Date maxEndOfPeriod = getMaxEndOfPeriod(end, periodStart, periodType);

                    periodEnd = getEndOfPeriod(periodType, periodEnd, maxEndOfPeriod);

                    Report7Period period = new Report7Period(periodNumber, periodStart, end, periodType);
                    Query query2 = this.getReport07Query2(periodStart, periodEnd, projectId, report.getDivisionEmployee());
                    Double durationByRP = 0D;
                    Double durationByAnalyst = 0D;
                    Double durationByDev = 0D;
                    Double durationBySystem = 0D;
                    Double durationByTest = 0D;
                    HashMap<String, Double> regions = new HashMap<String, Double>();
                    Double durationByCenterOwner = 0D;
                    Double durationByCenterEtc = 0D;
                    Double durationPeriod = 0D;
                    for (Object o1 : query2.getResultList()) {
                        Object[] values = (Object[]) o1;
                        String projectRegion = (String) values[5];
                        Double pDuration = (Double) values[3];
                        Integer job = (Integer) values[2];
                        projectName = String.valueOf(values[4]);
                        Integer projectEmpDivision = (Integer) values[1];

                        if (rolesRp.indexOf(job) != -1) {
                            durationByRP = durationByRP + pDuration;
                        }
                        if (rolesAnalyst.indexOf(job) != -1) {
                            durationByAnalyst = durationByAnalyst + pDuration;
                        }
                        if (rolesDev.indexOf(job) != -1) {
                            durationByDev = durationByDev + pDuration;
                        }
                        if (rolesSystem.indexOf(job) != -1) {
                            durationBySystem = durationBySystem + pDuration;
                        }
                        if (rolesTest.indexOf(job) != -1) {
                            durationByTest = durationByTest + pDuration;
                        }
                        durationPeriod += pDuration;

                        if (regions.get(projectRegion) == null) {
                            regions.put(projectRegion, pDuration);
                        } else {
                            regions.put(projectRegion, regions.get(projectRegion) + pDuration);
                        }
                        if (durations.get(projectName) == null) {
                            durations.put(projectName, pDuration);
                        } else {
                            durations.put(projectName, pDuration + durations.get(projectName));
                        }

                        // Подсчёт относительных затрат
                        if (projectDivision.equals(projectEmpDivision)) {
                            durationByCenterOwner = durationByCenterOwner + pDuration;
                        } else {
                            durationByCenterEtc = durationByCenterEtc + pDuration;
                        }
                    }

                    if (projectName != null) {
                        // по должностям
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Руководитель проекта, ч. (%)",
                                this.report7GenerateValue(durationByRP, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Аналитик, ч. (%)",
                                this.report7GenerateValue(durationByAnalyst, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Разработчик, ч. (%)",
                                this.report7GenerateValue(durationByDev, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Системный инженер, ч. (%)",
                                this.report7GenerateValue(durationBySystem, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Тестирование, ч. (%)",
                                this.report7GenerateValue(durationByTest, durationPeriod)));

                        // Подсчитаем к итоговому периоду
                        periodByAnalyst += durationByAnalyst;
                        periodByRP += durationByRP;
                        periodByDev += durationByDev;
                        periodBySystem += durationBySystem;
                        periodByTest += durationByTest;

                        // Подсчёт по регионам
                        for (Map.Entry<String, Double> region : regions.entrySet()) {
                            final String regionHoursAndPercents = region.getKey().concat(HOURS_WITH_PERCENTS);

                            dataSource.add(this.report7DataSourceRow(period, projectName, "По Регионам", (String) regionHoursAndPercents,
                                    this.report7GenerateValue(region.getValue(), durationPeriod)));
                            // Посчитаем для итого
                            if (periodRegions.get(regionHoursAndPercents) == null) {
                                periodRegions.put(regionHoursAndPercents, region.getValue());
                            } else {
                                periodRegions.put(regionHoursAndPercents, region.getValue() + periodRegions.get(regionHoursAndPercents));
                            }
                        }
                        if (durationPeriod > 0) {
                            dataSource.add(this.report7DataSourceRow(period, projectName, "Трудозатраты", "Общие, ч.", doubleFormat.format(durationPeriod)));
                        }

                        if (periodsDuration.get(period.getNumber().toString()) == null) {
                            HashMap<String, Double> value = new HashMap<String, Double>();
                            value.put(projectName, durationPeriod);
                            periodsDuration.put(period.getNumber().toString(), value);
                        } else {
                            periodsDuration.get(period.getNumber().toString()).put(projectName, durationPeriod);
                        }

                        // Относительные затраты по центрам
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Центрам", "Центр владельца проекта, ч. (%)",
                                this.report7GenerateValue(durationByCenterOwner, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Центрам", "Другие центры, ч. (%)",
                                this.report7GenerateValue(durationByCenterEtc, durationPeriod)));
                        periodByCenterEtc += durationByCenterEtc;
                        periodByCenterOwner += durationByCenterOwner;
                        regions.clear();
                    }

                    //durations.put(projectName, Double.valueOf(0));
                    if (periodEnd.equals(maxEndOfPeriod)) {
                        periodEnd = DateUtils.addDays(periodEnd, 1);
                    }

                    periodStart = periodEnd;    // Обязательно
                }
                // Вывод итого в dataSource
                if (projectName != null) {
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Руководитель проекта, ч. (%)",
                            this.report7GenerateValue(periodByRP, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Аналитик, ч. (%)",
                            this.report7GenerateValue(periodByAnalyst, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Разработчик, ч. (%)",
                            this.report7GenerateValue(periodByDev, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Системный инженер, ч. (%)",
                            this.report7GenerateValue(periodBySystem, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Тестирование, ч. (%)",
                            this.report7GenerateValue(periodByTest, durations.get(projectName))));

                    // Относительные затраты по центрам
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Центрам", "Центр владельца проекта, ч. (%)",
                            this.report7GenerateValue(periodByCenterOwner, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Центрам", "Другие центры, ч. (%)",
                            this.report7GenerateValue(periodByCenterEtc, durations.get(projectName))));

                    // Трудозатраты
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "Трудозатраты", "Общие, ч.", doubleFormat.format(durations.get(projectName))));

                    // По регионам
                    for (Map.Entry<String, Double> region : periodRegions.entrySet()) {
                        dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Регионам", (String) region.getKey(),
                                this.report7GenerateValue(region.getValue(), durations.get(projectName))));
                    }
                }
            }
            for (Map.Entry<String, HashMap<String, Double>> period : periodsDuration.entrySet()) {
                Double sum = 0D;
                for (Map.Entry<String, Double> project : period.getValue().entrySet()) {
                    sum = sum + project.getValue();
                }
                for (Map.Entry<String, Double> project : period.getValue().entrySet()) {
                    if (sum > 0 && project.getValue() > 0) {
                        temp = project.getValue() / sum * 100;
                        dataSource.add(this.report7DataSourceRow(itogoPeriod, project.getKey(), "Трудозатраты", "Относительные, %",
                                doubleFormat.format(temp).concat("%")));
                    }
                }
            }

            Double sum = 0D;
            for (Map.Entry<String, Double> period : durations.entrySet()) {
                sum += period.getValue();
            }

            for (Map.Entry<String, Double> period : durations.entrySet()) {
                temp = period.getValue() / sum * 100;
                dataSource.add(this.report7DataSourceRow(itogoPeriod, period.getKey(), "Трудозатраты", "Относительные, %",
                        doubleFormat.format(temp).concat("%")));
            }
            String[] fields = {"period", "name", "group", "type", "value"};
            return new HibernateQueryResultDataSource(dataSource, fields);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Date getEndOfPeriod(Report07PeriodTypeEnum periodType, Date periodEnd, Date maxEndOfPeriod) {
        final Date endOfPeriod = DateUtils.addMonths(periodEnd, periodType.getMonthsCount());

        return (Date) ObjectUtils.min(endOfPeriod, maxEndOfPeriod);
    }

    private Date getMaxEndOfPeriod(Date end, Date periodStart, Report07PeriodTypeEnum periodType) {
        final Calendar calendar = DateUtils.toCalendar(periodStart);

        return (Date) ObjectUtils.min(periodType.getMaxDateOfPartOfYear(calendar), end);
    }

    private String report7GenerateValue(Double projectDuration, Double periodDuration) {
        if (projectDuration == null || projectDuration.isNaN())
            projectDuration = 0D;
        if (projectDuration == null || projectDuration.isNaN())
            periodDuration = 0D;
        Double result;
        if (periodDuration > 0) {
            result = projectDuration / periodDuration * 100;
        } else {
            result = 0D;
        }
        return doubleFormat.format(projectDuration) + " (" + doubleFormat.format(result) + "%)";
    }

    private Object[] report7DataSourceRow(Report7Period period, String name, String group, String type, String value) {
        ArrayList list = new ArrayList();
        list.add(period);
        list.add(name);
        list.add(group);
        list.add(type);
        if (value.equals("0 (0%)")) {
            list.add("-");
        } else {
            list.add(value);
        }
        return list.toArray();
    }

    private ArrayList readRolesFromString(String s) {
        String[] roles = s.split(",");
        ArrayList role = new ArrayList();
        for (String role1 : roles) {
            role.add(Integer.parseInt(role1));
        }
        return role;
    }

    private Query getReport07Query1(Date periodStart, Date periodEnd, Integer divisionEmployeeId) {
        Query query;
        if (divisionEmployeeId != 0) {
            query = entityManager.createQuery("SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                    "LEFT JOIN project.timeSheetDetail tsd " +
                    "JOIN tsd.timeSheet ts " +
                    "JOIN ts.employee emp " +
                    "JOIN project.manager as manager " +
                    "JOIN manager.division as md " +
                    "WHERE emp.division.id = :divisionEmployeeId " +
                    "AND ts.calDate.calDate between :beginDate AND :endDate " +
                    "GROUP BY 1, 3 " +
                    "ORDER BY 2 DESC ");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
            query.setParameter("divisionEmployeeId", divisionEmployeeId);
        } else {
            query = entityManager.createQuery("SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                    "LEFT JOIN project.timeSheetDetail tsd " +
                    "JOIN  tsd.timeSheet ts " +
                    "JOIN ts.employee emp " +
                    "JOIN project.manager as manager " +
                    "JOIN manager.division as md " +
                    "WHERE ts.calDate.calDate between :beginDate AND :endDate " +
                    "GROUP BY 1, 3 " +
                    "ORDER BY 2 DESC ");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
        }
        return query;
    }

    private Query getReport07Query1(Date periodStart, Date periodEnd, Integer divisionEmployeeId, Integer divisionOwnerId) {
        Query query;
        if (divisionEmployeeId != 0) {
            query = entityManager.createQuery(
                    "SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                            "LEFT JOIN project.timeSheetDetail tsd " +
                            "JOIN project.divisions division " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee emp " +
                            "JOIN project.manager as manager " +
                            "JOIN manager.division as md " +
                            "WHERE emp.division.id = :divisionEmployeeId AND division.id = :divisionOwnerId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3 " +
                            "ORDER BY 2 DESC");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
            query.setParameter("divisionEmployeeId", divisionEmployeeId);
            query.setParameter("divisionOwnerId", divisionOwnerId);
        } else {
            query = entityManager.createQuery(
                    "SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                            "LEFT JOIN project.timeSheetDetail tsd " +
                            "JOIN project.divisions division " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee emp " +
                            "JOIN project.manager as manager " +
                            "JOIN manager.division as md " +
                            "WHERE division.id = :divisionOwnerId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3 " +
                            "ORDER BY 2 DESC");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
            query.setParameter("divisionOwnerId", divisionOwnerId);
        }
        return query;
    }

    private Query getReport07Query2(Date periodStart, Date periodEnd, Integer projectId, Integer divisionEmployeeId) {
        Query query;
        if (divisionEmployeeId == 0) {
            query = entityManager.createQuery(
                    "SELECT emp.region.id as region, emp.division.id as division, emp.job.id as job, SUM(tsd.duration) as duration, p.name, emp.region.name " +
                            "FROM Project p " +
                            "LEFT JOIN p.timeSheetDetail tsd " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee as emp " +
                            "WHERE p.id = :projectId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3, 2, 5, 6"
            );
            query.setParameter("projectId", projectId);
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
        } else {
            query = entityManager.createQuery(
                    "SELECT emp.region.id as region, emp.division.id as division, emp.job.id as job, SUM(tsd.duration) as duration, p.name, emp.region.name " +
                            "FROM Project p " +
                            "LEFT JOIN p.timeSheetDetail tsd " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee as emp " +
                            "WHERE p.id = :projectId AND emp.division.id = :divisionEmployeeId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3, 2, 5, 6"
            );
            query.setParameter("projectId", projectId);
            query.setParameter("divisionEmployeeId", divisionEmployeeId);
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
        }
        return query;
    }
}
