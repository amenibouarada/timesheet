var projectRolesFieldNames = {
    id: {
        firstPart: "projectManagers",
        secondPart: ".employee"
    },
    name: {
        firstPart: "projectManagers[",
        secondPart: "].employee"
    }
};

var billableFieldNames = {
    id: {
        firstPart: "projectBillables",
        secondPart: ".employee"
    },
    name: {
        firstPart: "projectBillables[",
        secondPart: "].employee"
    }
};

dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ObjectStore");
dojo.require("dojo.store.Memory");

var employeesDataStore = new dojo.data.ObjectStore({
    objectStore: new dojo.store.Memory({
        data: employeesListJSON
    })
});

dojo.declare("DateTextBox", dijit.form.DateTextBox, {
    popupClass: "dijit.Calendar"
});

dojo.ready(function () {
    window.focus();
    updateManagerSelect(dojo.byId("managerDivisionId").value, managerId);

});

function saveProject() {
    var validationResult = validateForm();

    if (validationResult.length == 0) {
        projectform.action = "save";
        projectform.submit();
    } else {
        alert("Следующие поля пустые или заполнены с ошибкой:\n" + validationResult + ".\n\nСохранение отменено.");
    }
}

/**
 * Заполняет список доступных для выбора руководителей проектов.
 * @param divisionId Идентификатор выбранного центра
 */
function updateManagerSelect(divisionId, managerId) {
    if (divisionsEmployeesJSON.length > 0) {
        dojo.forEach(dojo.filter(divisionsEmployeesJSON, function (division) {
            return (division.divisionId == divisionId);
        }), function (divisionData) {
            var employeeArray = [];
            dojo.forEach(divisionData.employees, function (employeeData) {
                employeeArray.push(employeeData);
            });
            employeeArray.sort(function (a, b) {
                return (a.name < b.name) ? -1 : 1;
            });

            var divManagerDataStore = new dojo.data.ObjectStore({
                objectStore: new dojo.store.Memory({
                    data: employeeArray,
                    idProperty: 'employeeId'
                })
            });

            var divManagerFilteringSelect = dijit.byId("managerId");
            if (!divManagerFilteringSelect) {
                divManagerFilteringSelect = new dijit.form.FilteringSelect({
                    id: "managerId",
                    name: "manager",
                    store: divManagerDataStore,
                    searchAttr: 'name',
                    queryExpr: "*\${0}*",
                    ignoreCase: true,
                    autoComplete: false,
                    style: 'width:200px',
                    required: true
                }, "managerId").startup();
            } else {
                divManagerFilteringSelect.set('store', divManagerDataStore);
                dijit.byId("managerId").set('value', null);
            }
        });
    }

    dijit.byId("managerId").set('value', managerId);
}

