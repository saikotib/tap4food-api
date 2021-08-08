package com.endeavour.tap4food.app.response.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResponseHolder {
	
	private String status;
	
	private String timestamp;
	
	private Object data;
}
