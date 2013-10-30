<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <script type="text/javascript">
        dojo.ready(function () {
            window.focus();
        });
    </script>
    <title><fmt:message key="report"/></title>
</head>
<body>

<h1><fmt:message key="title.timesheetwithdate">
    <fmt:param value="${day}"/>
    <fmt:param>
        <fmt:message key="month[${month}]"/>
    </fmt:param>
    <fmt:param value="${year}"/>
</fmt:message></h1>

<br/>

<form:form method="post" commandName="ReportForm" name="mainForm">

    <c:if test="${fn:length(errors) > 0}">
        <div class="errors_box">
            <c:forEach items="${errors}" var="error">
                <fmt:message key="${error.code}">
                    <fmt:param value="${error.arguments[0]}"/>
                </fmt:message><br/>
            </c:forEach>
        </div>
    </c:if>
    <br/>
    ${report}
    <c:if test="${fn:length(creationDate) > 0}">
        <br/>
        <b>Отчет был создан ${creationDate} мск</b>
        <br/>
    </c:if>
    <button id="close" style="width:210px" type="button" onclick="window.close()">Закрыть</button>
</form:form>
</body>
</html>