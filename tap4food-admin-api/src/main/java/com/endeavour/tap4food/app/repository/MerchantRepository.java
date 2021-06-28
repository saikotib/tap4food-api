package com.endeavour.tap4food.app.repository;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.endeavour.tap4food.app.model.UniqueNumber;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Filters.*;

@Repository
public class MerchantRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Transactional
	public synchronized Long getRecentUniqueNumber() {
		
		 MongoCollection<Document> collection = mongoTemplate.getCollection("merchantUniqueNumbers");

		 Bson sort = descending("uniqueNumber");
		 
		 FindIterable<Document> iterdoc = collection.find().sort(sort);
		
		 Document document = iterdoc.first();
		 
		 String maxval = String.valueOf(document.get("uniqueNumber"));
		 
		 Long nextMaxVal = Long.valueOf(maxval) + 1;
		 
		 System.out.println("Max Value : " + maxval);
		 
		 UniqueNumber uniqueNumber = new UniqueNumber();
		 uniqueNumber.setUniqueNumber(nextMaxVal);
		 
		 mongoTemplate.save(uniqueNumber);
		 
		return nextMaxVal;
	}
}
