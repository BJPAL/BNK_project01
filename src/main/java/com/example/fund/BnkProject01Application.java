package com.example.fund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BnkProject01Application {

	public static void main(String[] args) {
		SpringApplication.run(BnkProject01Application.class, args);
	}

}
