package it.govpay.gde.utils;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.govpay.gde.costanti.Costanti;

public class JpaConverterObjectMapperFactory {
	private JpaConverterObjectMapperFactory() {}
	
	
	
	private static final ObjectMapper objectMapper;
	
	static {
		objectMapper = Jackson2ObjectMapperBuilder.json()
		.dateFormat(new StdDateFormat().withColonInTimeZone(true)) // Usa StdDateFormat per supportare il fuso orario con i due punti
		.timeZone(TimeZone.getTimeZone("Europe/Rome"))
		.build();
		
		// Aggiungi moduli personalizzati se necessario
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        javaTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());
		objectMapper.registerModule(javaTimeModule);
		
		objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
		objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		objectMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID); 
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.setDateFormat(new SimpleDateFormat(Costanti.PATTERN_TIMESTAMP_3_YYYY_MM_DD_T_HH_MM_SS_SSSXXX)); 

	}

	public static ObjectMapper jpaConverterObjectMapper() {
		return objectMapper;
	}
}
