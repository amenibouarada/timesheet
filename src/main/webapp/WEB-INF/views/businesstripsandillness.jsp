<%@ page import="static com.aplana.timesheet.form.BusinessTripsAndIllnessForm.*" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title><fmt:message key="title.businesstripsandillness"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/businesstripsandillness.css">
    <script type="text/javascript" src="<%= getResRealPath("/resources/js/businesstripandillness.js", application) %>"></script>
    <script type="text/javascript">
        var employeeList = ${employeeListJson};
        var forAll = ${forAll};
        var empId = ${employeeId};
        var managerIdJsp = "${managerId}" != "" ? +"${managerId}" : null;

        var divisionIdJsp = ${divisionId};
        var reportFormedJsp = ${reportFormed};
        var periodicalsListNotEmpty = ${fn:length(reports.periodicalsList) > 0};
        var managerMapJson = '${managerMapJson}';
        var regions = ${regionIds};
        var allValue = <%= ALL_VALUE %>;
        var loadImg = "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

        function saveForm(){
            <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
            businesstripsandillness.action = getContextPath() + "/businesstripsandillnessadd/" + empId;
            businesstripsandillness.submit();
            </sec:authorize>
        }
    </script>
</head>
<body>
    <h1><fmt:message key="title.businesstripsandillness"/></h1>
    <br>
    <form:form method="post" commandName="businesstripsandillness" name="mainForm">
    <table class="no_border" style="margin-bottom: 20px;">
        <tr>
            <td>
                <span class="lowspace">Подразделение:</span>
            </td>
            <td>
                <form:select class="without_dojo" path="divisionId" id="divisionId"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td rowspan="5" style="padding: 9px;vertical-align: top;">
                <span class="lowspace">Регионы:</span>
            </td>
            <td rowspan="5" style="padding: 5px;vertical-align: top;">
                <form:select path="regions" onmouseover="showTooltip(this)" size="6"
                             onmouseout="tooltip.hide()" multiple="true">
                    <form:option value="<%= ALL_VALUE %>" label="Все регионы"/>
                    <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace">Руководитель:</span>
            </td>
            <td>
                <form:select class="without_dojo" path="manager" onmouseover="showTooltip(this);"
                             onmouseout="tooltip.hide();" multiple="false" cssStyle="margin-left: 0px">
                    <form:options items="${managerList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace ">Сотрудник:</span>
            </td>
            <td>
                <div id='employeeIdDiv' name='employeeIdDiv'></div>
                <form:hidden path="employeeId" id="employeeId"/>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace">Начало периода:</span>
            </td>
            <td colspan="3">
                <div class="horizontalBlock">
                    <form:input path="dateFrom" id="dateFrom" class="date_picker"
                                data-dojo-type="DateTextBox" required="true"
                                onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace">Окончание периода:</span>
            </td>
            <td colspan="3">
                <div class="horizontalBlock">
                    <form:input path="dateTo" id="dateTo" class="date_picker"
                                data-dojo-type="DateTextBox" required="true"
                                onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>
                </div>

            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace">Тип:</span>
            </td>
            <td>
                <form:select class="without_dojo" path="reportType" id="reportType"
                             onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                             multiple="false">
                    <form:options items="${businesstripsandillness.reportTypes}" itemLabel="name" itemValue="id"
                                  required="true"/>
                </form:select>
            </td>
            <td colspan="2">
                <div class="floatleft lowspace">
                    <button id="show" class="butt block " onclick="showBusinessTripsAndIllnessReport()">
                        Показать
                    </button>
                </div>
            </td>
        </tr>
    </table>

    <%------------------------------TABLE-----------------------------------%>

    <table id="reporttable">
        <thead>
            <tr>
                <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                <th width="15" class="iconbutton">
                    <img src="<c:url value="/resources/img/add.gif"/>" title="Создать" onclick="createBusinessTripOrIllness();"/>
                </th>
                <th class="tight"></th>
                <th class="tight"></th>
                </sec:authorize>
                <th width="200">Сотрудник</th>
                <th width="200">Центр</th>
                <th width="200">Регион</th>
                <th width="100">Дата с</th>
                <th width="100">Дата по</th>
                <th width="100">Кол-во календарных дней</th>
                <th width="100">Кол-во <br>рабочих дней</th>
                <c:choose>
                    <c:when test="${reportFormed == 7}">
                        <th width="160">Проектная/внепроектная</th>
                    </c:when>
                    <c:when test="${reportFormed == 6}">
                        <th width="160">Основание</th>
                    </c:when>
                </c:choose>
                <th width="200">Комментарий</th>
            </tr>
        </thead>

        <c:choose>
            <c:when test="${not hasAnyEmployee}">
                <div style="margin-left: 10px">
                    <b>По заданным критериям не найдено ни одного сотрудника.</b>
                </div>
            </c:when>
            <c:when test="${not hasAnyReports and hasAnyEmployee}">
                <div style="margin-left: 10px">
                    <b>Нет данных о
                        <c:if test="${reportFormed == 6}">
                            болезнях
                        </c:if>
                        <c:if test="${reportFormed == 7}">
                            командировках
                        </c:if>
                        сотрудников за выбранный период</b>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach var="employeeReport" items="${reportsMap}">
                    <c:set var="reports" value="${employeeReport.value}"/>
                    <c:choose>
                        <c:when test="${fn:length(reports.periodicalsList) > 0}">
                            <tbody>

                                <c:forEach var="report" items="${reports.periodicalsList}">
                                    <tr>
                                        <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                                        <td></td>
                                        <td>
                                                <div class="iconbutton">
                                                    <img src="<c:url value="/resources/img/edit.png"/>" title="Редактировать"
                                                         onclick="editReport(${report.id});" />
                                                </div>
                                            </td>
                                            <td>
                                                <div class="iconbutton">
                                                    <c:choose>
                                                        <c:when test="${reportFormed == 6}">
                                                            <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                                                                 onclick="deleteReport(this.parentElement, ${report.id}, ${report.calendarDays}, ${report.workingDays}, ${report.workDaysOnIllnessWorked});" />
                                                        </c:when>
                                                        <c:when test="${reportFormed == 7}">
                                                            <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                                                                 onclick="deleteReport(this.parentElement, ${report.id}, ${report.calendarDays}, ${report.workingDays});" />
                                                        </c:when>
                                                    </c:choose>
                                                </div>
                                            </td>
                                        </sec:authorize>
                                        <td class="textcenter">${employeeReport.key.name}</td>
                                        <td class="textcenter">${employeeReport.key.division.name}</td>
                                        <td class="textcenter">${employeeReport.key.region.name}</td>
                                        <td class="textcenter"><fmt:formatDate value="${report.beginDate}" pattern="dd.MM.yyyy"/></td>
                                        <td class="textcenter"><fmt:formatDate value="${report.endDate}" pattern="dd.MM.yyyy"/></td>
                                        <td class="textcenter">${report.calendarDays}</td>
                                        <td class="textcenter">${report.workingDays}</td>
                                        <c:choose>
                                            <c:when test="${reportFormed == 6}">
                                                <td class="textcenter">${report.reason.value}</td>
                                            </c:when>
                                            <c:when test="${reportFormed == 7}">
                                                <td class="textcenter">
                                                    ${report.type.value}
                                                        <c:if test="${report.project != null}">
                                                            (${report.project.name})
                                                        </c:if>
                                                </td>
                                            </c:when>
                                        </c:choose>
                                        <td>${report.comment}</td>
                                    </tr>
                                </c:forEach>

                            </tbody>
                        </c:when>

                    </c:choose>
                </c:forEach>
                <c:choose>
                    <c:when test="${forAll != true}">
                        <c:choose>
                            <c:when test="${reportFormed == 6}">
                                    <tr><td colspan="5" class="bold">Итоги за период:</td></tr>
                                    <c:choose>
                                        <c:when test="${fn:length(reports.periodicalsList) > 0}">
                                            <tr>
                                                <td colspan="4" class="resultrow">Общее кол-во календарных дней болезни:</td>
                                                <td colspan="1" class="resultrow" id="mounthCalendarDaysOnIllness">${reports.mounthCalendarDays}</td>
                                            </tr>
                                            <tr>
                                                <td colspan="4" class="resultrow">Общее кол-во рабочих дней болезни:</td>
                                                <td colspan="1" class="resultrow" id="mounthWorkDaysOnIllness">${reports.mounthWorkDays}</td>
                                            </tr>
                                            <tr>
                                                <td colspan="4" class="resultrow">Общее кол-во рабочих дней болезни, когда сотрудник работал:</td>
                                                <td colspan="1" class="resultrow" id="mounthWorkDaysOnIllnessWorked"><fmt:formatNumber value="${reports.mounthWorkDaysOnIllnessWorked}" pattern="#.#"/></td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="5" class="bold">Нет данных о больничных сотрудника за выбранный период.</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                    <tr><td colspan="5" class="bold">Итоги за год:</td></tr>
                                    <tr>
                                        <td colspan="4" class="resultrow">Общее кол-во рабочих дней болезни:</td>
                                        <td colspan="1" class="resultrow" id="yearWorkDaysOnIllness">${reports.yearWorkDaysOnIllness}</td>
                                    </tr>
                                    <tr>
                                        <td colspan="4" class="resultrow">Общее кол-во рабочих дней болезни, когда сотрудник работал:</td>
                                        <td colspan="1" class="resultrow" id="yearWorkDaysOnIllnessWorked"><fmt:formatNumber value="${reports.yearWorkDaysOnIllnessWorked}" pattern="#.#"/></td>
                                    </tr>
                            </c:when>

                            <c:when test="${reportFormed == 7}">
                                <c:choose>
                                    <c:when test="${fn:length(reports.periodicalsList) > 0}">
                                                <tr><td colspan="5" class="bold">Итоги за период:</td></tr>
                                                <tr>
                                                    <td colspan="4" class="resultrow">Общее кол-во календарных дней в командировке:</td>
                                                    <td colspan="1" class="resultrow" id="mounthCalendarDaysInBusinessTrip">${reports.mounthCalendarDays}</td>
                                                </tr>
                                                <tr>
                                                    <td colspan="4" class="resultrow">Общее кол-во рабочих дней в командировке:</td>
                                                    <td colspan="1" class="resultrow" id="mounthWorkDaysOnBusinessTrip">${reports.mounthWorkDays}</td>
                                                </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="bold"><tr><td colspan="5" class="bold">Нет данных о командировках сотрудника за выбранный период.</td></tr></span>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                        </c:choose>
                    </c:when>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </table>
    </form:form>
</body>
</html>