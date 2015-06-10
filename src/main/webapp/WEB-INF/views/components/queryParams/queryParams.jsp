<%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <table>
        <tr>
            <td>
                <span class="label">Год:</span>
            </td>
            <td>
                <select id="monthreport_year">
                    <option value="2015" label="2015"/>
                    <option value="2016" label="2016"/>
                    <option value="2017" label="2017"/>
                    <option value="2018" label="2018"/>
                    <option value="2019" label="2019"/>
                    <option value="2020" label="2020"/>
                </select>
            </td>

            <td>
                <span class="label">Месяц:</span>
            </td>
            <td>
                <%--// ToDo сделать отдельный файл для формирования выпадашки с месяцами--%>
                <select id="monthreport_month">
                    <option value="1" title="Январь">Январь</option>
                    <option value="2" title="Февраль">Февраль</option>
                    <option value="3" title="Март">Март</option>
                    <option value="4" title="Апрель">Апрель</option>
                    <option value="5" title="Май">Май</option>
                    <option value="6" title="Июнь">Июнь</option>
                    <option value="7" title="Июль">Июль</option>
                    <option value="8" title="Август">Август</option>
                    <option value="9" title="Сентябрь">Сентябрь</option>
                    <option value="10" title="Октябрь">Октябрь</option>
                    <option value="11" title="Ноябрь">Ноябрь</option>
                    <option value="12" title="Декабрь">Декабрь</option>
                </select>
        </tr>
    </table>


