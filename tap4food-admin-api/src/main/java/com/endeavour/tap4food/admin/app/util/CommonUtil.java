package com.endeavour.tap4food.admin.app.util;

import java.util.Random;

public class CommonUtil {

	public static String generateOTP() {
		Random random = new Random();
	    int number = random.nextInt(10000);
	    
	    return String.format("%04d", number);
	}
	
	public static String generateUniqueNumber() {
		
		return null;
	}
}
