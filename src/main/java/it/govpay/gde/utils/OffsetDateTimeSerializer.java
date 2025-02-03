package it.govpay.gde.utils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import it.govpay.gde.costanti.Costanti;


public class OffsetDateTimeSerializer extends StdScalarSerializer<OffsetDateTime> {

	private static final long serialVersionUID = 1L;

	private transient DateTimeFormatter formatterMillis = DateTimeFormatter.ofPattern(Costanti.PATTERN_TIMESTAMP_3_YYYY_MM_DD_T_HH_MM_SS_SSSXXX, Locale.getDefault());

	public OffsetDateTimeSerializer() {
		super(OffsetDateTime.class);
	}

	@Override
	public void serialize(OffsetDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
		String dateTimeAsString = dateTime != null ? this.formatterMillis.format(dateTime) : null;
		jsonGenerator.writeString(dateTimeAsString);
	}

}
