package com.aplana.timesheet.util;

import com.aplana.timesheet.form.UploadedFile;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.stereotype.Component;
/**
 * Created by abayanov
 * Date: 29.08.14
 */
@Component
public class XMLFileValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object uploadedFile, Errors errors) {

        UploadedFile file = (UploadedFile) uploadedFile;

        if (file.getFile().getSize() == 0) {
            errors.rejectValue("file", "uploadForm.salectFile",
                    "Please select a file!");
        }

    }

}