package com.endeavour.tap4food.app.model;

import lombok.Data;

@Data
public class Access {
	

	private String screenName;
	
	private boolean hasReadAccess;
	
	private boolean hasWriteAccess;

}