function createTask() {
    var projectTasks = dojo.byId("projectTasks");
    var projectTasksRows = dojo.query(".task_row");
    var tasksCount = projectTasksRows.length;

    var newTask = projectTasks.insertRow(tasksCount + 1);
    dojo.addClass(newTask, "task_row");

    var newTaskIndex;
    if (tasksCount == 0) {
        newTaskIndex = 0;
    }
    else {
        var lastTask = projectTasksRows[tasksCount - 1];
        var lastTaskId = dojo.attr(lastTask, "id");
        var lastTaskIndex = parseInt(lastTaskId.substring(lastTaskId.lastIndexOf("_") + 1, lastTaskId.length));
        newTaskIndex = lastTaskIndex + 1;
    }
    dojo.attr(newTask, {id: "projectTask_" + newTaskIndex});

    /*------------------------*/
    /*    Кнопка удаления     */
    /*------------------------*/

    var deleteCell = newTask.insertCell(0);
    var img = dojo.doc.createElement("img");
    dojo.attr(img, {
        id: "taskDeleteButton_" + newTaskIndex,
        'class': "iconbutton",
        src: getContextPath() + "/resources/img/delete.png",
        alt: "Удалить",
        title: "Удалить"
    });
    img.onclick = function () {
        deleteTask(newTaskIndex);
    };
    deleteCell.appendChild(img);

    /*---------------------------*/
    /*    Наименование задачи    */
    /*---------------------------*/

    var nameCell = newTask.insertCell(1);
    dojo.addClass(nameCell, "multiline");
    var nameInput = dojo.doc.createElement("textarea");
    dojo.addClass(nameInput, "task_name");
    dojo.attr(nameInput, {
        id: "taskNameInput_" + newTaskIndex,
        name: "projectTasks[" + newTaskIndex + "].name",
        wrap: "soft",
        rows: "3"
    });
    nameCell.appendChild(nameInput);

    var _toDeleteInput = dojo.doc.createElement("input");
    dojo.addClass(_toDeleteInput, "to_delete");
    dojo.attr(_toDeleteInput, {
        id: "projectTasks" + newTaskIndex + ".toDelete",
        name: "projectTasks[" + newTaskIndex + "].toDelete",
        type: "hidden",
        value: ""
    });
    nameCell.appendChild(_toDeleteInput);

    /*----------------*/
    /*    Описание    */
    /*----------------*/

    var descriptionCell = newTask.insertCell(2);
    dojo.addClass(descriptionCell, "multiline");
    var descriptionInput = dojo.doc.createElement("textarea");
    dojo.addClass(descriptionInput, "task_description");
    dojo.attr(descriptionInput, {
        id: "taskDescriptionInput_" + newTaskIndex,
        name: "projectTasks[" + newTaskIndex + "].description",
        wrap: "soft",
        rows: "3"
    });
    descriptionCell.appendChild(descriptionInput);

    /*------------------------------------*/
    /*    Чекбокс "Признак активности"    */
    /*------------------------------------*/

    var activeCell = newTask.insertCell(3);
    var activeInput = dojo.doc.createElement("input");
    dojo.attr(activeInput, {
        id: "taskActiveInput_" + newTaskIndex,
        name: "projectTasks[" + newTaskIndex + "].active",
        type: "checkbox"
    });
    activeCell.appendChild(activeInput);

    var _activeInput = dojo.doc.createElement("input");
    dojo.attr(_activeInput, {
        name: "_projectTasks[" + newTaskIndex + "].active",
        type: "hidden",
        value: "on"
    });
    activeCell.appendChild(_activeInput);


    /*-----------------*/
    /*    Приоритет    */
    /*-----------------*/

    var priorityCell = newTask.insertCell(4);
    dojo.addClass(priorityCell, "multiline");
    var priorityInput = dojo.doc.createElement("textarea");
    dojo.attr(priorityInput, {
        id: "taskPriorityInput_" + newTaskIndex,
        name: "projectTasks[" + newTaskIndex + "].priority",
        wrap: "soft",
        rows: "3"
    });
    priorityCell.appendChild(priorityInput);
}

