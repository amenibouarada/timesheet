dojo.require("dijit.form.DateTextBox");
dojo.require("dojo.NodeList-traverse");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ObjectStore");
dojo.require("dojo.store.Memory");

var selectedAllRegion = null;

dojo.ready(function () {
    window.focus();
    dojo.connect(dojo.byId("divisionId"), "onchange", dojo.byId("divisionId"), updateManagerList);
    dojo.connect(dojo.byId("manager"), "onchange", dojo.byId("manager"), updateEmployeeList);
    dojo.connect(dojo.byId("regions"), "onchange", dojo.byId("regions"), updateEmployeeList);

    dojo.byId("divisionId").value = divisionIdJsp;

    initRegionsList();
    updateManagerList();

    if (dojo.byId("regions").value != -1) {
        sortEmployee();
        selectedAllRegion = false;
    } else {
        selectedAllRegion = true;
    }
});

dojo.declare("DateTextBox", dijit.form.DateTextBox, {
    popupClass: "dijit.Calendar",
    datePattern: 'dd.MM.yyyy'
});

//устанавливается значение по умолчанию "Все регионы"
function initRegionsList() {
    var regionsSelect = dojo.byId("regions");
    if (regions.length == 1) {
        if (regions[0] == allValue) {
            regionsSelect[0].selected = true;
            selectedAllRegion = true;
        } else {
            selectedAllRegion = false;
        }
    }

}

function showBusinessTripsAndIllnessReport() {
    var divisionId = divisionIdJsp;
    var regions = dojo.byId("regions").value;
    var manager = dojo.byId("manager").value;

    empId = getEmployeeId();
    var divisionId = dojo.byId("divisionId").value;

    var dateFrom = dojo.byId("dateFrom").value;
    var dateTo = dojo.byId("dateTo").value

    var regionsValid = getSelectedIndexes(dojo.byId("regions")).length > 0;

    var datesValid = (dateFrom != null && dateFrom != undefined && dateFrom != "") && (dateTo != null && dateTo != undefined && dateTo != "") && (dateFrom <= dateFrom);

    if (datesValid && divisionId != null && divisionId != 0 && empId != null && empId != 0 && regionsValid) {
        businesstripsandillness.action = getContextPath() + "/businesstripsandillness/"
            + divisionId + "/" + empId;
        businesstripsandillness.submit();
    } else {
        var error = "";
        if (dateFrom == null || dateFrom == undefined || dateFrom == "") {
            error += ("Необходимо выбрать дату начало периода!\n");
        }

        if (dateTo == null || dateTo == undefined || dateTo == "") {
            error += ("Необходимо выбрать дату окончания периода!\n");
        }

        if (dateFrom > dateTo) {
            error += ("Дата окончания периода должна быть больше даты начала периода!\n");
        }

        if (divisionId == 0 || divisionId == null) {
            error += ("Необходимо выбрать подразделение и сотрудника!\n");
        }
        else if (empId == 0 || empId == null) {
            error += ("Необходимо выбрать сотрудника!\n");
        }
        if (!regionsValid) {
            error += ("Необходимо выбрать регион или несколько регионов!\n");
        }
        alert(error);
    }
}

function getEmployeeId() {
    return dijit.byId("employeeIdDiv").item != undefined ? dijit.byId("employeeIdDiv").item.id : null;
}

function createBusinessTripOrIllness() {
    var empId = getEmployeeId();

    if (empId != null && empId != 0) {
        saveForm();
    } else {
        alert("Необходимо выбрать сотрудника!\n");
    }
}

