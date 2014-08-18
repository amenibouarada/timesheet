<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="menu.manager"/></title>
    <script>
        function openUrl(link) {
            if (dojo.isIE <= 8) {
                window.location = link;
            } else {
                window.open(link, '_blank');
            }
        }
    </script>
</head>
<body>

<h1><fmt:message key="title.reportlist"/></h1>

<ul>
    <li><a href="#" onclick="openUrl('<c:url value='/managertools/report/1'/>');"><fmt:message key="title.report01"/></a></li>
    <li><a href="#" onclick="openUrl('<c:url value='/managertools/report/2'/>');"><fmt:message key="title.report02"/></a></li>
    <li><a href="#" onclick="openUrl('<c:url value='/managertools/report/3'/>');"><fmt:message key="title.report03"/></a></li>
    <li><a href="#" onclick="openUrl('<c:url value='/managertools/report/4'/>');"><fmt:message key="title.report04"/></a></li>
    <li><a href="#" onclick="openUrl('<c:url value='/managertools/report/5'/>');"><fmt:message key="title.report05"/></a></li>
    <li><a href="#" onclick="openUrl('<c:url value='/managertools/report/6'/>');"><fmt:message key="title.report06"/></a></li>
    <li><a href="#" onclick="openUrl('<c:url value='/managertools/report/7'/>');"><fmt:message key="title.report07"/></a></li>

    <c:if test="${pentahoUrl != null}">
        <li><a href="#" onclick="openUrl('${pentahoUrl}');">Отчеты в Pentaho</a></li>
    </c:if>
</ul>
</body>
</html>