package it.govpay.gde.repository;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govpay.gde.beans.CategoriaEvento;
import it.govpay.gde.beans.ComponenteEvento;
import it.govpay.gde.beans.EsitoEvento;
import it.govpay.gde.beans.RuoloEvento;
import it.govpay.gde.entity.EventoEntity;
import it.govpay.gde.entity.EventoEntity_;

public class EventoFilters {

	private EventoFilters() {}

	public static Specification<EventoEntity> empty() {
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	private static Specification<EventoEntity> addEqualCondition(String attributeName, Object attributeValue) {
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.equal(root.get(attributeName),attributeValue);
	}
	
	public static Specification<EventoEntity> byDataDa(OffsetDateTime dataDa) {
		if (dataDa == null) {
			return null;
		}
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.greaterThanOrEqualTo(root.get(EventoEntity_.DATA),dataDa);
	}

	public static Specification<EventoEntity> byDataA(OffsetDateTime dataA) {
		if (dataA == null) {
			return null;
		}
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.lessThanOrEqualTo(root.get(EventoEntity_.DATA),dataA);
	}

	/**
	 * Tie-break su {@code id} necessario: più eventi possono condividere lo stesso
	 * {@code data} (timestamp non garantito univoco), e la sola colonna data non
	 * basta a dare un ordinamento totale. Senza tie-break la keyset pagination
	 * (vedi EventoSearchService) può saltare o duplicare righe sui pareggi.
	 */
	public static Sort sort() {
		return Sort.by(Direction.DESC, EventoEntity_.DATA).and(Sort.by(Direction.DESC, EventoEntity_.ID));
	}

	public static Specification<EventoEntity> byIdDominioIn(List<String> idDomini) {
		if (idDomini == null || idDomini.isEmpty()) {
			return null;
		}
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		root.get(EventoEntity_.COD_DOMINIO).in(idDomini);
	}

	public static Specification<EventoEntity> byMessaggi(String messaggi) {
		if (messaggi == null || messaggi.isBlank()) {
			return null;
		}
		String pattern = "%" + messaggi.toLowerCase() + "%";
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.or(
				cb.like(cb.lower(root.get(EventoEntity_.SOTTOTIPO_ESITO)), pattern),
				cb.like(cb.lower(root.get(EventoEntity_.DETTAGLIO_ESITO)), pattern));
	}

	public static Specification<EventoEntity> byIuv(String iuv) {
		if (iuv == null) {
			return null;
		}
		return addEqualCondition(EventoEntity_.IUV,iuv);
	}

	public static Specification<EventoEntity> byCcp(String ccp) {
		if (ccp == null) {
			return null;
		}
		return addEqualCondition(EventoEntity_.CCP,ccp);
	}

	public static Specification<EventoEntity> byIdA2A(String idA2A) {
		if (idA2A == null) {
			return null;
		}
		return addEqualCondition(EventoEntity_.COD_APPLICAZIONE,idA2A);
	}

	public static Specification<EventoEntity> byIdPendenza(String idPendenza) {
		if (idPendenza == null) {
			return null;
		}
		return addEqualCondition(EventoEntity_.COD_VERSAMENTO_ENTE,idPendenza);
	}

	public static Specification<EventoEntity> bySeveritaDa(Integer severitaDa) {
		if (severitaDa == null) {
			return null;
		}
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.greaterThanOrEqualTo(root.get(EventoEntity_.SEVERITA),severitaDa);
	}

	public static Specification<EventoEntity> bySeveritaA(Integer severitaA) {
		if (severitaA == null) {
			return null;
		}
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.lessThanOrEqualTo(root.get(EventoEntity_.SEVERITA),severitaA);
	}

	public static Specification<EventoEntity> byTipoEvento(String tipoEvento) {
		if (tipoEvento == null) {
			return null;
		}
		return addEqualCondition(EventoEntity_.TIPO_EVENTO,tipoEvento);
	}

	public static Specification<EventoEntity> bySottotipoEvento(String sottotipoEvento) {
		if (sottotipoEvento == null) {
			return null;
		}
		return addEqualCondition(EventoEntity_.SOTTOTIPO_EVENTO,sottotipoEvento);
	}

	public static Specification<EventoEntity> byComponenteEvento(ComponenteEvento componente) {
		if (componente == null) {
			return null;
		}
		return addEqualCondition(EventoEntity_.COMPONENTE,componente.getValue());
	}

	public static Specification<EventoEntity> byCategoriaEvento(CategoriaEvento categoriaEvento) {
		if (categoriaEvento == null) {
			return null;
		}
		it.govpay.gde.entity.EventoEntity.CategoriaEvento cat = null;
		switch (categoriaEvento) {
		case INTERFACCIA:
			cat = it.govpay.gde.entity.EventoEntity.CategoriaEvento.I;
			break;
		case INTERNO:
			cat = it.govpay.gde.entity.EventoEntity.CategoriaEvento.B;
			break;
		case UTENTE:
			cat = it.govpay.gde.entity.EventoEntity.CategoriaEvento.U;
			break;
		}

		return addEqualCondition(EventoEntity_.CATEGORIA_EVENTO,cat);
	}

	public static Specification<EventoEntity> byEsitoEvento(EsitoEvento esito) {
		if (esito == null) {
			return null;
		}
		it.govpay.gde.entity.EventoEntity.EsitoEvento es = null;
		switch (esito) {
		case FAIL:
			es = it.govpay.gde.entity.EventoEntity.EsitoEvento.FAIL;
			break;
		case KO:
			es = it.govpay.gde.entity.EventoEntity.EsitoEvento.KO;
			break;
		case OK:
			es = it.govpay.gde.entity.EventoEntity.EsitoEvento.OK;
			break;
		}
		return addEqualCondition(EventoEntity_.ESITO_EVENTO,es);
	}

	public static Specification<EventoEntity> byRuoloEvento(RuoloEvento ruolo) {
		if (ruolo == null) {
			return null;
		}
		it.govpay.gde.entity.EventoEntity.RuoloEvento r= null;
		if(RuoloEvento.CLIENT.equals(ruolo)) {
			r = it.govpay.gde.entity.EventoEntity.RuoloEvento.C;
		} else {
			r = it.govpay.gde.entity.EventoEntity.RuoloEvento.S;
		}

		return addEqualCondition(EventoEntity_.RUOLO_EVENTO,r);
	}

}
