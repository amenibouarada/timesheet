<%@ page import="com.aplana.timesheet.enums.OvertimeCausesEnum" %>
<%@ page import="com.aplana.timesheet.enums.UndertimeCausesEnum" %>
<%@ page import="com.aplana.timesheet.enums.WorkOnHolidayCausesEnum" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.timesheet"/></title>

    <script type="text/javascript">
        var unfinishedDayCauseList = ${unfinishedDayCauseJson};
        var overtimeCauseList = ${overtimeCauseJson};
        var overtimeThreshold = ${overtimeThreshold};
        var undertimeThreshold = ${undertimeThreshold};
        var workplaceList = ${workplaceJson};
        var actTypeList = ${actTypeJson};
        var projectList = ${projectListJson};
        var actCategoryList = ${actCategoryListJson};
        var availableActCategoryList = ${availableActCategoriesJson};
        var projectRoleList = ${projectRoleListJson};
        var projectTaskList = ${projectTaskListJson};
        var selectedProjects = ${selectedProjectsJson};
        var selectedProjectTasks = ${selectedProjectTasksJson};
        var selectedProjectRoles = ${selectedProjectRolesJson};
        var selectedActCategories = ${selectedActCategoriesJson};
        var selectedWorkplace = ${selectedWorkplaceJson};
        var selectedCalDate = ${selectedCalDateJson};
        var listOfActDescription = ${listOfActDescriptionJson};
        var workOnHolidayCauseList = ${workOnHolidayCauseJson};
        var defaultOvertimeCause = '${timeSheetForm.overtimeCause}';
        var dataDraft = '${data}';
        var isErrorPage = "${isErrorPage}";
        var divIdJsp = "${timeSheetForm.divisionId}" != "" ? +"${timeSheetForm.divisionId}" : null;
        var employeeIdJsp = "${timeSheetForm.employeeId}" != "" ? +"${timeSheetForm.employeeId}" : null;
        var undertimeOtherJsp = +"<%= UndertimeCausesEnum.OTHER.getId() %>";
        var workHolidayOtherJsp = +"<%= WorkOnHolidayCausesEnum.OTHER.getId() %>";
        var overtimeOtherJsp = +"<%= OvertimeCausesEnum.OTHER.getId() %>";
        var employeeList;

        dojo.ready(function () {
            initTimeSheetForm();
        });
    </script>
    <%-- TODO Возможно стоит при сборке складывать в один файл и тогда браузер будет только 1 запрос делать, а не 6 --%>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/timesheet/timesheet.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/timesheet/draft.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/timesheet/jira.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/timesheet/submit.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/timesheet/table.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/timesheet/widgetChanging.js"></script>

    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/timesheetForm.css">
</head>
<body>

<h1><fmt:message key="title.timesheet"/></h1>

<div id="dialogOne" data-dojo-type="dijit.Dialog" title="" style="display: none;">
    <div data-dojo-type="dijit.layout.ContentPane" style="width: 500px; height: 250px;">
        <div id="holidayWarning" style="margin-bottom: 15px;">
            <span style="font-weight: bold; color: red;">
                Обращаем внимание, что работа в выходной день должна быть согласована с руководителем проекта и руководителем центра компетенции
            </span>
        </div>
        <div style="margin-bottom: 3px;">Выберите причину</div>
        <div id="overtimeCause" onChange="overtimeCauseChange(this);requiredCommentSet();"
             data-dojo-type="dijit.form.Select"
             style="width: 99%;" data-dojo-props="value: '${timeSheetForm.overtimeCause}'"></div>
        <div style="margin-top: 10px;"><span>Комментарий</span></div>
        <div data-dojo-type="dijit.form.ValidationTextBox"
             data-dojo-prop="missingMessage:'Комментарий для причины 'Другое' является обязательным!'"
             wrap="soft" id="overtimeCauseComment" rows="10" style="width: 99%;margin-top: 3px;"
             placeHolder="Напишите причину, если нет подходящей в списке"
             tooltip="комментарий">${timeSheetForm.overtimeCauseComment}</div>
        <div id="typeOfCompensationContainer" style="margin-top: 10px;">
            <div style="margin-bottom: 3px;">Тип компенсации</div>
            <select data-dojo-type="dijit.form.Select" style="width: 99%;" id="typeOfCompensation"
                    data-dojo-props="value: '${timeSheetForm.typeOfCompensation}'">
                <option value="0"></option>
                <c:forEach items="${typesOfCompensation}" var="t">
                    <option value="${t.id}">${t.value}</option>
                </c:forEach>
            </select>
        </div>
        <button id="confirmOvertimeCauseButton" style="margin-top: 10px; margin-left: -1px"
                onclick="submitWithOvertimeCauseSet()" onmouseout="tooltip.hide()">
            Продолжить
        </button>
    </div>
