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
<c:set var="vacationAprovementWiyhLm" value="<%=VacationStatusEnum.APPROVEMENT_WITH_LM.getId()%>"/>
<c:set var="vacationAprovementWiyhPm" value="<%=VacationStatusEnum.APPROVEMENT_WITH_PM.getId()%>"/>

<html>
<head>
<title><fmt:message key="title.vacations"/></title>

<link rel="stylesheet" type="text/css" href="<%= getResRealPath("/resources/css/vacations.css", application) %>"/>
<link rel="stylesheet" type="text/css" href="<%= getResRealPath("/resources/css/vacationsGraphic.css", application) %>"/>

<script src="<%= getResRealPath("/resources/js/vacations.js", application) %>" type="text/javascript"></script>
<script src="<%= getResRealPath("/resources/js/vacationsGraphic.js", application) %>" type="text/javascript"></script>

<script type="text/javascript">
    dojo.require("dojo.NodeList-traverse");
    dojo.require("dojox.html.entities");
    dojo.require("dijit.form.DateTextBox");
    dojo.require(CALENDAR_EXT_PATH);
    require(["dojo/parser", "dijit/TitlePane"]);

    var regionsIdList = ${regionsIdList};
    var managerList = ${managerListJson};
    var selectedAllRegion = null;
    var selectedEmployee = ${employeeId};
    var vacationListJSON =  ${vacationListByProjectJSON};
    var fullProjectList = ${fullProjectListJsonWithDivisionId};

    var PROJECT_ID = "<%= PROJECT_ID%>";
    var CAL_FROM_DATE = "<%= CAL_FROM_DATE %>";
    var CAL_TO_DATE = "<%= CAL_TO_DATE %>";
    var DIVISION_ID = "<%= DIVISION_ID %>";
    var EMPLOYEE_ID = "<%= EMPLOYEE_ID %>";
    var VACATION_ID = "<%= VACATION_ID %>";
    var ALL_VALUE = <%= ALL_VALUE %>;
    var MANAGER_ID = "<%= MANAGER_ID %>";
    var REGIONS = "<%= REGIONS %>";
    var APPROVAL_ID = "<%= APPROVAL_ID %>";

    var VACATION_WITH_PAY = "62";    // отпуск с сохранением содержания
    var VACATION_WITHOUT_PAY = "63"; // отпуск без сохранения содержания
    var VACATION_WITH_WORK = "64";   // отпуск с последующей отработкой
    var VACATION_PLANNED = "65";     // планируемый отпуск

    var contextPath = "<%=request.getContextPath()%>";

    dojo.declare("DateTextBox", dijit.form.DateTextBox, {
        popupClass:"dijit.Calendar"
    });

    dojo.ready(function () {
        window.focus();
        divisionChangeVac(dojo.byId(DIVISION_ID).value);

        var managerSelect = dojo.byId(MANAGER_ID);
        <c:if test="${managerId != null}">
          managerSelect.value = '${managerId}';
        </c:if>

        selectedAllRegion = dojo.byId(REGIONS).options[0].selected;
        fillEmployeeSelect();
        dojo.byId(EMPLOYEE_ID).value = ${employeeId};
        dojo.byId(VACATION_ID).setAttribute("disabled", "disabled");

        var projectId = ${projectId};
        if (projectId != null){
            dojo.byId(PROJECT_ID).value = projectId;
        }

        showGraphic();
        dojo.attr(dojo.byId("vacations"), {
            width: document.body.clientWidth - 20
        });
    });

</script>
</head>
<body>

<h1><fmt:message key="title.vacations"/></h1>
<br/>
<a target="_blank" href="<c:url value='/vacations_needs_approval'/>"><fmt:message key="link.vacation.approval"/></a>
<br/>
<a><fmt:message key="title.approval.waiting">
    <c:choose>
        <c:when test="${vacationNeedsApprovalCount!=1}">
            <fmt:message key="title.waiting.parts" var="waitingPart"/>
        </c:when>
        <c:otherwise>
            <fmt:message key="title.waiting.part" var="waitingPart"/>
        </c:otherwise>
    </c:choose>
    <fmt:param value="${waitingPart}"/>
    <fmt:param value="${vacationNeedsApprovalCount}"/>
    <fmt:param value="${approvalPart}"/>
</fmt:message></a>

