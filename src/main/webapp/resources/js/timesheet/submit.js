function submitform(sendType) {
    if (typeof(root.onbeforeunload) != "undefined") {
        root.onbeforeunload = null;
    }

    if ((sendType == 'send' && confirmSendReport()) || sendType == 'send_draft') {
        var division = dojo.byId('divisionId');
        var employee = dojo.byId('employeeId');
        var rowsCount = dojo.query(".time_sheet_row").length;
        var projectId;
        var projectComponent;
        var workPlaceId;
        var workPlaceComponent;
        var diffProjects = false;
        var diffWorkPlaces = false;
        for (var i = 0; i < rowsCount; i++) {
            projectComponent = dojo.query("#project_id_" + i);
            if (!diffProjects && projectComponent.length > 0)
                if (projectComponent[0].value) {
                    if (projectId && (projectId != projectComponent[0].value)) {
                        if (projectComponent[0].value != 0)
                            diffProjects = true;
                    }
                    else
                        projectId = projectComponent[0].value;
                }

            workPlaceComponent = dojo.query("#workplace_id_" + i)
            if (!diffWorkPlaces && workPlaceComponent.length > 0)
                if (workPlaceComponent[0].value) {
                    if (workPlaceId && (workPlaceId != workPlaceComponent[0].value)) {
                        if (workPlaceComponent[0].value != 0)
                            diffWorkPlaces = true;
                    }
                    else
                        workPlaceId = workPlaceComponent[0].value;
                }
        }
        setCookie('aplanaDivision', division.value, TimeAfter(7, 0, 0));
        setCookie('aplanaEmployee', employee.value, TimeAfter(7, 0, 0));
        setCookie('aplanaRowsCount', rowsCount, TimeAfter(7, 0, 0));
        if (diffProjects)
            deleteCookie("aplanaProject");
        else
            setCookie('aplanaProject', projectId, TimeAfter(7, 0, 0));
        if (diffWorkPlaces)
            deleteCookie("aplanaWorkPlace");
        else
            setCookie('aplanaWorkPlace', workPlaceId, TimeAfter(7, 0, 0));
        if (sendType == 'send') {
            timeSheetForm.action = "timesheet";
        } else if (sendType == 'send_draft') {
            timeSheetForm.action = "sendDraft";
        }

        processing();
        // disabled не включается в submit. поэтому снимем аттрибут.
        dojo.removeAttr("divisionId", "disabled");
        dojo.removeAttr("employeeId", "disabled");
        timeSheetForm.submit();

    }
    else if (sendType == 'newReport' && confirmCreateNewReport()) {
        timeSheetForm.action = "newReport";
        timeSheetForm.submit();
    }
}

function submitWithOvertimeCauseSet() {
    var comment = dijit.byId("overtimeCauseComment").get("value");
    var required = dijit.byId("overtimeCauseComment").get("required");

    if (comment == "" && required == true) {
        tooltip.show("Комментарий для причины 'Другое' является обязательным!");
        return;
    }

    if (required == true && comment.length > 256) {
        tooltip.show("Комментарий к причинам недоработок/переработок/работы в выходной день должен быть не более 256 символов");
        return;
    }

    var overtimeCause = dijit.byId("overtimeCause").get("value");
    var typeOfCompensationRequired = dijit.byId("typeOfCompensation").get("required");
    var typeOfCompensation = dijit.byId("typeOfCompensation").get("value");

    if (overtimeCause == 0) {
        tooltip.show("Необходимо указать причину работы в праздничный/выходной день!");
        return;
    }

    if (typeOfCompensation == 0 && typeOfCompensationRequired == true) {
        tooltip.show("Необходимо указать тип компенсации!");
        return;
    }

    dojo.byId("overtimeCauseComment_hidden").value = dijit.byId("overtimeCauseComment").get('value');
    dojo.byId("overtimeCause_hidden").value = overtimeCause;
    dojo.byId("typeOfCompensation_hidden").value = dijit.byId("typeOfCompensation").get('value');


    dijit.byId('dialogOne').hide();
    submitform('send');
}

