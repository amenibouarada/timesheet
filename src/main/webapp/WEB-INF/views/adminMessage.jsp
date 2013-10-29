<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title></title>
    <script type="text/javascript">
        function submitMessage() {
            adminMessageForm.action="adminMessage";
            adminMessageForm.submit();
        }
    </script>
</head>
<body>

    <h1><fmt:message key="admin.sendMessage"/></h1><br/>

    <form:form commandName="adminMessageForm" method="post">

        <c:if test="${fn:length(errors) > 0}">
            <div class="errors_box">
                <c:forEach items="${errors}" var="error">
                    <fmt:message key="${error.code}">
                        <fmt:param value="${error.arguments[0]}"/>
                    </fmt:message><br/>
                </c:forEach>
            </div>
            <br/>
        </c:if>

        <label for="email">Введите email:</label><br/>
        <form:input path="email" id="email" style="width:300px;"/><br/><br/>
        <label for="description">Суть проблемы:</label><br/>
        <form:textarea rows="10" cols="50" path="description" id="description"/><br/><br/>
        <input type="submit" value="Отправить сообщение" style="width:170px" onclick="submitMessage()">
        <input type="button" value="Вернуться" style="width:100px" onclick="location.href='/login'">
    </form:form>
</body>
</html>

