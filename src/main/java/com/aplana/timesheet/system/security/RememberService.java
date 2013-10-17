package com.aplana.timesheet.system.security;

import com.aplana.timesheet.service.LdapUserDetailsService;
import com.aplana.timesheet.system.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.EmployeeTokenDAO;
import com.aplana.timesheet.dao.entity.EmployeeToken;
import com.aplana.timesheet.system.security.entity.RememberToken;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class RememberService implements RememberMeServices, InitializingBean, LogoutHandler {

    private EmployeeTokenDAO employeeTokenDAO;
    private com.aplana.timesheet.service.LdapUserDetailsService LdapUserDetailsService;

    @Override
    @SuppressWarnings("empty-statement")
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public final Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = getCookie(request);
        if (cookie == null) {
            return null;
        }
        EmployeeToken token;
        token = this.employeeTokenDAO.find(cookie.getValue());
        if (token == null) {
            removeCookie(cookie, response);
            return null;
        }
        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        this.getLdapUserDetailsService().fillAuthority(token.getEmployee(), list);
        TimeSheetUser ts = new TimeSheetUser(token.getEmployee(), list);

        return new RememberToken(ts, null, list);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Cookie cookie = getCookie(request);
        if (cookie == null) {
            return;
        }
        EmployeeToken token = this.employeeTokenDAO.find(cookie.getValue());
        if (token == null) {
            removeCookie(cookie, response);
            return;
        }
        employeeTokenDAO.delete(token);
        removeCookie(cookie, response);
    }

    @Override
    @Transactional
    public final void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        String value = request.getParameter(TimeSheetConstants.POST_REMEMBER);
        if (value == null) {
            return;
        }
        TimeSheetUser tsu = (TimeSheetUser) successfulAuthentication.getPrincipal();
        EmployeeToken token = this.getEmployeeTokenDAO().create(tsu.getEmployee().getId());
        setCookie(token.getKey(), response);
    }

    @Override
    @SuppressWarnings("empty-statement")
    public final void loginFail(HttpServletRequest request, HttpServletResponse response) {

    }

    public EmployeeTokenDAO getEmployeeTokenDAO() {
        return employeeTokenDAO;
    }

    public void setEmployeeTokenDAO(EmployeeTokenDAO employeeTokenDAO) {
        this.employeeTokenDAO = employeeTokenDAO;
    }

    public LdapUserDetailsService getLdapUserDetailsService() {
        return LdapUserDetailsService;
    }

    public void setLdapUserDetailsService(LdapUserDetailsService LdapUserDetailsService) {
        this.LdapUserDetailsService = LdapUserDetailsService;
    }

    private Cookie getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String key = null;
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TimeSheetConstants.COOKIE_REMEMBER)) {
                return cookie;
            }
        }
        return null;
    }

    private void removeCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void setCookie(String key, HttpServletResponse response) {
        Cookie cookie = new Cookie(TimeSheetConstants.COOKIE_REMEMBER, key);
        cookie.setMaxAge(999999999);
        response.addCookie(cookie);
    }
}
