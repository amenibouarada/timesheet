<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<html>
    <head>
        <title><fmt:message key="title.adminpanel"/></title>
        <script type="text/javascript">
            window.onload = function () {
                ajaxClick("updateProperties", "/admin/update/propertiesAJAX", "Настройки системы успешно обновлены из файла");
                ajaxClick("schedulerplannedvacationcheck", "/admin/update/schedulerplannedvacationcheck", "Рассылка писем об отпусках:");
            };

            function ajaxClick(id, url, mess){
                var link = dojo.byId(id);
                link.href = "#";
                link.onclick = function () {
                    var text = link.innerHTML;
                    link.innerHTML = "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>" + text;

                    dojo.xhrGet({
                        url: "<%= request.getContextPath()%>" + url,
                        handleAs: "text",

                        load: function (data) {
                            if (data.size == 0) {
                                data = "неизвестно";
                            }
                            showTextMessage(mess + " " + data, false);
                            link.innerHTML = text;
                        },

                        error: function (error) {
                            showTextMessage("Произошла ошибка " + error, true);
                            link.innerHTML = text;
                        }
                    });
                }
            }

            function showTextMessage(msg, isError) {
                var messagebox = dojo.byId("messageBox");
                //Сбрасываем стиль
                messagebox.style.display = 'block';
                messagebox.setAttribute("class", "");

                messagebox.setAttribute("class", isError ? "errorbox" : "info-gray");
                messagebox.innerHTML = "<b>" + msg + "</b>";
            }

            function updateShowUser() {
                var checked = dojo.byId("allUserCheckBox").checked;
                var url = "/admin/update/hidealluser";

                if (checked) {
                    url = "/admin/update/showalluser";
                }

                dojo.xhrGet({
                    url: url
                });
            }

        </script>

        <style type="text/css">
            .headerText {
                font-size: 24px;
                text-align: center;
                height: 100px;
                width: 200px;
            }
            .elementText {
                padding: 10px;
            }
        </style>
    </head>

    <body>
        <h1><fmt:message key="title.adminpanel"/></h1>

        <br/>

        <table>
            <%--Уведомления--%>
            <tr>
                 <td class="headerText">
                    Уведомления
                 </td>
                <td class="elementText">
                    <a href="admin/update/checkreport"><fmt:message key="link.checkemails"/></a>
                </td>
            </tr>


            <%--Отпуска--%>
            <tr>
                <td class="headerText" rowspan="2">
                    Отпуска
                </td>
                <td class="elementText">
                    <a href="admin/update/employeeassistantactivestatus"><fmt:message key="link.employeeassistantactivestatus"/></a>
                </td>
            <tr>
                <td class="elementText">
                    <a href="admin/update/schedulerplannedvacationcheck" id="schedulerplannedvacationcheck"><fmt:message key="link.schedulerplannedvacationcheck"/></a>
                </td>
            </tr>


            <%--Система--%>
            <tr>
                <td class="headerText">
                    Система
                </td>
                <td class="elementText">
                    <a href="admin/update/properties" id="updateProperties"><fmt:message key="link.update.properties"/></a>
                </td>
            </tr>


            <%--Сотрудники--%>
            <tr>
                <td class="headerText" rowspan="5">
                    Сотрудники
                </td>
                <td class="elementText">
                    <input type="checkbox" name="showAllUser" id="allUserCheckBox" onChange="updateShowUser();"
                           <c:if test="${showalluser == true}">checked="checked"</c:if>
                            > <fmt:message key="link.showalluser"/>
                </td>
            </tr>
            <tr>
                <td class="elementText">
                    <a href="admin/update/ldap"><fmt:message key="link.updateldap"/></a>
                </td>
            </tr>
            <tr>
                <td class="elementText">
                    <a href="admin/update/siddisabledusersfromldap"><fmt:message key="link.disabledsidsync"/></a>
                </td>
            </tr>
            <tr>
                <td class="elementText">
                    <a href="admin/update/sidallusersfromldap"><fmt:message key="link.allsidsync"/></a>
                </td>
            </tr>
            <tr>
                <td class="elementText">
                    <a href="admin/update/jiranameallusersfromldap"><fmt:message key="link.alljiranamesync"/></a>
                </td>
            </tr>

            <%--Проекты--%>
            <tr>
                <td class="headerText" rowspan="2">
                    Проекты
                </td>
                <td class="elementText">
                    <a href="/admin/projects"><fmt:message key="link.editprojects"/></a>
                </td>
            </tr>
            <tr>
                <td class="elementText">
                    <a href="admin/update/oqsync"><fmt:message key="link.oqsync"/></a>
                </td>
            </tr>

        </table>
    </body>
</html>