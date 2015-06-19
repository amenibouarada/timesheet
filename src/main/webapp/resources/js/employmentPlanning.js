dojo.require("dojox.grid.DataGrid");
dojo.require("dojo.data.ItemFileWriteStore");
dojo.require("dojo.parser");
dojo.require("dijit.form.DateTextBox");
dojo.require("dojo.date.stamp");
dojo.require("dojo.date.locale");

require(["dijit/Dialog", "dijit/form/TextBox", "dijit/form/Button"]);

var PROBLEM_SOLVING_MESSAGE = 'Обратитесь к администратору системы или сообщите о проблеме через форму обратной связи.';

var projectPlanStore = {};
var projectPlanLayout = {};

var employeesPlansStore = [];
var employeesPlansLayoutWithFact = {}
var employeesPlansLayoutWithoutFact = {};
function employeesPlansLayout(){
    var isFact = dojo.byId("isFactCheckBox").checked;
    return isFact ? employeesPlansLayoutWithFact : employeesPlansLayoutWithoutFact;
}

// Первоначальные значения грида "сотрудник-проекты" - для отмены изменений
var clearData = {};
var gPlan;
var gEmployeeName;
var gEmployeeId;

// Инициализация при загрузке
dojo.addOnLoad(function () {
    employeesPlansHandler(gProjectId, gYearBegin, gMonthBegin, gYearEnd, gMonthEnd);
    // Построить грид "проект-сотрудники"
    initProjectGrid();
    // Посторить грид "сотрудник-проекты", но не заполнять
    initEmployeeGrid(0, gYearBegin, gMonthBegin, gYearEnd, gMonthEnd);
    // обновим руководителей на форме добавления сотрудников
    updateManagerListByDivision();
    // и сразу же обновим список сотрудников
    updateAdditionEmployeeList();
});

function updateGrids(){
    gPlan = null;
    employeesPlansHandler(gProjectId, gYearBegin, gMonthBegin, gYearEnd, gMonthEnd);
    refreshProjectGrid();
    refreshEmployeeGrid(0, gYearBegin, gMonthBegin, gYearEnd, gMonthEnd);
}

function textFormat(value, color) {
    if (color === undefined) {
        color = 'gray';
    }

    if (value === undefined || value == null) {
        value = "0";
    }

    if (isNumber(value)) {
        value = Math.round(value);
        if (value > 100) {
            color = "#E32636";
        } else if (value > 0) {
            color = "black";
        }
    }

    return dojo.create(
        "span",
        {
            innerHTML: value + '%',
            style: "color: " + color + "; font-weight: bold"
        }
    ).outerHTML
}

// Формат вывода ячеек в гриде
function formatterData(value) {
    return textFormat(value);
}

// Делает ajax запрос по занятости сотрудников на проекте, полученный json ответ отправляет в функцию handler(json_value)
function employeesPlansHandler(projectId, yearStart, monthStart, yearEnd, monthEnd) {
    function setStoresByGotPlans(response){
        var emplPlans = dojo.fromJson(response || "[]");
        projectPlans = {
            identifier: "employee_id",
            items: []
        };
        for (var employeeNum in emplPlans){
            var employee = emplPlans[employeeNum];
            var element = {
                employee_id     : employee.employee_id,
                employee_name   : employee.employee_name
            };
            for (var employeeProjectsNum in employee["planList"]){
                var employeeProjects = employee["planList"][employeeProjectsNum];
                for (var projectKey in employeeProjects){
                    if (projectKey != "project_id" && projectKey != "project_name"){
                        if (element[projectKey] == undefined){
                            element[projectKey] = 0;
                        }
                        element[projectKey] += employeeProjects[projectKey];
                    }
                }
            }
            projectPlans.items.push(element);

            // сохраним данные для грида сотрудник-проекты
            employeesPlansStore[employee.employee_id] =
                createStoreEmployee(employee["planList"], yearStart, monthStart, yearEnd, monthEnd);
        }
        // добавим пустой store, когда не выбран ни один из сотрудников
        employeesPlansStore[0] = createStoreEmployee([], yearStart, monthStart, yearEnd, monthEnd);
        employeesPlansLayoutWithFact    = createLayoutEmployee(true, 0, yearStart, monthStart, yearEnd, monthEnd);
        employeesPlansLayoutWithoutFact = createLayoutEmployee(false, 0, yearStart, monthStart, yearEnd, monthEnd);
        projectPlanStore    = createStoreProject(projectPlans.items);
        projectPlanLayout   = createLayoutProject(yearStart, monthStart, yearEnd, monthEnd);
    }

    dojo.xhrGet({
        url: "/employmentPlanning/getEmployeesPlanAsJSON",
        content: {
            yearBeg: yearStart,
            monthBeg: monthStart,
            yearEnd: yearEnd,
            monthEnd: monthEnd,
            projectId: projectId
        },
        handleAs: "text",
        sync: true,
        load: function (response, ioArgs) {
            setStoresByGotPlans(response);
        },
        error: function (response, ioArgs) {
            alert('projectDataHandler Panic !');
        }
    });
}

