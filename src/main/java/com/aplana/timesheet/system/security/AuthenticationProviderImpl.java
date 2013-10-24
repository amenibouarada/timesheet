package com.aplana.timesheet.system.security;

import com.aplana.timesheet.system.security.entity.RememberToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationProviderImpl implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication a) throws AuthenticationException {
        if (!this.supports(a.getClass())) {
            return null;
        }
        return a;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RememberToken.class.isAssignableFrom(authentication);
    }
}
