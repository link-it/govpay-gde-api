package it.govpay.gde.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

import it.govpay.common.utils.DateTimePatterns;

/**
 * Legge date e ora in formato ISO 8601, con secondi e frazioni di secondo
 * facoltativi. Il fuso, se presente, e' mantenuto; altrimenti la data e' in
 * ora {@value DateTimePatterns#DEFAULT_TIME_ZONE}, con l'ora legale.
 */
public class OffsetDateTimeDeserializer extends StdScalarDeserializer<OffsetDateTime> {

	private static final long serialVersionUID = 1L;

	public OffsetDateTimeDeserializer() {
		super(OffsetDateTime.class);
	}

	@Override
	public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
		JsonToken currentToken = jsonParser.currentToken();
		if (currentToken == JsonToken.VALUE_STRING) {
			String text = jsonParser.getText();
			try {
				return parseOffsetDateTime(text);
			} catch (DateTimeParseException e) {
				throw deserializationContext.weirdStringException(text, OffsetDateTime.class, e.getMessage());
			}
		} else {
			return null;
		}
	}

	public OffsetDateTime parseOffsetDateTime(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		TemporalAccessor data = DateTimeFormatter.ISO_DATE_TIME.parseBest(value.trim(), OffsetDateTime::from, LocalDateTime::from);
		if (data instanceof OffsetDateTime conFuso) {
			return conFuso;
		}
		return ((LocalDateTime) data).atZone(ZoneId.of(DateTimePatterns.DEFAULT_TIME_ZONE)).toOffsetDateTime();
	}
}
