package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.Project;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abayanov
 * Date: 16.07.13
 */
@Service
public class PlanEditExcelReportService {

    final private String PLAN = "План";
    final private String FACT = "Факт";

    private static final Logger logger = LoggerFactory.getLogger(PlanEditExcelReportService.class);

    @Transactional(readOnly = true)
    public void createAndExportReport(String fileName, String jsonData, List<Project> projectList, HttpServletResponse response, HttpServletRequest httpServletRequest) {

        HSSFWorkbook workbook = createAndFillReport(fileName, jsonData, projectList);

        try {

            String suffix = ".xls";
            String contentType = "application/vnd.ms-excel";
            response.setContentType(contentType);

            String agent = httpServletRequest.getHeader("user-agent");
            String contentDisposition = "attachment; filename=\"" + toUTF8String(fileName + suffix) + "\"";
            if (agent.contains("Firefox")) {
                contentDisposition = "attachment; filename=\"" + MimeUtility.encodeText(fileName + suffix, "UTF8", "B") + "\"";
            }

            response.setHeader("Content-Disposition", contentDisposition);

            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            error(e);
        } catch (Exception e) {
            error(e);
        }

    }

    private HSSFWorkbook createAndFillReport(String fileName, String jsonData, List<Project> projectList) {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = workBook.createSheet(fileName);
        sheet.autoSizeColumn((short) 2);

        createReportHeader(sheet, workBook, projectList);

        jsonData = updateJson(jsonData);

        fillReportFromJson(jsonData, projectList, workBook, sheet);

        return workBook;
    }

    private void createReportHeader(HSSFSheet sheet, HSSFWorkbook workBook, List<Project> projectList) {

        CellStyle style = getHeaderCellStyle(workBook);
        List<Row> rows = new ArrayList<Row>();

        Row row = sheet.createRow(0);
        rows.add(row);
        Cell cell = row.createCell(0);
        cell.setCellValue("Сотрудник");
        cell.setCellStyle(style);

        Row secondRow = sheet.createRow(1);
        rows.add(secondRow);
        Row thirdRow = sheet.createRow(2);
        rows.add(thirdRow);
        Row forthRow = sheet.createRow(3);
        cell = forthRow.createCell(0);
        cell.setCellStyle(style);
        cell = forthRow.createCell(1);
        cell.setCellStyle(style);

        cell = forthRow.createCell(2);
        cell.setCellStyle(style);

        rows.add(forthRow);


        CellRangeAddress region = new CellRangeAddress(0, 3, 0, 2);
        sheet.addMergedRegion(region);
        Integer numberCell = 3;

        createHeaderGroupCells("Итог", numberCell, rows, sheet, style);
        numberCell += 2;
        createHeaderGroupCells("Процент загрузки", numberCell, rows, sheet, style);
        numberCell += 2;
        createHeaderGroupCells("Проекты центра", numberCell, rows, sheet, style);
        numberCell += 2;
        createHeaderGroupCells("Пресейлы центра", numberCell, rows, sheet, style);
        numberCell += 2;
        createHeaderGroupCells("Проекты/Пресейлы других центров", numberCell, rows, sheet, style);
        numberCell += 2;
        createHeaderGroupCells("Непроектная", numberCell, rows, sheet, style);
        numberCell += 2;
        createHeaderGroupCells("Болезнь", numberCell, rows, sheet, style);
        numberCell += 2;
        createHeaderGroupCells("Отпуск", numberCell, rows, sheet, style);
        for (Project project : projectList) {
            numberCell += 2;
            createHeaderGroupCells(project.getName(), numberCell, rows, sheet, style);
        }
    }

