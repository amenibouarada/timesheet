package com.aplana.timesheet.form;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.DictionaryItemService;
import com.aplana.timesheet.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author dsysterov
 * @version 1.0
 */
// ToDo зачем нужен этот класс, если есть класс ProjectManager
public class AdminProjectManagerForm {
    private Integer id;
    private Integer employee;
    private Integer projectRole;
    private Boolean master;
    private Boolean active;
    private String toDelete;

    public AdminProjectManagerForm() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmployee() {
        return employee;
    }

    public void setEmployee(Integer employee) {
        this.employee = employee;
    }

    public Integer getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(Integer projectRole) {
        this.projectRole = projectRole;
    }

    public Boolean getMaster() {
        return master;
    }

    public void setMaster(Boolean master) {
        this.master = master;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getToDelete() {
        return toDelete;
    }

    public void setToDelete(String toDelete) {
        this.toDelete = toDelete;
    }
}
