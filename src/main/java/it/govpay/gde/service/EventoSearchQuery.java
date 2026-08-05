package it.govpay.gde.service;

import java.time.OffsetDateTime;
import java.util.List;

import it.govpay.gde.beans.CategoriaEvento;
import it.govpay.gde.beans.ComponenteEvento;
import it.govpay.gde.beans.EsitoEvento;
import it.govpay.gde.beans.PagingMode;
import it.govpay.gde.beans.RuoloEvento;

public record EventoSearchQuery(
		Long offset,
		Integer limit,
		OffsetDateTime dataDa,
		OffsetDateTime dataA,
		List<String> idDominio,
		String iuv,
		String ccp,
		String idA2A,
		String idPendenza,
		CategoriaEvento categoriaEvento,
		EsitoEvento esito,
		RuoloEvento ruolo,
		String sottotipoEvento,
		String tipoEvento,
		ComponenteEvento componente,
		Integer severitaDa,
		Integer severitaA,
		String messaggi,
		boolean total,
		PagingMode pagingMode,
		OffsetDateTime cursorData,
		Long cursorId) {

	private static final int LIMIT_DEFAULT_VALUE = 25;

	public int limitOrDefault() {
		return limit == null ? LIMIT_DEFAULT_VALUE : limit;
	}

	public long offsetOrDefault() {
		return (offset == null || offset < 0) ? 0 : offset;
	}

	public boolean isCursorMode() {
		return pagingMode == PagingMode.CURSOR;
	}
}
