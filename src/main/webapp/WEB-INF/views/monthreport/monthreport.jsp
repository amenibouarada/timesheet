<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="static com.aplana.timesheet.form.MonthReportForm.*" %>
<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<head>
    <title>Табель</title>

    <style type="text/css">
        /* REQUIRED STYLES!!! */
        @import "<%= DOJO_PATH %>/dojox/grid/resources/Grid.css";
        @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= getResRealPath("/resources/css/DataGrid.ext.css", application) %>";
        @import "<%= getResRealPath("/resources/css/planEdit.css", application) %>";
        @import "<%= getResRealPath("/resources/css/monthreport.css", application) %>";
    </style>

    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/addEmployeesForm.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/monthreport.js"></script>

    <script type="text/javascript">
        dojo.require("dojo.parser");
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dijit.layout.ContentPane");

        var projectListWithOwnerDivision = ${projectListWithOwnerDivision};
        var managerMapJson = ${managerList};

        function makeReport(tabNum) {
            var year = dojo.byId("monthreport_year").value;
            var month = dojo.byId("monthreport_month").value;
            var divisionOwner = dojo.byId("overtimeTable_divisionOwnerId").value;
            var divisionEmployee = dojo.byId("overtimeTable_divisionEmployeeId").value;

            switch (tabNum) {
                case 1:
                    dojo.xhrPost({
                        url: '<%= request.getContextPath()%>/monthreport/makeMonthReport',
                        handleAs: "text",
                        content: {
                            division: monthReportTable_divisionId.value,
                            manager: monthReportTable_managerId.value,
                            regions: "[" + getSelectValues(monthReportTable_regionListId) + "]",
                            roles: "[" + getSelectValues(monthReportTable_projectRoleListId) + "]",
                            year: year,
                            month: month
                        },
                        preventCache: false,
                        load: function (response, ioargs) {
                            window.location.href = ioargs.xhr.getResponseHeader('Location');
                        },
                        error: function () {
                            console.log('submitReportForm panic!');
                        }
                    });
                    break;
                case 2:
                    dojo.xhrPost({
                        url: '<%= request.getContextPath()%>/monthreport/makeOvertimeReport',
                        handleAs: "text",
                        content: {
                            year: year,
                            month: month,
                            divisionOwner: divisionOwner,
                            divisionEmployee: divisionEmployee
                        },
                        preventCache: false,
                        load: function (response, ioargs) {
                            window.location.href = "<%= request.getContextPath()%>" + ioargs.xhr.getResponseHeader('Location');
                        },
                        error: function () {
                            console.log('submitReportForm panic!');
                        }
                    });
                    break;
                case 3:
                    break;
            }

        }

        var eventConnections = [];
        dojo.addOnLoad(function () {
            // установим год/месяц по умолчанию
            var currentDate = new Date();
            dojo.byId("monthreport_year").value = currentDate.getFullYear();
            dojo.byId("monthreport_month").value = currentDate.getMonth();


            // функция для переназначения обработчиков нажатия кнопок
            // и отображения актуальных данных
            var changeButtonListeners = function(){
                dojo.forEach(eventConnections, dojo.disconnect);
                eventConnections = [];
                if (dijit.byId('tabContainer').selectedChildWidget.id == "monthReportTable_tab"){
                    monthReportTable_reloadTable();
                    eventConnections.push(dojo.connect(monthreport_year,  "onchange", function(){ monthReportTable_reloadTable()}));
                    eventConnections.push(dojo.connect(monthreport_month, "onchange", function(){ monthReportTable_reloadTable()}));
                    eventConnections.push(dojo.connect(saveButton,   "onclick", function(){ monthReportTable_save()}));
                    eventConnections.push(dojo.connect(exportButton, "onclick", function() {makeReport(1)}));
                }
                if (dijit.byId('tabContainer').selectedChildWidget.id == "overtimeTable_tab"){
                    overtimeTable_reloadTable();
                    eventConnections.push(dojo.connect(monthreport_year,  "onchange", function(){overtimeTable_reloadTable()}));
                    eventConnections.push(dojo.connect(monthreport_month, "onchange", function(){overtimeTable_reloadTable()}));
                    eventConnections.push(dojo.connect(saveButton,   "onclick", function(){overtimeTable_save()}));
                    eventConnections.push(dojo.connect(exportButton, "onclick", function() {makeReport(2)}));
                }
                if (dijit.byId('tabContainer').selectedChildWidget.id == "mutualWorkTable_tab"){
//                        overtimeTable_reloadTable();
//                        eventConnections.push(dojo.connect(monthreport_year,  "onchange", function(){overtimeTable_reloadTable()}));
//                        eventConnections.push(dojo.connect(monthreport_month, "onchange", function(){overtimeTable_reloadTable()}));

                    eventConnections.push(dojo.connect(saveButton,   "onclick", function(){}));
                    eventConnections.push(dojo.connect(exportButton, "onclick", function(){}));
                }
            }
            // назначим слушителей переключения табов
            tabContainer.watch("selectedChildWidget", changeButtonListeners);
            changeButtonListeners(); // выполним, чтобы загрузить слушателей для первой вкладки
        });

    </script>

</head>
<body>
<h1>Табель</h1>

<table>
    <tr>
        <td>
            <span class="label">Год:</span>
        </td>
        <td>
            <select data-dojo-id="monthreport_year" id="monthreport_year">
                <option value="2015" label="2015"/>
                <option value="2016" label="2016"/>
                <option value="2017" label="2017"/>
                <option value="2018" label="2018"/>
                <option value="2019" label="2019"/>
                <option value="2020" label="2020"/>
            </select>
        </td>

        <td>
            <span class="label">Месяц:</span>
        </td>
        <td>
            <%--// ToDo сделать отдельный файл для формирования выпадашки с месяцами--%>
            <select data-dojo-id="monthreport_month" id="monthreport_month">
                <option value="1" title="Январь">Январь</option>
                <option value="2" title="Февраль">Февраль</option>
                <option value="3" title="Март">Март</option>
                <option value="4" title="Апрель">Апрель</option>
                <option value="5" title="Май">Май</option>
                <option value="6" title="Июнь">Июнь</option>
                <option value="7" title="Июль">Июль</option>
                <option value="8" title="Август">Август</option>
                <option value="9" title="Сентябрь">Сентябрь</option>
                <option value="10" title="Октябрь">Октябрь</option>
                <option value="11" title="Ноябрь">Ноябрь</option>
                <option value="12" title="Декабрь">Декабрь</option>
            </select>
    </tr>
</table>
<br>
<button id="saveButton" data-dojo-id="saveButton">Сохранить</button>
<button id="exportButton" data-dojo-id="exportButton">Экспорт в Эксель</button>
<br>
<br>

<div data-dojo-id="tabContainer" data-dojo-type="dijit/layout/TabContainer" doLayout="false" id="tabContainer">
    <div id="monthReportTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Табель"
         data-dojo-props="selected:true">
        <%@include file="monthReportTable.jsp" %>
    </div>
    <div id="overtimeTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Переработки">
        <%@include file="overtimeTable.jsp" %>
    </div>
    <div id="mutualWorkTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Взаимная занятость">
        <%@include file="mutualWorkTable.jsp" %>
    </div>
</div>
<c:if test="${here != null}">
    ${here}
</c:if>

<%@include file="../components/addEmployees/addEmployeesForm.jsp" %>

</body>
</html>