function deleteReport(parentElement, rep_id, calendarDays, workingDays, workDaysOnIllnessWorked) {
    if (!confirm("Подтвердите удаление!")) {
        return;
    }

    var prevHtml = parentElement.innerHTML;

    dojo.addClass(parentElement, "activity-indicator");
    parentElement.innerHTML = loadImg;

    function handleError(error) {
        resetParent();

        alert("Удаление не произошло:\n\n" + error);
    }


    function resetParent() {
        dojo.removeClass(parentElement, "activity-indicator");
        parentElement.innerHTML = prevHtml;
    }

    dojo.xhrGet({
        url: getContextPath() + "/businesstripsandillness/delete/" + rep_id + "/" + reportFormedJsp,
        handleAs: "text",

        load: function (data) {
            if (data.length == 0) {
                dojo.destroy(dojo.NodeList(parentElement).parents("tr")[0]);
                recountResults(calendarDays, workingDays, workDaysOnIllnessWorked);
            } else {
                handleError(data);
            }
        },

        error: function (error) {
            handleError(error.message);
        }
    });
}
function recountResults(calendarDays, workingDays, workDaysOnIllnessWorked) {
    if (reportFormedJsp == 6) {
        recountIllness(calendarDays, workingDays, workDaysOnIllnessWorked);
    }
    if (reportFormedJsp == 7) {
        if (!forAll) {
            decreaseResultDays(document.getElementById("mounthCalendarDaysInBusinessTrip"), calendarDays);
            decreaseResultDays(document.getElementById("mounthWorkDaysOnBusinessTrip"), workingDays);
        }
    }
}
function decreaseResultDays(cellWithResults, daysToDecrease) {
    var daysInTable = parseFloat(cellWithResults.innerHTML);
    var recountedDays = daysInTable - daysToDecrease;
    cellWithResults.innerHTML = recountedDays;
}
function recountIllness(calendarDays, workingDays, workDaysOnIllnessWorked) {
    if (!forAll) {
        if (periodicalsListNotEmpty) {
            decreaseResultDays(document.getElementById("mounthCalendarDaysOnIllness"), calendarDays);
            decreaseResultDays(document.getElementById("mounthWorkDaysOnIllness"), workingDays);
            decreaseResultDays(document.getElementById("mounthWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
        }
        decreaseResultDays(document.getElementById("yearWorkDaysOnIllness"), workingDays);
        decreaseResultDays(document.getElementById("yearWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
    }
}
function editReport(reportId) {
    businesstripsandillness.action = getContextPath() + "/businesstripsandillnessadd/" + reportId + "/" + reportFormedJsp;
    businesstripsandillness.submit();
}

function updateManagerList() {
    var divisionId = dojo.byId("divisionId").value;
    var managersNode = dojo.byId("manager");
    var manager = managersNode.value;

    var emptyOption = dojo.doc.createElement("option");
    dojo.attr(emptyOption, {
        value: -1
    });
    emptyOption.title = "Все руководители";
    emptyOption.innerHTML = "Все руководители";

    managersNode.options.length = 0;
    managersNode.appendChild(emptyOption);

    if (managerMapJson.length > 0) {
        var managerMap = dojo.fromJson(managerMapJson);
        dojo.forEach(dojo.filter(managerMap, function (m) {
            return (m.division == divisionId);
        }), function (managerData) {
            var option = document.createElement("option");
            dojo.attr(option, {
                value: managerData.id
            });
            option.title = managerData.name;
            option.innerHTML = managerData.name;
            managersNode.appendChild(option);
        });
    }
    if (managersNode.options.length == 1 && emptyOption.value == managersNode.options[0].value) {
        managersNode.disabled = 'disabled';
    } else {
        managersNode.disabled = '';
    }
    managersNode.value = managerIdJsp;
    updateEmployeeList();
}

function getSelectedIndexes(multiselect) {
    var arrIndexes = new Array;
    for (var i = 0; i < multiselect.options.length; i++) {
        if (multiselect.options[i].selected) arrIndexes.push(i);
    }
    return arrIndexes;
}

function updateEmployeeList() {
    var divisionId = dojo.byId('divisionId').value;
    dojo.xhrGet({
        url: getContextPath() + "/employee/employeeListWithLastWorkday/" + divisionId,
        handleAs: "json",
        timeout: 10000,
        sync: true,
        preventCache: false,
        headers: {  'Content-Type': 'application/json;Charset=UTF-8',
            "Accept": "application/json;Charset=UTF-8"},
        load: function (data) {
            refreshEmployeeSelect(data);
        },

        error: function (error) {
            handleError(error.message);
        }
    });
}

function refreshEmployeeSelect(employeeList) {

    var managerId = dojo.byId('manager').value;
    var cities = [];
    var citySelect = dojo.byId('regions').options;

    for (var i = 0; i < citySelect.length; i++) {
        if (citySelect[i].selected) cities.push(citySelect[i].value);
    }

    if (employeeList.length > 0) {


            var employeeArray = [];
            var emptyObj = {
                id: 0,
                value: ""
            };
            employeeArray.push(emptyObj);
            dojo.forEach(employeeList, function (employee) {

                var manegerEquals = (managerId == -1 || employee.manId == managerId);
                var regionEquals = (cities.length == 1 && cities[0] == -1) || (dojo.indexOf(cities, +employee.regId) == 0);
                if (manegerEquals && regionEquals && !employee.lastWorkDate) {
                    employeeArray.push(employee);
                }
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

            var employeeFlteringSelect = dijit.byId("employeeIdDiv");

            if (!employeeFlteringSelect) {
                employeeFlteringSelect = new dijit.form.FilteringSelect({
                    id: "employeeIdDiv",
                    name: "employeeIdDiv",
                    labelAttr: "value",
                    store: employeeDataStore,
                    searchAttr: 'value',
                    queryExpr: "*\${0}*",
                    ignoreCase: true,
                    autoComplete: false,
                    style: 'width:200px',
                    required: true,
                    onChange: function () {
                        var selectedEmploye2 = this.item ? this.item.id : null;
                        dojo.byId('employeeId').value = selectedEmploye2;

                    },
                    onMouseOver: function () {
                        tooltip.show(getTitle(this));
                    },
                    onMouseOut: function () {
                        tooltip.hide();
                    }
                }, "employeeIdDiv");
                employeeFlteringSelect.startup();
            } else {
                employeeFlteringSelect.set('store', employeeDataStore);
                dijit.byId("employeeIdDiv").set('value', null);
                dojo.byId('employeeId').value = null;
            }

    }
    dijit.byId("employeeIdDiv").set('value', empId);
}