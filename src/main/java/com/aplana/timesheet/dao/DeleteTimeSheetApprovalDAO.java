package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DeleteTimeSheetApproval;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by abayanov
 * Date: 20.08.14
 */
@Repository
public class DeleteTimeSheetApprovalDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public DeleteTimeSheetApproval storeDeleteTimeSheetApproval(DeleteTimeSheetApproval deleteTimeSheetApproval) {
        DeleteTimeSheetApproval merge = entityManager.merge(deleteTimeSheetApproval);
        entityManager.flush();
        return merge;
    }
}