</div>

<div id="dialogChangeDate" data-dojo-type="dijit.Dialog" title="" style="display: none;">
    <div data-dojo-type="dijit.layout.ContentPane" style="width: 270px; height: 65px;">
        В отчете имеются несохраненные изменения.<br/>
        Продолжить без сохранения?<br/>
        <button id="confirmDateChange" style="margin-top: 10px; margin-left: 10px; width: 120px;"
                onclick="confirmCalDateChange()">
            Продолжить
        </button>
        <button id="cancelDateChange" style="margin-top: 10px; margin-left: 10px; width: 120px;"
                onclick="cancelCalDateChange()">
            Отмена
        </button>
    </div>
</div>

<form:form method="post" commandName="timeSheetForm" cssClass="noborder">

<%-- Костыль для диалога --%>
<form:hidden path="overtimeCauseComment" id="overtimeCauseComment_hidden"/>
<form:hidden path="overtimeCause" id="overtimeCause_hidden"/>
<form:hidden path="typeOfCompensation" id="typeOfCompensation_hidden"/>

<div id="form_header" style="margin-bottom: 15px;">
    <span class="label">Подразделение</span>
    <form:select path="divisionId" id="divisionId" onchange="divisionChange(this)" class="without_dojo"
                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:option label="" value="0"/>
        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
    </form:select>

    <span class="label">Отчет сотрудника</span>

    <div id='employeeIdSelect' name='employeeIdSelect'></div>
    <form:hidden path="employeeId" id="employeeId"/>

    <span class="label">за дату</span>
    <form:input path="calDate" id="calDate" class="date_picker" data-dojo-type="DateTextBox"
                data-dojo-id="reportDate"
                required="true" onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                onChange="onCalDateChange(this)"/>
    <span id="date_warning"></span>

</div>

<div style="width: 100%;">
    <div style="float:left;width: 450px;">
        <span id="lbPrevPlan">Планы предыдущего рабочего дня:</span>
    </div>
    <div id="load_draft_text" style="float:left;text-align: right; width:425px;color: red;display: none;">
        Имеется черновик не отправленного отчета!
    </div>
    <div id="plan_textarea"
         style="margin: 2px 0px; padding:2px;border: solid 1px silver;float:left;clear: left;width: 450px;"><br/></div>
    <div style="float:left;text-align: right;width: 425px;">
        <button id="load_draft" type="button" style="width:200px;display: none;" onclick="loadDraft()">
            Загрузить черновик
        </button>
    </div>
    <div style="clear: left;">
        <button id="add_in_comments" class="controlToDisable" type="button" style="width:300px" onclick="CopyPlan()">
            Скопировать в первый комментарий
        </button>
    </div>
</div>

<div id="marg_buttons" style="margin-top:15px;">
    <c:if test="${fn:length(errors) > 0}">
        <div id="errors_box" class="errors_box">
            <c:forEach items="${errors}" var="error">
                <fmt:message key="${error.code}">
                    <fmt:param value="${error.arguments[0]}"/>
                </fmt:message><br/>
            </c:forEach>
        </div>
    </c:if>
