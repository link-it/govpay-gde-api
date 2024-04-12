INSERT INTO eventi (
componente,ruolo,categoria_evento,tipo_evento,sottotipo_evento,data,intervallo,esito,sottotipo_esito,
dettaglio_esito,parametri_richiesta,parametri_risposta,dati_pago_pa,
cod_versamento_ente,cod_applicazione,iuv,ccp,cod_dominio,id_sessione,severita,cluster_id,transaction_id,id_fr,id_incasso,id_tracciato,id) 
VALUES (
'API_BACKOFFICE','S','B','addTipoPendenza',null,'2023-01-06 10:01:53.238',77,'OK','200',
null,null,null,null,
'idPendenza_01','idA2A01',null,null,null,null,null,'GovPay','fb695ba5-dbcb-4e11-bcf6-561bce720520',null,null,null,nextval('public.seq_eventi'));
INSERT INTO eventi (
componente,ruolo,categoria_evento,tipo_evento,sottotipo_evento,data,intervallo,esito,sottotipo_esito,
dettaglio_esito,parametri_richiesta,parametri_risposta,dati_pago_pa,
cod_versamento_ente,cod_applicazione,iuv,ccp,cod_dominio,id_sessione,severita,cluster_id,transaction_id,id_fr,id_incasso,id_tracciato,id) 
VALUES (
'API_PENDENZE','S','I','getPendenza','sottotipoEvento_1','2024-02-06 10:01:53.238',77,'OK','200',
null,null,null,null,
null,null,'45678012345123456','1234561234576','12345678901',null,3,'GovPay','fb695ba5-dbcb-4e11-bcf6-561bce720521',null,null,null,nextval('public.seq_eventi'));