function createManager() {
    var projectManagers = dojo.byId("projectManagers");
    var projectManagersRows = dojo.query(".manager_row");
    var managersCount = projectManagersRows.length;

    var newManager = projectManagers.insertRow(managersCount + 1);
    dojo.addClass(newManager, "manager_row");

    var newManagerIndex;
    if (managersCount == 0) {
        newManagerIndex = 0;
    }
    else {
        var lastManager = projectManagersRows[managersCount - 1];
        var lastManagerId = dojo.attr(lastManager, "id");
        var lastManagerIndex = parseInt(lastManagerId.substring(lastManagerId.lastIndexOf("_") + 1, lastManagerId.length));
        newManagerIndex = lastManagerIndex + 1;
    }
    dojo.attr(newManager, {id: "projectManager_" + newManagerIndex});

    /*------------------------*/
    /*    Кнопка удаления     */
    /*------------------------*/

    var deleteCell = newManager.insertCell(0);
    var img = dojo.doc.createElement("img");
    dojo.attr(img, {
        id: "managerDeleteButton_" + newManagerIndex,
        'class': "iconbutton",
        src: getContextPath() + "/resources/img/delete.png",
        alt: "Удалить",
        title: "Удалить"
    });
    img.onclick = function () {
        deleteManager(newManagerIndex);
    };
    deleteCell.appendChild(img);

    /*------------------------*/
    /*    Выбор сотрудника    */
    /*------------------------*/

    var managerCell = newManager.insertCell(1);

    var managerDiv = dojo.doc.createElement("div");
    var managerIdText = projectRolesFieldNames.id.firstPart + newManagerIndex + projectRolesFieldNames.id.secondPart;
    var managerNameText = projectRolesFieldNames.name.firstPart + newManagerIndex + projectRolesFieldNames.name.secondPart;
    dojo.attr(managerDiv, {
        id: managerIdText,
        name: managerNameText
    });

    managerCell.appendChild(managerDiv);

    new dijit.form.FilteringSelect({
        id: managerIdText,
        name: managerNameText,
        store: employeesDataStore,
        searchAttr: 'name',
        queryExpr: "*\${0}*",
        ignoreCase: true,
        autoComplete: false,
        style: 'width:238px;',
        required: true
    }, managerIdText).startup();

    var _toDeleteInput = dojo.doc.createElement("input");
    dojo.addClass(_toDeleteInput, "to_delete");
    dojo.attr(_toDeleteInput, {
        id: "projectManagers" + newManagerIndex + ".toDelete",
        name: "projectManagers[" + newManagerIndex + "].toDelete",
        type: "hidden",
        value: ""
    });
    managerCell.appendChild(_toDeleteInput);

    /*------------------------*/
    /*       Выбор роли       */
    /*------------------------*/

    var roleCell = newManager.insertCell(2);
    var roleSelect = dojo.doc.createElement("select");
    dojo.attr(roleSelect, {
        id: "projectManagers" + newManagerIndex + ".projectRole",
        name: "projectManagers[" + newManagerIndex + "].projectRole"
    });
    for (var i = 0; i < projectRoleTypesJSON.length; i++) {
        var roleOption = dojo.doc.createElement("option");
        dojo.attr(roleOption, {
            value: projectRoleTypesJSON[i].id,
            title: projectRoleTypesJSON[i].value
        });

        roleOption.innerHTML = projectRoleTypesJSON[i].value;
        roleSelect.appendChild(roleOption);
    }
    roleCell.appendChild(roleSelect);

    /*------------------------*/
    /*   Чекбокс "Главный"    */
    /*------------------------*/

    var masterCell = newManager.insertCell(3);
    var masterInput = dojo.doc.createElement("input");
    dojo.attr(masterInput, {
        id: "projectManagers" + newManagerIndex + ".master1",
        name: "projectManagers[" + newManagerIndex + "].master",
        type: "checkbox"
    });
    masterCell.appendChild(masterInput);

    var _masterInput = dojo.doc.createElement("input");
    dojo.attr(_masterInput, {
        name: "_projectManagers[" + newManagerIndex + "].master",
        type: "hidden",
        value: "on"
    });
    masterCell.appendChild(_masterInput);

    /*----------------------------------*/
    /*   Чекбокс "Признак активности"   */
    /*----------------------------------*/

    var activeCell = newManager.insertCell(3);
    var activeInput = dojo.doc.createElement("input");
    dojo.attr(activeInput, {
        id: "projectManagers" + newManagerIndex + ".active1",
        name: "projectManagers[" + newManagerIndex + "].active",
        type: "checkbox"
    });
    activeCell.appendChild(activeInput);

    var _activeInput = dojo.doc.createElement("input");
    dojo.attr(_activeInput, {
        name: "_projectManagers[" + newManagerIndex + "].active",
        type: "hidden",
        value: "on"
    });
    activeCell.appendChild(_activeInput);
}