// Делает ajax запрос, для сохранения планируемого процента занятости
function saveEmployeeDataHandler(projectId, monthBeg, yearBeg, monthEnd, yearEnd, jsonData, handler) {
    dojo.xhrPost({
        url: "/employmentPlanning/setEmployeeProjectAsJSON",
        content: {
            projectId: projectId,
            monthBeg: monthBeg,
            yearBeg: yearBeg,
            monthEnd: monthEnd,
            yearEnd: yearEnd,
            jsonData: jsonData
        },
        handleAs: "text",
        load: function (response, ioArgs) {
            handler(response);
        },
        error: function (response, ioArgs) {
            alert('saveEmployeeDataHandler Panic !');
        }
    });
}

// Делает ajax запрос, для сохранения планируемого процента занятости
function saveProjectDataHandler(jsonData, employeeId, handler) {
    dojo.xhrPost({
        url: "/employmentPlanning/setProjectDataAsJSON",
        content: {
            jsonData: jsonData,
            employeeId: employeeId
        },
        handleAs: "text",
        load: function (response, ioArgs) {
            handler(response);
        },
        error: function (response, ioArgs) {
            alert('saveProjectDataHandler Panic !');
        }
    });
}

// Переводит объект в JSON
function itemToJSON(store, items) {
    var data = [];
    if (items && store) {
        for (var n = 0; n < items.length; ++n) {
            var json = {};
            var item = items[n];
            if (item) {
                // Determine the attributes we need to process.
                var attributes = store.getAttributes(item);
                if (attributes && attributes.length > 0) {
                    for (var i = 0; i < attributes.length; i++) {
                        var values = store.getValues(item, attributes[i]);
                        if (values) {
                            // Handle multivalued and single-valued attributes.
                            if (values.length > 1) {
                                json[attributes[i]] = [];
                                for (var j = 0; j < values.length; j++) {
                                    var value = values[j];
                                    // Check that the value isn't another item. If it is, process it as an item.
                                    if (store.isItem(value)) {
                                        json[attributes[i]].push(dojo.fromJson(itemToJSON(store, value)));
                                    } else {
                                        json[attributes[i]].push(value);
                                    }
                                }
                            } else {
                                if (store.isItem(values[0])) {
                                    json[attributes[i]] = dojo.fromJson(itemToJSON(store, values[0]));
                                } else {
                                    json[attributes[i]] = values[0];
                                }
                            }
                        }
                    }
                }
                data.push(dojo.toJson(json));
            }
        }
    }
    return data;
}

//-----------------------------------------------------------------------------------//

