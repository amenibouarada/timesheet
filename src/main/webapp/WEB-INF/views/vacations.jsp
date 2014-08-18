<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ page import="static com.aplana.timesheet.form.VacationsForm.*" %>
<%@ page import="com.aplana.timesheet.dao.entity.Employee" %>
<%@ page import="com.aplana.timesheet.dao.entity.Vacation" %>
<%@ page import="com.aplana.timesheet.enums.VacationStatusEnum" %>
<%@ page import="com.aplana.timesheet.service.VacationService" %>
<%@ page import="com.aplana.timesheet.util.DateTimeUtil" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="vacationApproved" value="<%=VacationStatusEnum.APPROVED.getId()%>"/>
<c:set var="vacationAprovementWiyhLm" value="<%=VacationStatusEnum.APPROVEMENT_WITH_LM.getId()%>"/>
<c:set var="vacationAprovementWiyhPm" value="<%=VacationStatusEnum.APPROVEMENT_WITH_PM.getId()%>"/>
<c:set var="vacationAprovedByPm" value="<%=VacationStatusEnum.APPROVED_BY_PM.getId()%>"/>
<c:set var="vacationRejected" value="<%=VacationStatusEnum.REJECTED.getId()%>"/>
<c:set var="vacationCreated" value="<%=VacationStatusEnum.CREATED.getId()%>"/>

<%
    VacationService vacationService = (VacationService) request.getAttribute("vacationService");
%>

<html>
<head>
<title><fmt:message key="menu.vacations"/></title>

<link rel="stylesheet" type="text/css" href="<%= getResRealPath("/resources/css/vacations.css", application) %>"/>
<link rel="stylesheet" type="text/css" href="<%= getResRealPath("/resources/css/vacationsGraphic.css", application) %>"/>

<script src="<%= getResRealPath("/resources/js/vacations.js", application) %>" type="text/javascript"></script>
<script src="<%= getResRealPath("/resources/js/vacationsGraphic.js", application) %>" type="text/javascript"></script>