<form:form method="post" commandName="vacationsForm" name="mainForm">
    <form:hidden path="<%= VACATION_ID%>"/>
    <form:hidden path="<%= APPROVAL_ID%>"/>
    <table class="without_borders">
        <colgroup>
            <col width="130"/>
            <col width="320"/>
        </colgroup>
        <tr>
            <td>
                <span class="label">Подразделение:</span>
            </td>
            <td>
                <form:select path="<%= DIVISION_ID %>" id="<%= DIVISION_ID %>" onchange="divisionChangeVac(this)"
                             class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <span class="label">Руководитель:</span>
            </td>
            <td>
                <form:select path="<%= MANAGER_ID %>" id="<%= MANAGER_ID %>" onChange="managerChange(this)"
                             class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:options items="${managerList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Проект</span>
            </td>
            <td>
                <form:select id="<%= PROJECT_ID %>" path="<%= PROJECT_ID %>" onChange="fillEmployeeSelect()"
                             cssClass="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Начало периода</span>
            </td>
            <td>
                <form:input path="<%= CAL_FROM_DATE %>" id="<%= CAL_FROM_DATE %>" class="date_picker"
                            data-dojo-type="DateTextBox" required="true"
                            onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                            onchange="fillEmployeeSelect()"/>
            </td>
            <td>
                <span class="label">Окончание периода</span>
            </td>
            <td>
                <form:input path="<%= CAL_TO_DATE %>" id="<%= CAL_TO_DATE %>" class="date_picker"
                            data-dojo-type="DateTextBox" required="true"
                            onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                            onchange="fillEmployeeSelect()"/>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Регионы:</span>
            </td>
            <td>
                <form:select path="<%= REGIONS %>" onmouseover="showTooltip(this)" size="5"
                             onmouseout="tooltip.hide()" multiple="true" onchange="updateMultipleForSelect(this)">
                    <form:option value="<%= ALL_VALUE %>" label="Все регионы"/>
                    <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <span class="label">Сотрудник:</span>
            </td>
            <td>
                <form:select path="<%= EMPLOYEE_ID %>" id="<%= EMPLOYEE_ID %>" class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();" onChange="changeSelectedEmployee()">
                    <form:option items="${employeeList}" label="" value="0"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Тип отпуска:</span>
            </td>
            <td>
                <form:select path="<%= VACATION_TYPE %>" id="<%= VACATION_TYPE %>"
                             onMouseOver="tooltip.show(getTitle(this));"
                             onMouseOut="tooltip.hide();" multiple="false" size="1">
                    <form:option value="0" label="Все"/>
                    <form:options items="${vacationTypes}" itemLabel="value" itemValue="id"/>
                </form:select>
            </td>
        </tr>
    </table>
    <button id="show" style="width:150px" style="vertical-align: middle" type="submit"
            onclick="showVacations()">Показать
    </button>

    <br/><br/>

    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>"/>
</form:form>
<br/>


