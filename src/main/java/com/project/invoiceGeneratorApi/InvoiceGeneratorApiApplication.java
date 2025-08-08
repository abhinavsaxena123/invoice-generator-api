package com.project.invoiceGeneratorApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class InvoiceGeneratorApiApplication {

	public static void main(String[] args) {

		SpringApplication.run(InvoiceGeneratorApiApplication.class, args);
	}

}
