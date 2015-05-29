package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class AvailableActivityCategoryDAO {
	@PersistenceContext
    private EntityManager entityManager;
	
	/**
	 * Возвращает доступные категории активности
	 * @param actType
	 * 			Тип активности.
	 * @param projectRole
	 * 			Проектная роль.
	 * @return List<AvailableActivityCategory>
	 * 			Список доступных категорий активности.
	 */
	@SuppressWarnings("unchecked")
	public List<AvailableActivityCategory> getAvailableActivityCategories(
           DictionaryItem actType, ProjectRole projectRole
    ) {
		Query query = entityManager.createQuery(
                "from AvailableActivityCategory as ac where ac.actType=:actType and ac.projectRole=:projectRole"
        ).setParameter("actType", actType).setParameter("projectRole", projectRole);

        return query.getResultList();
    }

    public List<AvailableActivityCategory> getAllAvailableActivityCategories(){
        Query query = entityManager.createQuery("from AvailableActivityCategory as aac");
        return query.getResultList();
    }
}