// Создает store для грида "проект-сотрудники"
function createStoreProject(projectPlans) {
    var data = {
        identifier: "employee_id",
        items: []
    };

    dojo.forEach(projectPlans, function (row) {
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
function createLayoutProject(yearStart, monthStart, yearEnd, monthEnd) {
    var leftView = {
        noscroll: true,
        sortable: false,
        cells: [
            [
                {name: 'Сотрудник <img align="right" width="15px" src = "' + addImg + '" onclick="employeeDialog.show();"/>',
                    field: 'employee_name',
                    width: '200px',
                    noresize: true,
                    formatter: function (cellValue, rowIndex) {
                        var grid = dijit.byId("projectGrid");
                        var item = grid.getItem(rowIndex);
                        if (item) {
                            var employeeId = item["employee_id"];
                            var isChanged = item["isChanged"];
                            var plan = item["plan"]

                            if (isChanged == 1 && plan && plan != "") {
                                var isNew = item["isNew"];

                                if (isNew == 1) {
                                    var spanValue = dojo.create(
                                        "span",
                                        {
                                            innerHTML: cellValue,
                                            class: "editedCell"
                                        }
                                    ).outerHTML

                                    return spanValue + ' <img align="right" width="15px" src = "' + deleteImg + '" onclick="removeRow(' + employeeId + ');"/>'
                                } else {
                                    return cellValue + ' <img align="right" width="15px" src = "' + deleteImg + '" onclick="cancelChange(' + employeeId + ');"/>'
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
                    formatter: function (cellValue, rowIndex) {
                        var grid = dijit.byId("projectGrid");
                        var item = grid.getItem(rowIndex);
                        if (item) {
                            var isChanged = item["isChanged"];

                            if (isChanged == 1) {
                                var spanValue = dojo.create(
                                    "span",
                                    {
                                        innerHTML: cellValue,
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
            ]
        ]};

    var input_layout = [];

    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
        input_layout.push({
            name: getMonthByNumber(month) + ", " + year,
            field: year + "_" + month,
            width: '100px',
            editable: false,
            noresize: true,
            styles: 'text-align: center;',
            formatter: formatterData
        });
    });
    var middleView = {
        cells: [input_layout]
    };

    var layout = [leftView, middleView];
    return layout;
}

// Создает грид "проект-сотрудники"
function initProjectGrid() {
    var grid = new dojox.grid.DataGrid({
        id: 'projectGrid',
        store: projectPlanStore,
        structure: projectPlanLayout,
        rowSelector: '0px',
        autoHeight: true,
        styles: 'text-align: center;',
        canSort: function () {
            return false;
        },
        onApplyCellEdit: function (inValue, inRowIndex, inFieldIndex) {
            if (inFieldIndex == "plan") {
                var item = grid.getItem(inRowIndex);
                if (item) {
                    if (inValue == "undefined") {
                        grid.store.setValue(item, 'plan', '');
                        grid.store.save();
                        return;
                    }
                    grid.store.setValue(item, 'isChanged', 1);
                    grid.store.save();

                    var projectId = dojo.byId("projectId").value;
                    forceCalcEmployeeGrid(projectId, inValue);
                }
            }
        }
    });

    grid.placeAt("projectGridDiv");
    grid.startup();

    if (grid.rowCount > 0) {
        dojo.byId("spanEmployeeName").innerHTML = "Выберите пользователя для показа детализации планов";
    }

    dojo.connect(grid, "onRowClick", function (e) {
        function action() {
            var item = grid.getItem(e.rowIndex);
            if (item) {
                var employeeId = item["employee_id"];
                var employeeName = item["employee_name"];
                gPlan = item["plan"];

                if (gEmployeeId == employeeId) {
                    // кликают несколько раз на одну строку
                    return;
                }
                gEmployeeName = employeeName;
                gEmployeeId = employeeId;

                dojo.byId("spanEmployeeName").innerHTML = 'Загрузка сотрудника "' + employeeName + '" по проектам';
                dojo.byId("spanEmployeeName").hidden = true;
                dojo.byId("divEmployeeInfo").hidden = false;

                refreshEmployeeGrid(employeeId, gYearBegin, gMonthBegin, gYearEnd, gMonthEnd);
            }
        }
        checkChanges(action);
    });
}

// Перерасчет грида "сотрудник-проекты" после ввода планируемого процента занятости в грид "проект-сотрудники"
function forceCalcEmployeeGrid(projectId, plan) {
    var projectGrid = dijit.byId("projectGrid");
    var employeeGrid = dijit.byId("employeeGrid");

    employeeGrid.store.fetch({query: {project_id: projectId}, queryOptions: {deep: true}, onComplete: function (items) {
        dojo.forEach(items, function (it) {
            var fieldsValue = it["fields"];
            iterateMonth(gYearBegin, gMonthBegin, gYearEnd, gMonthEnd, function (month, year) {
                var key = year + "_" + month;
                employeeGrid.store.setValue(it, key, Number(plan));
                if (fieldsValue) {
                    fieldsValue += ";" + key;
                } else {
                    fieldsValue = key;
                }
            });
            employeeGrid.store.setValue(it, 'fields', fieldsValue);
            employeeGrid.store.save();
        });
    }});

    // Перерасчет итоговой строки
    recalculateTotalRow();
}

// Перерасчет итоговых строк на гриде "сотрудник-проекты"
function recalculateTotalRow() {
    var employeeGrid = dijit.byId("employeeGrid");

    // Перерасчет итоговой строки
    employeeGrid.store.fetch({query: {}, queryOptions: {deep: true}, onComplete: function (allRows) {
        var totalRowMap = calcTotalRow(allRows, gYearBegin, gMonthBegin, gYearEnd, gMonthEnd);
        employeeGrid.store.fetch({query: {project_id: 0}, queryOptions: {deep: true}, onComplete: function (items) {
            dojo.forEach(items, function (item) {
                iterateMonth(gYearBegin, gMonthBegin, gYearEnd, gMonthEnd, function (month, year) {
                    var planKey = year + '_' + month;
                    var factKey = year + '-' + month;
                    employeeGrid.store.setValue(item, planKey, totalRowMap[planKey]);
                    employeeGrid.store.setValue(item, factKey, totalRowMap[factKey]);
                });
            });
            employeeGrid.store.save();
            copyTotalRow(totalRowMap);
        }});
    }});
}

// Перерасчет строки в гриде "проект-сотрудник"
// "Переносит" итоговую строку из грида "сотрудник-проекты" в грид "проект-сотрудник" для выбранного сотрудника
function copyTotalRow(totalRowMap) {
    var projectGrid = dijit.byId("projectGrid");
    projectGrid.store.fetch({query: {employee_id: Number(gEmployeeId)}, queryOptions: {deep: true}, onComplete: function (items) {
        dojo.forEach(items, function (item) {
            iterateMonth(gYearBegin, gMonthBegin, gYearEnd, gMonthEnd, function (month, year) {
                var planKey = year + '_' + month;
                projectGrid.store.setValue(item, planKey, totalRowMap[planKey]);
            });
        });
        projectGrid.store.save();
    }});

}

// Динамически перестраивает грид "проект-сотрудники"
function refreshProjectGrid() {
    if (projectPlanStore._jsonData.items.length > 0) {
        dojo.style(dojo.byId("grids"), 'display', '');
        dojo.style(dojo.byId("errorBox"), 'display', 'none');
    } else {
        dojo.style(dojo.byId("errorBox"), 'display', '');
        dojo.style(dojo.byId("grids"), 'display', '');
        dojo.byId("errorBox").innerHTML = 'По данному проекту отсутвует запланированная занятость сотрудников';
    }
    var grid = dijit.byId("projectGrid");
    grid.store.close();
    grid.setStore(projectPlanStore);
    grid.setStructure(projectPlanLayout);
    grid.render();
}

// Создает structure для грида "сотрудник-проекты"
function createLayoutEmployee(isFact, employeeId, yearStart, monthStart, yearEnd, monthEnd) {
    gEmployeeId = employeeId;
    var grid = dijit.byId("employeeGrid");
    var cnt = monthCount(yearStart, monthStart, yearEnd, monthEnd);

    var divEmployee = '<div style="text-align: left; font-size: 9px">Загрузка сотрудника: ' + gEmployeeName + '</div>';
    var divProject = '<div style="text-align: center; float: both;">Проект</div>';

    var leftView = {
        noscroll: true,
        cells: [
            [
                {
                    name: divEmployee + '<br/>' + divProject,
                    field: 'project_name',
                    width: '200px',
                    noresize: true,
                    formatter: function (cellValue, rowIndex) {
                        var item = grid.getItem(rowIndex);
                        if (item) {
                            var projectId = item["project_id"];
                            var isChanged = item["isChanged"];

                            if (isChanged == 1) {
                                return cellValue + '<img align="right" width="15px" src = "' +
                                    deleteImg +
                                    '" onclick="cancelEmployeeChange(' + projectId + ');"/><img align="right" width="15px" src = "' +
                                    okImg +
                                    '" onclick="saveEmployeePlan(' +
                                    employeeId +
                                    ',' +
                                    projectId +
                                    ');"/>'
                            }
                        }
                        return cellValue;
                    }
                }
            ]
        ]
    };

    var dataLayout = [];

    var headerLayout = [];
    var padding = (dojo.isWebKit ? 1 : 1.25);
    var cellStyles = "padding-left: " + padding + "px; padding-right: " + padding + "px; text-align: center;";

    if (isFact) {
        headerLayout.push({
            name: 'Среднее за период',
            colSpan: 2,
            headerStyles: "width: 100px; text-align: center;",
            cellStyles: cellStyles,
            noresize: true
        });

        iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
            headerLayout.push({
                name: getMonthByNumber(month) + ", " + year,
                colSpan: 2,
                headerStyles: "width: 100px; text-align: center;",
                cellStyles: cellStyles,
                noresize: true
            });
        });
    }


    var middleView = {};

    if (isFact) {
        middleView.cells = [headerLayout, dataLayout];
        middleView.onBeforeRow = function (inDataIndex, inSubRows) {
            var hidden = (inDataIndex >= 0);

            for (var i = inSubRows.length - 2; i >= 0; i--) {
                inSubRows[i].hidden = hidden;
            }
        }
    } else {
        middleView.cells = [dataLayout];
    }

    if (isFact) {
        dataLayout.push({
            name: 'П',
            width: '50px',
            headerStyles: "width: 50px; text-align: center;",
            cellStyles: cellStyles,
            noresize: true,
            formatter: function (cellValue, rowIndex) {
                var item = grid.getItem(rowIndex);
                var value = 0;
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
                    value += Number(item[year + "_" + month]);
                });
                return textFormat(Math.round(value / cnt));
            }
        });

        dataLayout.push({
            name: 'Ф',
            width: '50px',
            headerStyles: "width: 50px; text-align: center;",
            cellStyles: cellStyles,
            noresize: true,
            formatter: function (cellValue, rowIndex) {
                var item = grid.getItem(rowIndex);
                var value = 0;
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
                    value += Number(item[year + "-" + month]);
                });
                return textFormat(Math.round(value / cnt));
            }
        });
    } else {
        dataLayout.push({
            name: 'Среднее за период',
            formatter: function (cellValue, rowIndex) {
                var item = grid.getItem(rowIndex);
                var value = 0;
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
                    value += Number(item[year + "_" + month]);
                });
                return textFormat(value / cnt);
            },
            width: '100px',
            noresize: true,
            styles: 'text-align: center;'
        });
    }

    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
        if (isFact) {
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
                name: getMonthByNumber(month) + ", " + year,
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

// Возвращает итоговую строку для грида "сотрудник-проекты"
function calcTotalRow(items, yearStart, monthStart, yearEnd, monthEnd) {
    var resultMap = [];

    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
        var planKey = year + '_' + month;
        var factKey = year + '-' + month;

        resultMap[planKey] = 0;
        resultMap[factKey] = 0;
    });

    dojo.forEach(items, function (item) {
        var projectId = item["project_id"];
        if (projectId != 0) {
            iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
                var planKey = year + '_' + month;
                var factKey = year + '-' + month;

                var planVal = Number(item[planKey]);
                var factVal = Number(item[factKey]);

                if (factVal) {
                    resultMap[factKey] += factVal;
                }

                if (planVal) {
                    resultMap[planKey] += planVal;
                }
            });
        }
    });

    return resultMap;
}

// Создает store для грида "сотрудник-проекты"
function createStoreEmployee(data_list, yearStart, monthStart, yearEnd, monthEnd) {
    data_list = data_list || [];
    // Если не пришла информация по выбранному проекту, добавляем строку с этим проектом
    var projectId = dojo.byId("projectId").value;
    var projectName = dojo.byId("projectId").options[dojo.byId("projectId").selectedIndex].text;
    var needAddProject = true;
    dojo.forEach(data_list, function (project_info) {
        if (project_info["project_id"] == projectId) {
            needAddProject = false;
        }
    });
    if (needAddProject) {
        data_list.push({project_id: Number(projectId), project_name: projectName});
    }

    // Добавляем итоговою строку
    var totalRowMap = calcTotalRow(data_list, yearStart, monthStart, yearEnd, monthEnd);
    var totalRow = {"project_id": 0, "project_name": 'Итого'};
    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
        var planKey = year + '_' + month;
        var factKey = year + '-' + month;

        totalRow[planKey] = totalRowMap[planKey];
        totalRow[factKey] = totalRowMap[factKey];
    });
    data_list.push(totalRow);

    var data = {
        identifier: "project_id",
        items: []
    };

    dojo.forEach(data_list, function (row) {
        for (var field in row) {
            if (typeof row[field] == typeof undefined) { row[field] = ""; }
        }

        iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
            var planKey = year + '_' + month;
            var factKey = year + '-' + month;
            if (row[planKey] == undefined) { row[planKey] = 0; }
            if (row[factKey] == undefined) { row[factKey] = 0; }
        });

        data.items.push(row);
    });

    // Сохранение первоначальный данных
    dojo.forEach(data.items, function (row) {
        var projectId = row["project_id"];
        clearData[projectId] = {};
        for (var key in row) {
            clearData[projectId][key] = row[key];
        }
    });

    var store = new dojo.data.ItemFileWriteStore({data: data});
    return store;
}

