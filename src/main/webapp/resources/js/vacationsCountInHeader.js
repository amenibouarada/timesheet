

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