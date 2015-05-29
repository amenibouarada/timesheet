package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DeleteTimeSheetApprovalDAO;
import com.aplana.timesheet.dao.entity.DeleteTimeSheetApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by abayanov
 * Date: 20.08.14
 */
@Service
public class DeleteTimeSheetApprovalService {

    @Autowired
    DeleteTimeSheetApprovalDAO deleteTimeSheetApprovalDAO;

    public DeleteTimeSheetApproval storeDeleteTimeSheetApproval(DeleteTimeSheetApproval deleteTimeSheetApproval) {
        return deleteTimeSheetApprovalDAO.storeDeleteTimeSheetApproval(deleteTimeSheetApproval);
    }
}
