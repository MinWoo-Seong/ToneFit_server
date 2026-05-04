package com.example.ToneFit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ToneFitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToneFitApplication.class, args);
	}

}
