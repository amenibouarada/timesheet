function dateDiffInDay(begin, end){
    return (Date.parse(end) - Date.parse(begin)) / 86400000; // разница в датах в днях
}

// ковертирует дату в тип Date из строки (разделитель ".")
function getDateByString(dateString){
    var date = new Date();
    date.setHours(0, 0, 0, 0);
    if (dateString == null){ return date;}
    if (dateString instanceof Date){ return dateString; }
    if (dateString == ""){ return date;}
    var dvArr = dateString.split('.');
    date.setFullYear(dvArr[2], dvArr[1] - 1, dvArr[0]);
    return date;
}

function getFirstDayOfMonth(year, month) {
    var date = new Date(year, month + 1, 1);
    return date.getDate();
}

function getLastDayOfMonth(year, month) {
    var date = new Date(year, month + 1, 0);
    return date.getDate();
}

// GET NUMBER OF DAYS IN MONTH
function getDaysInMonth(month, year)
{
    var days;
    switch(month)
    {
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            days = 31;
            break;
        case 4:
        case 6:
        case 9:
        case 11:
            days = 30;
            break;
        case 2:
            if (((year% 4)==0) && ((year% 100)!=0) || ((year% 400)==0))
                days = 29;
            else
                days = 28;
            break;
    }
    return (days);
}

// Возвращает название месяца по номеру
function getMonthByNumber(number) {
    var month = ["январь", "февраль", "март", "апрель", "май", "июнь", "июль",
        "август", "сентябрь", "октябрь", "ноябрь", "декабрь"];
    return month[number - 1];
}

function getWeek(date){ // возвращает номер недели
    var tmp = new Date(date.getFullYear(),0,1);
    return Math.ceil(((dateDiffInDay(tmp, date)) + tmp.getDay() + 1)/7);
}

function addWeek(date){
    var tmpDate = new Date(date);
    tmpDate.setDate(tmpDate.getDate() + 7);
    return tmpDate;
}
function monthCount(yearStart, monthStart, yearEnd, monthEnd) {
    var cnt = 0;
    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function () {
        ++cnt
    });
    return cnt;
}

// Бежит по циклу между двумя месяцами и для каждого месяца дергает функцию handler(month, year)
function iterateMonth(yearStart, monthStart, yearEnd, monthEnd, handler) {
    for (var month = monthStart, year = yearStart; (month <= monthEnd && year == yearEnd) || year < yearEnd;) {
        handler(month, year);
        ++month;
        if (month == 13) {
            month = 1;
            ++year;
        }
    }
}

/* Выдает время через d дней h часов m минут */
function TimeAfter(d, h, m) {
    var now = new Date(), // объект класса Data
        nowMS = now.getTime(), // в миллисекундах (мс)
        newMS = ((d * 24 + h) * 60 + m) * 60 * 1000 + nowMS;
    now.setTime(newMS);    // новое время в мс
    return now.toGMTString();
}

/*
 * Превращает timestamp строку (yyyy-mm-dd) в строку для
 * displayValue DateTextBoxА (dd.mm.yyyy)
 */
function timestampStrToDisplayStr(str) {
    if (str != "") {
        var splittedStr = str.split("-");
        return splittedStr[2] + "." + splittedStr[1] + "." + splittedStr[0];
    } else {
        return str;
    }
}





