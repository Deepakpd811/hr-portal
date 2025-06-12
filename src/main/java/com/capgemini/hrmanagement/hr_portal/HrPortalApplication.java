package com.capgemini.hrmanagement.hr_portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HrPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrPortalApplication.class, args);
	}

}
