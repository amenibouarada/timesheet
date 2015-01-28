dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.Dialog");
dojo.require("dijit.form.Textarea");
dojo.require("dijit.form.Select");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dojox.html.entities");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dojo.parser");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ObjectStore");
dojo.require("dojo.on");
dojo.require("dojo.store.Memory");
dojo.require(CALENDAR_EXT_PATH);

dojo.declare("Calendar", com.aplana.dijit.ext.Calendar, {
    getEmployeeId: function () {
        return dojo.byId("employeeId").value;
    },

    getClassForDateInfo: function (dateInfo, date) {
        switch (dateInfo) {
            case "1":// в этот день имеется отчет
                return 'classDateGreen';
                break;
            case "2":   //выходной или праздничный день
                return 'classDateRedText';
                break;
            case "3":
                return 'classDateBrownBack';
                break;
            case "0":   //день без отчета
                if (date <= getFirstWorkDate()) // день раньше начала работы
                    return '';
                var lastWorkDate = getLastWorkDate();
                if (lastWorkDate != null && lastWorkDate != "" && date > lastWorkDate) // день после увольнения
                    return '';
                if (date <= new Date())
                    return 'classDateRedBack';
                else return '';
            default: // Никаких классов не назначаем, если нет информации
                return '';
                break;
        }
    }
});

dojo.declare("DateTextBox", dijit.form.DateTextBox, {
    popupClass: "Calendar"
});

var month = correctLength(new Date().getMonth() + 1);
var root = getRootEventListener();
var dojoxDecode = dojox.html.entities.decode;
var decodeMap = [
    ["\u0027", "#39"],
    ["\u0028", "#40"],
    ["\u0029", "#41"]
];
var currentDate;
var isFinalForm;

function initTimeSheetForm() {
    dojo.connect(dojo.byId("jira_get_plans_button"), "click", dojo.byId("jira_get_plans_button"), getJiraPlans);
    dojo.connect(dojo.byId("plan"), "onkeyup", dojo.byId("plan"), textareaAutoGrow);
    dojo.connect(dojo.byId("divisionId"), "onchange", dojo.byId("divisionId"), updateEmployeeSelect);
    dojo.connect(dojo.byId("employeeId"), "onchange", dojo.byId("employeeId"), checkIsVacationDay);

    /*смотрим, поддерживаются ли куки и рисуем индикатор*/
    showCookieIndicator();

    timeSheetForm.divisionId.value = divIdJsp;
    divisionChange(timeSheetForm.divisionId);
    updateEmployeeSelect();

    timeSheetForm.employeeId.value = employeeIdJsp;

    if (selectedCalDate != "") {
        var datePicker = dijit.byId("calDate");
        datePicker.set("displayedValue", selectedCalDate);
    } else {
        setDefaultDate();
    }

    currentDate = dijit.byId('calDate').get("value");

    initCurrentDateInfo(employeeIdJsp, currentDate, '/calendar/dates');

    if (!isErrorPage) {
        requestAndRefreshDailyTimesheetData(currentDate, dojo.byId('employeeId').value);
    } else {
        requestAndRefreshPreviousDayPlans(currentDate, dojo.byId('employeeId').value);
        reloadRowsState();
    }
}

// переопределение метода из timesheet.js, не удалять
function getEmployeeData() {
    return dijit.byId("employeeIdSelect").item;
}

function updateEmployeeSelect() {
    var divisionId = timeSheetForm.divisionId.target == null ? timeSheetForm.divisionId.value : timeSheetForm.divisionId.target.value;
    if (!divisionId || +divisionId == 0) {
        refreshEmployeeSelect([]);
        checkIsVacationDay();
    } else {
        dojo.xhrGet({
            url: getContextPath() + "/employee/employeeListWithLastWorkday/" + divisionId + "/true/true",
            handleAs: "json",
            timeout: 10000,
            sync: true,
            preventCache: false,
            headers: {  'Content-Type': 'application/json;Charset=UTF-8',
                "Accept": "application/json;Charset=UTF-8"},
            load: function (data) {
                refreshEmployeeSelect(data);
                checkIsVacationDay();
            },

            error: function (error) {
                handleError(error.message);
            }
        });
    }
}

function refreshEmployeeSelect(employeeList) {
    employeeList.unshift({
        id: 0,
        value: ""
    });

    employeeList.sort(function (a, b) {
        return (a.value < b.value) ? -1 : 1;
    });

    var employeeDataStore = new dojo.data.ObjectStore({
        objectStore: new dojo.store.Memory({
            data: employeeList,
            idProperty: 'id'
        })
    });

    var employeeFlteringSelect = dijit.byId("employeeIdSelect");

    if (!employeeFlteringSelect) {
        employeeFlteringSelect = new dijit.form.FilteringSelect({
            id: "employeeIdSelect",
            name: "employeeIdSelect",
            labelAttr: "value",
            store: employeeDataStore,
            searchAttr: 'value',
            queryExpr: "*\${0}*",
            ignoreCase: true,
            autoComplete: false,
            style: 'width:200px',
            required: true,
            onMouseOver: function () {
                tooltip.show(getTitle(this));
            },
            onMouseOut: function () {
                tooltip.hide();
            },
            onChange: function () {
                var value = this.item ? this.item.id : null;
                dojo.byId('employeeId').value = value;
                var obj = { value: value };
                onEmployeeChange(obj);
            }
        }, "employeeIdSelect");
        employeeFlteringSelect.startup();
    } else {
        employeeFlteringSelect.set('store', employeeDataStore);
        dijit.byId("employeeIdSelect").set('value', null);
        dojo.byId('employeeId').value = null;
    }


    dijit.byId("employeeIdSelect").set('value', employeeIdJsp);
}

/**
 * Устанавливает видимость и активность для элементов управления, отвечающих за редактирование отчета.
 * @param isFinal [true/false] Показатель, является ли отчёт уже отправленным.
 */
function setElementsAvailability(isFinal) {
    var controlsToHide = dojo.query(".controlToHide");
    var controlsToHideCount = controlsToHide.length;

    for (var i = 0; i < controlsToHideCount; i++) {
        controlsToHide[i].style.visibility = (isFinal) ? "collapse" : "visible";
    }

    var controlsToDisable = dojo.query(".controlToDisable");
    var controlsToDisableCount = controlsToDisable.length;

    for (var i = 0; i < controlsToDisableCount; i++) {
        controlsToDisable[i].disabled = isFinal;
    }
}

/**
 * Запрашивает данные timesheet и обновляет элементы страницы (план с прошлого дня, строки таблицы списания,
 * план на следующий день) в соответствии с полученными данными.
 * @param date Дата, для которой осуществляется запрос.
 * @param employeeId Идентификатор сотрудника.
 */
function requestAndRefreshDailyTimesheetData(date, employeeId) {
    var month = correctLength(date.getMonth() + 1);
    var year = date.getFullYear();
    var day = correctLength(date.getDate());
    var requestDate = year + "-" + month + "-" + day;

    dojo.xhrGet({
        url: getContextPath() + "/timesheet/dailyTimesheetData",
        handleAs: "json",
        timeout: 10000,
        content: {date: requestDate, employeeId: employeeId},
        load: function (data, ioArgs) {
            refreshDailyTimesheetData(data, ioArgs);
        },
        error: function (err, ioArgs) {
            if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                console.log(err);
            }
        }
    });
}

