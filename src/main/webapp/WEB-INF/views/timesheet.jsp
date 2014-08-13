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
    dojo.require("dojox.html.entities");
    dojo.require(CALENDAR_EXT_PATH);
    dojo.require("dijit.form.ValidationTextBox");
    dojo.require("dojo.parser");
    dojo.require("dijit.form.FilteringSelect");
    dojo.require("dojo.data.ObjectStore");
    dojo.require("dojo.on");
    dojo.require("dojo.store.Memory");

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
    var dataDraft = '${data}';
    var isErrorPage = "${isErrorPage}";

    var root = getRootEventListener();
    var month = correctLength(new Date().getMonth() + 1);
    var standByElement;
    var dojoxDecode = dojox.html.entities.decode;
    var decodeMap = [["\u0027", "#39"], ["\u0028", "#40"], ["\u0029", "#41"]];
    var currentDate;
    var isFinalForm;

    dojo.declare("Calendar", com.aplana.dijit.ext.Calendar, {
        getEmployeeId: function () {
            return dojo.byId("employeeId").value;
        },

        getClassForDateInfo: function (dateInfo, date) {
            switch (dateInfo) {
                case "1":// в этот день имеется отчет
                    return 'classDateGreen';
                    break;
                case "2":   //выходной или праздничный день
                    return 'classDateRedText';
                    break;
                case "3":
                    return 'classDateBrownBack';
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
        popupClass: "Calendar"
    });

    dojo.ready(function () {

        dojo.connect(dojo.byId("jira_get_plans_button"), "click", dojo.byId("jira_get_plans_button"), getJiraPlans);
        dojo.connect(dojo.byId("plan"), "onkeyup", dojo.byId("plan"), textareaAutoGrow);
        dojo.connect(dojo.byId("divisionId"), "onchange", dojo.byId("divisionId"), updateEmployeeSelect);


        <sec:authorize access="!hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')">
        // Выбор сотрудника доступен только руководителю и администратору
        // пока не будем блокировать
        //dojo.attr("divisionId", {disabled:"disabled"});
        //dojo.attr("employeeId", {disabled:"disabled"});
        </sec:authorize>

        timeSheetForm.divisionId.value = ${timeSheetForm.divisionId};

        divisionChange(timeSheetForm.divisionId);
        updateEmployeeSelect();

        timeSheetForm.employeeId.value = ${timeSheetForm.employeeId};

        /*смотрим, поддерживаются ли куки и рисуем индикатор*/
        showCookieIndicator();

        if (selectedCalDate != "") {
            setTimesheetDate(selectedCalDate);
        } else {
            setDefaultDate(dojo.byId("employeeId").value);
        }
        initCurrentDateInfo('${timeSheetForm.employeeId}', dijit.byId('calDate').value, '/calendar/dates');

        currentDate = dijit.byId('calDate').get("value");

        if (!isErrorPage) {
            requestAndRefreshDailyTimesheetData(dijit.byId('calDate').value, dojo.byId('employeeId').value);
        } else {
            requestAndRefreshPreviousDayPlans(dijit.byId('calDate').value, dojo.byId('employeeId').value);
            reloadRowsState();
        }

        //крутилка создается при после загрузки страницы,
        //т.к. если она создается в месте использования - ghb show не отображается картинка
        standByElement = new dojox.widget.Standby({target: dojo.query("body")[0], zIndex: 1000});

        checkIsVacationDay();
    });

    // ToDo перенести все скрипты в timesheet.js
    function updateEmployeeSelect() {
        var divisionId =  timeSheetForm.divisionId.target == null ? timeSheetForm.divisionId.value : timeSheetForm.divisionId.target.value;

        if (employeeList.length > 0) {

            dojo.forEach(dojo.filter(employeeList, function (division) {
                return (division.divId == divisionId);
            }), function (divisionData) {
                var employeeArray = [];
                var emptyObj = {
                    id: 0,
                    value: ""
                };
                employeeArray.push(emptyObj);
                dojo.forEach(divisionData.divEmps, function (employee) {
                    employeeArray.push(employee);
                });
                employeeArray.sort(function (a, b) {
                    return (a.value < b.value) ? -1 : 1;
                });


                var employeeDataStore = new dojo.data.ObjectStore({
                    objectStore: new dojo.store.Memory({
                        data: employeeArray,
                        idProperty: 'id'
                    })
                });

                var employeeFlteringSelect = dijit.byId("employeeIdSelect");

                if (!employeeFlteringSelect) {
                    employeeFlteringSelect = new dijit.form.FilteringSelect({
                        id: "employeeIdSelect",
                        name: "employeeIdSelect",
                        labelAttr: "value",
                        store: employeeDataStore,
                        searchAttr: 'value',
                        queryExpr: "*\${0}*",
                        ignoreCase: true,
                        autoComplete: false,
                        style: 'width:200px',
                        required: true,
                        onMouseOver: function() {
                            tooltip.show(getTitle(this));
                        },
                        onMouseOut: function() {
                            tooltip.hide();
                        },
                        onChange: function() {
                            var value = this.item ? this.item.id : null;
                            dojo.byId('employeeId').value = value;
                            var obj = { value:value };
                            onEmployeeChange(obj);
                        }
                    }, "employeeIdSelect");
                    employeeFlteringSelect.startup();
                } else {
                    employeeFlteringSelect.set('store', employeeDataStore);
                    dijit.byId("employeeIdSelect").set('value', null);
                    dojo.byId('employeeId').value = null;
                }
            });
        }

        dijit.byId("employeeIdSelect").set('value', "${timeSheetForm.employeeId}" != "" ? +"${timeSheetForm.employeeId}" : null);
    }

    /**
     * Устанавливает видимость и активность для элементов управления, отвечающих за редактирование отчета.
     * @param isFinal [true/false] Показатель, является ли отчёт уже отправленным.
     */
    function setElementsAvailability(isFinal) {
        var controlsToHide = dojo.query(".controlToHide");
        var controlsToHideCount = controlsToHide.length;

        for (var i = 0; i < controlsToHideCount; i++) {
            controlsToHide[i].style.visibility = (isFinal) ? "collapse" : "visible";
        }

        var controlsToDisable = dojo.query(".controlToDisable");
        var controlsToDisableCount = controlsToDisable.length;

        for (var i = 0; i < controlsToDisableCount; i++) {
            controlsToDisable[i].disabled = isFinal;
        }
    }

    /**
    * Запрашивает данные timesheet и обновляет элементы страницы (план с прошлого дня, строки таблицы списания,
    * план на следующий день) в соответствии с полученными данными.
    * @param date Дата, для которой осуществляется запрос.
    * @param employeeId Идентификатор сотрудника.
    */
    function requestAndRefreshDailyTimesheetData(date, employeeId) {
        var month = correctLength(date.getMonth() + 1);
        var year = date.getFullYear();
        var day = correctLength(date.getDate());
        var requestDate = year + "-" + month + "-" + day;

        dojo.xhrGet({
            url: "${pageContext.request.contextPath}" + "/timesheet/dailyTimesheetData",
            handleAs: "json",
            timeout: 10000,
            content: {date: requestDate, employeeId: employeeId},
            load: function (data, ioArgs) {
                if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
                    var previous = data.previousDayData;
                    var current  = data.currentDayData;
                    var next     = data.nextDayData;

                    // Чистка таблицы занятости.

                    var timesheetRows = dojo.query(".time_sheet_row");
                    var timesheetRowsCount = timesheetRows.length;
                    for (var i = 0; i < timesheetRowsCount; i++) {
                        timesheetRows[i].parentNode.removeChild(timesheetRows[i]);
                    }

                    // Заполнение поля планов, указанных в предыдущий рабочий день.

                    var previousWorkDate = previous.workDate;
                    var previousPlan = previous.plan;

                    dojo.byId("lbPrevPlan").innerHTML = (previousWorkDate != null) ?
                            "Планы предыдущего рабочего дня (" + timestampStrToDisplayStr(previousWorkDate.toString()) + "):" :
                            "Планы предыдущего рабочего дня:";

                    dojo.byId("plan_textarea").innerHTML = (previousPlan != null && previousPlan.length != 0) ?
                            previousPlan.replace(/\n/g, '<br>') :
                            "План предыдущего рабочего дня не был определен";

                    // Заполнение строк таблицы занятости.

                    var currentTableData = current.data;
                    var isFinal = current.isFinal;

                    if (currentTableData != null) {
                        for (var j = 0; j < currentTableData.length; j++) {
                            addNewRow();
                            loadTableRow(j, currentTableData, isFinal);
                        }
                    } else if (existsCookie("aplanaRowsCount")) {
                        var cookieRowsCount = cookieValue("aplanaRowsCount");
                        addNewRows((cookieRowsCount > 0) ? cookieRowsCount : 1);
                    } else {
                        addNewRow();
                    }

                    recalculateDuration();

                    // Заполнение планов работы на будущее

                    var currentPlan = current.plan;
                    var nextWorkDate = current.nextWorkDate;
                    var nextDayEffort = current.effort;
                    var nextDaySummary = next.workSummary;

                    dojo.byId("lbNextPlan").innerHTML = (nextWorkDate != null) ?
                            "Планы на следующий рабочий день (" + timestampStrToDisplayStr(nextWorkDate.toString()) + "):" :
                            "Планы на следующий рабочий день:";

                    if (nextDayEffort != null) {
                        dojo.byId("effortInNextDay").value = nextDayEffort;
                    }

                    dojo.byId('plan').value =
                            (currentPlan != null && currentPlan.length != 0) ? currentPlan :
                            (nextDaySummary != null && nextDaySummary.length != 0) ? nextDaySummary : "";

                    dojo.attr("effortInNextDay", {
                        disabled: isFinal
                    });
                    dojo.attr("plan", {
                        readonly: isFinal
                    });

                    isFinalForm = isFinal;

                    setElementsAvailability(isFinal);
                }
            },
            error: function (err, ioArgs) {
                if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                    console.log(err);
                }
            }
        });
    }

    function requestAndRefreshPreviousDayPlans(date, employeeId) {
        var month = correctLength(date.getMonth() + 1);
        var year = date.getFullYear();
        var day = correctLength(date.getDate());
        var requestDate = year + "-" + month + "-" + day;

        dojo.xhrGet({
            url: "${pageContext.request.contextPath}" + "/timesheet/dailyTimesheetData",
            handleAs: "json",
            timeout: 10000,
            content: {date: requestDate, employeeId: employeeId},
            load: function (data, ioArgs) {
                if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
                    var previous = data.previousDayData;
                    var current = data.currentDayData;

                    // Заполнение поля планов, указанных в предыдущий рабочий день.

                    var previousWorkDate = previous.workDate;
                    var previousPlan = previous.plan;

                    dojo.byId("lbPrevPlan").innerHTML = (previousWorkDate != null) ?
                            "Планы предыдущего рабочего дня (" + timestampStrToDisplayStr(previousWorkDate.toString()) + "):" :
                            "Планы предыдущего рабочего дня:";

                    dojo.byId("plan_textarea").innerHTML = (previousPlan != null && previousPlan.length != 0) ?
                            previousPlan.replace(/\n/g, '<br>') :
                            "План предыдущего рабочего дня не был определен";

                    isFinalForm = false;
                }
            },
            error: function (err, ioArgs) {
                if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                    console.log(err);
                }
            }
        });
    }


    function hideShowElement(id, isHide) {
        console.log(id + " " + isHide);
        dojo.setStyle(id, {"display": isHide ? "none" : ""});
    }

