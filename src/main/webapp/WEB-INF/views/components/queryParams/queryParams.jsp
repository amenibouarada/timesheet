<%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <table>
        <tr>
            <td>
                <span class="label">Год:</span>
            </td>
            <td>
                <form:select id="monthreport_year" path="<%= YEAR %>" class="without_dojo" onmouseover="showTooltip(this);"
                             onmouseout="tooltip.hide();"
                             >
                    <%--onchange="updateMonthList(this.value)"--%>
                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                </form:select>
            </td>

            <td>
                <span class="label">Месяц:</span>
            </td>
            <td>
                <form:select id="monthreport_month" path="<%= MONTH %>" class="without_dojo" onmouseover="showTooltip(this);"
                             onmouseout="tooltip.hide();">
                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                </form:select>
            </td>
        </tr>
    </table>