// Создает грид "сотрудник-проекты"
function initEmployeeGrid(employeeId, yearStart, monthStart, yearEnd, monthEnd) {
    var grid = new dojox.grid.DataGrid({
        id: 'employeeGrid',
        store: employeesPlansStore[employeeId],
        structure: employeesPlansLayout(),
        rowSelector: '0px',
        width: '400px',
        autoHeight: true,
        onApplyCellEdit: function (inValue, inRowIndex, inFieldIndex) {
            var item = grid.getItem(inRowIndex);
            if (item) {
                var value = item["fields"];
                if (value && value != "") {
                    value += ";" + inFieldIndex;
                } else {
                    value = inFieldIndex;
                }
                grid.store.setValue(item, 'isChanged', 1);
                grid.store.setValue(item, 'fields', value);
            }
            grid.store.save();
            recalculateTotalRow();
        },
        canEdit: function (inCell, inRowIndex) {
            var item = grid.getItem(inRowIndex);
            return item["project_id"] > 0;
        }
    });
    grid.placeAt("employeeGridDiv");
    grid.startup();
};

// Динамически перестраивает грид "сотрудник-проекты"
function refreshEmployeeGrid(employeeId, yearStart, monthStart, yearEnd, monthEnd) {
    var grid = dijit.byId("employeeGrid");
    grid.store.close();
    grid.setStore( employeesPlansStore[employeeId] );
    grid.setStructure( employeesPlansLayout() );

    function actionSelection(items) {
        if (grid.selection.selectedIndex >= 0) {
            grid.selection.setSelected(grid.selection.selectedIndex, false);
        }
        // Выбра только один
        dojo.forEach(items, function (item) {
            var index = grid.getItemIndex(item);
            grid.selection.setSelected(index, true);
            if (gPlan != "") {
                iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function (month, year) {
                    grid.store.setValue(item, year + '_' + month, gPlan);
                });
                grid.store.save();
                recalculateTotalRow();
            }
        });
    }

    var projectId = dojo.byId("projectId").value;
    grid.store.fetch({query: {project_id: projectId}, onComplete: actionSelection, queryOptions: {deep: true}});
    grid.render();
}

