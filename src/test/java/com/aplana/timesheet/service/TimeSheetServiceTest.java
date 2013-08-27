package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.aplana.timesheet.enums.TypesOfActivityEnum.ILLNESS;
import static com.aplana.timesheet.enums.TypesOfActivityEnum.getById;

public class TimeSheetServiceTest extends AbstractJsonTest {

    private static final int EMPLOYEE_ID = 1;
    private static final String DATE = DateFormatUtils.format(new Date(), DateTimeUtil.DATE_PATTERN);
    private static final Random RANDOM = new Random();

    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    AvailableActivityCategoryDAO availableActivityCategoryDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private TimeSheetDAO timeSheetDAO;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProjectService projectService;

    private TimeSheetForm timeSheetForm = new TimeSheetForm();

    @Before
    public void initTimeSheetForm() {
        final int rowsCount = 5;
        final ArrayList<TimeSheetTableRowForm> tableRowForms = new ArrayList<TimeSheetTableRowForm>(rowsCount);
        final List<Project> projects = projectService.getAll();

        for (int i = 0; i < rowsCount; i++) {
            addTimeSheetTableRow(tableRowForms, projects);
        }

        timeSheetForm.setTimeSheetTablePart(tableRowForms);
        timeSheetForm.setCalDate(DATE);
    }

    private void addTimeSheetTableRow(ArrayList<TimeSheetTableRowForm> tableRowForms, List<Project> projects) {
        final TimeSheetTableRowForm timeSheetTableRowForm = new TimeSheetTableRowForm();

        Integer projId = projects.get(tableRowForms.size() % projects.size()).getId();
        timeSheetTableRowForm.setProjectId(projId);
        Project project = projectService.find(projId);
        if (project.isCqRequired()) {
            Set<ProjectTask> projectTasks = project.getProjectTasks();
            Iterator<ProjectTask> iterator = projectTasks.iterator();
            if (iterator.hasNext()) {
                ProjectTask next = iterator.next();
                timeSheetTableRowForm.setProjectTaskId(next.getId());
            }
        }
        timeSheetTableRowForm.setProjectRoleId(RANDOM.nextInt());
        timeSheetTableRowForm.setWorkplaceId(RANDOM.nextInt());
        timeSheetTableRowForm.setActivityCategoryId(RANDOM.nextInt());
    }

    private String getListOfActDescriptoinForTest() {
        List<AvailableActivityCategory> availableActivityCategories = availableActivityCategoryDAO.getAllAvailableActivityCategories();
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (AvailableActivityCategory activityCategory : availableActivityCategories) {
            result.append("{");
            result.append("\"actCat\":" + activityCategory.getActCat().getId() + ",");
            result.append("\"actType\":" + activityCategory.getActType().getId() + ",");
            result.append("\"projectRole\":" + activityCategory.getProjectRole().getId() + ",");
            result.append("\"description\":\"");
            if (activityCategory.getDescription() != null) {
                result.append(activityCategory.getDescription());
            }
            result.append("\"");
            result.append("},");
        }
        result.append("{\"actCat\":0,\"actType\":0,\"projectRole\":0,\"description\":\"\"}");
        result.append("]");
        return result.toString();
    }

    private String getPlansJson(String date, Integer employeeId) {
        StringBuilder json = new StringBuilder();

        TimeSheet lastTimeSheet = timeSheetDAO.findLastTimeSheetBefore(calendarService.find(date), employeeId);
        Calendar nextWorkDay = calendarService.getNextWorkDay(calendarService.find(date), employeeService.find(employeeId).getRegion());
        TimeSheet nextTimeSheet = timeSheetDAO.findNextTimeSheetAfter(nextWorkDay, employeeId);

        json.append("{");
        if (lastTimeSheet != null) {
            json.append("\"prev\":{");
            json.append("\"dateStr\":");
            json.append("\"").append(DateTimeUtil.formatDate(lastTimeSheet.getCalDate().getCalDate()))
                    .append("\",");   //преобразование к  yyyy-MM-dd
            json.append("\"plan\":\"");
            String lastPlan = lastTimeSheet.getPlan();
            if (lastPlan != null)
                json.append("").append(lastPlan.replace("\r\n", "\\n"));
            json.append("\"}");
        }

        if (lastTimeSheet != null && nextTimeSheet != null)
            json.append(",");

        if (nextTimeSheet != null &&
                !(ILLNESS == getById(
                        Lists.newArrayList(
                                nextTimeSheet.getTimeSheetDetails()).get(0).getActType().getId()))) { // <APLANATS-458>
            json.append("\"next\":{")
                    .append("\"dateStr\":").append("\"")
                    .append(DateTimeUtil.formatDate(nextTimeSheet.getCalDate().getCalDate()))
                    .append("\",")   //преобразование к  yyyy-MM-dd
                    .append("\"plan\":\"");
            String nextPlan = timeSheetService.getStringTimeSheetDetails(nextTimeSheet);
            if (nextPlan != null)
                json.append(nextPlan.replace("\r\n", "\\n"));
            json.append("\"}");
        }
        json.append("}");
        return json.toString();

    }