function refreshDailyTimesheetData(data, ioArgs) {
    if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
        var previous = data.previousDayData;
        var current = data.currentDayData;
        var next = data.nextDayData;

        // Чистка таблицы занятости.

        var timesheetRows = dojo.query(".time_sheet_row");
        var timesheetRowsCount = timesheetRows.length;
        for (var i = 0; i < timesheetRowsCount; i++) {
            timesheetRows[i].parentNode.removeChild(timesheetRows[i]);
        }

        // Заполнение поля планов, указанных в предыдущий рабочий день.

        var previousWorkDate = previous.workDate;
        var previousPlan = previous.plan;

        dojo.byId("lbPrevPlan").innerHTML = (previousWorkDate != null) ?
            "Планы предыдущего рабочего дня (" + timestampStrToDisplayStr(previousWorkDate.toString()) + "):" :
            "Планы предыдущего рабочего дня:";

        dojo.byId("plan_textarea").innerHTML = (previousPlan != null && previousPlan.length != 0) ?
            previousPlan.replace(/\n/g, '<br>') :
            "План предыдущего рабочего дня не был определен";

        // Заполнение строк таблицы занятости.

        var currentTableData = current.data;
        var isFinal = current.isFinal;

        if (currentTableData != null) {
            for (var j = 0; j < currentTableData.length; j++) {
                addNewRow();
                loadTableRow(j, currentTableData, isFinal);
            }
        } else if (existsCookie("aplanaRowsCount")) {
            var cookieRowsCount = getCookieValue("aplanaRowsCount");
            addNewRows((cookieRowsCount > 0) ? cookieRowsCount : 1);
        } else {
            addNewRow();
        }

        recalculateDuration();

        // Заполнение планов работы на будущее

        var currentPlan = current.plan;
        var nextWorkDate = current.nextWorkDate;
        var nextDayEffort = current.effort;
        var nextDaySummary = next.workSummary;

        dojo.byId("lbNextPlan").innerHTML = (nextWorkDate != null) ?
            "Планы на следующий рабочий день (" + timestampStrToDisplayStr(nextWorkDate.toString()) + "):" :
            "Планы на следующий рабочий день:";

        if (nextDayEffort != null) {
            dojo.byId("effortInNextDay").value = nextDayEffort;
        }

        dojo.byId('plan').value =
            (currentPlan != null && currentPlan.length != 0) ? currentPlan :
                (nextDaySummary != null && nextDaySummary.length != 0) ? nextDaySummary : "";

        dojo.attr("effortInNextDay", {
            disabled: isFinal
        });
        dojo.attr("plan", {
            readonly: isFinal
        });

        isFinalForm = isFinal;

        setElementsAvailability(isFinal);
    }
}

// TODO т.к. функция вызывается только один раз - надо перенести её в контроллер и передавать сразу в jsp
function requestAndRefreshPreviousDayPlans(date, employeeId) {
    var month = correctLength(date.getMonth() + 1);
    var year = date.getFullYear();
    var day = correctLength(date.getDate());
    var requestDate = year + "-" + month + "-" + day;

    dojo.xhrGet({
        url: getContextPath() + "/timesheet/dailyTimesheetData",
        handleAs: "json",
        timeout: 10000,
        content: {date: requestDate, employeeId: employeeId},
        load: function (data, ioArgs) {
            refreshPreviousDayPlans(data, ioArgs);
        },
        error: function (err, ioArgs) {
            if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                console.log(err);
            }
        }
    });
}

function refreshPreviousDayPlans(data, ioArgs) {
    if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
        var previous = data.previousDayData;
        var current = data.currentDayData;

        // Заполнение поля планов, указанных в предыдущий рабочий день.

        var previousWorkDate = previous.workDate;
        var previousPlan = previous.plan;

        dojo.byId("lbPrevPlan").innerHTML = (previousWorkDate != null) ?
            "Планы предыдущего рабочего дня (" + timestampStrToDisplayStr(previousWorkDate.toString()) + "):" :
            "Планы предыдущего рабочего дня:";

        dojo.byId("plan_textarea").innerHTML = (previousPlan != null && previousPlan.length != 0) ?
            previousPlan.replace(/\n/g, '<br>') :
            "План предыдущего рабочего дня не был определен";

        isFinalForm = false;
    }
}

function hideShowElement(id, isHide) {
    console.log(id + " " + isHide);
    dojo.setStyle(id, {"display": isHide ? "none" : ""});
}

function submitform(sendType) {
    if (typeof(root.onbeforeunload) != "undefined") {
        root.onbeforeunload = null;
    }

    if ((sendType == 'send' && confirmSendReport()) || sendType == 'send_draft') {
        var division = dojo.byId('divisionId');
        var employee = dojo.byId('employeeId');
        var rowsCount = dojo.query(".time_sheet_row").length;
        var projectId;
        var projectComponent;
        var workPlaceId;
        var workPlaceComponent;
        var diffProjects = false;
        var diffWorkPlaces = false;
        for (var i = 0; i < rowsCount; i++) {
            projectComponent = dojo.query("#project_id_" + i);
            if (!diffProjects && projectComponent.length > 0)
                if (projectComponent[0].value) {
                    if (projectId && (projectId != projectComponent[0].value)) {
                        if (projectComponent[0].value != 0)
                            diffProjects = true;
                    }
                    else
                        projectId = projectComponent[0].value;
                }

            workPlaceComponent = dojo.query("#workplace_id_" + i)
            if (!diffWorkPlaces && workPlaceComponent.length > 0)
                if (workPlaceComponent[0].value) {
                    if (workPlaceId && (workPlaceId != workPlaceComponent[0].value)) {
                        if (workPlaceComponent[0].value != 0)
                            diffWorkPlaces = true;
                    }
                    else
                        workPlaceId = workPlaceComponent[0].value;
                }
        }
        setCookie('aplanaDivision', division.value, TimeAfter(7, 0, 0));
        setCookie('aplanaEmployee', employee.value, TimeAfter(7, 0, 0));
        setCookie('aplanaRowsCount', rowsCount, TimeAfter(7, 0, 0));
        if (diffProjects)
            deleteCookie("aplanaProject");
        else
            setCookie('aplanaProject', projectId, TimeAfter(7, 0, 0));
        if (diffWorkPlaces)
            deleteCookie("aplanaWorkPlace");
        else
            setCookie('aplanaWorkPlace', workPlaceId, TimeAfter(7, 0, 0));
        if (sendType == 'send') {
            timeSheetForm.action = "timesheet";
        } else if (sendType == 'send_draft') {
            timeSheetForm.action = "sendDraft";
        }

        processing();
        // disabled не включается в submit. поэтому снимем аттрибут.
        dojo.removeAttr("divisionId", "disabled");
        dojo.removeAttr("employeeId", "disabled");
        timeSheetForm.submit();

    }
    else if (sendType == 'newReport' && confirmCreateNewReport()) {
        timeSheetForm.action = "newReport";
        timeSheetForm.submit();
    }
}

function requiredCommentSet() {
    var overtimeCause = dijit.byId("overtimeCause").get("value");
    var undertimeExp = (overtimeCause == undertimeOtherJsp);
    var workOnHolidayExp = (overtimeCause == workHolidayOtherJsp);
    var overtimeExp = (overtimeCause == overtimeOtherJsp);

    if (undertimeExp || overtimeExp || workOnHolidayExp) {
        dijit.byId("overtimeCauseComment").attr("required", true);
        dijit.byId("typeOfCompensation").attr("required", true);
    } else {
        dijit.byId("overtimeCauseComment").attr("required", false);
        dijit.byId("typeOfCompensation").attr("required", false);
    }
}

