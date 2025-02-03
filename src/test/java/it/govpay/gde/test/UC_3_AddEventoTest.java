package it.govpay.gde.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.TimeZone;

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
import it.govpay.gde.beans.CategoriaEvento;
import it.govpay.gde.beans.ComponenteEvento;
import it.govpay.gde.beans.DatiPagoPA;
import it.govpay.gde.beans.DettaglioRichiesta;
import it.govpay.gde.beans.DettaglioRisposta;
import it.govpay.gde.beans.EsitoEvento;
import it.govpay.gde.beans.Header;
import it.govpay.gde.beans.NuovoEvento;
import it.govpay.gde.beans.RuoloEvento;
import it.govpay.gde.test.costanti.Costanti;
import it.govpay.gde.utils.OffsetDateTimeDeserializer;
import it.govpay.gde.utils.OffsetDateTimeSerializer;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test Lettura Eventi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ActiveProfiles("test")
class UC_3_AddEventoTest {


	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper mapper;

	@BeforeEach
	public void init() {
		SimpleDateFormat sdf = new SimpleDateFormat(Costanti.PATTERN_TIMESTAMP_3_YYYY_MM_DD_T_HH_MM_SS_SSSXXX);
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		sdf.setLenient(false);

		mapper = JsonMapper.builder().build();

		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
		javaTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());
		mapper.registerModule(javaTimeModule); 

		mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID); 
		mapper.setDateFormat(sdf);
	}

	@Test
	void UC_3_01_AddEventoOk() throws Exception {
		NuovoEvento nuovoEvento = new NuovoEvento();
		nuovoEvento.setCategoriaEvento(CategoriaEvento.UTENTE);
		nuovoEvento.setCcp("ccp");
		nuovoEvento.setClusterId("GovPay");
		nuovoEvento.setComponente(ComponenteEvento.API_BACKOFFICE);
		nuovoEvento.setDataEvento(OffsetDateTime.now());
		// nuovoEvento.setDatiPagoPA(null);
		nuovoEvento.setDettaglioEsito("dettaglioEsito");
		nuovoEvento.setDurataEvento(10l);
		nuovoEvento.setEsito(EsitoEvento.OK);
		nuovoEvento.setIdA2A("idA2A");
		nuovoEvento.setIdDominio(Costanti.ID_DOMINIO_1);
		nuovoEvento.setIdFr(1l);
		nuovoEvento.setIdPagamento("idPagamento");
		nuovoEvento.setIdPendenza("idPendenza");
		nuovoEvento.setIdRiconciliazione(1l);
		nuovoEvento.setIdTracciato(1l);
		nuovoEvento.setIuv("iuv");
		//nuovoEvento.setParametriRichiesta(null);
		//nuovoEvento.setParametriRisposta(null);
		nuovoEvento.setRuolo(RuoloEvento.CLIENT);
		nuovoEvento.setSeverita(1);
		nuovoEvento.setSottotipoEsito("200");
		nuovoEvento.setSottotipoEvento("testAddEvento");
		nuovoEvento.setTipoEvento("addEvento");
		nuovoEvento.setTransactionId("1234567890");

		String body = mapper.writeValueAsString(nuovoEvento);

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn();

		String locationDettaglioEvento = result.getResponse().getHeader("Location");

		// estrazione idEvento
		int idEvento = Integer.parseInt(locationDettaglioEvento.substring(locationDettaglioEvento.lastIndexOf("/")+1));

		result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
				.andExpect(status().isOk())
				.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject eventoDetail = reader.readObject();

		assertNotNull(eventoDetail);
		assertEquals(idEvento, eventoDetail.getInt("id"));
		assertEquals(nuovoEvento.getCategoriaEvento().toString(), eventoDetail.getString("categoriaEvento"));
		assertEquals(nuovoEvento.getCcp(), eventoDetail.getString("ccp"));
		assertEquals(nuovoEvento.getClusterId(), eventoDetail.getString("clusterId"));
		assertEquals(nuovoEvento.getComponente().toString(), eventoDetail.getString("componente"));
		assertEquals(nuovoEvento.getDataEvento().format(Costanti.DEFAULT_FORMATTER), eventoDetail.getString("dataEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("datiPagoPA"));
		assertEquals(nuovoEvento.getDettaglioEsito(), eventoDetail.getString("dettaglioEsito"));
		assertEquals(nuovoEvento.getDurataEvento().intValue(), eventoDetail.getInt("durataEvento"));
		assertEquals(nuovoEvento.getEsito().toString(), eventoDetail.getString("esito"));        
		assertEquals(nuovoEvento.getIdA2A(), eventoDetail.getString("idA2A"));
		assertEquals(nuovoEvento.getIdDominio(), eventoDetail.getString("idDominio"));
		assertEquals(nuovoEvento.getIdFr(), eventoDetail.getInt("idFr"));
		assertEquals(nuovoEvento.getIdPagamento(), eventoDetail.getString("idPagamento"));
		assertEquals(nuovoEvento.getIdPendenza(), eventoDetail.getString("idPendenza"));
		assertEquals(nuovoEvento.getIdRiconciliazione(), eventoDetail.getInt("idRiconciliazione"));
		assertEquals(nuovoEvento.getIdTracciato(), eventoDetail.getInt("idTracciato"));
		assertEquals(nuovoEvento.getIuv(), eventoDetail.getString("iuv"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRichiesta"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRisposta"));
		assertEquals(nuovoEvento.getRuolo().toString(), eventoDetail.getString("ruolo"));
		assertEquals(nuovoEvento.getSeverita(), eventoDetail.getInt("severita"));
		assertEquals(nuovoEvento.getSottotipoEsito(), eventoDetail.getString("sottotipoEsito"));
		assertEquals(nuovoEvento.getSottotipoEvento(), eventoDetail.getString("sottotipoEvento"));
		assertEquals(nuovoEvento.getTipoEvento(), eventoDetail.getString("tipoEvento"));
		assertEquals(nuovoEvento.getTransactionId(), eventoDetail.getString("transactionId"));
	}

	@Test
	void UC_3_02_AddEvento_DatiPagoPAOk() throws Exception {
		NuovoEvento nuovoEvento = new NuovoEvento();
		nuovoEvento.setCategoriaEvento(CategoriaEvento.INTERFACCIA);
		nuovoEvento.setCcp("ccp");
		nuovoEvento.setClusterId("GovPay");
		nuovoEvento.setComponente(ComponenteEvento.API_BACKOFFICE);
		nuovoEvento.setDataEvento(OffsetDateTime.now());
		DatiPagoPA datiPagoPA = new DatiPagoPA();
		datiPagoPA.setIdCanale("canale");
		datiPagoPA.setIdDominio(Costanti.ID_DOMINIO_1);
		datiPagoPA.setIdentificativoErogatore("govpay");
		datiPagoPA.setIdentificativoFruitore("ndp");
		datiPagoPA.setIdFlusso("idFlusso");
		datiPagoPA.setIdIntermediario("12345678121");
		datiPagoPA.setIdIntermediarioPsp("12345678121");
		datiPagoPA.setIdPsp("banca1");
		datiPagoPA.setIdRiconciliazione("rncId");
		datiPagoPA.setIdStazione("12345678121_01");
		datiPagoPA.setIdTracciato(BigDecimal.ONE);
		datiPagoPA.setModelloPagamento("0");
		datiPagoPA.setSct("sct");
		datiPagoPA.setTipoVersamento("PO");
		nuovoEvento.setDatiPagoPA(datiPagoPA);
		nuovoEvento.setDettaglioEsito("dettaglioEsito");
		nuovoEvento.setDurataEvento(10l);
		nuovoEvento.setEsito(EsitoEvento.OK);
		nuovoEvento.setIdA2A("idA2A");
		nuovoEvento.setIdDominio(Costanti.ID_DOMINIO_1);
		nuovoEvento.setIdFr(1l);
		nuovoEvento.setIdPagamento("idPagamento");
		nuovoEvento.setIdPendenza("idPendenza");
		nuovoEvento.setIdRiconciliazione(1l);
		nuovoEvento.setIdTracciato(1l);
		nuovoEvento.setIuv("iuv");
		//nuovoEvento.setParametriRichiesta(null);
		//nuovoEvento.setParametriRisposta(null);
		nuovoEvento.setRuolo(RuoloEvento.SERVER);
		nuovoEvento.setSeverita(1);
		nuovoEvento.setSottotipoEsito("200");
		nuovoEvento.setSottotipoEvento("testAddEvento");
		nuovoEvento.setTipoEvento("addEvento");
		nuovoEvento.setTransactionId("1234567890");

		String body = mapper.writeValueAsString(nuovoEvento);

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn();

		String locationDettaglioEvento = result.getResponse().getHeader("Location");

		// estrazione idEvento
		int idEvento = Integer.parseInt(locationDettaglioEvento.substring(locationDettaglioEvento.lastIndexOf("/")+1));

		result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
				.andExpect(status().isOk())
				.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject eventoDetail = reader.readObject();

		assertNotNull(eventoDetail);
		assertEquals(idEvento, eventoDetail.getInt("id"));
		assertEquals(nuovoEvento.getCategoriaEvento().toString(), eventoDetail.getString("categoriaEvento"));
		assertEquals(nuovoEvento.getCcp(), eventoDetail.getString("ccp"));
		assertEquals(nuovoEvento.getClusterId(), eventoDetail.getString("clusterId"));
		assertEquals(nuovoEvento.getComponente().toString(), eventoDetail.getString("componente"));
		assertEquals(nuovoEvento.getDataEvento().format(Costanti.DEFAULT_FORMATTER), eventoDetail.getString("dataEvento"));
		assertNotNull(eventoDetail.get("datiPagoPA"));
		assertEquals(nuovoEvento.getDettaglioEsito(), eventoDetail.getString("dettaglioEsito"));
		assertEquals(nuovoEvento.getDurataEvento().intValue(), eventoDetail.getInt("durataEvento"));
		assertEquals(nuovoEvento.getEsito().toString(), eventoDetail.getString("esito"));        
		assertEquals(nuovoEvento.getIdA2A(), eventoDetail.getString("idA2A"));
		assertEquals(nuovoEvento.getIdDominio(), eventoDetail.getString("idDominio"));
		assertEquals(nuovoEvento.getIdFr(), eventoDetail.getInt("idFr"));
		assertEquals(nuovoEvento.getIdPagamento(), eventoDetail.getString("idPagamento"));
		assertEquals(nuovoEvento.getIdPendenza(), eventoDetail.getString("idPendenza"));
		assertEquals(nuovoEvento.getIdRiconciliazione(), eventoDetail.getInt("idRiconciliazione"));
		assertEquals(nuovoEvento.getIdTracciato(), eventoDetail.getInt("idTracciato"));
		assertEquals(nuovoEvento.getIuv(), eventoDetail.getString("iuv"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRichiesta"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRisposta"));
		assertEquals(nuovoEvento.getRuolo().toString(), eventoDetail.getString("ruolo"));
		assertEquals(nuovoEvento.getSeverita(), eventoDetail.getInt("severita"));
		assertEquals(nuovoEvento.getSottotipoEsito(), eventoDetail.getString("sottotipoEsito"));
		assertEquals(nuovoEvento.getSottotipoEvento(), eventoDetail.getString("sottotipoEvento"));
		assertEquals(nuovoEvento.getTipoEvento(), eventoDetail.getString("tipoEvento"));
		assertEquals(nuovoEvento.getTransactionId(), eventoDetail.getString("transactionId"));

		JsonObject datiPagoPADetail = eventoDetail.getJsonObject("datiPagoPA");
		assertEquals(datiPagoPA.getIdCanale(), datiPagoPADetail.getString("idCanale"));
		assertEquals(datiPagoPA.getIdDominio(), datiPagoPADetail.getString("idDominio"));
		assertEquals(datiPagoPA.getIdentificativoErogatore(), datiPagoPADetail.getString("identificativoErogatore"));
		assertEquals(datiPagoPA.getIdentificativoFruitore(), datiPagoPADetail.getString("identificativoFruitore"));
		assertEquals(datiPagoPA.getIdFlusso(), datiPagoPADetail.getString("idFlusso"));
		assertEquals(datiPagoPA.getIdIntermediario(), datiPagoPADetail.getString("idIntermediario"));
		assertEquals(datiPagoPA.getIdIntermediarioPsp(), datiPagoPADetail.getString("idIntermediarioPsp"));
		assertEquals(datiPagoPA.getIdPsp(), datiPagoPADetail.getString("idPsp"));
		assertEquals(datiPagoPA.getIdRiconciliazione(), datiPagoPADetail.getString("idRiconciliazione"));
		assertEquals(datiPagoPA.getIdStazione(), datiPagoPADetail.getString("idStazione"));
		assertEquals(datiPagoPA.getIdTracciato().intValue(), datiPagoPADetail.getInt("idTracciato"));
		assertEquals(datiPagoPA.getModelloPagamento(), datiPagoPADetail.getString("modelloPagamento"));
		assertEquals(datiPagoPA.getSct(), datiPagoPADetail.getString("sct"));
		assertEquals(datiPagoPA.getTipoVersamento(), datiPagoPADetail.getString("tipoVersamento"));
	}

	@Test
	void UC_3_03_AddEvento_ParametriRichiestaOk() throws Exception {
		NuovoEvento nuovoEvento = new NuovoEvento();
		nuovoEvento.setCategoriaEvento(CategoriaEvento.UTENTE);
		nuovoEvento.setCcp("ccp");
		nuovoEvento.setClusterId("GovPay");
		nuovoEvento.setComponente(ComponenteEvento.API_BACKOFFICE);
		nuovoEvento.setDataEvento(OffsetDateTime.now());
		// nuovoEvento.setDatiPagoPA(null);
		nuovoEvento.setDettaglioEsito("dettaglioEsito");
		nuovoEvento.setDurataEvento(10l);
		nuovoEvento.setEsito(EsitoEvento.OK);
		nuovoEvento.setIdA2A("idA2A");
		nuovoEvento.setIdDominio(Costanti.ID_DOMINIO_1);
		nuovoEvento.setIdFr(1l);
		nuovoEvento.setIdPagamento("idPagamento");
		nuovoEvento.setIdPendenza("idPendenza");
		nuovoEvento.setIdRiconciliazione(1l);
		nuovoEvento.setIdTracciato(1l);
		nuovoEvento.setIuv("iuv");
		DettaglioRichiesta parametriRichiesta = new DettaglioRichiesta();
		parametriRichiesta.setDataOraRichiesta(OffsetDateTime.now());
		Header header = new Header("Accept");
		header.setValore("application/json");
		parametriRichiesta.addHeadersItem(header );
		parametriRichiesta.setMethod("GET");
		parametriRichiesta.setPayload("{}");
		parametriRichiesta.setPrincipal("principal");
		parametriRichiesta.setUrl("http://localhost:8080/gde/addEvento");
		parametriRichiesta.setUtente("mariorossi");
		nuovoEvento.setParametriRichiesta(parametriRichiesta );
		//nuovoEvento.setParametriRisposta(null);
		nuovoEvento.setRuolo(RuoloEvento.CLIENT);
		nuovoEvento.setSeverita(1);
		nuovoEvento.setSottotipoEsito("200");
		nuovoEvento.setSottotipoEvento("testAddEvento");
		nuovoEvento.setTipoEvento("addEvento");
		nuovoEvento.setTransactionId("1234567890");

		String body = mapper.writeValueAsString(nuovoEvento);

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn();

		String locationDettaglioEvento = result.getResponse().getHeader("Location");

		// estrazione idEvento
		int idEvento = Integer.parseInt(locationDettaglioEvento.substring(locationDettaglioEvento.lastIndexOf("/")+1));

		result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
				.andExpect(status().isOk())
				.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject eventoDetail = reader.readObject();

		assertNotNull(eventoDetail);
		assertEquals(idEvento, eventoDetail.getInt("id"));
		assertEquals(nuovoEvento.getCategoriaEvento().toString(), eventoDetail.getString("categoriaEvento"));
		assertEquals(nuovoEvento.getCcp(), eventoDetail.getString("ccp"));
		assertEquals(nuovoEvento.getClusterId(), eventoDetail.getString("clusterId"));
		assertEquals(nuovoEvento.getComponente().toString(), eventoDetail.getString("componente"));
		assertEquals(nuovoEvento.getDataEvento().format(Costanti.DEFAULT_FORMATTER), eventoDetail.getString("dataEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("datiPagoPA"));
		assertEquals(nuovoEvento.getDettaglioEsito(), eventoDetail.getString("dettaglioEsito"));
		assertEquals(nuovoEvento.getDurataEvento().intValue(), eventoDetail.getInt("durataEvento"));
		assertEquals(nuovoEvento.getEsito().toString(), eventoDetail.getString("esito"));        
		assertEquals(nuovoEvento.getIdA2A(), eventoDetail.getString("idA2A"));
		assertEquals(nuovoEvento.getIdDominio(), eventoDetail.getString("idDominio"));
		assertEquals(nuovoEvento.getIdFr(), eventoDetail.getInt("idFr"));
		assertEquals(nuovoEvento.getIdPagamento(), eventoDetail.getString("idPagamento"));
		assertEquals(nuovoEvento.getIdPendenza(), eventoDetail.getString("idPendenza"));
		assertEquals(nuovoEvento.getIdRiconciliazione(), eventoDetail.getInt("idRiconciliazione"));
		assertEquals(nuovoEvento.getIdTracciato(), eventoDetail.getInt("idTracciato"));
		assertEquals(nuovoEvento.getIuv(), eventoDetail.getString("iuv"));
		assertNotNull(eventoDetail.get("parametriRichiesta"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRisposta"));
		assertEquals(nuovoEvento.getRuolo().toString(), eventoDetail.getString("ruolo"));
		assertEquals(nuovoEvento.getSeverita(), eventoDetail.getInt("severita"));
		assertEquals(nuovoEvento.getSottotipoEsito(), eventoDetail.getString("sottotipoEsito"));
		assertEquals(nuovoEvento.getSottotipoEvento(), eventoDetail.getString("sottotipoEvento"));
		assertEquals(nuovoEvento.getTipoEvento(), eventoDetail.getString("tipoEvento"));
		assertEquals(nuovoEvento.getTransactionId(), eventoDetail.getString("transactionId"));

		JsonObject parametriRichiestaDetail = eventoDetail.getJsonObject("parametriRichiesta");
		assertEquals(parametriRichiesta.getDataOraRichiesta().format(Costanti.DEFAULT_FORMATTER), parametriRichiestaDetail.getString("dataOraRichiesta"));
		assertNotNull(parametriRichiestaDetail.get("headers"));
		assertEquals(parametriRichiesta.getMethod(), parametriRichiestaDetail.getString("method"));
		assertEquals(parametriRichiesta.getPayload(), parametriRichiestaDetail.getString("payload"));
		assertEquals(parametriRichiesta.getPrincipal(), parametriRichiestaDetail.getString("principal"));
		assertEquals(parametriRichiesta.getUrl(), parametriRichiestaDetail.getString("url"));
		assertEquals(parametriRichiesta.getUtente(), parametriRichiestaDetail.getString("utente"));

		JsonArray headersDetail = parametriRichiestaDetail.getJsonArray("headers");
		assertEquals(1, headersDetail.size());
		JsonObject headerDetail = headersDetail.getJsonObject(0);

		assertEquals(header.getNome(), headerDetail.getString("nome"));
		assertEquals(header.getValore(), headerDetail.getString("valore"));
	}

	@Test
	void UC_3_04_AddEvento_ParametriRispostaOk() throws Exception {
		NuovoEvento nuovoEvento = new NuovoEvento();
		nuovoEvento.setCategoriaEvento(CategoriaEvento.UTENTE);
		nuovoEvento.setCcp("ccp");
		nuovoEvento.setClusterId("GovPay");
		nuovoEvento.setComponente(ComponenteEvento.API_BACKOFFICE);
		nuovoEvento.setDataEvento(OffsetDateTime.now());
		// nuovoEvento.setDatiPagoPA(null);
		nuovoEvento.setDettaglioEsito("dettaglioEsito");
		nuovoEvento.setDurataEvento(10l);
		nuovoEvento.setEsito(EsitoEvento.OK);
		nuovoEvento.setIdA2A("idA2A");
		nuovoEvento.setIdDominio(Costanti.ID_DOMINIO_1);
		nuovoEvento.setIdFr(1l);
		nuovoEvento.setIdPagamento("idPagamento");
		nuovoEvento.setIdPendenza("idPendenza");
		nuovoEvento.setIdRiconciliazione(1l);
		nuovoEvento.setIdTracciato(1l);
		nuovoEvento.setIuv("iuv");
		DettaglioRisposta parametriRisposta = new DettaglioRisposta();
		parametriRisposta.setDataOraRisposta(OffsetDateTime.now());
		Header header = new Header("ContentType");
		header.setValore("application/json");
		parametriRisposta.addHeadersItem(header );
		parametriRisposta.setPayload("{}");
		parametriRisposta.setStatus(BigDecimal.valueOf(200));
		//		nuovoEvento.setParametriRichiesta(parametriRichiesta );
		nuovoEvento.setParametriRisposta(parametriRisposta);
		nuovoEvento.setRuolo(RuoloEvento.CLIENT);
		nuovoEvento.setSeverita(1);
		nuovoEvento.setSottotipoEsito("200");
		nuovoEvento.setSottotipoEvento("testAddEvento");
		nuovoEvento.setTipoEvento("addEvento");
		nuovoEvento.setTransactionId("1234567890");

		String body = mapper.writeValueAsString(nuovoEvento);

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn();

		String locationDettaglioEvento = result.getResponse().getHeader("Location");

		// estrazione idEvento
		int idEvento = Integer.parseInt(locationDettaglioEvento.substring(locationDettaglioEvento.lastIndexOf("/")+1));

		result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
				.andExpect(status().isOk())
				.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject eventoDetail = reader.readObject();

		assertNotNull(eventoDetail);
		assertEquals(idEvento, eventoDetail.getInt("id"));
		assertEquals(nuovoEvento.getCategoriaEvento().toString(), eventoDetail.getString("categoriaEvento"));
		assertEquals(nuovoEvento.getCcp(), eventoDetail.getString("ccp"));
		assertEquals(nuovoEvento.getClusterId(), eventoDetail.getString("clusterId"));
		assertEquals(nuovoEvento.getComponente().toString(), eventoDetail.getString("componente"));
		assertEquals(nuovoEvento.getDataEvento().format(Costanti.DEFAULT_FORMATTER), eventoDetail.getString("dataEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("datiPagoPA"));
		assertEquals(nuovoEvento.getDettaglioEsito(), eventoDetail.getString("dettaglioEsito"));
		assertEquals(nuovoEvento.getDurataEvento().intValue(), eventoDetail.getInt("durataEvento"));
		assertEquals(nuovoEvento.getEsito().toString(), eventoDetail.getString("esito"));        
		assertEquals(nuovoEvento.getIdA2A(), eventoDetail.getString("idA2A"));
		assertEquals(nuovoEvento.getIdDominio(), eventoDetail.getString("idDominio"));
		assertEquals(nuovoEvento.getIdFr(), eventoDetail.getInt("idFr"));
		assertEquals(nuovoEvento.getIdPagamento(), eventoDetail.getString("idPagamento"));
		assertEquals(nuovoEvento.getIdPendenza(), eventoDetail.getString("idPendenza"));
		assertEquals(nuovoEvento.getIdRiconciliazione(), eventoDetail.getInt("idRiconciliazione"));
		assertEquals(nuovoEvento.getIdTracciato(), eventoDetail.getInt("idTracciato"));
		assertEquals(nuovoEvento.getIuv(), eventoDetail.getString("iuv"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRichiesta"));
		assertNotNull(eventoDetail.get("parametriRisposta"));
		assertEquals(nuovoEvento.getRuolo().toString(), eventoDetail.getString("ruolo"));
		assertEquals(nuovoEvento.getSeverita(), eventoDetail.getInt("severita"));
		assertEquals(nuovoEvento.getSottotipoEsito(), eventoDetail.getString("sottotipoEsito"));
		assertEquals(nuovoEvento.getSottotipoEvento(), eventoDetail.getString("sottotipoEvento"));
		assertEquals(nuovoEvento.getTipoEvento(), eventoDetail.getString("tipoEvento"));
		assertEquals(nuovoEvento.getTransactionId(), eventoDetail.getString("transactionId"));

		JsonObject parametriRispostaDetail = eventoDetail.getJsonObject("parametriRisposta");
		assertEquals(parametriRisposta.getDataOraRisposta().format(Costanti.DEFAULT_FORMATTER), parametriRispostaDetail.getString("dataOraRisposta"));
		assertNotNull(parametriRispostaDetail.get("headers"));
		assertEquals(parametriRisposta.getPayload(), parametriRispostaDetail.getString("payload"));
		assertEquals(parametriRisposta.getStatus().intValue(), parametriRispostaDetail.getInt("status"));

		JsonArray headersDetail = parametriRispostaDetail.getJsonArray("headers");
		assertEquals(1, headersDetail.size());
		JsonObject headerDetail = headersDetail.getJsonObject(0);

		assertEquals(header.getNome(), headerDetail.getString("nome"));
		assertEquals(header.getValore(), headerDetail.getString("valore"));
	}

	@Test
	void UC_3_05_AddEvento_CampiNullOk() throws Exception {
		NuovoEvento nuovoEvento = new NuovoEvento();
		//		nuovoEvento.setCategoriaEvento(CategoriaEvento.UTENTE);
		nuovoEvento.setCcp("ccp");
		nuovoEvento.setClusterId("GovPay");
		nuovoEvento.setComponente(ComponenteEvento.API_BACKOFFICE);
		nuovoEvento.setDataEvento(OffsetDateTime.now());
		// nuovoEvento.setDatiPagoPA(null);
		nuovoEvento.setDettaglioEsito("dettaglioEsito");
		nuovoEvento.setDurataEvento(10l);
		nuovoEvento.setEsito(EsitoEvento.OK);
		nuovoEvento.setIdA2A("idA2A");
		nuovoEvento.setIdDominio(Costanti.ID_DOMINIO_1);
		nuovoEvento.setIdFr(1l);
		nuovoEvento.setIdPagamento("idPagamento");
		nuovoEvento.setIdPendenza("idPendenza");
		nuovoEvento.setIdRiconciliazione(1l);
		nuovoEvento.setIdTracciato(1l);
		nuovoEvento.setIuv("iuv");
		DettaglioRichiesta parametriRichiesta = new DettaglioRichiesta();
		nuovoEvento.setParametriRichiesta(parametriRichiesta);
		//nuovoEvento.setParametriRisposta(null);
		//		nuovoEvento.setRuolo(RuoloEvento.CLIENT);
		nuovoEvento.setSeverita(1);
		nuovoEvento.setSottotipoEsito("200");
		nuovoEvento.setSottotipoEvento("testAddEvento");
		nuovoEvento.setTipoEvento("addEvento");
		nuovoEvento.setTransactionId("1234567890");

		String body = mapper.writeValueAsString(nuovoEvento);

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn();

		String locationDettaglioEvento = result.getResponse().getHeader("Location");

		// estrazione idEvento
		int idEvento = Integer.parseInt(locationDettaglioEvento.substring(locationDettaglioEvento.lastIndexOf("/")+1));

		result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
				.andExpect(status().isOk())
				.andReturn();
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject eventoDetail = reader.readObject();

		assertNotNull(eventoDetail);
		assertEquals(idEvento, eventoDetail.getInt("id"));
		assertEquals(JsonValue.NULL, eventoDetail.get("categoriaEvento"));
		assertEquals(nuovoEvento.getCcp(), eventoDetail.getString("ccp"));
		assertEquals(nuovoEvento.getClusterId(), eventoDetail.getString("clusterId"));
		assertEquals(nuovoEvento.getComponente().toString(), eventoDetail.getString("componente"));
		assertEquals(nuovoEvento.getDataEvento().format(Costanti.DEFAULT_FORMATTER), eventoDetail.getString("dataEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("datiPagoPA"));
		assertEquals(nuovoEvento.getDettaglioEsito(), eventoDetail.getString("dettaglioEsito"));
		assertEquals(nuovoEvento.getDurataEvento().intValue(), eventoDetail.getInt("durataEvento"));
		assertEquals(nuovoEvento.getEsito().toString(), eventoDetail.getString("esito"));        
		assertEquals(nuovoEvento.getIdA2A(), eventoDetail.getString("idA2A"));
		assertEquals(nuovoEvento.getIdDominio(), eventoDetail.getString("idDominio"));
		assertEquals(nuovoEvento.getIdFr(), eventoDetail.getInt("idFr"));
		assertEquals(nuovoEvento.getIdPagamento(), eventoDetail.getString("idPagamento"));
		assertEquals(nuovoEvento.getIdPendenza(), eventoDetail.getString("idPendenza"));
		assertEquals(nuovoEvento.getIdRiconciliazione(), eventoDetail.getInt("idRiconciliazione"));
		assertEquals(nuovoEvento.getIdTracciato(), eventoDetail.getInt("idTracciato"));
		assertEquals(nuovoEvento.getIuv(), eventoDetail.getString("iuv"));
		assertNotNull(eventoDetail.get("parametriRichiesta"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRisposta"));
		assertEquals(JsonValue.NULL, eventoDetail.get("ruolo"));
		assertEquals(nuovoEvento.getSeverita(), eventoDetail.getInt("severita"));
		assertEquals(nuovoEvento.getSottotipoEsito(), eventoDetail.getString("sottotipoEsito"));
		assertEquals(nuovoEvento.getSottotipoEvento(), eventoDetail.getString("sottotipoEvento"));
		assertEquals(nuovoEvento.getTipoEvento(), eventoDetail.getString("tipoEvento"));
		assertEquals(nuovoEvento.getTransactionId(), eventoDetail.getString("transactionId"));
	}

	@Test
	void UC_3_06_AddEvento_EmptyBody() throws Exception {
		String body = "{}";

		MvcResult result = this.mockMvc.perform(post(Costanti.EVENTI_PATH)
				.content(body)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn();

		String locationDettaglioEvento = result.getResponse().getHeader("Location");

		// estrazione idEvento
		int idEvento = Integer.parseInt(locationDettaglioEvento.substring(locationDettaglioEvento.lastIndexOf("/")+1));

		result = this.mockMvc.perform(get(Costanti.EVENTO_PATH,idEvento))
				.andExpect(status().isOk())
				.andReturn();
		System.out.println("RES: "+result.getResponse().getContentAsString());
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject eventoDetail = reader.readObject();

		assertNotNull(eventoDetail);
		assertEquals(idEvento, eventoDetail.getInt("id"));
		assertEquals(JsonValue.NULL, eventoDetail.get("categoriaEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("ccp"));
		assertEquals(JsonValue.NULL, eventoDetail.get("clusterId"));
		assertEquals(JsonValue.NULL, eventoDetail.get("componente"));
		assertEquals(JsonValue.NULL, eventoDetail.get("dataEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("datiPagoPA"));
		assertEquals(JsonValue.NULL, eventoDetail.get("dettaglioEsito"));
		assertEquals(JsonValue.NULL, eventoDetail.get("durataEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("esito"));        
		assertEquals(JsonValue.NULL, eventoDetail.get("idA2A"));
		assertEquals(JsonValue.NULL, eventoDetail.get("idDominio"));
		assertEquals(JsonValue.NULL, eventoDetail.get("idFr"));
		assertEquals(JsonValue.NULL, eventoDetail.get("idPagamento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("idPendenza"));
		assertEquals(JsonValue.NULL, eventoDetail.get("idRiconciliazione"));
		assertEquals(JsonValue.NULL, eventoDetail.get("idTracciato"));
		assertEquals(JsonValue.NULL, eventoDetail.get("iuv"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRichiesta"));
		assertEquals(JsonValue.NULL, eventoDetail.get("parametriRisposta"));
		assertEquals(JsonValue.NULL, eventoDetail.get("ruolo"));
		assertEquals(JsonValue.NULL, eventoDetail.get("severita"));
		assertEquals(JsonValue.NULL, eventoDetail.get("sottotipoEsito"));
		assertEquals(JsonValue.NULL, eventoDetail.get("sottotipoEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("tipoEvento"));
		assertEquals(JsonValue.NULL, eventoDetail.get("transactionId"));


	}
}
