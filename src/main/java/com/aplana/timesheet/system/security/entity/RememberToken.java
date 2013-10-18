package com.aplana.timesheet.system.security.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author ekuvshinov
 */
public class RememberToken implements Authentication {
    Boolean authenticated;
    List<GrantedAuthority> authorities;
    TimeSheetUser principal;
    Object credentials;
    
    public RememberToken(TimeSheetUser principal, Object credentials, List<GrantedAuthority> authorities) {
        this.authorities = authorities;
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    /**
     *
     * @param bln
     * @throws IllegalArgumentException
     */
    @Override
    public void setAuthenticated(boolean bln) throws IllegalArgumentException {
        this.authenticated = bln;
    }

    @Override
    public String getName() {
        return principal.getEmployee().getName();
    }
}
