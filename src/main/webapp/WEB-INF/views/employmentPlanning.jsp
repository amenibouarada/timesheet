<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="static com.aplana.timesheet.form.EmploymentPlanningForm.*" %>
<%@ page import="static com.aplana.timesheet.form.AddEmployeeForm.*" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>

<html>
<head>
    <title></title>
    <script src="<%= getResRealPath("/resources/js/employmentPlanning.js", application) %>" type="text/javascript"></script>
    <style type="text/css">
        @import "<%= DOJO_PATH %>/dojox/grid/resources/Grid.css";
        @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
        .dojoxGrid table { margin: 0;}
        html, body { width: 100%; height: 100%; margin: 0; }
        .header {padding-right: 300px; border: 1px}
        .semiHeaderFirst {padding-left: 3000px; border: 0px; width: 100px}
        .semiHeaderSecond {padding-right: 1000px; border: 0px}
    </style>

    <script type="text/javascript">
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dojo.parser");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dojo.date.stamp");
        dojo.require("dojo.date.locale");

        require(["dijit/Dialog", "dijit/form/TextBox", "dijit/form/Button"]);

        // Создает грид "проект-сотрудники"
        function initProjectGrid(dataJson, yearStart, monthStart, yearEnd, monthEnd){
            var data = {
                identifier: "employee_id",
                items: []
            };

            var data_list = dojo.fromJson(dataJson);

            dojo.forEach(data_list, function (row) {
                for (var field in row) {
                    if (typeof row[field] == typeof undefined) {
                        row[field] = 0;
                    }
                }
                data.items.push(row);
            });

            var store = new dojo.data.ItemFileWriteStore({data: data});

            var leftView = {
                noscroll: true,
                sortable: false,
                cells: [[{
                    'name': 'Employee <img align="right" width="15px" src = "<c:url value="/resources/img/add.gif"/>" onclick="employeeDialog.show();"/>',
                    'field': 'employee_name',
                    'width': '200px',
                    noresize: true}
                ]]};

            var input_layout = [];
            input_layout.push({
                name: 'Планируемый процент занятости  <img align="right" width="15px" src = "<c:url value="/resources/img/edit.png"/>" onclick="savePlan();"/>',
                width: '150px',
                editable: true,
                formatter: formatterEditableData,
                field: 'plan',
                noresize: true
            });

            iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
                input_layout.push({
                    name: getMonthByNumber(month)+ ", " + year,
                    field: year + "_" + month,
                    width: '100px',
                    formatter: formatterData,
                    editable: false,
                    noresize: true
                });
            });
            var middleView = {
                cells: [input_layout]
            };

            var layout = [leftView, middleView];

            var grid = new dojox.grid.DataGrid({
                id: 'projectGrid',
                store: store,
                structure: layout,
                rowSelector: '20px',
                width: '400px',
                noresize: true,
                canSort: function(){return false;},
                onApplyCellEdit: function(inValue, inRowIndex, inFieldIndex){
                    if (inFieldIndex == "plan"){
                        var item = grid.getItem(inRowIndex);
                        if (item){
                            store.setValue(item, 'changed', 'yes');
                        }
                    }
                }
            });

            grid.placeAt("projectGridDiv");
            grid.startup();

            dojo.connect(grid, "onRowClick", function(e){
                var item = grid.getItem(e.rowIndex);
                if (item){
                    var employeeId = item["employee_id"];
                    var employeeName = item["employee_name"];
                    dojo.byId("spanEmployeeName").innerHTML = 'Загрузка сотрудника "'+employeeName+'" по проектам';
                    employeeDataHandler(employeeId, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, refreshEmployeeGrid);
                }
            });
        };

        function createLayout(isFact, yearStart, monthStart, yearEnd, monthEnd){
            var leftView = {
                noscroll: true,
                cells: [[
                    {
                        'name': 'Проект',
                        'field': 'project_name',
                        'width': '200px',
                        noresize: true
                    },{
                        'name': 'Среднее за период',
                        'field': '0_0',
                        'width': '150px',
                        formatter: formatterData,
                        noresize: true}
                ]]
            };

            var dataLayout = [];

            var headerLayout = [];
            var padding = (dojo.isWebKit ? 1 : 1.25);
            var cellStyles = "padding-left: "+padding+"px; padding-right: "+padding+"px;";

            if (isFact){
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
                    headerLayout.push({
                        name: getMonthByNumber(month)+ ", " + year,
                        colSpan: 2,
                        headerStyles: "width: 100px;",
                        cellStyles: cellStyles,
                        noresize: true
                    });
                });
            }
            var middleView = {};

            if (isFact){
                middleView.cells = [headerLayout, dataLayout];
                middleView.onBeforeRow = function(inDataIndex, inSubRows) {
                    var hidden = (inDataIndex >= 0);

                    for (var i = inSubRows.length - 2; i >= 0; i--) {
                        inSubRows[i].hidden = hidden;
                    }
                }
            } else {
                middleView.cells = [dataLayout];
            }

            iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
                if (isFact){
                    dataLayout.push({
                        name: 'П',
                        field: year + "_" + month,
                        formatter: formatterData,
                        width: '50px',
                        headerStyles: "width: 50px;",
                        cellStyles: cellStyles,
                        noresize: true
                    });

                    dataLayout.push({
                        name: 'Ф',
                        field: year + "-" + month,
                        formatter: formatterData,
                        width: '50px',
                        headerStyles: "width: 50px;",
                        cellStyles: cellStyles,
                        noresize: true
                    });
                } else {
                    dataLayout.push({
                        name: getMonthByNumber(month)+ ", " + year,
                        field: year + "_" + month,
                        formatter: formatterData,
                        width: '100px',
                        editable: false,
                        noresize: true
                    });
                }
            });

            var layout = [leftView, middleView];

            return layout;
        }

        // Создает грид "сотрудник-проекты"
        function initEmployeeGrid(dataJson, yearStart, monthStart, yearEnd, monthEnd){
            var data = {
                identifier: "project_id",
                items: []
            };

            var data_list = dojo.fromJson(dataJson);

            dojo.forEach(data_list, function (row) {
                for (var field in row) {
                    if (typeof row[field] == typeof undefined) {
                        row[field] = "";
                    }
                }
                data.items.push(row);
            });

            var store = new dojo.data.ItemFileWriteStore({data: data});

            var isFact = dojo.byId("isFact").checked;
            var layout = createLayout(isFact, yearStart, monthStart, yearEnd, monthEnd);

            var grid = new dojox.grid.DataGrid({
                id: 'employeeGrid',
                store: store,
                structure: layout,
                rowSelector: '20px',
                width: '400px'
            });

            grid.placeAt("employeeGridDiv");
            grid.startup();
        };

        // Динамически перестраивает грид "сотрудник-проекты"
        function refreshEmployeeGrid(response, yearStart, monthStart, yearEnd, monthEnd){
            var data = {
                identifier: "project_id",
                items: []
            };

            var data_list = dojo.fromJson(response);

            dojo.forEach(data_list, function (row) {
                for (var field in row) {
                    if (typeof row[field] == typeof undefined) {
                        row[field] = "";
                    }
                }
                data.items.push(row);
            });

            var isFact = dojo.byId("isFact").checked;
            var layout = createLayout(isFact, yearStart, monthStart, yearEnd, monthEnd);

            var store = new dojo.data.ItemFileWriteStore({data: data});
            var grid = dijit.byId("employeeGrid");
            grid.store.close();
            grid.setStore(store);
            grid.setStructure(layout);
            grid.render();
        }

        // Динамически перестраивает грид "проект-сотрудники"
        // yearStart, monthStart, yearEnd, monthEnd - не используется, нужны если менять структуру грида
        function refreshProjectGrid(response, yearStart, monthStart, yearEnd, monthEnd){
            var data = {
                identifier: "employee_id",
                items: []
            };

            var data_list = dojo.fromJson(response);

            dojo.forEach(data_list, function (row) {
                for (var field in row) {
                    if (typeof row[field] == typeof undefined) {
                        row[field] = "";
                    }
                }
                data.items.push(row);
            });

            var store = new dojo.data.ItemFileWriteStore({data: data});
            var grid = dijit.byId("projectGrid");
            grid.store.close();
            grid.setStore(store);
            grid.render();
        }

        // Обновляет список работников на форме добавления работников
        function updateAdditionEmployeeList(){
            var divisionId = dojo.byId("divisionId").value;
            var managerId = dojo.byId("managerId").value;
            var projectRoleListId = getSelectValues(dojo.byId("projectRoleListId"));
            var regionListId = getSelectValues(dojo.byId("regionListId"));

            additionEmployeeDataHandler(divisionId, managerId, projectRoleListId, regionListId, function(response){
                var grid = dijit.byId("projectGrid");
                var employeeSelect = dojo.byId("additionEmployeeList");
                clearSelectValues(employeeSelect);

                grid.store.fetch({query: {}, onComplete: checkExists, queryOptions: {deep:true}});

                // Те люди, которые уже есть в гриде - дизейблятся
                function checkExists(items){
                    var map = [];

                    for(var i = 0; i < items.length; i++){
                        var item = items[i];
                        map[item["employee_id"]] = item;
                    }

                    dojo.forEach(dojo.fromJson(response), function (row) {
                        var disable = map[row["employee_id"]] != undefined;
                        dojo.create("option", { value: row["employee_id"], innerHTML: row["employee_name"], disabled: disable}, employeeSelect);
                    });
                }
            });
        }

        // Очищает select'ы на форме выбора сотрудников
        function clearDialogSelection(){
            var projectRoleListId = dojo.byId("projectRoleListId");
            var regionListId = dojo.byId("regionListId");
            var employeeSelect = dojo.byId("additionEmployeeList");

            unselectValues(projectRoleListId);
            unselectValues(regionListId);
            clearSelectValues(employeeSelect);
        }

        // Добавляет строчку к гриду "проект-сотрудники"
        function addRow() {
            var grid = dijit.byId("projectGrid");
            var select = dojo.byId("additionEmployeeList");
            var employeeList = getSelectObjects(select);

            dojo.forEach(employeeList, function (row){
                var myNewItem = {
                    employee_id:  parseFloat(row.value),
                    employee_name: row.text,
                    changed: "yes"
                };
                grid.store.newItem(myNewItem);
            });
            employeeDialog.hide();
            clearDialogSelection();
        }

        // Инициализация при загрузке
        dojo.addOnLoad(function () {
            projectDataHandler(${form.projectId}, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, initProjectGrid);
            // Посторить грид, но не заполнять
            employeeDataHandler(0, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, initEmployeeGrid);
        });

        // Отправка данных на сервер для сохранения
        function savePlan(){
            var grid = dijit.byId("projectGrid");

            var monthBeg  = dojo.byId("monthBeg").value;
            var yearBeg   = dojo.byId("yearBeg").value;
            var monthEnd  = dojo.byId("monthEnd").value;
            var yearEnd   = dojo.byId("yearEnd").value;
            var projectId = dojo.byId("projectId").value;

            grid.store.fetch({query: {changed: "yes"}, queryOptions: {deep:true}, onComplete: function(items){
                saveEmployeeDataHandler(
                    projectId,
                    monthBeg,
                    yearBeg,
                    monthEnd,
                    yearEnd,
                    itemToJSON(grid.store, items),
                    actionAfterSave);
            }});

            // После сохранения перестраиваем гриды
            // TODO избавиться от $ - переделать на js
            function actionAfterSave(result){
                projectDataHandler(${form.projectId}, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, refreshProjectGrid);
                employeeDataHandler(0, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, refreshEmployeeGrid);
            }
        }
    </script>
