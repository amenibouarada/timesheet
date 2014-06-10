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
        dojo.require("dojo.cookie");
        dojo.ready(function () {
            window.focus();
            reloadViewReportsState();
            divisionChange(dojo.byId("divisionId"));
            dojo.byId("employeeId").value = ${employeeId};
            if (dojo.query('input[id^="delete_"]').length > 0) {
                var deleteAllCheckbox = dojo.byId("deleteAllCheckbox");

                dojo.connect(deleteAllCheckbox, "onclick", function (evt) {
                    setAllCheckBoxChecked();
                });
            } else {
                dojo.query("#deleteAllCheckbox").style("display", "none");
            }
        });

        dojo.require("dijit.form.DateTextBox");
        var monthList = ${monthList};
        var employeeList = ${employeeListJson};

        /* По умолчанию отображается текущий год и месяц. */
        function reloadViewReportsState() {
            var temp_date = new Date();
            var lastYear = ${year};
            var lastMonth = ${month};
            if (lastYear == 0 && lastMonth == 0) {
                dojo.byId("year").value = temp_date.getFullYear();
                dojo.byId("year").onchange();
                dojo.byId("month").value = temp_date.getMonth() + 1;
            }
            else {
                dojo.byId("year").value = lastYear;
                dojo.byId("year").onchange();
                dojo.byId("month").value = lastMonth;
            }
        }

        function showDates() {
            var empId = dojo.byId("employeeId").value;
            var year = dojo.byId("year").value;
            var divisionId = dojo.byId("divisionId").value;
            var month = dojo.byId("month").value;
            if (year != null && year != 0 && month != null && month != 0 && divisionId != null && divisionId != 0 && empId != null && empId != 0) {
                viewReportsForm.action = "<%=request.getContextPath()%>/viewreports/" + divisionId + "/" + empId + "/" + year + "/" + month;
                viewReportsForm.submit();
            } else {
                var error = "";
                if (year == 0 || year == null) {
                    error += ("Необходимо выбрать год и месяц!\n");
                }
                else if (month == 0 || month == null) {
                    error += ("Необходимо выбрать месяц!\n");
                }
                if (divisionId == 0 || divisionId == null) {
                    error += ("Необходимо выбрать подразделение и сотрудника!\n");
                }
                else if (empId == 0 || empId == null) {
                    error += ("Необходимо выбрать сотрудника!\n");
                }
                alert(error);
            }
        }

        function yearChange(obj) {
            var year = null;
            var monthSelect = dojo.byId("month");
            var monthValue = monthSelect.value;
            var monthOption = null;
            if (obj.target == null) {
                year = obj.value;
            }
            else {
                year = obj.target.value;
            }
            //Очищаем список месяцев.
            monthSelect.options.length = 0;
            for (var i = 0; i < monthList.length; i++) {
                if (year == monthList[i].year) {
                    insertEmptyOption(monthSelect);
                    for (var j = 0; j < monthList[i].months.length; j++) {
                        if (monthList[i].months[j].number != 0 && monthList[i].months[j].number != 27) {
                            monthOption = dojo.doc.createElement("option");
                            dojo.attr(monthOption, {value: monthList[i].months[j].number});
                            monthOption.title = monthList[i].months[j].name;
                            monthOption.innerHTML = monthList[i].months[j].name;
                            monthSelect.appendChild(monthOption);
                        }
                    }
                }
            }
            monthSelect.value = monthValue;
            if (year == 0) {
                insertEmptyOption(monthSelect);
            }
        }

        function setIdsToForm() {
            var idsField = dojo.byId("ids");
            var ids = [];
            var idString = "delete_";
            var deleteIdsElements = dojo.query('input[id^="' + idString + '"]');

            for (var index = 0; index < deleteIdsElements.length; ++index) {
                var element = deleteIdsElements[index];
                if (element.checked) {
                    ids.push(element.id.substring(idString.length, element.id.length));
                }
            }
            idsField.value = ids;
        }

        function deleteSelectedReports() {
            if (!confirm("Вы действительно хотите удалить выделенные отчеты")) {
                return;
            }
            setIdsToForm();
            var deleteForm = dojo.byId("deleteReportsForm");
            dojo.byId("link").value = document.URL;
            deleteForm.action = "<%=request.getContextPath()%>/deleteReports";
            deleteForm.submit();
        }

        function sendToRawReports() {
            if (!confirm("Вы действительно хотите отправить выделенные отчеты в черновик")) {
                return;
            }
            setIdsToForm();
            var deleteForm = dojo.byId("deleteReportsForm");
            dojo.byId("link").value = document.URL;
            deleteForm.action = "<%=request.getContextPath()%>/sendToRawReports";
            deleteForm.submit();
        }

        function setAllCheckBoxChecked() {
            var idString = "delete_";
            var deleteIdsElements = dojo.query('input[id^="' + idString + '"]');
            var deleteAllCheckbox = dojo.byId("deleteAllCheckbox");
            for (var index = 0; index < deleteIdsElements.length; ++index) {
                var element = deleteIdsElements[index];
                element.checked = deleteAllCheckbox.checked;
            }
        }

        function openVacation(date, emplId, divId){
            var vacationForm = dojo.byId("vacationsForm");
            vacationForm.action = "<%=request.getContextPath()%>/vacations";

            dojo.byId("calFromDate").value = date;
            dojo.byId("calToDate").value = date;
            dojo.query("#vacationsForm > #employeeId")[0].value = emplId;
            dojo.query("#vacationsForm > #divisionId")[0].value = divId;
            vacationForm.submit();
        }
    </script>
    <style type="text/css">
        .colortext {
            color: brown;
        }

        .center {
            text-align: center;
            text-valign: middle;
        }

        tr.b {
            font-weight: bold;
        }
    </style>
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
    <form:select path="divisionId" id="divisionId" onchange="divisionChange(this)" class="without_dojo"
                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
    </form:select>

    <span class="label">Отчет сотрудника</span>
    <form:select path="employeeId" id="employeeId" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                 onmouseout="tooltip.hide();" onchange="setDefaultEmployeeJob(-1);" cssStyle="width: auto">
        <form:option items="${employeeList}" label="" value="0"/>
    </form:select>
    <br><br>

    Год:
    <form:select path="year" id="year" class="without_dojo" onchange="yearChange(this)"
                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:option label="" value="0"/>
        <form:options items="${yearsList}" itemLabel="year" itemValue="year"/>
    </form:select>
    Месяц:
    <form:select path="month" id="month" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                 onmouseout="tooltip.hide();">
        <form:option label="" value="0"/>
    </form:select>
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
        <th width="150">Дата</th>
        <th width="160">Статус</th>
        <th width="150">Часы</th>
        <th width="150">Отсутствие</th>
        <th width="160">Проблемы</th>
    </tr>
    </thead>
    <tbody>
    <%-- <% for(DayTimeSheet report : request.getAttribute("reports")) { %> --%>
    <c:forEach var="report" items="${reports}">
        <c:if test="${report.statusHoliday}">
            <tr class="statusHoliday">
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <input type="checkbox" id="delete_${report.id}"/>
                    </c:if>
                </td>
            </sec:authorize>
            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>

            <td>Выходной
                <c:if test="${!report.isDayNotCome && !report.isCalDateLongAgo}">
                    <a href="<%=request.getContextPath()%>/timesheet?date=<fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>&id=${employeeId}">(Создать)</a>
                </c:if>
            </td>

            <td></td>
        </c:if>
        <c:if test="${report.statusNotStart}">
            <tr class="statusNotStart">
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <input type="checkbox" id="delete_${report.id}"/>
                    </c:if>
                </td>
            </sec:authorize>
            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
            <td>Ещё не принят на работу</td>
            <td></td>
        </c:if>
        <c:if test="${report.statusNormalDay}">
            <tr class="statusNormalDay toplan">
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <input type="checkbox" id="delete_${report.id}"/>
                    </c:if>
                </td>
            </sec:authorize>
            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
            <td>
                <a target="_blank"
                   href="<%=request.getContextPath()%>/report<fmt:formatDate value="${report.calDate}" pattern="/yyyy/MM/dd/"/>${report.timeSheet.employee.id}">Посмотреть
                    отчёт</a>
            </td>
            <td class="duration">${report.duration}</td>
        </c:if>
        <c:if test="${report.statusWorkOnHoliday}">
            <tr class="statusWorkOnHoliday">
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <input type="checkbox" id="delete_${report.id}"/>
                    </c:if>
                </td>
            </sec:authorize>
            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
            <td>
                Работа в выходной день <a target="_blank"
                                          href="<%=request.getContextPath()%>/report<fmt:formatDate value="${report.calDate}" pattern="/yyyy/MM/dd/"/>${report.timeSheet.employee.id}">Посмотреть
                отчёт</a>
            </td>
            <td class="duration">${report.duration}</td>
        </c:if>
        <c:if test="${report.statusNoReport}">
            <tr class="statusNoReport toplan">
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <input type="checkbox" id="delete_${report.id}"/>
                    </c:if>
                </td>
            </sec:authorize>
            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
            <td>Отчёта нет <a
                    href="<%=request.getContextPath()%>/timesheet?date=<fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>&id=${employeeId}">(Создать)</a>
            </td>
            <c:choose>
                <c:when test="${report.vacationDay || report.illnessDay}">
                    <td class="duration">${report.duration}</td>
                </c:when>
                <c:otherwise>
                    <td></td>
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:if test="${report.statusNotCome}">
            <tr class="statusNotCome">
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <input type="checkbox" id="delete_${report.id}"/>
                    </c:if>
                </td>
            </sec:authorize>
            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
            <td></td>
            <td></td>
        </c:if>

        <c:if test="${report.statusHaveDraft}">
            <tr class="statusHaveDraft">
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td style="text-align: center;">
                    <c:if test="${report.id != null}">
                        <input type="checkbox" id="delete_${report.id}"/>
                    </c:if>
                </td>
            </sec:authorize>
            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="dd.MM.yyyy"/></td>
            <td>
                Черновик
                <a href="<%=request.getContextPath()%>/timesheet?date=<fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>&id=${employeeId}&type=<%=TypesOfTimeSheetEnum.DRAFT.getId()%>"
                   onclick="">(Редактировать)
                        <%--<img src="<c:url value="/resources/img/edit.png"/>" width="15px"--%>
                        <%--title="Редактировать отчет"/>--%>
                </a>
            </td>
            <td class="durationDraft duration">
                <div class="durationDraftText">${report.duration}</div>
            </td>
        </c:if>

        <td>
            <c:if test="${report.illnessDay}">Болезнь</c:if>
            <c:if test="${report.vacationDay}">
                Отпуск
                <a href="#" onclick="openVacation('<fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>', ${report.timeSheet.employee.id},  ${report.timeSheet.employee.division.id});">(Подробнее)</a>
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
        <td colspan="2">Всего(факт):</td>
        <td id="durationall">${durationFact}</td>
    </tr>
    <tr>
        <td colspan="2">Всего к текущему времени(план):</td>
        <td id="durationPlanToCurrDate">${durationPlanToCurrDate}</td>
    </tr>
    <tr>
        <td colspan="2">Всего(план):</td>
        <td id="durationplan">${durationPlan}</td>
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

<sec:authorize access="hasRole('ROLE_ADMIN')">
    <form:form method="post" commandName="deleteReportsForm" name="deleteReportsForm">
        <form:hidden path="ids"/>
        <form:hidden path="link"/>
        <button id="deleteReportsButton" style="width:150px" style="vertical-align: middle" type="button"
                onclick="deleteSelectedReports()">Удалить
        </button>
        <button id="sendToRawReportsButton" style="width:150px" style="vertical-align: middle" type="button"
                onclick="sendToRawReports()">В черновик
        </button>
    </form:form>
</sec:authorize>
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
                    <c:when test="${reportdetail.act_type.value == 'Итого'}">
                        <tr style="font-weight: bold;">
                    </c:when>
                    <c:otherwise>
                        <tr>
                    </c:otherwise>
                </c:choose>
                <td>${reportdetail.act_type.value}</td>
                <td>${reportdetail.project.name}</td>
                <td class="duration">${reportdetail.planHours}</td>
                <td class="duration">${reportdetail.planPercent}%</td>
                <td class="duration">${reportdetail.factHours}</td>
                <td class="duration">${reportdetail.factPercent}%</td>
                </tr>
            </c:forEach>
        </c:otherwise>
    </c:choose>
    </tbody>
</table>
</body>
</html>