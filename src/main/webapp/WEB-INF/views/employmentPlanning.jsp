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
    @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
    @import "<%= getResRealPath("/resources/css/employmentPlanning.css", application) %>";
    <%--Для сокрытия строк грида, выходящих за грид--%>
    div.dojoxGridRow {width: 100%;}
    .tundra .dojoxGridHeader{
        background-color: white;
    }
    .editedCell {
        color: #FF8500;
        text-align: center;
    }
    .employeeLabel {
        font-size: 14px;
    }
</style>

<script type="text/javascript">
dojo.require("dojox.grid.DataGrid");
dojo.require("dojo.data.ItemFileWriteStore");
dojo.require("dojo.parser");
dojo.require("dijit.form.DateTextBox");
dojo.require("dojo.date.stamp");
dojo.require("dojo.date.locale");

require(["dijit/Dialog", "dijit/form/TextBox", "dijit/form/Button"]);

// Создает store для грида "проект-сотрудники"
function createStoreProject(dataJson, yearStart, monthStart, yearEnd, monthEnd){
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
        row["plan"] = '';
        data.items.push(row);
    });

    var store = new dojo.data.ItemFileWriteStore({data: data});

    return store;
}

// Создает structure для грида "проект-сотрудники"
function createLayoutProject(dataJson, yearStart, monthStart, yearEnd, monthEnd){

    var leftView = {
        noscroll: true,
        sortable: false,
        cells: [[
            {name: 'Сотрудник <img align="right" width="15px" src = "<c:url value="/resources/img/add.gif"/>" onclick="employeeDialog.show();"/>',
                field: 'employee_name',
                width: '200px',
                noresize: true,
                formatter: function(cellValue, rowIndex){
                    var grid = dijit.byId("projectGrid");
                    var item = grid.getItem(rowIndex);
                    if (item){
                        var employeeId = item["employee_id"];
                        var isChanged = item["isChanged"];
                        var plan = item["plan"]

                        if (isChanged == 1 && plan && plan != ""){
                            var isNew = item["isNew"];

                            if (isNew == "yes"){
                                var spanValue = dojo.create(
                                        "span",
                                        {
                                            innerHTML:cellValue,
                                            class: "editedCell"
                                        }
                                ).outerHTML

                                return spanValue + ' <img align="right" width="15px" src = "<c:url value="/resources/img/delete.png"/>" onclick="removeRow('+employeeId+');"/>'
                            } else{
                                return cellValue + ' <img align="right" width="15px" src = "<c:url value="/resources/img/delete.png"/>" onclick="cancelChange('+employeeId+');"/>'
                            }
                        }
                    }
                    return cellValue;
                }
            },

            {name: 'Планируемый процент занятости',
                width: '100px',
                editable: true,
                defaultValue: '',
                formatter: function(cellValue, rowIndex){
                    var grid = dijit.byId("projectGrid");
                    var item = grid.getItem(rowIndex);
                    if (item){
                        var isChanged = item["isChanged"];

                        if (isChanged == 1){
                            var spanValue = dojo.create(
                                    "span",
                                    {
                                        innerHTML:cellValue,
                                        class: "editedCell"
                                    }
                            ).outerHTML
                            return spanValue;
                        }
                    }
                    return cellValue;
                },
                field: 'plan',
                noresize: true,
                styles: 'text-align: center;'}
        ]]};

    var input_layout = [];

    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
        input_layout.push({
            name: getMonthByNumber(month)+ ", " + year,
            field: year + "_" + month,
            width: '100px',
            formatter: formatterData,
            editable: false,
            noresize: true,
            styles: 'text-align: center;'
        });
    });
    var middleView = {
        cells: [input_layout]
    };

    var layout = [leftView, middleView];

    return layout;
}

