package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("divisionService")
public class DivisionService {
	
	@Autowired
	DivisionDAO divisionDAO;
	@Autowired
    ProjectDAO projectDAO;

    @Transactional(readOnly = true)
    public List<Division> getDivisions() {
		return divisionDAO.getActiveDivisions();
	}

	/**
	 * Ищет активное подразделение с указанным именем.
	 * 
	 * @param title название подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
    @Transactional(readOnly = true)
    public Division find(String title) {
		return divisionDAO.find(title);
	}

    @Transactional(readOnly = true)
    public List<Division> getDivisionCheck() {
        return divisionDAO.getDivisionCheck();
    }
	
	/**
	 * Ищет активное подразделение с указанным идентификатором.
	 * 
	 * @param division идентификатор подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
    @Transactional(readOnly = true)
    public Division find(Integer division) {
		if (division != null)
            return divisionDAO.find(division);
        else
            return null;
	}

    @Transactional
    public String setDivisions(List<Division> divisionsToSync) {
        return divisionDAO.setDivisions(divisionsToSync);
    }

    @Transactional
    public void setDivision(Division division) {
        divisionDAO.save(division);
    }

    @Transactional(readOnly = true)
    public Iterable<Division> getAllDivisions() {
        return divisionDAO.getAllDivisions();
    }

    @Transactional(readOnly = true)
    public Boolean isValidDivisionProject(Integer divisionId, Integer projectId) {
        Division division = divisionDAO.find(divisionId);
        if (division != null) {
            Project project = projectDAO.find(projectId);
            if (project != null) {
                return division.getProjects().contains(project);
            }
        }
        return Boolean.FALSE;
    }

}