function loadTableRow(i, data, isFinal) {
    isFinal = !!isFinal;

    dojo.attr("activity_type_id_" + i, {
        value: data[i].activity_type_id
    });

    typeActivityChange(dojo.byId("activity_type_id_" + i));

    dojo.attr("workplace_id_" + i, {
        value: data[i].workplace_id
    });

    dojo.attr("project_id_" + i, {
        value: data[i].project_id
    });
    projectChange(dojo.byId("project_id_" + i));

    dojo.attr("project_role_id_" + i, {
        value: data[i].project_role_id
    });
    projectRoleChange(dojo.byId("project_role_id_" + i));

    dojo.attr("activity_category_id_" + i, {
        value: data[i].activity_category_id
    });
    setActDescription(i);

    dojo.attr("projectTask_id_" + i, {
        value: data[i].projectTask_id
    });
    setTaskDescription(i);

    dojo.attr("duration_id_" + i, {
        value: data[i].duration_id
    });
    checkDuration(dojo.byId("duration_id_" + i));
    recalculateDuration();

    dojo.attr("description_id_" + i, {
        value: dojoxDecode(dojoxDecode(data[i].description_id), decodeMap)
    });
    textareaAutoGrow(dojo.byId("description_id_" + i));

    dojo.attr("problem_id_" + i, {
        value: dojoxDecode(dojoxDecode(data[i].problem_id), decodeMap)
    });
    textareaAutoGrow(dojo.byId("problem_id_" + i));

    if (isFinal) {
        dojo.attr("activity_type_id_" + i, {disabled: "disabled"});
        dojo.attr("workplace_id_" + i, {disabled: "disabled"});
        dojo.attr("project_id_" + i, {disabled: "disabled"});
        dojo.attr("project_role_id_" + i, {disabled: "disabled"});
        dojo.attr("activity_category_id_" + i, {disabled: "disabled"});
        dojo.attr("projectTask_id_" + i, {disabled: "disabled"});
        dojo.attr("duration_id_" + i, {readonly: true});
        dojo.attr("description_id_" + i, {readonly: true});
        dojo.attr("problem_id_" + i, {readonly: true});
    }
}

/**
 Загрузка черновика
 **/
function loadDraft() {
    var date = dijit.byId('calDate').value;

    var currentDate = date.getFullYear() + "-" +
        correctLength(date.getMonth() + 1) + "-" +
        correctLength(date.getDate());
    var employeeId = dojo.byId('employeeId').value;
    var rowsCount;

    dojo.xhrGet({
        url: getContextPath() + "/timesheet/loadDraft",
        handleAs: "json",
        timeout: 10000,
        content: {date: currentDate, employeeId: employeeId},
        load: function (data, ioArgs) {
            if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
                var div = dojo.byId('time_sheet_table');
                var tr = document.querySelectorAll('.time_sheet_row');
                rowsCount = tr.length;
                for (var j = 0; j < rowsCount; j++) {
                    tr[j].parentNode.removeChild(tr[j]);
                }
                for (var i = 0; i < data.data.length; i++) {
                    addNewRow();
                    loadTableRow(i, data.data);
                }
                dojo.byId('plan').innerHTML = data.plan;
                hideShowElement("load_draft", true);
                hideShowElement("load_draft_text", true);
            }
        },
        error: function (err, ioArgs) {
            console.log("error");
        }
    });
}

function deleteRow(id_row) {
    var tsRow = document.getElementById("ts_row_" + id_row);
    if (tsRow !== null) {
        tsRow.parentNode.removeChild(tsRow);
    }
    recalculateRowNumbers();
    recalculateDuration();
}

String.prototype.trim = function () {
    return this.replace(/^\s+|\s+$/g, '');
};

function fillWorkplaceSelect(workplaceSelect) {
    insertEmptyOption(workplaceSelect);
    for (var i = 0; i < workplaceList.length; i++) {
        var workplaceOption = dojo.doc.createElement("option");
        dojo.attr(workplaceOption, {
            value: workplaceList[i].id
        });
        workplaceOption.title = workplaceList[i].value;

        workplaceOption.innerHTML = workplaceList[i].value;
        workplaceSelect.appendChild(workplaceOption);
    }
}