</div>
<div id="form_table">
    <table id="time_sheet_table">
        <tr id="time_sheet_header">
            <th style="min-width: 30px">
                <a onclick="addNewRow()">
                    <img class="controlToHide" style="cursor: pointer;" src="<c:url value="/resources/img/add.gif"/>"
                         width="15px"
                         title="Добавить строку"/>
                </a>
            </th>
            <th style="min-width: 20px">№</th>
            <th style="min-width: 120px">Тип активности</th>
            <th style="min-width: 100px">Место работы</th>
            <th style="min-width: 200px">Название проекта/пресейла</th>
            <th style="min-width: 130px">Проектная роль</th>
            <th style="width: 170px">Активность</th>
            <th style="min-width: 130px">Задача</th>
            <th style="min-width: 30px">ч.</th>
            <th style="min-width: 240px">Комментарии</th>
            <th style="min-width: 35px">JIRA</th>
            <th style="min-width: 200px">Проблемы</th>
        </tr>


        <c:if test="${isErrorPage}">
            <c:forEach items="${timeSheetForm.timeSheetTablePart}" varStatus="row">
                <tr class="time_sheet_row" id="ts_row_${row.index}">
                    <td class="text_center_align" id="delete_button_id_${row.index}">

                    </td>
                    <td class="text_center_align row_number"><c:out value="${row.index + 1}"/></td>
                    <td class="top_align"> <!-- Тип активности -->
                        <form:select path="timeSheetTablePart[${row.index}].activityTypeId"
                                     id="activity_type_id_${row.index}" onchange="typeActivityChange(this)"
                                     cssClass="activityType" onmouseover="tooltip.show(getTitle(this));"
                                     onmouseout="tooltip.hide();" onkeyup="somethingChanged();"
                                     onmouseup="somethingChanged();">
                            <form:option label="" value="0"/>
                            <form:options items="${actTypeList}" itemLabel="value" itemValue="id"/>
                        </form:select>
                    </td>
                    <td class="top_align"> <!-- Место работы -->
                        <form:select path="timeSheetTablePart[${row.index}].workplaceId"
                                     id="workplace_id_${row.index}"
                                     cssClass="workplaceType" onmouseover="tooltip.show(getTitle(this));"
                                     onmouseout="tooltip.hide();" onkeyup="somethingChanged();"
                                     onmouseup="somethingChanged();">
                            <form:option label="" value="0"/>
                            <form:options items="${workplaceList}" itemLabel="value" itemValue="id"/>
                        </form:select>
                    </td>

                    <td class="top_align"> <!-- Название проекта/пресейла -->
                        <form:select path="timeSheetTablePart[${row.index}].projectId"
                                     id="project_id_${row.index}"
                                     onchange="projectChange(this)" cssClass="project"
                                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();"
                                     onkeyup="somethingChanged();" onmouseup="somethingChanged();">
                            <form:option label="" value="0"/>
                        </form:select>
                    </td>
                    <td class="top_align"> <!-- Проектная роль -->
                        <form:select path="timeSheetTablePart[${row.index}].projectRoleId"
                                     id="project_role_id_${row.index}" onchange="projectRoleChange(this)"
                                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();"
                                     onkeyup="somethingChanged();" onmouseup="somethingChanged();">
                            <form:option label="" value="0"/>
                            <form:options items="${projectRoleList}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    </td>
                    <td class="top_align"> <!-- Категория активности/название работы -->
                        <form:select path="timeSheetTablePart[${row.index}].activityCategoryId"
                                     id="activity_category_id_${row.index}"
                                     onchange="setActDescription(${row.index})"
                                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();"
                                     onkeyup="somethingChanged();" onmouseup="somethingChanged();">
                            <form:option label="" value="0"/>
                        </form:select>
                        <label id="act_description_${row.index}" style="font-style: italic"/>
                    </td>
                    <td class="top_align"> <!-- Проектная задача -->
                        <form:select path="timeSheetTablePart[${row.index}].projectTaskId"
                                     id="projectTask_id_${row.index}"
                                     onchange="setTaskDescription(${row.index})"
                                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();"
                                     onkeyup="somethingChanged();" onmouseup="somethingChanged();">
                            <form:option label="" value="0"/>
                        </form:select>
                        <label id="task_description_${row.index}" style="font-style: italic"/>
                    </td>
                    <td class="top_align"><form:input cssClass="text_right_align duration" type="text"
                                                      path="timeSheetTablePart[${row.index}].duration"
                                                      id="duration_id_${row.index}"
                                                      onchange="checkDuration(this);"
                                                      onkeyup="somethingChanged();"/></td>
                    <td class="top_align"><form:textarea wrap="soft"
                                                         path="timeSheetTablePart[${row.index}].description"
                                                         rows="3" style="width: 100%"
                                                         id="description_id_${row.index}"
                                                         onkeyup="somethingChanged();"/></td>

                    <td class="text_center_align" id="jira_button_id_${row.index}"/>

                    <td class="top_align"><form:textarea wrap="soft" path="timeSheetTablePart[${row.index}].problem"
                                                         rows="3" style="width: 100%" id="problem_id_${row.index}"
                                                         onkeyup="somethingChanged();"/></td>
                </tr>
            </c:forEach>
        </c:if>
        <tr style="height : 20px;" id="total_duration_row">
            <td colspan="7"/>
            <td style="text-align: right">&nbsp;ИТОГО</td>
            <td id="total_duration" class="text_right_align">0</td>
            <td colspan="3"/>
        </tr>
    </table>
