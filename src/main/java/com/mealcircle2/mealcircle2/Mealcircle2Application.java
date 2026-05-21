package com.mealcircle2.mealcircle2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Mealcircle2Application {

	public static void main(String[] args) {
		SpringApplication.run(Mealcircle2Application.class, args);
	}

}
