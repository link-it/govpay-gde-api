package it.govpay.gde.repository;

import java.time.OffsetDateTime;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.greaterThanOrEqualTo(root.get(EventoEntity_.DATA),dataDa);
	}

	public static Specification<EventoEntity> byDataA(OffsetDateTime dataA) {
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.lessThanOrEqualTo(root.get(EventoEntity_.DATA),dataA);
	}

	public static Sort sort() {
		return Sort.by(Direction.DESC, EventoEntity_.DATA);
	}

	public static Specification<EventoEntity> byIdDominio(String idDominio) {
		return addEqualCondition(EventoEntity_.COD_DOMINIO,idDominio);
	}

	public static Specification<EventoEntity> byIuv(String iuv) {
		return addEqualCondition(EventoEntity_.IUV,iuv);
	}

	public static Specification<EventoEntity> byCcp(String ccp) {
		return addEqualCondition(EventoEntity_.CCP,ccp);
	}

	public static Specification<EventoEntity> byIdA2A(String idA2A) {
		return addEqualCondition(EventoEntity_.COD_APPLICAZIONE,idA2A);
	}

	public static Specification<EventoEntity> byIdPendenza(String idPendenza) {
		return addEqualCondition(EventoEntity_.COD_VERSAMENTO_ENTE,idPendenza);
	}

	public static Specification<EventoEntity> bySeveritaDa(Integer severitaDa) {
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.greaterThanOrEqualTo(root.get(EventoEntity_.SEVERITA),severitaDa);
	}

	public static Specification<EventoEntity> bySeveritaA(Integer severitaA) {
		return (Root<EventoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		cb.lessThanOrEqualTo(root.get(EventoEntity_.SEVERITA),severitaA);
	}

	public static Specification<EventoEntity> byTipoEvento(String tipoEvento) {
		return addEqualCondition(EventoEntity_.TIPO_EVENTO,tipoEvento);
	}

	public static Specification<EventoEntity> bySottotipoEvento(String sottotipoEvento) {
		return addEqualCondition(EventoEntity_.SOTTOTIPO_EVENTO,sottotipoEvento);
	}

	public static Specification<EventoEntity> byComponenteEvento(ComponenteEvento componente) {
		return addEqualCondition(EventoEntity_.COMPONENTE,componente.getValue());
	}

	public static Specification<EventoEntity> byCategoriaEvento(CategoriaEvento categoriaEvento) {
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
		it.govpay.gde.entity.EventoEntity.RuoloEvento r= null;
		switch (ruolo) {
		case CLIENT:
			r = it.govpay.gde.entity.EventoEntity.RuoloEvento.C;
			break;
		case SERVER:
			r = it.govpay.gde.entity.EventoEntity.RuoloEvento.S;
			break;
		}
		
		return addEqualCondition(EventoEntity_.RUOLO_EVENTO,r);
	}

}
