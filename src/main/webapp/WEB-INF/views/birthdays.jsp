<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="static com.aplana.timesheet.util.DateTimeUtil.*" %>
<%@ page import="com.aplana.timesheet.dao.entity.Employee" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<head>
    <title>
        <fmt:message key="title.birthdays"/>
    </title>
    <script type="text/javascript">
        dojo.addOnLoad(function () {
            dojo.byId("month").value = "${currentMonth}";
            dojo.byId("divisionId").value = "${currentDivision}";
        });

        function getBirthdays() {
            var divisionId = dojo.byId("divisionId").value;
            var month = dojo.byId("month").value;
            window.location = "<%=request.getContextPath()%>/birthdays/" + divisionId + "/" + month;
        }
    </script>
</head>
<body>
    <h1><fmt:message key="title.birthdays"/></h1>
    <br><br>

    <span class="lowspace">Месяц:</span>
    <select id="month" name="month" onmouseover="tooltip.show(getTitle(this));"
            onmouseout="tooltip.hide();" class="without_dojo">
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

    <span class="lowspace">Подразделение:</span>
    <select  path="divisionId" id="divisionId" onmouseover="tooltip.show(getTitle(this));"
         onmouseout="tooltip.hide();" class="without_dojo">
        <c:forEach items="${divisionList}" var="division">
            <option value="${division.id}" title="${division.name}">${division.name}</option>
        </c:forEach>
    </select>

    <br><br>

    <button onclick="getBirthdays()">Показать</button>

    <br><br>

    <c:if test="${ fn:length(employeesForSelectedMonth) == 0}">
        По заданным параметрам записи отсутствуют
    </c:if>
    <c:if test="${ fn:length(employeesForSelectedMonth) > 0}">
        <table>
            <thead>
            <tr>
                <th width="200" height="30">Сотрудник</th>
                <th>День рождения</th>
            </tr>
            </thead>
            <tbody>
                <c:forEach items="${employeesForSelectedMonth}" var="employee">
                    <tr height="30">
                        <td>${employee.name}</td>
                        <td><%=getDayMonthFromDate(((Employee)(pageContext.getAttribute("employee"))).getBirthday())%>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:if>
</body>
</html>