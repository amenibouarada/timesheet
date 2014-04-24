<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Проект сохранён</title>
</head>
<body>
<p>Параметры проекта сохранены</p>
<input type="button" value="К списку проектов" style="width:200px; margin: 10px" onclick="location.href='<%= request.getContextPath()%>/admin/projects'">
<input type="button" value="Новый проект" style="width:200px; margin: 10px" onclick="location.href='<%= request.getContextPath()%>/admin/projects/add'">
</body>
</html>