// ToDo использовать метод из addEmployeesForm.js
// Обновляет список сотрудников на форме добавления сотрудников
function updateAdditionEmployeeList() {
    var divisionId = dojo.byId("divisionId").value;
    var managerId = dojo.byId("managerId").value;
    var projectRoleListId = getSelectValues(dojo.byId("projectRoleListId"));
    var regionListId = getSelectValues(dojo.byId("regionListId"));

    // Делает ajax запрос, возвращающий сотрудников по центру/руководителю/должности/региону,
    processing();
    dojo.xhrGet({
        url: "/employmentPlanning/getAddEmployeeListAsJSON",
        content: {
            divisionId: divisionId,
            managerId: managerId,
            projectRoleListId: projectRoleListId,
            regionListId: regionListId
        },
        handleAs: "text",
        load: function (response, ioArgs) {
            updateEmployeeList(response);
            stopProcessing();
        },
        error: function (response, ioArgs) {
            stopProcessing();
            alert('При запросе списка сотрудников произошла ошибка. ' + PROBLEM_SOLVING_MESSAGE);
        }
    });

    function updateEmployeeList(response) {
        var grid = dijit.byId("projectGrid");
        var employeeSelect = dojo.byId("additionEmployeeList");
        employeeSelect.options.length = 0;

        grid.store.fetch({query: {}, onComplete: checkExists, queryOptions: {deep: true}});

        // Те люди, которые уже есть в гриде - дизейблятся
        function checkExists(items) {
            var map = [];

            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                map[item["employee_id"]] = item;
            }

            var selectSize = 0;
            dojo.forEach(dojo.fromJson(response), function (row) {
                if (map[row["employee_id"]] === undefined) {
                    ++selectSize;
                    dojo.create("option", { value: row["employee_id"], innerHTML: row["employee_name"]}, employeeSelect);
                }
            });

            if (selectSize == 0) {
                dojo.create("option", { value: "-1", innerHTML: "Сотрудников не найдено", disabled: true}, employeeSelect);
                return;
            }
        }
    };
}

