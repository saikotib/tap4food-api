package com.endeavour.tap4food.app.response.dto;

import lombok.Data;

@Data
public class StallManager {

	private Long managerId;
	
	private String managerName;
	
	private String email;
	
	private String phoneNumber;
	
	private String foodStallName;
	
	private Long foodStallId;
	
}
