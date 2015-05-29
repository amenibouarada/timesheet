package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.ProjectRole;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static argo.jdom.JsonNodeBuilders.anArrayBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;

@Service
public class AvailableActivityCategoryService {
	@Autowired
	AvailableActivityCategoryDAO availableActivityCategoryDAO;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private DictionaryItemService dictionaryItemService;
	
	/**
	 * Возвращает доступные категории активности
	 * @param actType
	 * 			Тип активности.
	 * @param projectRole
	 * 			Проектная роль.
	 * @return List<AvailableActivityCategory>
	 * 			Список доступных категорий активности.
	 */
    @Transactional(readOnly = true)
    public List<AvailableActivityCategory> getAvailableActivityCategories(
														DictionaryItem actType, ProjectRole projectRole) {
		return availableActivityCategoryDAO.getAvailableActivityCategories(actType, projectRole);
	}

    @Transactional(readOnly = true)
    public String getAvailableActCategoriesJson() {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final List<DictionaryItem> actTypes = dictionaryItemService.getTypesOfActivity();
        final List<ProjectRole> projectRoles = projectRoleService.getProjectRoles();

        for (DictionaryItem actType : actTypes) {
            for (ProjectRole projectRole : projectRoles) {
                final List<AvailableActivityCategory> avActCats = getAvailableActivityCategories(actType, projectRole);
                final JsonArrayNodeBuilder avActCatsBuilder = anArrayBuilder();

                for (AvailableActivityCategory avActCat : avActCats) {
                    avActCatsBuilder.withElement(JsonUtil.aStringBuilderNumber(avActCat.getActCat().getId()));
                }

                builder.withElement(
                        anObjectBuilder().
                                withField("actType", JsonUtil.aStringBuilderNumber(actType.getId())).
                                withField("projRole", JsonUtil.aStringBuilderNumber(projectRole.getId())).
                                withField("avActCats", avActCatsBuilder)
                );
            }
        }

        return JsonUtil.format(builder);
    }

}