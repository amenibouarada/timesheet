<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ page import="static com.aplana.timesheet.form.VacationsForm.*" %>
<%@ page import="com.aplana.timesheet.enums.VacationStatusEnum" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<c:set var="vacationApproved" value="<%=VacationStatusEnum.APPROVED.getId()%>"/>
<c:set var="vacationRejected" value="<%=VacationStatusEnum.REJECTED.getId()%>"/>
<c:set var="vacationAprovementWiyhLm" value="<%=VacationStatusEnum.APPROVEMENT_WITH_LM.getId()%>"/>
<c:set var="vacationAprovementWiyhPm" value="<%=VacationStatusEnum.APPROVEMENT_WITH_PM.getId()%>"/>
<c:set var="vacationAprovedByPm" value="<%=VacationStatusEnum.APPROVED_BY_PM.getId()%>"/>


<html>
<head>
    <title><fmt:message key="title.vacations.approvals"/></title>
    <link rel="stylesheet" type="text/css" href="<%= getResRealPath("/resources/css/vacations.css", application) %>" />
    <script src="<%= getResRealPath("/resources/js/vacations.js", application) %>" type="text/javascript"></script>
    <script type="text/javascript">
        dojo.require("dojo.NodeList-traverse");
        dojo.require("dojox.html.entities");
        require(["dojo/parser", "dijit/TitlePane"]);

        function deleteVacation(vac_id) {
            if (!confirm("Удалить заявку?")) {
                return;
            }

            dojo.byId("<%= VACATION_ID %>").removeAttribute("disabled");
            dojo.byId("<%= VACATION_ID %>").value = vac_id;
            vacationsForm.action =
                    "<%=request.getContextPath()%>/vacations_needs_approval";
            vacationsForm.submit();
        }

    </script>
</head>
<body>

<h1><fmt:message key="title.vacations.approvals"/></h1>
<br/>

<form:form method="post" commandName="vacationsForm" name="mainForm">
    <form:hidden path="<%= VACATION_ID%>" />
</form:form>

<table id="vacations">
    <thead>
    <tr>
        <th width="15">
        </th>
        <th width="160">Статус</th>
        <th width="220">Тип отпуска</th>
        <th width="270">Сотрудник</th>
        <th width="120">Дата создания</th>
        <th width="120">Дата с</th>
        <th width="120">Дата по</th>
        <th width="130">Кол-во календарных дней</th>
        <th width="130">Кол-во рабочих дней</th>
        <th width="270">Комментарий</th>
    </tr>
    </thead>
    <tbody>
    <c:choose>
    <c:when test="${fn:length(vacationsList) == 0}">
        <tr>
            <td colspan="11">Нет ни одного необработанного запроса на согласование</td>
        </tr>
    </tbody>
    </c:when>
    <c:otherwise>
    <c:forEach var="vacation" items="${vacationsList}" varStatus="lp">
        <tr>
            <td>
                <sec:authorize access="
                    hasRole('ROLE_ADMIN') or
                    ${
                        (vacation.employee.id eq curEmployee.id) or
                        (vacation.author.id eq curEmployee.id)
                    }
                ">
                    <div class="delete-button">
                        <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                             onclick="deleteVacation(${vacation.id});" />
                    </div>
                </sec:authorize>
            </td>
            <td class="centered">
                <c:choose>
                <c:when test="${vacation.status.id == vacationApproved}">
                <span  style="color: #00b114">
                    </c:when>
                    <c:when test="${vacation.status.id == vacationRejected}">
                <span style="color: #d90002">
                    </c:when>
                    <c:when test="${vacation.status.id == vacationAprovementWiyhLm || vacation.status.id == vacationAprovementWiyhPm || vacation.status.id == vacationAprovedByPm}">
                <span style="color: blue">
                    </c:when>
                    <c:otherwise>
                <span class="centered">
                    </c:otherwise>
                </c:choose>
                        ${vacation.status.value}   </span>
                <c:if test="${fn:length(vacation.vacationApprovals) > 0}">
                <div data-dojo-type="dijit/TitlePane" data-dojo-props="title: 'Согласующие', open: true"
                     style="margin: 3px; padding: 0;">
                    <table class="centered">
                        <c:forEach var="va" items="${vacation.vacationApprovals}">
                        <tr>
                            <td>${va.manager.name}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${va.result}">
                                        Согласовано
                                        <br>
                                        <fmt:formatDate value="${va.responseDate}" pattern="dd.MM.yyyy" />
                                    </c:when>
                                    <c:when test="${!va.result && va.result != null}">
                                        Отклонено
                                        <br>
                                        <fmt:formatDate value="${va.responseDate}" pattern="dd.MM.yyyy" />
                                    </c:when>
                                    <c:when test="${va.manager.id == curEmployee.id}">
                                        <a href="<%= request.getContextPath() %>/vacation_approval?uid=${va.uid}" target="blank">
                                                Ожидается Ваше согласование</a>
                                    </c:when>
                                    <c:otherwise>
                                        Запрос отправлен<br>
                                           <fmt:formatDate value="${va.requestDate}" pattern="dd.MM.yyyy" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        </c:forEach>
                    </table>
                </div>
                </c:if>
            </td>
            <td class="centered">${vacation.type.value}</td>
            <td class="centered">${vacation.employee.name}</td>
            <td class="date"><fmt:formatDate value="${vacation.creationDate}" pattern="dd.MM.yyyy"/></td>
            <td class="date"><fmt:formatDate value="${vacation.beginDate}" pattern="dd.MM.yyyy"/></td>
            <td class="date"><fmt:formatDate value="${vacation.endDate}" pattern="dd.MM.yyyy"/></td>
            <td class="centered">${calDays[vacation]}</td>
            <td class="centered">${workDays[vacation]}</td>
            <td class="centered">
                ${vacation.comment}
                <c:if test="${vacation.author.id ne vacation.employee.id}">
                    <c:if test="${fn:length(vacation.comment) != 0}"><br/><br/></c:if>
                    Заявка создана сотрудником ${vacation.author.name}
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
    </c:otherwise>
    </c:choose>
</table>
</body>
</html>