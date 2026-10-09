package com.ashraf.jobapptracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class JobAppTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobAppTrackerApplication.class, args);
	}

}
