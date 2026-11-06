package it.govpay.gde.test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import it.govpay.gde.Application;
import it.govpay.gde.beans.ComponenteEvento;
import it.govpay.gde.beans.NuovoEvento;
import it.govpay.gde.test.costanti.Costanti;
import it.govpay.gde.utils.OffsetDateTimeDeserializer;
import it.govpay.gde.utils.OffsetDateTimeSerializer;

/**
 * {@code codDocumento} non è di per sé univoco: la business key della
 * posizione debitoria complessiva è la terna {@code idA2A}+{@code idDominio}+
 * {@code codDocumento}. Verifica che la ricerca sul solo {@code codDocumento}
 * sia ambigua tra applicazioni/domini diversi, e che la combinazione con
 * {@code idA2A} e {@code idDominio} identifichi un singolo evento.
 */
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test Ricerca per codDocumento")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ActiveProfiles("test")
class UC_5_FindEventiByCodDocumentoTest {

	private static final String COD_DOCUMENTO = "docDocumentoCondiviso";
	private static final String COD_DOCUMENTO_STESSA_APP = "docDocumentoStessaApplicazione";

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

	private void creaEvento(String codDocumento, String idA2A, String idDominio) throws Exception {
		NuovoEvento nuovoEvento = new NuovoEvento();
		nuovoEvento.setComponente(ComponenteEvento.API_BACKOFFICE);
		nuovoEvento.setDataEvento(OffsetDateTime.now());
		nuovoEvento.setIdA2A(idA2A);
		nuovoEvento.setIdDominio(idDominio);
		nuovoEvento.setCodDocumento(codDocumento);

		String body = mapper.writeValueAsString(nuovoEvento);

		this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	void UC_5_01_StessoCodDocumento_ApplicazioniEDominiDiversi() throws Exception {
		creaEvento(COD_DOCUMENTO, "idA2A_X", Costanti.ID_DOMINIO_1);
		creaEvento(COD_DOCUMENTO, "idA2A_Y", "99999999999");

		// codDocumento da solo e' ambiguo: trova entrambi gli eventi
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
						.param("codDocumento", COD_DOCUMENTO)
						.param("total", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(2)));

		// chiave completa idA2A+idDominio+codDocumento: un solo evento
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
						.param("codDocumento", COD_DOCUMENTO)
						.param("idA2A", "idA2A_X")
						.param("idDominio", Costanti.ID_DOMINIO_1)
						.param("total", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(1)))
				.andExpect(jsonPath("$.items[0].idA2A", is("idA2A_X")))
				.andExpect(jsonPath("$.items[0].idDominio", is(Costanti.ID_DOMINIO_1)));

		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
						.param("codDocumento", COD_DOCUMENTO)
						.param("idA2A", "idA2A_Y")
						.param("idDominio", "99999999999")
						.param("total", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(1)))
				.andExpect(jsonPath("$.items[0].idA2A", is("idA2A_Y")))
				.andExpect(jsonPath("$.items[0].idDominio", is("99999999999")));
	}

	@Test
	void UC_5_02_StessoCodDocumentoEStessaApplicazione_DominiDiversi() throws Exception {
		creaEvento(COD_DOCUMENTO_STESSA_APP, "idA2A_Z", "33333333333");
		creaEvento(COD_DOCUMENTO_STESSA_APP, "idA2A_Z", "44444444444");

		// stessa applicazione: codDocumento+idA2A resta ambiguo tra i due domini
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
						.param("codDocumento", COD_DOCUMENTO_STESSA_APP)
						.param("idA2A", "idA2A_Z")
						.param("total", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(2)));

		// idDominio disambigua indipendentemente dall'applicazione
		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
						.param("codDocumento", COD_DOCUMENTO_STESSA_APP)
						.param("idA2A", "idA2A_Z")
						.param("idDominio", "33333333333")
						.param("total", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(1)))
				.andExpect(jsonPath("$.items[0].idDominio", is("33333333333")));

		this.mockMvc.perform(get(Costanti.EVENTI_PATH)
						.param("codDocumento", COD_DOCUMENTO_STESSA_APP)
						.param("idA2A", "idA2A_Z")
						.param("idDominio", "44444444444")
						.param("total", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.total", is(1)))
				.andExpect(jsonPath("$.items[0].idDominio", is("44444444444")));
	}
}