</div>

<div id="plan_box" style="margin-bottom: 10px; margin-top: 10px">
    <span id="lbNextPlan" class="label">Планы на следующий рабочий день:</span>

    <div id="box_margin" style="margin-top :6px; margin-bottom: 8px;">
        <div id='box_textArea' style="border: #AAA solid 1px;width: 685px;">

            <form:textarea wrap="soft" path="plan" id="plan" rows="7"
                           cssStyle="border: none; outline: none;overflow:auto;width: 97%;"/>
            <img id="jira_get_plans_button" src="resources/img/logo-jira.png"
                 alt="Запрос из JIRA" title="Запрос из JIRA" height="15" width="15"
                 style="cursor: pointer; visibility: visible; position: absolute; margin-top: 4px; margin-left: 3px;">
        </div>
        <br/>
        <script>
            dojo.ready(function () {
                if (dojo.isIE <= 8) {
                    dojo.setStyle('box_textArea', 'width', '777px');
                }
            });
        </script>
    </div>
</div>
<div id="effort_box">
    <span class="label">Оценка объема работ на следующий рабочий день:</span>
    <form:select path="effortInNextDay" id="effortInNextDay" class="without_dojo"
                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:options items="${effortList}" itemLabel="value" itemValue="id"/>
    </form:select>
</div>
<div style="margin-top: 5px;">
    <table>
        <tr>
            <td class="no_border">
                <button id="save_for_revision" class="controlToDisable" style="margin-left:5px;width:210px"
                        onclick="submitform('send_draft')"
                        type="button">
                    Сохранить для доработки
                </button>
                <button id="submit_button" class="controlToDisable" style="width:210px"
                        onclick="checkDurationThenSendForm()" type="button">
                    Отправить отчёт
                </button>
            </td>
            <td class="no_border" width="220px">
                <button id="new_report_button" style="width:210px; display:none;" type="button"
                        onclick="submitform('newReport')">
                    Очистить все поля
                </button>
            </td>
        </tr>
    </table>
</div>
</form:form>
</body>
</html>
