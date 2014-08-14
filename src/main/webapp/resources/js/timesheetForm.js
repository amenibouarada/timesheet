dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.Dialog");
dojo.require("dijit.form.Textarea");
dojo.require("dijit.form.Select");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dojox.widget.Standby");
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

var month = correctLength(new Date().getMonth() + 1);
var root = getRootEventListener();
var standByElement;
var dojoxDecode = dojox.html.entities.decode;
var decodeMap = [
    ["\u0027", "#39"],
    ["\u0028", "#40"],
    ["\u0029", "#41"]
];
var currentDate;
var isFinalForm;


dojo.declare("DateTextBox", dijit.form.DateTextBox, {
    popupClass: "Calendar"
});

function initTimeSheetForm() {
    dojo.connect(dojo.byId("jira_get_plans_button"), "click", dojo.byId("jira_get_plans_button"), getJiraPlans);
    dojo.connect(dojo.byId("plan"), "onkeyup", dojo.byId("plan"), textareaAutoGrow);
    dojo.connect(dojo.byId("divisionId"), "onchange", dojo.byId("divisionId"), updateEmployeeSelect);

    if (timeSheetForm) {
        timeSheetForm.divisionId.value = divIdJsp;
    }

    divisionChange(timeSheetForm.divisionId);
    updateEmployeeSelect();

    timeSheetForm.employeeId.value = employeeIdJsp;

    /*смотрим, поддерживаются ли куки и рисуем индикатор*/
    showCookieIndicator();

    if (selectedCalDate != "") {
        setTimesheetDate(selectedCalDate);
    } else {
        setDefaultDate(dojo.byId("employeeId").value);
    }

    initCurrentDateInfo(employeeIdJsp, dijit.byId('calDate').value, '/calendar/dates');

    currentDate = dijit.byId('calDate').get("value");

    if (!isErrorPage) {
        requestAndRefreshDailyTimesheetData(dijit.byId('calDate').value, dojo.byId('employeeId').value);
    } else {
        requestAndRefreshPreviousDayPlans(dijit.byId('calDate').value, dojo.byId('employeeId').value);
        reloadRowsState();
    }

    //крутилка создается при после загрузки страницы,
    //т.к. если она создается в месте использования - ghb show не отображается картинка
    standByElement = new dojox.widget.Standby({target: dojo.query("body")[0], zIndex: 1000});

    checkIsVacationDay();
}

function updateEmployeeSelect() {
    var divisionId = timeSheetForm.divisionId.target == null ? timeSheetForm.divisionId.value : timeSheetForm.divisionId.target.value;

    if (employeeList.length > 0) {

        dojo.forEach(dojo.filter(employeeList, function (division) {
            return (division.divId == divisionId);
        }), function (divisionData) {
            var employeeArray = [];
            var emptyObj = {
                id: 0,
                value: ""
            };
            employeeArray.push(emptyObj);
            dojo.forEach(divisionData.divEmps, function (employee) {
                employeeArray.push(employee);
            });
            employeeArray.sort(function (a, b) {
                return (a.value < b.value) ? -1 : 1;
            });


            var employeeDataStore = new dojo.data.ObjectStore({
                objectStore: new dojo.store.Memory({
                    data: employeeArray,
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
        });
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
            var cookieRowsCount = cookieValue("aplanaRowsCount");
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

function submitform(s) {
    if (typeof(root.onbeforeunload) != "undefined") {
        root.onbeforeunload = null;
    }

    if ((s == 'send' && confirmSendReport()) || s == 'send_draft') {
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
        if (s == 'send') {
            timeSheetForm.action = "timesheet";
        } else if (s == 'send_draft') {
            timeSheetForm.action = "sendDraft";
        }

        processing();
        // disabled не включается в submit. поэтому снимем аттрибут.
        dojo.removeAttr("divisionId", "disabled");
        dojo.removeAttr("employeeId", "disabled");
        timeSheetForm.submit();

    }
    else if (s == 'newReport' && confirmCreateNewReport()) {
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