    private String getSelectedProjectsJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{\"row\":\"").append(i).append("\",");
                sb.append("\"project\":\"").append(tablePart.get(i).getProjectId()).append("\"}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(",");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{\"row\":\"0\",\"project\":\"\"}]");
        }
        return sb.toString();
    }

    private String getSelectedProjectRolesJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                if (!"".equals(tablePart.get(i).getProjectTaskId())) {
                    sb.append("{\"row\":\"").append(i).append("\",");
                    sb.append("\"role\":\"").append(tablePart.get(i).getProjectRoleId()).append("\"}");
                    if (i < (tablePart.size() - 1)) {
                        sb.append(",");
                    }
                }
            }
            sb.append("]");
        } else {
            sb.append("[{\"row\":\"0\",\"role\":\"\"}]");
        }
        return sb.toString();
    }

    private String getSelectedProjectTasksJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                if (!"".equals(tablePart.get(i).getProjectTaskId())) {
                    sb.append("{\"row\":\"").append(i).append("\",");
                    sb.append("\"task\":\"").append(tablePart.get(i).getProjectTaskId()).append("\"}");
                    if (i < (tablePart.size() - 1)) {
                        sb.append(",");
                    }
                }
            }
            sb.append("]");
        } else {
            sb.append("[{\"row\":\"0\",\"task\":\"\"}]");
        }
        return sb.toString();
    }

    private String getSelectedWorkplaceJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{\"row\":\"").append(i).append("\",");
                sb.append("\"workplace\":\"").append(tablePart.get(i).getWorkplaceId()).append("\"}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(",");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{\"row\":\"0\",\"workplace\":\"\"}]");
        }
        return sb.toString();
    }

    private String getSelectedActCategoriesJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{\"row\":\"").append(i).append("\",");
                sb.append("\"actCat\":\"").append(tablePart.get(i).getActivityCategoryId()).append("\"}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(",");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{\"row\":\"0\",\"actCat\":\"\"}]");
        }
        return sb.toString();
    }

    @Test
    public void testGetListOfActDescriptoin() {
        String currentResult = timeSheetService.getListOfActDescriptoin();
        String testResult = getListOfActDescriptoinForTest();
        assertJsonEquals(testResult, currentResult);
    }

    @Test
    public void testGetPlansJson() {
        assertJsonEquals(getPlansJson(DATE, EMPLOYEE_ID), timeSheetService.getPlansJson(DATE, EMPLOYEE_ID));
    }

    @Test
    public void testGetSelectedProjectsJson() {
        assertJsonEquals(getSelectedProjectsJson(timeSheetForm), timeSheetService.getSelectedProjectsJson(timeSheetForm));
    }

    @Test
    public void testGetSelectedProjectRolesJson() {
        assertJsonEquals(
                getSelectedProjectRolesJson(timeSheetForm),
                timeSheetService.getSelectedProjectRolesJson(timeSheetForm)
        );
    }

    @Test
    public void testGetSelectedProjectTasksJson() {
        assertJsonEquals(getSelectedProjectTasksJson(timeSheetForm), timeSheetService.getSelectedProjectTasksJson(timeSheetForm));
    }

    @Test
    public void testGetSelectedWorkplaceJson() {
        assertJsonEquals(getSelectedWorkplaceJson(timeSheetForm), timeSheetService.getSelectedWorkplaceJson(timeSheetForm));
    }

    @Test
    public void testGetSelectedActCategoriesJson() {
        assertJsonEquals(
                getSelectedActCategoriesJson(timeSheetForm),
                timeSheetService.getSelectedActCategoriesJson(timeSheetForm)
        );
    }

}
