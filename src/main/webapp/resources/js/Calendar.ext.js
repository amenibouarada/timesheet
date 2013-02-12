const CALENDAR_EXT_PATH = "com.aplana.dijit.ext";

dojo.require("dijit.Calendar");
dojo.provide(CALENDAR_EXT_PATH);

var dateInfoHolder = [];

dojo.declare(CALENDAR_EXT_PATH + ".Calendar", dijit.Calendar, {
    constructor: function() {
        if (dateInfoHolder.length == 0) {
            colorDayWithReportFromThreeMonth(
                    new Date().getFullYear(), correctLength(new Date().getMonth() + 1), this.getEmployeeId()
            );
        }
    },

    getClassForDate:function (date) {
        var employeeId = this.getEmployeeId();
        var month = correctLength(date.getMonth() + 1);
        var year = date.getFullYear();
        var day = correctLength(date.getDate());

        var info = dateInfoHolder[year + "-" + month + ":" + employeeId];
        var dateInfo;
        if (info == null) {
            dateInfoHolder[year + "-" + month + ":" + employeeId] = {}; //Создаем пустой объект, чтобы показать, что за этот месяц запрос уже отправлен
            colorDayWithReportFromThreeMonth(year, month, employeeId)
        }
        info = dateInfoHolder[year + "-" + month  + ":" + employeeId];
        dateInfo = info[year + "-" + month + "-" + day];

        return this.getClassForDateInfo(dateInfo, date);
    }
});

dojo.declare(CALENDAR_EXT_PATH + ".SimpleCalendar", com.aplana.dijit.ext.Calendar, {
    getClassForDateInfo: function(dateInfo, date) {
        switch (dateInfo) {
            case "2":   //выходной или праздничный день
                return 'classDateRedText';
                break;

            default:
                return '';
                break;
        }
    }
});

function initCurrentDateInfo(employeeId) {
    var date = new Date();

    colorDayWithReportFromThreeMonth(date.getFullYear(), correctLength(date.getMonth() + 1), employeeId);
}

function colorDayWithReportFromThreeMonth(/* int */ year, /* int */ month, /* int */ employeeId) {
    loadCalendarColors(year, month, employeeId);
    var monthPrev =  parseInt(month, 10) - 1;
    var yearPrev = year;
    if (monthPrev <= 0){
        monthPrev = 12;
        yearPrev = parseInt(year, 10) - 1;
    }
    if (dateInfoHolder[yearPrev + "-" + correctLength(monthPrev) + ":" + employeeId] == null)
        loadCalendarColors(yearPrev, correctLength(monthPrev), employeeId);
    var monthNext =  parseInt(month, 10) + 1;
    var yearNext = year;
    if (monthNext > 12){
        monthNext = 1;
        yearNext = parseInt(year, 10) + 1;
    }
    if (dateInfoHolder[yearNext + "-" + correctLength(monthNext) + ":" + employeeId] == null)
        loadCalendarColors(yearNext, correctLength(monthNext), employeeId);
}

//загружает список дней с раскраской календаря за месяц
function loadCalendarColors(/* int */ year, /* int */ month, /* int */ employeeId) {
    dojo.xhrGet({
        url: getContextPath() + "/calendar/dates",
        headers: {
            "If-Modified-Since":"Sat, 1 Jan 2000 00:00:00 GMT"
        },
        handleAs:"json",
        timeout:1000,
        content:{queryYear:year, queryMonth:month, employeeId:employeeId},
        sync: (dateInfoHolder.length == 0),
        load:function (data, ioArgs) {
            if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
                dateInfoHolder[ioArgs.args.content.queryYear + "-" + ioArgs.args.content.queryMonth  + ":" + ioArgs.args.content.employeeId] = data;
            }
        },
        error:function (err, ioArgs) {
            if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                // Если ошибка - не будем ничего рисовать. При следующем запросе на отрисовку - снова будет сделана попытка получения данных за этот месяц
                dateInfoHolder[ioArgs.args.content.queryYear + "-" + ioArgs.args.content.queryMonth  + ":" + ioArgs.args.content.employeeId] = null;
            }
        }
    });
}

function correctLength(dayOrMonth){
    if (dayOrMonth < 10)
        return '0' + dayOrMonth;
    return dayOrMonth;
}