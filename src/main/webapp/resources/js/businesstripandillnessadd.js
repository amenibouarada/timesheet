dojo.require("dijit.form.Select");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ObjectStore");
dojo.require("dojo.store.Memory");
dojo.require(CALENDAR_EXT_PATH);

var illnessReportType = 6;
var businessTripReportType = 7;

var businesstrip_project = 55;
var businesstrip_notproject = 56;

var projectUndefined = -1;

var errors;

function getEmployeeId() {
    var employee = dijit.byId("employeeId");
    if (employee) {
        return parseInt(employee.item != undefined ? employee.item.id : -1);
    } else {
        return employeeIdJsp;
    }
}

initCurrentDateInfo(getEmployeeId());

dojo.declare("Calendar", com.aplana.dijit.ext.SimpleCalendar, {
    getEmployeeId: getEmployeeId
});

dojo.declare("DateTextBox", dijit.form.DateTextBox, {
    popupClass: "Calendar",
    datePattern: 'dd.MM.yyyy',

    openDropDown: function () {
        updateDateConstraints();
        this.inherited(arguments);
    }
});

dojo.ready(function () {
    window.focus();
    updateView();
    updateDateConstraints();
    initEmployeeSelect();
});

function initEmployeeSelect() {

    var employeeArray = [];
    var empty = {id: -1, value: ""};
    employeeArray.push(empty);

    dojo.forEach(employeeList, function (employee) {
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

    var employeeFlteringSelect = new dijit.form.FilteringSelect({
        id: "employeeId",
        name: "employeeId",
        store: employeeDataStore,
        searchAttr: 'value',
        queryExpr: "*\${0}*",
        ignoreCase: true,
        autoComplete: false,
        style: 'width:200px',
        required: true,
        onChange: function () {
            setDefaultEmployeeJob(-1);
        },
        onMouseOver: function () {
            tooltip.show(getTitle(this));
        },
        onMouseOut: function () {
            tooltip.hide();
        }
    }, "employeeId");
    employeeFlteringSelect.startup();
    employeeFlteringSelect.set('value', employeeIdJsp != "" ? +employeeIdJsp : null);

}

function updateView() {
    var obj = dojo.byId("reportType");
    var willBeDisplayedId = null;
    if (obj.target == null) {
        willBeDisplayedId = obj.value;
    }
    else {
        willBeDisplayedId = obj.target.value;
    }

    if (willBeDisplayedId == businessTripReportType) {
        showBusinessTrips();
        updateProject();
    }
    else if (willBeDisplayedId == illnessReportType) {
        showIllnesses();
    }
    document.getElementById("headerName").innerHTML = getHeader(willBeDisplayedId);
}

function getHeader(willBeDisplayedId) {
    if (hasReportId) {
        return "Создание " + getReportName();
    } else {
        return "Редактирование " + getReportName();
    }
}

function showBusinessTrips() {
    document.getElementById("illness").className = 'off';
    document.getElementById("businesstrip").className = 'creationform';
}

function showIllnesses() {
    document.getElementById("illness").className = 'creationform';
    document.getElementById("businesstrip").className = 'off';
}

function submitform() {
    dojo.byId("create").disabled = true;
    processing();
    if (validate()) {
        if (hasReportId) {
            var employeeId = getEmployeeId();
            mainForm.action = getContextPath() + "/businesstripsandillnessadd/tryAdd/" + employeeId;
        } else {
            mainForm.action = getContextPath() + "/businesstripsandillnessadd/trySave/" + +reportId;
        }
        mainForm.submit();
    } else {
        stopProcessing();
        dojo.byId("create").disabled = false;
    }
}

function cancelform() {
    mainForm.action = getContextPath() + "/businesstripsandillness/";
    mainForm.submit();
}

function validate() {
    delete errors;
    errors = new Array();
    if (checkRequired() && checkDates()) {
        return true;
    } else {
        showErrors();
        return false;
    }
}

function showErrors() {
    document.getElementById("errorboxdiv").className = 'fullwidth onblock errorbox';
    var errortext = "";
    for (var errorindex in errors) {
        errortext = errortext + errors[errorindex] + "\n";
    }
    document.getElementById("errorboxdiv").firstChild.nodeValue = errortext;
}

function checkRequired() {
    if (checkCommon()) {
        if (document.getElementById("reportType").value == illnessReportType) {
            return checkIllness();
        } else if (document.getElementById("reportType").value == businessTripReportType) {
            return checkBusinessTrip();
        } else {
            errors.push("Выбран неизвестный тип отчета!");
            return false;
        }
    } else {
        errors.push("Необходимо правильно заполнить все поля!");
        return false;
    }
}

function checkBusinessTrip() {
    var businessTripType = document.getElementById("businessTripType").value;
    if (businessTripType != null) {
        if (businessTripType == businesstrip_project) {
            var projectId = document.getElementById("projectId").value;
            if (projectId == null || projectId == projectUndefined || projectId == "0") {
                errors.push("Для проектной командировки необходимо выбрать проект!");
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    } else {
        errors.push("Не выбран тип командировки!");
        return false;
    }
}

function checkIllness() {
    return document.getElementById("reason").value != null;
}

function checkCommon() {
    return (document.getElementById("reportType").value != null && document.getElementById("beginDate").value != null &&
        document.getElementById("endDate").value != null);
}

function checkDates() {
    var beginDate = parseDate(document.getElementById("beginDate").value);
    var endDate = parseDate(document.getElementById("endDate").value);

    if (endDate >= beginDate) {
        return true;
    } else {
        var reportName = getReportName();
        errors.push("Дата окончания " + reportName + " не может быть раньше даты начала!");
        return false;
    }

}

function getReportName() {
    var reportType = dojo.byId("reportType").value;
    reportType = parseInt(reportType);
    switch (reportType) {
        case illnessReportType:
            return"больничного";
        case businessTripReportType :
            return "командировки";
        default:
            return "";
    }
}

function parseDate(dateString) {
    var dateParts = dateString.split(".");
    return new Date(dateParts[2], (dateParts[1] - 1), dateParts[0]);
}

function updateProject() {
    var businessTripType = dojo.byId("businessTripType").value;
    if (businessTripType == businesstrip_notproject) {
        document.getElementById("businesstripproject").className = 'off';
        document.getElementById("projectId").disabled = true;
    }
    else {
        document.getElementById("businesstripproject").className = 'onblock';
        document.getElementById("projectId").disabled = false;
        var projectIdElement = dojo.byId("projectId");
        projectIdElement.innerHTML = loadingImageUrl;

        dojo.xhrGet({
            url: getContextPath() + "/businesstripsandillnessadd/getprojects",
            handleAs: "json",

            load: function (data) {
                updateProjectList(data);
            },

            error: function (error) {
                projectIdElement.setAttribute("class", "error");
                projectIdElement.innerHTML = error;
            }
        });
    }
}

function updateProjectList(obj) {
    var projectIdElement = dojo.byId("projectId");

    for (var projectindex in obj) {
        var projectOption = dojo.doc.createElement("option");
        dojo.attr(projectOption, {
            value: obj[projectindex].id
        });
        projectOption.title = obj[projectindex].value;

        projectOption.innerHTML = obj[projectindex].value;
        projectIdElement.appendChild(projectOption);
    }
    sortSelectOptions(projectIdElement);
    if (hasProjectId) {
        projectIdElement.value = projectId;
    }
}

function updateDateConstraints() {
    var fromDateBox = dijit.byId("beginDate");
    var toDateBox = dijit.byId("endDate");

    fromDateBox.set('constraints', { max: toDateBox.value });
    toDateBox.set('constraints', { min: fromDateBox.value });
}

