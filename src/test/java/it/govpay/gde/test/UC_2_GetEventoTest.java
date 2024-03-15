package it.govpay.gde.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
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
class UC_2_GetEventoTest {
	
	
	@Autowired
	private MockMvc mockMvc;

	@Test
	void UC_2_01_GetEventoOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.EVENTI_PATH))
					.andExpect(status().isOk()).andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        JsonObject userList = reader.readObject();
        JsonArray itemsList = userList.getJsonArray("items");
        
        JsonObject firstItem = itemsList.get(0).asJsonObject();
        int idEvento = firstItem.getInt("id");
        
        result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
        		.andExpect(status().isOk())
        		.andReturn();
        reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        JsonObject eventoDetail = reader.readObject();
        
        assertNotNull(eventoDetail);
        assertEquals(idEvento, eventoDetail.getInt("id"));
        assertEquals("API_PENDENZE", eventoDetail.getString("componente"));
        assertEquals("INTERFACCIA", eventoDetail.getString("categoriaEvento"));
        assertEquals("SERVER", eventoDetail.getString("ruolo"));
        assertEquals("getPendenza", eventoDetail.getString("tipoEvento"));
        assertEquals("OK", eventoDetail.getString("esito"));
        assertEquals("2024-02-06T10:01:53.238+01:00", eventoDetail.getString("dataEvento"));
        assertEquals(77, eventoDetail.getInt("durataEvento"));
        assertEquals("sottotipoEvento_1", eventoDetail.getString("sottotipoEvento"));
        assertEquals("200", eventoDetail.getString("sottotipoEsito"));
        assertEquals("12345678901", eventoDetail.getString("idDominio"));
        assertEquals("45678012345123456", eventoDetail.getString("iuv"));
        assertEquals("1234561234576", eventoDetail.getString("ccp"));
        assertEquals(3, eventoDetail.getInt("severita"));
        assertEquals("GovPay", eventoDetail.getString("clusterId"));
        assertEquals("fb695ba5-dbcb-4e11-bcf6-561bce720521", eventoDetail.getString("transactionId"));
        assertNull(eventoDetail.get("parametriRichiesta"));
        assertNull(eventoDetail.get("parametriRisposta"));
        assertNull(eventoDetail.get("datiPagoPA"));
        
	}
	
	@Test
	void UC_2_02_GetEvento_NotFound() throws Exception {
        int idEvento = Integer.MAX_VALUE;
        
        MvcResult result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
        		.andExpect(status().isNotFound())
        		.andReturn();
        JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        JsonObject problem = reader.readObject();
        assertNotNull(problem.getString("type"));
        assertNotNull(problem.getString("title"));
        assertNotNull(problem.getString("detail"));
        assertEquals(404, problem.getInt("status"));
        assertEquals("Not Found", problem.getString("title"));
        assertEquals("Risorsa non trovata", problem.getString("detail"));
        assertEquals("https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found", problem.getString("type"));
	}
}
