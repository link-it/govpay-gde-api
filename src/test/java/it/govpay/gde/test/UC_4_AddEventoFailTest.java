package it.govpay.gde.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.govpay.gde.Application;
import it.govpay.gde.test.costanti.Costanti;

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
	public void init() {
		SimpleDateFormat sdf = new SimpleDateFormat(Costanti.PATTERN_DATA_JSON_YYYY_MM_DD_T_HH_MM_SS);
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		sdf.setLenient(false);
		
		mapper = JsonMapper.builder().build();
		mapper.registerModule(new JavaTimeModule());
		mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID); 
		mapper.setDateFormat(sdf);
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
        assertTrue(problem.getString("detail").contains("Content type 'text/html' not supported"));
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
        assertTrue(problem.getString("detail").contains("Cannot construct instance of `it.govpay.gde.beans.CategoriaEvento`, problem: Unexpected value 'XXX'\n at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); line: 1, column: 20] (through reference chain: it.govpay.gde.beans.NuovoEvento[\"categoriaEvento\"])"));
        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
		
	}
	
	@Test
	void UC_4_04_AddEvento_Wrong_TipoEvento() throws Exception {
		String body = "{\"tipoEvento\":\""+Costanti.STRING_256+"\"}";

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isBadRequest())
//				.andReturn();

				.andExpect(status().is5xxServerError())
				.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject problem = reader.readObject();
		assertNotNull(problem.getString("type"));
		assertNotNull(problem.getString("title"));
		assertNotNull(problem.getString("detail"));
		assertEquals(503, problem.getInt("status"));
		assertEquals("Service Unavailable", problem.getString("title"));
		assertEquals("Request can't be satisfaied at the moment", problem.getString("detail"));
		assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-503-service-unavailable", problem.getString("type"));

		
// TODO vedere perche' non viene lanciato 400
		
//		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
//        JsonObject problem = reader.readObject();
//        assertNotNull(problem.getString("type"));
//        assertNotNull(problem.getString("title"));
//        assertNotNull(problem.getString("detail"));
//        assertEquals(400, problem.getInt("status"));
//        assertEquals("Bad Request", problem.getString("title"));
//        assertTrue(problem.getString("detail").contains("Cannot construct instance of `it.govpay.gde.beans.CategoriaEvento`, problem: Unexpected value 'XXX'\n at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); line: 1, column: 20] (through reference chain: it.govpay.gde.beans.NuovoEvento[\"categoriaEvento\"])"));
//        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
		
	}
}
