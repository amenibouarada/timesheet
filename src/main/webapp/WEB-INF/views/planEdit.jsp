<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ page import="com.aplana.timesheet.controller.CreatePlanForPeriodContoller" %>
<%@ page import="static com.aplana.timesheet.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.controller.PlanEditController.*" %>
<%@ page import="static com.aplana.timesheet.form.PlanEditForm.*" %>
<%@ page import="static com.aplana.timesheet.controller.PlanEditController.PERCENT_OF_CHARGE" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%! private static final String GRID_JS_ID = "myGrid"; %>

<html>
<head>
<title><fmt:message key="title.planEdit"/></title>
<script src="<%= getResRealPath("/resources/js/DataGrid.ext.js", application) %>" type="text/javascript"></script>
<script src="<%= getResRealPath("/resources/js/utils.js", application) %>" type="text/javascript"></script>

<style type="text/css">
        /* REQUIRED STYLES!!! */
    @import "<%= DOJO_PATH %>/dojox/grid/resources/Grid.css";
    @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
    @import "<%= getResRealPath("/resources/css/DataGrid.ext.css", application) %>";

    #planEditForm > table {
        margin-bottom: 10px;
    }

    #planEditForm td {
        padding: 2px;
    }

    #planEditForm table table {
        width: 100%;
    }

    #planEditForm label {
        padding-left: 2px;
    }

    #footer {
        display: none;
    }

</style>

<script type="text/javascript">

var VACATION_PLAN_COLUMN = 'vacation_plan';
var hasChanges = false;
var ALL_VALUE = <%= ALL_VALUE %>;

dojo.addOnLoad(function () {

    updateManagerList(dojo.byId('divisionId').value);
    //TODO костыль, так как для данного поля куки не устанавливаются автоматически при загрузке
    var prev_manager = getCookieValue("cookie_manager");
    if (prev_manager == "null" || prev_manager == undefined) {
        prev_manager = ALL_VALUE;
    }
    dojo.byId('manager').value = prev_manager;

    dojo.require("dojox.grid.DataGrid",
            function(DataGrid) {
                var grid = new DataGrid({keepSelection: true}, div);
            }
    );

    updateMultipleForSelect(dojo.byId("<%= REGIONS %>"));
    updateMultipleForSelect(dojo.byId("<%= PROJECT_ROLES %>"));

    var prevValue;

    <%= GRID_JS_ID %>.
    onStartEdit = function (inCell, inRowIndex) {
        prevValue = myStoreObject.items[inRowIndex][inCell.field][0];
    }

    <%= GRID_JS_ID %>.onApplyCellEdit = function (inValue, inRowIndex, inFieldIndex) {
        var newValue = replacePeriodsWithDots(inValue);

        if (!isNumber(newValue)) {
            newValue = '';
        }

        myStoreObject.items[inRowIndex][inFieldIndex][0] = newValue;

        if (prevValue != newValue) {
            hasChanges = true;
            cellHasBeenEdited(<%= GRID_JS_ID %>, inFieldIndex, inRowIndex);
        }
    };

    setTimeout(function () {
        restoreHiddenStateFromCookie(<%= GRID_JS_ID %>);
    }, 10);
});

function getCookieValue(CookieName) {
    var razrez = document.cookie.split(CookieName + '=');
    if (razrez.length > 1) { // Значит, куки с этим именем существует
        var hvost = razrez[1],
                tzpt = hvost.indexOf(';'),
                EndOfValue = (tzpt > -1) ? tzpt : hvost.length;
        return unescape(hvost.substring(0, EndOfValue));
    }
}

function checkChanges() {
    if (hasChanges) {
        return "Изменения не были сохранены.";
    }
}

dojo.addOnUnload(checkChanges);
dojo.addOnWindowUnload(checkChanges);
getRootEventListener().onbeforeunload = checkChanges;

