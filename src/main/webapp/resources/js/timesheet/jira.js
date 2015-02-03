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
            error: function (err) {
                dojo.byId("description_id_" + rowIndex).value = "Ошибка при поиске активности в JIRA(" + err + ")";
                standbyElementJira.hide();
            }
        });
    }
}
