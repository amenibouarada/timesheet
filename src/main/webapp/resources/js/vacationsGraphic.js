function formatDDMMYYYY(inputDate){
    var currentDay = inputDate.getDate();
    var currentMonth =  1 + inputDate.getMonth();
    var currentYear = inputDate.getFullYear();
    var formatDate = currentDay + '.' + currentMonth + '.' + currentYear;
    return formatDate;
}


function RegionEmployees(regionName, regionEmployees, holidayList){
    var region = regionName;
    var employees = new Array();
    var holiday=holidayList;
    for (var i in regionEmployees){
        var employeeName = regionEmployees[i].employee;
        var employeeVacations = regionEmployees[i].vacations;
        var vacations = new EmployeeVacations(employeeName, employeeVacations);
        employees.push(vacations);
    }

    this.getRegion   = function(){ return region};
    this.getEmployees = function(){ return employees};
    this.getHoliday = function() {return holiday};
}

function EmployeeVacations(employeeName, employeeVacations){
    var employee = employeeName;
    var vacations = new Array();
    for (var i in employeeVacations){

        var from = new Date();
        var to = new Date();
        try{
            var dvArr = employeeVacations[i].beginDate.split('.');
            from.setFullYear(dvArr[2], dvArr[1] - 1, dvArr[0]);
            dvArr = employeeVacations[i].endDate.split('.');
            to.setFullYear(dvArr[2], dvArr[1] - 1, dvArr[0]);

            employeeVacations[i].beginDate = from;
            employeeVacations[i].endDate = to;
        }catch(exception){   }

        vacations.push(employeeVacations[i]);
    }

    this.getEmployee  = function(){ return employee};
    this.getVacations = function(){ return vacations};
}

