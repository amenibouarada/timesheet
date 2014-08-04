<%@ page import="com.aplana.timesheet.system.properties.TSPropertyProvider" %>
<%@ page import="com.aplana.timesheet.enums.VacationTypesEnum" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%
    String rules = TSPropertyProvider.getVacationRulesUrl();
%>

<html>
<head>
<title><fmt:message key="title.createVacation"/></title>
<link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/vacations.css"/>

<script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/utils.js"></script>
<script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/vacations.js"></script>

<script type="text/javascript">

dojo.ready(function () {
    window.focus();
    dojo.byId("divisionId").value = ${divisionId};
    updateEmployeeSelect();
    dojo.byId("employeeId").value = ${employeeId};
    initCurrentDateInfo(${employeeId}, dijit.byId('calFromDate').value, getUrl());
});

dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ObjectStore");
dojo.require("dojo.store.Memory");
dojo.require(CALENDAR_EXT_PATH);

function getEmployeeId() {
    return dojo.byId("employeeId").value;
}

function getUrl() {
    if (dojo.byId("types").value === "${typeVacationPlanned}") {
        return '/calendar/vacationDatesPlanned';
    } else {
        return '/calendar/vacationDates';
    }
}

dojo.declare("Calendar", com.aplana.dijit.ext.Calendar, {
    getEmployeeId: getEmployeeId,
    getClassForDateInfo: function (dateInfo, date) {
        switch (dateInfo) {
            /*       <sec:authorize access="not hasRole('ROLE_ADMIN')">
             case "1":// этот день прошел
             return 'classDateRedBack';
             break;
                </sec:authorize>*/
            case "2":   //выходной или праздничный день
                return 'classDateRedText';
                break;
            case "3":   //в этот день имеется отпуск
                return 'classDateRedBack';
                break;
            case "4":   //в этот день имеется планируемый отпуск
                return 'classDateBlueBack';
                break;
                classDateVioletBack
            case "5":   //в этот день имеется пересечение планируемого и реального отпуска
                return 'classDateVioletBack';
                break;
            case "0":   //день без отпуска
                if (date <= getFirstWorkDate()) {// день раньше начала работы
                    return '';
                }
                else {
                    return 'classDateGreen';
                }
            default: // Никаких классов не назначаем, если нет информации
                return '';
                break;
        }
    }
});

dojo.declare("DateTextBox", com.aplana.dijit.ext.DateTextBox, {
    popupClass: "Calendar", isDisabledDate: function (date) {
        var typeDay = new Number(getTypeDay(date));
        if (dojo.byId("types").value === "${typeVacationPlanned}") {
            if (typeDay == 4 || typeDay == 3 || typeDay == 5) { //если выбран тип отпуска планируемый
                // и имеется пересечение планируемого и обычного отпуска
                // или планируемый или обычный то делаем ячейку недоступной
                return true;
            }
        } else if (typeDay == 3 || typeDay == 5) {  //если в этот день отпуск или
            // пересечение отпусков - делаем ячейку недоступной
            return true;
        } else
                <sec:authorize access="not hasRole('ROLE_ADMIN')">
            return (date <= new Date());
        </sec:authorize>
        <sec:authorize access="hasRole('ROLE_ADMIN')">
        return false;
        </sec:authorize>
    }
});

require(["dijit/Tooltip", "dojo/domReady!"], function (Tooltip) {
    new Tooltip({
        connectId: ["calToDateToolTip", "calFromDateToolTip"],
        label: "<table class='without_borders'>" +
                "<tr><td><div class='blockTooltip classDateGreen'> </div></td><td><div style='padding: 5px;'> - эти дни доступны для оформления отпуска</div></td></tr>" +
                "<tr><td><div class='blockTooltip classDateRedBack'> </div></td><td> <div style='padding: 5px;'> - эти дни недоступны для оформления отпуска (имется отпуск)</div> </td></tr>" +
                "<tr><td><div class='blockTooltip classDateBlueBack'> </div></td><td> <div style='padding: 5px;'> - в эти дни запланирован отпуск</div> </td></tr>" +
                "<tr><td><div class='blockTooltip classDateVioletBack'> </div></td><td> <div style='padding: 5px;'> - эти дни недоступны для оформления <br> " +
                "отпуска (имется обычный и запланированный отпуск)</div> </td></tr>" +
                "<table>"
    });
});

var employeeList = ${employeeListJson};

function setDate(date_picker, date) {
    date_picker.set("displayedValue", date);
}

