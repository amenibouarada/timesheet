// предназначен для обозначения значения "Все" в выпадашках (select)
var ALL_VALUE = 0;

/* Добавляет в указанный select пустой option. */
function insertEmptyOption(select) {
    insertEmptyOptionWithCaptionInHead(select, "");
}

/**
 *  Добавляет в указанный select пустой option с указанной подписью.
 *  @param value - значение пустого option, необязательный (по умолчанию 0)
*/
function insertEmptyOptionWithCaptionInHead(select, caption, value) {
    value = value || "0"; // если не указан, то по умолчанию 0
    var option = dojo.doc.createElement("option");
    dojo.attr(option, {
        value: value
    });
    option.innerHTML = caption;
    select.insertBefore(option, select.options[0]);
}

// Снимает выбранные элементы в селекте
function unselectValues(select) {
    for (var option in select.selectedOptions){
        option.selected = false;
    }
}

// Возвращает все выбранные значение(value) в <select multiple="true">
function getSelectValues(select) {
    var result = [];
    for (var i = 0; i < select.selectedOptions.length; i++){
        result.push(select.selectedOptions[i].value);
    }
    return result;
}

/* Сортирует по алфавиту содержимое выпадающих списков. */
function sortSelectOptions(select) {
    var tmpArray = [];
    for (var i = 0; i < select.options.length; i++) {
        tmpArray.push(select.options[i]);
    }
    tmpArray.sort(function (a, b) {
        return (a.text < b.text) ? -1 : 1;
    });
    select.options.length = 0;
    for (var i = 0; i < tmpArray.length; i++) {
        select.options[i] = tmpArray[i];
    }
}

/**
 * Заполняет список доступных проектов/пресейлов
 * @param rowIndex
 * @param projectState тип проекта (проект, пресейл ...)
 */
function fillProjectList(rowIndex, projectState) {
    var projectSelect = dojo.byId("project_id_" + rowIndex);
    projectSelect.options.length = 0;
    var division = dojo.byId("divisionId").value;
    if (division != 0) {

        dojo.forEach(dojo.filter(projectList, function (projectsOfDiv) {
            return (projectsOfDiv.divId == division);
        }), function (projectsOfDiv) {
            dojo.forEach(dojo.filter(projectsOfDiv.divProjs, function (project) {
                return ((project.state === projectState) && (project.active == 'true'));
            }), function (project) {
                var projectOption = dojo.doc.createElement("option");
                dojo.attr(projectOption, {
                    value: project.id
                });
                projectOption.title = project.value;
                projectOption.innerHTML = project.value;
                projectSelect.appendChild(projectOption);
            });
        });

        if (existsCookie('aplanaProject')) {
            projectSelect.value = getCookieValue('aplanaProject');
            // ToDo нужен ли этот вызов?
            projectChange(projectSelect);
        }
    } else {

    }
    sortSelectOptions(projectSelect);
}


/* Заполняет список доступных проектов/пресейлов для "Взаимной занятости" и формирования отчётов */

function fillProjectListByDivision(division, projectSelect, projectState) {

    if (division.value == null) {
        division.value = 0;
    }

    var showInactiveProjects = dojo.byId("showInactiveProjects");
    showInactiveProjects = showInactiveProjects == undefined ? false : showInactiveProjects.checked;

    //Очищаем список проектов.
    projectSelect.options.length = 0;
    var hasAny = false;
    dojo.removeAttr(projectSelect, "disabled");
    if (division == 0) {
        for (var i = 0; i < projectListWithOwnerDivision.length; i++) {
             if (projectState == undefined || projectState == null ||
                (projectState != undefined && projectListWithOwnerDivision[i].state == projectState)) {
                if (showInactiveProjects == true || projectListWithOwnerDivision[i].active == 'true') {
                    projectOption = dojo.doc.createElement("option");
                    dojo.attr(projectOption, {
                        value: projectListWithOwnerDivision[i].id
                    });
                    projectOption.title = projectListWithOwnerDivision[i].value;
                    projectOption.innerHTML = projectListWithOwnerDivision[i].value;
                    projectSelect.appendChild(projectOption);
                    hasAny = true;
                }
            }
        }
    } else {
        dojo.removeAttr(projectSelect, "disabled");
        for (var i = 0; i < projectListWithOwnerDivision.length; i++) {
            if (projectState == undefined || projectState == null ||
                (projectState != undefined && projectListWithOwnerDivision[i].state == projectState)) {
                if ((showInactiveProjects == true || projectListWithOwnerDivision[i].active == 'true') &&
                    division == projectListWithOwnerDivision[i].ownerDivisionId) {
                    projectOption = dojo.doc.createElement("option");
                    dojo.attr(projectOption, {
                        value: projectListWithOwnerDivision[i].id
                    });
                    projectOption.title = projectListWithOwnerDivision[i].value;
                    projectOption.innerHTML = projectListWithOwnerDivision[i].value;
                    projectSelect.appendChild(projectOption);
                    hasAny = true;
                }
            }
        }
    }
    sortSelectOptions(projectSelect);
    validateAndAddNewOption(hasAny, division, projectSelect);
    /* выбираем по умолчанию пункт "Все" */
    projectSelect.value = 0;
}