/* Добавляет новую строку в табличную часть отчёта. */
function addNewRow() {
    var tsTable = dojo.byId("time_sheet_table");
    var tsRows = dojo.query(".time_sheet_row");
    var tsRowCount = tsRows.length; // Количество активных строк таблицы.
    var tsTableTrCount = tsRows.length + 2; // +2 это tr с заголовком и tr с ИТОГО
    // Добавляем новую строку перед строкой ИТОГО
    var newTsRow = tsTable.insertRow(tsTableTrCount - 1);
    dojo.addClass(newTsRow, "time_sheet_row");
    // Индекс новой строки
    var newRowIndex;
    if (tsRowCount == 0) {
        newRowIndex = 0;
    }
    else {
        // Получаем идентификатор последней строки для получения ее индекса (не
        // путать с номером строки, они могут не совпадать).
        var lastRow = tsRows[tsRowCount - 1];
        var lastRowId = dojo.attr(lastRow, "id");
        var lastRowIndex = parseInt(lastRowId.substring(lastRowId.lastIndexOf("_") + 1, lastRowId.length));
        newRowIndex = lastRowIndex + 1;
    }
    dojo.attr(newTsRow,
        { id: "ts_row_" + newRowIndex });

    //ячейка с кнопкой(картинкой) удаления строки
    var deleteCell = newTsRow.insertCell(0);
    dojo.addClass(deleteCell, "text_center_align");
    var img = dojo.doc.createElement("img");
    dojo.addClass(img, "pointer");
    dojo.attr(img, {
        id: "delete_button_" + newRowIndex,
        src: "resources/img/delete.png",
        alt: "Удалить",
        title: "Удалить",
        //без px так как IE не понимает
        height: "15",
        width: "15"
    });
    dojo.attr(img, "class", "controlToHide");
    // неведома ошибка исправляется для IE добавлением onclick именно через функцию
    img.onclick = function () {
        deleteRow(newRowIndex);
    };
    deleteCell.appendChild(img);

    // Ячейка с номером строки
    var rowNumCell = newTsRow.insertCell(1);
    dojo.addClass(rowNumCell, "text_center_align row_number");
    rowNumCell.innerHTML = tsRowCount + 1;

    // Ячейка с типами активности
    var actTypeCell = newTsRow.insertCell(2);
    dojo.addClass(actTypeCell, "top_align");
    var actTypeSelect = dojo.doc.createElement("select");
    dojo.attr(actTypeSelect, {
        id: "activity_type_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].activityTypeId"
    });
    dojo.addClass(actTypeSelect, "activityType");
    insertEmptyOption(actTypeSelect);
    for (var i = 0; i < actTypeList.length; i++) {
        var actTypeOption = dojo.doc.createElement("option");
        dojo.attr(actTypeOption, {
            value: actTypeList[i].id
        });
        actTypeOption.title = actTypeList[i].value;

        actTypeOption.innerHTML = actTypeList[i].value;
        actTypeSelect.appendChild(actTypeOption);
    }
    actTypeCell.appendChild(actTypeSelect);

    // Ячейка с местом работы
    var workplaceCell = newTsRow.insertCell(3);
    dojo.addClass(workplaceCell, "top_align");
    var workplaceSelect = dojo.doc.createElement("select");
    dojo.attr(workplaceSelect, {
        id: "workplace_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].workplaceId"
    });
    dojo.addClass(workplaceSelect, "workplace");
    fillWorkplaceSelect(workplaceSelect);
    workplaceCell.appendChild(workplaceSelect);

    // Ячейка с названиями проектов/пресейлов
    var projectNameCell = newTsRow.insertCell(4);
    dojo.addClass(projectNameCell, "top_align");
    var projectSelect = dojo.doc.createElement("select");
    dojo.attr(projectSelect, {
        id: "project_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].projectId"
    });
    insertEmptyOption(projectSelect);
    projectNameCell.appendChild(projectSelect);

    // Ячейка с проектной ролью
    var projectRoleCell = newTsRow.insertCell(5);
    dojo.addClass(projectRoleCell, "top_align");
    var projectRoleSelect = dojo.doc.createElement("select");
    dojo.attr(projectRoleSelect, {
        id: "project_role_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].projectRoleId"
    });
    insertEmptyOption(projectRoleSelect);
    for (var i = 0; i < projectRoleList.length; i++) {
        var projectRoleOption = dojo.doc.createElement("option");
        dojo.attr(projectRoleOption, {
            value: projectRoleList[i].id
        });
        projectRoleOption.title = projectRoleList[i].value;

        projectRoleOption.innerHTML = projectRoleList[i].value;
        projectRoleSelect.appendChild(projectRoleOption);
    }
    sortSelectOptions(projectRoleSelect);
    projectRoleCell.appendChild(projectRoleSelect);

    // Ячейка с категорией активности
    var actCatCell = newTsRow.insertCell(6);
    dojo.addClass(actCatCell, "top_align");
    var actCatSelect = dojo.doc.createElement("select");
    dojo.attr(actCatSelect, {
        id: "activity_category_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].activityCategoryId",
        onchange: "setActDescription(" + newRowIndex + ")"
    });
    insertEmptyOption(actCatSelect);
    actCatCell.appendChild(actCatSelect);
    var labelDescription = dojo.doc.createElement("label");
    dojo.attr(labelDescription, {
        id: "act_description_" + newRowIndex,
        style: "font-style: italic"
    });
    actCatCell.appendChild(labelDescription);

    // Ячейка с проектными задачами
    var projectTasksCell = newTsRow.insertCell(7);
    dojo.addClass(projectTasksCell, "top_align");
    var projectTasksSelect = dojo.doc.createElement("select");
    dojo.attr(projectTasksSelect, {
        id: "projectTask_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].projectTaskId",
        onchange: "setTaskDescription(" + newRowIndex + ")"
    });
    insertEmptyOption(projectTasksSelect);
    var taskLabelDescription = dojo.doc.createElement("label");
    dojo.attr(taskLabelDescription, {
        id: "task_description_" + newRowIndex,
        style: "font-style: italic"
    });

    projectTasksCell.appendChild(projectTasksSelect);
    projectTasksCell.appendChild(taskLabelDescription);

    // Ячейка с часами
    var durationCell = newTsRow.insertCell(8);
    dojo.addClass(durationCell, "top_align");
    var durationInput = dojo.doc.createElement("input");
    dojo.attr(durationInput, {
        id: "duration_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].duration",
        //type:"number",
        type: "text"
    });
    dojo.addClass(durationInput, "text_right_align duration");
    durationCell.appendChild(durationInput);

    // Ячейка с комментариями
    var descriptionCell = newTsRow.insertCell(9);
    dojo.addClass(descriptionCell, "top_align");
    var descriptionTextarea = dojo.doc.createElement("textarea");
    dojo.attr(descriptionTextarea, {
        id: "description_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].description",
        wrap: "soft",
        rows: "3",
        style: "width: 100%"
    });
    descriptionCell.appendChild(descriptionTextarea);

    //ячейка с кнопкой(картинкой) запроса из JIRA
    var jiraCell = newTsRow.insertCell(10);
    dojo.addClass(jiraCell, "text_center_align");
    var jiraImg = dojo.doc.createElement("img");
    dojo.addClass(jiraImg, "pointer");
    dojo.attr(jiraImg, {
        id: "jira_button_" + newRowIndex,
        // class:"controlToHide",
        src: "resources/img/logo-jira.png",
        alt: "Запрос из JIRA",
        title: "Запрос из JIRA",
        //без px так как IE не понимает
        height: "15",
        width: "15",
        style: "cursor:pointer;"
    });
    dojo.attr(jiraImg, "class", "controlToHide");

    //неведома ошибка исправляется для IE добавлением onclick именно через функцию
    jiraImg.onclick = function () {
        getJiraInfo(newRowIndex);
    };
    jiraCell.appendChild(jiraImg);

    // Ячейка с проблемами
    var problemCell = newTsRow.insertCell(11);
    dojo.addClass(problemCell, "top_align");
    var problemTextarea = dojo.doc.createElement("textarea");
    dojo.attr(problemTextarea, {
        id: "problem_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].problem",
        wrap: "soft",
        rows: "3",
        style: "width: 100%"
    });
    problemCell.appendChild(problemTextarea);

    // Помещаем новую строку в конец таблицы
    recalculateRowNumbers();
    resetRowState(newRowIndex, true);

    /*подключаем функции показа тултипов и регистрации изменений для селктов */
    setDefaultSelectEvents(workplaceSelect);
    //для типа активности
    setDefaultSelectEvents(actTypeSelect);
    //для проекта
    setDefaultSelectEvents(projectSelect);
    //для проектной роли
    setDefaultSelectEvents(projectRoleSelect);
    //для категории активности
    setDefaultSelectEvents(actCatSelect);
    //для задачи
    setDefaultSelectEvents(projectTasksSelect);

    /*для инпутов подключаем только регистрацию изменений по нажатию*/
    dojo.connect(durationInput, "onkeyup", durationInput, somethingChanged);
    dojo.connect(descriptionTextarea, "onkeyup", descriptionTextarea, somethingChanged);
    dojo.connect(problemTextarea, "onkeyup", problemTextarea, somethingChanged);

    dojo.connect(actTypeSelect, "onchange", actTypeSelect, typeActivityChange);
    dojo.connect(projectSelect, "onchange", projectSelect, projectChange);
    dojo.connect(projectRoleSelect, "onchange", projectRoleSelect, projectRoleChange);
    dojo.connect(durationInput, "onchange", durationInput, checkDuration);
    dojo.connect(descriptionTextarea, "onkeyup", descriptionTextarea, textareaAutoGrow);
    dojo.connect(problemTextarea, "onkeyup", problemTextarea, textareaAutoGrow);

    dojo.connect(actTypeSelect, "onchange", actTypeSelect, deleteEmptyOption);
    dojo.connect(workplaceSelect, "onchange", workplaceSelect, deleteEmptyOption);
    dojo.connect(projectSelect, "onchange", projectSelect, deleteEmptyOption);
    dojo.connect(projectRoleSelect, "onchange", projectRoleSelect, deleteEmptyOption);
    dojo.connect(projectSelect, "onchange", projectSelect, deleteEmptyOption);
    dojo.connect(actCatSelect, "onchange", actCatSelect, deleteEmptyOption);
    dojo.connect(projectTasksSelect, "onchange", projectTasksSelect, deleteEmptyOption);
}

/* удаляет пустой option */
function deleteEmptyOption() {
    var selected = this.options[0];
    if (selected.value == 0) {
        this.remove(0);
    }
}

function setActDescription(rowIndex) {
    var label = dojo.byId("act_description_" + rowIndex);
    if (label == null) {
        return;
    }
    var actCat = (dojo.byId("activity_category_id_" + rowIndex)).value;
    var actType = (dojo.byId("activity_type_id_" + rowIndex)).value;
    var projectRole = (dojo.byId("project_role_id_" + rowIndex)).value;
    var finded = false;
    for (var i = 0; i < listOfActDescription.length; i++) {
        if (listOfActDescription[i].actCat == actCat &&
            listOfActDescription[i].actType == actType &&
            listOfActDescription[i].projectRole == projectRole) {
            label.innerHTML = listOfActDescription[i].description;
            finded = true;
            break;
        }
    }
    if (!finded) {
        label.innerHTML = "";
    }
}

function setTaskDescription(rowIndex) {
    var label = dojo.byId("task_description_" + rowIndex);
    if (label == null) {
        return;
    }
    var task = (dojo.byId("projectTask_id_" + rowIndex)).value;
    var finded = false;
    for (var i = 0; i < projectTaskList.length; i++) {
        for (var j = 0; j < projectTaskList[i].projTasks.length; j++) {
            if (projectTaskList[i].projTasks[j].id == task) {
                label.innerHTML = projectTaskList[i].projTasks[j].desc;
                finded = true;
                break;
            }
        }
    }
    if (!finded) {
        label.innerHTML = "";
    }
}

function setDefaultSelectEvents(obj) {
    dojo.connect(obj, "onmouseover", obj, showTooltip);
    dojo.connect(obj, "onmouseout", obj, hideTooltip);
    dojo.connect(obj, "onmouseup", obj, somethingChanged);
    dojo.connect(obj, "onkeyup", obj, somethingChanged);
}


/* Проверяет, включены ли куки в браузере
 * если нет индикатор серый, если да-синий*/