function replacePeriodsWithDots(value) {
    if (typeof value == "string") {
        value = value.replace(/,/, ".");
    }

    return value;
}

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
        "<%= EMPLOYEE_ID %>", "<%= OTHER_PROJECTS_AND_PRESALES_PLAN %>", "<%= NON_PROJECT_PLAN %>",
        "<%= ILLNESS_PLAN %>", "<%= VACATION_PLAN %>"
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
            { name:"Итог", field:"<%= SUMMARY %>" },
            {
                name:"Процент загрузки",
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

        secondView.groups = [];

        dojo.forEach(projectList, function (project) {
            var projectId = project.<%= PROJECT_ID %>;

            secondView.groups.push({
                name:project.<%= PROJECT_NAME %>,
                field:projectId
            });

            modelFieldsForSave.push(projectId + "<%= _PLAN %>");
        });

        function generateCellsAndFillModelFields(view) {
            if (typeof view.cells == typeof undefined) {
                view.cells = [];
            }

            function createCell(name, field) {
                var scale = 2;
            <c:if test="${planEditForm.showPlans and planEditForm.showFacts}">
                scale = 1;
            </c:if>
                return {
                    name:name,
                    field:field,
                    noresize:true,

                    width:(49 * scale) + "px",
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

                    view.cells.push(createCell("План", planField), createCell("Факт", factField));
                    modelFields.push(planField, factField);

                    group.colSpan = 2;
                    group.expand = true;
                    </c:when>
                    <c:otherwise>
                    var fieldComponent = "<%= _PLAN %>";

                    <c:if test="${planEditForm.showFacts}">
                    fieldComponent = "<%= _FACT %>";
                    </c:if>

                    var field = group.field + fieldComponent;

                    view.cells.push(
                            createCell(group.name, field)
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

    dojo.forEach(normalize(modelFields, dojo.fromJson(dataJson)), function (row) {
        for (var field in row) {
            if (typeof row[field] == typeof undefined) {
                row[field] = "";
            }
        }

        myStoreObject.items.push(row);
    });

}


function updateMonthList(year) {
    var monthNode = dojo.byId("<%= MONTH %>");
    var month = monthNode.value;

    monthNode.options.length = 0;

    var monthMapJson = '${monthMapJson}';

    if (monthMapJson.length > 0) {
        var monthMap = dojo.fromJson(monthMapJson);
        dojo.forEach(dojo.filter(monthMap,function (monthData) {
            return (monthData.year == year);
        }), function (monthData) {
            dojo.forEach(monthData.months, function (monthObj) {
                var option = document.createElement("option");
                dojo.attr(option, {
                    value:monthObj.number
                });
                option.text = monthObj.name;
                option.innerHTML = monthObj.name;
                if (monthObj.number == month) {
                    option.selected = "selected";
                }
                monthNode.appendChild(option);
            });
        });
    }
}

function updateMultipleForSelect(select) {
    var allOptionIndex;
    var isAllOption = dojo.some(select.options, function (option, idx) {
        if (option.value == <%= ALL_VALUE %> && option.selected) {
            allOptionIndex = idx;
            return true;
        }

        return false;
    });

    if (isAllOption) {
        select.removeAttribute("multiple");
        select.selectedIndex = allOptionIndex;
    } else {
        select.setAttribute("multiple", "multiple");
    }
}

function validate() {
    var errors = [];

    if (dojo.byId("<%= DIVISION_ID %>").value == 0) {
        errors.push("Не выбрано подразделение");
    }

    if (dojo.byId("<%= YEAR %>").value == 0) {
        errors.push("Не выбран год");
    }

    if (dojo.byId("<%= MONTH %>").value == 0) {
        errors.push("Не выбран месяц");
    }

    if (!dojo.byId("<%= REGIONS %>").value) {
        errors.push("Не выбран ни один регион");
    }

    if (!dojo.byId("<%= PROJECT_ROLES %>").value) {
        errors.push("Не выбрана ни одна должность");
    }
    if ((!dojo.byId("<%= SHOW_PLANS%>").checked )
            && (!dojo.byId("<%= SHOW_FACTS%>").checked )) {
        errors.push("Необходимо выбрать плановые или фактические показатели для отображения");
    }

    return !showErrors(errors);
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

            /*if ((value || "").length == 0) {
             delete item[field];
             } else {*/
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

                    item.<%= PROJECTS_PLANS %>.push({
                        "<%= PROJECT_ID %>":parseInt(match[1]),
                        "<%= _PLAN %>":value
                    });

                    delete item[field];
                }
            }
            //}
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

function exportTableInExcel() {
    var year = dojo.byId("year").value;
    var month = dojo.byId("month").value;
    var form = dojo.byId("<%= FORM %>");
    form.action = (getContextPath() + "<%= EXPORT_TABLE_EXCEL %>/"+year+"/"+month);
    form.submit();

    form.action = getContextPath() + "<%= PLAN_EDIT_URL %>";
}

function getRegionsSelected() {
    var regionsNode = dojo.byId("<%= REGIONS %>");
    var regionsSelected = [];
    for (var i = 0; i < regionsNode.length; i++) {
        if (regionsNode.options[i].selected) {regionsSelected.push(regionsNode.options[i].value); }
    }
    return regionsSelected;
}

function updateManagerList(id) {
    if (id==null) {
        id = dojo.byId("<%= DIVISION_ID %>").value;
    }
    var managersNode = dojo.byId("<%= MANAGER %>");
    var regionsNode = dojo.byId("<%= REGIONS %>");

    var manager = managersNode.value;
    /* создаём значение Все руководители */
    var emptyOption = dojo.doc.createElement("option");
    dojo.attr(emptyOption, {
        value:-1
    });
    emptyOption.title = "Все руководители";
    emptyOption.innerHTML = "Все руководители";
    /* очищаем список */
    managersNode.options.length = 0;
    /* приклеиваем 'всех руководителей' */
    managersNode.appendChild(emptyOption);

    var isAllOption = dojo.some(regionsNode.options, function (option, idx) {
        if (option.value == <%= ALL_VALUE %> && option.selected) {
            return true;
        }
        return false;
    });
    var selectedRegions;
    if (!isAllOption) {selectedRegions = getRegionsSelected();}
    var managerMapJson = '${managerMapJson}';
    var count = 0;
    if (managerMapJson.length > 0) {
        var managerMap = dojo.fromJson(managerMapJson);
        dojo.forEach(dojo.filter(managerMap,function (m) {
            return (m.division == id);
        }), function (managerData) {
            if (isAllOption) {
                var option = document.createElement("option");
                dojo.attr(option, {
                    value:managerData.id
                });
                option.text = managerData.name;
                option.innerHTML = managerData.name;
                if (managerData.number == manager) {
                    option.selected = "selected";
                }
                managersNode.appendChild(option);
            } else {
                var add = false;
                dojo.forEach(managerData.regionWhereMan,
                        function(redData) {
                            for (var i = 0; i < selectedRegions.length; i++) {
                                if (selectedRegions[i] == redData.id) {
                                    add = true;
                                }
                            }
                        }
                );

                if (add) {
                    var option = document.createElement("option");
                    dojo.attr(option, {
                        value:managerData.id
                    });
                    option.text = managerData.name;
                    option.innerHTML = managerData.name;
                    if (managerData.number == manager) {
                        option.selected = "selected";
                    }
                    managersNode.appendChild(option);
                }
            }
        });
    }
    if (managersNode.options.length == 1 && emptyOption.value == managersNode.options[0].value){
        dojo.byId("<%= MANAGER %>").disabled = 'disabled';
    } else {
       dojo.byId("<%= MANAGER %>").disabled = '';
    }
}

function log(text){
    console.log(text);
}

</script>
<style>
    .topAlignTD {
        padding: 5px;
        vertical-align: top;
    }
    .blockYearMonth {
        float: left;
        position: relative;
    }
    .blockElement {
        float: left;
        position: relative;
        top: -8px;
    }
</style>
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
                                        <td style="text-align: center">
                                            <button id="show" style="width:150px;vertical-align: middle;" type="submit"
                                                    onclick="return validate()">Показать
                                            </button>
                                        </td>

                                        <td>
                                            <div>
                                                <form:checkbox id="<%= SHOW_PLANS %>" path="<%= SHOW_PLANS %>"
                                                               label="Показывать плановые показатели"/>
                                            </div>
                                            <div style="padding-top: 5px;">
                                                <form:checkbox id="<%= SHOW_FACTS %>" path="<%= SHOW_FACTS %>"
                                                               label="Показывать фактические показатели"/>
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
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
            <td>
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
            <td>
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

    <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
        <c:if test="${fn:length(jsonDataToShow) > 0 and editable}">
            <button style="width:150px;margin-left: 23px;" onclick="save()" type="button">Сохранить планы</button>
        </c:if>
        <button style="margin-left: 20px;" onclick="createPlanForPeriod()" type="button">
            Запланировать на период
        </button>
        <button style="margin-left: 20px;" onclick="exportTableInExcel()" type="button">
            Сохранить в Excel
        </button>
    </sec:authorize>
</form:form>

<br/>

<c:if test="${fn:length(jsonDataToShow) > 0}">
    <div dojoType="dojox.layout.ContentPane" style="width: 100%; min-width: 1260px;">
        <div id="myTable" jsId="<%= GRID_JS_ID %>" dojoType="dojox.grid.DataGrid" store="myStore"
             selectionMode="single" canSort="false" query="myQuery" <%--autoHeight="true"--%> style="height: 620px;"
             structure="myLayout"></div>
    </div>
</c:if>
</body>
</html>