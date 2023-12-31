package com.endeavour.tap4food.app.model;

import java.util.List;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.enums.BusinessUnitEnum;
import com.endeavour.tap4food.app.enums.BusinessUnitTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "businessUnits")
public class BusinessUnit {

	@Id
	private String id;
	
	private Long businessUnitId;
	
	private String name;
	
	private String logo;
	
	private String type;
	
	private String address;
	
	private String pincode;
	
	private String city;
	
	private String state;
	
	private String country;
	
	private String status;   // ACTIVE, INACTIVE, DELETED

	@Transient
	public static final String SEQUENCE = "businessUnit_Seq";
}

