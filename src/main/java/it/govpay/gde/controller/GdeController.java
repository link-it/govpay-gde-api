package it.govpay.gde.controller;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import it.govpay.gde.beans.PageInfo;
import it.govpay.gde.beans.RuoloEvento;
import it.govpay.gde.entity.EventoEntity;
import it.govpay.gde.exception.ResourceNotFoundException;
import it.govpay.gde.mapper.EventoMapperImpl;
import it.govpay.gde.mapper.NuovoEventoMapperImpl;
import it.govpay.gde.repository.EventoFilters;
import it.govpay.gde.repository.EventoRepository;
import it.govpay.gde.repository.LimitOffsetPageRequest;
import it.govpay.gde.utils.ListaUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class GdeController implements EventiApi{
	
	private Logger logger = LoggerFactory.getLogger(GdeController.class);
	
	private EventoRepository eventoRepository;
	
	private NuovoEventoMapperImpl nuovoEventoMapperImpl;
	
	private EventoMapperImpl eventoMapperImpl;
	
	public GdeController(EventoRepository eventoRepository, NuovoEventoMapperImpl nuovoEventoMapperImpl, EventoMapperImpl eventoMapperImpl) {
		this.eventoRepository = eventoRepository;
		this.nuovoEventoMapperImpl = nuovoEventoMapperImpl;
		this.eventoMapperImpl = eventoMapperImpl;
    }

	@Override
	public ResponseEntity<Void> addEvento(@Valid NuovoEvento nuovoEvento) {
		this.logger.info("Salvataggio evento: {}", nuovoEvento);
		
		EventoEntity entity = this.nuovoEventoMapperImpl.nuovoEventoToEventoEntity(nuovoEvento);
		
		entity = this.eventoRepository.save(entity);
		
		this.logger.debug("Salvataggio evento completato.");
		
		MultiValueMap<String, String> headers = new HttpHeaders();
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

		headers.add("Location", ListaUtils.createLocation(curRequest, entity.getId()));
		
		return new ResponseEntity<>(headers, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<ListaEventi> findEventi(Long offset,
			Integer limit, OffsetDateTime dataDa, OffsetDateTime dataA,
			String idDominio, String iuv, String ccp,
			String idA2A, String idPendenza, CategoriaEvento categoriaEvento,
			EsitoEvento esito, RuoloEvento ruolo, String sottotipoEvento, String tipoEvento,
			ComponenteEvento componente, Integer severitaDa, Integer severitaA) {
		
		this.logger.info("Ricerca eventi...");
		
		Specification<EventoEntity> spec = creaFiltriDiRicercaDate(dataDa, dataA);
		
		spec = creaFiltriDiRicercaEvento(spec, categoriaEvento, esito, ruolo, sottotipoEvento, tipoEvento, componente);

		spec = creaFiltriDiRicercaDatiPendenza(spec, idDominio, iuv, ccp, idA2A, idPendenza);
		
		spec = creaFiltriDiRicercaSeverita(spec, severitaDa, severitaA);
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, EventoFilters.sort());
		
		Page<EventoEntity> eventi = this.eventoRepository.findAll(spec, pageRequest.pageable);
		
		PageInfo pageInfo = new PageInfo(offset,limit);
		pageInfo.setTotal(eventi.getTotalElements()); 
		
		ListaEventi ret = ListaUtils.buildPaginatedList(eventi, pageRequest.limit, new ListaEventi(pageInfo, null));
		
		for (EventoEntity user : eventi) {
			ret.addItemsItem(this.eventoMapperImpl.eventoEntityToEvento(user));
		}
		
		this.logger.info("Ricerca eventi completata");
		
		return ResponseEntity.ok(ret);
	}
	
	private Specification<EventoEntity> creaFiltriDiRicercaDate(OffsetDateTime dataDa, OffsetDateTime dataA) {
		Specification<EventoEntity> spec = EventoFilters.empty();
		
		if(dataDa != null) {
			spec = spec.and(EventoFilters.byDataDa(dataDa));
		}
		if(dataA != null) {
			spec = spec.and(EventoFilters.byDataA(dataA));
		}
		return spec;
	}

	private Specification<EventoEntity> creaFiltriDiRicercaEvento(Specification<EventoEntity> spec, CategoriaEvento categoriaEvento, EsitoEvento esito,
			RuoloEvento ruolo, String sottotipoEvento, String tipoEvento, ComponenteEvento componente) {
		
		if (spec == null) {
			spec = EventoFilters.empty();
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
	
	private Specification<EventoEntity> creaFiltriDiRicercaDatiPendenza(Specification<EventoEntity> spec, String idDominio,
			String iuv, String ccp, String idA2A, String idPendenza) {
		if (spec == null) {
			spec = EventoFilters.empty();
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
		return spec;
	}
	
	private Specification<EventoEntity> creaFiltriDiRicercaSeverita(Specification<EventoEntity> spec, Integer severitaDa, Integer severitaA) {
		
		if (spec == null) {
			spec = EventoFilters.empty();
		}
		
		if(severitaDa != null) {
			spec = spec.and(EventoFilters.bySeveritaDa(severitaDa));
		}
		if(severitaA != null) {
			spec = spec.and(EventoFilters.bySeveritaA(severitaA));
		}
		return spec;
	}

	@Override
	public ResponseEntity<Evento> getEventoById(Long id) {
		this.logger.info("Lettura evento: {}", id);
		
		ResponseEntity<Evento> res = this.eventoRepository.findById(id).map(this.eventoMapperImpl::eventoEntityToEvento)
		.map(ResponseEntity::ok) 
		.orElseThrow(ResourceNotFoundException::new);
		
		this.logger.info("Lettura evento completata.");
		return res;
	}
}
