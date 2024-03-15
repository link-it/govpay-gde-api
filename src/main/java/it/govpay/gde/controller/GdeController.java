package it.govpay.gde.controller;

import java.time.OffsetDateTime;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govpay.gde.api.EventiApi;
import it.govpay.gde.beans.CategoriaEvento;
import it.govpay.gde.beans.ComponenteEvento;
import it.govpay.gde.beans.EsitoEvento;
import it.govpay.gde.beans.Evento;
import it.govpay.gde.beans.ListaEventi;
import it.govpay.gde.beans.NuovoEvento;
import it.govpay.gde.beans.RuoloEvento;
import it.govpay.gde.entity.EventoEntity;
import it.govpay.gde.exception.ResourceNotFoundException;
import it.govpay.gde.mapper.EventoMapperImpl;
import it.govpay.gde.mapper.NuovoEventoMapperImpl;
import it.govpay.gde.repository.EventoFilters;
import it.govpay.gde.repository.EventoRepository;
import it.govpay.gde.repository.LimitOffsetPageRequest;
import it.govpay.gde.utils.ListaUtils;

@Controller
//@RequestMapping("/api/v1")
public class GdeController implements EventiApi{
	
	private Logger logger = LoggerFactory.getLogger(GdeController.class);
	
	@Autowired
	private EventoRepository eventoRepository;
	
	@Autowired
	NuovoEventoMapperImpl nuovoEventoMapperImpl;
	
	@Autowired
	EventoMapperImpl eventoMapperImpl;

	@Override
	public ResponseEntity<Void> addEvento(NuovoEvento nuovoEvento) {
		this.logger.debug("Salvataggio evento: {}", nuovoEvento);
		
		EventoEntity entity = this.nuovoEventoMapperImpl.nuovoEventoToEventoEntity(nuovoEvento);
		
		entity = this.eventoRepository.save(entity);
		
		this.logger.debug("Salvataggio evento completato.");
		
		MultiValueMap<String, String> headers = new HttpHeaders();
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

		headers.add("Location", ListaUtils.createLocation(curRequest, entity.getId()));
		
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<ListaEventi> findEventi(Long offset,
			Integer limit, OffsetDateTime dataDa, OffsetDateTime dataA,
			String idDominio, String iuv, String ccp,
			String idA2A, String idPendenza, CategoriaEvento categoriaEvento,
			EsitoEvento esito, RuoloEvento ruolo, String sottotipoEvento, String tipoEvento,
			ComponenteEvento componente, Integer severitaDa, Integer severitaA) {
		
		this.logger.debug("Ricerca eventi...");
		
		Specification<EventoEntity> spec = creaFiltriDiRicerca(dataDa, dataA, idDominio, iuv, ccp, idA2A, idPendenza,
				categoriaEvento, esito, ruolo, sottotipoEvento, tipoEvento, componente, severitaDa, severitaA);
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, EventoFilters.sort());
		
		Page<EventoEntity> eventi = this.eventoRepository.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		
		ListaEventi ret = ListaUtils.buildPaginatedList(eventi, pageRequest.limit, curRequest, new ListaEventi());
		
		for (EventoEntity user : eventi) {
			ret.addItemsItem(this.eventoMapperImpl.eventoEntityToEvento(user));
		}
		
		this.logger.debug("Ricerca eventi completata");
		
		return ResponseEntity.ok(ret);
	}

	private Specification<EventoEntity> creaFiltriDiRicerca(OffsetDateTime dataDa, OffsetDateTime dataA, String idDominio,
			String iuv, String ccp, String idA2A, String idPendenza, CategoriaEvento categoriaEvento, EsitoEvento esito,
			RuoloEvento ruolo, String sottotipoEvento, String tipoEvento, ComponenteEvento componente,
			Integer severitaDa, Integer severitaA) {
		Specification<EventoEntity> spec = EventoFilters.empty();
		
		if(dataDa != null) {
			spec = spec.and(EventoFilters.byDataDa(dataDa));
		}
		if(dataA != null) {
			spec = spec.and(EventoFilters.byDataA(dataA));
		}
		if(idDominio != null) {
			spec = spec.and(EventoFilters.byIdDominio(idDominio));
		}
		if(iuv != null) {
			spec = spec.and(EventoFilters.byIuv(iuv));
		}
		if(ccp != null) {
			spec = spec.and(EventoFilters.byCcp(ccp));
		}
		if(idA2A != null) {
			spec = spec.and(EventoFilters.byIdA2A(idA2A));
		}
		if(idPendenza != null) {
			spec = spec.and(EventoFilters.byIdPendenza(idPendenza));
		}
		if(severitaDa != null) {
			spec = spec.and(EventoFilters.bySeveritaDa(severitaDa));
		}
		if(severitaA != null) {
			spec = spec.and(EventoFilters.bySeveritaA(severitaA));
		}
		if(tipoEvento != null) {
			spec = spec.and(EventoFilters.byTipoEvento(tipoEvento));
		}
		if(sottotipoEvento != null) {
			spec = spec.and(EventoFilters.bySottotipoEvento(sottotipoEvento));
		}
		if(componente != null) {
			spec = spec.and(EventoFilters.byComponenteEvento(componente));
		}
		if(categoriaEvento != null) {
			spec = spec.and(EventoFilters.byCategoriaEvento(categoriaEvento));
		}
		if(esito != null) {
			spec = spec.and(EventoFilters.byEsitoEvento(esito));
		}
		if(ruolo != null) {
			spec = spec.and(EventoFilters.byRuoloEvento(ruolo));
		}
		return spec;
	}

	@Override
	public ResponseEntity<Evento> getEventoById(Long id) {
		this.logger.debug("Lettura evento: {}", id);
		
		ResponseEntity<Evento> res = this.eventoRepository.findById(id).map(this.eventoMapperImpl::eventoEntityToEvento)
		.map(ResponseEntity::ok) 
		.orElseThrow(() -> new ResourceNotFoundException());
		
		this.logger.debug("Lettura evento completata.");
		return res;
	}
}
