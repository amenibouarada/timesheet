<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.import.employee.vacation.days"/></title>
</head>
<body>

<h1><fmt:message key="title.import.employee.vacation.days"/></h1>
<form:form method="post" enctype="multipart/form-data"
           modelAttribute="uploadedFile" action="${pageContext.request.contextPath}/admin/update/importEmpVacDays">
    <div style="width: 300px;margin-top: 20px;">
        <div style="color: red; font-size: medium; margin-bottom: 15px;">
            <form:errors path="file"/>
        </div>
        <div style="margin-bottom: 15px;">
            Выберите файл для загрузки:
        </div>
        <div style="margin-bottom: 15px;">
            <input type="file" name="file" accept=".xls,.xlsx"/>
        </div>
        <input type="submit" value="Импортировать" style="width: 150px;"/>
    </div>

</form:form>
<c:if test="${trace != null}">
    <p>
            ${trace}
    </p>
</c:if>
</body>
</html>