// Создает грид "проект-сотрудники"
function initProjectGrid(dataJson, yearStart, monthStart, yearEnd, monthEnd){
    var store = createStoreProject(dataJson, yearStart, monthStart, yearEnd, monthEnd);
    var layout = createLayoutProject(dataJson, yearStart, monthStart, yearEnd, monthEnd);

    var grid = new dojox.grid.DataGrid({
        id: 'projectGrid',
        store: store,
        structure: layout,
        rowSelector: '0px',
        autoHeight: true,
        styles: 'text-align: center;',
        canSort: function(){return false;},
        onApplyCellEdit: function(inValue, inRowIndex, inFieldIndex){
            if (inFieldIndex == "plan"){
                var item = grid.getItem(inRowIndex);
                if (item){
                    if (inValue == "undefined"){
                        grid.store.setValue(item, 'plan', '');
                        return;
                    }
                    grid.store.setValue(item, 'isChanged', 1);
                    grid.store.save();
                }
            }
        }
    });

    grid.placeAt("projectGridDiv");
    grid.startup();

    if (grid.rowCount>0){
        dojo.byId("spanEmployeeName").innerHTML = "Выберите пользователя для показа детализации планов";
    }

    dojo.connect(grid, "onRowClick", function(e){
        function action(){
            var item = grid.getItem(e.rowIndex);
            if (item){
                var employeeId = item["employee_id"];
                var employeeName = item["employee_name"];
                globalEmployeeName = employeeName;
                dojo.byId("spanEmployeeName").innerHTML = 'Загрузка сотрудника "'+employeeName+'" по проектам';
                dojo.byId("spanEmployeeName").hidden = true;
                dojo.byId("divEmployeeInfo").hidden = false;

                var monthBeg  = Number(dojo.byId("monthBeg").value);
                var yearBeg   = Number(dojo.byId("yearBeg").value);
                var monthEnd  = Number(dojo.byId("monthEnd").value);
                var yearEnd   = Number(dojo.byId("yearEnd").value);

                employeeDataHandler(employeeId, yearBeg, monthBeg, yearEnd, monthEnd, refreshEmployeeGrid);
            }
        }

        checkChanges(action);
    });
}

var globalEmployeeName;

// Динамически перестраивает грид "проект-сотрудники"
// yearStart, monthStart, yearEnd, monthEnd - не используется, нужны если менять структуру грида
function refreshProjectGrid(response, yearStart, monthStart, yearEnd, monthEnd){
    if (dojo.fromJson(response).length > 0){
        dojo.style(dojo.byId("grids"), 'display', '');
        dojo.style(dojo.byId("errorBox"), 'display', 'none');
    } else {
        dojo.style(dojo.byId("errorBox"), 'display', '');
        dojo.byId("errorBox").innerHTML = 'По данному проекту отсутвует запланированная занятость сотрудников';
    }

    var grid = dijit.byId("projectGrid");
    var store = createStoreProject(response, yearStart, monthStart, yearEnd, monthEnd);
    var layout = createLayoutProject(response, yearStart, monthStart, yearEnd, monthEnd);
    grid.store.close();
    grid.setStore(store);
    grid.setStructure(layout);
    grid.render();
}