<script type="text/javascript">
    dojo.require("dojo.NodeList-traverse");
    dojo.require("dojox.html.entities");
    dojo.require("dijit.form.DateTextBox");
    dojo.require("dijit.form.FilteringSelect");
    dojo.require("dojo.data.ObjectStore");
    dojo.require("dojo.store.Memory");
    dojo.require(CALENDAR_EXT_PATH);
    require(["dojo/parser", "dijit/TitlePane"]);

    // список отпусков для построения графика отпусков, если не пустой, то получим от контроллера
    var vacationListJSON =  null;
    <c:if test="${vacationListByRegionJSON != null}">
        vacationListJSON = ${vacationListByRegionJSON};
    </c:if>
    // список праздников для построения графика отпусков, если не пустой, то получим от контроллера
    var holidayList = null;
    <c:if test="${vacationListByRegionJSON != null}">
        holidayList = ${holidayList};
    </c:if>

    var managerList = ${managerListJson};
    var selectedEmployee = ${employeeId};
    var fullProjectList = ${fullProjectListJsonWithDivisionId};

    var PROJECT_ID = "<%= PROJECT_ID %>";
    var CAL_FROM_DATE = "<%= CAL_FROM_DATE %>";
    var CAL_TO_DATE = "<%= CAL_TO_DATE %>";
    var DIVISION_ID = "<%= DIVISION_ID %>";
    var EMPLOYEE_ID = "<%= EMPLOYEE_ID %>";
    var VACATION_ID = "<%= VACATION_ID %>";
    var ALL_VALUE = <%= ALL_VALUE %>;
    var MANAGER_ID = "<%= MANAGER_ID %>";
    var REGIONS = "<%= REGIONS %>";
    var APPROVAL_ID = "<%= APPROVAL_ID %>";
    var VIEW_MODE = "<%= VIEW_MODE %>";

    var VACATION_WITH_PAY = "62";    // отпуск с сохранением содержания
    var VACATION_WITHOUT_PAY = "63"; // отпуск без сохранения содержания
    var VACATION_WITH_WORK = "64";   // отпуск с последующей отработкой
    var VACATION_PLANNED = "65";     // планируемый отпуск

    var VIEW_TABLE = <%= VIEW_TABLE %>;
    var VIEW_GRAPHIC_BY_DAY = <%= VIEW_GRAPHIC_BY_DAY %>;
    var VIEW_GRAPHIC_BY_WEEK = <%= VIEW_GRAPHIC_BY_WEEK %>;

    var contextPath = "<%=request.getContextPath()%>";

    dojo.declare("DateTextBox", dijit.form.DateTextBox, {
        popupClass:"dijit.Calendar"
    });

    dojo.ready(function () {

        dojo.connect(dojo.byId("<%= MANAGER_ID %>"), "onchange", dojo.byId("<%= MANAGER_ID %>"), updateEmployeeSelect);
        dojo.connect(dojo.byId("<%= PROJECT_ID %>"), "onchange", dojo.byId("<%= PROJECT_ID %>"), updateEmployeeSelect);

        dojo.query("#graphic_div").style("width", (document.body.offsetWidth - 30) + "px");
        window.focus();
        divisionChangeVac(dojo.byId(DIVISION_ID).value);

        var managerSelect = dojo.byId(MANAGER_ID);
        <c:if test="${managerId != null}">
          managerSelect.value = '${managerId}';
        </c:if>

        // регистрируем событие на переключение между вкладками
        var tabContainer = dojo.dijit.registry.byId("tabContainer");
        tabContainer.watch("selectedChildWidget", function(name, oval, nval){
            var selectedTabInput = dojo.byId(VIEW_MODE);
            if (nval.id == "firstTab") { // вкладка на которую переключились
                selectedTabInput.value = VIEW_TABLE;
            }else{ // иначе если это график, то смотрим состояние переключателей
                if (dojo.byId("byDay").checked){
                    selectedTabInput.value = VIEW_GRAPHIC_BY_DAY;
                }else{
                    selectedTabInput.value = VIEW_GRAPHIC_BY_WEEK;
                }
            }
        });
        // отобразим необходимую вкладку
        var viewMode = dojo.byId(VIEW_MODE).value;
        var tabContainer = dojo.dijit.registry.byId("tabContainer");
        var tab = dojo.dijit.registry.byId("secondTab");
        if (viewMode == VIEW_TABLE){
            tab = dojo.dijit.registry.byId("firstTab");
            dojo.byId("byDay").checked = true;
        }else{
            if (viewMode == VIEW_GRAPHIC_BY_DAY){
                dojo.byId("byDay").checked = true;
            }else{
                dojo.byId("byWeek").checked = true;
            }
        }
        tabContainer.selectChild(tab);

        updateEmployeeSelect();
        dojo.byId(EMPLOYEE_ID).value = ${employeeId};
        dojo.byId(VACATION_ID).setAttribute("disabled", "disabled");

        var projectId = ${projectId};
        if (projectId != null){
            dojo.byId(PROJECT_ID).value = projectId;
        }

        showGraphic(viewMode);
    });

</script>
</head>
<body>

<h1><fmt:message key="menu.vacations"/></h1>
<br/>
<a target="_blank" href="<c:url value='/vacations_needs_approval'/>"><fmt:message key="link.vacation.approval"/></a>
<br/>
<a style="color: blue">
    <c:choose>
        <c:when test="${vacationNeedsApprovalCount == 0}">
            <fmt:message key="title.approval.waiting.nothing"/>
        </c:when>
        <c:otherwise>
            <fmt:message key="title.approval.waiting">
                <fmt:param value="${vacationNeedsApprovalCount}"/>
            </fmt:message>
        </c:otherwise>
    </c:choose>
</a>
<br/>
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
                <form:select path="<%= MANAGER_ID %>" id="<%= MANAGER_ID %>" class="without_dojo"
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
                <form:select id="<%= PROJECT_ID %>" path="<%= PROJECT_ID %>" cssClass="without_dojo"
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
                            onchange="updateEmployeeSelect()"/>
            </td>
            <td>
                <span class="label">Окончание периода</span>
            </td>
            <td>
                <form:input path="<%= CAL_TO_DATE %>" id="<%= CAL_TO_DATE %>" class="date_picker"
                            data-dojo-type="DateTextBox" required="true"
                            onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                            onchange="updateEmployeeSelect()"/>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Регионы:</span>
            </td>
            <td>
                <form:select path="<%= REGIONS %>" onmouseover="showTooltip(this)" size="5" cssStyle="width: 200px;"
                             onmouseout="tooltip.hide()" multiple="true" onchange="multipleOptSelectedInSelect(this)">
                    <form:option value="<%= ALL_VALUE %>" label="Все регионы"/>
                    <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
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
                <span class="label">Тип отпуска:</span>
            </td>
            <td>
                <form:select path="<%= VACATION_TYPE %>" id="<%= VACATION_TYPE %>" cssStyle="width: 200px;"
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
    <form:input path="<%= VIEW_MODE %>" id="<%= VIEW_MODE %>" type="hidden"/>
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>"/>
</form:form>
<br/>


