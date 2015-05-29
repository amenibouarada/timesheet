<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="feedback"/></title>
</head>

<body>
<br/>
<span class="label">Ваше сообщение успешно отправлено</span>
<br/>
<br/>
<form:form method="post" action="sendNewFeedbackMessage">
    <button id="submit_button" type="submit">Отправить новое сообщение</button>
</form:form>
</body>
</html>