function createBillable() {
    var projectBillables = dojo.byId("projectBillables");
    var projectBillablesRows = dojo.query(".billable_row");
    var billablesCount = projectBillablesRows.length;

    var newBillable = projectBillables.insertRow(billablesCount + 1);
    dojo.addClass(newBillable, "billable_row");

    var newBillableIndex;
    if (billablesCount == 0) {
        newBillableIndex = 0;
    }
    else {
        var lastBillable = projectBillablesRows[billablesCount - 1];
        var lastBillableId = dojo.attr(lastBillable, "id");
        var lastBillableIndex = parseInt(lastBillableId.substring(lastBillableId.lastIndexOf("_") + 1, lastBillableId.length));
        newBillableIndex = lastBillableIndex + 1;
    }
    dojo.attr(newBillable, {id: "projectBillable_" + newBillableIndex});

    /*------------------------*/
    /*    Кнопка удаления     */
    /*------------------------*/

    var deleteCell = newBillable.insertCell(0);
    var img = dojo.doc.createElement("img");
    dojo.attr(img, {
        id: "billableDeleteButton_" + newBillableIndex,
        'class': "iconbutton",
        src: getContextPath() + "/resources/img/delete.png",
        alt: "Удалить",
        title: "Удалить"
    });
    img.onclick = function () {
        deleteBillable(newBillableIndex);
    };
    deleteCell.appendChild(img);

    /*------------------------*/
    /*    Выбор сотрудника    */
    /*------------------------*/

    var employeeCell = newBillable.insertCell(1);

    var billableDiv = dojo.doc.createElement("div");
    var idBillbleName = billableFieldNames.id.firstPart + newBillableIndex + billableFieldNames.id.secondPart;
    var nameBillableName = billableFieldNames.name.firstPart + newBillableIndex + billableFieldNames.name.secondPart;
    dojo.attr(billableDiv, {
        id: idBillbleName,
        name: nameBillableName
    });

    employeeCell.appendChild(billableDiv);

    new dijit.form.FilteringSelect({
        id: idBillbleName,
        name: nameBillableName,
        store: employeesDataStore,
        searchAttr: 'name',
        queryExpr: "*\${0}*",
        ignoreCase: true,
        autoComplete: false,
        style: 'width:238px;',
        required: true
    }, idBillbleName).startup();

    var _toDeleteInput = dojo.doc.createElement("input");
    dojo.addClass(_toDeleteInput, "to_delete");
    dojo.attr(_toDeleteInput, {
        id: "projectBillables" + newBillableIndex + ".toDelete",
        name: "projectBillables[" + newBillableIndex + "].toDelete",
        type: "hidden",
        value: ""
    });
    employeeCell.appendChild(_toDeleteInput);

    /*--------------------------------------*/
    /*    Чекбокс "Учитывать в затратах"    */
    /*--------------------------------------*/

    var billableCell = newBillable.insertCell(2);
    var billableInput = dojo.doc.createElement("input");
    dojo.attr(billableInput, {
        id: "projectBillables" + newBillableIndex + ".billable1",
        name: "projectBillables[" + newBillableIndex + "].billable",
        type: "checkbox"
    });
    billableCell.appendChild(billableInput);

    var _activeInput = dojo.doc.createElement("input");
    dojo.attr(_activeInput, {
        name: "_projectBillables[" + newBillableIndex + "].billable",
        type: "hidden",
        value: "on"
    });
    billableCell.appendChild(_activeInput);

    /*------------------------*/
    /*         Дата с         */
    /*------------------------*/

    var startDateCell = newBillable.insertCell(3);
    dojo.addClass(startDateCell, "billable_date");
    var startDateInput = dojo.doc.createElement("input");
    dojo.attr(startDateInput, {
        id: "projectBillables" + newBillableIndex + ".startDate",
        name: "projectBillables[" + newBillableIndex + "].startDate",
        type: "text",
        "data-dojo-type": "dijit.form.DateTextBox"
    });
    startDateCell.appendChild(startDateInput);

    /*-------------------------*/
    /*         Дата по         */
    /*-------------------------*/

    var endDateCell = newBillable.insertCell(4);
    dojo.addClass(endDateCell, "billable_date");
    var endDateInput = dojo.doc.createElement("input");
    dojo.attr(endDateInput, {
        id: "projectBillables" + newBillableIndex + ".endDate",
        name: "projectBillables[" + newBillableIndex + "].endDate",
        type: "text",
        "data-dojo-type": "dijit.form.DateTextBox"
    });
    endDateCell.appendChild(endDateInput);
    dojo.parser.parse();

    /*-------------------------*/
    /*        Основание        */
    /*-------------------------*/

    var commentCell = newBillable.insertCell(5);
    dojo.addClass(commentCell, "multiline");
    var commentInput = dojo.doc.createElement("textarea");
    dojo.attr(commentInput, {
        id: "projectBillables" + newBillableIndex + ".comment",
        name: "projectBillables[" + newBillableIndex + "].comment",
        wrap: "soft",
        rows: "3"
    });
    commentCell.appendChild(commentInput);
}

function deleteTask(row) {
    var task = dojo.byId("projectTasks" + row + ".toDelete");
    dojo.attr(task, {
        value: "delete"
    });
    var row = dojo.byId("projectTask_" + row);
    dojo.addClass(row, "hidden_delete");
}

function deleteManager(row) {
    var task = dojo.byId("projectManagers" + row + ".toDelete");
    dojo.attr(task, {
        value: "delete"
    });
    var row = dojo.byId("projectManager_" + row);
    dojo.addClass(row, "hidden_delete");
}

function deleteBillable(row) {
    var task = dojo.byId("projectBillables" + row + ".toDelete");
    dojo.attr(task, {
        value: "delete"
    });
    var row = dojo.byId("projectBillable_" + row);
    dojo.addClass(row, "hidden_delete");
}

