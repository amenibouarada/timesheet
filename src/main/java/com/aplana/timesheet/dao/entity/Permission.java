package com.aplana.timesheet.dao.entity;

import javax.persistence.*;
import java.util.Set;

/**
 * User: iziyangirov
 * Date: 21.01.13
 */
@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "employee_permissions",
            joinColumns = {
                    @JoinColumn(name = "permission_id", nullable = false) },
            inverseJoinColumns = {
                    @JoinColumn(name = "employee_id", nullable = false) })
    private Set<Employee> employees;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "project_role_permissions",
            joinColumns = {
                    @JoinColumn(name = "permission_id", nullable = false) },
            inverseJoinColumns = {
                    @JoinColumn(name = "project_role_id", nullable = false) })
    private Set<ProjectRole> projectRoles;

    public Set<ProjectRole> getProjectRoles() {
        return projectRoles;
    }

    public void setProjectRoles(Set<ProjectRole> projectRoles) {
        this.projectRoles = projectRoles;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String beginDate) {
        this.name = beginDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Permission that = (Permission) o;

        final Integer thatId = that.getId();

        return !(id != null ? !id.equals(thatId) : thatId != null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (employees != null ? employees.hashCode() : 0);
        result = 31 * result + (projectRoles != null ? projectRoles.hashCode() : 0);
        return result;
    }
}