var globalEmployeeId;
// Создает structure для грида "сотрудник-проекты"
function createLayoutEmployee(isFact, employeeId, yearStart, monthStart, yearEnd, monthEnd){
    globalEmployeeId = employeeId;
    var grid = dijit.byId("employeeGrid");
    var cnt = monthCount(yearStart, monthStart, yearEnd, monthEnd);

    var divEmployee = '<div style="text-align: left; font-size: 9px">Загрузка сотрудника: ' + globalEmployeeName + '</div>';
    var divProject = '<div style="text-align: center; float: both;">Проект</div>';

    var leftView = {
        noscroll: true,
        cells: [[
            {
                name: divEmployee + '<br/>' + divProject,
                field: 'project_name',
                width: '200px',
                noresize: true,
                formatter: function(cellValue, rowIndex){
                    var item = grid.getItem(rowIndex);
                    if (item){
                        var projectId = item["project_id"];
                        var isChanged = item["isChanged"];

                        if (isChanged == 1){
                            return cellValue + '<img align="right" width="15px" src = "<c:url value="/resources/img/delete.png"/>" onclick="cancelEmployeeChange('+projectId+');"/><img align="right" width="15px" src = "<c:url value="/resources/img/ok.png"/>" onclick="saveEmployeePlan('+employeeId+','+projectId+');"/>'
                        }
                    }
                    return cellValue;
                }
            }
        ]]
    };

    var dataLayout = [];

    var headerLayout = [];
    var padding = (dojo.isWebKit ? 1 : 1.25);
    var cellStyles = "padding-left: "+padding+"px; padding-right: "+padding+"px; text-align: center;";

    if (isFact){
        headerLayout.push({
            name: 'Среднее за период',
            colSpan: 2,
            headerStyles: "width: 100px; text-align: center;",
            cellStyles: cellStyles,
            noresize: true
        });

        iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
            headerLayout.push({
                name: getMonthByNumber(month)+ ", " + year,
                colSpan: 2,
                headerStyles: "width: 100px; text-align: center;",
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

    if (isFact){
        dataLayout.push({
            name: 'П',
            width: '50px',
            headerStyles: "width: 50px; text-align: center;",
            cellStyles: cellStyles,
            noresize: true,
            formatter: function(cellValue, rowIndex){
                var item = grid.getItem(rowIndex);
                var value = 0;
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
                    value+=Number(item[year+"_"+month]);
                });
                return textFormat(Math.round(value/cnt));
            }
        });

        dataLayout.push({
            name: 'Ф',
            width: '50px',
            headerStyles: "width: 50px; text-align: center;",
            cellStyles: cellStyles,
            noresize: true,
            formatter: function(cellValue, rowIndex){
                var item = grid.getItem(rowIndex);
                var value = 0;
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
                    value+=Number(item[year+"-"+month]);
                });
                return textFormat(Math.round(value/cnt));
            }
        });
    } else {
        dataLayout.push({
            name: 'Среднее за период',
            formatter: function(cellValue, rowIndex){
                var item = grid.getItem(rowIndex);
                var value = 0;
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
                    value+=Number(item[year+"_"+month]);
                });
                return textFormat(value/cnt);
            },
            width: '100px',
            noresize: true,
            styles: 'text-align: center;'
        });
    }

    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
        if (isFact){
            dataLayout.push({
                name: 'П',
                field: year + "_" + month,
                formatter: formatterData,
                width: '50px',
                headerStyles: "width: 50px; text-align: center;",
                cellStyles: cellStyles,
                editable: true,
                noresize: true
            });

            dataLayout.push({
                name: 'Ф',
                field: year + "-" + month,
                formatter: formatterData,
                width: '50px',
                headerStyles: "width: 50px; text-align: center;",
                cellStyles: cellStyles,
                noresize: true
            });
            } else {
                dataLayout.push({
                    name: getMonthByNumber(month)+ ", " + year,
                    field: year + "_" + month,
                    formatter: formatterData,
                    width: '100px',
                    editable: true,
                    noresize: true,
                    styles: 'text-align: center;'
            });
        }
    });

    var layout = [leftView, middleView];

    return layout;
}

// Первоначальные значения грида "сотрудник-проекты" - для отмены изменений
var clearData = {};

function calcTotalRow(items, yearStart, monthStart, yearEnd, monthEnd){
    var resultMap = [];

    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
        var planKey = year + '_' + month;
        var factKey = year + '-' + month;

        resultMap[planKey] = 0;
        resultMap[factKey] = 0;
    });

    dojo.forEach(items, function(item){
        var projectId = item["project_id"];
        if (projectId != 0){
            iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
                var planKey = year + '_' + month;
                var factKey = year + '-' + month;

                var planVal = Number(item[planKey]);
                var factVal = Number(item[factKey]);

                if (item["isChanged"] == 1){
                    planVal = clearData[projectId][planKey];
                    factVal = clearData[projectId][factKey];
                }

                if (factVal){
                    resultMap[factKey] += factVal;
                }

                if (planVal){
                    resultMap[planKey] += planVal;
                }
            });
        }
    });

    return resultMap;
}

