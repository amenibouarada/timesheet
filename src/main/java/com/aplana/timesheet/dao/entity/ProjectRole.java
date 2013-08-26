package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.dao.Identifiable;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "project_role", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "code"}))
public class ProjectRole implements Identifiable {
    @Id
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(length = 3, nullable = false)
    private String code;

    @Column(columnDefinition = "bool not null default true")
    private boolean active;

    @Column(name = "ldap_title", length = 100, nullable = false)
    public String ldapTitle;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "project_role_permissions",
            joinColumns = {
                    @JoinColumn(name = "project_role_id", nullable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "permission_id", nullable = false)})
    private Set<Permission> permissions;

    /**
     * Конструктор по умолчанию
     */
    public ProjectRole() {
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

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLdapTitle() {
        return ldapTitle;
    }

    public void setLdapTitle(String ldapTitle) {
        this.ldapTitle = ldapTitle;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(" id=").append(id)
                .append(" active=").append(active)
                .append(" name=").append(name)
                .append(" code=").append(code)
                .append(" ldapTitle=").append(ldapTitle)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectRole)) return false;

        ProjectRole that = (ProjectRole) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}