</script>

<script type="text/javascript">
function submitform(s) {
    if (typeof(root.onbeforeunload) != "undefined") {
        root.onbeforeunload = null;
    }

    var longIllness = dojo.byId("long_illness");
    if ((s == 'send' && confirmSendReport()) || s == 'send_draft') {
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
            projectComponent = dojo.query("#project_id_" + i);
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
        if (s == 'send') {
            timeSheetForm.action = "timesheet";
        } else if (s == 'send_draft') {
            timeSheetForm.action = "sendDraft";
        }

        processing();
        // disabled не включается в submit. поэтому снимем аттрибут.
        dojo.removeAttr("divisionId", "disabled");
        dojo.removeAttr("employeeId", "disabled");
        timeSheetForm.submit();

    }
    else if (s == 'newReport' && confirmCreateNewReport()) {
        timeSheetForm.action = "newReport";
        timeSheetForm.submit();
    }
}

function requiredCommentSet() {
    var overtimeCause = dijit.byId("overtimeCause").get("value");
    var undertimeExp = (overtimeCause ==<%= UndertimeCausesEnum.OTHER.getId() %>);
    var workOnHolidayExp = (overtimeCause ==<%= WorkOnHolidayCausesEnum.OTHER.getId() %>)
    var overtimeExp = (overtimeCause ==<%= OvertimeCausesEnum.OTHER.getId() %>)

    if (undertimeExp || overtimeExp || workOnHolidayExp) {
        dijit.byId("overtimeCauseComment").attr("required", true);
        dijit.byId("typeOfCompensation").attr("required", true);
    } else {
        dijit.byId("overtimeCauseComment").attr("required", false);
        dijit.byId("typeOfCompensation").attr("required", false);
    }
}