// Пересчитывает итоговые строки у града "сотрудник-проекты"
function updateTotalRow(resultMap, yearStart, monthStart, yearEnd, monthEnd){
    var grid = dijit.byId("employeeGrid");

    grid.store.fetch({query: {}, queryOptions: {deep:true}, onComplete: function(items){
        grid.store.fetch({query: {project_id: 0}, queryOptions: {deep:true}, onComplete: function(items){
            dojo.forEach(items, function(item){
                var planKey = item["year"] + '_' + item["month"];
                var factKey = item["year"] + '-' + item["month"];

                grid.store.setValue(item, planKey, 0);//resultMap[planKey]);
                grid.store.setValue(item, factKey, resultMap[factKey]);
            });
        }});

        grid.store.save();
    }});
}

// Создает store для грида "сотрудник-проекты"
function createStoreEmployee(dataJson, yearStart, monthStart, yearEnd, monthEnd){
    var data = {
        identifier: "project_id",
        items: []
    };

    var data_list = dojo.fromJson(dataJson);

    // Добавляем итоговою строку
    var totalRowMap = calcTotalRow(data_list, yearStart, monthStart, yearEnd, monthEnd);
    var totalRow = {"project_id": 0, "project_name": 'Итого'};
    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
        var planKey = year + '_' + month;
        var factKey = year + '-' + month;

        totalRow[planKey] = totalRowMap[planKey];
        totalRow[factKey] = totalRowMap[factKey];
    });
    data_list.push(totalRow);

    dojo.forEach(data_list, function (row) {
        for (var field in row) {
            if (typeof row[field] == typeof undefined) {
                row[field] = "";
            }
        }

        iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(month, year){
            var planKey = year + '_' + month;
            var factKey = year + '-' + month;

            if (row[planKey] == undefined){
                row[planKey] = 0;
            }

            if (row[factKey] == undefined){
                row[factKey] = 0;
            }
        });

        data.items.push(row);
    });

    // Сохранение первоначальный данных
    dojo.forEach(data.items, function(row){
        var projectId = row["project_id"];
        clearData[projectId] = {};
        for(var key in row){
            clearData[projectId][key] = row[key];
        }
    });

    var store = new dojo.data.ItemFileWriteStore({data: data});
    return store;
}

// Создает грид "сотрудник-проекты"
function initEmployeeGrid(dataJson, employeeId, yearStart, monthStart, yearEnd, monthEnd){
    var store = createStoreEmployee(dataJson, yearStart, monthStart, yearEnd, monthEnd);

    var isFact = dojo.byId("isFactCheckBox").checked;
    var layout = createLayoutEmployee(isFact, employeeId, yearStart, monthStart, yearEnd, monthEnd);

    var grid = new dojox.grid.DataGrid({
        id: 'employeeGrid',
        store: store,
        structure: layout,
        rowSelector: '0px',
        width: '400px',
        autoHeight: true,
        onApplyCellEdit: function(inValue, inRowIndex, inFieldIndex){
            var item = grid.getItem(inRowIndex);
            if (item){
                if (inValue == clearData[item["project_id"]][inFieldIndex]){
                    return;
                }
                var value = item["fields"];
                if (value){
                    value += ";" + inFieldIndex;
                } else {
                    value = inFieldIndex;
                }
                grid.store.setValue(item, 'isChanged', 1);
                grid.store.setValue(item, 'fields', value);
            }
            grid.store.save();
        },
        canEdit: function(inCell, inRowIndex) {
            var item = grid.getItem(inRowIndex);
            return item["project_id"]>0;
        }
    });

    grid.placeAt("employeeGridDiv");
    grid.startup();
};

// Динамически перестраивает грид "сотрудник-проекты"
function refreshEmployeeGrid(response, employeeId, yearStart, monthStart, yearEnd, monthEnd){
    var isFact = dojo.byId("isFactCheckBox").checked;
    var store = createStoreEmployee(response, yearStart, monthStart, yearEnd, monthEnd);
    var layout = createLayoutEmployee(isFact, employeeId, yearStart, monthStart, yearEnd, monthEnd);
    var grid = dijit.byId("employeeGrid");

    grid.store.close();
    grid.setStore(store);
    grid.setStructure(layout);


    var projectId = dojo.byId("projectId").value;
    function actionSelection(items){
        if (grid.selection.selectedIndex >= 0){
            grid.selection.setSelected(grid.selection.selectedIndex, false);
        }
        dojo.forEach(items, function(item){
            var index = grid.getItemIndex(item);
            grid.selection.setSelected(index, true);
        });
    }
    grid.store.fetch({query: {project_id: projectId}, onComplete: actionSelection, queryOptions: {deep:true}});

    grid.render();
}

