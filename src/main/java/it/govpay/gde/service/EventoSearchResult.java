package it.govpay.gde.service;

import java.util.List;

import it.govpay.gde.entity.EventoEntity;

public record EventoSearchResult(List<EventoEntity> items, boolean hasNext, Long total) {
}
