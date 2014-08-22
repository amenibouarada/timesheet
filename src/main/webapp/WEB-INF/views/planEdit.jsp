<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ page import="com.aplana.timesheet.controller.CreatePlanForPeriodContoller" %>
<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.controller.PlanEditController.*" %>
<%@ page import="static com.aplana.timesheet.form.PlanEditForm.*" %>
<%@ page import="static com.aplana.timesheet.service.PlanEditService.*" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="com.aplana.timesheet.enums.ProjectFundingTypeEnum" %>
<%@ page import="com.aplana.timesheet.enums.TypesOfActivityEnum" %>
<%@ page import="com.aplana.timesheet.controller.PlanEditController" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
<title><fmt:message key="menu.planEdit"/></title>
<script src="<%= getResRealPath("/resources/js/DataGrid.ext.js", application) %>" type="text/javascript"></script>
<script src="<%= getResRealPath("/resources/js/planEdit.js", application) %>" type="text/javascript"></script>

<style type="text/css">
        /* REQUIRED STYLES!!! */
    @import "<%= DOJO_PATH %>/dojox/grid/resources/Grid.css";
    @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
    @import "<%= getResRealPath("/resources/css/DataGrid.ext.css", application) %>";
    @import "<%= getResRealPath("/resources/css/planEdit.css", application) %>";
</style>

