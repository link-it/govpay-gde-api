package it.govpay.gde.config;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.govpay.gde.utils.OffsetDateTimeDeserializer;
import it.govpay.gde.utils.OffsetDateTimeSerializer;


@Configuration
public class WebConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Crea un ObjectMapper con configurazioni personalizzate
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Imposta il formato delle date
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        
        // Disabilita la serializzazione delle date come timestamp
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // Aggiungi moduli personalizzati se necessario
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        javaTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());
		objectMapper.registerModule(javaTimeModule);
        
        return objectMapper;
    }
}
