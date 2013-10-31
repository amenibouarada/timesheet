package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.Employee;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author iziyangirov
 */
public class LdapUserDetailsServiceTest extends AbstractTest {

    @Autowired
    LdapUserDetailsService ldapUserDetailsService;

    @Test
    public void testFillAuthority() throws Exception {
        List<GrantedAuthority> mockedList = spy(new ArrayList<GrantedAuthority>());
        Employee mockedEmpl = mock(Employee.class);
        when(mockedEmpl.getId()).thenReturn(1);

        ldapUserDetailsService.fillAuthority(mockedEmpl, mockedList);

        verify(mockedList, atLeastOnce()).add((GrantedAuthority) anyObject());
    }

}