function Gantt(gDiv, holidayList, type)
{
    var GanttDiv = gDiv;
    var viewType = type;
    var tableRowHeight = 20;
    var TABLE_COLUMN_WIDTH = 0;
    var holidays = new Array();
    var date = new Date();
    for (var i in holidayList) {
        var dvArr = holidayList[i].split('.');
        date.setFullYear(dvArr[2], dvArr[1] - 1, dvArr[0]);
        holidays.push(date.toLocaleDateString());
    }
    var regionEmployeeList = new Array();
    this.AddRegionEmployeeList = function (value) {
        regionEmployeeList.push(value);
    }
    if (viewType == VIEW_GRAPHIC_BY_WEEK) {
        TABLE_COLUMN_WIDTH = 10;
    } else {
        TABLE_COLUMN_WIDTH = 15;
    }

    /**
     * функция отрисовки отпусков
     * @constructor
     */
    this.DrawVacations = function () {
        var dateDiff = 0;
        var gStr = "";

        if (regionEmployeeList.length <= 0) {
            return
        }

        var vacations = createFullVacationsList(regionEmployeeList);
        var minDate = findMinDate(vacations, viewType);
        var dayOffset = getAlignOffset(minDate);
        var td = document.getElementsByClassName('GDay')[1];
        var num = td.clientWidth - TABLE_COLUMN_WIDTH + 1;
        console.log(td.clientWidth);
        if (viewType == VIEW_GRAPHIC_BY_WEEK) {
            num = 0;
        }

        var employeeCount = 0;
        for (var i in regionEmployeeList) {
            var employeeList = sortEmployeeList(regionEmployeeList[i].getEmployees());
            var indent = parseInt(i) + parseInt(employeeCount);
            gStr += "<div style='position:absolute; top:" + (tableRowHeight * indent + 38) + "px; left:5px'><b>" + regionEmployeeList[i].getRegion() + "</b></div>";
            employeeCount += employeeList.length;
            for (var hol in regionEmployeeList[i].getHoliday()) {
                var fr = new Date();
                var dvArr = regionEmployeeList[i].getHoliday()[hol].split('.');
                fr.setFullYear(dvArr[2], dvArr[1] - 1, dvArr[0]);

                if (Date.parse(fr.format("yyyy-mm-dd")) < Date.parse(minDate.format("yyyy-mm-dd"))){
                    continue;
                }

                var leftOffset = ((dayOffset + (Date.parse(fr.format("yyyy-mm-dd")) - Date.parse(minDate.format("yyyy-mm-dd"))) / (24 * 60 * 60 * 1000))) * TABLE_COLUMN_WIDTH + 202 + num;
                var heightDivision = (employeeList.length + 1) * 20 + 8; //это надо править - считать широкие-узкие строки, чистый тык пальцем
                gStr += "<div style='position:absolute; top:" + (tableRowHeight * indent + 38 - 8) + "px; left:" + leftOffset + "px; width:" + (TABLE_COLUMN_WIDTH - 1) + "px'><div title='Региональный праздник' class='GVacation' style='float:center;padding-left:3;background-color:sandybrown; width:" + (TABLE_COLUMN_WIDTH - 1) + "px;height:" + heightDivision + "px;'></div></div>";
            }
            for (var j in employeeList) {
                var vacationList = sortVacationsByType(employeeList[j].getVacations());
                indent += 1;
                for (var k in vacationList) {
                    var color = getColorByType(vacationList[k].type);
                    var tooltip = employeeList[j].getEmployee() + "\n" + vacationList[k].typeName + "\n" + formatDDMMYYYY(vacationList[k].beginDate) + " - " + formatDDMMYYYY(vacationList[k].endDate) + "\n" + vacationList[k].status;
                    var offSet = dayOffset + (Date.parse(vacationList[k].beginDate) - Date.parse(minDate)) / (24 * 60 * 60 * 1000);
                    if ((Date.parse(vacationList[k].beginDate) - Date.parse(minDate)) <= 0 && (viewType == VIEW_GRAPHIC_BY_DAY)) {
                        dateDiff = (Date.parse(vacationList[k].endDate) - Date.parse(vacationList[k].beginDate)) / (24 * 60 * 60 * 1000) + 1;
                        gStr += "<div style='position:absolute; top:" + (tableRowHeight * indent + 37) + "px; left:" + (offSet * TABLE_COLUMN_WIDTH + 202 ) + "px; width:" + (TABLE_COLUMN_WIDTH * dateDiff - 1 + num) + "px'><div title='" + tooltip + "' class='GVacation' style='float:center;padding-left:3;" + "background-color:" + color + "; width:" + (TABLE_COLUMN_WIDTH * dateDiff - 1 + num) + "px;'>" + "</div></div>"; //+ vacationList[k].status
                    } else {
                        dateDiff = (Date.parse(vacationList[k].endDate) - Date.parse(vacationList[k].beginDate)) / (24 * 60 * 60 * 1000) + 1;
                        gStr += "<div style='position:absolute; top:" + (tableRowHeight * indent + 37) + "px; left:" + (offSet * TABLE_COLUMN_WIDTH + 202 + num) + "px; width:" + (TABLE_COLUMN_WIDTH * dateDiff - 1) + "px'><div title='" + tooltip + "' class='GVacation' style='float:center;padding-left:3;" + "background-color:" + color + "; width:" + (TABLE_COLUMN_WIDTH * dateDiff - 1) + "px;'>" + "</div></div>"; //+ vacationList[k].status
                    }
                }
                gStr += "<div style='position:absolute; top:" + (tableRowHeight * indent + 38) + "px; left:5px'>" + employeeList[j].getEmployee() + "</div>";
            }
        }
        GanttDiv.innerHTML += gStr;
    }

    /**
     * функция отрисовки пустой таблицы графика отпусков
     * @constructor
     */
    this.DrawTable = function () {

        if (regionEmployeeList.length <= 0) {
            return
        }

        var vacations = createFullVacationsList(regionEmployeeList);
        var maxDate = findMaxDate(vacations, viewType);
        var minDate = findMinDate(vacations, viewType);

        var tableRowCount = getTableRowCount(regionEmployeeList);
        GanttDiv.innerHTML = fillTableHeader(viewType, minDate, maxDate, holidays, tableRowCount, tableRowHeight, TABLE_COLUMN_WIDTH);
    }
}

