package it.govpay.gde.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import it.govpay.gde.entity.EventoEntity;

public interface EventoRepository extends JpaRepository<EventoEntity, Long>,JpaSpecificationExecutor<EventoEntity> {

}
