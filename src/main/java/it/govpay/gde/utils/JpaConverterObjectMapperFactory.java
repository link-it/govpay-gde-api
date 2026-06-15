package it.govpay.gde.utils;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import it.govpay.gde.costanti.Costanti;

public class JpaConverterObjectMapperFactory {
	private JpaConverterObjectMapperFactory() {}



	private static final ObjectMapper objectMapper;

	static {
		// Formato delle date con fuso orario Europe/Rome (i due punti nel timezone
		// sono gestiti dal pattern XXX di SimpleDateFormat)
		SimpleDateFormat dateFormat = new SimpleDateFormat(Costanti.PATTERN_TIMESTAMP_3_YYYY_MM_DD_T_HH_MM_SS_SSSXXX);
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

		// Aggiungi moduli personalizzati per OffsetDateTime
		SimpleModule offsetDateTimeModule = new SimpleModule();
		offsetDateTimeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
		offsetDateTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());

		// In Jackson 3 l'ObjectMapper e' immutabile: ogni configurazione va impostata sul builder
		objectMapper = JsonMapper.builder()
				.addModule(offsetDateTimeModule)
				.enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
				.enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
				.enable(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID)
				.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
				.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.ALWAYS))
				.defaultDateFormat(dateFormat)
				.defaultTimeZone(TimeZone.getTimeZone("Europe/Rome"))
				.build();
	}

	public static ObjectMapper jpaConverterObjectMapper() {
		return objectMapper;
	}
}
