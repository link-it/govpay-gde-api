package it.govpay.gde.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter	
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(
		name = "eventi", 
		indexes = {
				@Index(name="idx_evt_data", columnList = "data"),
				@Index(name="idx_evt_fk_vrs", columnList = "cod_applicazione,cod_versamento_ente"),
				@Index(name="idx_evt_id_sessione", columnList = "id_sessione"),
				@Index(name="idx_evt_iuv", columnList = "iuv"),
				@Index(name="idx_evt_fk_fr", columnList = "id_fr"),
				@Index(name="idx_evt_fk_inc", columnList = "id_incasso"),
				@Index(name="idx_evt_fk_trac", columnList = "id_tracciato")
		}
)
public class EventoEntity {
	
	public enum CategoriaEvento { B, I, U }
	
	public enum RuoloEvento { C, S }
	
	public enum EsitoEvento { OK, KO, FAIL }
	
	@Id
	@SequenceGenerator(name="seq_eventi",sequenceName="seq_eventi", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_eventi")
	private Long id;
	
	@Column(name = "componente")
	private String componente;
	
	@Column(name = "categoria_evento")
	@Enumerated(EnumType.STRING)
	private CategoriaEvento categoriaEvento;
	
	@Column(name = "ruolo")
	@Enumerated(EnumType.STRING)
	private RuoloEvento ruoloEvento;
	
	@Column(name = "tipo_evento")
	private String tipoEvento;
	
	@Column(name = "sottotipo_evento")
	private String sottotipoEvento;
	
	@Column(name = "intervallo")
	private Long intervallo;
	
	@Column(name = "data")
	private OffsetDateTime data;
	
	@Column(name = "esito")
	@Enumerated(EnumType.STRING)
	private EsitoEvento esitoEvento;
	
	@Column(name = "sottotipo_esito")
	private String sottotipoEsito;
	
	@Column(name = "dettaglio_esito")
	private String dettaglioEsito;
	
//	@Lob
//	@Convert(converter = DatiPagoPAConverter.class)
	@Type(type = "org.hibernate.type.TextType")
//	private String descrizione;
	@Column(name = "dati_pago_pa")
	private String datiPagoPA;
	
	@Column(name = "cod_dominio")
	private String codDominio;
	
	@Column(name = "iuv")
	private String iuv;
	
	@Column(name = "ccp")
	private String ccp;
	
	@Column(name = "cod_versamento_ente")
	private String codVersamentoEnte;
	
	@Column(name = "cod_applicazione")
	private String codApplicazione;
	
	@Column(name = "id_sessione")
	private String idSessione;
	
	@Column(name = "id_tracciato")
	private Long idTracciato;
	
	@Column(name = "id_fr")
	private Long idFr;
	
	@Column(name = "id_incasso")
	private Long idIncasso;
	
	@Column(name = "severita")
	private Integer severita;
	
	@Column(name = "cluster_id")
	private String clusterId;
	
	@Column(name = "transaction_id")
	private String transactionId;
	
	@Lob
	@Type(type="org.hibernate.type.BinaryType")
//	@Convert(converter = DettaglioRichiestaConverter.class)
	@Column(name = "parametri_richiesta")
	private byte[] parametriRichiesta;
	
	@Lob
	@Type(type="org.hibernate.type.BinaryType")
//	@Convert(converter = DettaglioRispostaConverter.class)
	@Column(name = "parametri_risposta")
	private byte[] parametriRisposta;
	
}
