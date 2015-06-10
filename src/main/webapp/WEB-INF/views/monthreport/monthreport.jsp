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

        //var ALL_VALUE = ALL_VALUE; // присвоение глобального значения из selectWidgetValues.js

        //var monthReportStore = {};
        var projectListWithOwnerDivision = ${projectListWithOwnerDivision};
        var managerMapJson = ${managerList};

        var eventConnections = [];
        dojo.addOnLoad(function(){
            // установим год/месяц по умолчанию
            var currentDate = new Date();
            dojo.byId("monthreport_year").value = currentDate.getFullYear();
            dojo.byId("monthreport_month").value = currentDate.getMonth() + 1;

            // назначим слушителей переключения табов
            tabContainer.watch("selectedChildWidget",
                // функция для переназначения обработчиков нажатия кнопок
                function changeButtonListeners(){
                    dojo.forEach(eventConnections, dojo.disconnect);
                    eventConnections = [];
                    if (dijit.byId('tabContainer').selectedChildWidget.id == "monthReportTable_tab"){
                        eventConnections.push(dojo.connect(showButton,   "onclick", function(){}));
                        eventConnections.push(dojo.connect(saveButton,   "onclick", function(){}));
                        eventConnections.push(dojo.connect(exportButton, "onclick", function(){}));
                    }
                    if (dijit.byId('tabContainer').selectedChildWidget.id == "overtimeTable_tab"){
                        eventConnections.push(dojo.connect(showButton,   "onclick", function(){overtimeTable_reloadTable()}));
                        eventConnections.push(dojo.connect(saveButton,   "onclick", function(){overtimeTable_save()}));
                        eventConnections.push(dojo.connect(exportButton, "onclick", function(){}));
                    }
                    if (dijit.byId('tabContainer').selectedChildWidget.id == "mutualWorkTable_tab"){
                        eventConnections.push(dojo.connect(showButton,   "onclick", function(){}));
                        eventConnections.push(dojo.connect(saveButton,   "onclick", function(){}));
                        eventConnections.push(dojo.connect(exportButton, "onclick", function(){}));
                    }
                }
            );
        });

    </script>

</head>
<body>
<h1>Табель</h1>

<%@include file="../components/queryParams/queryParams.jsp" %>
<br>
<button id="showButton"     data-dojo-id="showButton"     >Показать</button>
<button id="saveButton"     data-dojo-id="saveButton"     >Сохранить</button>
<button id="exportButton"   data-dojo-id="exportButton"   >Экспорт в Эксель</button>
<br>
<br>
    <div data-dojo-id="tabContainer" data-dojo-type="dijit/layout/TabContainer" doLayout="false" id="tabContainer">
        <div id="monthReportTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Табель" data-dojo-props="selected:true">
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