// Обновляет список сотрудников на форме добавления сотрудников
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

            var selectSize = 0;
            dojo.forEach(dojo.fromJson(response), function (row) {
                if(map[row["employee_id"]] === undefined){
                    ++selectSize;
                    dojo.create("option", { value: row["employee_id"], innerHTML: row["employee_name"]}, employeeSelect);
                }
            });

            if (selectSize == 0){
                dojo.create("option", { value: "-1", innerHTML: "Сотрудников не найдено", disabled: true}, employeeSelect);
                return;
            }
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
        var employeeId = parseFloat(row.value);

        var myNewItem = {
            employee_id: employeeId,
            employee_name: row.text,
            isChanged: 1,
            isNew: "yes",
            plan: "0"
        };
        grid.store.newItem(myNewItem);
    });
    employeeDialog.hide();
    clearDialogSelection();
}

// Удаление строки грида "проект-сотрудники"
function removeRow(employeeId){
    var grid = dijit.byId("projectGrid");

    grid.store.fetch({query: {employee_id: employeeId}, onComplete: delRow, queryOptions: {deep:true}});
    function delRow(items){
        dojo.forEach(items, function(item){
            grid.store.deleteItem(item);
        });
    }

}

// Отмена изменения плана на гриде "проект-сотрудники"
function cancelChange(employeeId){
    var grid = dijit.byId("projectGrid");

    grid.store.fetch({query: {employee_id: employeeId}, queryOptions: {deep:true}, onComplete: function(items){
        dojo.forEach(items, function(item){
            grid.store.setValue(item, 'plan', '');
            grid.store.setValue(item, 'isChanged', 0);
            grid.store.setValue(item, 'isNew', 'no');
        });
        grid.store.save();
    }});
}

// Откат для изменений для строки на гриде "сотрудник-проекты"
function cancelEmployeeChange(projectId){
    var grid = dijit.byId("employeeGrid");

    grid.store.fetch({query: {project_id: projectId}, queryOptions: {deep:true}, onComplete: function(items){
        dojo.forEach(items, function(item){
            var project = clearData[projectId];
            for(key in project){
                if (key!="project_id" && key!="isChanged"){
                    grid.store.setValue(item, key, project[key]);
                }
            }
            grid.store.setValue(item, 'isChanged', 0);
            grid.store.setValue(item, 'fields', null);
        });
        grid.store.save();
    }});
}

// Инициализация при загрузке
dojo.addOnLoad(function () {
    projectDataHandler(${form.projectId}, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, initProjectGrid);
    // Посторить грид, но не заполнять
    employeeDataHandler(0, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, initEmployeeGrid);
});

// Отправка данных на сервер для сохранения. Грид проект-сотрудники
function saveProjectPlan(){
    checkChanges(saving);
    function saving(){
        var grid = dijit.byId("projectGrid");

        var monthBeg  = Number(dojo.byId("monthBeg").value);
        var yearBeg   = Number(dojo.byId("yearBeg").value);
        var monthEnd  = Number(dojo.byId("monthEnd").value);
        var yearEnd   = Number(dojo.byId("yearEnd").value);
        var projectId = Number(dojo.byId("projectId").value);

        grid.store.fetch({query: {isChanged: 1}, queryOptions: {deep:true}, onComplete: function(items){
            saveEmployeeDataHandler(
                    projectId,
                    monthBeg,
                    yearBeg,
                    monthEnd,
                    yearEnd,
                    '{"employee": [' + itemToJSON(grid.store, items) + ']}',
                    actionAfterSaveProject);
        }});

        // После сохранения перестраиваем гриды
        // TODO избавиться от $ - переделать на js
        function actionAfterSaveProject(result){
            projectDataHandler(projectId, yearBeg, monthBeg, yearEnd, monthEnd, refreshProjectGrid);
            employeeDataHandler(0, ${form.yearBeg}, ${form.monthBeg}, ${form.yearEnd}, ${form.monthEnd}, refreshEmployeeGrid);
        }
    }
}

