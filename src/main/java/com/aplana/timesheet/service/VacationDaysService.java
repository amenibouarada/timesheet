package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationDaysDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.dao.entity.VacationDays;
import com.aplana.timesheet.exception.service.ImportXLSException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by abayanov
 * Date: 29.08.14
 */
@Service
public class VacationDaysService {

    private final StringBuffer trace = new StringBuffer();

    @Autowired
    private VacationDaysDAO vacationDaysDAO;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private EmployeeService employeeService;

    public VacationDays save(VacationDays vacationDays) {
        return vacationDaysDAO.save(vacationDays);
    }

    public VacationDays findById(Integer id) {
        return vacationDaysDAO.findById(id);
    }

    public VacationDays findByEmployee(Employee employee) {
        return vacationDaysDAO.findByEmployee(employee);
    }

    public String getTrace() {
        return trace.toString();
    }

    public void importFile(MultipartFile file) {
        trace.setLength(0);
        Workbook workbook = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
            if (file.getOriginalFilename().endsWith("xls")) {
                workbook = new HSSFWorkbook(bis);
            } else if (file.getOriginalFilename().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(bis);
            } else {
                throw new ImportXLSException("Неизвестный формат файла");
            }
            Iterator<Row> iterator = workbook.getSheetAt(0).rowIterator();
            while( iterator.hasNext() ) {
                Row row = iterator.next();
                int rowNum = row.getRowNum();
                if (row.getRowNum() != 0) {
                    ++rowNum;
                    String fio = null;
                    String centerString = null;
                    String regionString = null;
                    Integer count = null;
                    fio = row.getCell(0) != null ? row.getCell(0).getStringCellValue() : "";
                    centerString = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";
                    regionString = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : "";
                    count = row.getCell(3) != null ? new Double(row.getCell(3).getNumericCellValue()).intValue() : null;
                    Date actDate = row.getCell(4) != null ? row.getCell(4).getDateCellValue() : null;
                    Date empBegDate = row.getCell(5) != null ? row.getCell(5).getDateCellValue() : null;

                    if (    StringUtils.isBlank(fio) ||
                            StringUtils.isBlank(centerString) ||
                            StringUtils.isBlank(regionString) ||
                            count == null ||
                            actDate == null ||
                            empBegDate == null
                            ){
                        String col = count == null ? "кол-во дней" :
                                actDate == null ? "дата актуализации" :
                                        empBegDate == null ? "дата устройства сотрудника" :
                                                StringUtils.isBlank(fio) ? "ФИО" :
                                                        StringUtils.isBlank(centerString) ? "центр" :
                                                                StringUtils.isBlank(regionString) ? "регион" : "";
                        trace.append(String.format("Строка %d, не заполнен столбец \"%s\" \n", rowNum, col));
                        continue;
                    }

                    Region region = regionService.find(regionString);
                    if (region == null) {
                        trace.append(String.format("Строка %d, регион не найден: %s \n", rowNum, regionString));
                        continue;
                    }
                    Division division = divisionService.find(centerString);
                    if (division == null) {
                        trace.append(String.format("Строка %d, подразделение не найдено: %s \n", rowNum, centerString));
                        continue;
                    }
                    Employee employee = employeeService.tryFindByFioRegionDivision(fio, region, division);
                    if (employee == null) {
                        trace.append(String.format("Строка %d, cотрудник не найден: %s \n", rowNum, fio));
                        continue;
                    }

                    VacationDays vacationDays = vacationDaysDAO.findByEmployee(employee);
                    if (vacationDays == null) {
                        vacationDays = new VacationDays();
                        vacationDays.setEmployee(employee);
                    }

                    vacationDays.setCountDays(count);
                    vacationDays.setActualizationDate(actDate);
                    vacationDaysDAO.save(vacationDays);
                }
            }
            trace.append("Все строки обработаны");
        } catch (IOException e) {
            trace.append("Ошибка при чтении файла");
        } catch (ImportXLSException e) {
            trace.append(e.getMessage());
        } catch (NumberFormatException e) {
            trace.append("Неверный формат числа в строке");
        }

    }
}