function showCookieIndicator() {
    if (navigator.cookieEnabled) {
        document.getElementById('indicator').style.display = 'none';
    }
    else {
        document.getElementById('indicator').style.display = 'block';
    }
}

/* Возвращает массив текущих Id строк в таблице */
function getRowsId(obj) {
    var listId = [];
    for (var i = 0; i < obj.length; i++) {
        var id = dojo.attr(obj[i], "id");
        var id_num = parseInt(id.substring(id.lastIndexOf("_") + 1, id.length));
        listId[i] = id_num;
    }
    return listId;
}

/*Проверяем, есть ли в табличной части отчета непустые строки
 * когда кликаем на чекбокс отпуска/болезни
 * если находим, передаем тру, и потом эти строки очищаем.*/
function tablePartNotEmpty() {
    var notEmpty = false;
    var tsRows = getRowsId(dojo.query(".time_sheet_row"));
    for (var i = 0; i < tsRows.length; i++) {
        var actTypeSelect = dojo.byId('activity_type_id_' + tsRows[i]);
        if (actTypeSelect.value != 0) {
            notEmpty = true;
            break;
        }
    }
    return notEmpty;
}

function planBoxNotEmpty() {
    var planBox = dojo.query("#plan");
    if (planBox && (planBox.length > 0)) planBox = planBox[0];
    if (!planBox) return false
    else return !!planBox.value;
}

/* Устанавливает компоненту calDate дату по умолчанию. */
function setDefaultDate() {
    var date_picker = dijit.byId("calDate");
    var employee = getEmployeeData();
    if (!employee) {
        date_picker.set('disabled', true);
        return;
    } else {
        date_picker.set('disabled', false);
        date_picker.set("displayedValue", employee.dateByDefault);
    }
}

// возващает последний рабочий день сотрудника
function getLastWorkDate() {
    var employeeId = dojo.byId("employeeId").value;
    var employee = getEmployeeData();
    return convertStringToDate(employee.lastWorkDate);
}

function validateReportDate(value) {

    if (checkIsVacationDay()) {
        return;
    }

    if (value != null && dateNotBetweenMonth(value)) {
        dojo.style("date_warning", {"display": "inline", "color": "red"});
        if (invalidReportDate(value) > 0) {
            dojo.byId("date_warning").innerHTML = "Разница текущей и указанной дат больше 27 дней";
            return;
        } else {
            dojo.byId("date_warning").innerHTML = "Разница текущей и указанной дат больше 27 дней";
            return;
        }
    }
    else {
        dojo.style("date_warning", {"display": "none"});
    }
}

function checkIsVacationDay() {
    var employeeId = dojo.byId("employeeId").value;
    if (!employeeId || employeeId == undefined || !dijit.byId('calDate').get('value')) {
        dojo.style("date_warning", {"display": "none"});
        return false;
    }
    var reportDate = dijit.byId('calDate').get('value').format("yyyy-mm-dd");
    var resp = false;
    dojo.xhrGet({
        url: getContextPath() + "/vacations/checkDate",
        handleAs: "text",
        timeout: 10000,
        sync: true,
        content: { employeeId: employeeId, date: reportDate},
        preventCache: true,
        load: function (data) {
            if (dojo.fromJson(data)[0].isVacationDay == "true") {
                dojo.style("date_warning", {"display": "inline", "color": "red"});
                dojo.byId("date_warning").innerHTML = "Внимание! На выбранную дату у сотрудника запланирован отпуск";
                resp = true;
            } else {
                dojo.style("date_warning", {"display": "none"});
                resp = false;
            }

        },
        error: function (err) {
            resp = false;
        }
    });
    return resp;
}

function onCalDateChange(calDateObj) {
    calDateObj.constraints.min = getFirstWorkDate();
    var lastWorkDate = getLastWorkDate();
    if (lastWorkDate != null && lastWorkDate != "") {
        calDateObj.constraints.max = lastWorkDate;
    } else {
        calDateObj.constraints.max = new Date(2100, 1, 1);
    }
    validateReportDate(calDateObj.value);

    var sameDateOnChangeEventFired = (currentDate.getTime() == dijit.byId('calDate').get("value").getTime());

    if (!sameDateOnChangeEventFired) {
        if (!isFinalForm && (tablePartNotEmpty() || planBoxNotEmpty())) {
            var dialog = dijit.byId("dialogChangeDate");
            dojo.style(dialog.closeButtonNode, "display", "none");
            dialog.show();
        } else {
            requestAndRefreshDailyTimesheetData(calDateObj.value, dojo.byId('employeeId').value);

            if (isErrorPage) {
                dojo.style("errors_box", {"display": "none"});
                isErrorPage = false;
            }
            currentDate = dijit.byId('calDate').get("value");
        }
    }
}

function confirmCalDateChange() {
    var dialog = dijit.byId("dialogChangeDate");
    dialog.hide();

    requestAndRefreshDailyTimesheetData(dijit.byId('calDate').value, dojo.byId('employeeId').value);

    if (isErrorPage) {
        dojo.style("errors_box", {"display": "none"});
        isErrorPage = false;
    }
    currentDate = dijit.byId('calDate').get("value");
}

function cancelCalDateChange() {
    var dialog = dijit.byId("dialogChangeDate");
    dialog.hide();

    dijit.byId('calDate').set("value", currentDate);
}

function onEmployeeChange(employeeObj) {
    setDefaultEmployeeJob(-1);
    setDefaultDate()
}

/*
 * Растягивает по высоте текстовую область, если введённый
 * в неё текст не умещается.
 */
function textareaAutoGrow(obj) {
    var textarea = null;
    if (obj.target == null) {
        textarea = obj;
    }
    else {
        textarea = obj.target;
    }
    var rowsMin = 4;
    var rowsMax = 16;
    var str = textarea.value;
    var cols = textarea.cols;
    var linecount = 0;
    var arStr = str.split("\n");
    for (var i = arStr.length - 1; i >= 0; --i) {
        linecount = linecount + 1 + Math.floor(arStr[i].length / cols);
    }
    linecount++;
    linecount = Math.max(rowsMin, linecount);
    linecount = Math.min(rowsMax, linecount);
    textarea.rows = linecount;
}

/**
 * Срабатывает при смене значения в списке подразделений.
 * Управляет содержимым списка сотрудников в зависимости от выбранного
 * значения в списке подразделений.
 */
function divisionChange(obj) {
    var rows = dojo.query(".row_number");
    var activityType;
    for (var i = 0; i < rows.length; i++) {
        activityType = dojo.byId("activity_type_id_" + i).value;
        if ((activityType == EnumConstants.TypesOfActivityEnum.PROJECT) || (activityType == EnumConstants.TypesOfActivityEnum.PROJECT_PRESALE)) {
            fillProjectList(i, EnumConstants.TypesOfActivityEnum.PROJECT);
        } else {
            fillProjectList(i, activityType);
        }
    }
}

/*
 Влючено ли поле выбора задачи для для текущей строчки
 */
function isEnableTaskSelect(rowIndex) {
    var typeActivitySelect = dojo.byId("activity_type_id_" + rowIndex);
    return typeActivitySelect.value != EnumConstants.TypesOfActivityEnum.NON_PROJECT;
}

/*
 * Срабатывает при смене значения в списке "Тип активности".
 * Управляет доступностью компонентов соответсвующей строки
 * табличной части отчёта в соответствии с определённой логикой.
 */