function validateAndAddNewOption(hasAny, divisionId, select){
    if (hasAny || divisionId == 0){
        insertEmptyOptionWithCaptionInHead(select, "Все");
    }else{
        insertEmptyOptionWithCaptionInHead(select, "Пусто");
        dojo.attr(select, {disabled:"disabled"});
    }
}

/* Выставляет должность сотрудника (проектная роль по умолчанию) */
function setDefaultEmployeeJob(rowIndex) {
    if (!document.employeeList || !dojo.byId("divisionId")) {
        return;
    }
    var selectedEmployeeId = dojo.byId("employeeId").value;
    var divisionId = dojo.byId("divisionId").value;
    var defaultEmployeeJobId = 0;
    for (var i = 0; i < employeeList.length; i++) {
        if (divisionId == employeeList[i].divId) {
            for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                if (employeeList[i].divEmps[j].id == selectedEmployeeId) {
                    defaultEmployeeJobId = employeeList[i].divEmps[j].jobId;
                    break;
                }
            }
        }
    }
    var actTypeLists = new Array();
    if (rowIndex >= 0) {
        actTypeLists.push(dojo.byId("activity_type_id_" + rowIndex));
    } else { //Если функция вызвана при выборе сотрудника
        actTypeLists = dojo.query(".activityType");
    }

    for (var j = 0; j < actTypeLists.length; j++) {
        var listId = dojo.attr(actTypeLists[j], "id");
        var row = listId.substring(listId.lastIndexOf("_") + 1, listId.length);
        // Проект Пресейл
        var projectRoleIdText = "project_role_id_" + row;
        if ((actTypeLists[j].value == EnumConstants.TypesOfActivityEnum.PROJECT)
            || (actTypeLists[j].value == EnumConstants.TypesOfActivityEnum.PRESALE)) {
            var projectRoleList = dojo.byId(projectRoleIdText);
            dojo.attr(projectRoleList, { value: defaultEmployeeJobId });
        }
        // Внепроектная активность
        else if (actTypeLists[j].value == "14") {
            var projectRoleList = dojo.byId(projectRoleIdText);
            dojo.attr(projectRoleList, { value: defaultEmployeeJobId });
        }
        //fillAvailableActivityCategoryList(row);
    }
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

/**
 * Обновляет список руководителей подразделения
 *
 * @param currentValue      - текущее значение селекта руководителей, чтоб после обновления попробовать его же и установить
 * @param managerList       - список всех руководителей типа List<Employee>
 * @param divisionId        - подразделение, по которому определяется список сотрудников
 * @param managerSelect     - селект, который заполняется руководителями
 */
function updateManagerListByDivision(currentValue, managerList, division, managerSelect) {
    var optionAllValue = -1;
    // зададим значения по умолчанию
    if (division == undefined) {
        division = dojo.byId("divisionId");
        if (division.value == null)
            division.value = 0;
    }
    var divisionId = division.value;
    managerList = managerList || managerMapJson;
    managerSelect = managerSelect || dojo.byId("managerId");
    currentValue = managerSelect.value || optionAllValue;

    managerSelect.options.length = 0;
    insertEmptyOptionWithCaptionInHead(managerSelect, "Все руководители", optionAllValue);

    if (managerList.length > 0) {
        dojo.forEach(dojo.filter(managerList, function (m) {
            return (m.division == divisionId);
        }), function (managerData) {
            var option = document.createElement("option");
            dojo.attr(option, {
                value: managerData.id
            });
            option.title = managerData.name;
            option.innerHTML = managerData.name;
            managerSelect.appendChild(option);
        });
    }
    if (managerSelect.options.length == 1 && managerSelect.options[0].value == optionAllValue) {
        managerSelect.disabled = true;
    } else {
        managerSelect.disabled = '';
    }
    managerSelect.value = currentValue;
    if (managerSelect.value == "") {
        managerSelect.value = optionAllValue;
    }
}

/*
 * функция чтобы показать хинт для уже выбранных значений селектов
 * здесь title-это атрибут у селекта - он же хинт
 */
function getTitle(processed) {
    var select = null;
    if (processed.target == null) {
        select = processed;
    }
    else {
        select = processed.target;
    }
    //костыль чтобы в категории активности отображалось описание
    if (select.id.indexOf("activity_category_id_") + 1) {
        var description = dojo.byId("act_description_" + select.id.substring(21)).innerHTML;
        if (description && description.trim() != "") {
            return  description;
        }
    }
    //
    var index = select.selectedIndex;
    if (select.options != null) {
        if ((index > -1) && (select.options[index].text != "")) {
            return select.options[index].text;
        }
        else {
            return 'значение еще не выбрано';
        }
    }
    else if (select.textbox != null) {
        if (select.textbox.value != "") return select.textbox.value
        else return 'значение еще не выбрано';
    }
}