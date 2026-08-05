package it.govpay.gde.test;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govpay.gde.Application;
import it.govpay.gde.test.costanti.Costanti;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

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
				.param("total", "true")
				.accept("application/hal+json"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(2)));
	}

	@Test
	void UC_1_02_FindAll_ByData() throws Exception {
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("dataDa", "2022-12-03T10:15:30+01:00[Europe/Rome]")
								.param("dataA",  "2023-12-03T10:15:30+01:00[Europe/Rome]")
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)));
	}

	@Test
	void UC_1_03_FindAll_ByIdDominio() throws Exception {
		String parametro = Costanti.ID_DOMINIO_1;
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("idDominio", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].idDominio", is(parametro)));
	}

	@Test
	void UC_1_03_02_FindAll_ByIdDominio_Multiplo() throws Exception {
		// idDominio ripetuto: deve comportarsi come un OR (IN) tra i valori,
		// una singola chiamata copre l'ACL multi-dominio del chiamante.
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("idDominio", Costanti.ID_DOMINIO_1, "99999999999")
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].idDominio", is(Costanti.ID_DOMINIO_1)));

		// nessuno dei domini richiesti presente -> nessun risultato
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("idDominio", "11111111111", "99999999999")
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}

	@Test
	void UC_1_04_FindAll_ByIuv() throws Exception {
		String parametro = "45678012345123456";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("iuv", parametro)
								.param("total", "true")
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
								.param("total", "true")
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
								.param("total", "true")
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
								.param("total", "true")
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
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].categoriaEvento", is(parametro)));

		parametro = "INTERNO";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("categoriaEvento", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].categoriaEvento", is(parametro)));

		parametro = "UTENTE";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("categoriaEvento", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}

	@Test
	void UC_1_09_FindAll_ByEsito() throws Exception {
		String parametro = "OK";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("esito", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					.andExpect(jsonPath("$.items[0].esito", is(parametro)))
					.andExpect(jsonPath("$.items[1].esito", is(parametro)));

		parametro = "KO";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("esito", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));

		parametro = "FAIL";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("esito", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}

	@Test
	void UC_1_10_FindAll_ByRuolo() throws Exception {
		String parametro = "SERVER";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("ruolo", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					.andExpect(jsonPath("$.items[0].ruolo", is(parametro)))
					.andExpect(jsonPath("$.items[1].ruolo", is(parametro)));

		parametro = "CLIENT";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("ruolo", parametro)
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}

	@Test
	void UC_1_11_FindAll_BySottotipoEvento() throws Exception {
		String parametro = "sottotipoEvento_1";
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("sottotipoEvento", parametro)
								.param("total", "true")
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
								.param("total", "true")
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
								.param("total", "true")
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
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(1)))
					.andExpect(jsonPath("$.items[0].componente", is(parametro)));
	}

	@Test
	void UC_1_14_02_FindAll_ByMessaggi() throws Exception {
		// full-text case-insensitive su sottotipoEsito/dettaglioEsito: entrambe le
		// righe di fixture hanno sottotipoEsito="200"
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("messaggi", "20")
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)));

		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("messaggi", "nessun-match-possibile")
								.param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(0)));
	}

	@Test
	void UC_1_15_FindAll_OffsetLimit() throws Exception {
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				 .param("offset", "0")
				 .param("limit",  "1")
				 .param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					;

		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				 .param("offset", "1")
				 .param("limit",  "1")
				 .param("total", "true")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total", is(2)))
					;
	}

	@Test
	void UC_1_16_FindAll_OffsetLimit_KO() throws Exception {
		// offset negativo: con la Bean Validation attiva (Spring Boot 4) viola @Min(0) -> 400
		MvcResult resultOffset = this.mockMvc.perform(get(Costanti.EVENTI_PATH)
				 .param("offset", "-1")
				 .param("limit",  "1")
								)
					.andExpect(status().isBadRequest())
					.andReturn();
		JsonReader readerOffset = Json.createReader(new ByteArrayInputStream(resultOffset.getResponse().getContentAsByteArray()));
		JsonObject problemOffset = readerOffset.readObject();
		assertNotNull(problemOffset.getString("type"));
		assertNotNull(problemOffset.getString("title"));
		assertNotNull(problemOffset.getString("detail"));
		assertEquals(400, problemOffset.getInt("status"));
		assertEquals("Bad Request", problemOffset.getString("title"));
		assertTrue(problemOffset.getString("detail").contains("offset"));
		assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problemOffset.getString("type"));

		// limit non positivo: viola @Min(1) -> 400
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
        assertTrue(problem.getString("detail").contains("limit"));
        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request", problem.getString("type"));
	}

	@Test
	void UC_1_17_FindAll_DefaultNoTotal() throws Exception {
		// senza ?total=true: niente COUNT, page.total assente, ma hasNext presente
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("limit", "1")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.total").doesNotExist())
					.andExpect(jsonPath("$.page.hasNext", is(true)))
					.andExpect(jsonPath("$.items.length()", is(1)));

		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("offset", "1")
								.param("limit", "1")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.hasNext", is(false)));
	}

	@Test
	void UC_1_18_FindAll_CursorMode() throws Exception {
		// prima pagina: nessun cursorData/cursorId, ordina data DESC quindi
		// il primo item e' quello piu' recente (2024-02-06, componente API_PENDENZE)
		MvcResult first = this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("pagingMode", "CURSOR")
								.param("limit", "1")
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.hasNext", is(true)))
					.andExpect(jsonPath("$.items[0].componente", is("API_PENDENZE")))
					.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(first.getResponse().getContentAsByteArray()));
		JsonObject firstBody = reader.readObject();
		JsonObject firstItem = firstBody.getJsonArray("items").getJsonObject(0);
		String dataEvento = firstItem.getString("dataEvento");
		long id = firstItem.getJsonNumber("id").longValue();

		// seconda pagina: usando cursorData/cursorId dell'ultimo item ricevuto,
		// arriva l'evento piu' vecchio (2023-01-06, componente API_BACKOFFICE)
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("pagingMode", "CURSOR")
								.param("limit", "1")
								.param("cursorData", dataEvento)
								.param("cursorId", Long.toString(id))
								)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.page.hasNext", is(false)))
					.andExpect(jsonPath("$.items[0].componente", is("API_BACKOFFICE")));
	}

	@Test
	void UC_1_19_FindAll_CursorMode_ValidazioneParametri_KO() throws Exception {
		// cursorData senza pagingMode=CURSOR -> 400
		MvcResult result = this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("cursorData", "2024-02-06T10:01:53.238+01:00")
								)
					.andExpect(status().isBadRequest())
					.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject problem = reader.readObject();
		assertEquals(400, problem.getInt("status"));

		// pagingMode=CURSOR con solo cursorId (senza cursorData) -> 400
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
								.param("pagingMode", "CURSOR")
								.param("cursorId", "1")
								)
					.andExpect(status().isBadRequest());
	}
}
