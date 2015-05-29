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
                dojo.byId('plan').innerHTML = dojoxDecode(data.plan);
                hideShowElement("load_draft", true);
                hideShowElement("load_draft_text", true);
            }
        },
        error: function (err, ioArgs) {
            console.log("error");
        }
    });
}