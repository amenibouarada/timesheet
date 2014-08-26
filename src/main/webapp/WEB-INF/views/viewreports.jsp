<%@ page import="java.io.File" %>
<%@ page import="com.aplana.timesheet.enums.EffortInNextDayEnum" %>
<%@ page import="com.aplana.timesheet.enums.TypesOfTimeSheetEnum" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html>
<head>
    <title><fmt:message key="viewreports"/></title>
    <link rel="stylesheet" type="text/css"
          href="<%= request.getContextPath()%>/resources/css/viewreports.css?modified=<%= new File(application.getRealPath("/resources/css/viewreports.css")).lastModified()%>">
    <script type="text/javascript">
        var monthList = ${monthList};
        var lastYear = ${year};
        var lastMonth = ${month};
        var employeeIdJsp = "${employeeId}" != "" ? +"${employeeId}" : null;
    </script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/viewreports.js"></script>
</head>
<body>
<c:set var="defEffort" value="<%=EffortInNextDayEnum.NORMAL.getName()%>"/>

<h1><fmt:message key="viewreports"/></h1>
<br/>

<form:form method="post" commandName="viewReportsForm" name="mainForm">
    <c:if test="${fn:length(errors) > 0}">
        <div class="errors_box">
            <c:forEach items="${errors}" var="error">
                <fmt:message key="${error.code}">
                    <fmt:param value="${error.arguments[0]}"/>
                </fmt:message><br/>
            </c:forEach>
        </div>
    </c:if>
    <span class="label">Подразделение</span>
    <form:select path="divisionId" id="divisionId" class="without_dojo"
                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
    </form:select>

    <span class="label">Отчет сотрудника</span>

    <div id="employeeId" name="employeeId"></div>
    <span id="employeeBirthdayLabel" class="label">День рождения:</span>
    <span id="employeeBirthday" class="label"></span>

    <br><br>

    Год:
    <div data-dojo-type="dijit/form/ToggleButton" onclick="decYear()"><</div>
    <form:select path="year" id="year" class="without_dojo" onchange="yearChange(this)"
                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:options items="${yearsList}" itemLabel="year" itemValue="year"/>
    </form:select>
    <div data-dojo-type="dijit/form/ToggleButton" onclick="incYear()">></div>
    Месяц:
    <div data-dojo-type="dijit/form/ToggleButton" onclick="decMonth()"><</div>
    <form:select path="month" id="month" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                 onmouseout="tooltip.hide();">
    </form:select>
    <div data-dojo-type="dijit/form/ToggleButton" onclick="incMonth()">></div>
    <button id="show" style="width:150px" style="vertical-align: middle" type="button" onclick="showDates()">Показать
    </button>
    <p/>
    <input type="hidden" name="commandURL" id="commandURL"/>
