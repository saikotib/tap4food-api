package com.endeavour.tap4food.admin.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan("com.endeavour.tap4food")
public class AdminApplication {

	public static void main(String[] args) {

		SpringApplication.run(AdminApplication.class, args);
	}

}
