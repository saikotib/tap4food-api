package com.endeavour.tap4food.admin.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.endeavour.tap4food.admin.app.controller.AdminController;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.exception.ErrorMessage;

@ControllerAdvice(basePackageClasses = { AdminController.class })
public class CustomExceptionController {

	@ExceptionHandler(value = TFException.class)
	public ResponseEntity<ErrorMessage> exception(TFException exception) {
		ErrorMessage error = ErrorMessage.builder().errorCode(HttpStatus.NOT_FOUND.name())
				.errorMessage(exception.getMessage()).build();

		return new ResponseEntity<ErrorMessage>(error, HttpStatus.NOT_FOUND);
	}
}
