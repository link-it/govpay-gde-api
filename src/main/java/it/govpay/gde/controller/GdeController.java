package it.govpay.gde.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import it.govpay.gde.beans.PagingMode;
import it.govpay.gde.beans.RuoloEvento;
import it.govpay.gde.entity.EventoEntity;
import it.govpay.gde.exception.ResourceNotFoundException;
import it.govpay.gde.mapper.EventoMapperImpl;
import it.govpay.gde.mapper.NuovoEventoMapperImpl;
import it.govpay.gde.repository.EventoRepository;
import it.govpay.gde.service.EventoSearchQuery;
import it.govpay.gde.service.EventoSearchResult;
import it.govpay.gde.service.EventoSearchService;
import it.govpay.gde.utils.ListaUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class GdeController implements EventiApi{

	private Logger logger = LoggerFactory.getLogger(GdeController.class);

	private EventoRepository eventoRepository;

	private NuovoEventoMapperImpl nuovoEventoMapperImpl;

	private EventoMapperImpl eventoMapperImpl;

	private EventoSearchService eventoSearchService;

	public GdeController(EventoRepository eventoRepository, NuovoEventoMapperImpl nuovoEventoMapperImpl,
			EventoMapperImpl eventoMapperImpl, EventoSearchService eventoSearchService) {
		this.eventoRepository = eventoRepository;
		this.nuovoEventoMapperImpl = nuovoEventoMapperImpl;
		this.eventoMapperImpl = eventoMapperImpl;
		this.eventoSearchService = eventoSearchService;
    }

	@Override
	public ResponseEntity<Void> addEvento(@Valid NuovoEvento nuovoEvento) {
		this.logger.info("Salvataggio evento [componente={}, categoria={}, tipo={}/{}, esito={}, idDominio={}, iuv={}]",
				nuovoEvento.getComponente(), nuovoEvento.getCategoriaEvento(),
				nuovoEvento.getTipoEvento(), nuovoEvento.getSottotipoEvento(),
				nuovoEvento.getEsito(), nuovoEvento.getIdDominio(), nuovoEvento.getIuv());
		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Salvataggio evento - payload completo: {}", nuovoEvento);
		}

		EventoEntity entity = this.nuovoEventoMapperImpl.nuovoEventoToEventoEntity(nuovoEvento);
		
		entity = this.eventoRepository.save(entity);

		this.logger.info("Salvataggio evento completato [id={}]", entity.getId());
		
		HttpHeaders headers = new HttpHeaders();

		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

		headers.add("Location", ListaUtils.createLocation(curRequest, entity.getId()));

		return ResponseEntity.status(HttpStatus.CREATED).headers(headers).build();
	}

	@Override
	public ResponseEntity<ListaEventi> findEventi(Long offset,
			Integer limit, OffsetDateTime dataDa, OffsetDateTime dataA,
			List<String> idDominio, String iuv, String ccp,
			String idA2A, String idPendenza, CategoriaEvento categoriaEvento,
			EsitoEvento esito, RuoloEvento ruolo, String sottotipoEvento, String tipoEvento,
			ComponenteEvento componente, Integer severitaDa, Integer severitaA,
			String messaggi, Boolean total, PagingMode pagingMode,
			OffsetDateTime cursorData, Long cursorId) {

		this.logger.debug("Ricerca eventi [pagingMode={}, total={}]...", pagingMode, total);

		EventoSearchQuery query = new EventoSearchQuery(offset, limit, dataDa, dataA, idDominio, iuv, ccp,
				idA2A, idPendenza, categoriaEvento, esito, ruolo, sottotipoEvento, tipoEvento, componente,
				severitaDa, severitaA, messaggi, Boolean.TRUE.equals(total), pagingMode, cursorData, cursorId);

		EventoSearchResult result = this.eventoSearchService.search(query);

		PageInfo pageInfo = query.isCursorMode()
				? new PageInfo(0L, query.limitOrDefault())
				: new PageInfo(query.offsetOrDefault(), query.limitOrDefault());
		pageInfo.setHasNext(result.hasNext());
		pageInfo.setTotal(result.total());

		ListaEventi ret = new ListaEventi(pageInfo, null);
		for (EventoEntity evento : result.items()) {
			ret.addItemsItem(this.eventoMapperImpl.eventoEntityToEvento(evento));
		}

		this.logger.debug("Ricerca eventi completata [trovati={}, hasNext={}]", result.items().size(), result.hasNext());

		return ResponseEntity.ok(ret);
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