function sortEmployeeList(employeeList){
    for (var i = 0; i < employeeList.length; i++){
        for (var j = i + 1; j < employeeList.length; j++){
            if (employeeList[i].getEmployee() > employeeList[j].getEmployee()){
                var tmp = employeeList[i];
                employeeList[i] = employeeList[j];
                employeeList[j] = tmp;
            }
        }
    }
    return employeeList;
}

function sortVacationsByType(vacations){
    var arrayWithPlanVacations = new Array();
    var arrayWithRealVacations = new Array();
    for (var i in vacations){
        if (vacations[i].type == VACATION_PLANNED) {// планируемый отпуск
            arrayWithPlanVacations.push(vacations[i]);
        }else{
            arrayWithRealVacations.push(vacations[i]);
        }
    }
    arrayWithPlanVacations = sortVacationsByDate(arrayWithPlanVacations);
    arrayWithRealVacations = sortVacationsByDate(arrayWithRealVacations);
    return arrayWithPlanVacations.concat(arrayWithRealVacations);
}

function sortVacationsByDate(vacations){
    for (var i = 0; i < vacations.length; i++){
        for (var j = i + 1; j < vacations.length; j++){
            if (vacations[i].beginDate > vacations[j].beginDate){
                var tmp = vacations[i];
                vacations[i] = vacations[j];
                vacations[j] = tmp;
            }
        }
    }
    return vacations;
}

function getColorByType(type){
    switch (type){
        case VACATION_WITH_PAY : return "green";    // отпуск с сохранением содержания
        case VACATION_WITHOUT_PAY : return "green"; // отпуск без сохранения содержания
        case VACATION_WITH_WORK : return "green";   // отпуск с последующей отработкой
        case VACATION_PLANNED : return "yellow";         // планируемый отпуск
    }
}

function createFullVacationsList(regionEmployeeList){
    var vacations = new Array();
    for (var i in regionEmployeeList){
        var employeeList = regionEmployeeList[i].getEmployees();
        for (var j in employeeList){
            var employeeVacations = employeeList[j].getVacations();
            for (var k in employeeVacations){
                vacations.push(employeeVacations[k]);
            }
        }
    }
    return vacations;
}

function getTableRowCount(regionEmployeeList){
    var result = regionEmployeeList.length;
    for (var i in regionEmployeeList){
        result += regionEmployeeList[i].getEmployees().length;
    }
    return result;
}

