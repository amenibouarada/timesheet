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
    dojo.require("dijit.form.DateTextBox");
    dojo.require("dijit.Dialog");
    dojo.require("dijit.form.Textarea");
    dojo.require("dijit.form.Select");
    dojo.require("dijit.layout.TabContainer");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dojox.widget.Standby");
    dojo.require(CALENDAR_EXT_PATH);
    dojo.require("dijit.form.ValidationTextBox");
    dojo.require("dojo.parser");

    var unfinishedDayCauseList = ${unfinishedDayCauseJson};
    var overtimeCauseList = ${overtimeCauseJson};
    var overtimeThreshold = ${overtimeThreshold};
    var undertimeThreshold = ${undertimeThreshold};
    var workplaceList = ${workplaceJson};
    var actTypeList = ${actTypeJson};
    var projectList = ${projectListJson};
    var actCategoryList = ${actCategoryListJson};
    var availableActCategoryList = ${availableActCategoriesJson};
    var employeeList = ${employeeListJson};
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

    var root = getRootEventListener();
    var month = correctLength(new Date().getMonth() + 1);
    var standByElement;
    dojo.declare("Calendar", com.aplana.dijit.ext.Calendar, {
        getEmployeeId:function () {
            return dojo.byId("employeeId").value;
        },

        getClassForDateInfo:function (dateInfo, date) {
            switch (dateInfo) {
                case "1":// в этот день имеется отчет
                    return 'classDateGreen';
                    break;
                case "2":   //выходной или праздничный день
                    return 'classDateRedText';
                    break;
                case "0":   //день без отчета
                    if (date <= getFirstWorkDate()) // день раньше начала работы
                        return '';
                    var lastWorkDate = getLastWorkDate();
                    if (lastWorkDate != null && lastWorkDate != "" && date > lastWorkDate) // день после увольнения
                        return '';
                    if (date <= new Date())
                        return 'classDateRedBack';
                    else return '';
                default: // Никаких классов не назначаем, если нет информации
                    return '';
                    break;
            }
        }
    });

    dojo.declare("DateTextBox", dijit.form.DateTextBox, {
        popupClass:"Calendar"
    });

    dojo.ready(function () {

        //dojo.connect(dojo.byId("add_row_button"), "onclick", "addNewRow");
        dojo.connect(dojo.byId("plan"), "onkeyup", dojo.byId("plan"), textareaAutoGrow);

        <sec:authorize access="!hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')">
        // Выбор сотрудника доступен только руководителю и администратору
        // пока не будем блокировать
        //dojo.attr("divisionId", {disabled:"disabled"});
        //dojo.attr("employeeId", {disabled:"disabled"});
        </sec:authorize>

        timeSheetForm.divisionId.value = ${timeSheetForm.divisionId};

        divisionChange(timeSheetForm.divisionId);

        timeSheetForm.employeeId.value = ${timeSheetForm.employeeId};

        /* После реализации LDAP аутентификации в этом нет необходимости
         if (existsCookie('aplanaDivision')) {
         timeSheetForm.divisionId.value = CookieValue('aplanaDivision');
         if (existsCookie('aplanaEmployee')) {
         divisionChange(timeSheetForm.divisionId);
         timeSheetForm.employeeId.value = CookieValue('aplanaEmployee');
         dojo.removeAttr("view_reports_button", "disabled");
         }
         } */

        /*смотрим, поддерживаются ли куки и рисуем индикатор*/
        showCookieIndicator();
        /*смотрим, сколько строк отправляли в предыдущем отчете
         и задаем в новом отчете столько же*/
        if (existsCookie('aplanaRowsCount')) {
            if (CookieValue('aplanaRowsCount') > 0) {
                /* нафига это тут так было написано --- неизвестно
                 addNewRows(CookieValue('aplanaRowsCount') > 1 ? CookieValue('aplanaRowsCount') : 2);
                 */
                // Fix APLANATS-346
                addNewRows(CookieValue('aplanaRowsCount'));
            } else {
                addNewRows(2);
            }
        } else {
            addNewRows(2);
        }
        setDefaultDate(dojo.byId("employeeId").value);
        reloadTimeSheetState();
        recalculateDuration();
        refreshPlans(dijit.byId('calDate').value, dojo.byId('employeeId').value);

        // инициализация данных по выходным и отчетам для текущей даты
        initCurrentDateInfo('${timeSheetForm.employeeId}', dijit.byId('calDate').value,'/calendar/dates');
        //крутилка создается при после загрузки страницы,
        //т.к. если она создается в месте использования - ghb show не отображается картинка
        standByElement = new dojox.widget.Standby({target: dojo.query("body")[0], zIndex:1000});
    });

    function refreshPlans(date, employeeId) {
        var month = correctLength(date.getMonth() + 1);
        var year = date.getFullYear();
        var day = correctLength(date.getDate());

        var corretDate = year + "-" + month + "-" + day;
        dojo.xhrGet({
            url:"${pageContext.request.contextPath}" + "/timesheet/plans",
            handleAs:"json",
            timeout:10000,
            content:{date:corretDate, employeeId:employeeId},
            load:function (data, ioArgs) {
                if (data && ioArgs && ioArgs.args && ioArgs.args.content) {

                    if (data.prev != null) {
                        var prev = data.prev;
                        var dateString = prev.dateStr;

                        dojo.byId("lbPrevPlan").innerHTML = "Планы предыдущего рабочего дня (" + timestampStrToDisplayStr(dateString.toString()) + "):";
                        var planText = prev.plan;
                        if (planText.length != 0) {
                            var text = prev.plan;
                            text = text.replace(/\n/g, '<br>');
                            dojo.byId("plan_textarea").innerHTML = text;
                        }
                        else
                            dojo.byId("plan_textarea").innerHTML = "План предыдущего рабочего дня не был определен";
                    }
                    else {
                        dojo.byId("lbPrevPlan").innerHTML = "Ничего не запланировано.";
                        dojo.byId("plan_textarea").innerHTML = "";
                    }

                    if (data.next != null) {
                        var next = data.next;
                        var dateString = next.dateStr;
                        dojo.byId("lbNextPlan").innerHTML = "Планы на следующий(" + timestampStrToDisplayStr(dateString.toString()) + ") рабочий день:";
                        dojo.byId("plan").value = next.plan;
                    }
                    else {
                        dojo.byId("lbNextPlan").innerHTML = "Планы на следующий рабочий день:";
                    }

                }
            },
            error:function (err, ioArgs) {
                if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                    dojo.byId("lbPrevPlan").innerHTML = "Ничего не запланировано.";
                    dojo.byId("plan_textarea").innerHTML = "";
                }
            }
        });
    }

