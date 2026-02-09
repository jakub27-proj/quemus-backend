package com.niebo.quemus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class QuemusApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuemusApplication.class, args);
	}

}
