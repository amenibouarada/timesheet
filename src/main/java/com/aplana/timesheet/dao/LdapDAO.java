package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.*;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LdapDAO {
	private static final Logger logger = LoggerFactory.getLogger(LdapDAO.class);
    private static final String ANY_BUT_NOT_EMPTY = "*";

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Autowired
    private DivisionDAO divisionDAO;

	private LdapTemplate ldapTemplate;

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

    public EmployeeLdap getEmployeeByEmail(String email) {
        logger.info("Getting Employee {} from LDAP",email);
        EqualsFilter filter = new EqualsFilter(propertyProvider.getLdapFieldForEmail(), email);
        logger.debug("LDAP Query {}", filter.encode());
        EmployeeLdap first =
                (EmployeeLdap) Iterables.getFirst(
                        ldapTemplate.search("", filter.encode(), new EmployeeAttributeMapper()),
                        null);
        logger.debug("LDAP Query finished. result = {}", first);
        return first;
    }

    public EmployeeLdap getEmployeeByLdapName(String name) {
        if (name != null && !name.isEmpty()) {
            try {
                EqualsFilter filter = new EqualsFilter(
                        propertyProvider.getLdapFieldForLdapCn(),
                        name.replaceAll("/", ","));
                logger.debug("LDAP Query {}", filter.encode());
                EmployeeLdap first =
                        (EmployeeLdap) Iterables.getFirst(
                                ldapTemplate.search("", filter.encode(), new EmployeeAttributeMapper()),
                                null);
                logger.debug("LDAP Query finished. result = {}", first);
                return first;
            } catch (NameNotFoundException e) {
                logger.debug("Not found: " + name);
                return null;
            }
        } else {
            return null;
        }
    }

    public EmployeeLdap getEmployeeByDisplayName(String name) {
        try {
            EqualsFilter filter = new EqualsFilter(propertyProvider.getLdapFieldForDisplayName(), name);
            logger.debug("LDAP Query {}", filter.encode());
            EmployeeLdap first =
                    (EmployeeLdap) Iterables.getFirst(
                            ldapTemplate.search("", filter.encode(), new EmployeeAttributeMapper()),
                            null);
            logger.debug("LDAP Query finished. result = {}", first);
            return first;
        } catch (NameNotFoundException e) {
            logger.debug("Not found: " + name);
            return null;
        }
    }

    public EmployeeLdap getEmployeeBySID(String sid) {
        try {
            EqualsFilter filter = new EqualsFilter(propertyProvider.getLdapFieldForSID(), sid);
            logger.debug("LDAP Query {}", filter.encode());
            EmployeeLdap first =
                    (EmployeeLdap) Iterables.getFirst(
                            ldapTemplate.search("", filter.encode(), new EmployeeAttributeMapper()),
                            null);
            logger.debug("LDAP Query finished. result = {}", first);
            return first;
        } catch (NameNotFoundException e) {
            logger.debug("Not found: " + sid);
            return null;
        }
    }

    public List<EmployeeLdap> getEmployeesByDepartmentNameFromDb(String department) {
        if (department != null && !department.isEmpty()){
            logger.debug("DeparmentName – {}", department);
            String[] split = department.split(",");
            List<EmployeeLdap> result = new ArrayList<EmployeeLdap>();

            for (String s : split) {
                result.addAll(getEmployees(s));
            }
            return result;
        } else {
            return null;
        }
    }

	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getEmployees(String department) {
		logger.info("Getting Employees from LDAP.");
        AndFilter andFilter = new AndFilter()
                .and(new EqualsFilter(propertyProvider.getLdapFieldForDivision(), department))
                .and(new EqualsFilter(
                        propertyProvider.getLdapFieldForObjectClass(),
                        propertyProvider.getLdapObjectClassEmployee()))
                .and(new LikeFilter(propertyProvider.getLdapFieldForEmail(), ANY_BUT_NOT_EMPTY));
        logger.debug("LDAP Query {}", andFilter.encode());
        // для OpenLDAP используется вместо whenCreated поле createTimestamp, которое является скрытым
        // чтобы его получить мы запрашиваем это поле и все остальные ("*")
        // когда данные будут запрашиваться для AD, то будет запрашиваться whenCreated, которое и так там видно
        // и будет получено и по "*", но это проблем не создаст и для AD тоже всё будет корректно
		List<EmployeeLdap> employees = ldapTemplate.search("", andFilter.encode(), SearchControls.SUBTREE_SCOPE,
                new String[]{ propertyProvider.getLdapFieldForWhenCreated(), ANY_BUT_NOT_EMPTY }, new EmployeeAttributeMapper());
		logger.debug("LDAP Query finished. Employees size is {}", employees.size());
		if(!employees.isEmpty())
            logger.debug("Employee {} City is {}", employees.get(0).getDisplayName(), employees.get(0).getCity());
		return employees;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getDisabledEmployees() {
		logger.info("Getting Disabled Employees from LDAP.");
        AndFilter andFilter = new AndFilter()
                .and(new EqualsFilter(
                        propertyProvider.getLdapFieldForObjectClass(),
                        propertyProvider.getLdapObjectClassDisabledEmployee()))
                .and(new NotFilter(
                        new LikeFilter(propertyProvider.getLdapFieldForEmail(), ANY_BUT_NOT_EMPTY)
                ));
        OrFilter orFilter = new OrFilter();
        List<Division> divisions = divisionDAO.getDivisionsToSyncEmployee();
        for (Division division : divisions){
            orFilter.or(new EqualsFilter(propertyProvider.getLdapFieldForDivision(), division.getLdapName()));
        }
        andFilter.and(orFilter);
		logger.debug("LDAP Query {}", andFilter.encode());
        List search = ldapTemplate.search(DistinguishedName.EMPTY_PATH, andFilter.encode(), new EmployeeAttributeMapper());
        logger.debug("LDAP Query finished. result.size = {}", search.size());
        return search;
	}

	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getDivisionLeader(String divisionLeaderName, String division) {
		logger.info("Getting Division Leaders from LDAP.");
        AndFilter andFilter = new AndFilter()
                .and(new EqualsFilter(propertyProvider.getLdapFieldForDisplayName(), divisionLeaderName))
                .and( new EqualsFilter( propertyProvider.getLdapFieldForDivision(), division ) );
        logger.debug("LDAP Query {}", andFilter.encode());
        List search = ldapTemplate.search("", andFilter.encode(), new EmployeeAttributeMapper());
        logger.debug("LDAP Query finished. result.size = {}", search.size());
        return search;
	}

    @SuppressWarnings("unchecked")
    public List<Map> getDivisions() {
        AndFilter andFilter = new AndFilter()
                .and(new EqualsFilter(
                        propertyProvider.getLdapFieldForObjectClass(),
                        propertyProvider.getLdapObjectClassDivision()))
                .and(new LikeFilter("cn", propertyProvider.getLdapCnDivision()));
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        logger.debug("LDAP Query {}", andFilter.encode());
        List search = ldapTemplate.search("", andFilter.encode(), ctls, new AttributesMapper() {
            @Override
            public Map mapFromAttributes(Attributes attributes) throws NamingException {
                Map map = new HashMap();
                NamingEnumeration<? extends Attribute> all = attributes.getAll();
                while (all.hasMoreElements()) {
                    Attribute attribute = all.nextElement();
                    map.put(attribute.getID(), attribute.get());
                }
                return map;
            }
        });
        logger.debug("LDAP Query finished. result.size = {}", search.size());
        return search;
    }

    private class EmployeeAttributeMapper implements AttributesMapper {
		public Object mapFromAttributes(Attributes attributes) throws NamingException {
			EmployeeLdap employee = new EmployeeLdap();

            // Для OpenLDAP в качестве objectSid используется uid, который приходит как строка,
            // поэтому его нет необходимости преобразовывать из массива байт в строку.
            // В AD используется objectSid, которое не поддерживается OpenLDAP
            Object sid = attributes.get(propertyProvider.getLdapFieldForSID()).get();
            if (sid instanceof String){
                employee.setObjectSid((String)sid);
            }else{
                employee.setObjectSid(LdapUtils.convertBinarySidToString((byte[])sid));
            }
            employee.setDepartment  ( getAttributeByName( attributes, propertyProvider.getLdapFieldForDivision()));
            employee.setDisplayName ( getAttributeByName( attributes, propertyProvider.getLdapFieldForDisplayName()));
            employee.setEmail       ( getAttributeByName( attributes, propertyProvider.getLdapFieldForEmail()));
		    employee.setManager     ( getAttributeByName( attributes, propertyProvider.getLdapFieldForManager()));
            employee.setTitle       ( getAttributeByName( attributes, propertyProvider.getLdapFieldForTitle()));
            employee.setWhenCreated ( getAttributeByName( attributes, propertyProvider.getLdapFieldForWhenCreated()));
            employee.setCity        ( getAttributeByName( attributes, propertyProvider.getLdapFieldForCity()));
            employee.setMailNickname( getAttributeByName( attributes, propertyProvider.getLdapFieldForMailNickname()));

            Attribute ldapCn = attributes.get(propertyProvider.getLdapFieldForLdapCn());
			if(ldapCn != null)
				employee.setLdapCn(ldapCn.get().toString());
			
			return employee;
		}

        private String getAttributeByName( Attributes attributes, String attributeName ) throws NamingException {
            Attribute department = attributes.get( attributeName );

            return department != null ? ( String ) department.get() : null;
        }
    }
}