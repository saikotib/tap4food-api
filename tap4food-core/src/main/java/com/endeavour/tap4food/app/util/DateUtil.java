package com.endeavour.tap4food.app.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtil {
	
	public static void main(String[] args) throws ParseException {
		
		System.out.println(getPresentDateAndTime());
		System.out.println(getPresentDateAndTimeInGMT());
		System.out.println(getPresentDateAndTimeInIST());

//		String gmtDate = "2022-05-26 18:16:38";
//		String timezone = "GMT";
//
//		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(gmtDate);
//
//		DateFormat istFormat = new SimpleDateFormat();
//		DateFormat gmtFormat = new SimpleDateFormat();
//		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
//		TimeZone istTime = TimeZone.getTimeZone("IST");
//
//		istFormat.setTimeZone(gmtTime);
//		gmtFormat.setTimeZone(istTime);
//		System.out.println("GMT Time: " + istFormat.format(date));
//		System.out.println("IST Time: " + gmtFormat.format(date));
//
//		System.out.println(Integer.parseInt("01"));
	}
	
	public static String todayName() {
		
		return LocalDateTime.now().getDayOfWeek().name();
	}
	
	public static String getPresentTimeHHMM() {
		
		LocalDateTime presentTime = LocalDateTime.now();
		
		return presentTime.getHour() + ":" + presentTime.getMinute();
	}

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
	
	public static String getPresentDateAndTimeInGMT() {
		
		SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		String time = gmtDateFormat.format(new Date());
		
		log.info("Current Date & Time : {}", time);
		return time;
	}
	
	public static String getPresentDateAndTimeInIST() {
		
		SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		gmtDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		
		String time = gmtDateFormat.format(new Date());
		
		log.info("Current Date & Time : {}", time);
		return time;
	}
	
	public static String getPresentTime() {
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		
		String time = dtf.format(now);
		
		log.info("Current Date & Time : {}", time);
		return time;
	}
	
	public static String getToday() {
		String pattern = "dd-MMM-yyyy";
		String dateInString =new SimpleDateFormat(pattern).format(new Date());
		
		return dateInString;
	}
	
	public static boolean checkIfStallOpenedNow(String openTime, String closeTime) {
		
		boolean flag = false;
		
		String openTimeTokens[] = openTime.split(":");
		String closeTimeTokens[] = closeTime.split(":");
		
		LocalDateTime presentTime = LocalDateTime.now();
		
		int hour = presentTime.getHour();
		int minutes = presentTime.getMinute();
		
		if(hour >= Integer.parseInt(openTimeTokens[0])) {
			
			if(minutes >= Integer.parseInt(openTimeTokens[1])) {
				flag = true;				
			}else {
				flag = false;
			}
			
		}else {
			flag = false;
		}
		
		if(hour <= Integer.parseInt(closeTimeTokens[0])) {
			
			if(minutes <= Integer.parseInt(closeTimeTokens[1])) {
				flag = true;				
			}else {
				flag = false;
			}
			
		}else {
			flag = false;
		}
		
		
		return flag;
	}
}