function validateForm() {
    var valid = true;
    var errors = [];

    var name = dojo.byId("name");
    dojo.style(name, "background-color", "#ffffff");
    if (name.value.length == 0) {
        valid = false;
        //name.style({"background-color" : "#FFCFCF"});
        dojo.style(name, "background-color", "#f9f7ba");
        errors.push("наименование проекта");
    }

    var startDate = dojo.byId("startDate");
    dojo.style(startDate, "background-color", "#ffffff");
    if (startDate.getAttribute("aria-invalid") == "true") {
        valid = false;
        dojo.style(startDate, "background-color", "#f9f7ba");
        errors.push("дата начала проекта");
    }

    var endDate = dojo.byId("endDate");
    dojo.style(endDate, "background-color", "#ffffff");
    if (endDate.getAttribute("aria-invalid") == "true") {
        valid = false;
        dojo.style(endDate, "background-color", "#f9f7ba");
        errors.push("дата окончания проекта");
    }

    var taskRequired = dojo.byId("cqRequired1");
    if (taskRequired.checked) {
        var tasks = dojo.query(".task_row:not(.hidden_delete)");
        if (tasks.length == 0) {
            valid = false;
            errors.push("не указаны задачи по проекту");
        }
    }

    var taskNames = dojo.query(".task_row:not(.hidden_delete) .task_name");
    var namesValid = true;
    dojo.forEach(taskNames, function (name) {
        dojo.style(name, "background-color", "#ffffff");
        if (name.value.length == 0) {
            dojo.style(name, "background-color", "#f9f7ba");
            namesValid = false;
        }
    });
    if (!namesValid) {
        valid = false;
        errors.push("наименование задачи");
    }

    var taskDescriptions = dojo.query(".task_row:not(.hidden_delete) .task_description");
    var descriptionsValid = true;
    dojo.forEach(taskDescriptions, function (description) {
        dojo.style(description, "background-color", "#ffffff");
        if (description.value.length == 0) {
            dojo.style(description, "background-color", "#f9f7ba");
            descriptionsValid = false;
        }
    });
    if (!descriptionsValid) {
        valid = false;
        errors.push("описание задачи");
    }

    var dates = dojo.query(".billable_date:not(> tr.hidden_delete)").query(".dijitInputInner");
    var billablesValid = true;
    dojo.forEach(dates, function (date) {
        if (date.getAttribute("aria-invalid") == "true") {
            billablesValid = false;
        }
    });
    if (!billablesValid) {
        valid = false;
        errors.push("дата учёта в затратах");
    }

    for (var i = 0; i < dojo.query('table[id="projectManagers"]').query('tr').length - 1; i++) {
        var isHidden = dojo.getStyle('projectManager_' + i, 'display') == 'none';
        if (!dojo.query('TR#projectManager_' + i + ' > TD').query('input[name$="employee"]')[0].value && !isHidden) {
            valid = false;
            errors.push("пустое имя сотрудника в строке " + (i + 1) + " таблицы 'Проектные роли'");
        }
    }

    for (var i = 0; i < dojo.query('table[id="projectBillables"]').query('tr').length - 1; i++) {
        var isHidden = dojo.getStyle('projectBillable_' + i, 'display') == 'none';
        if (!dojo.query('TR#projectBillable_' + i + ' > TD').query('input[name$="employee"]')[0].value && !isHidden) {
            valid = false;
            errors.push("пустое имя сотрудника в строке " + (i + 1) + " таблицы 'Учитывать в затратах'");
        }
    }

    if (!valid) {
        return errors.join(",\n");
    } else {
        return "";
    }
}

function createFilteringSelect(row, idValue, fieldNames, rowName) {
    var div = dojo.doc.createElement("div");
    var idElement = fieldNames.id.firstPart + row + fieldNames.id.secondPart;
    var nameElement = fieldNames.name.firstPart + row + fieldNames.name.secondPart;
    dojo.attr(div, {
        id: idElement,
        name: nameElement
    });

    dojo.query('TR#' + rowName + row + ' > TD')[1].appendChild(div);

    new dijit.form.FilteringSelect({
        id: idElement,
        name: nameElement,
        store: employeesDataStore,
        searchAttr: 'name',
        queryExpr: "*\${0}*",
        ignoreCase: true,
        autoComplete: false,
        style: 'width:238px',
        required: true
    }, idElement).startup();
    dijit.byId(idElement).set('value', idValue)
}

