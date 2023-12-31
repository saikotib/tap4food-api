package com.endeavour.tap4food.user.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan("com.endeavour.tap4food")
public class UserApplication {

	public static void main(String[] args) {

		SpringApplication.run(UserApplication.class, args);	
	}
}