// Очищает select'ы на форме выбора сотрудников
function clearDialogSelection() {
    unselectValues(dojo.byId("projectRoleListId"));
    unselectValues(dojo.byId("regionListId"));
    dojo.byId("additionEmployeeList").options.length = 0;
}

// Добавляет строчку к гриду "проект-сотрудники"
function addRow() {
    var grid = dijit.byId("projectGrid");
    var employeeList = dojo.byId("additionEmployeeList").selectedOptions;

    dojo.forEach(employeeList, function (row) {
        var employeeId = parseFloat(row.value);

        var myNewItem = {
            employee_id: employeeId,
            employee_name: row.text,
            isChanged: 1,
            isNew: 1,
            plan: "0"
        };
        grid.store.newItem(myNewItem);
    });
    employeeDialog.hide();
    clearDialogSelection();
}

// Удаление строки грида "проект-сотрудники"
function removeRow(employeeId) {
    var grid = dijit.byId("projectGrid");
    grid.store.fetch({query: {employee_id: employeeId}, onComplete: delRow, queryOptions: {deep: true}});
    function delRow(items) {
        dojo.forEach(items, function (item) {
            grid.store.deleteItem(item);
        });
    }
    recalculateTotalRow();
}

// Отмена изменения плана на гриде "проект-сотрудники"
function cancelChange(employeeId) {
    var grid = dijit.byId("projectGrid");
    var projectId = Number(dojo.byId("projectId").value);

    grid.store.fetch({query: {employee_id: Number(employeeId)}, queryOptions: {deep: true}, onComplete: function (items) {
        dojo.forEach(items, function (item) {
            grid.store.setValue(item, 'plan', '');
            grid.store.setValue(item, 'isChanged', 0);
            grid.store.setValue(item, 'isNew', 0);
            cancelEmployeeChange(projectId, true);
        });
        grid.store.save();
    }});
    recalculateTotalRow();
}

