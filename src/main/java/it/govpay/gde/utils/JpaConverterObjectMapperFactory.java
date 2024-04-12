package it.govpay.gde.utils;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JpaConverterObjectMapperFactory {
	private JpaConverterObjectMapperFactory() {}
	
	private static final String PATTERN_DATA_JSON_YYYY_MM_DD_T_HH_MM_SS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	private static final ObjectMapper objectMapper;
	
	static {
		objectMapper = Jackson2ObjectMapperBuilder.json()
		.dateFormat(new StdDateFormat().withColonInTimeZone(true)) // Usa StdDateFormat per supportare il fuso orario con i due punti
		.timeZone(TimeZone.getTimeZone("Europe/Rome"))
		.build();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
		objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		objectMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID); 
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.setDateFormat(new SimpleDateFormat(PATTERN_DATA_JSON_YYYY_MM_DD_T_HH_MM_SS_SSS_Z)); 

	}

	public static ObjectMapper jpaConverterObjectMapper() {
		return objectMapper;
	}
}
