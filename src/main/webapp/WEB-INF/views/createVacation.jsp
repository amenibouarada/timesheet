<%@ page import="com.aplana.timesheet.properties.TSPropertyProvider" %>
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
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/vacations.css" />
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/vacations.js"></script>
    <script type="text/javascript">

        dojo.ready(function() {
            window.focus();
            dojo.byId("divisionId").value = ${divisionId};
            vacationCreate_divisionChange(dojo.byId("divisionId"));
            dojo.byId("employeeId").value = ${employeeId};
            initCurrentDateInfo(${employee.id},dijit.byId('calFromDate').value,getUrl());



        });

        dojo.require("dijit.form.DateTextBox");
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
            getEmployeeId: getEmployeeId
            ,
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
                            return '';                   }
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
                    if (typeDay == 4 || typeDay ==3 || typeDay == 5) { //если выбран тип отпуска планируемый
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

        require(["dijit/Tooltip", "dojo/domReady!"], function(Tooltip){
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
            var empId = dojo.byId("employeeId").value;
            if (validate()) {
                createVacationForm.action =
                        "<%=request.getContextPath()%>/validateAndCreateVacation/" + empId + "/"
                                + (approved ? "1" : "0");
                createVacationForm.submit();
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

        function updateExitToWork() {
            var date = dojo.byId("calToDate").value;
            var exitToWorkElement = dojo.byId("exitToWork");

            if (typeof date == typeof undefined || date == null || date.length == 0) {
                exitToWorkElement.innerHTML = '';
            } else {
                exitToWorkElement.innerHTML =
                        "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

                dojo.xhrGet({
                    url: "<%= request.getContextPath()%>/getExitToWork/${employee.id}/" + date + "/",
                    handleAs: "text",

                    load: function(data) {
                        if (data.size != 0) {
                            exitToWorkElement.innerHTML = data;
                        } else {
                            exitToWorkElement.innerHTML = "Не удалось получить дату выхода из отпуска!";
                        }
                    },

                    error: function(error) {
                        exitToWorkElement.setAttribute("class", "error");
                        exitToWorkElement.innerHTML = error;
                    }
                });
            }
        }

        function cancel() {
            window.location = "<%= request.getContextPath() %>/vacations";
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

<div style="height: 50px">
    <br/>
    <fmt:message key="vacation.rules.begin"/> <a href="<%=rules%>"><fmt:message key="vacation.rules.link"/></a>
    <br/>
</div>
<form:form method="post" commandName="createVacationForm" name="mainForm" cssStyle="padding-top: 5px;">
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>" />

    <%--<form:hidden path="employeeId" />--%>

    <table class="without_borders">
        <colgroup>
            <col width="150" />
            <col width="320" />
        </colgroup>
        <tr>
            <td>
                <span class="label">Подразделение</span>
            </td>
            <td>
                <form:select path="divisionId" id="divisionId" onchange="vacationCreate_divisionChange(this)" class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <span class="label">Сотрудник:</span>
            </td>
            <td>
                <form:select path="employeeId" id="employeeId" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();"
                             onchange="dateInfoHolder = []"
                        >
                    <form:options items="${employeeList}" itemLabel="name" itemValue="id"/>
                </form:select>
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
                                    onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>
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
                                    onChange="updateExitToWork();"/>
                    </td>
                    <td>
                        <div class="question-hint">
                            <img id="calToDateToolTip"  src="<c:url value="/resources/img/question.png"/>"/>
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
                             onchange="initCurrentDateInfo(dojo.byId('employeeId').value,dijit.byId('calFromDate').value, getUrl());"
                             onMouseOut="tooltip.hide();" multiple="false" size="1">
                    <form:option value="0" label="" />
                    <form:options items="${vacationTypes}" itemLabel="value" itemValue="id" />
                </form:select>
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Комментарий</span>
            </td>
            <td>
                <form:textarea path="comment" id="comment" maxlength="400" rows="5" cssStyle="width: 100%" />
            </td>
        </tr>
    </table>

    <button type="button" onclick="createVacation(false)">Создать</button>
    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <button type="button" onclick="createVacation(true)">Добавить утвержденное заявление на отпуск</button>
    </sec:authorize>
    <button type="button" onclick="cancel()">Отмена</button>
</form:form>
</body>
</html>