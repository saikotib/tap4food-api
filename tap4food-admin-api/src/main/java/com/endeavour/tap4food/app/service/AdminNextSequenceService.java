package com.endeavour.tap4food.app.service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.sequences.AdminRoleSequence;

@Service
public class AdminNextSequenceService {

	@Autowired
	private MongoOperations mongo;

	public long getNextSequence(String seqName) {
		AdminRoleSequence counter = mongo.findAndModify(query(where("_id").is(seqName)), new Update().inc("seq", 1),
				options().returnNew(true).upsert(true), AdminRoleSequence.class);
		return counter.getSeq();
	}
}
