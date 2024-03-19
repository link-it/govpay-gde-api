package it.govpay.gde.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter	
@NoArgsConstructor
public class DettaglioRisposta {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime dataOraRisposta;
  private BigDecimal status;
  private List<Header> headers = null;
  private String payload;
}

