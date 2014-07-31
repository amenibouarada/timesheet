package com.aplana.timesheet.form;

import com.aplana.timesheet.dao.entity.ApprovalResultModel;

import java.util.List;

/**
 * @author iziyangirov
 */
public class VacationApprovalForm {

    String message;
    String buttonsVisible;
    List<ApprovalResultModel> approvalList;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getButtonsVisible() {
        return buttonsVisible;
    }

    public void setButtonsVisible(String buttonsVisible) {
        this.buttonsVisible = buttonsVisible;
    }

    public List<ApprovalResultModel> getApprovalList(){
        return approvalList;
    }

    public void setApprovalList(List<ApprovalResultModel> approvalList){
        this.approvalList = approvalList;
    }

    public Integer getSize(){
        return approvalList!=null?approvalList.size():0;
    }
}
