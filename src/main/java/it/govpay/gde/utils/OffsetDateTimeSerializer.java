package it.govpay.gde.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdScalarSerializer;

import it.govpay.gde.costanti.Costanti;


public class OffsetDateTimeSerializer extends StdScalarSerializer<OffsetDateTime> {

	private static final long serialVersionUID = 1L;

	private transient DateTimeFormatter formatterMillis = DateTimeFormatter.ofPattern(Costanti.PATTERN_TIMESTAMP_3_YYYY_MM_DD_T_HH_MM_SS_SSSXXX, Locale.getDefault());

	public OffsetDateTimeSerializer() {
		super(OffsetDateTime.class);
	}

	@Override
	public void serialize(OffsetDateTime dateTime, JsonGenerator jsonGenerator, SerializationContext provider) throws JacksonException {
		String dateTimeAsString = dateTime != null ? this.formatterMillis.format(dateTime) : null;
		jsonGenerator.writeString(dateTimeAsString);
	}

}