// Откат для изменений для строки на гриде "сотрудник-проекты"
function cancelEmployeeChange(projectId, force) {
    var employeeGrid = dijit.byId("employeeGrid");
    var projectGrid = dijit.byId("projectGrid");

    employeeGrid.store.fetch({query: {project_id: projectId}, queryOptions: {deep: true}, onComplete: function (items) {
        dojo.forEach(items, function (item) {
            var project = clearData[projectId];

            iterateMonth(gYearBegin, gMonthBegin, gYearEnd, gMonthEnd, function (month, year) {
                var keyPlan = year + "_" + month;
                var keyFact = year + "-" + month;
                employeeGrid.store.setValue(item, keyPlan, project[keyPlan]);
                employeeGrid.store.setValue(item, keyFact, project[keyFact]);
            });
            employeeGrid.store.setValue(item, 'isChanged', 0);
            employeeGrid.store.setValue(item, 'fields', '');
        });
        employeeGrid.store.save();

        projectGrid.store.fetch({query: {employee_id: Number(gEmployeeId)}, queryOptions: {deep: true}, onComplete: function (employeeArray) {
            dojo.forEach(employeeArray, function (employee) {
                projectGrid.store.setValue(employee, 'plan', '');
            });
            recalculateTotalRow();
        }});
    }});
}

// Отправка данных на сервер для сохранения. Грид проект-сотрудники
function saveProjectPlan() {
    checkChanges(saving);
    function saving() {
        var grid = dijit.byId("projectGrid");

        grid.store.fetch({query: {isChanged: 1}, queryOptions: {deep: true}, onComplete: function (items) {
            saveEmployeeDataHandler(
                gProjectId,
                gMonthBegin,
                gYearBegin,
                gMonthEnd,
                gYearEnd,
                '{"employee": [' + itemToJSON(grid.store, items) + ']}',
                updateGrids);
        }});
    }
}

