<%@ page import="static com.aplana.timesheet.form.BusinessTripsAndIllnessForm.*" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title><fmt:message key="businesstripsandillness"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/businesstripsandillness.css">
    <script type="text/javascript">

        var employeeList = ${employeeListJson};
        var forAll = ${forAll};
        var selectedAllRegion = null;
        var empId = ${employeeId};

        dojo.ready(function () {
            window.focus();

            dojo.byId("divisionId").value = ${divisionId};
            divisionChanged(dojo.byId("divisionId").value);
            updateManagerList(dojo.byId('divisionId').value);
            dojo.byId("employeeId").value = ${employeeId};
            dojo.byId("manager").value = ${managerId};

            initRegionsList();

            if (dojo.byId("regions").value != -1) {
                sortEmployee();
                selectedAllRegion = false;
            } else {
                sortEmployeeFull();
                selectedAllRegion = true;
            }
        });

        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass:"dijit.Calendar",
            datePattern: 'dd.MM.yyyy'
        });

        dojo.require("dijit.form.DateTextBox");
        dojo.require("dojo.NodeList-traverse");

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

            empId = dojo.byId("employeeId").value;
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

        function createBusinessTripOrIllness() {
            var empId = dojo.byId("employeeId").value;

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

        function updateManagerList(id) {
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
                    return (m.division == id);
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
                dojo.byId("manager").disabled = 'disabled';
            } else {
                dojo.byId("manager").disabled = '';
            }
        }

        function updateMultipleForSelect(select) {
            var allOptionIndex;
            var isAllOption = dojo.some(select.options, function (option, idx) {
                if (option.value == <%= ALL_VALUE %> && option.selected) {
                    allOptionIndex = idx;
                    return true;
                }
                return false;
            });

            if (isAllOption) {
                select.removeAttribute("multiple");
                select.selectedIndex = allOptionIndex;
                selectedAllRegion = true;
                sortEmployeeFull();
            } else {
                select.setAttribute("multiple", "multiple");
                selectedAllRegion = false;
                sortEmployee();
            }
        }

        function getSelectedIndexes (multiselect)
        {
            var arrIndexes = new Array;
            for (var i=0; i < multiselect.options.length; i++)
            {
                if (multiselect.options[i].selected) arrIndexes.push(i);
            }
            return arrIndexes;
        }

      function sortEmployeeFull() {
          var manager = dojo.byId("manager").value;
          var employeeSelect = dojo.byId("employeeId");
          var divisionSelectValue = dojo.byId("divisionId").value;
          var employeeOption = null;
          employeeSelect.options.length = 0;
          if (employeeList.length > 0) {
              dojo.forEach(dojo.filter(employeeList,function (m) {
                  return (m.divId == divisionSelectValue);
              }), function (divEmps) {
                  dojo.forEach(divEmps.divEmps, function (empData) {
                      if (empData.manId == manager || manager == 0) {
                          addEmployeeToList(empData, employeeOption, employeeSelect,null, divisionSelectValue);
                      } else if (manager == -1) {
                          employeeOption = dojo.doc.createElement("option");
                          dojo.attr(employeeOption, {
                              value:empData.id
                          });
                          employeeOption.title = empData.value;
                          employeeOption.innerHTML = empData.value;
                          employeeSelect.appendChild(employeeOption);
                      }
                  })
              });
          }
          sortSelect(employeeSelect);
          if (selectCurrentEmployee(employeeSelect)) {
              dojo.byId("employeeId").value = empId;
          } else {
              dojo.byId("employeeId").value = -1;
          }
      }

        function managerChange(obj) {
            var managerId = null;

            if (obj.target == null) {
                managerId = obj.value;
            }
            else {
                managerId = obj.target.value;
            }
            if (selectedAllRegion) {
                sortEmployeeFull();

            } else {
                sortEmployee();
            }
        }


        function sortEmployee() {
            var employeeSelect = dojo.byId("employeeId");
            var divisionId = dojo.byId("divisionId").value;
            var employeeOption = null;
            var select = dojo.byId("regions");
            var managerId = dojo.byId("manager").value;
            var selectedRegions = [];
            for (var i = 0; i < select.options.length; i++) {
                var option = select.options[i];

                if (option.selected) selectedRegions.push(option.value);
            }
            employeeSelect.options.length = 0;
            for (var i = 0; i < employeeList.length; i++) {
                if (divisionId == employeeList[i].divId) {
                    for (var l = 0; l < employeeList[i].divEmps.length; l++) {
                        if (employeeList[i].divEmps[l].id != 0) {
                            if (managerId != 0 && ((employeeList[i].divEmps[l].manId == managerId) || (managerId == 0))) {
                                addEmployeeToList(employeeList[i].divEmps[l], employeeOption, employeeSelect, selectedRegions,divisionId);
                            } else if (managerId == -1 && (dojo.indexOf(selectedRegions, employeeList[i].divEmps[l].regId) != -1)) {
                                employeeOption = dojo.doc.createElement("option");
                                dojo.attr(employeeOption, {
                                    value:employeeList[i].divEmps[l].id
                                });
                                employeeOption.title = employeeList[i].divEmps[l].value;
                                employeeOption.innerHTML = employeeList[i].divEmps[l].value;
                                employeeSelect.appendChild(employeeOption);
                            }
                        }
                    }
                }
            }
            sortSelect(employeeSelect);
            if (selectCurrentEmployee(employeeSelect)) {
                dojo.byId("employeeId").value = empId;
            } else {
                dojo.byId("employeeId").value = -1;
            }
        }

        function addEmployeeToList(employee, employeeOption, employeeSelect, selectedRegions ,divisionSV) {
            var addEmployee = true;

            // Если есть список выбранных регионов - перед добавлением в список проверим, что данный сотрудник в этом регионе
            if (selectedRegions) {
                addEmployee = (dojo.indexOf(selectedRegions, employee.regId) != -1);
            }

            if (addEmployee || selectedRegions == null) {
                employeeOption = dojo.doc.createElement("option");
                dojo.attr(employeeOption, {
                    value:employee.id
                });
                employeeOption.title = employee.value;
                employeeOption.innerHTML = employee.value;
                employeeSelect.appendChild(employeeOption);
            }

            var filteredEmpMap3 =  dojo.map(employeeList,function (item) {
                if (item.divId==divisionSV) {
                    dojo.map(item.divEmps,function (itemm) {
                        if (itemm.manId == employee.id) {
                            addEmployeeToList(itemm, employeeOption, employeeSelect,selectedRegions, divisionSV);
                        }
                    });
                }
            })
        }

        /* Сортирует по алфавиту содержимое выпадающих списков. */
        function sortSelect(select) {
            var tmpArray = [];
            for (var i = 0; i < select.options.length; i++) {
                tmpArray.push(select.options[i]);
            }
            tmpArray.sort(function (a, b) {
                return (a.text < b.text) ? -1 : 1;
            });
            select.options.length = 0;
            insertAllInclusiveOption(select);
            for (var i = 0; i < tmpArray.length; i++) {
                select.options[i + 1] = tmpArray[i];
            }
        }

        /* Добавляет в указанный select пустой option. */
        function insertAllInclusiveOption(select) {
            var option = dojo.doc.createElement("option");
            dojo.attr(option, {
                value:"-1"
            });
            option.innerHTML = "Все сотрудники";
            select.appendChild(option);
        }


        function selectCurrentEmployee(employeeSelect) {
            for (var i = 0; i < employeeSelect.options.length; i++) {
                if (employeeSelect[i].value == empId) {
                    return true;
                }
            }
            return false;
        }

        function divisionChanged(division) {
            updateManagerList(division);
            dojo.byId("manager").onchange();
        }

    function log(text){
        console.log(text);
    }
    </script>
