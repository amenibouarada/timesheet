package com.aplana.timesheet.enums;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author pmakarov
 * @see <a href="">Аналитика</a>
 *      creation date: 04.12.13
 */
public class EnumTest extends AbstractTest {

    @Autowired
    DictionaryDAO dictionaryDao;

    @Autowired
    DictionaryItemDAO dictionaryItemDAO;

    /**
     * Упрощенная версия enumTest для DictionaryItem
     * @param clazz - класс enum'а
     * @param <E> - enum
     */
    public <E extends Enum<E> & TSEnum> void enumDictionaryTest(Class<E> clazz){
        enumTest(clazz, new TSEnumHandler() {
            @Override
            public void handle(TSEnum tsEnum) {
                DictionaryItem dictionaryItem = dictionaryItemDAO.find(tsEnum.getId());
                assertNotNull(dictionaryItem);
                assertEquals(tsEnum.getName(), dictionaryItem.getValue());
            }
        });
    }

    @Test
    public void dictionaryEnumTest(){
        enumTest(DictionaryEnum.class, new TSEnumHandler() {
            @Override
            public void handle(TSEnum tsEnum) {
                Dictionary dictionary = dictionaryDao.find(tsEnum.getId());
                assertNotNull(dictionary);
                assertEquals(dictionary.getName(), ((DictionaryEnum)tsEnum).getDictName());
            }
        });
    }


    @Test
    public void quickReportTypesEnumTest(){
        enumTest(QuickReportTypesEnum.class, new TSEnumHandler() {
            @Override
            public void handle(TSEnum tsEnum) {
                Dictionary dictionary = dictionaryDao.find(tsEnum.getId());
                assertNotNull(dictionary);
                assertEquals(dictionary.getName(), ((QuickReportTypesEnum)tsEnum).getDictName());
            }
        });
    }


    @Autowired
    RegionDAO regionDAO;

    @Test
    public void regionsEnumTest(){
        enumTest(RegionsEnum.class, new TSEnumHandler() {
            @Override
            public void handle(TSEnum tsEnum) {
                Region region = regionDAO.find(tsEnum.getId());
                assertNotNull(region);
                assertEquals(region.getName(), tsEnum.getName());
            }
        });
    }


    @Autowired
    PermissionDAO permissionDAO;

    @Test
    public void permissionsEnumTest(){
        enumTest(PermissionsEnum.class, new TSEnumHandler() {
            @Override
            public void handle(TSEnum tsEnum) {
                Permission permission = permissionDAO.find(tsEnum.getId());
                assertNotNull(permission);
                assertEquals(permission.getName(), tsEnum.getName());
            }
        });
    }


    @Autowired
    ProjectRoleDAO projectRoleDAO;

    @Test
    public void projectRolesEnumTest(){
        enumTest(ProjectRolesEnum.class, new TSEnumHandler() {
            @Override
            public void handle(TSEnum tsEnum) {
                ProjectRole projectRole = projectRoleDAO.find(tsEnum.getId());
                assertNotNull(projectRole);
                assertEquals(projectRole.getName(), tsEnum.getName());
            }
        });
    }

    @Test
    public void businessTripTypesEnumTest(){
        enumDictionaryTest(BusinessTripTypesEnum.class);
    }

    @Test
    public void effortInNextDayEnumTest(){
        enumDictionaryTest(EffortInNextDayEnum.class);
    }

    @Test
    public void employeePlanTypeTest(){
        enumDictionaryTest(EmployeePlanType.class);
    }

    @Test
    public void illnessTypesEnumTest(){
        enumDictionaryTest(IllnessTypesEnum.class);
    }

    @Test
    public void overtimeCausesEnumTest(){
        enumDictionaryTest(OvertimeCausesEnum.class);
    }

    @Test
    public void projectFundingTypeEnumTest(){
        enumDictionaryTest(ProjectFundingTypeEnum.class);
    }

    @Test
    public void typesOfActivityEnumTest(){
        enumDictionaryTest(TypesOfActivityEnum.class);
    }

    @Test
    public void TypesOfCompensationEnumTest(){
        enumDictionaryTest(TypesOfCompensationEnum.class);
    }

    @Test
    public void typesOfTimeSheetEnumTest(){
        enumDictionaryTest(TypesOfTimeSheetEnum.class);
    }

    @Test
    public void undertimeCausesEnumTest(){
        enumDictionaryTest(UndertimeCausesEnum.class);
    }

    @Test
    public void vacationStatusEnumTest(){
        enumDictionaryTest(VacationStatusEnum.class);
    }

    @Test
    public void VacationTypesEnumTest(){
        enumDictionaryTest(VacationTypesEnum.class);
    }

    @Test
    public void workOnHolidayCausesEnumTest(){
        enumDictionaryTest(WorkOnHolidayCausesEnum.class);
    }

    @Test
    public void workPlacesEnumTest(){
        enumDictionaryTest(WorkPlacesEnum.class);
    }
}
