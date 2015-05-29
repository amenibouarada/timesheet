<%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <table>
        <tr>
            <td class="topAlignTD">
                <div class="blockYearMonth">
                    <table>
                        <tr>
                            <td>
                                <span class="label">Подразделение</span>
                            </td>
                            <td>
                                <form:select path="<%= DIVISION_ID %>" class="without_dojo"
                                             onmouseover="showTooltip(this);"
                                             onmouseout="tooltip.hide();"
                                             onchange="updateManagerList(this.value)">
                                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                                </form:select>
                            </td>
                            <td>
                                <span class="label">Год:</span>
                            </td>
                            <td>
                                <form:select path="<%= YEAR %>" class="without_dojo" onmouseover="showTooltip(this);"
                                             onmouseout="tooltip.hide();" onchange="updateMonthList(this.value)">
                                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                                </form:select>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <span class="label">Руководитель:</span>
                            </td>
                            <td>
                                <form:select path="<%= MANAGER %>" class="without_dojo"
                                             onmouseover="showTooltip(this);"
                                             onmouseout="tooltip.hide();" multiple="false">
                                    <form:option label="Все руководители" value="<%= ALL_VALUE %>"/>
                                    <form:options items="${managerMapJson}" itemLabel="name" itemValue="id"/>
                                </form:select>
                            </td>
                            <td>
                                <span class="label">Месяц:</span>
                            </td>
                            <td>
                                <form:select path="<%= MONTH %>" class="without_dojo" onmouseover="showTooltip(this);"
                                             onmouseout="tooltip.hide();">
                                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                                </form:select>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
            <td class="topAlignTD" style="padding-top: 10px;">
                <div class="blockElement">
                    <table>
                        <tr>
                            <td class="topAlignTD">
                                <span class="label">Регионы</span>
                            </td>
                            <td>
                                <form:select path="<%= REGIONS %>" onmouseover="showTooltip(this)" size="5"
                                             onmouseout="tooltip.hide()" multiple="true"
                                             onchange="updateMultipleForSelect(this); updateManagerList(null)">
                                    <form:option value="<%= ALL_VALUE %>" label="Все регионы"/>
                                    <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                                </form:select>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
            <td class="topAlignTD" style="padding-top: 10px;">
                <div class="blockElement">
                    <table>
                        <tr>
                            <td class="topAlignTD">
                                <span class="label">Должности</span>
                            </td>
                            <td>
                                <form:select path="<%= PROJECT_ROLES %>" onmouseover="showTooltip(this)" size="5"
                                             onmouseout="tooltip.hide()" multiple="true"
                                             onchange="updateMultipleForSelect(this)">
                                    <form:option value="<%= ALL_VALUE %>" label="Все должности"/>
                                    <form:options items="${projectRoleList}" itemLabel="name" itemValue="id"/>
                                </form:select>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
        </tr>
    </table>


