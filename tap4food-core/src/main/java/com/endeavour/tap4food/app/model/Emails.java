package com.endeavour.tap4food.app.model;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_EMAILS)
public class Emails {

	@Id
	private String id;
	
	private String toAddress;
	
	private String subject;
	
	private String mailBody;
	
	private String status;
	
	private ZonedDateTime entryTime;
	
	private ZonedDateTime updatedTime;
	
	private String comments;
}
