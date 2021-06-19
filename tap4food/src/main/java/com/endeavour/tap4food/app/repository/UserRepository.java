package com.endeavour.tap4food.app.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.security.model.User;

@Repository
public class UserRepository {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	public Optional<User> findByUserName(String userName){
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(userName));
		
		User user = mongoTemplate.findOne(query, User.class);
		
		return Optional.ofNullable(user);
	}
	
	public void save(User user) {
		mongoTemplate.save(user);
	}
}