function loadTableRow(i, data, isFinal) {
    isFinal = !!isFinal;

    //устанавливаем аттрибут
    dojo.attr("activity_type_id_" + i, {
        value: data[i].activity_type_id
    });
    //вызываем метод
    typeActivityChange(dojo.byId("activity_type_id_" + i));

    dojo.attr("workplace_id_" + i, {
        value: data[i].workplace_id
    });

    dojo.attr("project_id_" + i, {
        value: data[i].project_id
    });
    projectChange(dojo.byId("project_id_" + i));

    dojo.attr("project_role_id_" + i, {
        value: data[i].project_role_id
    });
    projectRoleChange(dojo.byId("project_role_id_" + i));

    dojo.attr("activity_category_id_" + i, {
        value: data[i].activity_category_id
    });
    setActDescription(i);

    dojo.attr("projectTask_id_" + i, {
        value: data[i].projectTask_id
    });
    setTaskDescription(i);

    dojo.attr("duration_id_" + i, {
        value: data[i].duration_id
    });
    checkDuration(dojo.byId("duration_id_" + i));
    recalculateDuration();

    dojo.attr("description_id_" + i, {
        value: dojoxDecode(dojoxDecode(data[i].description_id), decodeMap)
    });
    textareaAutoGrow(dojo.byId("description_id_" + i));

    dojo.attr("problem_id_" + i, {
        value: dojoxDecode(dojoxDecode(data[i].problem_id), decodeMap)
    });
    textareaAutoGrow(dojo.byId("problem_id_" + i));

    if (isFinal) {
        dojo.attr("activity_type_id_" + i,     {disabled: "disabled"});
        dojo.attr("workplace_id_" + i,         {disabled: "disabled"});
        dojo.attr("project_id_" + i,           {disabled: "disabled"});
        dojo.attr("project_role_id_" + i,      {disabled: "disabled"});
        dojo.attr("activity_category_id_" + i, {disabled: "disabled"});
        dojo.attr("projectTask_id_" + i,       {disabled: "disabled"});
        dojo.attr("duration_id_" + i,          {readonly: true});
        dojo.attr("description_id_" + i,       {readonly: true});
        dojo.attr("problem_id_" + i,           {readonly: true});
    }
}

