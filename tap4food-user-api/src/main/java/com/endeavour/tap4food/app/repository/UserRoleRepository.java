package com.endeavour.tap4food.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.enums.UserRoleEnum;
import com.endeavour.tap4food.app.security.model.UserRole;

@Repository
public class UserRoleRepository {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	public Optional<UserRole> findByRole(UserRoleEnum role){
		
		System.out.println("In repository role :" + role);
		Query query = new Query();
		
		query.addCriteria(Criteria.where("name").is(role.name()));
		
		System.out.println(">>>"+mongoTemplate.getCollectionNames());
		
		System.out.println("===> " + mongoTemplate.getCollection("userRoles").countDocuments());
		
		List<UserRole> roles = mongoTemplate.findAll(UserRole.class);
		
		System.out.println("Roles from DB : " + roles);
		
		UserRole userRole = mongoTemplate.findOne(query, UserRole.class);
		
		System.out.println("from DB :"+userRole);
		
		return Optional.ofNullable(userRole);
	}
}
