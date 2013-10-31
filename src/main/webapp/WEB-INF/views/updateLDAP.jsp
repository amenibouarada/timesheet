<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
   <title><fmt:message key="title.adminpanel"/></title>
</head>
<body>

//Результаты синхронизации списка сотрудников с LDAP
<h1><fmt:message key="title.updateldap"/></h1>

<c:if test="${trace != null}">
    <p>
            ${trace}
    </p>
</c:if>

</body>
</html>