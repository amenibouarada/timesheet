package com.aplana.timesheet.service;

import com.aplana.timesheet.system.constants.RoleConstants;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Permission;
import com.aplana.timesheet.enums.PermissionsEnum;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.system.security.entity.TimeSheetUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service("myLdapUserDetailsService")
public class LdapUserDetailsService implements UserDetailsContextMapper {

    private static final Logger logger = LoggerFactory.getLogger(LdapUserDetailsService.class);

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private EmployeeLdapService employeeLdapService;

    public void fillAuthority(Employee employee, List<GrantedAuthority> list) {
        list.add( new SimpleGrantedAuthority(RoleConstants.ROLE_USER) ); // права обычного пользователя в любом случае

        Collection<Permission> permissions = employee.getPermissions();

        if (permissions == null) {
            return;
        }

        for (Permission permission : permissions){
            switch ( EnumsUtils.getEnumById(permission.getId(), PermissionsEnum.class)) {
                case REPORTS_PERMISSION: {
                    list.add( new SimpleGrantedAuthority(RoleConstants.ROLE_MANAGER) );
                    break;
                }

                case ADMIN_PERMISSION: {
                    list.add( new SimpleGrantedAuthority(RoleConstants.ROLE_ADMIN) );
                    break;
                }

                case VIEW_PLANS: {
                    list.add(new SimpleGrantedAuthority(RoleConstants.ROLE_PLAN_VIEW));
                    break;
                }

                case EDIT_PLANS: {
                    list.add(new SimpleGrantedAuthority(RoleConstants.ROLE_PLAN_VIEW));
                    list.add(new SimpleGrantedAuthority(RoleConstants.ROLE_PLAN_EDIT));
                    break;
                }

                case VIEW_ILLNESS_BUSINESS_TRIP: {
                    list.add(new SimpleGrantedAuthority(RoleConstants.VIEW_ILLNESS_BUSINESS_TRIP));
                    break;
                }

                case CHANGE_ILLNESS_BUSINESS_TRIP: {
                    list.add(new SimpleGrantedAuthority(RoleConstants.CHANGE_ILLNESS_BUSINESS_TRIP));
                    list.add(new SimpleGrantedAuthority(RoleConstants.VIEW_ILLNESS_BUSINESS_TRIP));
                    break;
                }

                case MONTH_REPORT_PERMISSION:{
                    list.add(new SimpleGrantedAuthority(RoleConstants.ROLE_MONTH_REPORT_MANAGER));
                    // медеджер по табелю также имеет права на просмотр формы "Формирование отчётов"
                    list.add(new SimpleGrantedAuthority(RoleConstants.ROLE_MANAGER));
                    break;
                }

                default:
                    logger.error("Unknown permission");
            }
        }
    }

    public UserDetails mapUserFromContext(DirContextOperations dirContextOperations, String s, Collection<? extends GrantedAuthority> grantedAuthorities) {
        try {
            String email = dirContextOperations.getStringAttribute("mail");

            Employee employee = employeeDAO.findByEmail(email);

            List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
            if (employee != null) {
                //если на текущий момент employee считается уволенным
                if (employee.isDisabled(null)) {
                    logger.warn("Employee is archived for email {}", email);
                    throw new BadCredentialsException("Ваша учетная запись отключена");
                }
                fillAuthority(employee, list);
            } else {
                logger.warn("Employee add in DB {}", email);
                String errors = employeeLdapService.synchronizeOneEmployee(email);
                if (errors != null) {
                    if ( !errors.equals( "" ) ) {
                        final int endIndex = errors.lastIndexOf(',');

                        String errorsSub = (endIndex < 0) ? errors : errors.substring(0, endIndex);

                        throw new BadCredentialsException("Авторизация выполнена успешно, но не удалось определить следующие параметры: <br>" + errorsSub);
                    } else {
                        throw new BadCredentialsException("Авторизация выполнена успешно, пользователь добавлен в БД<br> Авторизуйтесь еще раз");
                    }
                } else {
                    throw new BadCredentialsException("В LDAP что-то изменилось. попробуйте еще раз");
                }
            }

            return new TimeSheetUser(employee, list);
        } catch (RuntimeException e) {
            if (!(e instanceof AuthenticationException)) {
                logger.error("Неопознанная ошибка при авторизации", e);
                throw new AccountExpiredException("Неопознанная ошибка при авторизации", e);
            } else
                throw e;
        }
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
    }

}
