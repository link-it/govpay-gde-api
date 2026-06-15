package it.govpay.gde.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import it.govpay.gde.Application;
import it.govpay.gde.test.costanti.Costanti;
import it.govpay.gde.utils.OffsetDateTimeDeserializer;
import it.govpay.gde.utils.OffsetDateTimeSerializer;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test Lettura Eventi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ActiveProfiles("test")
class UC_4_AddEventoFailTest {


	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper mapper;

	@BeforeEach
	void init() {
		SimpleDateFormat sdf = new SimpleDateFormat(Costanti.PATTERN_TIMESTAMP_3_YYYY_MM_DD_T_HH_MM_SS_SSSXXX);
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		sdf.setLenient(false);

		SimpleModule offsetDateTimeModule = new SimpleModule();
		offsetDateTimeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
		offsetDateTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());

		mapper = JsonMapper.builder()
				.addModule(offsetDateTimeModule)
				.enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
				.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
				.enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
				.enable(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID)
				.defaultDateFormat(sdf)
				.build();
	}

	@Test
	void UC_4_01_AddEvento_NoBody() throws Exception {
		String body = "";

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        JsonObject problem = reader.readObject();
        assertNotNull(problem.getString("type"));
        assertNotNull(problem.getString("title"));
        assertNotNull(problem.getString("detail"));
        assertEquals(400, problem.getInt("status"));
        assertEquals("Bad Request", problem.getString("title"));
        assertTrue(problem.getString("detail").contains("Required request body is missing"));
        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
		
		
	}
	
	@Test
	void UC_4_02_AddEvento_WrongContentType() throws Exception {
		String body = "{}";

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.TEXT_HTML))
				.andExpect(status().isBadRequest())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        JsonObject problem = reader.readObject();
        assertNotNull(problem.getString("type"));
        assertNotNull(problem.getString("title"));
        assertNotNull(problem.getString("detail"));
        assertEquals(400, problem.getInt("status"));
        assertEquals("Bad Request", problem.getString("title"));
        assertTrue(problem.getString("detail").contains("Content-Type 'text/html' is not supported"));
        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
		
	}
	
	@Test
	void UC_4_03_AddEvento_Wrong_CategoriaEvento() throws Exception {
		String body = "{\"categoriaEvento\":\"XXX\"}";

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        JsonObject problem = reader.readObject();
        assertNotNull(problem.getString("type"));
        assertNotNull(problem.getString("title"));
        assertNotNull(problem.getString("detail"));
        assertEquals(400, problem.getInt("status"));
        assertEquals("Bad Request", problem.getString("title"));
        assertTrue(problem.getString("detail").contains("Cannot construct instance of `it.govpay.gde.beans.CategoriaEvento`, problem: Unexpected value 'XXX'"));
        assertTrue(problem.getString("detail").contains("through reference chain: it.govpay.gde.beans.NuovoEvento[\"categoriaEvento\"]"));
        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
		
	}
	
	@Test
	void UC_4_04_AddEvento_Wrong_TipoEvento() throws Exception {
		String body = "{\"tipoEvento\":\""+Costanti.STRING_256+"\"}";

		// tipoEvento di 256 caratteri: con la Bean Validation attiva (Spring Boot 4)
		// viola @Size(max = 255) sul body -> 400 (prima generava un errore DB -> 503)
		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject problem = reader.readObject();
		assertNotNull(problem.get("type"));
		assertNotNull(problem.get("title"));
		assertNotNull(problem.get("detail"));
		assertEquals(400, problem.getInt("status"));
		assertEquals("Bad Request", problem.getString("title"));
		assertTrue(problem.getString("detail").contains("size must be between 0 and 255"));
		assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
	}
}
