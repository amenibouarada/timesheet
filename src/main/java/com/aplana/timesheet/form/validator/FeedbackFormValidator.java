package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.service.EmployeeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FeedbackFormValidator extends AbstractValidator {
	
	private final Long SUM_FILE_SIZE = 8388608L;//8 мегабайт
	
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(FeedbackForm.class);
	}
	
	public void validate(Object target, Errors errors){
		FeedbackForm fbForm = (FeedbackForm) target;

		String feedbackDescription = fbForm.getFeedbackDescription();
		MultipartFile[] files = new MultipartFile[]{fbForm.getFile1Path(), fbForm.getFile2Path()};

        Integer feedbackType = fbForm.getFeedbackType();
		// Тип проблемы не выбран.
		if (feedbackType == null) {
			errors.rejectValue("feedbackType", "error.fbform.feedbackType.required", 	"Тип сообщения не выбран.");
        // Неверный тип проблемы
        } else if ( ! isFeedbackTypeValid( feedbackType ) ) {
            errors.rejectValue( "feedbackType", "error.fbform.feedbackType.invalid", "Неверный тип сообщения." );
        }

		// Суть проблемы не описана
        if ( StringUtils.isBlank( feedbackDescription ) ) {
            errors.rejectValue( "feedbackDescription", "error.fbform.feedbackDescription.required", "Текст сообщения не заполнен." );
        }
		// Лимит на суммарный размер файлов првышен
		if(!areFilesValid(files)){
			errors.rejectValue("file1Path", "error.fbform.attachSize.invalid", "Лимит на суммарный размер файлов превышен");
		}
	}

	private boolean isFeedbackTypeValid(int feedbackType) {
		return feedbackType > -1 && feedbackType < 7;
	}
	private boolean areFilesValid(MultipartFile[] files){
		long sumSize = 0;
        for ( MultipartFile file : files ) {
            if ( file != null && ! file.isEmpty() ) {
                sumSize += file.getSize();
            }
        }
        return (sumSize <= SUM_FILE_SIZE);
	}

}
