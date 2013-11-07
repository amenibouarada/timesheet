package com.aplana.timesheet.system.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * User: eyaroslavtsev
 * Date: 06.08.12
 */
public class AutentificationFilter implements Filter {
    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(true);
        try {
            session.setAttribute("lastLogin", request.getParameter("j_username"));
        } catch (Exception ex) {
            session.setAttribute("lastLogin", "");
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

}
