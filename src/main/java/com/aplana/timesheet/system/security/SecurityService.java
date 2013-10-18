package com.aplana.timesheet.system.security;

import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public TimeSheetUser getSecurityPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof TimeSheetUser)
            return (TimeSheetUser) principal;
        else
            return null;
    }

}
