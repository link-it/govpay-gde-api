package it.govpay.gde.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@NoArgsConstructor
public class DettaglioRichiesta {

  private String principal;
  private String utente;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime dataOraRichiesta;
  private String url;
  private List<Header> headers = null;
  private String payload;
  private String method;
}

