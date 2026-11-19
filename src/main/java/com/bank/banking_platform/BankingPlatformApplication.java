package com.bank.banking_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bank.banking_platform")
public class BankingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingPlatformApplication.class, args);
	}

}
