function somethingChanged() {
    if (typeof(root.onbeforeunload) != "undefined")
        root.onbeforeunload = function(){
            if (tablePartNotEmpty() || planBoxNotEmpty())
                return "Отчет не был отправлен.";
        }
}

function onEmployeeChange(employeeObj) {
    setDefaultEmployeeJob(-1);
    setDefaultDate();
}

/**
 * Срабатывает при смене даты отчета
 * @param calDateObj
 */
function onCalDateChange(calDateObj) {
    calDateObj.constraints.min = getFirstWorkDate(getEmployeeData());
    var lastWorkDate = getLastWorkDate(getEmployeeData());
    if (lastWorkDate != null && lastWorkDate != "") {
        calDateObj.constraints.max = lastWorkDate;
    } else {
        calDateObj.constraints.max = new Date(2100, 1, 1);
    }
    validateReportDate(calDateObj.value);

    var sameDateOnChangeEventFired = (currentDate.getTime() == dijit.byId('calDate').get("value").getTime());

    if (!sameDateOnChangeEventFired) {
        if (!isFinalForm && (tablePartNotEmpty() || planBoxNotEmpty()) &&
            (selectedCalDate == '')) // если дата пришла с контроллера, то не запускаем диалог, иначе показываем
        {
            var dialog = dijit.byId("dialogChangeDate");
            dojo.style(dialog.closeButtonNode, "display", "none");
            dialog.show();
        } else {
            if (selectedCalDate != ''){
                dijit.byId('calDate').set("value", getDateByString(selectedCalDate));
                selectedCalDate = ''; // чтоб при следующей смене дат - диалог всё же отображался
            }
            refreshDailyTimesheetData(requestDailyTimesheetData(calDateObj.value, dojo.byId('employeeId').value));

            if (isErrorPage) {
                dojo.style("errors_box", {"display": "none"});
                isErrorPage = false;
            }
            currentDate = dijit.byId('calDate').get("value");
        }
    }
}

function confirmCalDateChangeWithReload() {
    var dialog = dijit.byId("dialogChangeDate");
    dialog.hide();

    refreshDailyTimesheetData(requestDailyTimesheetData(dijit.byId('calDate').value, dojo.byId('employeeId').value));

    if (isErrorPage) {
        dojo.style("errors_box", {"display": "none"});
        isErrorPage = false;
    }
    currentDate = dijit.byId('calDate').get("value");
}

function confirmCalDateChangeWithSave(){
    var dialog = dijit.byId("dialogChangeDate");
    dialog.hide();
}

function cancelCalDateChange() {
    var dialog = dijit.byId("dialogChangeDate");
    dialog.hide();

    dijit.byId('calDate').set("value", currentDate);
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
    updateJiraButtonVisibility();
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

function overtimeCauseChange(obj) {
    var select = obj.target === null || obj.target === undefined ? obj : obj.target;
    var selectId = dijit.byId(select.id).get('value');
    defaultOvertimeCause = selectId;
}

