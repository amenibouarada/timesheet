

function getVacationsNeedsApprovalCountString(){
    dojo.xhrGet({
        url: getContextPath() + "/vacations/count",
        timeout:10000,
        load:function (data) {
            var component = dojo.byId("vacationCount");
            if (component) {
                if (data) {
                    dojo.byId("vacationCount").innerHTML = data;
                } else {
                    dojo.byId("vacationCount").innerHTML = "";
                }
            }
        }
    });
}

function getReportOverdueEmployeesNames() {
    dojo.xhrGet({
        url: getContextPath() + "/timesheet/reportOverdue",
        timeout:10000,
        load:function (data) {
            var component = dojo.byId("headerMarqueeContent");
            if (component) {
                if (data) {
                    dojo.byId("headerMarqueeContent").innerHTML = data;
                } else {
                    dojo.byId("headerMarqueeContent").innerHTML = "";
                }
            }
        }
    });
}