</head>
<body>
    <h1><fmt:message key="businesstripsandillness"/></h1>
    <br>
    <form:form method="post" commandName="businesstripsandillness" name="mainForm">
    <table class="no_border" style="margin-bottom: 20px;">
        <tr>
            <td>
                <span class="lowspace">Подразделение:</span>
            </td>
            <td>
                <form:select class="without_dojo" path="divisionId" id="divisionId"
                             onchange="divisionChanged(this.value);updateManagerList(this.value)"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td rowspan="5" style="padding: 9px;vertical-align: top;">
                <span class="lowspace">Регионы:</span>
            </td>
            <td rowspan="5" style="padding: 5px;vertical-align: top;">
                <form:select path="regions" onmouseover="showTooltip(this)" size="6"
                             onmouseout="tooltip.hide()" multiple="true"
                             onchange="updateMultipleForSelect(this)">
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
                <form:select class="without_dojo" path="manager"
                             onchange="managerChange(this)" onmouseover="showTooltip(this);"
                             onmouseout="tooltip.hide();" multiple="false" cssStyle="margin-left: 0px">
                    <%--<form:option label="Все руководители" value="0"/>--%>
                    <form:options items="${managerList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace ">Сотрудник:</span>
            </td>
            <td>
                <form:select class="without_dojo" path="employeeId" id="employeeId"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                </form:select>
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