</form:form>
<table id="viewreports">
    <thead>
    <tr>
        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <th width="25"><input type="checkbox" id="deleteAllCheckbox"/></th>
        </sec:authorize>
        <sec:authorize access="!hasRole('ROLE_ADMIN')">
            <th width="25"></th>
        </sec:authorize>
        <th width="150">Дата</th>
        <th width="160">Статус</th>
        <th width="150">Часы</th>
        <th width="150">Отсутствие</th>
        <th width="160">Проблемы</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="report" items="${reports}">
        <c:choose>
            <c:when test="${report.statusHoliday}">
                <tr class="statusHoliday">
                <td style="text-align: center;"></td>
                <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>

                <td>Выходной
                    <c:if test="${!report.isDayNotCome && !report.isCalDateLongAgo && !report.statusNotStart}">
                        <a href="<%=request.getContextPath()%>/timesheet?date=<fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>&id=${employeeId}">(Создать)</a>
                    </c:if>
                </td>

                <td></td>
            </c:when>

            <c:when test="${report.statusNotStart}">
                <tr class="statusNotStart">
                <td style="text-align: center;"></td>
                <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
                <td>Ещё не принят на работу</td>
                <td></td>
            </c:when>

            <c:when test="${report.statusNormalDay}">
                <tr class="statusNormalDay toplan">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <sec:authorize access="hasRole('ROLE_ADMIN')">
                            <input type="checkbox" id="delete_${report.id}"/>
                        </sec:authorize>
                        <sec:authorize access="!hasRole('ROLE_ADMIN')">
                            <c:if test="${report.deleteSendApprovalDate eq null}">
                                <div class="delete-button">
                                    <img src="/resources/img/delete.png" title="Удалить"
                                         onclick="sendDeleteTimeSheetApproval(${report.id});">
                                </div>
                            </c:if>
                        </sec:authorize>
                    </c:if>
                </td>
                <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
                <td>
                    <a target="_blank"
                       href="<%=request.getContextPath()%>/report<fmt:formatDate value="${report.calDate}" pattern="/yyyy/MM/dd/"/>${report.timeSheet.employee.id}">
                        Посмотреть отчёт
                    </a>
                    <c:if test="${report.deleteSendApprovalDate ne null}">
                        <sec:authorize access="hasRole('ROLE_ADMIN')">
                            (имеется
                            <a href="#" onclick="showApprovalDialog('${report.deleteSendApprovalComment}')">запрос</a>
                            на ${report.deleteSendApprovalTypeName})
                        </sec:authorize>
                        <sec:authorize access="!hasRole('ROLE_ADMIN')">
                            (отправлен запрос на ${report.deleteSendApprovalTypeName})
                        </sec:authorize>
                    </c:if>
                </td>
                <td class="rightAlign">${report.duration}</td>
            </c:when>


            <c:when test="${report.statusWorkOnHoliday}">
                <tr class="statusWorkOnHoliday">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <sec:authorize access="hasRole('ROLE_ADMIN')">
                            <input type="checkbox" id="delete_${report.id}"/>
                        </sec:authorize>
                        <sec:authorize access="!hasRole('ROLE_ADMIN')">
                            <c:if test="${report.deleteSendApprovalDate eq null}">
                                <div class="delete-button">
                                    <img src="/resources/img/delete.png" title="Удалить"
                                         onclick="sendDeleteTimeSheetApproval(${report.id});">
                                </div>
                            </c:if>
                        </sec:authorize>
                    </c:if>
                </td>
                <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
                <td>
                    Работа в выходной день
                    <a target="_blank"
                       href="<%=request.getContextPath()%>/report<fmt:formatDate value="${report.calDate}" pattern="/yyyy/MM/dd/"/>${report.timeSheet.employee.id}">
                        Посмотреть отчёт
                    </a>
                    <c:if test="${report.deleteSendApprovalDate ne null}">
                        <sec:authorize access="hasRole('ROLE_ADMIN')">
                            (имеется
                            <a href="#" onclick="showApprovalDialog('${report.deleteSendApprovalComment}')">запрос</a>
                            на ${report.deleteSendApprovalTypeName})
                        </sec:authorize>
                        <sec:authorize access="!hasRole('ROLE_ADMIN')">
                            (отправлен запрос на ${report.deleteSendApprovalTypeName})
                        </sec:authorize>
                    </c:if>
                </td>
                <td class="rightAlign">${report.duration}</td>
            </c:when>


            <c:when test="${report.statusNoReport}">
                <tr class="statusNoReport toplan">
                <td style="text-align: center;"></td>
                <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
                <td>Отчёта нет <a
                        href="<%=request.getContextPath()%>/timesheet?date=<fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>&id=${employeeId}">(Создать)</a>
                </td>
                <c:choose>
                    <c:when test="${report.vacationDay || report.illnessDay}">
                        <td class="rightAlign">${report.duration}</td>
                    </c:when>
                    <c:otherwise>
                        <td></td>
                    </c:otherwise>
                </c:choose>
            </c:when>


            <c:when test="${report.statusNotCome}">
                <tr class="statusNotCome">
                <td style="text-align: center;">
                </td>
                <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
                <td></td>
                <td></td>
            </c:when>

            <c:when test="${report.statusHaveDraft}">
                <tr class="statusHaveDraft">
                <td style="text-align: center;"></td>
                <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
                <td>
                    Черновик
                    <a href="<%=request.getContextPath()%>/timesheet?date=
                        <fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>&id=${employeeId}&type=<%=TypesOfTimeSheetEnum.DRAFT.getId()%>"
                       onclick="">
                        (Редактировать)
                    </a>
                </td>
                <td class="durationDraft rightAlign">
                    <div class="durationDraftText">${report.duration}</div>
                </td>
            </c:when>

        </c:choose>
        <td>
            <c:if test="${report.illnessDay}">Болезнь</c:if>
            <c:if test="${report.vacationDay}">
                Отпуск
                <a href="#" onclick="openVacation('<fmt:formatDate value="${report.calDate}"
                                                                   pattern="yyyy-MM-dd"/>', ${report.emp.id},  ${report.emp.division.id});">(Подробнее)</a>
            </c:if>
            <c:if test="${report.businessTripDay}">Командировка</c:if>
        </td>

        <td>
            <c:if test="${report.trouble}">Проблема</c:if>
            <c:if test="${report.effort != defEffort}">${report.effort}</c:if>
        </td>

        </tr>

    </c:forEach>
    </tbody>
    <thead>
    <tr>
        <td colspan="3">Всего к текущему времени(план):</td>
        <td class="rightAlign" id="durationPlanToCurrDate">${durationPlanToCurrDate}</td>
    </tr>
    <tr>
        <td colspan="3">Всего к текущему времени(факт):</td>
        <td class="rightAlign" id="durationFactToCurrDate">${durationFactToCurrDate}</td>
    </tr>
    <tr>
        <td colspan="3">Всего(план):</td>
        <td class="rightAlign" id="durationplan">${durationPlan}</td>
    </tr>
    <tr>
        <td colspan="3">Всего(факт):</td>
        <td class="rightAlign" id="durationall">${durationFact}</td>
    </tr>
    </thead>
