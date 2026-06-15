package it.govpay.gde.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

import it.govpay.gde.costanti.Costanti;

public class OffsetDateTimeDeserializer extends StdScalarDeserializer<OffsetDateTime> {

	private transient Logger logger = LoggerFactory.getLogger(OffsetDateTimeDeserializer.class);

	private static final long serialVersionUID = 1L;

	private transient DateTimeFormatter formatterMillis = DateTimeFormatter.ofPattern(Costanti.PATTERN_YYYY_MM_DD_T_HH_MM_SS_MILLIS_VARIABILI_XXX, Locale.getDefault());

	public OffsetDateTimeDeserializer() {
		super(OffsetDateTime.class);
	}

	@Override
	public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
		JsonToken currentToken = jsonParser.currentToken();
		if (currentToken == JsonToken.VALUE_STRING) {
			String text = jsonParser.getText();
			try {
				return parseOffsetDateTime(text, this.formatterMillis);
			} catch (DateTimeParseException e) {
				throw deserializationContext.weirdStringException(text, OffsetDateTime.class, e.getMessage());
			}
		} else {
			return null;
		}
	}

	public OffsetDateTime parseOffsetDateTime(String value, DateTimeFormatter formatter)  {
		if (value != null && !value.trim().isEmpty()) {
			String dateString = value.trim();
			logger.debug("dateString: {}" , dateString);
			try {
				return OffsetDateTime.parse(dateString, formatter);
			}catch (DateTimeParseException e) {
				logger.warn("Parsing OffsetDateTime fallito, tentativo come LocalDateTime: {}", e.getMessage());
				ZoneOffset offset = ZoneOffset.ofHoursMinutes(1, 0); // CET (Central European Time)
				LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
				if (localDateTime != null) {
					return OffsetDateTime.of(localDateTime, offset);
				}
			}
		}

		return null;
	}
}
