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

/**
 * Loads a JavaScript from the given path.
 *
 * @param {String} path  the path to the script
 */
function load(path) {
    var uri = ((path.charAt(0) == '/' || path.match(/^\w+:/)) ?
        "" : aplana.dbmi._jsContextPath + "/js/") + path;
    try {
        return dojo._loadUri(uri);
    } catch (e) {
        console.error(e);
        return false;
    }
}

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
                if (date <= getFirstWorkDate(getEmployeeData())) // день раньше начала работы
                    return '';
                var lastWorkDate = getLastWorkDate(getEmployeeData());
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

    var dailyTimesheetData = requestDailyTimesheetData(currentDate,  dojo.byId('employeeId').value);

    if (isErrorPage) {
        refreshPreviousDayPlans(dailyTimesheetData);
        reloadRowsState();
    } else {
        refreshDailyTimesheetData(dailyTimesheetData);
    }

    updateJiraButtonVisibility();
}

// переопределение метода из timesheet.js, не удалять
function getEmployeeData() {
    return dijit.byId("employeeIdSelect").item;
}

function updateEmployeeSelect() {
    var showFiredEmployees = getCookieValue('SHOW_ALLUSER') == 'active' ? true : false;
    var divisionId = timeSheetForm.divisionId.target == null ? timeSheetForm.divisionId.value : timeSheetForm.divisionId.target.value;
    if (!divisionId || +divisionId == 0) {
        refreshEmployeeSelect([]);
        checkIsVacationDay();
    } else {
        dojo.xhrGet({
            url: getContextPath() + "/employee/employeeListWithLastWorkday/" + divisionId + "/" + showFiredEmployees + "/true",
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

function requestDailyTimesheetData(date, employeeId){
    var dailyTimesheetData = null;
    var month = correctLength(date.getMonth() + 1);
    var year = date.getFullYear();
    var day = correctLength(date.getDate());
    var requestDate = year + "-" + month + "-" + day;
    dojo.xhrGet({
        url: getContextPath() + "/timesheet/dailyTimesheetData",
        handleAs: "json",
        timeout: 10000,
        sync: true,
        content: {date: requestDate, employeeId: employeeId},
        load: function (data, ioArgs) {
            dailyTimesheetData = data;
        },
        error: function (err, ioArgs) {
            if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                console.log(err);
            }
        }
    });
    return dailyTimesheetData;
}

/**
 * Обновляет элементы страницы (план с прошлого дня, строки таблицы списания, план на следующий день)
 */
function refreshDailyTimesheetData(data) {
    if (data == null) { return; }
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
    refreshPreviousDayPlans(data);

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

    var currentPlan = (current.plan != null && current.plan.length != 0) ? (current.plan) : "";
    var nextWorkDate = current.nextWorkDate;
    var nextDayEffort = current.effort;
    var nextDaySummary = (next.workSummary != null && next.workSummary != 0) ? (next.workSummary) : "";

    dojo.byId("lbNextPlan").innerHTML = (nextWorkDate != null) ?
        "Планы на следующий рабочий день (" + timestampStrToDisplayStr(nextWorkDate.toString()) + "):" :
        "Планы на следующий рабочий день:";

    if (nextDayEffort != null) {
        dojo.byId("effortInNextDay").value = nextDayEffort;
    }

    dojo.byId('plan').value = dojoxDecode(currentPlan != "" ? currentPlan : nextDaySummary);

    dojo.attr("effortInNextDay", {
        disabled: isFinal
    });
    dojo.attr("plan", {
        readonly: isFinal
    });

    isFinalForm = isFinal;

    updateJiraButtonVisibility();
    setElementsAvailability(isFinal);
}

// Заполнение поля планов, указанных в предыдущий рабочий день.
function refreshPreviousDayPlans(data) {
    if (data == null) { return; }

    var previousWorkDate = data.previousDayData.workDate;
    var previousPlan = dojoxDecode(data.previousDayData.plan);

    dojo.byId("lbPrevPlan").innerHTML = (previousWorkDate != null) ?
        "Планы предыдущего рабочего дня (" + timestampStrToDisplayStr(previousWorkDate.toString()) + "):" :
        "Планы предыдущего рабочего дня:";

    dojo.byId("plan_textarea").innerHTML = (previousPlan != null && previousPlan.length != 0) ?
        previousPlan.replace(/\n/g, '<br>') :
        "План предыдущего рабочего дня не был определен";

    isFinalForm = false;
}

function hideShowElement(id, isHide) {
    console.log(id + " " + isHide);
    dojo.setStyle(id, {"display": isHide ? "none" : ""});
}

function requiredCommentSet() {
    var overtimeCause = dijit.byId("overtimeCause").get("value");
    var undertimeExp = (overtimeCause == undertimeOtherJsp);
    var workOnHolidayExp = (overtimeCause == workHolidayOtherJsp);
    var overtimeExp = (overtimeCause == overtimeOtherJsp);

    if (undertimeExp || overtimeExp || workOnHolidayExp) {
        dijit.byId("overtimeCauseComment").attr("required", true);
    } else {
        dijit.byId("overtimeCauseComment").attr("required", false);
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

/*
 Влючено ли поле выбора задачи для для текущей строчки
 */
function isEnableTaskSelect(rowIndex) {
    var typeActivitySelect = dojo.byId("activity_type_id_" + rowIndex);
    return typeActivitySelect.value != EnumConstants.TypesOfActivityEnum.NON_PROJECT;
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