<div data-dojo-type="dijit/layout/TabContainer" doLayout="false" id="tabContainer">
    <div data-dojo-type="dijit/layout/ContentPane" id="firstTab" title="Таблица">
        <table id="vacations">
            <thead>
            <tr>
                <th width="15" class="create-button">
                    <img src="<c:url value="/resources/img/add.gif"/>" title="Создать" onclick="addVacation();"/>
                </th>
                <th width="160">Статус</th>
                <th width="220">Тип отпуска</th>
                <th width="250">Сотрудник</th>
                <th width="150">Дата создания</th>
                <th width="110">Дата с</th>
                <th width="110">Дата по</th>
                <th width="120">Кол-во календарных дней</th>
                <th width="130">Кол-во рабочих дней</th>
                <th width="270">Комментарий</th>
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
                            <% Vacation vacation = (Vacation) pageContext.getAttribute("vacation"); %>
                            <c:set var="vacationDeletePermission"
                                   value="<%= vacationService.isVacationDeletePermission(
                                        vacation,
                                        (Employee) pageContext.findAttribute(\"curEmployee\")) %>"
                                    />
                            <sec:authorize access="hasRole('ROLE_ADMIN') or ${vacationDeletePermission}">
                                <div class="delete-button">
                                    <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                                         onclick="deleteVacation(this.parentElement, ${vacation.id});"/>
                                </div>
                            </sec:authorize>
                            <c:set var="isVacationNotApproved"
                                   value="<%= vacationService.isVacationNotApproved(vacation)%>"/>
                            <c:set var="vacationApprovePermission"
                                   value="<%= vacationService.isVacationApprovePermission(vacation)%>"/>

                            <c:set var="beginDate" value="<%= DateTimeUtil.getOnlyDate(vacation.getBeginDate()) %>" />
                            <c:set var="endDate" value="<%= DateTimeUtil.getOnlyDate(vacation.getEndDate()) %>" />
                            <c:if test="${isVacationNotApproved }">
                                <sec:authorize access="hasRole('ROLE_ADMIN') or ${vacationApprovePermission}">
                                    <div class="delete-button">
                                        <img src="<c:url value="/resources/img/ok.png"/>" title="Утвердить"
                                             onclick="approveVacation(${vacation.id},'${beginDate}','${endDate}','${vacation.type.value}');"/>
                                    </div>
                                </sec:authorize>
                            </c:if>
                        </td>
                    <td id="statusTd" class="centered">
                        <c:choose>
                        <c:when test="${vacation.status.id == vacationApproved}">
                            <span style="color: #00b114">
                        </c:when>
                        <c:when test="${vacation.status.id == vacationCreated}">
                            <span style="color: #00b114">
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
                            ${vacation.status.value}</span>
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
                                                        }">
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
                                        <th width="170">Тип отпуска</th>
                                            <th width="170">Год</th>
                                            <th width="170">Календарные дни</th>
                                            <th width="170">Рабочие дни</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="cal" items="${calDaysCount}" varStatus="status">

                                            <c:if test="${(status.count-1)%years == 0 && years!=1}">
                                                <c:if test="${(status.count==1)}"><tr></c:if>
                                                <c:if test="${(status.count!=1)}"><tr class="trDelimeter"></c:if>
                                                <td rowspan="${years}">${cal.vacationType}</td>
                                            </c:if>
                                            <c:if test="${years==1}">
                                                <c:if test="${(status.count==1)}"><tr></c:if>
                                                <c:if test="${(status.count!=1)}"><tr class="trDelimeter"></c:if>
                                                <td>${cal.vacationType}</td>
                                            </c:if>
                                            <td>${cal.year}</td>
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
    <div data-dojo-type="dijit/layout/ContentPane" id="secondTab" title="График" onShow="showGraphic(dojo.byId(VIEW_MODE).value)">
        <div style="padding-left: 10px">

            <br>
            Отображение:<br>
            <input type="radio" onclick="showGraphic(VIEW_GRAPHIC_BY_DAY)" name="radiobuttons" id="byDay"> &nbsp; По дням <br>
            <input type="radio" onclick="showGraphic(VIEW_GRAPHIC_BY_WEEK)" name="radiobuttons" id="byWeek"> &nbsp; По неделям
            <br><br>

        </div>
        <div id="emptyMessage"></div>
        <div style="position:relative;overflow: scroll" class="Gantt" id="graphic_div"> </div>
    </div>
</div>

</body>
</html>



