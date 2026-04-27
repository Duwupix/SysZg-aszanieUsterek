package com.usterki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SystemUsterekApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemUsterekApplication.class, args);
	}
}
