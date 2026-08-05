package it.govpay.gde.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govpay.gde.entity.EventoEntity;
import it.govpay.gde.entity.EventoEntity_;
import it.govpay.gde.exception.BadRequestException;
import it.govpay.gde.repository.EventoFilters;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Ricerca eventi in due modalità mutuamente esclusive (vedi {@link it.govpay.gde.beans.PagingMode}):
 * OFFSET (classica, costo crescente con la profondità) e CURSOR (keyset su
 * {@code data DESC, id DESC}, costo indipendente dalla profondità — pensata per
 * una tabella che può crescere a milioni di righe). Il conteggio totale è
 * sempre calcolato a parte, solo su richiesta esplicita ({@code total=true}):
 * è l'operazione più costosa e non serve alla sola navigazione pagina per pagina.
 */
@Service
public class EventoSearchService {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public EventoSearchResult search(EventoSearchQuery query) {
		validate(query);

		Specification<EventoEntity> spec = buildSpecification(query);
		int limit = query.limitOrDefault();

		List<EventoEntity> sliced = query.isCursorMode()
				? findByCursor(spec, query.cursorData(), query.cursorId(), limit + 1)
				: findSlice(spec, query.offsetOrDefault(), limit + 1);

		boolean hasNext = sliced.size() > limit;
		List<EventoEntity> items = hasNext ? sliced.subList(0, limit) : sliced;

		Long total = query.total() ? countMatching(spec) : null;

		return new EventoSearchResult(items, hasNext, total);
	}

	private void validate(EventoSearchQuery query) {
		boolean cursorGiven = query.cursorData() != null || query.cursorId() != null;
		if (cursorGiven && !query.isCursorMode()) {
			throw new BadRequestException("cursorData/cursorId richiedono pagingMode=CURSOR");
		}
		if (query.isCursorMode() && (query.cursorData() == null) != (query.cursorId() == null)) {
			throw new BadRequestException("cursorData e cursorId vanno forniti insieme");
		}
	}

	private Specification<EventoEntity> buildSpecification(EventoSearchQuery query) {
		return Specification.allOf(
				Stream.of(
						EventoFilters.byDataDa(query.dataDa()),
						EventoFilters.byDataA(query.dataA()),
						EventoFilters.byIdDominioIn(query.idDominio()),
						EventoFilters.byIuv(query.iuv()),
						EventoFilters.byCcp(query.ccp()),
						EventoFilters.byIdA2A(query.idA2A()),
						EventoFilters.byIdPendenza(query.idPendenza()),
						EventoFilters.byCategoriaEvento(query.categoriaEvento()),
						EventoFilters.byEsitoEvento(query.esito()),
						EventoFilters.byRuoloEvento(query.ruolo()),
						EventoFilters.bySottotipoEvento(query.sottotipoEvento()),
						EventoFilters.byTipoEvento(query.tipoEvento()),
						EventoFilters.byComponenteEvento(query.componente()),
						EventoFilters.bySeveritaDa(query.severitaDa()),
						EventoFilters.bySeveritaA(query.severitaA()),
						EventoFilters.byMessaggi(query.messaggi()))
				.filter(Objects::nonNull)
				.toList());
	}

	private List<EventoEntity> findByCursor(Specification<EventoEntity> spec, OffsetDateTime cursorData, Long cursorId, int maxResults) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<EventoEntity> q = cb.createQuery(EventoEntity.class);
		Root<EventoEntity> root = q.from(EventoEntity.class);

		Predicate specPredicate = spec.toPredicate(root, q, cb);
		Path<OffsetDateTime> dataPath = root.get(EventoEntity_.DATA);
		Path<Long> idPath = root.get(EventoEntity_.ID);

		Predicate where = specPredicate;
		if (cursorData != null) {
			Predicate keyset = cb.or(
					cb.lessThan(dataPath, cursorData),
					cb.and(cb.equal(dataPath, cursorData), cb.lessThan(idPath, cursorId)));
			where = where != null ? cb.and(where, keyset) : keyset;
		}
		if (where != null) {
			q.where(where);
		}
		q.orderBy(cb.desc(dataPath), cb.desc(idPath));

		return entityManager.createQuery(q).setMaxResults(maxResults).getResultList();
	}

	private List<EventoEntity> findSlice(Specification<EventoEntity> spec, long offset, int maxResults) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<EventoEntity> q = cb.createQuery(EventoEntity.class);
		Root<EventoEntity> root = q.from(EventoEntity.class);

		Predicate predicate = spec.toPredicate(root, q, cb);
		if (predicate != null) {
			q.where(predicate);
		}
		List<Order> orders = new ArrayList<>();
		for (Sort.Order o : EventoFilters.sort()) {
			Path<Object> path = root.get(o.getProperty());
			orders.add(o.isAscending() ? cb.asc(path) : cb.desc(path));
		}
		q.orderBy(orders);

		TypedQuery<EventoEntity> typed = entityManager.createQuery(q);
		// setFirstResult vuole un int: un offset oltre Integer.MAX_VALUE non ha
		// comunque senso pratico su una ricerca paginata, lo tronchiamo.
		typed.setFirstResult((int) Math.min(offset, Integer.MAX_VALUE));
		typed.setMaxResults(maxResults);
		return typed.getResultList();
	}

	private Long countMatching(Specification<EventoEntity> spec) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> q = cb.createQuery(Long.class);
		Root<EventoEntity> root = q.from(EventoEntity.class);
		Predicate predicate = spec.toPredicate(root, q, cb);
		q.select(cb.count(root));
		if (predicate != null) {
			q.where(predicate);
		}
		return entityManager.createQuery(q).getSingleResult();
	}
}
