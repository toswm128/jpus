package com.minsu.jpus;

import com.minsu.jpus.basic.GreetingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpusApplication {

	public static void main(String[] args) {

		var context = SpringApplication.run(JpusApplication.class, args);

		GreetingService greetingService = context.getBean(GreetingService.class);
		System.out.println(greetingService.greet("minsu"));
	}
}
