package it.govpay.gde.entity;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@NoArgsConstructor
public class DatiPagoPA {

  private String idPsp;
  private String idCanale;
  private String idIntermediarioPsp;
  private String tipoVersamento;
  private String modelloPagamento;
  private String idDominio;
  private String idIntermediario;
  private String idStazione;
  private String idRiconciliazione;
  private String sct;
  private String idFlusso;
  private BigDecimal idTracciato;
  private String identificativoErogatore;
  private String identificativoFruitore;
}

