package com.aplana.timesheet.system.security.entity;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.util.HibernateUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class TimeSheetUser extends User {

    private Employee employee;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        if (employee != null) {
            HibernateUtils.fetchAllFields(employee);
        }

        this.employee = employee;
    }

    public TimeSheetUser(Employee user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getName(), "[PROTECTED]", authorities);

        setEmployee(user);
    }
}
