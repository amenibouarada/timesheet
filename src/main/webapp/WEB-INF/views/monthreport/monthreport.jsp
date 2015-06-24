<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.enums.MonthReportStatusEnum.*" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

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

    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/monthreport.js"></script>

    <script type="text/javascript">
        dojo.require("dojo.parser");
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dijit.layout.ContentPane");

        var statusList = {
            open:       {id: <%=OPEN.getId()%>,         name: '<%=OPEN.getName()%>'},
            closed:     {id: <%=CLOSED.getId()%>,       name: '<%=CLOSED.getName()%>'},
            inWork:     {id: <%=IN_WORK.getId()%>,      name: '<%=IN_WORK.getName()%>'},
            notCreated: {id: <%=NOT_CREATED.getId()%>,  name: '<%=NOT_CREATED.getName()%>'}
        };

        function monthreport_getStatusById(/* int */ id){
            if (statusList.open.id == id){ return statusList.open; }
            if (statusList.closed.id == id){ return statusList.closed; }
            if (statusList.inWork.id == id){ return statusList.inWork; }
            if (statusList.notCreated.id == id){ return statusList.notCreated; }
        }

        var projectListWithOwnerDivision = ${projectListWithOwnerDivision};
        var managerMapJson = ${managerList};

        <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
        function makeReport(tabNum) {
            processing();
            var year = dojo.byId("monthreport_year").value;
            var month = dojo.byId("monthreport_month").value;
            var divisionOwner = dojo.byId("overtimeTable_divisionOwnerId").value;
            var divisionEmployee = dojo.byId("overtimeTable_divisionEmployeeId").value;

            var url;
            var content;
            switch (tabNum) {
                case 1:
                    url = '<%= request.getContextPath()%>/monthreport/makeMonthReport';
                    content = {
                        division: monthReportTable_divisionId.value,
                        manager: monthReportTable_managerId.value,
                        regions: "[" + getSelectValues(monthReportTable_regionListId) + "]",
                        roles: "[" + getSelectValues(monthReportTable_projectRoleListId) + "]",
                        year: year,
                        month: month
                    };
                    break;
                case 2:
                    url = '<%= request.getContextPath()%>/monthreport/makeOvertimeReport';
                    content = {
                            year: year,
                            month: month,
                            divisionOwner: divisionOwner,
                            divisionEmployee: divisionEmployee
                    };
                    break;
                case 3:
                    url = '<%= request.getContextPath()%>/monthreport/makeMutualWorkReport';
                    content = {
                        divisionOwner: divisionOwner,
                        divisionEmployee: divisionEmployee,
                        projectId: dojo.byId("mutualWorkTable_projectId").value,
                        regions: "[" + getSelectValues(mutualWorkTable_regionListId) + "]",
                        year: year,
                        month: month
                    };
                    break;
            }
            dojo.xhrPost({
                url:        url,
                handleAs:   "text",
                content:    content,
                preventCache: false,
                load: function (response, ioargs) {
                    stopProcessing();
                    if (response == ""){
                        window.location.href = ioargs.xhr.getResponseHeader('Location');
                    }else{
                        alert(response);
                    }
                },
                error: function () {
                    stopProcessing();
                    alert("Во время формирования отчёта произошла ошибка. Обратитесь к системным администраторам.");
                }
            });

        }
        </sec:authorize>

        var eventConnections = [];
        dojo.addOnLoad(function () {
            // установим год/месяц по умолчанию
            var currentDate = new Date();
            dojo.byId("monthreport_year").value = currentDate.getFullYear();
            dojo.byId("monthreport_month").value = currentDate.getMonth();
            monthReport_updateStatus();

            // функция для переназначения обработчиков нажатия кнопок
            // и отображения актуальных данных
            var changeButtonListeners = function(){
                dojo.forEach(eventConnections, dojo.disconnect);
                eventConnections = [];
                if (dijit.byId('tabContainer').selectedChildWidget.id == "monthReportTable_tab"){
                    monthReportTable_reloadTable();
                    eventConnections.push(dojo.connect(monthreport_year,  "onchange", function(){ monthReportTable_reloadTable()}));
                    eventConnections.push(dojo.connect(monthreport_month, "onchange", function(){ monthReportTable_reloadTable()}));
                    <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
                        eventConnections.push(dojo.connect(monthReport_saveButton,   "onclick", function(){ monthReportTable_save()}));
                        eventConnections.push(dojo.connect(monthReport_exportButton, "onclick", function(){makeReport(1)}));
                    </sec:authorize>
                }
                if (dijit.byId('tabContainer').selectedChildWidget.id == "overtimeTable_tab"){
                    overtimeTable_reloadTable();
                    eventConnections.push(dojo.connect(monthreport_year,  "onchange", function(){overtimeTable_reloadTable()}));
                    eventConnections.push(dojo.connect(monthreport_month, "onchange", function(){overtimeTable_reloadTable()}));
                    <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
                        eventConnections.push(dojo.connect(monthReport_saveButton,   "onclick", function(){overtimeTable_save()}));
                        eventConnections.push(dojo.connect(monthReport_exportButton, "onclick", function(){makeReport(2)}));
                    </sec:authorize>
                }
                <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
                if (dijit.byId('tabContainer').selectedChildWidget.id == "mutualWorkTable_tab"){
                        mutualWorkTable_reloadTable();
                        eventConnections.push(dojo.connect(monthreport_year,  "onchange", function(){mutualWorkTable_reloadTable()}));
                        eventConnections.push(dojo.connect(monthreport_month, "onchange", function(){mutualWorkTable_reloadTable()}));
                        eventConnections.push(dojo.connect(monthReport_saveButton,   "onclick", function(){mutualWorkTable_save()}));
                        eventConnections.push(dojo.connect(monthReport_exportButton, "onclick", function(){makeReport(3)}));
                }
                </sec:authorize>
            }
            // назначим слушителей переключения табов
            tabContainer.watch("selectedChildWidget", changeButtonListeners);
            changeButtonListeners(); // выполним, чтобы загрузить слушателей для первой вкладки
        });

        function monthReport_updateStatus(){
            dojo.xhrPost({
                url:        "<%= request.getContextPath()%>/monthreport/getStatus",
                handleAs:   "text",
                content:    {
                                year: monthreport_year.value,
                                month: monthreport_month.value
                            },
                preventCache: false,
                load: function (response, ioargs) {
                    var status = response;
                    monthReportStatus.innerHTML = status != "" ? monthreport_getStatusById(status).name : "не удалось получить статус";
                    if (status == statusList.closed.id){
                        monthReport_openButton.style.visibility = "hidden";
                        monthReport_saveButton.style.visibility = "hidden";

                        // ToDo добавить про редактирование ячеек
                    }else if(status == statusList.notCreated.id){
                        monthReport_closeButton.style.visibility = "hidden";
                        monthReport_saveButton.style.visibility  = "visible";
                    }else{
                        monthReport_closeButton.style.visibility = "visible";
                        monthReport_saveButton.style.visibility  = "visible";
                    }
                },
                error: function () {
                    monthReportStatus.innerHTML = "не удалось получить статус";
                }
            });
        }

        <sec:authorize access="hasRole('ROLE_MONTH_REPORT_MANAGER')">
        function monthReport_changeStatus(url){
            dojo.xhrPost({
                url:        url,
                handleAs:   "text",
                content:    {
                    year: monthreport_year.value,
                    month: monthreport_month.value
                },
                preventCache: false,
                load: function (response, ioargs) {
                    alert(response);
                    monthReport_updateStatus();
                },
                error: function () {
                    alert(response);
                }
            });
        }
        function monthReport_close(){
            monthReport_changeStatus("<%= request.getContextPath()%>/monthreport/closeMonthReport");
        }
        function monthReport_open(){
            monthReport_changeStatus("<%= request.getContextPath()%>/monthreport/openMonthReport");
        }
        </sec:authorize>
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
            <select data-dojo-id="monthreport_year" id="monthreport_year" onchange="monthReport_updateStatus();">
                <option value="2015" label="2015">2015</option>
                <option value="2016" label="2016">2016</option>
            </select>
        </td>

        <td>
            <span class="label">Месяц:</span>
        </td>
        <td>
            <%--// ToDo сделать отдельный файл для формирования выпадашки с месяцами--%>
            <select data-dojo-id="monthreport_month" id="monthreport_month" onchange="monthReport_updateStatus();">
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
        </td>
        <td>
            <span class="label">Статус:</span>
        </td>
        <td>
            <div id="monthReportStatus" data-dojo-id="monthReportStatus" style="font-weight: bold"></div>
        </td>
        <sec:authorize access="hasRole('ROLE_MONTH_REPORT_MANAGER')">
        <td>
            <button data-dojo-id="monthReport_closeButton" id="monthReport_closeButton" style="margin-left: 15px; visibility: hidden;"
                    onclick="monthReport_close();">Закрыть табель</button>
            <button data-dojo-id="monthReport_openButton" id="monthReport_openButton"  style="margin-left: 15px; visibility: hidden;"
                    onclick="monthReport_open();">Открыть табель</button>
        </td>
        </sec:authorize>

    </tr>
</table>
<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
    <br>
    <button id="monthReport_saveButton"     data-dojo-id="monthReport_saveButton"     >Сохранить</button>
    <button id="monthReport_exportButton"   data-dojo-id="monthReport_exportButton"   >Экспорт в Эксель</button>
    <br>
</sec:authorize>
<br>
    <div data-dojo-id="tabContainer" data-dojo-type="dijit/layout/TabContainer" doLayout="false" id="tabContainer">
        <div id="monthReportTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Табель" data-dojo-props="selected:true">
            <%@include file="monthReportTable.jsp" %>
        </div>
        <div id="overtimeTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Переработки">
            <%@include file="overtimeTable.jsp" %>
        </div>
        <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
            <div id="mutualWorkTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Взаимная занятость">
                <%@include file="mutualWorkTable.jsp" %>
            </div>
        </sec:authorize>
    </div>

</body>
</html>