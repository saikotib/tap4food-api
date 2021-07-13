package com.endeavour.tap4food.app.model.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ErrorMessage {

	private String errorCode;
	
	private String errorMessage;
}
