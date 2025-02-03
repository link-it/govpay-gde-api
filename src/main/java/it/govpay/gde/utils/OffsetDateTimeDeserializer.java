package it.govpay.gde.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import it.govpay.gde.costanti.Costanti;

public class OffsetDateTimeDeserializer extends StdScalarDeserializer<OffsetDateTime> {

	private transient Logger logger = LoggerFactory.getLogger(OffsetDateTimeDeserializer.class);

	private static final long serialVersionUID = 1L;

	private transient DateTimeFormatter formatterMillis = DateTimeFormatter.ofPattern(Costanti.PATTERN_YYYY_MM_DD_T_HH_MM_SS_MILLIS_VARIABILI_XXX, Locale.getDefault());

	public OffsetDateTimeDeserializer() {
		super(OffsetDateTime.class);
	}

	@Override
	public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		try {
			JsonToken currentToken = jsonParser.getCurrentToken();
			if (currentToken == JsonToken.VALUE_STRING) {
				return parseOffsetDateTime(jsonParser.getText(), this.formatterMillis);
			} else {
				return null;
			}
		} catch (IOException | DateTimeParseException e) {
			throw new IOException(e);
		}
	}

	public OffsetDateTime parseOffsetDateTime(String value, DateTimeFormatter formatter)  {
		if (value != null && !value.trim().isEmpty()) {
			String dateString = value.trim();
			logger.debug("dateString: {}" , dateString);
			try {
				return OffsetDateTime.parse(dateString, formatter);
			}catch (DateTimeParseException e) {
				logger.error("Error parsing date: " + e.getMessage(), e);
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
