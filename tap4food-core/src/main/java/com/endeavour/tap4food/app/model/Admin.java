package com.endeavour.tap4food.app.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = MongoCollectionConstant.COLLECTION_ADMIN_USERS)
public class Admin {

	@Id
	private String id;

	private String userName;
	
	private String password;
	
	private String phoneNumber;
	
	private String email;
}