function typeActivityChange(obj) {
    var select = null;
    if (obj.target == null) {
        select = obj;
    }
    else {
        select = obj.target;
    }
    var selectId = dojo.attr(select, "id");
    var rowIndex = selectId.substring(selectId.lastIndexOf("_") + 1, selectId.length);
    var workPlaceIdEl = "workplace_id_" + rowIndex;
    var projectIdEl = "project_id_" + rowIndex;
    var projectRoleIdEl = "project_role_id_" + rowIndex;
    var durationIdEl = "duration_id_" + rowIndex;
    var descIdEl = "description_id_" + rowIndex;

    // Проект или Пресейл

    if ((select.value == EnumConstants.TypesOfActivityEnum.PROJECT) ||
        (select.value == EnumConstants.TypesOfActivityEnum.PRESALE) ||
        (select.value == EnumConstants.TypesOfActivityEnum.PROJECT_PRESALE)) {
        dojo.removeAttr(workPlaceIdEl, "disabled");
        dojo.removeAttr(projectIdEl, "disabled");
        dojo.removeAttr(projectRoleIdEl, "disabled");
        if (select.value == EnumConstants.TypesOfActivityEnum.PRESALE) {
            fillProjectList(rowIndex, select.value);
        } else {
            fillProjectList(rowIndex, EnumConstants.TypesOfActivityEnum.PROJECT);
        }
    }

    // Внепроектная активность
    else if (select.value == EnumConstants.TypesOfActivityEnum.NON_PROJECT) {
        dojo.removeAttr(workPlaceIdEl, "disabled");
        dojo.attr(projectIdEl, {
            disabled: "disabled",
            value: "0"
        });
    } else if (select.value == "0") {
        resetRowState(rowIndex, true);
    } else if (select.value == EnumConstants.TypesOfActivityEnum.ILLNESS) { //Болезнь
        var duration = parseFloat(dojo.attr(durationIdEl, "value"));
        resetRowState(rowIndex, false);
        dojo.removeAttr(durationIdEl, "disabled");
        if (!isNaN(duration)) {
            dojo.attr(durationIdEl, { value: duration });
        }
    } else if (select.value == EnumConstants.TypesOfActivityEnum.COMPENSATORY_HOLIDAY) { //Отгулы
        var duration = parseFloat(dojo.attr(durationIdEl, "value"));
        var description = dojo.byId(descIdEl).value;
        resetRowState(rowIndex, false);
        dojo.removeAttr(durationIdEl, "disabled");
        dojo.removeAttr(descIdEl, "disabled");
        dojo.byId(descIdEl).value = description;
        if (!isNaN(duration)) {
            dojo.attr(durationIdEl, { value: duration });
        }
    } else if (select.value == EnumConstants.TypesOfActivityEnum.VACATION) { //Отпуск
        resetRowState(rowIndex, false);
    }

    if (select.value && select.value != "0") {
        var workplaceSelect = dojo.byId(workPlaceIdEl);
        if (!workplaceSelect.value || workplaceSelect.value == "" || workplaceSelect.value == "0") {
            if (existsCookie('aplanaWorkPlace')) {
                workplaceSelect.value = getCookieValue('aplanaWorkPlace');
            }
        }
    }

    if (!isEnableTaskSelect(rowIndex)) {
        dojo.attr("projectTask_id_" + rowIndex, {
            disabled: "disabled",
            value: "0"
        });
    }

    if ((select.value == EnumConstants.TypesOfActivityEnum.PROJECT)
        || (select.value == EnumConstants.TypesOfActivityEnum.PRESALE)
        || (select.value == EnumConstants.TypesOfActivityEnum.NON_PROJECT)
        || (select.value == EnumConstants.TypesOfActivityEnum.PROJECT_PRESALE)) {
        dojo.removeAttr(workPlaceIdEl, "disabled");
        dojo.removeAttr("activity_category_id_" + rowIndex, "disabled");
        dojo.removeAttr(descIdEl, "disabled");
        dojo.removeAttr("problem_id_" + rowIndex, "disabled");
        dojo.removeAttr(durationIdEl, "disabled");
        dojo.removeAttr(projectRoleIdEl, "disabled");
        setDefaultEmployeeJob(rowIndex);
        fillAvailableActivityCategoryList(rowIndex);
    }
    setActDescription(rowIndex);
    setTaskDescription(rowIndex);
}


function getJiraPlans() {
    var employeeId = dojo.byId("employeeId").value;

    if (employeeId) {
        var jiraCell = dojo.byId("plan").parentNode;
        var standbyElementJira = new dojox.widget.Standby({target: jiraCell, zIndex: 1000});
        jiraCell.appendChild(standbyElementJira.domNode);
        standbyElementJira.startup();
        standbyElementJira.show();
        dojo.xhrGet({
            url: getContextPath() + "/timesheet/jiraIssuesPlanned",
            handleAs: "text",
            timeout: 30000,
            content: {employeeId: employeeId},
            preventCache: true,
            load: function (data) {
                if (data.length != 0)
                    dojo.byId("plan").value = data;
                else
                    dojo.byId("plan").value = "Активности по задачам не найдено";
                standbyElementJira.hide();
            },
            error: function (err) {
                dojo.byId("plan").value = "Ошибка при поиске активности в JIRA(" + err + ")";
                standbyElementJira.hide();
            }
        });
    }
}

function getJiraInfo(rowIndex) {
    var employeeId = dojo.byId("employeeId").value;
    var projectId = dojo.byId("project_id_" + rowIndex).value;
    var reportDate = dijit.byId('calDate').get('value').format("yyyy-mm-dd");
    if (employeeId != 0 && projectId != 0 && reportDate != 0) {
        var jiraCell = dojo.byId("jira_button_" + rowIndex).parentNode;
        var standbyElementJira = new dojox.widget.Standby({target: jiraCell, zIndex: 1000});
        jiraCell.appendChild(standbyElementJira.domNode);
        standbyElementJira.startup();
        standbyElementJira.show();
        dojo.xhrGet({
            url: getContextPath() + "/timesheet/jiraIssues",
            handleAs: "text",
            timeout: 10000,
            content: {employeeId: employeeId, date: reportDate, projectId: projectId},
            preventCache: true,
            load: function (data) {
                if (data.length != 0)
                    dojo.byId("description_id_" + rowIndex).value = data;
                else
                    dojo.byId("description_id_" + rowIndex).value = "Активности по задачам не найдено";
                textareaAutoGrow(dojo.byId("description_id_" + rowIndex));
                standbyElementJira.hide();
            },
            error: function (err) {
                dojo.byId("description_id_" + rowIndex).value = "Ошибка при поиске активности в JIRA(" + err + ")";
                standbyElementJira.hide();
            }
        });
    }
}

function somethingChanged() {
    if (typeof(root.onbeforeunload) != "undefined") root.onbeforeunload = confirmTimeSheetCloseWindow;
}

function confirmTimeSheetCloseWindow() {
    if (tablePartNotEmpty() || planBoxNotEmpty())
        return "Отчет не был отправлен.";
}

/*
 * Превращает timestamp строку (yyyy-mm-dd) в строку для
 * displayValue DateTextBoxА (dd.mm.yyyy)
 */
function timestampStrToDisplayStr(str) {
    if (str != "") {
        var splittedStr = str.split("-");
        return splittedStr[2] + "." + splittedStr[1] + "." + splittedStr[0];
    } else {
        return str;
    }
}

function submitWithOvertimeCauseSet() {
    var comment = dijit.byId("overtimeCauseComment").get("value");
    var required = dijit.byId("overtimeCauseComment").get("required");

    if (comment == "" && required == true) {
        tooltip.show("Комментарий для причины 'Другое' является обязательным!");
        return;
    }

    var overtimeCause = dijit.byId("overtimeCause").get("value");
    var overtimeRequired = dijit.byId("overtimeCause").get("required");

    if (overtimeCause == 0 && overtimeRequired == true) {
        tooltip.show("Необходимо указать причину!");
        return;
    }

    dojo.byId("overtimeCauseComment_hidden").value = dijit.byId("overtimeCauseComment").get('value');
    dojo.byId("overtimeCause_hidden").value = overtimeCause;
    dojo.byId("typeOfCompensation_hidden").value = dijit.byId("typeOfCompensation").get('value');


    dijit.byId('dialogOne').hide();
    submitform('send');
}

/*
 * Срабатывает при смене значения в списке "Проектная роль"
 * Влияет на доступные категории активности.
 */