function checkDurationThenSendForm() {
    var totalDuration = recalculateDuration();
    var isHoliday = false;
    var isVacation = false;
    var isDivisionLeader = false;

    var formattedDate;
    var divisionId = dojo.byId('divisionId').value;
    var employeeId = dojo.byId('employeeId').value;
    var pickedDate = dijit.byId('calDate').get('value');

    if (typeof divisionId == typeof undefined || divisionId == null || divisionId == 0) {
        alert("Укажите подразделение, сотрудника и дату");
    } else if (typeof employeeId == typeof undefined || employeeId == null || employeeId == 0) {
        alert("Укажите сотрудника и дату");
    } else if (getFirstWorkDate(getEmployeeData()) > pickedDate) {
        alert("Нельзя отправить отчет за выбранную дату, так как сотрудник еще не был принят на работу");
    } else {
        if (pickedDate) {
            formattedDate = pickedDate.format("yyyy-mm-dd");
        }

        dojo.xhrGet({
            url: getContextPath() + "/calendar/isholiday",
            headers: {
                "If-Modified-Since": "Sat, 1 Jan 2000 00:00:00 GMT"
            },
            handleAs: "text",
            timeout: 1000,
            failOk: true,
            content: { date: formattedDate, employeeId: employeeId },
            sync: true,

            load: function (dataAsText, ioArgs) {
                var data;

                try {
                    data = dojo.fromJson(dataAsText);
                } catch (e) {
                }

                if (data) {
                    isHoliday = data.isHoliday;
                }
            }
        });

        dojo.xhrGet({
            url: getContextPath() + "/calendar/isvacationwithoutplanned",
            headers: {
                "If-Modified-Since": "Sat, 1 Jan 2000 00:00:00 GMT"
            },
            handleAs: "text",
            timeout: 1000,
            failOk: true,
            content: { date: formattedDate, employeeId: employeeId },
            sync: true,

            load: function (dataAsText, ioArgs) {
                var data;

                try {
                    data = dojo.fromJson(dataAsText);
                } catch (e) {
                }

                if (data) {
                    isVacation = data.isVacation
                }
            }
        });

        dojo.xhrGet({
            url: getContextPath() + "/employee/isDivisionLeader",
            headers: {
                "If-Modified-Since": "Sat, 1 Jan 2000 00:00:00 GMT"
            },
            handleAs: "text",
            timeout: 1000,
            failOk: true,
            content: { employeeId: employeeId },
            sync: true,

            load: function (dataAsText, ioArgs) {
                var data;
                try {
                    data = dojo.fromJson(dataAsText);
                } catch (e) {
                }

                if (data) {
                    isDivisionLeader = data.isDivisionLeader
                }
            }
        });

        if (isHoliday || isVacation) {
            dijit.byId("typeOfCompensation").attr("required", true);
        } else {
            dijit.byId("typeOfCompensation").attr("required", false);
        }

        /* не РЦК */
        var check = !isDivisionLeader &&
            /*недоработка */
            ( ( totalDuration < (8 - undertimeThreshold) ||
                /* переработка */
                totalDuration > (8 + overtimeThreshold)
                ) ||
                /* выходной и не РЦК */
                isHoliday ||
                /* работа в отпуск */
                isVacation
                );
        if (check) {
            var select_box = dijit.byId("overtimeCause");

            select_box.removeOption(select_box.getOptions());
            select_box.addOption({ value: 0, label: "<div style='visibility: hidden;'>some invisible text, don't remove me!</div>" });

            var evald_json = isHoliday || isVacation ? workOnHolidayCauseList : (totalDuration < 8 ? unfinishedDayCauseList : overtimeCauseList);

            for (var key in evald_json) {
                var row = evald_json[key];
                select_box.addOption({ value: row.id, label: row.value });
            }

            if (defaultOvertimeCause) {
                select_box.set('value', defaultOvertimeCause);
            }

            var holidayDisplays = isHoliday || isVacation ? "" : "none";

            dojo.byId("holidayWarning").style.display = dojo.byId("typeOfCompensationContainer").style.display = holidayDisplays;

            var dialog = dijit.byId("dialogOne");

            dialog.set("title", "Укажите причину " + (isHoliday || isVacation ? "работы в выходной день" : (totalDuration < 8 ? "недоработок" : "переработок")));
            dialog.show();
        } else {

            // При отправке без диалога о недоработках очищаем служебные поля
            dojo.byId("overtimeCauseComment_hidden").value = "";
            dojo.byId("overtimeCause_hidden").value = "";
            dojo.byId("typeOfCompensation_hidden").value = "";

            submitform('send');
        }
    }
}

