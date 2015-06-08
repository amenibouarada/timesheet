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

    <script type="text/javascript">
        dojo.require("dojo.parser");
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dijit.layout.ContentPane");

        //var monthReportStore = {};
        var projectListWithOwnerDivision = ${projectListWithOwnerDivision};
        var managerMapJson = ${managerList};

    </script>

</head>
<body>
<h1>Табель</h1>
<form:form method="post" commandName="<%= FORM %>">
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>"/>
    <%--<form:hidden path="<%= JSON_DATA %>"/>--%>


    <%@include file="../components/queryParams/queryParams.jsp" %>

    <button id="show" style="width:150px;vertical-align: middle;" type="submit">Показать</button>
</form:form>

<button id="exportExcel">Экспорт в Эксель</button>

<br>

    <div data-dojo-type="dijit/layout/TabContainer" doLayout="false" id="tabContainer">
        <div id="firstTab" data-dojo-type="dijit/layout/ContentPane" title="Табель" data-dojo-props="selected:true">
            <%@include file="monthReportTable.jsp" %>
        </div>
        <div id="secondTab" data-dojo-type="dijit/layout/ContentPane" title="Переработки">
            <%@include file="overtimeTable.jsp" %>
        </div>
    </div>


<c:if test="${here != null}">
    ${here}
</c:if>

<%@include file="../components/addEmployees/addEmployeesForm.jsp" %>

</body>
</html>