<script type="text/javascript">

    var REGIONS = "<%= REGIONS %>";
    var PROJECT_ROLES = "<%= PROJECT_ROLES %>";
    var EMPLOYEE_ID = "<%= EMPLOYEE_ID %>";
    var OTHER_PROJECTS_AND_PRESALES_PLAN = "<%= OTHER_PROJECTS_AND_PRESALES_PLAN %>";
    var NON_PROJECT_PLAN = "<%= NON_PROJECT_PLAN %>";
    var VACATION_PLAN = "<%= VACATION_PLAN %>";
    var EMPLOYEE = "<%= EMPLOYEE %>";
    var PROJECT_ROLES = "<%= PROJECT_ROLES %>";
    var ALL_VALUE = <%= ALL_VALUE %>;
    var DIVISION_ID = "<%= DIVISION_ID %>";
    var MANAGER = "<%= MANAGER %>";
    var MONTH = "<%= MONTH %>";
    var YEAR = "<%= YEAR %>";
    var SHOW_PLANS = "<%= SHOW_PLANS %>";
    var SHOW_FACTS = "<%= SHOW_FACTS %>";
    var PROJECTS_PLANS = "<%= PROJECTS_PLANS %>";
    var FORM = "<%= FORM %>";
    var EXPORT_TABLE_EXCEL = "<%= EXPORT_TABLE_EXCEL %>";
    var PLAN_EDIT_URL = "<%= PLAN_EDIT_URL %>";
    var EMPLOYEE_DIVISION = "<%= EMPLOYEE_DIVISION %>";
    var PROJECT_DIVISION = "<%= PROJECT_DIVISION %>";

    var _PLAN  = "<%= _PLAN %>";

    var SUMMARY_PROJECTS = "<%= SUMMARY_PROJECTS %>";
    var SUMMARY_PRESALES = "<%= SUMMARY_PRESALES %>";
    var PERCENT_OF_CHARGE = "<%= PERCENT_OF_CHARGE %>";
    var SUMMARY = "<%= SUMMARY %>";
    var CENTER_PROJECTS = "<%= CENTER_PROJECTS %>";
    var CENTER_PRESALES = "<%= CENTER_PRESALES %>";
    var OTHER_PROJECT = "<%= OTHER_PROJECT %>";
    var OTHER_PRESALE = "<%= OTHER_PRESALE %>";
    var OTHER_PROJECTS_AND_PRESALES = "<%= OTHER_PROJECTS_AND_PRESALES %>";
    var OTHER_COMERCIAL_PROJECT = "<%= OTHER_COMERCIAL_PROJECT %>";
    var OTHER_INVEST_PROJECT = "<%= OTHER_INVEST_PROJECT %>";
    var NON_PROJECT = "<%= NON_PROJECT %>";
    var ILLNESS = "<%= ILLNESS %>";
    var VACATION = "<%= VACATION %>";
    var SUMMARY_INVESTMENT = "<%= SUMMARY_INVESTMENT %>";
    var SUMMARY_COMMERCIAL = "<%= SUMMARY_COMMERCIAL %>";
    var MONTH_PLAN = "<%= MONTH_PLAN %>";

    var PRESALE = <%= TypesOfActivityEnum.PRESALE.getId() %>;
    var PROJECT = <%= TypesOfActivityEnum.PROJECT.getId() %>;

    var COMMERCIAL_PROJECT = <%= ProjectFundingTypeEnum.COMMERCIAL_PROJECT.getId() %>;
    var INVESTMENT_PROJECT = <%= ProjectFundingTypeEnum.INVESTMENT_PROJECT.getId() %>;

    var VACATION_PLAN_COLUMN = 'vacation_plan';
    var hasChanges = false;

    var isEditable = ${editable};
    var showSumFundingType = ${planEditForm.showSumFundingType};
    var monthMapJson = '${monthMapJson}';
    var managerMapJson = '${managerMapJson}';

    var COOKIE_SELECTION_ROW = '<%=PlanEditController.COOKIE_SELECTION_ROW%>';
    var COOKIE_SCROLL_X = '<%=PlanEditController.COOKIE_SCROLL_X%>';
    var COOKIE_SCROLL_Y = '<%=PlanEditController.COOKIE_SCROLL_Y%>';

    var selectedRowIndex = '${selectionRowIndex}';
    var scrollX = '${scrollX}';
    var scrollY = '${scrollY}';

    dojo.addOnLoad(function () {

        require(["dijit/Tooltip", "dojo/domReady!"], function (Tooltip) {
            new Tooltip({
                connectId: ["calFromDateToolTip"],
                label: "Обозначения цветов заголовков в таблице:<table class='without_borders'>" +
                        "<tr><td><div class='blockTooltip classDateBlueBack'> </div></td><td><div style='padding: 5px;'> - проекты</div></td></tr>" +
                        "<tr><td><div class='blockTooltip classDateRedBack'> </div></td><td> <div style='padding: 5px;'> - пресейлы</div></td></tr>" +
                        "<tr><td><div class='blockTooltip classDateGrayBack'> </div></td><td> <div style='padding: 5px;'> - прочие</div></td></tr>" +
                        "</table>"
            });
        });


        updateManagerList(dojo.byId(DIVISION_ID).value);
        var prev_manager = getCookieValue("cookie_manager");// - костыль, так как для данного поля куки не устанавливаются автоматически при загрузке
        if (prev_manager == "null" || prev_manager == undefined) {
            prev_manager = ALL_VALUE;
        }
        dojo.byId(MANAGER).value = prev_manager;
        dojo.require("dojox.grid.DataGrid",
                function(DataGrid) {
                    var grid = new DataGrid({keepSelection: true}, div);
                }
        );
        updateMultipleForSelect(dojo.byId(REGIONS));
        updateMultipleForSelect(dojo.byId(PROJECT_ROLES));

        var prevValue;
        planningGrid.onStartEdit = function (inCell, inRowIndex) {
            prevValue = myStoreObject.items[inRowIndex][inCell.field][0];
        }

        planningGrid.onApplyCellEdit = function (inValue, inRowIndex, inFieldIndex) {
            dojo.cookie(COOKIE_SELECTION_ROW, inRowIndex, { expire: -1 });
            dojo.cookie(COOKIE_SELECTION_ROW, inRowIndex, { expire: 999999999 });

            dojo.query(".dojoxGridScrollbox").forEach(function(divs) {
                dojo.cookie(COOKIE_SCROLL_X, divs.scrollTop, { expire: -1 });
                dojo.cookie(COOKIE_SCROLL_X, divs.scrollTop, { expire: 999999999 });

                dojo.cookie(COOKIE_SCROLL_Y, divs.scrollLeft, { expire: -1 });
                dojo.cookie(COOKIE_SCROLL_Y, divs.scrollLeft, { expire: 999999999 });
            });

            var newValue = replacePeriodsWithDots(inValue);

            if (!isNumber(newValue)) {
                newValue = '';
            }

            myStoreObject.items[inRowIndex][inFieldIndex][0] = newValue;

            if (prevValue != newValue) {
                recalcColumns(myStoreObject, inRowIndex);
                hasChanges = true;
                cellHasBeenEdited(planningGrid, inFieldIndex, inRowIndex);
            }
        };

        setTimeout(function () {
            restoreHiddenStateFromCookie(planningGrid);
            if (isNumber(selectedRowIndex)) {
                planningGrid.selection.setSelected(selectedRowIndex, true);
                dojo.query(".dojoxGridScrollbox").forEach(function(divs) {
                    divs.scrollTop = scrollX;
                    divs.scrollLeft = scrollY;
                });
            }
        }, 10);

        var items = normalize(modelFieldsForSave, myStoreObject.items);
        dojo.forEach(myStoreObject.items, function (item, idx) {
            recalcColumns(myStoreObject, idx);
        });
    });

    dojo.addOnUnload(checkChanges);
    dojo.addOnWindowUnload(checkChanges);
    getRootEventListener().onbeforeunload = checkChanges;

    var dataJson = '${jsonDataToShow}';
    var projectListJson = '${projectListJson}';
    var projectList = (projectListJson.length > 0) ? dojo.fromJson(projectListJson) : [];

    if (dataJson.length > 0) {
        dojo.require("dojox.layout.ContentPane");

        var myQuery = { "<%= EMPLOYEE_ID %>":"*" };
        var myStoreObject = {
            identifier:"<%= EMPLOYEE_ID %>",
            items:[]
        };
        var modelFields = ["<%= EMPLOYEE_ID %>"];
        var modelFieldsForSave = [
            "<%= EMPLOYEE_ID %>",
            "<%= NON_PROJECT_PLAN %>",
            "<%= VACATION_PLAN %>"
        ];
        var myStore = new dojo.data.ItemFileWriteStore({
            data:myStoreObject
        });

        function createHeaderViewsAndFillModelFields() {
            var firstView = {
                noscroll:true,
                expand:true
            };

            var secondView = {
                expand:true
            };

            var views = [
                {
                    noscroll:true,
                    cells:[
                        {
                            name:"Сотрудник",
                            field:"<%= EMPLOYEE %>",
                            noresize:true,
                            width:"120px",
                            editable:false
                        }
                    ]
                },
                firstView,
                secondView
            ];

            firstView.groups = [
                { name:"Итог, ч", field:"<%= SUMMARY %>" },
                {
                    name:"Итог, %",
                    field:"<%= PERCENT_OF_CHARGE %>",
                    cellsFormatter:function (text) {
                        var number = parseFloat(replacePeriodsWithDots(text));

                        if (number > 100) {
                            return dojo.create(
                                    "span",
                                    {
                                        innerHTML:text,
                                        style:"color: red; font-weight: bold"
                                    }
                            ).outerHTML;
                        }

                        return text;
                    }
                },
                { name:"Проекты центра", field:"<%= CENTER_PROJECTS %>" },
                { name:"Пресейлы центра", field:"<%= CENTER_PRESALES %>" },
                { name:"Проекты/Пресейлы других центров", field:"<%= OTHER_PROJECTS_AND_PRESALES %>" },
                { name:"Непроектная", field:"<%= NON_PROJECT %>" },
                { name:"Болезнь", field:"<%= ILLNESS %>" },
                { name:"Отпуск", field:"<%= VACATION %>" }
            ];

            <c:if test="${planEditForm.showSumProjectsPresales}">
            firstView.groups.push(
                    {
                        name: "Проекты",
                        field: "<%= SUMMARY_PROJECTS %>"
                    },
                    {
                        name: "Пресейлы",
                        field: "<%= SUMMARY_PRESALES %>"
                    }
            );
            </c:if>

            <c:if test="${planEditForm.showSumFundingType}">
            firstView.groups.push(
                    {
                        name: "Инвестиционные активности",
                        field: "<%= SUMMARY_INVESTMENT %>"
                    },
                    {
                        name: "Коммерческие активности",
                        field: "<%= SUMMARY_COMMERCIAL %>"
                    }
            );
            </c:if>

            secondView.groups = [];

            var presalesProjects=[];
            var projectProjects=[];
            dojo.forEach(projectList, function (project) {
                if (project.<%= PROJECT_TYPE %> == <%= TypesOfActivityEnum.PROJECT.getId() %>) {
                    projectProjects.push(project);
                } else if (project.<%= PROJECT_TYPE %> == <%= TypesOfActivityEnum.PRESALE.getId() %>) {
                    presalesProjects.push(project);
                }
            });
            addPresalesAndProjects(projectProjects, "background: url(/resources/img/tabEnabledProject.png) #CDECFF repeat-x top;");
            addPresalesAndProjects(presalesProjects, "background: url(/resources/img/tabEnabledPresale.png) #FFD8EE repeat-x top;");

            function addPresalesAndProjects(list, style) {
                dojo.forEach(list, function (project) {
                    var projectId = project. <%= PROJECT_ID %>;
                    secondView.groups.push({
                        name: project. <%= PROJECT_NAME %>,
                        field: projectId,
                        headerStyles: style
                    });
                    modelFieldsForSave.push(projectId + "<%= _PLAN %>");
                });
            }

            addPresalesAndProjects();
            function generateCellsAndFillModelFields(view) {
                if (typeof view.cells == typeof undefined) {
                    view.cells = [];
                }

                function createCell(name, field, newScale, headerStyles) {
                    var scale = 2;
                <c:if test="${planEditForm.showPlans and planEditForm.showFacts}">
                    scale = 1;
                </c:if>
                    if (newScale != null){
                        scale = newScale;
                    }
                    return {
                        name:name,
                        field:field,
                        noresize:true,
                        headerStyles: headerStyles,
                        width:(29 * scale) + "px",
                        <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                        <c:if test="${editable}">
                            editable:dojo.some(modelFieldsForSave, function (fieldForSave) {
                                return ((field == fieldForSave) && (field !== VACATION_PLAN_COLUMN));
                            })
                        </c:if>
                        </sec:authorize>
                    };
                }



                if (view.groups && view.groups.length != 0) {
                    dojo.forEach(view.groups, function (group) {
                        <c:choose>
                            <c:when test="${planEditForm.showPlans and planEditForm.showFacts}">
                                var planField = group.field + "<%= _PLAN %>";
                                var factField = group.field + "<%= _FACT %>";

                                if (group.field == "<%= SUMMARY %>"){
                                    view.cells.push(createCell("П", planField, 2), createCell("Ф", factField, null));
                                }else{
                                    view.cells.push(createCell("П", planField, null), createCell("Ф", factField, null));
                                }
                                modelFields.push(planField, factField);

                                group.colSpan = 2;
                                group.expand = true;
                            </c:when>
                            <c:otherwise>
                                var fieldComponent = "<%= _PLAN %>";

                                <c:if test="${planEditForm.showFacts}">
                                    fieldComponent = "<%= _FACT %>";
                                </c:if>

                        /**
                         * true - project, false - presale, null - other
                         * @param field
                         */
                        function isFieldProjectOrPresale(field) {
                            var result = null;
                            dojo.forEach(projectProjects, function (project) {
                                if (project. <%= PROJECT_ID %> == field) {
                                    result = true;

                                }
                            });
                            if (result != null) {
                                return result;
                            }
                            dojo.forEach(presalesProjects, function (presale) {
                                if (presale. <%= PROJECT_ID %> == field) {
                                    result = false;

                                }
                            });
                            return result;
                        }

                                var field = group.field + fieldComponent;
                                var isFieldProjectOrPresale = isFieldProjectOrPresale(group.field); //true - project, false - presale, null - other
                                var headerStyles = isFieldProjectOrPresale != null ? (isFieldProjectOrPresale ?
                                        "background: url(/resources/img/tabEnabledProject.png) #CDECFF repeat-x top;" :
                                        "background: url(/resources/img/tabEnabledPresale.png) #FFD8EE repeat-x top;") : "";

                                view.cells.push(
                                    createCell(group.name, field, null, headerStyles)
                                );
                                modelFields.push(field);
                            </c:otherwise>
                        </c:choose>
                    });
                <c:if test="${not (planEditForm.showPlans and planEditForm.showFacts)}">
                    view.groups = undefined;
                </c:if>
                } else {
                    dojo.forEach(view.cells, function (cell) {
                        modelFields.push(cell.field);
                    });
                }
            }

            dojo.forEach(views, function (view) {
                generateCellsAndFillModelFields(view);
            });

            return views;
        }

        var myLayout = createLayout(createHeaderViewsAndFillModelFields());

        modelFields.push(EMPLOYEE_DIVISION);
        modelFields.push(MONTH_PLAN);
        modelFields.push(OTHER_PROJECT + _PLAN);
        modelFields.push(OTHER_PRESALE + _PLAN);
        modelFields.push(OTHER_COMERCIAL_PROJECT + _PLAN);
        modelFields.push(OTHER_INVEST_PROJECT + _PLAN);

        dojo.forEach(normalize(modelFields, dojo.fromJson(dataJson)), function (row) {
            for (var field in row) {
                if (typeof row[field] == typeof undefined) {
                    row[field] = "";
                }
            }

            myStoreObject.items.push(row);
        });

    }

    function save() {
        var items = normalize(modelFieldsForSave, myStoreObject.items);
        var errors = [];

        dojo.forEach(items, function (item, idx) {
            for (var field in item) {
                var value = item[field];

                if (field == "<%= PROJECTS_PLANS %>") {
                    continue;
                }

                if ((value || "").length == 0) {
                    value = 0;
                }

                value = replacePeriodsWithDots(value);

                if (!isNumber(value)) {
                    errors.push("Неверный формат числа в строке №" + (idx + 1));
                    return;
                } else {
                    value = parseFloat(value);

                    var match = field.match(/^(\d+?)<%= _PLAN %>$/);

                    if (match) {
                        if (!item.<%= PROJECTS_PLANS %>) {
                            item.<%= PROJECTS_PLANS %> = [];
                        }

                        // переводим проценты в часы
                        lastValue = myStoreObject.items[idx][SUMMARY + _PLAN][0];
                        var monthPlan = (lastValue.split("/"))[1];
                        value = monthPlan * value / 100;

                        item.<%= PROJECTS_PLANS %>.push({
                            "<%= PROJECT_ID %>":parseInt(match[1]),
                            "<%= _PLAN %>": value
                        });

                        delete item[field];
                    }

                    var matchNonProject = (field == (NON_PROJECT + _PLAN));

                    if (matchNonProject) {
                        var summaryValue = myStoreObject.items[idx][SUMMARY + _PLAN][0];
                        var monthPlan = (summaryValue.split("/"))[1];
                        value = monthPlan * value / 100;

                        item.<%=NON_PROJECT + _PLAN%> = value;
                    }
                }
            }
        });

        if (!showErrors(errors)) {
            var object = {
                "<%= JSON_DATA_YEAR %>": ${planEditForm.year},
                "<%= JSON_DATA_MONTH %>": ${planEditForm.month},
                "<%= JSON_DATA_ITEMS %>":items
            };
            hasChanges = false;
            var form = dojo.byId("<%= FORM %>");
            form.action = "<%= PLAN_SAVE_URL %>";
            form.<%= JSON_DATA %>.value = dojo.toJson(object);
            form.submit();
        }
    }

    function createPlanForPeriod() {
        window.location = getContextPath() + "<%= CreatePlanForPeriodContoller.CREATE_PLAN_FOR_PERIOD_URL %>";
    }