</script>

<script type="text/javascript">
    function submitform(s) {
        if (typeof(root.onbeforeunload) != "undefined") {
            root.onbeforeunload = null;
        }

        var longIllness = dojo.byId("long_illness");
        if (s == 'send' && confirmSendReport()) {
            var division = dojo.byId('divisionId');
            var employee = dojo.byId('employeeId');
            var rowsCount = dojo.query(".time_sheet_row").length;
            var projectId;
            var projectComponent;
            var workPlaceId;
            var workPlaceComponent;
            var diffProjects = false;
            var diffWorkPlaces = false;
            for (var i = 0; i < rowsCount; i++) {
                projectComponent = dojo.query("#project_id_" + i)
                if (!diffProjects && projectComponent.length > 0)
                    if (projectComponent[0].value) {
                        if (projectId && (projectId != projectComponent[0].value)) {
                            if (projectComponent[0].value != 0)
                                diffProjects = true;
                        }
                        else
                            projectId = projectComponent[0].value;
                    }

                workPlaceComponent = dojo.query("#workplace_id_" + i)
                if (!diffWorkPlaces && workPlaceComponent.length > 0)
                    if (workPlaceComponent[0].value) {
                        if (workPlaceId && (workPlaceId != workPlaceComponent[0].value)) {
                            if (workPlaceComponent[0].value != 0)
                                diffWorkPlaces = true;
                        }
                        else
                            workPlaceId = workPlaceComponent[0].value;
                    }
            }
            setCookie('aplanaDivision', division.value, TimeAfter(7, 0, 0));
            setCookie('aplanaEmployee', employee.value, TimeAfter(7, 0, 0));
            setCookie('aplanaRowsCount', rowsCount, TimeAfter(7, 0, 0));
            if (diffProjects)
                deleteCookie("aplanaProject");
            else
                setCookie('aplanaProject', projectId, TimeAfter(7, 0, 0));
            if (diffWorkPlaces)
                deleteCookie("aplanaWorkPlace");
            else
                setCookie('aplanaWorkPlace', workPlaceId, TimeAfter(7, 0, 0));
            timeSheetForm.action = "timesheet";
            processing();
            // disabled не включается в submit. поэтому снимем аттрибут.
            dojo.removeAttr("divisionId", "disabled");
            dojo.removeAttr("employeeId", "disabled");
            timeSheetForm.submit();

        }
        if (s == 'newReport' && confirmCreateNewReport()) {
            timeSheetForm.action = "newReport";
            timeSheetForm.submit();
        }
//            if (s == 'problem') {
//                var empId = dojo.byId("employeeId").value;
//                var divId = dojo.byId("divisionId").value;
//                window.open('problem/' + divId + '/' + empId, 'problem_window');
//            }
    }

    function CopyPlan() {
        var plan_text = dojo.byId("plan_textarea").innerHTML;
        plan_text = plan_text.replace(/<br>/g, '\n');
        plan_text = plan_text.replace(/&amp;/g, '&');
        dojo.byId("description_id_" + GetFirstIdDescription()).value = plan_text;
    }
    function requiredCommentSet(){
        var overtimeCause = dijit.byId("overtimeCause").get("value");
        var undertimeExp = (overtimeCause ==<%= UndertimeCausesEnum.OTHER.getId() %>);
        var workOnHolidayExp = (overtimeCause ==<%= WorkOnHolidayCausesEnum.OTHER.getId() %>)
        var overtimeExp = (overtimeCause ==<%= OvertimeCausesEnum.OTHER.getId() %>)

        if (undertimeExp || overtimeExp || workOnHolidayExp) {
            dijit.byId("overtimeCauseComment").attr("required", true);
        } else {
            dijit.byId("overtimeCauseComment").attr("required", false);
        }
     }
