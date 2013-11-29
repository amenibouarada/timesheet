package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.ManagerDAO;
import com.aplana.timesheet.dao.RegionDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Manager;
import com.aplana.timesheet.dao.entity.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 */
@Service
public class ManagerService {
    @Autowired
    ManagerDAO managerDAO;
    @Autowired
    DivisionDAO divisionDAO;
    @Autowired
    RegionDAO regionDAO;


    @Transactional(readOnly = true)
    public Manager getManagerByDivisionAndRegion(Integer divisionId, Integer regionId){

        Division division = divisionDAO.find(divisionId);
        Region region = regionDAO.find(regionId);

        if (region == null || division == null) {
            return null;
        }

        return managerDAO.getDivisionRegionManager(division, region);
    }

    public Manager getManagerByDivisionAndRegion(Division division, Region region){
        if (region == null || division == null) {
            return null;
        }
        return managerDAO.getDivisionRegionManager(division, region);
    }

    @Transactional
    public void deleteManager(Manager manager){
        managerDAO.deleteManager(manager);
    }

    @Transactional
    public void insertManager(Manager manager){
        managerDAO.updateInsertManager(manager);
    }

    @Transactional
    public void updateManager(Manager manager){
        managerDAO.updateInsertManager(manager);
    }

    @Transactional(readOnly = true)
    public List<Manager> getManagerList(){
        return managerDAO.getManagerList();
    }

}