function createVacation(approved) {
    dojo.byId("createVacationId").disabled = true;
    var createVacAdminBtn = dojo.byId("createApprovedVacationId");
    //Может быть null если пользователь не с админискими правами
    if (createVacAdminBtn) createVacAdminBtn.disabled = true;

    var empId = dojo.byId("employeeId").value;
    if (validate()) {
        createVacationForm.action =
                "<%=request.getContextPath()%>/validateAndCreateVacation/" + empId + "/"
                        + (approved ? "1" : "0");
        createVacationForm.submit();
    } else {
        dojo.byId("createVacationId").disabled = false;
        if (createVacAdminBtn) createVacAdminBtn.disabled = false;
    }
}

function validate() {
    var fromDate = dijit.byId("calFromDate").get('value');
    var toDate = dijit.byId("calToDate").get('value');
    var type = dojo.byId("types").value;
    var comment = dojo.byId("comment").value;

    var error = "";

    if (isNilOrNull(fromDate)) {
        error += "Необходимо указать дату начала отпуска\n";
    }

    if (isNilOrNull(toDate)) {
        error += "Необходимо указать дату окончания отпуска\n";
    }

    if (fromDate > toDate) {
        error += "Дата начала отпуска не может быть больше даты окончания\n";
    }

    if (isNilOrNull(type)) {
        error += "Необходимо указать тип отпуска\n";
    }

    if (type == ${typeWithRequiredComment} && comment.length == 0) {
        error += "Необходимо написать комментарий\n";
    }

    if (error.length == 0) {
        return true;
    }

    alert(error);

    return false;
}

function updateExitToWorkAndCountVacationDay() {
    var fromDate = dojo.byId("calFromDate").value;
    var endDate = dojo.byId("calToDate").value;
    var vacationType = dojo.byId("types").value;
    var exitToWorkElement = dojo.byId("exitToWork");

    if ((typeof fromDate == typeof undefined || fromDate == null || fromDate.length == 0)
            || (typeof endDate == typeof undefined || endDate == null || endDate.length == 0)
            || (typeof vacationType == typeof undefined || vacationType == null)) {
        exitToWorkElement.innerHTML = '';
    } else {
        exitToWorkElement.innerHTML =
                "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

        dojo.xhrGet({
            url: "<%= request.getContextPath()%>/getExitToWorkAndCountVacationDay",
            handleAs: "json",
            content: {
                beginDate: fromDate,
                endDate: endDate,
                employeeId: getEmployeeId(),
                vacationTypeId: vacationType
            },
            load: function (data) {
                if (data.size != 0) {
                    exitToWorkElement.setAttribute("class", "");
                    if (data.error != undefined) {
                        var errorField = dojo.byId("errorField");
                        errorField.innerHTML = "<div style='background: #F9F7BA;padding: 5px;color: #F00;'>" + data.error + "</div>";
                        exitToWorkElement.innerHTML = "";
                        if (dojo.byId("createVacationForm.errors") != undefined) {
                            dojo.destroy("createVacationForm.errors");
                        }
                    } else {
                        var errorField = dojo.byId("errorField");
                        errorField.innerHTML = "";
                        exitToWorkElement.innerHTML = "Количество рабочих дней в отпуске :" + data.vacationWorkDayCount +
                                "<br>Количество дней в отпуске :" + data.vacationDayCount +
                                "<br>Дата выхода на работу: " + data.exitDate;
                        if (data.vacationFridayInform) {
                            exitToWorkElement.innerHTML += "<br><b><i>Отпуск необходимо оформлять с понедельника по воскресенье</i></b>";
                        }
                    }
                } else {
                    exitToWorkElement.innerHTML = "Не удалось получить дату выхода из отпуска!";
                }
            },

            error: function (error) {
                exitToWorkElement.setAttribute("class", "error");
                exitToWorkElement.innerHTML = "Не удалось получить дату выхода из отпуска!";
            }
        });
    }
}

function cancel() {
    window.location = "<%= request.getContextPath() %>/vacations";
}
function openCreateVacationRules() {
    window.open("<%=rules%>");
}

function updateSubmitButton() {
    if (dojo.byId('types').value == "<%= VacationTypesEnum.CHILDBEARING.getId() %>" || dojo.byId('types').value == "<%= VacationTypesEnum.CHILDCARE.getId() %>") {
        dojo.byId('createVacationId').setAttribute("onclick", "javascript: createVacation(true)");
    } else {
        dojo.byId('createVacationId').setAttribute("onclick", "javascript: createVacation(false)");
    }

}

