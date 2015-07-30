package com.aplana.timesheet.reports;

import com.aplana.timesheet.dao.AbstractReportDAO;
import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.exception.JReportBuildError;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.aplana.timesheet.util.DateTimeUtil.*;

public abstract class BaseReport implements TSJasperReport {

    @Autowired
    protected AbstractReportDAO reportDAO;

    @Override
    public void setReportDAO(AbstractReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    @Override
    public void checkParams() {
        this.beginDate = "".equals(this.beginDate) ? MIN_DATE : this.beginDate;
        this.endDate = "".equals(this.endDate) ? MAX_DATE : this.endDate;
    }

    protected String beginDate = currentMonthFirstDay();

    protected String endDate = currentDay();

    public Integer getDivisionOwnerId() {
        return divisionOwnerId;
    }

    public void setDivisionOwnerId(Integer divisionOwnerId) {
        this.divisionOwnerId = divisionOwnerId;
    }

    private Integer divisionOwnerId;

	/**
	 * Список идентификаторов регионов
	 */
	protected List<Integer> regionIds;

	/**
	 * Список полных наименований регионов
	 */
    protected List<String> regionNames;
	
	/**
	 * Костыль. Ознчачает что отчет надо формировать по всем регионам
	 */
	protected boolean allRegions;

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<Integer> getRegionIds() {
        return regionIds;
    }

    public void setRegionIds(List<Integer> regionIds) {
        this.regionIds = regionIds;
    }

    public List<String> getRegionNames() {
        return regionNames;
    }

    public void setRegionNames(List<String> regionNames) {
        this.regionNames = regionNames;
    }
	
	/**
	 * Определяет есть ли хоть один регион в списке
	 * @return true - когда есть выбран хоть один регион
	 */
	public boolean hasRegions() {
        return regionIds != null && ! regionIds.isEmpty();
	}
	
	public boolean isAllRegions() {
		return allRegions;
	}
	
	public void setAllRegions(boolean allRegions) {
		this.allRegions = allRegions;
	}
	
	/**
	 * Преобразует список регионов в строку. Не нашел как это сделать средствами jasperreport
	 * или закинуть в jassperreport класс занимающийся форматированием таких вещей.
	 * @return Возвращает список регионов упакованных в строку.
	 */
	//TODO переделать на использование библиотек (ApacheCommons или Guava)
	public String wellFormedRegionList() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
        if (regionNames == null) {
            return "Все";
        }
		for( String rName : regionNames) {
			if(first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(rName);
		}
		return sb.toString();
	}

    @Override
    public JRDataSource prepareDataSource() throws JReportBuildError {
        return reportDAO.getReportData(this);
    }
}
