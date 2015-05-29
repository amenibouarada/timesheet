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

    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/commonJS/addEmployeesForm.js"></script>

    <script type="text/javascript">
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojo.data.ItemFileWriteStore");

        var data = {
            identifier: 'id',
            items: []
        };
        var data_list = [
            { employee: "normal", division: false, region: 'But are not followed by two hexadecimal', type: 29.91,
                prType: "normal", overtime: false, premium: 'But are not followed by two hexadecimal', allAccountedOvertime: 29.91,
                comment: "comment"}
        ];
        var rows = 1;
        for(var i=0, l=data_list.length; i<rows; i++){
            data.items.push(dojo.mixin({ id: i+1 }, data_list[i%l]));
        }
        var store = new dojo.data.ItemFileWriteStore({data: data});
        var overtimeStore = store;

        function addRow(){
                     alert("Text");
        }

    </script>

</head>
<body>
<h1>Табель</h1>
<form:form method="post" commandName="<%= FORM %>">
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>"/>
    <%--<form:hidden path="<%= JSON_DATA %>"/>--%>


    <%@include file="queryParams.jsp" %>

    <button id="show" style="width:150px;vertical-align: middle;" type="submit">Показать</button>

    <%@include file="overtimeTable.jsp" %>
</form:form>

<c:if test="${here != null}">
    ${here}
</c:if>

<%@include file="addEmployeesForm.jsp" %>

</body>
</html>