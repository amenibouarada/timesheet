<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="feedback"/></title>
    <script type="text/javascript">

        dojo.ready(function () {
            window.focus();
        });

		var hasFile1 = false;
		var hasFile2 = false;

		/**
		 * проверяет чтобы суммарный размер файлов не превышал 8 Mb
		 */
		function checkFileSize() {
            if( window.FormData === undefined ){
                return false;   // Такой возврат не я придумал, функция так странно возвращала до меня.
            }
			var file1 = feedbackForm.file1Path.files[0];
			var file2 = feedbackForm.file2Path.files[0];
			var size1;
			var size2;

			if (file1 != null) {
				size1 = file1.size;
			} else {
				size1 = 0;
			}

			if (file2 != null) {
				size2 = file2.size;
			} else {
				size2 = 0;
			}
			var totalSize = size1 + size2;
			return totalSize > 8388608;
		}		

        //проверяем и отсылаем форму
        function submitform() {
            var description = dojo.byId('feedbackDescription');

			if (checkFileSize()) {
				alert("Суммарный размер вложений превышает 8 Mb");
                enableInput("send_button");
				return;
			}
            if (description != null && description.value != "") {
                feedbackForm.action = "feedback";
                feedbackForm.submit();
            } else {
                alert("Поле 'Текст сообщения' не определено.");
                enableInput("send_button");
            }
        }

        //очищаем форму
        function clearForm(obj) {
            if (confirmClearWindow()) {
                document.forms.mainForm.reset();
                feedbackForm.action = "newFeedbackMessage";
                feedbackForm.submit();
            }
        }
		
		/**
		 * Отображает второй набор контролов и кнопку удаления первого файла
		 */
		function showControls() {
			showAdditionalInput('file2PathContainer');
			showAdditionalInput('fileDelete2');
			disableInput('fileDelete2');
			enableInput('fileDelete1');
		}
		
		/**
		 * Удаляет первый файл и скрывает второй набор контролов, если он пустой
		 */
		function hideControlsAndDeleteFile() {
			deleteFile('file1PathContainer');
			disableInput('fileDelete1')
		}
		
		/**
		 * Удаляет файл из контрола
		 */
		function deleteFile(controlName) {
			document.getElementById(controlName).innerHTML = document.getElementById(controlName).innerHTML;
		}

        //Показать скрытый input
        function showAdditionalInput(id) {
            document.getElementById(id).style.display = '';
        }
		
		//Cкрыть контрол
        function hideInput(id) {
            document.getElementById(id).style.display = 'none';
        }
		//Включить контрол
        function enableInput(id) {
            document.getElementById(id).disabled = false;
        }
		//Выключить контрол
        function disableInput(id) {
            document.getElementById(id).disabled = true;
        }
		
		function deleteFileIn2Position() {
			disableInput('fileDelete2');
			deleteFile('file2PathContainer');
		}
    </script>

</head>
<body>
	<c:if test="${jiraIssueCreateUrl != null}">
		<h2><a target="_blank" href=${jiraIssueCreateUrl}>Перейти к созданию запроса в Jira</a></h2>
	</c:if>
    <h1><fmt:message key="feedback"/></h1>

<form:form method="post" commandName="feedbackForm" name="mainForm" enctype="multipart/form-data" cssClass="noborder">

    <div id="errorboxdiv" name="errorboxdiv" class="errorbox">
        <form:errors path="*" delimiter="<br/><br/>" />
    </div>

    <div id="form_table">
        <table id="time_sheet_table">
            <tr id="time_sheet_header">
                <th align="center" style="min-width: 200px">Тип сообщения</th>
                <th align="center" style="min-width: 400px">Текст сообщения</th>
                <th align="center" style="min-width: 270px">Вложения</th>
            </tr>
				
            <tr class="time_sheet_row" id="ts_row">
                <td width="38" class="top_align"> <!-- Тип проблемы -->
                    <form:select path="feedbackType" cssClass="activityType" cssStyle="width: 100%;" id="feedback_type"
                                 name="feedback_type" onchange="feedbackTypeChange(this);"
                                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                        <fmt:message key="feedback.type.newproposal" var="problemNewProposal"/>
                        <form:option value="1" title="${problemNewProposal}" label="${problemNewProposal}"/>
                        <fmt:message key="feedback.type.incorrectdata" var="problemIncorrectMessage"/>
                        <form:option value="2" title="${problemIncorrectMessage}" label="${problemIncorrectMessage}"/>
                        <fmt:message key="feedback.type.cantsendreport" var="problemCantReport"/>
                        <form:option value="3" title="${problemCantReport}" label="${problemCantReport}"/>
                        <fmt:message key="feedback.type.notfoundproject" var="problemNotFoundProject"/>
                        <form:option value="4" title="${problemNotFoundProject}" label="${problemNotFoundProject}"/>
                        <fmt:message key="feedback.type.deletevacation" var="problemDeleteVacation"/>
                        <form:option value="6" title="${problemDeleteVacation}" label="${problemDeleteVacation}"/>
                        <fmt:message key="feedback.type.other" var="problemOther"/>
                        <form:option value="5" title="${problemOther}" label="${problemOther}"/>
                    </form:select>
                </td>
                <td class="top_align"> <!-- Текст сообщения -->
                    <form:textarea path="feedbackDescription" id="feedbackDescription" name="feedbackDescription" rows="8"
                                   cssStyle="width: 100%;"></form:textarea>
                </td>
                <td class="top_align"><!-- Вложения -->
					<table style="border-style:none">
						<tr>
							<td style="border-style:none">
								<button id="fileDelete1" name="fileDelete1" type="button" onclick="hideControlsAndDeleteFile()" disabled="true">
									Удалить
								</button>
							</td>
							<td style="border-style:none">
								<div id="file1PathContainer">
									<input id="file1Path" name="file1Path" type="file" size="30" onchange="showControls()"/>
								</div>
							</td>
						</tr>
						<tr>
							<td style="border-style:none">
								<button style="display:none" id="fileDelete2" name="fileDelete2" type="button" onclick="deleteFileIn2Position()" disabled="true">
									Удалить
								</button>
							</td>
							<td style="border-style:none">
								<div id="file2PathContainer" style="display:none">
									<input id="file2Path" name="file2Path" type="file" size="30" onchange="enableInput('fileDelete2')" /><br/>
								</div>
							</td>
					</table>
                    <span>Суммарный размер вложений - не более 8МБ, не более 2 файлов.</span>
                </td>
					
            </tr>
        </table>
    </div>

    <div id="marg_buttons" style="margin-top:10px; margin-bottom:10px ">
        <button id="send_button" name="send_button" style="width:150px" type="button"
                onclick="disableInput(this.id);submitform()">Отправить
        </button>

        <button id="clear_button" name="clear_button" style="width:150px" type="button"
                onclick="clearForm(this)">Очистить
        </button>
    </div>
</form:form>
</body>
</html>