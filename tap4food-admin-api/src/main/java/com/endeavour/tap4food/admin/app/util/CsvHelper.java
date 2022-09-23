package com.endeavour.tap4food.admin.app.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import com.endeavour.tap4food.admin.app.security.model.User;

public class CsvHelper {

	public static ByteArrayInputStream usersToCsv(List<User> users) {
		final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {

			List<String> header = Arrays.asList(
					"S. No",
					"Phone Number",
					"Full Name", 
					"Email"
					);
			
			csvPrinter.printRecord(header);
			int i = 1;
			for (User user : users) {
				List<String> data = Arrays.asList( 
						String.valueOf(i), String.valueOf(user.getPhoneNumber()), user.getFullName(), user.getEmail());
				csvPrinter.printRecord(data);
				i++;
			}
			csvPrinter.flush();
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("fail to import data to CSV file: " + e.getMessage());
		}
	}
	
}
