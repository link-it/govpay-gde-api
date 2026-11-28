package it.govpay.gde.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govpay.gde.Application;
import it.govpay.gde.test.costanti.Costanti;
import jakarta.json.Json;
import jakarta.json.JsonObject;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test Date degli Eventi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ActiveProfiles("test")
class UC_6_DateEventoTest {

	private static final ZoneId ROMA = ZoneId.of("Europe/Rome");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@ParameterizedTest(name = "{0} salvata come {1}")
	@DisplayName("Le date sono salvate in ora Europe/Rome e rilette con il relativo fuso")
	@CsvSource({
			"2026-07-15T00:30:00.000+02:00, 2026-07-15T00:30:00",
			"2026-07-14T22:30:00.614239Z,   2026-07-15T00:30:00.614239",
			"2026-07-14T22:30:00Z,          2026-07-15T00:30:00",
			"2026-07-14T22:30Z,             2026-07-15T00:30:00",
			"2026-07-15T04:00:00+05:30,     2026-07-15T00:30:00",
			"2026-01-14T23:30:00Z,          2026-01-15T00:30:00",
			"2026-01-15T00:30:00+01:00,     2026-01-15T00:30:00",
			"2026-07-15T00:30:00,           2026-07-15T00:30:00",
			"2026-07-15T00:30:00.000,       2026-07-15T00:30:00",
			"2026-07-15T00:30,              2026-07-15T00:30:00",
			"2026-01-15T00:30:00,           2026-01-15T00:30:00" })
	void dateSalvateInOraItaliana(String ricevuta, LocalDateTime attesa) throws Exception {
		String location = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(evento(ricevuta, ricevuta, ricevuta))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");
		long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		assertEquals(attesa, this.jdbcTemplate.queryForObject("select data from eventi where id = ?",
				(rs, i) -> rs.getObject(1, LocalDateTime.class), id));
		assertEquals(attesa, LocalDateTime.parse(parametri("parametri_richiesta", id).getString("dataOraRichiesta")));
		assertEquals(attesa, LocalDateTime.parse(parametri("parametri_risposta", id).getString("dataOraRisposta")));

		MvcResult result = this.mockMvc.perform(get(Costanti.EVENTO_PATH, id))
				.andExpect(status().isOk())
				.andReturn();
		JsonObject letto = json(result.getResponse().getContentAsByteArray());
		String risposta = attesa.atZone(ROMA).toOffsetDateTime().format(Costanti.DEFAULT_FORMATTER);

		assertEquals(risposta, letto.getString("dataEvento"));
		assertEquals(risposta, letto.getJsonObject("parametriRichiesta").getString("dataOraRichiesta"));
		assertEquals(risposta, letto.getJsonObject("parametriRisposta").getString("dataOraRisposta"));
	}

	@ParameterizedTest(name = "{0}")
	@DisplayName("Le date non valide sono rifiutate")
	@ValueSource(strings = { "2026-07-15T00:30:00+0200", "2026-07-15T00:30:00+02", "2026-07-15 00:30:00+02:00",
			"2026-07-15", "2026-02-30T10:00:00Z" })
	void dateNonValideRifiutate(String ricevuta) throws Exception {
		String valida = "2026-07-15T00:30:00+02:00";

		for (String body : new String[] { evento(ricevuta, valida, valida), evento(valida, ricevuta, valida),
				evento(valida, valida, ricevuta) }) {
			this.mockMvc.perform(post(Costanti.EVENTI_PATH)
					.content(body)
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		}
	}

	private static String evento(String dataEvento, String dataOraRichiesta, String dataOraRisposta) {
		return """
				{"categoriaEvento":"UTENTE","ruolo":"CLIENT","componente":"API_BACKOFFICE","esito":"OK",
				 "idDominio":"%s","tipoEvento":"date","dataEvento":"%s",
				 "parametriRichiesta":{"dataOraRichiesta":"%s"},
				 "parametriRisposta":{"dataOraRisposta":"%s"}}"""
				.formatted(Costanti.ID_DOMINIO_1, dataEvento, dataOraRichiesta, dataOraRisposta);
	}

	private JsonObject parametri(String colonna, long id) {
		return json(this.jdbcTemplate.queryForObject("select " + colonna + " from eventi where id = ?",
				byte[].class, id));
	}

	private static JsonObject json(byte[] contenuto) {
		return Json.createReader(new ByteArrayInputStream(contenuto)).readObject();
	}
}
