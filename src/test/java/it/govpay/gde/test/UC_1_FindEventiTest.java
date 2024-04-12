package it.govpay.gde.test;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govpay.gde.Application;
import it.govpay.gde.test.costanti.Costanti;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test Lettura Eventi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ActiveProfiles("test")
class UC_1_FindEventiTest {
	
	@Autowired
	private MockMvc mockMvc;

	@Test
	void UC_1_01_FindAll_NoParametri() throws Exception {
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				.accept("application/hal+json"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(2)));
	}

	@Test
	void UC_1_02_FindAll_ByData() throws Exception {
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("dataDa", "2022-12-03T10:15:30+01:00[Europe/Rome]")	
								.param("dataA",  "2023-12-03T10:15:30+01:00[Europe/Rome]")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)));
	}
	
	@Test
	void UC_1_03_FindAll_ByIdDominio() throws Exception {
		String parametro = Costanti.ID_DOMINIO_1;
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("idDominio", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].idDominio", is(parametro)));
	}
	
	@Test
	void UC_1_04_FindAll_ByIuv() throws Exception {
		String parametro = "45678012345123456";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("iuv", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].iuv", is(parametro)));
	}
	
	@Test
	void UC_1_05_FindAll_ByCcp() throws Exception {
		String parametro = "1234561234576";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("ccp", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].ccp", is(parametro)));
	}
	
	@Test
	void UC_1_06_FindAll_ByIdA2A() throws Exception {
		String parametro = "idA2A01";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("idA2A", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].idA2A", is(parametro)));
	}
	
	@Test
	void UC_1_07_FindAll_ByIdPendenza() throws Exception {
		String parametro = "idPendenza_01";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("idPendenza", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].idPendenza", is(parametro)));
	}
	
	@Test
	void UC_1_08_FindAll_ByCategoriaEvento() throws Exception {
		String parametro = "INTERFACCIA";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("categoriaEvento", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].categoriaEvento", is(parametro)));
		
		parametro = "INTERNO";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("categoriaEvento", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].categoriaEvento", is(parametro)));
		
		parametro = "UTENTE";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("categoriaEvento", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}
	
	@Test
	void UC_1_09_FindAll_ByEsito() throws Exception {
		String parametro = "OK";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("esito", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					.andExpect(jsonPath("$.items[0].esito", is(parametro)))
					.andExpect(jsonPath("$.items[1].esito", is(parametro)));
		
		parametro = "KO";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("esito", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
		
		parametro = "FAIL";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("esito", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}
	
	@Test
	void UC_1_10_FindAll_ByRuolo() throws Exception {
		String parametro = "SERVER";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("ruolo", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					.andExpect(jsonPath("$.items[0].ruolo", is(parametro)))
					.andExpect(jsonPath("$.items[1].ruolo", is(parametro)));
		
		parametro = "CLIENT";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("ruolo", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}
	
	@Test
	void UC_1_11_FindAll_BySottotipoEvento() throws Exception {
		String parametro = "sottotipoEvento_1";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("sottotipoEvento", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].sottotipoEvento", is(parametro)));
	}
	
	@Test
	void UC_1_12_FindAll_ByTipoEvento() throws Exception {
		String parametro = "getPendenza";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("tipoEvento", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].tipoEvento", is(parametro)));
	}
	
	@Test
	void UC_1_13_FindAll_BySeverita() throws Exception {
		String parametro = "3";
		int parametroCheck = 3;
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("severitaDa", parametro)
								.param("severitaA", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].severita", is(parametroCheck)));
	}
	
	@Test
	void UC_1_14_FindAll_ByComponente() throws Exception {
		String parametro = "API_PENDENZE";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("componente", parametro)
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].componente", is(parametro)));
	}
	
	@Test
	void UC_1_15_FindAll_OffsetLimit() throws Exception {
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				 .param("offset", "0")
				 .param("limit",  "1")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					;
		
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				 .param("offset", "1")
				 .param("limit",  "1")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					;
	}
	
	@Test
	void UC_1_16_FindAll_OffsetLimit_KO() throws Exception {
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				 .param("offset", "-1")
				 .param("limit",  "1")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					;
		
		
		MvcResult result = this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				 .param("offset", "1")
				 .param("limit",  "-1")
								)
        		.andExpect(status().isBadRequest())
        		.andReturn();
        JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        JsonObject problem = reader.readObject();
        assertNotNull(problem.getString("type"));
        assertNotNull(problem.getString("title"));
        assertNotNull(problem.getString("detail"));
        assertEquals(400, problem.getInt("status"));
        assertEquals("Bad Request", problem.getString("title"));
        assertEquals("Limit must be > 0", problem.getString("detail"));
        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
	}
}
