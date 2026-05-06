package com.staffs.leaveservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:text.properties")
public class LeaveServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(LeaveServiceApplication.class, args);
	}
}
