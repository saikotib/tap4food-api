package com.endeavour.tap4food.app.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "foodCourts")
public class FoodCourt {

	@Id
	private String id;
	
	private Long foodCourtId;
	
	private Long businessUnitId;
	
	private String name;
	
	private String logo;
	
	private String qrCodeUrl;
	
	private boolean isQRCodeGenerated;
	
	@Transient
	public static final String SEQ_NAME = "foodCourt_sequence";
}
