function getJiraPlans() {
    var employeeId = dojo.byId("employeeId").value;

    if (employeeId) {
        var jiraCell = dojo.byId("plan").parentNode;
        var standbyElementJira = new dojox.widget.Standby({target: jiraCell, zIndex: 1000});
        var reportDate = dijit.byId('calDate').get('value').format("yyyy-mm-dd");
        jiraCell.appendChild(standbyElementJira.domNode);
        standbyElementJira.startup();
        standbyElementJira.show();
        dojo.xhrGet({
            url: getContextPath() + "/timesheet/jiraIssuesPlanned",
            handleAs: "text",
            timeout: 30000,
            content: {employeeId: employeeId, date: reportDate},
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
            error: function (err, args) {
                if (args.xhr.status == 901)
                    window.location.href = getContextPath() + "/login";
                else {
                    dojo.byId("description_id_" + rowIndex).value = "Ошибка при поиске активности в JIRA(" + err + ")";
                    standbyElementJira.hide();
                }
            }
        });
    }
}

function createJiraCell(row, rowIndex) {
    var jiraCell = row.insertCell(10);
    dojo.addClass(jiraCell, "text_center_align");
    var jiraImg = dojo.doc.createElement("img");
    dojo.addClass(jiraImg, "pointer");
    dojo.attr(jiraImg, {
        id: "jira_button_" + rowIndex,
        src: getJiraLogo(),
        alt: "Запрос из JIRA",
        title: "Запрос из JIRA",
        //без px так как IE не понимает
        height: "15",
        width: "15",
        style: getJiraLogoStyle()
    });
    dojo.attr(jiraImg, "class", "controlToHide");

    //неведома ошибка исправляется для IE добавлением onclick именно через функцию
    jiraImg.onclick = function () {
        getJiraInfo(rowIndex);
    };
    jiraCell.appendChild(jiraImg);
}

function getJiraLogo() {
    return "resources/img/logo-jira.png";
}

function getJiraLogoDisabled() {
    return "resources/img/logo-jira-disabled.png";
}

function getJiraLogoStyle() {
    return "cursor:pointer;";
}

function updateJiraButtonVisibility() {
    var timesheetRowsCount = dojo.query(".time_sheet_row").length;
    for (var i = 0; i < timesheetRowsCount; i++) {
        if (dojo.byId("project_id_" + i).value == undefined || dojo.byId("project_id_" + i).value == 0) {
            dojo.byId("jira_button_" + i).src = getJiraLogoDisabled();
            dojo.removeAttr("jira_button_" + i, "style");
        } else {
            dojo.byId("jira_button_" + i).src = getJiraLogo();
            dojo.setAttr("jira_button_" + i, "style", getJiraLogoStyle());
        }
    }
}