</script>

</head>
<body>

<br/>

<form:form method="post" commandName="<%= FORM %>">
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>"/>
    <form:hidden path="<%= JSON_DATA %>"/>

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
                                    <form:options items="${managerList}" itemLabel="name" itemValue="id"/>
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
                        <tr>
                            <td colspan="4" style="padding-top: 10px;">
                                <table>
                                    <tr>
                                        <td valign="top"><b>Показывать колонки</b></td>
                                        <td>
                                            <div>
                                                <form:checkbox id="<%= SHOW_PLANS %>" path="<%= SHOW_PLANS %>"
                                                               label="План"/>
                                            </div>
                                            <div style="padding-top: 5px;">
                                                <form:checkbox id="<%= SHOW_FACTS %>" path="<%= SHOW_FACTS %>"
                                                               label="Факт"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div>
                                                <form:checkbox path="<%= SHOW_PROJECTS %>" label="Проекты"/>
                                            </div>
                                            <div style="padding-top: 5px;">
                                                <form:checkbox path="<%= SHOW_PRESALES %>" label="Пресейлы"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div>
                                                <form:checkbox id="<%= SHOW_SUM_PROJECTS_PRESALES %>" path="<%= SHOW_SUM_PROJECTS_PRESALES %>"
                                                               label="Итого по проектам/пресейлам"/>
                                            </div>
                                            <div style="padding-top: 5px;">
                                                <form:checkbox id="<%= SHOW_SUM_FUNDING_TYPE %>" path="<%= SHOW_SUM_FUNDING_TYPE %>"
                                                               label="Итого по инвест./коммер."/>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                    </tr>
                                </table>
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
    <table>
        <tr>
            <td style="text-align: center">
                <button id="show" style="width:150px;vertical-align: middle;" type="submit"
                        onclick="return validate()">Показать
                </button>
            </td>
            <td style="text-align: left">
                <div class="question-hint">
                    <img id="calFromDateToolTip" src="<c:url value="/resources/img/question.png"/>"/>
                </div>
            </td>
            <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                <td style="padding-left: 300px">

                <c:if test="${fn:length(jsonDataToShow) > 0 and editable}">
                        <button style="width:150px;margin-left: 23px;" onclick="save()" type="button">Сохранить планы</button>
                    </td>
                    <td>
                </c:if>

                    <button style="margin-left: 20px;" onclick="createPlanForPeriod()" type="button">Запланировать на период</button>
                </td>
                <td>
                    <button style="margin-left: 20px;" onclick="exportTableInExcel()" type="button">Сохранить в Excel</button>
                </td>
                <td>
                    <button style="margin-left: 20px;" onclick="location.href='/employmentPlanning';" type="button">Планирование занятости за период</button>
                </td>
            </sec:authorize>
        </tr>
    </table>
</form:form>

<br/>

<c:if test="${fn:length(jsonDataToShow) > 0}">
    <div dojoType="dojox.layout.ContentPane" style="width: 100%; min-width: 1260px;">
        <div id="myTable" jsId="planningGrid" dojoType="dojox.grid.DataGrid" store="myStore"
             selectionMode="single" canSort="false" query="myQuery" <%--autoHeight="true"--%> style="height: 620px;"
             structure="myLayout"></div>
    </div>
</c:if>
</body>
</html>