<%@ page import="static com.aplana.timesheet.form.BusinessTripsAndIllnessForm.*" %>
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
    <script type="text/javascript">

        dojo.require("dijit.form.DateTextBox");
        dojo.require("dojo.NodeList-traverse");
        dojo.require("dijit.form.FilteringSelect");
        dojo.require("dojo.data.ObjectStore");
        dojo.require("dojo.store.Memory");

        var employeeList = ${employeeListJson};
        var forAll = ${forAll};
        var selectedAllRegion = null;
        var empId = ${employeeId};

        dojo.ready(function () {
            window.focus();
            dojo.connect(dojo.byId("divisionId"), "onchange", dojo.byId("divisionId"), updateManagerList);
            dojo.connect(dojo.byId("manager"), "onchange", dojo.byId("manager"), updateEmployeeList);
            dojo.connect(dojo.byId("regions"), "onchange", dojo.byId("regions"), updateEmployeeList);

            dojo.byId("divisionId").value = ${divisionId};

            updateManagerList();
            initRegionsList();
            updateEmployeeList();

            if (dojo.byId("regions").value != -1) {
                sortEmployee();
                selectedAllRegion = false;
            } else {
                selectedAllRegion = true;
            }
        });

        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass:"dijit.Calendar",
            datePattern: 'dd.MM.yyyy'
        });

        //устанавливается значение по умолчанию "Все регионы"
        function initRegionsList(){
            var regions = ${regionIds};
            var regionsSelect = dojo.byId("regions");
            if (regions.length == 1) {
                if (regions[0] == <%= ALL_VALUE %>) {
                    regionsSelect[0].selected = true;
                    selectedAllRegion = true;
                } else {
                    selectedAllRegion = false;
                }
            }

        }

        function showBusinessTripsAndIllnessReport() {
            var divisionId = ${divisionId};
            var regions = dojo.byId("regions").value;
            var manager = dojo.byId("manager").value;

            empId =  getEmployeeId();
            var divisionId = dojo.byId("divisionId").value;

            var dateFrom = dojo.byId("dateFrom").value;
            var dateTo = dojo.byId("dateTo").value

            var regionsValid = getSelectedIndexes(dojo.byId("regions")).length > 0;

            var datesValid = (dateFrom != null && dateFrom != undefined && dateFrom != "")&& (dateTo != null && dateTo != undefined && dateTo != "") && (dateFrom <= dateFrom);

            if (datesValid && divisionId != null && divisionId != 0 && empId != null && empId != 0 && regionsValid) {
                businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillness/"
                        + divisionId + "/" + empId;
                businesstripsandillness.submit();
            } else {
                var error = "";
               if (dateFrom == null || dateFrom == undefined || dateFrom == "") {
                    error += ("Необходимо выбрать дату начало периода!\n");
                }

                 if (dateTo == null || dateTo == undefined || dateTo == "") {
                    error += ("Необходимо выбрать дату окончания периода!\n");
                }

                if (dateFrom > dateTo) {
                    error += ("Дата окончания периода должна быть больше даты начала периода!\n");
                }

                if (divisionId == 0 || divisionId == null) {
                    error += ("Необходимо выбрать подразделение и сотрудника!\n");
                }
                else if (empId == 0 || empId == null) {
                    error += ("Необходимо выбрать сотрудника!\n");
                }
                if (!regionsValid) {
                    error += ("Необходимо выбрать регион или несколько регионов!\n");
                }

                alert(error);

            }
        }

        function getEmployeeId() {
            return dijit.byId("employeeIdDiv").item != undefined ? dijit.byId("employeeIdDiv").item.id : null;
        }

        function createBusinessTripOrIllness() {
            var empId = getEmployeeId();

            if (empId != null && empId != 0) {
                <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/" + empId;
                businesstripsandillness.submit();
                </sec:authorize>
            } else {
                alert("Необходимо выбрать сотрудника!\n");
            }
        }

        function deleteReport(parentElement, rep_id, calendarDays, workingDays, workDaysOnIllnessWorked){
            if (!confirm("Подтвердите удаление!")) {
                return;
            }

            var prevHtml = parentElement.innerHTML;

            dojo.addClass(parentElement, "activity-indicator");
            parentElement.innerHTML =
                    "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

            function handleError(error) {
                resetParent();

                alert("Удаление не произошло:\n\n" + error);
            }


            function resetParent() {
                dojo.removeClass(parentElement, "activity-indicator");
                parentElement.innerHTML = prevHtml;
            }

            dojo.xhrGet({
                url: "<%= request.getContextPath()%>/businesstripsandillness/delete/" + rep_id + "/" + ${reportFormed},
                handleAs: "text",

                load: function(data) {
                    if (data.length == 0) {
                        dojo.destroy(dojo.NodeList(parentElement).parents("tr")[0]);
                        recountResults(calendarDays, workingDays, workDaysOnIllnessWorked);
                    } else {
                        handleError(data);
                    }
                },

                error: function(error) {
                    handleError(error.message);
                }
            });
        }
        function recountResults(calendarDays, workingDays, workDaysOnIllnessWorked){
            if (${reportFormed == 6}){
                recountIllness(calendarDays, workingDays, workDaysOnIllnessWorked);
            }
            if (${reportFormed == 7}){
                if(!forAll){
                    decreaseResultDays(document.getElementById("mounthCalendarDaysInBusinessTrip"), calendarDays);
                    decreaseResultDays(document.getElementById("mounthWorkDaysOnBusinessTrip"), workingDays);
                }
            }
        }
        function decreaseResultDays(cellWithResults, daysToDecrease){
            var daysInTable = parseFloat(cellWithResults.innerHTML);
            var recountedDays = daysInTable - daysToDecrease;
            cellWithResults.innerHTML = recountedDays;
        }
        function recountIllness(calendarDays, workingDays, workDaysOnIllnessWorked){
            if(!forAll){
              if (${fn:length(reports.periodicalsList) > 0}){
                  decreaseResultDays(document.getElementById("mounthCalendarDaysOnIllness"), calendarDays);
                  decreaseResultDays(document.getElementById("mounthWorkDaysOnIllness"), workingDays);
                  decreaseResultDays(document.getElementById("mounthWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
              }
             decreaseResultDays(document.getElementById("yearWorkDaysOnIllness"), workingDays);
             decreaseResultDays(document.getElementById("yearWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
            }
        }
        function editReport(reportId){
            businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/" + reportId + "/" + ${reportFormed};
            businesstripsandillness.submit();
        }

        function updateManagerList() {
            var divisionId = dojo.byId("divisionId").value;
            var managersNode = dojo.byId("manager");
            var manager = managersNode.value;

            var emptyOption = dojo.doc.createElement("option");
            dojo.attr(emptyOption, {
                value:-1
            });
            emptyOption.title = "Все руководители";
            emptyOption.innerHTML = "Все руководители";

            managersNode.options.length = 0;
            managersNode.appendChild(emptyOption);

            var managerMapJson = '${managerMapJson}';
            if (managerMapJson.length > 0) {
                var managerMap = dojo.fromJson(managerMapJson);
                dojo.forEach(dojo.filter(managerMap,function (m) {
                    return (m.division == divisionId);
                }), function (managerData) {
                    var option = document.createElement("option");
                    dojo.attr(option, {
                        value:managerData.id
                    });
                    option.title = managerData.name;
                    option.innerHTML = managerData.name;
                    managersNode.appendChild(option);
                });
            }
            if (managersNode.options.length == 1 && emptyOption.value == managersNode.options[0].value){
                managersNode.disabled = 'disabled';
            } else {
                managersNode.disabled = '';
            }
            managersNode.value = "${managerId}" != "" ? +"${managerId}" : null;
            updateEmployeeList();
        }

        function getSelectedIndexes(multiselect)
        {
            var arrIndexes = new Array;
            for (var i=0; i < multiselect.options.length; i++)
            {
                if (multiselect.options[i].selected) arrIndexes.push(i);
            }
            return arrIndexes;
        }

        function updateEmployeeList() {
            var divisionId =  dojo.byId('divisionId').value;
            var managerId =  dojo.byId('manager').value;
            var cities = [];
            var citySelect = dojo.byId('regions').options;

            for (var i = 0; i < citySelect.length; i++) {
                if ( citySelect[i].selected) cities.push(citySelect[i].value);
            }

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

                        var manegerEquals = (managerId == -1 || employee.manId == managerId);
                        var regionEquals = (cities.length == 1 && cities[0] == -1) || (dojo.indexOf(cities, +employee.regId) == 0) ;
                        if (manegerEquals && regionEquals && !employee.lastWorkDate) {
                            employeeArray.push(employee);
                        }
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

                    var employeeFlteringSelect = dijit.byId("employeeIdDiv");

                    if (!employeeFlteringSelect) {
                        employeeFlteringSelect = new dijit.form.FilteringSelect({
                            id: "employeeIdDiv",
                            name: "employeeIdDiv",
                            labelAttr: "value",
                            store: employeeDataStore,
                            searchAttr: 'value',
                            queryExpr: "*\${0}*",
                            ignoreCase: true,
                            autoComplete: false,
                            style: 'width:200px',
                            required: true,
                            onChange: function () {
                                var selectedEmploye2 = this.item ? this.item.id : null;
                                dojo.byId('employeeId').value = selectedEmploye2;

                            },
                            onMouseOver: function() {
                                tooltip.show(getTitle(this));
                            },
                            onMouseOut: function() {
                                tooltip.hide();
                            }
                        }, "employeeIdDiv");
                        employeeFlteringSelect.startup();
                    } else {
                        employeeFlteringSelect.set('store', employeeDataStore);
                        dijit.byId("employeeIdDiv").set('value', null);
                        dojo.byId('employeeId').value = null;
                    }
                });
            }

            dijit.byId("employeeIdDiv").set('value', " ${employeeId}" != "" ? +" ${employeeId}" : null);
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