/**
 Загрузка черновика
 **/
function loadDraft() {
    var date = dijit.byId('calDate').value;

    var currentDate = date.getFullYear() + "-" +
            correctLength(date.getMonth() + 1) + "-" +
            correctLength(date.getDate());
    var employeeId = dojo.byId('employeeId').value;
    var rowsCount;

    dojo.xhrGet({
        url: "${pageContext.request.contextPath}" + "/timesheet/loadDraft",
        handleAs: "json",
        timeout: 10000,
        content: {date: currentDate, employeeId: employeeId},
        load: function (data, ioArgs) {
            if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
                var div = dojo.byId('time_sheet_table');
                var tr = document.querySelectorAll('.time_sheet_row');
                rowsCount = tr.length;
                for (var j = 0; j < rowsCount; j++) {
                    tr[j].parentNode.removeChild(tr[j]);
                }
                for (var i = 0; i < data.data.length; i++) {
                    addNewRow();
                    loadTableRow(i, data.data);
                }
                dojo.byId('plan').innerHTML = data.plan;
                hideShowElement("load_draft", true);
                hideShowElement("load_draft_text", true);
            }
        },
        error: function (err, ioArgs) {
            console.log("error");
        }
    });
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

    .classDateBrownBack {
        background-color: #ddb491 !important;
    }

    .time_sheet_row select {
        width: 100%;
    }

    .dijitCalendarCurrentDate {
        background-color: #6495ED !important;
    }

    :disabled {
        background-color: #f0f0f0;
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
    <!--<button id="report_problem_button" style="width:200px" type="button" onclick="submitform('feedback')">Сообщить о
        проблеме
    </button>-->
</div>
<div id="form_table">
    <table id="time_sheet_table">
        <tr id="time_sheet_header">
            <th style="min-width: 30px">
                <a onclick="addNewRow()">
                    <img class="controlToHide" style="cursor: pointer;" src="<c:url value="/resources/img/add.gif"/>" width="15px"
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
        <div id='box_textArea' style="border: #AAA solid 1px;width: 775px;">

            <form:textarea wrap="soft" path="plan" id="plan" rows="7" cols="92"
                           cssStyle="border: none; outline: none;overflow:auto;"/>
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
                <button id="save_for_revision" class="controlToDisable" style="margin-left:5px;width:210px" onclick="submitform('send_draft')"
                        type="button">
                    Сохранить для доработки
                </button>
                <button id="submit_button" class="controlToDisable" style="width:210px" onclick="checkDurationThenSendForm()" type="button">
                    Отправить отчёт
                </button>
            </td>
            <td class="no_border" width="220px">
                <button id="new_report_button" style="width:210px; display:none;" type="button" onclick="submitform('newReport')">
                    Очистить все поля
                </button>
            </td>
        </tr>
    </table>
</div>
</form:form>
</body>
</html>
