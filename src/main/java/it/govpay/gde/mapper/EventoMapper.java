package it.govpay.gde.mapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.gde.beans.Evento;
import it.govpay.gde.entity.DatiPagoPA;
import it.govpay.gde.entity.DettaglioRichiesta;
import it.govpay.gde.entity.DettaglioRisposta;
import it.govpay.gde.entity.EventoEntity;
import it.govpay.gde.entity.EventoEntity.CategoriaEvento;
import it.govpay.gde.entity.EventoEntity.RuoloEvento;
import it.govpay.gde.exception.AttributeConverterException;
import it.govpay.gde.utils.JpaConverterObjectMapperFactory;

@Mapper(componentModel = "spring")
public interface EventoMapper {

	
	@Mapping(target = "categoriaEvento", source="categoriaEvento", qualifiedByName = "convertCategoriaEvento")
	@Mapping(target = "ruolo", source="ruoloEvento", qualifiedByName = "convertRuoloEvento")
	@Mapping(target = "esito", source="esitoEvento")
	@Mapping(target = "idDominio", source="codDominio")
	@Mapping(target = "idPendenza", source="codVersamentoEnte")
	@Mapping(target = "idA2A", source="codApplicazione")
	@Mapping(target = "idPagamento", source="idSessione")
	@Mapping(target = "idRiconciliazione", source="idIncasso")
	@Mapping(target = "dataEvento", source="data")
	@Mapping(target = "durataEvento", source="intervallo")
	@Mapping(target = "parametriRichiesta", source="parametriRichiesta", qualifiedByName = "convertParametriRichiesta")
	@Mapping(target = "parametriRisposta", source="parametriRisposta", qualifiedByName = "convertParametriRisposta")
	@Mapping(target = "datiPagoPA", source="datiPagoPA", qualifiedByName = "convertDatiPagoPA")
	public Evento eventoEntityToEvento(EventoEntity evento);
	
	
	@Named("convertCategoriaEvento")
	public default it.govpay.gde.beans.CategoriaEvento convertCategoriaEvento(CategoriaEvento categoriaEvento) {
		if(categoriaEvento == null) return null;
		
		// INTERNO ("B"), INTERFACCIA ("I"), UTENTE ("U")
		switch (categoriaEvento) {
		case I:
			return it.govpay.gde.beans.CategoriaEvento.INTERFACCIA;
		case U:
			return it.govpay.gde.beans.CategoriaEvento.UTENTE;
		case B:
		default:
			return it.govpay.gde.beans.CategoriaEvento.INTERNO;
		}
	}
	
	@Named("convertRuoloEvento")
	public default it.govpay.gde.beans.RuoloEvento convertRuoloEvento(RuoloEvento ruoloEvento) {
		if(ruoloEvento == null) return null;
		
		// CLIENT ("C"), SERVER ("U")
		switch (ruoloEvento) {
		case C:
			return it.govpay.gde.beans.RuoloEvento.CLIENT;
		case S:
		default:
			return it.govpay.gde.beans.RuoloEvento.SERVER;
		}
	}
	
	@Named("convertData")
	public default OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
		if(localDateTime == null) return null;
		
		ZoneId romeZoneId = ZoneId.of("Europe/Rome");
        ZoneOffset zoneOffset = romeZoneId.getRules().getOffset(localDateTime);
		return localDateTime.atOffset(zoneOffset);
	}
	
	public it.govpay.gde.beans.DatiPagoPA eventoEntityDatiPagoPAToEventoDatiPagoPA(DatiPagoPA datiPagoPA);
	
	@Mapping(target = "dataOraRichiesta", source="dataOraRichiesta", qualifiedByName = "convertData")
	public it.govpay.gde.beans.DettaglioRichiesta eventoEntityDettaglioRichiestaToEventoDettaglioRichiesta(DettaglioRichiesta dettaglioRichiesta);
	
	@Mapping(target = "dataOraRisposta", source="dataOraRisposta", qualifiedByName = "convertData")
	public it.govpay.gde.beans.DettaglioRisposta eventoEntityDettaglioRispostaToEventoDettaglioRisposta(DettaglioRisposta dettaglioRisposta);

	
	@Named("convertDatiPagoPA")
	public default it.govpay.gde.beans.DatiPagoPA convertDatiPagoPA(String datiPagoPA) {
		if(datiPagoPA == null) return null;
		ObjectMapper objectMapper = JpaConverterObjectMapperFactory.jpaConverterObjectMapper();
		try {
			DatiPagoPA datiPagoPAEntity = objectMapper.readValue(datiPagoPA, DatiPagoPA.class);
			return eventoEntityDatiPagoPAToEventoDatiPagoPA(datiPagoPAEntity);
		} catch (IOException ex) {
			throw new AttributeConverterException(ex);
		}
	}
	
	@Named("convertParametriRichiesta")
	public default it.govpay.gde.beans.DettaglioRichiesta convertParametriRichiesta(byte[] parametriRichiesta) {
		if(parametriRichiesta == null) return null;
		ObjectMapper objectMapper = JpaConverterObjectMapperFactory.jpaConverterObjectMapper();
		try {
			DettaglioRichiesta dettaglioRichiestaEntity = objectMapper.readValue(parametriRichiesta, DettaglioRichiesta.class);
			return eventoEntityDettaglioRichiestaToEventoDettaglioRichiesta(dettaglioRichiestaEntity);
		} catch (IOException ex) {
			throw new AttributeConverterException(ex);
		}
	}
	
	@Named("convertParametriRisposta")
	public default it.govpay.gde.beans.DettaglioRisposta convertParametriRisposta(byte[] parametriRisposta) {
		if(parametriRisposta == null) return null;
		ObjectMapper objectMapper = JpaConverterObjectMapperFactory.jpaConverterObjectMapper();
		try {
			DettaglioRisposta dettaglioRispostaEntity = objectMapper.readValue(parametriRisposta, DettaglioRisposta.class);
			return eventoEntityDettaglioRispostaToEventoDettaglioRisposta(dettaglioRispostaEntity);
		} catch (IOException ex) {
			throw new AttributeConverterException(ex);
		}
	}
}
