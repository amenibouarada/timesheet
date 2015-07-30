<%@ page import="com.aplana.timesheet.system.properties.TSPropertyProvider" %>
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

    <script type="text/javascript">
        var divisionIdJsp = "${divisionId}" != "" ? +"${divisionId}" : null;
        var employeeIdJsp = "${employeeId}" != "" ? +"${employeeId}" : null;
        var typeVacPlanned = "${typeVacationPlanned}";
        var typeWithRequiredCommentJsp = +"${typeWithRequiredComment}";
        var loadImg = "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";
        var rulesUrl = "<%=rules%>";

        <sec:authorize access="not hasRole('ROLE_ADMIN')">
            var hasRoleAdmin = false;
        </sec:authorize>
        <sec:authorize access="hasRole('ROLE_ADMIN')">
            var hasRoleAdmin = true;
        </sec:authorize>
    </script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/createVacation.js"></script>

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
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>" htmlEscape="false"/>
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
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <span class="label">Сотрудник:</span>
            </td>
            <td>
                <div id='employeeIdSelect'></div>
                <form:hidden path="employeeId"/>
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
                <div id="countDays"></div>
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
                                    onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>
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