// Отправлка данных на сервер для сохранения. Грид сотрудник-проекты
function saveEmployeePlan(employeeId, projectId){
    var grid = dijit.byId("employeeGrid");

    function actionAfterSaveEmployee(result){
        var monthBeg  = Number(dojo.byId("monthBeg").value);
        var yearBeg   = Number(dojo.byId("yearBeg").value);
        var monthEnd  = Number(dojo.byId("monthEnd").value);
        var yearEnd   = Number(dojo.byId("yearEnd").value);
        var projectId = Number(dojo.byId("projectId").value);

        projectDataHandler(projectId, yearBeg, monthBeg, yearEnd, monthEnd, refreshProjectGrid);
        // Перерасчет итоговой строки
        grid.store.fetch({query: {}, queryOptions: {deep:true}, onComplete: function(allRows){
            var totalRowMap = calcTotalRow(allRows, yearBeg, monthBeg, yearEnd, monthEnd);
            grid.store.fetch({query: {project_id: 0}, queryOptions: {deep:true}, onComplete: function(items){
                dojo.forEach(items, function(item){
                    iterateMonth(yearBeg, monthBeg, yearEnd, monthEnd, function(month, year){
                        var planKey = year + '_' + month;
                        var factKey = year + '-' + month;
                        grid.store.setValue(item, planKey, totalRowMap[planKey]);
                        grid.store.setValue(item, factKey, totalRowMap[factKey]);
                    });
                });
                grid.store.save();
            }});
        }});

        grid.store.fetch({query: {}, queryOptions: {deep:true}, onComplete: function(allRows){
            dojo.forEach(allRows, function(row){
                for(var key in row){
                    clearData[projectId][key] = row[key];
                }
            })
        }});
    }

    function saveProjectData(items){
        saveProjectDataHandler(
                '{"project": [' + itemToJSON(grid.store, items) + ']}',
                employeeId,
                actionAfterSaveEmployee);

        dojo.forEach(items, function(item){
            var projectId = item["project_id"];
            for(var key in item){
                clearData[projectId][key] = Number(item[key]);
            }
            grid.store.setValue(item, 'isChanged', 0);
            grid.store.setValue(item, 'fields', null);
        });

        grid.store.save();
    }

    if (isNumber(projectId)){
        grid.store.fetch({query: {project_id: projectId, isChanged: 1}, queryOptions: {deep:true}, onComplete: saveProjectData});
    } else {
        grid.store.fetch({query: {isChanged: 1}, queryOptions: {deep:true}, onComplete: saveProjectData});
    }
}

function checkChanges(handler){
    var grid2 = dijit.byId("employeeGrid");
    grid2.store.fetch({query: {isChanged: 1}, queryOptions: {deep:true}, onComplete: function(items){
        if (items.length>0){
            alert("Перед сменой сотрудника необходимо сохранить или отменить изменения");
            return;
        } else {
            handler();
        }
    }});
}

function submitSaveButton(){
    checkChanges(save);
    function save(){
        var monthBeg  = Number(dojo.byId("monthBeg").value);
        var yearBeg   = Number(dojo.byId("yearBeg").value);
        var monthEnd  = Number(dojo.byId("monthEnd").value);
        var yearEnd   = Number(dojo.byId("yearEnd").value);
        var projectId = Number(dojo.byId("projectId").value);

        var dateStart = new Date(yearBeg, monthBeg);
        var dateEnd = new Date(yearEnd, monthEnd);

        if (dateEnd < dateStart){
            alert("Дата конца периода превышает дату начала");
        } else {
            dojo.byId("divEmployeeInfo").hidden = true;
            dojo.byId("spanEmployeeName").hidden = false;
            dojo.byId("spanEmployeeName").innerHTML = "Выберите пользователя для показа детализации планов";
            projectDataHandler(projectId, yearBeg, monthBeg, yearEnd, monthEnd, refreshProjectGrid);
            employeeDataHandler(0, yearBeg, monthBeg, yearEnd, monthEnd, refreshEmployeeGrid);
        }
    }
}