</script>
<style type="text/css">
    #date_warning {
        display: none;
        padding-left: 15px;
    }

    .classDateGreen {
        background-color: #97e68d !important;
    }

    .classDateRedBack {
        background-color: #f58383 !important;
    }

    .time_sheet_row select {
        width: 100%;
    }

</style>
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
        <div id="overtimeCause" onChange="overtimeCauseChange(this);requiredCommentSet();" data-dojo-type="dijit.form.Select"
             style="width: 99%;" data-dojo-props="value: '${timeSheetForm.overtimeCause}'"></div>
        <div style="margin-top: 10px;"><span>Комментарий</span></div>
        <div data-dojo-type="dijit.form.ValidationTextBox"
                  data-dojo-prop="missingMessage:'Комментарий для причины 'Другое' является обязательным!'"
                  wrap="soft" id="overtimeCauseComment" rows="10" style="width: 99%;margin-top: 3px;"
                  placeHolder="Напишите причину, если нет подходящей в списке"
                  tooltip="комментарий">${timeSheetForm.overtimeCauseComment}</div>
        <div id="typeOfCompensationContainer" style="margin-top: 10px;">
            <div style="margin-bottom: 3px;">Тип компенсации</div>
            <select data-dojo-type="dijit.form.Select"style="width: 99%;" id="typeOfCompensation"
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
<form:form method="post" commandName="timeSheetForm" cssClass="noborder">

    <%-- Костыль для диалога --%>
    <form:hidden path="overtimeCauseComment" id="overtimeCauseComment_hidden" />
    <form:hidden path="overtimeCause" id="overtimeCause_hidden" />
    <form:hidden path="typeOfCompensation" id="typeOfCompensation_hidden" />

    <div id="form_header" style="margin-bottom: 15px;">
        <span class="label">Подразделение</span>
        <form:select path="divisionId" id="divisionId" onchange="divisionChange(this)" class="without_dojo"
                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
            <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
        </form:select>

        <span class="label">Отчет сотрудника</span>
        <form:select path="employeeId" id="employeeId" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                     onmouseout="tooltip.hide();" onchange="onEmployeeChange(this)">
            <form:option label="" value="0"/>
        </form:select>
        <span class="label">за дату</span>
        <form:input path="calDate" id="calDate" class="date_picker" data-dojo-type="DateTextBox"
                    data-dojo-id="reportDate"
                    required="true" onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                    onChange="onCalDateChange(this)" />
        <span id="date_warning"></span>
    </div>

    <div style="width: 500px;">
        <span id="lbPrevPlan">Планы предыдущего рабочего дня:</span>

        <div id="plan_textarea"
             style="margin: 2px 0px 2px 0px; padding:2px 2px 2px 2px;border: solid 1px silver;"></div>
        <button id="add_in_comments" type="button" style="width:300px" onclick="CopyPlan()">Скопировать в первый
            комментарий
        </button>
    </div>
    <div id="marg_buttons" style="margin-top:15px;">

            <%--перенесен в шапку таблицы как картинка--%>
            <%--<button id="add_row_button" style="width:150px" type="button">Добавить строку</button>--%>

            <%--убрано, но есть отдельная кнопка для каждой строки таблицы--%>
            <%--<button id="del_row_button" style="width:210px" type="button" onclick="delSelectedRows()">Удалить выбранные--%>
            <%--строки--%>

            <%--</button>--%>
        <c:if test="${fn:length(errors) > 0}">
            <div class="errors_box">
                <c:forEach items="${errors}" var="error">
                    <fmt:message key="${error.code}">
                        <fmt:param value="${error.arguments[0]}"/>
                    </fmt:message><br/>
                </c:forEach>
            </div>
        </c:if>
        <!--<button id="report_problem_button" style="width:200px" type="button" onclick="submitform('feedback')">Сообщить о
            проблеме
        </button>-->
    </div>
    <div id="form_table">
        <table id="time_sheet_table">
            <tr id="time_sheet_header">
                <th style="min-width: 30px">
                    <a onclick="addNewRow()">
                        <img style="cursor: pointer;" src="<c:url value="/resources/img/add.gif"/>" width="15px"
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

            <c:if test="${fn:length(timeSheetForm.timeSheetTablePart) > 0}">
                <c:forEach items="${timeSheetForm.timeSheetTablePart}" varStatus="row">
                    <tr class="time_sheet_row" id="ts_row_${row.index}">

                            <%--чекбоксики для кнопки удаления выбранных строк--%>
                            <%--<td class="text_center_align"><input class="selectedRow" type="checkbox"--%>
                            <%--name="selectedRow[${row.index}]"--%>
                            <%--id="selected_row_id_${row.index}"/></td>--%>

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
                                                             rows="4" style="width: 100%"
                                                             id="description_id_${row.index}"
                                                             onkeyup="somethingChanged();"/></td>
                        <td class="text_center_align" id="jira_button_id_${row.index}">

                        </td>
                        <td class="top_align"><form:textarea wrap="soft" path="timeSheetTablePart[${row.index}].problem"
                                                             rows="4" style="width: 100%" id="problem_id_${row.index}"
                                                             onkeyup="somethingChanged();"/></td>
                    </tr>
                </c:forEach>
            </c:if>

            <tr style="height : 20px;" id="total_duration_row">
                <td></td>
                <td></td>
                <td>&nbsp;ИТОГО</td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td id="total_duration" class="text_right_align">0</td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </table>
    </div>

    <div id="plan_box" style="margin-bottom: 10px; margin-top: 10px">
        <span id="lbNextPlan" class="label">Планы на следующий рабочий день:</span>

        <div id="box_margin" style="margin-top :6px; margin-bottom: 8px;">
            <form:textarea wrap="soft" path="plan" id="plan" rows="7" cols="92"/>
            <br/>
        </div>
    </div>
    <div id="effort_box" >
        <span class="label">Оценка объема работ на следующий рабочий день:</span>
        <form:select path="effortInNextDay" id="effortInNextDay" class="without_dojo" onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
            <form:options items="${effortList}" itemLabel="value" itemValue="id"/>
        </form:select>
    </div>
    <div>
        <table>
            <tr>
                <td class="no_border" width="155px">
                    <button id="submit_button" style="width:210px" onclick="checkDurationThenSendForm()" type="button">
                        Отправить
                        отчёт
                    </button>
                </td>
                <td class="no_border" width="220px">
                    <button id="new_report_button" style="width:210px; display:none;" type="button"
                            onclick="submitform('newReport')">Очистить все поля
                    </button>
                </td>
            </tr>
        </table>
    </div>
</form:form>
</body>
</html>