function updateEmployeeSelect() {
    var divisionId = dojo.byId('divisionId').value;
    dojo.forEach(dojo.filter(employeeList, function (division) {
        return (division.divId == divisionId);
    }), function (divisionData) {

        var employeeArray = [];
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
                labelAttr: "value",
                store: employeeDataStore,
                searchAttr: 'value',
                queryExpr: "*\${0}*",
                ignoreCase: true,
                autoComplete: false,
                style: 'width:200px',
                required: true,
                onMouseOver: function () {
                    tooltip.show(getTitle(this));
                },
                onMouseOut: function () {
                    tooltip.hide();
                },
                onChange: function () {
                    var value = this.item ? this.item.id : null;
                    dojo.byId('employeeId').value = value;
                    dateInfoHolder = [];
                    updateExitToWorkAndCountVacationDay();
                }
            }, "employeeIdSelect");
            employeeFlteringSelect.startup();
        } else {
            employeeFlteringSelect.set('store', employeeDataStore);
            dijit.byId("employeeIdSelect").set('value', null);

        }
    });


    dijit.byId("employeeIdSelect").set('value', "${employeeId}" != "" ? +"${employeeId}" : null);
}

</script>
<style type="text/css">

    .classDateGreen {
        background-color: #97e68d !important;
    }

    .classDateRedBack {
        background-color: #f58383 !important;
    }

    .classDateBlueBack {
        background-color: #09a6f5 !important;
    }

    .classDateVioletBack {
        background-color: #be98ff !important;
    }

    .blockTooltip {
        width: 20px;
        height: 20px;
    }

    .time_sheet_row select {
        width: 100%;
    }
</style>
</head>
<body>

<h1><fmt:message key="title.createVacation"/></h1>

<div style="height: 40px">
    <br/>
    <fmt:message key="vacation.rules.begin"/> <a href="" onclick="openCreateVacationRules();"><fmt:message
        key="vacation.rules.link"/></a>
    <br/>
</div>
<div id="errorField"></div>
<form:form method="post" commandName="createVacationForm" name="mainForm" cssStyle="padding-top: 5px;">
    <%-- htmlEscape="false" для ошибки: error.createVacation.rights.notadmin --%>
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>" htmlEscape="false"/>
    <%--<form:hidden path="employeeId" />--%>
    <table class="without_borders">
        <colgroup>
            <col width="150"/>
            <col width="320"/>
        </colgroup>
        <tr>
            <td>
                <span class="label">Подразделение</span>
            </td>
            <td>
                <form:select path="divisionId" id="divisionId" class="without_dojo"
                             onchange="updateEmployeeSelect();updateExitToWorkAndCountVacationDay();"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <span class="label">Сотрудник:</span>
            </td>
            <td>
                <div id='employeeIdSelect'></div>
                <form:hidden path="employeeId" />
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Дата с</span>
            </td>
            <td>
                <table>
                    <td>
                        <form:input path="calFromDate" id="calFromDate" class="date_picker" required="true"
                                    data-dojo-type="DateTextBox"
                                    onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                                    onChange="updateExitToWorkAndCountVacationDay();"/>
                    </td>
                    <td>
                        <div class="question-hint">
                            <img id="calFromDateToolTip" src="<c:url value="/resources/img/question.png"/>"/>
                        </div>
                    </td>
                </table>
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Дата по</span>
            </td>
            <td>
                <table>
                    <td>
                        <form:input path="calToDate" id="calToDate" class="date_picker" required="true"
                                    data-dojo-type="DateTextBox"
                                    onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                                    onChange="updateExitToWorkAndCountVacationDay();"/>
                    </td>
                    <td>
                        <div class="question-hint">
                            <img id="calToDateToolTip" src="<c:url value="/resources/img/question.png"/>"/>
                        </div>
                    </td>
                </table>
                <div id="exitToWork"></div>
            </td>

        </tr>

        <tr>
            <td>
                <span class="label">Тип отпуска</span>
            </td>
            <td>
                <form:select path="vacationType" id="types" onMouseOver="tooltip.show(getTitle(this));"
                             onChange="updateExitToWorkAndCountVacationDay();updateSubmitButton();"
                             onMouseOut="tooltip.hide();" multiple="false" size="1">
                    <form:option value="0" label=""/>
                    <form:options items="${vacationTypes}" itemLabel="value" itemValue="id"/>
                </form:select>
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Комментарий</span>
            </td>
            <td>
                <form:textarea path="comment" id="comment" maxlength="600" rows="5" cssStyle="width: 100%"/>
            </td>
        </tr>
    </table>

    <button type="button" id="createVacationId" onclick="createVacation(false)">Создать</button>
    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <button type="button" id="createApprovedVacationId" onclick="createVacation(true)">Добавить утвержденное
            заявление на отпуск
        </button>
    </sec:authorize>
    <button type="button" onclick="cancel()">Отмена</button>
</form:form>
</body>
</html>