</table>

<form:form method="post" commandName="vacationsForm" name="vacationsForm">
    <form:hidden path="year"/>
    <form:hidden path="vacationId"/>
    <form:hidden path="vacationType"/>
    <form:hidden path="managerId"/>
    <form:hidden path="regions"/>
    <form:hidden path="approvalId"/>
    <form:hidden path="projectId"/>
    <form:hidden path="viewMode"/>
    <form:hidden path="calFromDate"/>
    <form:hidden path="calToDate"/>
    <form:hidden path="employeeId"/>
    <form:hidden path="divisionId"/>
</form:form>


<form:form method="post" commandName="deleteReportsForm" name="deleteReportsForm">

    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <form:hidden path="ids"/>
    </sec:authorize>

    <form:hidden path="link"/>

    <sec:authorize access="!hasRole('ROLE_ADMIN')">
        <form:hidden path="reportId"/>
        <form:hidden path="comment"/>
    </sec:authorize>

    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <button id="deleteReportsButton" style="width:150px" style="vertical-align: middle" type="button"
                onclick="deleteSelectedReports()">Удалить
        </button>
        <button id="sendToRawReportsButton" style="width:150px" style="vertical-align: middle" type="button"
                onclick="sendToRawReports()">В черновик
        </button>
    </sec:authorize>
</form:form>

<br>
<table id="viewreports">
    <thead>
    <tr>
        <th width="80">Тип</th>
        <th width="360">Проект/Пресейл</th>
        <th width="80">План, ч</th>
        <th width="80">План, %</th>
        <th width="80">Факт, ч</th>
        <th width="80">Факт, %</th>
    </tr>
    </thead>
    <tbody>
    <c:choose>
        <c:when test="${fn:length(reportsDetail) == 0}">
            <tr>
                <td colspan="6">Нет данных</td>
            </tr>
        </c:when>
        <c:otherwise>
            <c:forEach var="reportdetail" items="${reportsDetail}">
                <c:choose>
                    <c:when test="${reportdetail.act_type.value == EmployeeMonthReportDetail.ITOGO}">
                        <tr style="font-weight: bold;">
                    </c:when>
                    <c:otherwise>
                        <tr>
                    </c:otherwise>
                </c:choose>
                <td>${reportdetail.act_type.value}</td>
                <td>${reportdetail.project.name}</td>
                <td class="rightAlign">${reportdetail.planHours}</td>
                <td class="rightAlign">${reportdetail.planPercent}%</td>
                <td class="rightAlign">${reportdetail.factHours}</td>
                <td class="rightAlign">${reportdetail.factPercent}%</td>
                </tr>
            </c:forEach>
        </c:otherwise>
    </c:choose>
    </tbody>
</table>

<div id="sendDeleteReportDialog" data-dojo-type="dijit.Dialog" title="Выберите действие над отчетом:"
     style="display: none;">
    <div data-dojo-type="dijit.layout.ContentPane" style="width: 300px; height: 160px;">
        <div style="margin-bottom: 10px;">
            <input type="radio" checked="checked" name="deleteGroup" value="delete" id="deleteRB" style="margin: 3px;">Отправить
            запрос на удаление
            отчета<br>
            <input type="radio" name="deleteGroup" value="setDraft" style="margin: 3px;">Перевести в черновик<br>
        </div>
        <span>Комментарий</span>
        <textarea id="commentApproval" maxlength="600" rows="3" style="width: 97%;margin: 3px;"></textarea>

        <button id="" style="margin-top: 10px; float: right;"
                onclick="submitDeleteApproval()">
            Отправить
        </button>
    </div>
</div>

<div id="showApprovaldialog" data-dojo-type="dijit.Dialog" title="Комментарий к запросу" style="display: none;">
    <div data-dojo-type="dijit.layout.ContentPane" style="width: 310px; height: 110px;">
        <span>Комментарий</span>
        <textarea id="commentText" disabled maxlength="600" rows="3" style="width: 97%;margin: 3px;"></textarea>
        <button style="margin-top: 10px; float: right;"
                onclick="closeShowApprovalDialog()">
            Закрыть
        </button>
    </div>
</div>

</body>
</html>