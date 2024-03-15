package it.govpay.gde;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Value("${gde.time-zone:Europe/Rome}")
	String timeZone;
	
	/**
	 * Impstazione del timezone nel mapper
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder ->  builder.
				timeZone(this.timeZone);
	}
}

