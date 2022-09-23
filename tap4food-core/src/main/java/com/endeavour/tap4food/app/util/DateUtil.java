package com.endeavour.tap4food.app.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtil {
	
	public static void main(String[] args) throws ParseException {
		
		System.out.println(getMonth("2022-07-01 02:50:41"));
//		System.out.println(getPresentDateAndTimeInGMT());
//		System.out.println(getPresentDateAndTimeInIST());

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
	
	public static String todayName(String customerTimeZone) {
				
		ZoneId zid = ZoneId.of(customerTimeZone);
		
		return LocalDateTime.now(zid).getDayOfWeek().name();
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
	
	public static String getPresentDate() {
		
		SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String date = gmtDateFormat.format(new Date());
		
		log.info("Current Date : {}", date);
		return date;
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
	
	public static boolean checkIfStallOpenedNow(String openTime, String closeTime, String customerTimeZone) {
		boolean flag = false;
		
		System.out.println("openTime : " + openTime);
		System.out.println("closeTime : " + closeTime);
		
		String openTimeTokens[] = openTime.split(":");
		String closeTimeTokens[] = closeTime.split(":");
		
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("HH:mm:ss");

		df.setTimeZone(TimeZone.getTimeZone(customerTimeZone));

		System.out.println(df.format(date));
		
		String formatedDateTimeValTokens[] = df.format(date).split(":");
		
		Integer currentHourVal = Integer.parseInt(formatedDateTimeValTokens[0]); // 21
		Integer currentMinuteVal = Integer.parseInt(formatedDateTimeValTokens[1]); // 12
//		Integer currentSecVal = Integer.parseInt(formatedDateTimeValTokens[0]);
		
		/*
		if(currentHourVal >= Integer.parseInt(openTimeTokens[0])) {
			flag = true;
			if(currentHourVal == Integer.parseInt(openTimeTokens[0]) && currentMinuteVal < Integer.parseInt(openTimeTokens[1])) {
				flag = false;				
			}
			
		}
		
		if(currentHourVal <= Integer.parseInt(closeTimeTokens[0])) {
			
			flag = true;
			if(currentHourVal == Integer.parseInt(closeTimeTokens[0]) && currentMinuteVal > Integer.parseInt(closeTimeTokens[1])) {
				flag = false;				
			}
		}	
		*/
		
		if(currentHourVal >= Integer.parseInt(openTimeTokens[0]) && currentHourVal <= Integer.parseInt(closeTimeTokens[0])) {
			flag = true;
			if(currentHourVal == Integer.parseInt(openTimeTokens[0]) && currentMinuteVal < Integer.parseInt(openTimeTokens[1])) {
				flag = false;				
			}
			
			if(currentHourVal == Integer.parseInt(closeTimeTokens[0]) && currentMinuteVal > Integer.parseInt(closeTimeTokens[1])) {
				flag = false;				
			}
			
		}
		
		return flag;
	}
	
	public static boolean checkIfStallOpenedNow(String openTime, String closeTime) {
		
		boolean flag = false;
		
		System.out.println("openTime : " + openTime);
		System.out.println("closeTime : " + closeTime);
		
		String openTimeTokens[] = openTime.split(":");
		String closeTimeTokens[] = closeTime.split(":");
		
		LocalDateTime presentTime = LocalDateTime.now();
		
		System.out.println("presentTime :"+presentTime);
		
		ZonedDateTime zonedUTC = presentTime.atZone(ZoneId.of("UTC"));
		// converting to IST
		ZonedDateTime zonedIST = zonedUTC.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
		
		int hour = zonedIST.getHour() - 5;
		int minutes = zonedIST.getMinute();
		
		System.out.println("hour : " + hour);
		System.out.println("minutes : " + minutes);
		
		System.out.println("Integer.parseInt(openTimeTokens[0]) : " + Integer.parseInt(openTimeTokens[0]));
		
		if(hour >= Integer.parseInt(openTimeTokens[0])) {
			flag = true;
			if(hour == Integer.parseInt(openTimeTokens[0]) && minutes < Integer.parseInt(openTimeTokens[1])) {
				flag = false;				
			}
			
		}
		
		if(hour <= Integer.parseInt(closeTimeTokens[0])) {
			
			flag = true;
			if(hour == Integer.parseInt(closeTimeTokens[0]) && minutes > Integer.parseInt(closeTimeTokens[1])) {
				flag = false;				
			}
		}		
		
		System.out.println("falg :" + flag);
		
		return flag;
	}
	
	public static Integer getMonth(String actualDate) {
		Integer month = null;
		
		SimpleDateFormat month_date = new SimpleDateFormat("MMM", Locale.ENGLISH);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		
		try {
			Date date = sdf.parse(actualDate);
			String month_name = month_date.format(date);
			System.out.println("Month :" + month_name);
			month = MONTHS.get(month_name.toUpperCase());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return month;
	}
	
	public static Map<String, Integer> MONTHS = new HashMap<String, Integer>();
	
	static {
		MONTHS.put("JAN", 1);
		MONTHS.put("FEB", 2);
		MONTHS.put("MAR", 3);
		MONTHS.put("APR", 4);
		MONTHS.put("MAY", 5);
		MONTHS.put("JUN", 6);
		MONTHS.put("JUL", 7);
		MONTHS.put("AUG", 8);
		MONTHS.put("SEP", 9);
		MONTHS.put("OCT", 10);
		MONTHS.put("NOV", 11);
		MONTHS.put("DEC", 12);
	}
}
