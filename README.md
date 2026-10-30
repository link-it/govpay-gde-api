<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay - Porta di accesso al sistema pagoPA - GDE Api

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=link-it_govpay-gde-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=link-it_govpay-gde-api)
[![Docker Hub](https://img.shields.io/docker/v/linkitaly/govpay-gde-api?label=Docker%20Hub&logo=docker)](https://hub.docker.com/r/linkitaly/govpay-gde-api)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-gde-api/main/LICENSE)
[![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Flink-it%2Fgovpay-bom%2Fmain%2Fpom.xml&query=%2F%2F*%5Blocal-name()%3D'spring-boot.version'%5D&label=Spring%20Boot&color=brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

API REST del Giornale degli Eventi (GDE) di GovPay: registra gli eventi prodotti dai componenti GovPay e ne permette la consultazione.

## Sommario

Il Giornale degli Eventi raccoglie, per ogni operazione rilevante, un evento con esito, durata, dati della richiesta e della risposta e riferimenti al pagamento (dominio, IUV, pendenza, flusso di rendicontazione, tracciato). Gli eventi sono salvati nella tabella `eventi` del database di GovPay.

Operazioni esposte (specifica in [src/main/resources/openapi.yaml](src/main/resources/openapi.yaml)):

| Operazione | Metodo e path | Descrizione |
|------------|---------------|-------------|
| `addEvento` | `POST /eventi` | Registra un nuovo evento |
| `findEventi` | `GET /eventi` | Ricerca paginata degli eventi, con filtri e ordinamento |
| `getEventoById` | `GET /eventi/{id}` | Dettaglio di un evento |

Il servizio non applica autenticazione: va esposto solo sulla rete interna dei componenti GovPay.

## Integrazione con GovPay

| Componente | Quando serve | Obbligatorio | Ruolo |
|------------|--------------|--------------|-------|
| Database di GovPay | runtime | sì | Contiene la tabella `eventi` e la sequenza `seq_eventi`, create dagli script di installazione di GovPay: il GDE non crea lo schema |
| [govpay-bom](https://github.com/link-it/govpay-bom) | build | sì | Parent Maven: versioni delle dipendenze e dei plugin |
| [govpay-common](https://github.com/link-it/govpay-common) | build | sì | Libreria comune: transaction id e correlation id nei log |
| Componenti che scrivono eventi | runtime | no | Inviano gli eventi con `POST /eventi` tramite il modulo GDE di `govpay-common`: [govpay-aca-batch](https://github.com/link-it/govpay-aca-batch), [govpay-fdr-batch](https://github.com/link-it/govpay-fdr-batch), [govpay-iban-batch](https://github.com/link-it/govpay-iban-batch), [govpay-maggioli-jppa](https://github.com/link-it/govpay-maggioli-jppa), [govpay-notify-batch](https://github.com/link-it/govpay-notify-batch), [govpay-rt-batch](https://github.com/link-it/govpay-rt-batch), [govpay-tracciati-batch](https://github.com/link-it/govpay-tracciati-batch), [govpay-pendenze-api](https://github.com/link-it/govpay-pendenze-api), [govpay-portal-api](https://github.com/link-it/govpay-portal-api) |
| [govpay-console-api](https://github.com/link-it/govpay-console-api) | runtime | no | Legge gli eventi con `GET /eventi` per mostrarli nella console di gestione |

Chi scrive eventi deve poter raggiungere il GDE: nell'installazione su container descritta nella [documentazione di GovPay](https://govpay.readthedocs.io/it/3.10.x/installazione/container/index.html) i batch partono solo dopo che il GDE risponde all'health check.

## Compilazione

Requisiti: JDK 21 e Maven 3.8.1 o successivo.

```bash
mvn clean install -P jar     # fat jar, per esecuzione standalone e container
mvn clean install -P war     # war, per il deploy su application server
```

Il profilo sceglie il packaging; il risultato è in `target/govpay-gde-api.jar` o `target/govpay-gde-api.war`. La build esegue i test e l'analisi OWASP Dependency-Check delle dipendenze; per escludere l'analisi OWASP, per esempio in sviluppo:

```bash
mvn clean install -P jar -Dowasp=none
```

## Configurazione

Le proprietà si impostano in `application.properties`, da riga di comando (`-D<proprietà>=<valore>`) o con le variabili d'ambiente equivalenti di Spring Boot (es. `spring.datasource.url` → `SPRING_DATASOURCE_URL`). Con l'immagine Docker si usano le variabili descritte in [Esecuzione con Docker](#esecuzione-con-docker).

### Server e API

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `server.port` | `8080` (`10002` nel container) | Porta HTTP del servizio |
| `spring.mvc.servlet.path` | `/` | Base path delle API (nel profilo `dev`: `/api/v1`) |
| `gde.time-zone` | `Europe/Rome` | Fuso orario usato per le date degli eventi |
| `spring.mvc.date-format` | `yyyy-MM-dd` | Formato delle date nei parametri |
| `spring.mvc.datetime-format` | `yyyy-MM-dd'T'HH:mm:ssZZZ` | Formato di data e ora nei parametri |
| `spring.mvc.throw-exception-if-no-handler-found` | `true` | Risponde con un errore applicativo sui path inesistenti |
| `spring.jackson.default-property-inclusion` | `non_null` | Esclude i campi nulli dalle risposte JSON |
| `server.error.whitelabel.enabled` | `true` | Pagina di errore di default di Spring Boot |
| `spring.main.banner-mode` | `off` | Banner di Spring Boot all'avvio |

### Database

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `spring.datasource.url` | - | URL JDBC del database di GovPay (obbligatoria, in alternativa a `spring.datasource.jndiName`) |
| `spring.datasource.jndiName` | - | Nome JNDI del datasource, per il deploy del war su application server |
| `spring.datasource.username` | - | Utente del database |
| `spring.datasource.password` | - | Password del database |
| `spring.datasource.driverClassName` | dedotto dall'URL | Classe del driver JDBC |
| `spring.jpa.mapping-resources` | - | Mapping ORM del database: `META-INF/orm-<h2\|hsql\|mysql\|oracle\|postgres\|sqlserver>.xml` (obbligatoria) |
| `spring.jpa.database-platform` | dedotto dal driver | Dialetto Hibernate |
| `spring.jpa.properties.hibernate.dialect` | dedotto dal driver | Dialetto Hibernate |
| `spring.jpa.hibernate.ddl-auto` | `none` | Gestione dello schema: lasciare `none` o `validate`, lo schema è di GovPay |
| `spring.jpa.open-in-view` | `false` | Sessione JPA limitata al servizio, non estesa alla vista |
| `spring.jpa.properties.hibernate.jdbc.time_zone` | `Europe/Rome` | Fuso orario delle date scritte su database |
| `spring.jpa.properties.hibernate.timezone.default_storage` | `NORMALIZE` | Normalizzazione delle date con fuso orario |
| `spring.datasource.hikari.minimum-idle` | `10` (`2` nel container) | Connessioni minime inattive del pool |
| `spring.datasource.hikari.maximum-pool-size` | `10` (`5` nel container) | Connessioni massime del pool |
| `spring.datasource.hikari.connection-timeout` | `30000` (`20000` nel container) | Attesa massima di una connessione, in ms |
| `spring.datasource.hikari.idle-timeout` | `600000` (`10000` nel container) | Tempo dopo cui una connessione inattiva viene chiusa, in ms |
| `spring.datasource.hikari.max-lifetime` | `1800000` (`600000` nel container) | Durata massima di una connessione, in ms |

I driver JDBC non sono inclusi nel jar: vedi [Esecuzione](#esecuzione).

### Logging

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `logging.file.name` | - (solo console) | File di log; nel container `/var/log/govpay-gde/govpay-gde-api.log` |
| `logging.level.it.govpay` | `INFO` | Livello di log dei package GovPay |
| `logging.pattern.level` | `%5p [%X{transactionId:-},%X{correlationId:-}]` | Aggiunge a ogni riga transaction id e correlation id (header `X-Correlation-ID` o `X-Request-ID`, restituiti anche in risposta) |
| `govpay.logging.livelli-dinamici.enabled` | `false` | Livelli di log letti da database: non attivi nel GDE, che non usa la tabella di configurazione di GovPay |

### Monitoraggio

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `management.server.port` | porta del servizio | Porta dedicata agli endpoint actuator |
| `management.endpoints.web.base-path` | `/actuator` | Base path degli endpoint actuator |
| `management.endpoints.web.exposure.include` | `health,prometheus` (variabile `MANAGEMENT_ENDPOINTS`) | Endpoint actuator esposti |
| `management.metrics.distribution.percentiles-histogram.http.server.requests` | `true` | Istogrammi delle latenze, per aggregare i percentili tra più istanze |
| `management.metrics.distribution.slo.http.server.requests` | `100ms,500ms,1s,2s,5s` | Soglie esplicite di latenza, compresa quella SLA pagoPA di 2s |
| `management.metrics.tags.application` | `govpay-gde-api` | Tag che identifica il servizio nelle metriche |

## Esecuzione

### Driver JDBC

I driver JDBC **non sono inclusi** nel fat jar e vanno forniti a runtime. Creare una directory (es. `jdbc-drivers/`) e copiarvi il driver del database utilizzato:

| Database   | Driver |
|------------|--------|
| PostgreSQL | [postgresql-42.7.9.jar](https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.9/postgresql-42.7.9.jar) |
| MySQL      | [mysql-connector-j-9.6.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.6.0/mysql-connector-j-9.6.0.jar) |
| Oracle     | [ojdbc11-23.26.1.0.0.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.26.1.0.0/ojdbc11-23.26.1.0.0.jar) |
| SQL Server | [mssql-jdbc-12.8.2.jre11.jar](https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/12.8.2.jre11/mssql-jdbc-12.8.2.jre11.jar) |
| H2         | [h2-2.4.240.jar](https://repo1.maven.org/maven2/com/h2database/h2/2.4.240/h2-2.4.240.jar) |

### Jar standalone

Il jar usa `PropertiesLauncher`: la proprietà `loader.path` indica la directory dei driver JDBC. Configurazione minima per PostgreSQL:

```bash
java -Dloader.path=./jdbc-drivers \
     -Dserver.port=10002 \
     -Dspring.datasource.url=jdbc:postgresql://localhost:5432/govpay \
     -Dspring.datasource.username=govpay \
     -Dspring.datasource.password=<password> \
     -Dspring.jpa.mapping-resources=META-INF/orm-postgres.xml \
     -jar target/govpay-gde-api.jar
```

### War su application server

Il war usa il datasource dell'application server, indicato con `spring.datasource.jndiName` (es. `java:comp/env/it.govpay.datasource`). Dove trovarlo:

| Origine | Dove si trova |
|---------|---------------|
| Release (tag) | Allegato alla [release GitHub](https://github.com/link-it/govpay-gde-api/releases) come `govpay-gde-api-<tag>.war` |
| Sviluppo (push su `main`, versione SNAPSHOT) | Nell'immagine `linkitaly/govpay-gde-api-dev:<versione>`, in `/opt/govpay-gde/dist/govpay-gde-api.war` |
| Qualsiasi build | Artifact `govpay-gde-api` della run di GitHub Actions (retention 90 giorni) |
| Build locale | `mvn clean install -P war`, in `target/govpay-gde-api.war` |

Per estrarre il war dall'immagine di sviluppo (la versione è quella del `pom.xml` su `main`):

```bash
id=$(docker create linkitaly/govpay-gde-api-dev:2.0.1-SNAPSHOT)
docker cp "${id}:/opt/govpay-gde/dist/govpay-gde-api.war" ./govpay-gde-api.war
docker rm "${id}"
```

## Esecuzione con Docker

L'immagine è pubblicata su Docker Hub come [linkitaly/govpay-gde-api](https://hub.docker.com/r/linkitaly/govpay-gde-api) (release) e `linkitaly/govpay-gde-api-dev` (sviluppo, a ogni push su `main`). Il servizio risponde sulla porta `10002`; i driver JDBC si montano in `/opt/jdbc-drivers`.

```bash
docker run -d --name govpay-gde-api -p 10002:10002 \
  -v ./jdbc-drivers:/opt/jdbc-drivers \
  -e GOVPAY_DB_TYPE=postgresql \
  -e GOVPAY_DB_SERVER=postgres:5432 \
  -e GOVPAY_DB_NAME=govpay \
  -e GOVPAY_DB_USER=govpay \
  -e GOVPAY_DB_PASSWORD=<password> \
  linkitaly/govpay-gde-api
```

Variabili d'ambiente dell'immagine, oltre alle proprietà Spring Boot in forma di variabile (es. `LOGGING_LEVEL_IT_GOVPAY=DEBUG`):

| Variabile | Obbligatoria | Default | Descrizione |
|-----------|--------------|---------|-------------|
| `GOVPAY_DB_TYPE` | sì | - | Tipo di database: `postgresql`, `mysql`, `mariadb`, `oracle` |
| `GOVPAY_DB_SERVER` | sì | - | Server del database, `host` o `host:porta` (porta di default del database se omessa) |
| `GOVPAY_DB_NAME` | sì | - | Nome del database (per Oracle, service name o SID) |
| `GOVPAY_DB_USER` | sì | - | Utente del database |
| `GOVPAY_DB_PASSWORD` | sì | - | Password del database |
| `GOVPAY_DS_CONN_PARAM` | no | - | Parametri aggiunti all'URL JDBC (es. `zeroDateTimeBehavior=convertToNull` per MySQL) |
| `GOVPAY_DS_JDBC_LIBS` | no | `/opt/jdbc-drivers` | Directory dei driver JDBC |
| `GOVPAY_ORACLE_JDBC_URL_TYPE` | no | `servicename` | Formato dell'URL Oracle: `servicename` o `sid` |
| `ORACLE_TNS_ADMIN` | no | - | Directory dei file TNS di Oracle |
| `GOVPAY_GDE_MIN_POOL` | no | `2` | Connessioni minime inattive del pool |
| `GOVPAY_GDE_MAX_POOL` | no | `5` | Connessioni massime del pool |
| `SERVER_PORT` | no | `10002` | Porta HTTP del servizio |
| `MANAGEMENT_SERVER_PORT` | no | porta del servizio | Porta dedicata agli endpoint actuator |
| `MANAGEMENT_ENDPOINTS` | no | `health,prometheus` | Endpoint actuator esposti |
| `GOVPAY_GDE_JVM_MAX_RAM_PERCENTAGE` | no | `80` | `-XX:MaxRAMPercentage` della JVM |
| `GOVPAY_GDE_JVM_INITIAL_RAM_PERCENTAGE` | no | - | `-XX:InitialRAMPercentage` della JVM |
| `GOVPAY_GDE_JVM_MIN_RAM_PERCENTAGE` | no | - | `-XX:MinRAMPercentage` della JVM |
| `GOVPAY_GDE_JVM_MAX_METASPACE_SIZE` | no | - | `-XX:MaxMetaspaceSize` della JVM |
| `GOVPAY_GDE_JVM_MAX_DIRECT_MEMORY_SIZE` | no | - | `-XX:MaxDirectMemorySize` della JVM |
| `JAVA_OPTS` | no | - | Opzioni aggiuntive della JVM |

Costruzione dell'immagine in locale, scansione delle vulnerabilità e pacchetti inclusi sono descritti in [docker/DOCKER.md](docker/DOCKER.md).

## Monitoraggio

Gli endpoint actuator rispondono di default sulla porta del servizio:

```bash
curl http://localhost:10002/actuator/health       # stato, usato anche dalle probe dei container
curl http://localhost:10002/actuator/prometheus   # metriche in formato Prometheus
```

Per spostarli su una porta dedicata impostare `management.server.port` (nel container `MANAGEMENT_SERVER_PORT`). L'endpoint `loggers`, che permette di cambiare i livelli di log a caldo, non è esposto di default perché il servizio non ha autenticazione: abilitarlo solo insieme a una porta dedicata e non raggiungibile dall'esterno, per esempio `MANAGEMENT_SERVER_PORT=9090` e `MANAGEMENT_ENDPOINTS=health,prometheus,loggers`.

## Licenza

GovPay GDE Api è distribuito con licenza [GNU General Public License v3.0](LICENSE).