function projectRoleChange(obj) {
    var select = null;
    if (obj.target == null) {
        select = obj;
    }
    else {
        select = obj.target;
    }
    var selectId = dojo.attr(select, "id");
    var rowIndex = selectId.substring(selectId.lastIndexOf("_") + 1, selectId.length);
    fillAvailableActivityCategoryList(rowIndex);
    setActDescription(rowIndex);
}


/*
 * Восстанавливает содержимое компонентов каждой строки табличной части отчёта
 * после возврата страницы валидатором.
 */
function reloadRowsState() {
    var rowsCount = dojo.query(".time_sheet_row").length;
    var rows = dojo.query(".time_sheet_row");
    for (var i = 0; i < rowsCount; i++) {

        var projectSelect = dojo.byId("project_id_" + i);
        if (dojo.attr(projectSelect, "disabled") != "disabled") {
            for (var k = 0; k < selectedProjects.length; k++) {
                if (selectedProjects[k].row == i) {
                    dojo.attr(projectSelect, { value: selectedProjects[k].project });
                }
            }
            projectChange(projectSelect);
        }

        var projectRoleSelect = dojo.byId("project_role_id_" + i);
        if (dojo.attr(projectRoleSelect, "disabled") != "disabled") {
            for (var p = 0; p < selectedProjectRoles.length; p++) {
                if (selectedProjectRoles[p].row == i) {
                    dojo.attr(projectRoleSelect, { value: selectedProjectRoles[p].role });
                }
            }
        }

        var actCatSelect = dojo.byId("activity_category_id_" + i);
        if ((dojo.attr(actCatSelect, "disabled") != "disabled")) {
            for (var q = 0; q < selectedActCategories.length; q++) {
                if (selectedActCategories[q].row == i) {
                    fillAvailableActivityCategoryList(i);
                    dojo.attr(actCatSelect, { value: selectedActCategories[q].actCat });
                }
            }
        }
        setActDescription(i);

        var taskSelect = dojo.byId("projectTask_id_" + i);
        if (dojo.attr(taskSelect, "disabled") != "disabled") {
            for (var j = 0; j < selectedProjectTasks.length; j++) {
                if (selectedProjectTasks[j].row == i) {
                    dojo.attr(taskSelect, { value: selectedProjectTasks[j].task });
                }
            }
        }
        setTaskDescription(i);

        if (dojo.byId("delete_button_" + i) === null || dojo.byId("delete_button_" + i) === undefined) {
            var deleteCell = rows[i].cells[0];
            var img = dojo.doc.createElement("img");
            dojo.addClass(img, "pointer");
            dojo.attr(img, {
                id: "delete_button_" + i,
                src: "resources/img/delete.png",
                alt: "Удалить",
                title: "Удалить",
                //без px так как IE не понимает
                height: "15",
                width: "15"
            });

            //неведома ошибка исправляется для IE добавлением onclick именно через функцию
            //индускод
            img.onclick = function () {
                var id = dojo.attr(this, "id");
                var id_num = parseInt(id.substring(id.lastIndexOf("_") + 1, id.length));
                console.log(id_num);
                deleteRow(id_num);
            };
            deleteCell.appendChild(img);
        }

        //ячейка с кнопкой(картинкой) запроса из JIRA
        if (dojo.byId("jira_button_" + i) === null || dojo.byId("jira_button_" + i) === undefined) {
            var jiraCell = rows[i].cells[10];
            var jiraImg = dojo.doc.createElement("img");
            dojo.addClass(jiraImg, "pointer");
            dojo.attr(jiraImg, {
                id: "jira_button_" + i,
                src: "resources/img/logo-jira.png",
                alt: "Запрос из JIRA",
                title: "Запрос из JIRA",
                //без px так как IE не понимает
                height: "15",
                width: "15"
            });

            //неведома ошибка исправляется для IE добавлением onclick именно через функцию
            jiraImg.onclick = function () {
                getJiraInfo(newRowIndex);
            };
            jiraCell.appendChild(jiraImg);
        }


    }

    recalculateDuration();

    if (dataDraft != null && dataDraft == "true") {
        loadDraft();
    }
}

/*
 * Очищает содержимое компонентов каждой строки табличной части отчёта.
 * Если resetActType == true, "Тип активности" тоже очищается.
 */