    private void fillReportFromJson(String jsonData, List<Project> projectList, HSSFWorkbook workBook, HSSFSheet sheet) {
        Integer numberRow = 4;
        Integer numberCell = 3;
        JSONObject rootObject = null;
        try {
            rootObject = new JSONObject(jsonData);
            JSONArray rows = rootObject.getJSONArray("rows");

            HSSFPalette palette = workBook.getCustomPalette();
            palette.setColorAtIndex((byte) 41, (byte) 216, (byte) 216, (byte) 216);

            CellStyle oddStyle = getTypicalCellStyle(workBook, palette.getColor(41).getIndex());
            CellStyle evenStyle = getTypicalCellStyle(workBook, IndexedColors.WHITE.getIndex());
            CellStyle style = null;
            for (int i = 0; i < rows.length(); i++) {
                if ((numberRow % 2) == 0) {
                    style = oddStyle;
                } else {
                    style = evenStyle;
                }
                JSONObject jsonRow = rows.getJSONObject(i);
                Row row = sheet.createRow(numberRow);

                fillCellFromJsonRow("employee", 0, jsonRow, row, style);

                Cell cell = row.createCell(1);
                cell.setCellStyle(style);

                cell = row.createCell(2);
                cell.setCellStyle(style);

                CellRangeAddress region = new CellRangeAddress(numberRow, numberRow, 0, 2);
                sheet.addMergedRegion(region);

                fillCellFromJsonRow("summary_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("summary_fact", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("percent_of_charge_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("percent_of_charge_fact", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("center_projects_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("center_projects_fact", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("center_presales_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("center_presales_fact", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("other_projects_and_presales_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("other_projects_and_presales_fact", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("non_project_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("non_project_fact", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("illness_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("illness_fact", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("vacation_plan", numberCell, jsonRow, row, style);
                numberCell++;
                fillCellFromJsonRow("vacation_fact", numberCell, jsonRow, row, style);
                //заполняем информацию по проектам
                for (Project project : projectList) {
                    numberCell++;
                    fillCellFromJsonRow(project.getId() + "_plan", numberCell, jsonRow, row, style);
                    numberCell++;
                    fillCellFromJsonRow(project.getId() + "_fact", numberCell, jsonRow, row, style);
                }
                numberCell = 3;
                numberRow++;
            }
        } catch (JSONException e) {
            error(e);
        }
    }

    private void fillCellFromJsonRow(String fieldName, Integer numberCell, JSONObject jsonRow, Row row, CellStyle style) throws JSONException {
        Cell cell = row.createCell(numberCell);
        cell.setCellStyle(style);
        String cellValue;
        cellValue = getFieldValue(fieldName, jsonRow);

        try {
            if (cellValue.contains("%")) {
                setCellValueAsPercent(cell, cellValue, style);
            } else {
                setCellValueAsDouble(cell, cellValue);
            }
        } catch (Exception ignored) {
            // Если произошла ошибка - записываем как строку
            cell.setCellValue(cellValue);
        }
    }

    private void setCellValueAsPercent(Cell cell, String cellValue, CellStyle outerStyle) {
        Workbook workbook = cell.getSheet().getWorkbook();
        String doubleValue = cellValue.replace("%", "");
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(outerStyle);
        style.setDataFormat((short) 10); // 0.00%
        cell.setCellStyle(style);
        Double v = Double.parseDouble(doubleValue) / 100;
        cell.setCellValue(v);
    }

    private void setCellValueAsDouble(Cell cell, String cellValue) throws Exception {
        double doubleCellValue = Double.parseDouble(cellValue);
        cell.setCellValue(doubleCellValue);
    }

    private String getFieldValue(String fieldName, JSONObject jsonRow) {
        String cellValue;
        try {
            cellValue = jsonRow.getString(fieldName);
            cellValue = cellValue.replace(',', '.');
        } catch (JSONException e) {
            cellValue = "0.00";
        }
        return cellValue;
    }

    private void createHeaderGroupCells(String name, Integer numberCell, List<Row> rows, HSSFSheet sheet, CellStyle style) {
        Cell cell = rows.get(0).createCell(numberCell);
        cell.setCellStyle(style);
        cell.setCellValue(name);
        cell = rows.get(0).createCell(numberCell + 1);
        cell.setCellStyle(style);


        cell = rows.get(1).createCell(numberCell);
        cell.setCellStyle(style);
        cell = rows.get(1).createCell(numberCell + 1);
        cell.setCellStyle(style);

        cell = rows.get(2).createCell(numberCell);
        cell.setCellStyle(style);
        cell = rows.get(2).createCell(numberCell + 1);
        cell.setCellStyle(style);

        Cell cellPlan = rows.get(3).createCell(numberCell);
        cellPlan.setCellValue(PLAN);
        cellPlan.setCellStyle(style);

        Cell cellFact = rows.get(3).createCell(numberCell + 1);
        cellFact.setCellValue(FACT);
        cellFact.setCellStyle(style);

        CellRangeAddress region = new CellRangeAddress(0, 2, numberCell, numberCell + 1);
        sheet.addMergedRegion(region);
    }

    private CellStyle getHeaderCellStyle(HSSFWorkbook workBook) {
        CellStyle style = workBook.createCellStyle();
        style.setWrapText(true);
        style.setBorderBottom(CellStyle.BORDER_MEDIUM);
        style.setBorderLeft(CellStyle.BORDER_MEDIUM);
        style.setBorderRight(CellStyle.BORDER_MEDIUM);
        style.setBorderTop(CellStyle.BORDER_MEDIUM);
        style.setFillBackgroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle getTypicalCellStyle(HSSFWorkbook workBook, Short fillColorId) {
        CellStyle style = workBook.createCellStyle();
        style.setWrapText(true);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setFillBackgroundColor(fillColorId);
        style.setFillForegroundColor(fillColorId);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return style;
    }

    private String toUTF8String(String s) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255 && !Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                byte[] b;
                b = Character.toString(c).getBytes("utf-8");
                for (byte aB : b) {
                    int k = aB;
                    if (k < 0) k += 256;
                    sb.append("%").append(Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    private String updateJson(String jsonRaw) {
        return "{rows:" + jsonRaw + "}";
    }

    private void error(Exception e) {
        logger.error("Error in PlanEditExcelReportService.createAndExportReport : {}", e.getMessage());
    }

}
