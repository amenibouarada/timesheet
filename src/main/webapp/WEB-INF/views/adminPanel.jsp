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
    </head>

    <body>
        <h1><fmt:message key="title.adminpanel"/></h1>

        <br/>

        <div id="messageBox" style="text-align: left!important; width: 90%;padding: 4px;"></div>
        <br/>
        <input type="checkbox" name="showAllUser" id="allUserCheckBox" onChange="updateShowUser();"
               <c:if test="${showalluser == true}">checked="checked"</c:if>
                > <fmt:message key="link.showalluser"/>
        <br/>
        <br/>
        <ul>
            <li><a href="/admin/projects"><fmt:message key="link.editprojects"/></a> </li>
            <li><a href="admin/update/ldap"><fmt:message key="link.updateldap"/></a></li>
            <li><a href="admin/update/checkreport"><fmt:message key="link.checkemails"/></a></li>
            <li><a href="admin/update/oqsync"><fmt:message key="link.oqsync"/></a></li>
            <li><a href="admin/update/properties" id="updateProperties"><fmt:message key="link.update.properties"/></a>
            </li>
            <li><a href="admin/update/siddisabledusersfromldap"><fmt:message key="link.disabledsidsync"/></a></li>
            <li><a href="admin/update/sidallusersfromldap"><fmt:message key="link.allsidsync"/></a></li>
            <li><a href="admin/update/jiranameallusersfromldap"><fmt:message key="link.alljiranamesync"/></a></li>
            <li><a href="admin/update/employeeassistantactivestatus"><fmt:message key="link.employeeassistantactivestatus"/></a></li>
            <li><a href="admin/update/schedulerplannedvacationcheck" id="schedulerplannedvacationcheck"><fmt:message key="link.schedulerplannedvacationcheck"/></a></li>
        </ul>

    </body>
</html>