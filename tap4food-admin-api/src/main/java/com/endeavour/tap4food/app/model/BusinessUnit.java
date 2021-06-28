package com.endeavour.tap4food.app.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.enums.BusinessUnitEnum;

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
	
	private String name;
	
	private String logo;
	
	private BusinessUnitEnum businesUnit;
	
	@DBRef
	private List<Merchant> merchants;
}