// Отправлка данных на сервер для сохранения. Грид сотрудник-проекты
function saveEmployeePlan(employeeId, projectId) {
    var grid = dijit.byId("employeeGrid");

    function actionAfterSaveEmployee(result) {
        cancelChange(Number(gEmployeeId));
        recalculateTotalRow();
    }

    function saveProjectData(items) {
        saveProjectDataHandler(
            '{"project": [' + itemToJSON(grid.store, items) + ']}',
            employeeId,
            actionAfterSaveEmployee);

        dojo.forEach(items, function (item) {
            var projectId = item["project_id"];
            for (var key in item) {
                clearData[projectId][key] = Number(item[key]);
            }
            grid.store.setValue(item, 'isChanged', 0);
            grid.store.setValue(item, 'fields', null);
        });

        grid.store.save();
    }

    if (isNumber(projectId)) {
        grid.store.fetch({query: {project_id: projectId, isChanged: 1}, queryOptions: {deep: true}, onComplete: saveProjectData});
    } else {
        grid.store.fetch({query: {isChanged: 1}, queryOptions: {deep: true}, onComplete: saveProjectData});
    }
}

// Проверки для грида "проект-сотрудники"
function checkChanges(handler) {
    var grid2 = dijit.byId("employeeGrid");
//    if (grid2.store == undefined) {
//        handler();
//        return;
//    }
    grid2.store.fetch({query: {isChanged: 1}, queryOptions: {deep: true}, onComplete: function (items) {
        if (items.length > 0) {
            alert("Перед сменой сотрудника необходимо сохранить или отменить изменения");
            return;
        } else {
            handler();
        }
    }});
}

function onProjectChanged(){
    checkChanges(save);

    function save() {
        gMonthBegin = Number(dojo.byId("monthBeg").value);
        gYearBegin = Number(dojo.byId("yearBeg").value);
        gMonthEnd = Number(dojo.byId("monthEnd").value);
        gYearEnd = Number(dojo.byId("yearEnd").value);
        gProjectId = Number(dojo.byId("projectId").value);

        var dateStart = new Date(gYearBegin, gMonthBegin);
        var dateEnd = new Date(gYearEnd, gMonthEnd);

        if (dateEnd < dateStart) {
            alert("Дата конца периода превышает дату начала");
            return;
        }

        if (gProjectId == -1) {
            alert("Необходимо выбрать проект");
            return;
        }

        dojo.byId("divEmployeeInfo").hidden = true;
        dojo.byId("spanEmployeeName").hidden = false;
        dojo.byId("spanEmployeeName").innerHTML = "Выберите пользователя для показа детализации планов";

        updateGrids();
    }
}

// Смена списка проектов в зависимости от выбранного Подразделения
function updateProjectList() {
    var selectDivision = dojo.byId("selectDivisionId").value;
    var monthBegin = Number(dojo.byId("monthBeg").value);
    var yearBegin = Number(dojo.byId("yearBeg").value);

    // Делает ajax запрос, возвращающий проекты по центру
    // и заполняет выпадашку
    dojo.xhrGet({
        url: "/employmentPlanning/getProjectByDivisionAsJSON",
        content: {
            divisionId: selectDivision,
            monthBegin: monthBegin,
            yearBegin: yearBegin
        },
        handleAs: "text",
        load: function (response, ioArgs) {
            dojo.byId("projectId").options.length = 0;
            dojo.create("option", { value: "-1", innerHTML: ""}, dojo.byId("projectId"));
            dojo.forEach(dojo.fromJson(response), function (row) {
                dojo.create("option", { value: row["project_id"], innerHTML: row["project_name"]}, dojo.byId("projectId"));
            });
        },
        error: function (response, ioArgs) {
            alert('Не получилось загрузить список проектов по центру. ' + PROBLEM_SOLVING_MESSAGE);
        }
    });
}

// Показать/скрыть фактические значения
function hideFact() {
    var grid2 = dijit.byId("employeeGrid");
    grid2.setStructure(employeesPlansLayout());
    grid2.render();
}