function resetRowState(rowIndex, resetActType) {
    if (resetActType) {
        //dojo.byId("selected_row_id_" + rowIndex).checked = false;
        dojo.attr("activity_type_id_" + rowIndex, {
            value: "0"
        });
    }

    var disabledText = "disabled";
    dojo.attr("workplace_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });

    dojo.attr("project_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    dojo.attr("project_role_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    dojo.attr("activity_category_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    var labelDescription = dojo.byId("act_description_" + rowIndex);
    labelDescription.innerHtml = "";
    setActDescription(rowIndex);
    dojo.attr("projectTask_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    dojo.byId("description_id_" + rowIndex).value = "";
    dojo.attr("description_id_" + rowIndex, {
        disabled: disabledText
    });

    dojo.byId("problem_id_" + rowIndex).value = "";
    dojo.attr("problem_id_" + rowIndex, {
        disabled: disabledText
    });

    dojo.attr("duration_id_" + rowIndex, {
        disabled: disabledText,
        value: ""
    });
    recalculateDuration();
}


/* Проверяет введённое значение часов, потраченных на выполнение задачи на валидность. */
function checkDuration(processed) {
    var input = null;
    if (processed.target == null) {
        input = processed;
    }
    else {
        input = processed.target;
    }
    var duration = dojo.attr(input, "value");
    var reg = /^\d{1,2}((\.|\,)\d{1,2})?$/;
    if (!reg.test(duration)) {
        duration = "";
    }
    dojo.attr(input, { value: duration    });
    recalculateDuration();
}

/* Производит пересчет общего количества часов, потраченных на все задачи. */
function recalculateDuration() {
    var totalDurationValue = 0.00;
    var hoursNodes = dojo.query(".duration");
    for (var i = 0; i < hoursNodes.length; i++) {
        var hours = parseFloat(dojo.attr(hoursNodes[i], "value").replace(",", "."));
        if (!isNaN(hours)) {
            totalDurationValue += hours;
        }
    }
    dojo.byId("total_duration").innerHTML = totalDurationValue.toFixed(2);
    return totalDurationValue;
}

/* Добавляет в табличную часть отчёта указанное количество новых строк. */
function addNewRows(rowsCount) {
    for (var i = 0; i < rowsCount; i++) {
        addNewRow();
    }
}

function CopyPlan() {
    var firstDescriptionId = "description_id_0";
    if (dojo.attr(firstDescriptionId, "disabled")) {
        return;
    }
    var plan_text = dojo.byId("plan_textarea").innerHTML;
    plan_text = plan_text.replace(/<br>/g, '\n');
    plan_text = plan_text.replace(/&amp;/g, '&');
    dojo.byId(firstDescriptionId).value = plan_text;
}

/* Производит пересчёт номеров строк табличной части отчёта. */
function recalculateRowNumbers() {
    var rows = dojo.query(".row_number");
    for (var i = 0; i < rows.length; i++) {
        rows[i].innerHTML = i + 1;
    }
}

/* Отображает диалог подтверждения отправки отчёта. */
function confirmSendReport() {
    var reportDate = dijit.byId("calDate").value;

    if (reportDate !== null) {
        if (reportDate !== undefined) {
            if (dateNotBetweenMonth(reportDate))
                return confirm("Указанная дата отличается от текущей более чем на 27 дней. Вы уверены, что хотите отправить отчет?");
        }
        else
            return confirm("Указанная дата некорректна. Вы уверены, что хотите отправить отчет?");
    }
    return confirm("Вы действительно хотите отправить отчет?");
}

function dateNotBetweenMonth(value) {
    var maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + 27);
    var minDate = new Date();
    minDate.setDate(minDate.getDate() - 27);
    return value > maxDate || value < minDate;
}

function invalidReportDate(value) {
    var maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + 27);
    var minDate = new Date();
    minDate.setDate(minDate.getDate() - 27);
    if (value > maxDate) return 1;
    if (value < minDate) return -1;
    return 0;
}

/* Отображает диалог подтверждения создания нового отчёта. */
function confirmCreateNewReport() {
    return confirm("Вы уверены?");
}

/*
 * Заполняет список "Категория активности" доступными значениями
 * в соответствии с определённой логикой.
 */
function fillAvailableActivityCategoryList(rowIndex) {
    var actTypeSelect = dojo.byId("activity_type_id_" + rowIndex);
    var projectRoleSelect = dojo.byId("project_role_id_" + rowIndex);
    var actCatSelect = dojo.byId("activity_category_id_" + rowIndex);
    var actType = dojo.attr(actTypeSelect, "value");
    var projectRole = dojo.attr(projectRoleSelect, "value");
    actCatSelect.options.length = 0;
    insertEmptyOption(actCatSelect);
    for (var i = 0; i < availableActCategoryList.length; i++) {
        if (actType == availableActCategoryList[i].actType && projectRole != 0 && projectRole == availableActCategoryList[i].projRole) {
            for (var j = 0; j < availableActCategoryList[i].avActCats.length; j++) {
                var actCatOption = dojo.doc.createElement("option");
                dojo.attr(actCatOption, {
                    value: availableActCategoryList[i].avActCats[j]
                });
                for (var k = 0; k < actCategoryList.length; k++) {
                    if (availableActCategoryList[i].avActCats[j] == actCategoryList[k].id) {
                        actCatOption.title = actCategoryList[k].value;
                        actCatOption.innerHTML = actCategoryList[k].value;
                        actCatSelect.appendChild(actCatOption);
                    }
                }
            }
        }
    }
    sortSelectOptions(actCatSelect);
}

function overtimeCauseChange(obj) {
    var select = obj.target === null || obj.target === undefined ? obj : obj.target;
    var selectId = dijit.byId(select.id).get('value');
    defaultOvertimeCause = selectId;
}

function checkDurationThenSendForm() {
    var totalDuration = recalculateDuration();
    var isHoliday = false;
    var isVacation = false;
    var isDivisionLeader = false;

    var formattedDate;
    var divisionId = dojo.byId('divisionId').value;
    var employeeId = dojo.byId('employeeId').value;
    var pickedDate = dijit.byId('calDate').get('value');

    if (typeof divisionId == typeof undefined || divisionId == null || divisionId == 0) {
        alert("Укажите подразделение, сотрудника и дату");
    } else if (typeof employeeId == typeof undefined || employeeId == null || employeeId == 0) {
        alert("Укажите сотрудника и дату");
    } else if (getFirstWorkDate() > pickedDate) {
        alert("Нельзя отправить отчет за выбранную дату, так как сотрудник еще не был принят на работу");
    } else {
        if (pickedDate) {
            formattedDate = pickedDate.format("yyyy-mm-dd");
        }

        dojo.xhrGet({
            url: getContextPath() + "/calendar/isholiday",
            headers: {
                "If-Modified-Since": "Sat, 1 Jan 2000 00:00:00 GMT"
            },
            handleAs: "text",
            timeout: 1000,
            failOk: true,
            content: { date: formattedDate, employeeId: employeeId },
            sync: true,

            load: function (dataAsText, ioArgs) {
                var data;

                try {
                    data = dojo.fromJson(dataAsText);
                } catch (e) {
                }

                if (data) {
                    isHoliday = data.isHoliday;
                }
            }
        });

        dojo.xhrGet({
            url: getContextPath() + "/calendar/isvacationwithoutplanned",
            headers: {
                "If-Modified-Since": "Sat, 1 Jan 2000 00:00:00 GMT"
            },
            handleAs: "text",
            timeout: 1000,
            failOk: true,
            content: { date: formattedDate, employeeId: employeeId },
            sync: true,

            load: function (dataAsText, ioArgs) {
                var data;

                try {
                    data = dojo.fromJson(dataAsText);
                } catch (e) {
                }

                if (data) {
                    isVacation = data.isVacation
                }
            }
        });

        dojo.xhrGet({
            url: getContextPath() + "/employee/isDivisionLeader",
            headers: {
                "If-Modified-Since": "Sat, 1 Jan 2000 00:00:00 GMT"
            },
            handleAs: "text",
            timeout: 1000,
            failOk: true,
            content: { employeeId: employeeId },
            sync: true,

            load: function (dataAsText, ioArgs) {
                var data;
                try {
                    data = dojo.fromJson(dataAsText);
                } catch (e) {
                }

                if (data) {
                    isDivisionLeader = data.isDivisionLeader
                }
            }
        });
        /* не РЦК */
        var check = !isDivisionLeader &&
            /*недоработка */
            ( ( totalDuration < (8 - undertimeThreshold) ||
                /* переработка */
                totalDuration > (8 + overtimeThreshold)
                ) ||
                /* выходной и не РЦК */
                isHoliday ||
                /* работа в отпуск */
                isVacation
                );
        if (check) {
            var select_box = dijit.byId("overtimeCause");

            select_box.removeOption(select_box.getOptions());
            select_box.addOption({ value: 0, label: "<div style='visibility: hidden;'>some invisible text, don't remove me!</div>" });

            var evald_json = isHoliday || isVacation ? workOnHolidayCauseList : (totalDuration < 8 ? unfinishedDayCauseList : overtimeCauseList);

            for (var key in evald_json) {
                var row = evald_json[key];
                select_box.addOption({ value: row.id, label: row.value });
            }

            if (defaultOvertimeCause) {
                select_box.set('value', defaultOvertimeCause);
            }

            var holidayDisplays = isHoliday || isVacation ? "" : "none";

            dojo.byId("holidayWarning").style.display = dojo.byId("typeOfCompensationContainer").style.display = holidayDisplays;

            var dialog = dijit.byId("dialogOne");

            dialog.set("title", "Укажите причину " + (isHoliday || isVacation ? "работы в выходной день" : (totalDuration < 8 ? "недоработок" : "переработок")));
            dialog.show();
        } else {

            // При отправке без диалога о недоработках очищаем служебные поля
            dojo.byId("overtimeCauseComment_hidden").value = "";
            dojo.byId("overtimeCause_hidden").value = "";
            dojo.byId("typeOfCompensation_hidden").value = "";

            submitform('send');
        }
    }
}


/*
 * Срабатывает при смене значения в списке проектов\пресейлов.
 * Управляет доступностью и содержимым списка проектных задач.
 */
function projectChange(obj) {
    var select = null;
    if (obj.target == null) {
        select = obj;
    }
    else {
        select = obj.target;
    }
    var selectId = dojo.attr(select, "id");
    var projectId = select.value;
    var rowIndex = selectId.substring(selectId.lastIndexOf("_") + 1, selectId.length);
    var taskSelect = dojo.byId("projectTask_id_" + rowIndex);
    var taskOption = null;
    taskSelect.options.length = 0;
    if (isEnableTaskSelect(rowIndex)) {
        dojo.attr(taskSelect, {
            disabled: "disabled",
            value: "0"
        });
        for (var i = 0; i < projectTaskList.length; i++) {
            if (projectId == projectTaskList[i].projId) {
                dojo.removeAttr(taskSelect, "disabled");
                insertEmptyOption(taskSelect);
                for (var j = 0; j < projectTaskList[i].projTasks.length; j++) {
                    taskOption = dojo.doc.createElement("option");
                    dojo.attr(taskOption, {
                        value: projectTaskList[i].projTasks[j].id
                    });
                    taskOption.title = projectTaskList[i].projTasks[j].value;
                    taskOption.innerHTML = projectTaskList[i].projTasks[j].value;
                    taskSelect.appendChild(taskOption);
                }
            }
        }
        dojo.byId("task_description_" + rowIndex).innerHTML = "";
    }
}