package com.endeavour.tap4food.app.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtil {

	public static String getDateFromMillisec(Long milliSeconds) {
		
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);

		return formatter.format(calendar.getTime()); 
	}
	
	public static String getPresentDateAndTime() {
		String time = String.valueOf(LocalDateTime.now());
		
		log.info("Current Date & Time : {}", time);
		return time;
	}
}
