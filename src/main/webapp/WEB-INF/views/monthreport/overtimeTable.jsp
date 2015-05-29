<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<table data-dojo-type="dojox.grid.DataGrid" data-dojo-props="store:overtimeStore" style="height: 500px">
    <thead>
        <tr>

            <th field="id" width="20px">
                <img src="<c:url value='/resources/img/add.gif'/>" class="create_img" title="Создать" onclick="employeeDialog.show();"/>
            </th>
            <th field="employee"                width="150px"   >Сотрудник</th>
            <th field="division"                width="150px"   >Подразделение</th>
            <th field="region"                  width="100px"   >Регион</th>
            <th field="type"                    width="150px"   >Тип</th>
            <th field="prType"                  width="100px"   >Проект/Пресейл</th>
            <th field="overtime"                width="100px"   >Переработки</th>
            <th field="premium"                 width="100px"   >Премия</th>
            <th field="allAccountedOvertime"    width="100px"   >Всего учтенных переработок и премий</th>
            <th field="comment"                 width="200px"   >Комментарий</th>
        </tr>
    </thead>
</table>