function findMaxDate(vacations, viewType){
    var maxDate = new Date();
    maxDate.setFullYear(vacations[0].endDate.getFullYear(),
                        vacations[0].endDate.getMonth(),
                        vacations[0].endDate.getDate());
    // найдем максимальную дату
    for (var i in vacations){
        if(Date.parse(vacations[i].endDate) > Date.parse(maxDate)){
            maxDate.setFullYear(vacations[i].endDate.getFullYear(), vacations[i].endDate.getMonth(), vacations[i].endDate.getDate());
        }
    }
    //---- Добавим несколько дней к maxDate для лучшего отображения -----
    var daysToAdd = 7;
    maxDate.setDate(maxDate.getDate() + daysToAdd);

    if (viewType == VIEW_GRAPHIC_BY_WEEK){
        maxDate.setDate(maxDate.getDate() + daysToAdd);
        while(maxDate.getDay() != 0){
            maxDate.setDate(maxDate.getDate() + 1);
        }
    }
    return maxDate;
}
function findMinDate(vacations, viewType){
    var minDate = new Date();
    minDate.setFullYear(vacations[0].beginDate.getFullYear(),
                        vacations[0].beginDate.getMonth(),
                        vacations[0].beginDate.getDate());
    // найдем минимальную дату
    for (var i in vacations){
        if(Date.parse(vacations[i].beginDate) < Date.parse(minDate)){
            minDate.setFullYear(vacations[i].beginDate.getFullYear(), vacations[i].beginDate.getMonth(), vacations[i].beginDate.getDate());
        }
    }
    if (viewType == VIEW_GRAPHIC_BY_WEEK){
        //minDate.setDate(minDate.getDate() - 14);
        while (minDate.getDay() != 1){ // пока не понедельник
            minDate.setDate(minDate.getDate() - 1);
        }
    }
    return minDate;
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

function getMonthByNumber(number){
    var month;
    switch(number)
    {
        case 1:  month = 'январь'; break;
        case 2:  month = 'февраль'; break;
        case 3:  month = 'март'; break;
        case 4:  month = 'апрель'; break;
        case 5:  month = 'май'; break;
        case 6:  month = 'июнь'; break;
        case 7:  month = 'июль'; break;
        case 8:  month = 'август'; break;
        case 9:  month = 'сентябрь'; break;
        case 10: month = 'октябрь'; break;
        case 11: month = 'ноябрь'; break;
        case 12: month = 'декабрь'; break;
    }
    return (month);
}

function getWeek(date){
    var tmp = new Date(date.getFullYear(),0,1);
    return Math.ceil((((date - tmp) / 86400000) + tmp.getDay()+1)/7);
}

function addWeek(date){
    var tmpDate = new Date(date);
    tmpDate.setDate(tmpDate.getDate() + 7);
    return tmpDate;
}

function makeMonthDivTitle(date){
    var firstDay = new Date(date);
    var lastDay  = new Date(date);
    var month;
    while (firstDay.getDay() != 1){ // пока не понедельник
        firstDay.setDate(firstDay.getDate() - 1);
    }
    while (lastDay.getDay() != 0){ // пока не воскресенье
        lastDay.setDate(lastDay.getDate() + 1);
    }
    return firstDay.getDate() + " " + getMonthByNumber(firstDay.getMonth() + 1) + " - " +
        lastDay.getDate() + " " + getMonthByNumber(lastDay.getMonth() + 1);
}

function getAlignOffset(minDate){
    var offset = 0;
    // magic const
    if (minDate.getDate() > 26) {
        offset = getDaysInMonth(minDate.getMonth() + 1, minDate.getYear()) - 26;
    }
    return offset;
}

function fillTableHeader(viewType, minDate, maxDate, holidays, tableRowCount, tableRowHeight, tableColumnWidth){
    var offset = getAlignOffset(minDate);

    var currentDate = new Date();
    currentDate.setFullYear(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
    var dTemp = new Date();
    var firstRowStr;
    var thirdRow;
    var gStr = "";
    var tableHeight = tableRowCount * tableRowHeight + 10;

    // разлинуем в разный цвет четные и нечетные строки
    var dateDiff = (Date.parse(maxDate) - Date.parse(minDate)) / (24 * 60 * 60 * 1000) + 1;
    var width = (tableColumnWidth * (dateDiff + offset) + 200);
    var top = 38;
    for (var i = 0; i < tableRowCount; i++ ){
        if (i % 2 == 0){
            gStr += "<div style='position:absolute; top:" + top + "px; left:1px; width:" + width + "px; height: 16px; background-color:rgb(240,240,240);'></div>";
        }
        top += tableRowHeight;
    }

    /*
        чтобы дивки разлиновки отображались под таблицей (а не перекрывали таблицу) - таблице был задан параметр абсолютного позиционирования
        соответсвтенно дивка содержащая график нерастягивается, относительно размеров таблице
        поэтому была создана невидимая таблица с relative позиционированием, для того, чтобы расятнуть дивку вкладки
        шапка + высота нижней ячейки
    */
    firstRowStr = "<table style='visibility: false;height:" + (tableHeight + 50) + "px;'>";
    firstRowStr += "<table id='tableGraphic' border=1 style='border-collapse:collapse;position: absolute; left:0px; top: 0px'><tr><td rowspan='2' width='200px' style='width:200px;'><div class='GVacationTitle' style='width:200px;'>Сотрудник</div></td>";
    firstRowStr = gStr + firstRowStr; // сперва поместим дивки, потом таблицу

    gStr += "</tr><tr>";
    thirdRow = "<tr><td>&nbsp;</td>";
    dTemp.setFullYear(minDate.getFullYear(), minDate.getMonth(), minDate.getDate());


    var colSpan = offset;
    var counter = 0;

    if (viewType == VIEW_GRAPHIC_BY_DAY) {

        if (offset>0){
            var totalOffset = offset * tableColumnWidth - 1;
            gStr += "<td class='GDay' colspan='"+offset+"' width='" + totalOffset + "px'></td>";
            thirdRow += "<td class='GDay' colspan='"+offset+"' style='height:" + (tableHeight) + "px'>&nbsp;</td>";
        }

        while(Date.parse(dTemp) <= Date.parse(maxDate)){
            var cssClass = "GDay";
            if (Date.parse(dTemp) == Date.parse(currentDate)){
                cssClass = "GToday";
            }else if(holidays.indexOf(dTemp.toLocaleDateString()) >= 0){ //Weekend
                cssClass = "GWeekend";
            }
            gStr += "<td class='" + cssClass + "'><div style='width:" + (tableColumnWidth - 1) + "px;'>" + dTemp.getDate() + "</div></td>";
            thirdRow += "<td id='GC_" + (counter++) + "' class='" + cssClass + "' style='height:" + (tableHeight) + "px'>&nbsp;</td>";
            if(dTemp.getDate() < getDaysInMonth(dTemp.getMonth() + 1, dTemp.getFullYear())){
                colSpan++;
            }else{
                firstRowStr += "<td class='GMonthDayGraph' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
                colSpan = 0;
            }
            dTemp.setDate(dTemp.getDate() + 1);
        }
        firstRowStr += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
    }else{ // BY_WEEK
        while(Date.parse(dTemp) <= Date.parse(maxDate)){
            gStr += "<td class='GDay' title='" + makeMonthDivTitle(dTemp) + "'><div style='width:" + ((tableColumnWidth - 1)*7 + 6) + "px;'>" + getWeek(dTemp) + "</div></td>";
            thirdRow += "<td id='GC_" + (counter++) + "' class='GDay' style='height:" + (tableHeight) + "px'>&nbsp;</td>";
            dTemp = addWeek(dTemp);
            colSpan++;
        }
        firstRowStr += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>&nbsp;</td>";

        // месяцы поместим отдельной дивкой, т.к. параллели строк месяцев и недель не совпадают
        colSpan = 0;
        dTemp.setFullYear(minDate.getFullYear(), minDate.getMonth(), minDate.getDate());
        var strMonthDiv = "<tr>";
        var strMonthDivBegin = "<div style='position: absolute;left: 201px; top: 0px; height: 5px'><table border=1><tr>";
        while(Date.parse(dTemp) <= Date.parse(maxDate)){
            strMonthDiv += "<td class='GDay'><div style='width:" + (tableColumnWidth - 1) + "px;'></div></td>";
            if(dTemp.getDate() < getDaysInMonth(dTemp.getMonth() + 1, dTemp.getFullYear())){
                colSpan++;
            }else{
                strMonthDivBegin += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
                colSpan = 0;
            }
            dTemp.setDate(dTemp.getDate() + 1);
        }
        strMonthDivBegin += "<td title='" + makeMonthDivTitle(dTemp) + "' class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
        strMonthDivBegin += "</tr>";
        strMonthDiv += "</tr></table></div>";
        firstRowStr = strMonthDivBegin + strMonthDiv + firstRowStr;
    }


    thirdRow += "</tr>";
    gStr += "</tr>" + thirdRow;
    gStr += "</table>";
    gStr = firstRowStr + gStr;

    return gStr;
}