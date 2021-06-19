package com.endeavour.tap4food.app.response.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ResponseHolder {
	
	private String status;
	
	private String timestamp;
	
	private Object data;
}
