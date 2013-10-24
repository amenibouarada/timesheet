package com.aplana.timesheet.form.validator;

import javax.servlet.http.HttpSession;

/**
 * @author pmakarov
 * mock для тестов
 */
public class FakeSessionAttributes {
    public HttpSession getHttpSession(){
        return null;
    }
}
