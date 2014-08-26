/**
 * User: iziyangirov
 * Date: 29.01.13
 * Здесь собраны общие скрипты для отчетов
 */

dojo.addOnLoad(function() {
    setDefaultRegion();
});

function allRegionsCheckBoxChange(checked){
    var regionIds = "regionIds";
    if (checked) {
        dojo.attr(regionIds, {disabled:"disabled"});
        dojo.byId(regionIds).selectedIndex = -1;
    } else {
        dojo.removeAttr(regionIds, "disabled");
    }
}

function allDatesCheckBoxChange(checked){
    var begin = "beginDate";
    var end = "endDate";
    if (checked) {
        dijit.byId(begin).set("readOnly", true);
        dijit.byId(end).set("readOnly", true);
    } else {
        dijit.byId(begin).set("readOnly", false);
        dijit.byId(end).set("readOnly", false);
    }
}

function setDefaultRegion(){
    var allRegions = dojo.byId("allRegions");
    if (allRegions != null){
        allRegions.checked = true;
        allRegionsCheckBoxChange(true);
    }
}

function fillEmployeeListByDivision(division) {
    var employeeSelect = dojo.byId("employeeId");
    var employeeOption = null;
    if (division == null) {
        division = dojo.byId("divisionId");

        if (division.value == null)
            division.value = 0;
    }
    var divisionId = division.value;
    var showInactiveEmployees = dojo.byId("showInactiveEmployees").checked;
    dojo.removeAttr(employeeSelect, "disabled");
    //Очищаем список сотрудников.
    employeeSelect.options.length = 0;
    var hasAny = false;
    for (var i = 0; i < employeeList.length; i++) {
        if (divisionId == employeeList[i].divId) {
            for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                if (employeeList[i].divEmps[j].id != 0 && (showInactiveEmployees==true || employeeList[i].divEmps[j].active=='true')) {
                    employeeOption = dojo.doc.createElement("option");
                    dojo.attr(employeeOption, {
                        value:employeeList[i].divEmps[j].id
                    });
                    employeeOption.title = employeeList[i].divEmps[j].value;
                    employeeOption.innerHTML = employeeList[i].divEmps[j].value;
                    employeeSelect.appendChild(employeeOption);
                    hasAny = true;
                }
            }
        }
    }
    sortSelectOptions(employeeSelect);
    validateAndAddNewOption(hasAny, divisionId, employeeSelect);

    var rows = dojo.query(".row_number");
    for (var i = 0; i < rows.length; i++) {
        fillProjectList(i, dojo.byId("activity_type_id_" + i).value);
    }
}

/* Заполняет список доступных проектов/пресейлов */
function fillProjectListByDivision(division) {

    if (division == null) {
        division = dojo.byId("divisionId");

        if (division.value == null)
            division.value = 0;
    }
    var divisionId = division.value;
    var showInactiveProjects = dojo.byId("showInactiveProjects").checked;
    var projectSelect = dojo.byId("projectId");
    dojo.removeAttr(projectSelect, "disabled");
    //Очищаем список проектов.
    projectSelect.options.length = 0;
    var hasAny = false;
    if (divisionId == 0) {
        for (var i = 0; i < fullProjectList.length; i++) {
            if (showInactiveProjects==true || projectListWithOwnerDivision[i].active=='true') {
                projectOption = dojo.doc.createElement("option");
                dojo.attr(projectOption, {
                    value: fullProjectList[i].id
                });
                projectOption.title = fullProjectList[i].value;
                projectOption.innerHTML = fullProjectList[i].value;
                projectSelect.appendChild(projectOption);
                hasAny = true;
            }
        }
    } else {
        dojo.removeAttr("projectId", "disabled");
        dojo.removeAttr("divisionId", "disabled");
        for (var i = 0; i < projectListWithOwnerDivision.length; i++) {
            if (divisionId == projectListWithOwnerDivision[i].ownerDivisionId && (showInactiveProjects==true || projectListWithOwnerDivision[i].active=='true')) {
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
    sortSelectOptions(projectSelect);
    validateAndAddNewOption(hasAny, divisionId, projectSelect);
    /* выбираем по умолчанию пункт "Все" */
    projectSelect.value=0;
}

function validateAndAddNewOption(hasAny, divisionId, select){
    if (hasAny || divisionId == 0){
        insertEmptyOptionWithCaptionInHead(select, "Все");
    }else{
        insertEmptyOptionWithCaptionInHead(select, "Пусто");
        dojo.attr(select, {disabled:"disabled"});
    }
}

function setDefaultValuesForReport2And3(){
    reportForm.emplDivisionId.value = 0;
    reportForm.employeeId.value = 0;
    reportForm.projectId.value = 0;
    fillProjectListByDivision(reportForm.divisionId);
    fillEmployeeListByDivision(reportForm.emplDivisionId);
}