package com.endeavour.tap4food.admin.app.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.admin.app.repository.AdminRepository;
import com.endeavour.tap4food.admin.app.security.model.User;
import com.endeavour.tap4food.admin.app.util.CsvHelper;

@Service
public class CSVService {
	
	@Autowired
	private AdminRepository adminRepository;

	public ByteArrayInputStream getCustomersStream() {
		
		List<User> users = adminRepository.getUsers();
		
		ByteArrayInputStream stream = CsvHelper.usersToCsv(users);
		
		return stream;
	}
}