<div data-dojo-type="dijit/layout/TabContainer" style="width: 100%;" doLayout="false">
    <div data-dojo-type="dijit/layout/ContentPane" title="Таблица" data-dojo-props="selected:true">
        <table id="vacations">
            <thead>
            <tr>
                <th width="15" class="create-button">
                    <img src="<c:url value="/resources/img/add.gif"/>" title="Создать" onclick="createVacation();"/>
                </th>
                <th width="160">Статус</th>
                <th width="220">Тип отпуска</th>
                <th width="110">Дата создания</th>
                <th width="110">Дата с</th>
                <th width="110">Дата по</th>
                <th width="120">Кол-во календарных дней</th>
                <th width="130">Кол-во рабочих дней</th>
                <th width="270">Комментарий</th>
                <th width="270">Сотрудник</th>
                <th width="200">Центр</th>
                <th width="120">Регион</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
            <c:when test="${fn:length(vacationsList) == 0}">
            <tr>
                <td colspan="12">Нет ни одного заявления на отпуск, удовлетворяющего выбранным параметрам</td>
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
                                         onclick="deleteVacation(this.parentElement, ${vacation.id});"/>
                                </div>
                            </sec:authorize>
                        </td>
                        <td class="centered">
                                ${vacation.status.value}
                            <c:if test="${fn:length(vacation.vacationApprovals) > 0}">
                                <div data-dojo-type="dijit/TitlePane" data-dojo-props="title: 'Согласующие', open: false"
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
                                                            <fmt:formatDate value="${va.responseDate}" pattern="dd.MM.yyyy"/>
                                                        </c:when>
                                                        <c:when test="${!va.result && va.result != null}">
                                                            Отклонено
                                                            <br>
                                                            <fmt:formatDate value="${va.responseDate}" pattern="dd.MM.yyyy"/>
                                                        </c:when>
                                                        <c:when test="${vacation.status.id == vacationApproved && va.result == null}">
                                                            Согласовано автоматически по истечении установленного времени
                                                        </c:when>
                                                        <c:when test="${va.manager.id == curEmployee.id}">
                                                            <a href="<%= request.getContextPath() %>/vacation_approval?uid=${va.uid}"
                                                               target="blank">
                                                                Ожидается Ваше согласование</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            Запрос отправлен<br>
                                                            <fmt:formatDate value="${va.requestDate}" pattern="dd.MM.yyyy"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <sec:authorize access="
                                               hasRole('ROLE_ADMIN') and
                                                ${
                                                    ((vacation.status.id eq vacationAprovementWiyhLm)
                                                    or (vacation.status.id eq vacationAprovementWiyhPm))
                                                    and (!va.result)
                                                }
                                        ">
                                                        <div class="delete-button">
                                                            <img src="<c:url value="/resources/img/delete.png"/>"
                                                                 title="Удалить утверждающего"
                                                                 onclick="deleteApprover(${va.id})"/>
                                                        </div>
                                                    </sec:authorize>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </table>
                                </div>
                            </c:if>
                        </td>
                        <td class="centered">${vacation.type.value}</td>
                        <td class="date"><fmt:formatDate value="${vacation.creationDate}" pattern="dd.MM.yyyy"/></td>
                        <td class="date"><fmt:formatDate value="${vacation.beginDate}" pattern="dd.MM.yyyy"/></td>
                        <td class="date"><fmt:formatDate value="${vacation.endDate}" pattern="dd.MM.yyyy"/></td>
                        <td class="centered">${calDays[lp.index]}</td>
                        <td class="centered">${workDays[lp.index]}</td>
                        <td class="centered">
                                ${vacation.comment}
                            <c:if test="${vacation.author.id ne vacation.employee.id}">
                                <c:if test="${fn:length(vacation.comment) != 0}"><br/><br/></c:if>
                                Заявка создана сотрудником ${vacation.author.name}
                            </c:if>
                        </td>
                        <td class="centered">${vacation.employee.name}</td>
                        <td class="centered">${vacation.employee.division.name}</td>
                        <td class="centered">${vacation.employee.region.name}</td>
                    </tr>
                </c:forEach>
                </tbody>
                <tfoot>
                <tr class="summary">
                    <td colspan="3">Кол-во утвержденных заявлений на отпуск</td>
                    <td colspan="1">${summaryApproved}</td>
                </tr>
                <tr class="summary">
                    <td colspan="3">Кол-во отклоненных заявлений на отпуск</td>
                    <td colspan="1">${summaryRejected}</td>
                </tr>
                <tr>
                    <td colspan="4" class="centered">
                        <c:choose>
                            <c:when test="${employeeId != -1}">
                                <div data-dojo-type="dijit/TitlePane"
                                     data-dojo-props="title: 'Кол-во дней отпуска за период', open: false"
                                     style="margin: 3px; padding: 0;">
                                    <table class="centered">
                                        <thead>
                                        <tr>
                                            <th width="170">Год</th>
                                            <th width="170">Тип отпуска</th>
                                            <th width="170">Календарные дни</th>
                                            <th width="170">Рабочие дни</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="cal" items="${calDaysCount}">
                                            <tr>
                                                <td>${cal.year}</td>
                                                <td>${cal.vacationType}</td>
                                                <td>${cal.summaryCalDays}</td>
                                                <td>${cal.summaryWorkDays}</td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                        </c:choose>
                    </td>
                </tr>
                </tfoot>
            </c:otherwise>
            </c:choose>
        </table>
    </div>
    <div data-dojo-type="dijit/layout/ContentPane" title="График">
        <div style="position:relative;" class="Gantt" id="graphic_div"> </div>
    </div>
</div>

</body>
</html>