function hidePlan(){
    var isFact = dojo.byId("isFactCheckBox").checked;
    var grid2 = dijit.byId("employeeGrid");

    var monthStart = Number(dojo.byId("monthBeg").value);
    var yearStart  = Number(dojo.byId("yearBeg").value);
    var monthEnd   = Number(dojo.byId("monthEnd").value);
    var yearEnd    = Number(dojo.byId("yearEnd").value);

    var layout = createLayoutEmployee(isFact, globalEmployeeId, yearStart, monthStart, yearEnd, monthEnd)
    grid2.setStructure(layout);
    grid2.render();
}

function updateProjectList(){
    var selectDivision = dojo.byId("selectDivisionId");
    additionProjectDataHandler(selectDivision.value, function(response){
        clearSelectValues(dojo.byId("projectId"));
        dojo.forEach(dojo.fromJson(response), function (row) {
            dojo.create("option", { value: row["project_id"], innerHTML: row["project_name"]}, dojo.byId("projectId"));
        });
    });
}
</script>
</head>

<body>
<form:form method="post" commandName="employmentPlanningForm" cssClass="employmentPlanningForm">
    <table class="no_border employmentPlanningTable">
        <tr>
            <td><span class="label">Центр</span></td>
            <td colspan="2">
                <form:select path="selectDivisionId" onchange="updateProjectList()" cssClass="bigSelect">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <input style="width:150px;margin-left: 23px;" type="button" value="Показать планы" onclick="submitSaveButton();"/>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Месяц, год начала периода:</span>
            </td>
            <td>
                <form:select path="monthBeg">
                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                </form:select>
            </td>
            <td>
                <form:select path="yearBeg">
                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                </form:select>
            </td>
            <td>
                <input style="width:150px;margin-left: 23px;" type="button" value="Сохранить" onclick="saveProjectPlan();"/>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Месяц, год конца периода:</span>
            </td>
            <td>
                <form:select path="monthEnd">
                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                </form:select>
            </td>
            <td>
                <form:select path="yearEnd">
                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                </form:select>
            </td>
            <td></td>
        </tr>
        <tr>
            <td>
                <span class="label">Выбор проекта:</span>
            </td>
            <td colspan="2">
                <form:select path="projectId"  cssClass="bigSelect">
                    <form:option label="" value="${all}"/>
                    <form:options items="${projectList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td></td>
        </tr>
    </table>
</form:form>

<div class="errors_box" id="errorBox" style="display: none"></div>
<div id="grids"  style="display: none">
    <div id="projectGridDiv" style="width: auto; max-width: 100%;"></div><br/>

    <span id="spanEmployeeName" class="employeeLabel">&nbsp;</span>

    <div id="divEmployeeInfo" hidden="true">
        <input type="checkbox" id="isFactCheckBox" onclick="hidePlan()" checked><span class="employeeLabel">Отображать фактическое значение</span></input><br/>
        <div id="employeeGridDiv"></div>
    </div>
</div>

<div data-dojo-type="dijit/Dialog" data-dojo-id="employeeDialog" title="Добавить сотрудника">

    <form:form commandName="<%= ADD_FORM %>">
        <table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
            <tr>
                <td><label>Центр </label></td>
                <td>
                    <form:select path="divisionId" onchange="updateAdditionEmployeeList()">
                        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                <td>
            </tr>
            <tr>
                <td><label>Руководитель </label></td>
                <td>
                    <form:select path="managerId" onchange="updateAdditionEmployeeList()">
                        <form:option label="Все руководители" value="${all}"/>
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
            <button type="button" onclick="employeeDialog.hide();">Отмена</button>
        </div>

    </form:form>
</div>
</body>
</html>