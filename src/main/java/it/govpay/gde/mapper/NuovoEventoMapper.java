package it.govpay.gde.mapper;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.gde.beans.NuovoEvento;
import it.govpay.gde.entity.DatiPagoPA;
import it.govpay.gde.entity.DettaglioRichiesta;
import it.govpay.gde.entity.DettaglioRisposta;
import it.govpay.gde.entity.EventoEntity;
import it.govpay.gde.entity.EventoEntity.CategoriaEvento;
import it.govpay.gde.entity.EventoEntity.RuoloEvento;
import it.govpay.gde.exception.AttributeConverterException;
import it.govpay.gde.exception.BadRequestException;
import it.govpay.gde.utils.JpaConverterObjectMapperFactory;

@Mapper(componentModel = "spring")
public interface NuovoEventoMapper {

	@Mapping(target = "categoriaEvento", source="categoriaEvento", qualifiedByName = "convertCategoriaEvento")
	@Mapping(target = "ruoloEvento", source="ruolo", qualifiedByName = "convertRuoloEvento")
	@Mapping(target = "esitoEvento", source="esito")
	@Mapping(target = "codDominio", source="idDominio")
	@Mapping(target = "codVersamentoEnte", source="idPendenza")
	@Mapping(target = "codApplicazione", source="idA2A")
	@Mapping(target = "idSessione", source="idPagamento")
	@Mapping(target = "idIncasso", source="idRiconciliazione")
	@Mapping(target = "data", source="dataEvento")
	@Mapping(target = "intervallo", source="durataEvento")
	@Mapping(target = "parametriRichiesta", expression = "java(nuovoEvento.getParametriRichiesta() != null ? convertParametriRichiesta(nuovoEvento.getParametriRichiesta()) : null)")
	@Mapping(target = "parametriRisposta", expression = "java(nuovoEvento.getParametriRisposta() != null ? convertParametriRisposta(nuovoEvento.getParametriRisposta()) : null)")
//	@Mapping(target = "parametriRichiesta", source="parametriRichiesta", qualifiedByName = "convertParametriRichiesta")
//	@Mapping(target = "parametriRisposta", source="parametriRisposta", qualifiedByName = "convertParametriRisposta")
	@Mapping(target = "datiPagoPA", source="datiPagoPA", qualifiedByName = "convertDatiPagoPA")
	public EventoEntity nuovoEventoToEventoEntity(NuovoEvento nuovoEvento);
	

	
	
	@Named("convertCategoriaEvento")
	public default CategoriaEvento convertCategoriaEvento(it.govpay.gde.beans.CategoriaEvento categoriaEvento) {
		if(categoriaEvento == null) return null;
		
		// CLIENT ("C"), SERVER ("U")
		switch (categoriaEvento) {
		case INTERFACCIA:
			return CategoriaEvento.I;
		case INTERNO:
			return CategoriaEvento.B;
		case UTENTE:
			return CategoriaEvento.U;
		default:
			throw new BadRequestException(MessageFormat.format("Valore {0} non valido per il campo nuovoEvento.categoriaEvento", categoriaEvento));
		}
	}
	
	@Named("convertRuoloEvento")
	public default RuoloEvento convertRuoloEvento(it.govpay.gde.beans.RuoloEvento ruoloEvento) {
		if(ruoloEvento == null) return null;
		
		// INTERNO ("B"), INTERFACCIA ("I"), UTENTE ("U")
		switch (ruoloEvento) {
		case CLIENT:
			return RuoloEvento.C;
		case SERVER:
			return RuoloEvento.S;
		default:
			throw new BadRequestException(MessageFormat.format("Valore {0} non valido per il campo nuovoEvento.ruoloEvento", ruoloEvento));
		}
	}
	
	@Named("convertData")
	public default LocalDateTime offsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
		if(offsetDateTime == null) return null;
		
		return offsetDateTime.toLocalDateTime();
	}
	
	
	public DatiPagoPA nuovoEventoDatiPagoPAToEventoEntityDatiPagoPA(it.govpay.gde.beans.DatiPagoPA datiPagoPA);
	
	@Mapping(target = "dataOraRichiesta", source="dataOraRichiesta", qualifiedByName = "convertData")
	public DettaglioRichiesta nuovoEventoDettaglioRichiestaToEventoEntityDettaglioRichiesta(it.govpay.gde.beans.DettaglioRichiesta dettaglioRichiesta);
	
	@Mapping(target = "dataOraRisposta", source="dataOraRisposta", qualifiedByName = "convertData")
	public DettaglioRisposta nuovoEventoDettaglioRispostaToEventoEntityDettaglioRisposta(it.govpay.gde.beans.DettaglioRisposta dettaglioRisposta);
	
	@Named("convertDatiPagoPA")
	public default String convertDatiPagoPA(it.govpay.gde.beans.DatiPagoPA datiPagoPA) {
		if(datiPagoPA == null) return null;
		DatiPagoPA datiPagoPAEntity = nuovoEventoDatiPagoPAToEventoEntityDatiPagoPA(datiPagoPA);
		
		ObjectMapper objectMapper = JpaConverterObjectMapperFactory.jpaConverterObjectMapper();
		try {
			return objectMapper.writeValueAsString(datiPagoPAEntity);
		} catch (JsonProcessingException ex) {
			throw new AttributeConverterException(ex);
		}
	}
	
	@Named("convertParametriRichiesta")
	public default byte[] convertParametriRichiesta(it.govpay.gde.beans.DettaglioRichiesta dettaglioRichiesta) {
		
		DettaglioRichiesta dettaglioRichiestaEntity = nuovoEventoDettaglioRichiestaToEventoEntityDettaglioRichiesta(dettaglioRichiesta);
		ObjectMapper objectMapper = JpaConverterObjectMapperFactory.jpaConverterObjectMapper();
		try {
			return objectMapper.writeValueAsBytes(dettaglioRichiestaEntity);
		} catch (JsonProcessingException ex) {
			throw new AttributeConverterException(ex);
		}
	}
	
	@Named("convertParametriRisposta")
	public default byte[] convertParametriRisposta(it.govpay.gde.beans.DettaglioRisposta dettaglioRisposta) {
		
		DettaglioRisposta dettaglioRispostaEntity = nuovoEventoDettaglioRispostaToEventoEntityDettaglioRisposta(dettaglioRisposta);
		ObjectMapper objectMapper = JpaConverterObjectMapperFactory.jpaConverterObjectMapper();
		try {
			return objectMapper.writeValueAsBytes(dettaglioRispostaEntity	);
		} catch (JsonProcessingException ex) {
			throw new AttributeConverterException(ex);
		}
	}
}