</head>

<body>
<form:form method="post" commandName="<%= FORM %>">
    <table>
        <tr>
            <td>
                <span class="label">Месяц, год начала периода:</span>
            </td>
            <td>
                <form:select path="monthBeg" class="without_dojo" >
                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                </form:select>
            </td>
            <td>
                <form:select path="yearBeg" class="without_dojo" >
                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Месяц, год конца периода:</span>
            </td>
            <td>
                <form:select path="monthEnd" class="without_dojo" >
                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                </form:select>
            </td>
            <td>
                <form:select path="yearEnd" class="without_dojo" >
                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Выбор проекта:</span>
            </td>
            <td colspan="2">
                <form:select path="projectId">
                    <form:options items="${projectList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
    </table>

    <input style="width:150px;margin-left: 23px;" type="submit" value="Показать планы"/>
</form:form>
    <div id="projectGridDiv" style="height: 30em;"></div>
    <br/>
    <span id="spanEmployeeName"></span>
    <br/>
    <input type="checkbox" id="isFact" checked>Отображать фактическое значение</input>
    <br/>
    <div id="employeeGridDiv" style="height: 30em;"></div>
    <br>

    <div data-dojo-type="dijit/Dialog" data-dojo-id="employeeDialog" title="Add employee">

        <form:form commandName="<%= ADD_FORM %>">
            <table class="dijitDialogPaneContentArea">
                <tr>
                    <td><label>Центр </label></td>
                    <td>
                        <form:select path="divisionId" class="without_dojo" onchange="updateAdditionEmployeeList()">
                            <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    <td>
                </tr>
                <tr>
                    <td><label>Руководитель </label></td>
                    <td>
                        <form:select path="managerId" class="without_dojo" onchange="updateAdditionEmployeeList()">
                            <form:options items="${managerList}" itemLabel="employee.name" itemValue="employee.id"/>
                        </form:select>
                    <td>
                </tr>
                <tr>
                    <td><label>Должности </label></td>
                    <td>
                        <form:select path="projectRoleListId" multiple="true" onchange="updateAdditionEmployeeList()">
                            <form:options items="${projectRoleList}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    <td>
                </tr>
                <tr>
                    <td><label>Регионы </label></td>
                    <td>
                        <form:select path="regionListId" multiple="true" onchange="updateAdditionEmployeeList()">
                            <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    <td>
                </tr>
                <tr>
                    <td><label>Сотрудники </label></td>
                    <td>
                        <select id="additionEmployeeList" multiple="true" />
                    <td>
                </tr>
            </table>

            <div class="dijitDialogPaneActionBar">
                <button type="button" onclick="addRow();">Добавить</button>
            </div>
        </form:form>
    </div>
</body>
</html>