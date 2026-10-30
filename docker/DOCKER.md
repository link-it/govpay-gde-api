# GovPay GDE (Giornale degli Eventi) API - Configurazione Docker

Containerizzazione Docker per il servizio REST API GovPay GDE che fornisce registrazione degli eventi e accesso al giornale per le applicazioni GovPay.

## Panoramica

Questa configurazione Docker fornisce:
- Supporto multi-database (PostgreSQL, MySQL/MariaDB, Oracle)
- Servizio REST API leggero
- Controlli di salute e monitoraggio
- Connection pooling configurabile
- Mappatura ORM per diversi database
- Integrazione pronta per GovPay A.C.A. e altri servizi

## Avvio Rapido

### 1. Costruire l'Immagine Docker

```bash
# Build con supporto PostgreSQL (predefinito)
./build_image.sh

# Build con database specifico
./build_image.sh -v 3.8.0 -d postgresql
./build_image.sh -v 3.8.0 -d mysql
./build_image.sh -v 3.8.0 -d oracle
```

### 2. Configurare l'Ambiente

```bash
# Copiare il template e modificarlo
cp .env.template .env
nano .env
```

**Configurazione richiesta:**
- `GOVPAY_DB_TYPE`: Tipo di database (postgresql, mysql, mariadb, oracle)
- `GOVPAY_DB_SERVER`: Server del database (formato: host:porta)
- `GOVPAY_DB_NAME`: Nome del database
- `GOVPAY_DB_USER`: Username del database
- `GOVPAY_DB_PASSWORD`: Password del database

### 3. Avviare i Servizi

```bash
# Avviare con docker-compose
docker-compose up -d

# Visualizzare i log
docker-compose logs -f govpay-gde-api

# Verificare lo stato
docker-compose ps

# Testare l'API
curl http://localhost:10002/actuator/health
```

## Driver JDBC

I driver JDBC **non sono inclusi** nel fat jar e devono essere forniti esternamente.
L'entrypoint imposta automaticamente `LOADER_PATH=/opt/jdbc-drivers` per il `PropertiesLauncher` di Spring Boot.

I driver devono essere montati come volume nella directory `/opt/jdbc-drivers`:

```bash
docker run -v ./jdbc-drivers:/opt/jdbc-drivers linkitaly/govpay-gde-api
```

Per ulteriori dettagli sui driver supportati, consultare [jdbc-drivers/README.md](jdbc-drivers/README.md).

## Architettura

```
┌─────────────────────────────────────┐
│      GovPay GDE REST API            │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Spring Boot Application    │  │
│  │   - REST Controllers         │  │
│  │   - Event Service            │  │
│  │   - JPA Repositories         │  │
│  └──────────────────────────────┘  │
│              │                      │
│              ▼                      │
│  ┌──────────────────────────────┐  │
│  │   HikariCP Connection Pool   │  │
│  └──────────────────────────────┘  │
└─────────────────┬───────────────────┘
                  │
                  ▼
      ┌───────────────────────┐
      │   Database (RDBMS)    │
      │  - PostgreSQL         │
      │  - MySQL/MariaDB      │
      │  - Oracle             │
      └───────────────────────┘
```

## Configurazione Database

### PostgreSQL (Predefinito)

```env
GOVPAY_DB_TYPE=postgresql
GOVPAY_DB_SERVER=postgres:5432
GOVPAY_DB_NAME=govpay
GOVPAY_DB_USER=govpay
GOVPAY_DB_PASSWORD=password_sicura
```

### MySQL/MariaDB

```env
GOVPAY_DB_TYPE=mysql
GOVPAY_DB_SERVER=mysql:3306
GOVPAY_DB_NAME=govpay
GOVPAY_DB_USER=govpay
GOVPAY_DB_PASSWORD=password_sicura
GOVPAY_DS_CONN_PARAM=zeroDateTimeBehavior=convertToNull
```

Modificare `docker-compose.yml` per decommentare il servizio MySQL.

### Oracle

```env
GOVPAY_DB_TYPE=oracle
GOVPAY_DB_SERVER=oracle:1521
GOVPAY_DB_NAME=XE
GOVPAY_DB_USER=govpay
GOVPAY_DB_PASSWORD=password_sicura
GOVPAY_ORACLE_JDBC_URL_TYPE=servicename  # oppure 'sid'
```


## Integrazione con GovPay A.C.A.

Per utilizzare GDE con il processore batch A.C.A.:

### 1. Condividere il Database

Entrambi i servizi possono utilizzare lo stesso database:

```yaml
# In docker-compose.yml per entrambi i servizi
networks:
  - govpay-network
```

### 2. Configurare A.C.A. per Utilizzare GDE

Nel file `.env` di A.C.A.:
```env
GDE_ENABLED=true
GDE_BASE_URL=http://govpay-gde-api:10002
```

### 3. Esempio di docker-compose.yml Combinato

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    # ... shared database config

  govpay-gde-api:
    image: linkitaly/govpay-gde:3.8.0_postgresql
    depends_on:
      - postgres
    # ... GDE config

  govpay-aca:
    image: linkitaly/govpay-aca:3.8.0_postgresql
    depends_on:
      - postgres
      - govpay-gde-api
    environment:
      IT_GOVPAY_GDE_ENABLED: "true"
      IT_GOVPAY_GDE_CLIENT_BASEURL: "http://govpay-gde-api:10002"
    # ... ACA config

networks:
  govpay-network:
```

## Riferimento Configurazione

### Variabili d'Ambiente

L'elenco completo delle variabili d'ambiente dell'immagine e delle proprietà configurabili è nel [README](../README.md#esecuzione-con-docker).

### Impostazioni Predefinite

Queste sono incorporate nell'immagine Docker:
- Timezone: `Europe/Rome`
- Timeout connessione: `20000ms`
- Timeout idle: `10000ms`
- File di log: `/var/log/govpay/govpay-gde-api.log`

### File di Mappatura ORM

Il servizio seleziona automaticamente la mappatura ORM corretta in base al tipo di database:
- **PostgreSQL**: `META-INF/orm-postgres.xml`
- **MySQL/MariaDB**: `META-INF/orm-mysql.xml`
- **Oracle**: `META-INF/orm-oracle.xml`

## Endpoint API

Una volta avviato, sono disponibili i seguenti endpoint:

### Health Check
```bash
curl http://localhost:10002/actuator/health
```

### Metriche Prometheus
```bash
curl http://localhost:10002/actuator/prometheus
```

### Informazioni
```bash
curl http://localhost:10002/actuator/info
```

## Ottimizzazione delle Prestazioni

### Connection Pool

Per scenari ad alto traffico:

```env
GOVPAY_GDE_MIN_POOL=5
GOVPAY_GDE_MAX_POOL=20
```

### Opzioni JVM

Per deployment in produzione:

```env
JAVA_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=100
GOVPAY_GDE_JVM_MAX_RAM_PERCENTAGE=90
```

## War per il deploy su application server

L'immagine di sviluppo `linkitaly/govpay-gde-api-dev:<versione>` contiene, oltre
al jar avviato dall'entrypoint, anche il war prodotto dalla stessa build, in
`/opt/govpay-gde/dist/govpay-gde-api.war`. Serve a provare il deploy su
application server partendo da una SNAPSHOT, quando non esiste ancora una
release GitHub a cui il war sia allegato.

Per estrarlo:

```bash
# Creare un container senza avviarlo e copiare il war
id=$(docker create linkitaly/govpay-gde-api-dev:2.0.0-SNAPSHOT)
docker cp "${id}:/opt/govpay-gde/dist/govpay-gde-api.war" ./govpay-gde-api.war
docker rm "${id}"
```

In alternativa, il war di ogni build e' scaricabile dall'artifact
`govpay-gde-api` della run di GitHub Actions (retention 90 giorni), mentre sui
tag e' allegato alla release come `govpay-gde-api-<tag>.war`.

## Scansione delle vulnerabilita' dell'immagine

Ogni immagine costruita dalla pipeline, di release (su tag) e di sviluppo (su `main`), viene scansionata **prima** di essere pubblicata, con [Trivy](https://trivy.dev) e [Grype](https://github.com/anchore/grype): sistema operativo di base, JRE e librerie dell'applicazione. Un'immagine che non supera il gate non viene pubblicata; un'immagine di release gia' pubblicata non viene piu' toccata.

| | |
|---|---|
| **Gate** (Trivy) | vulnerabilita' CRITICAL e HIGH con una versione corretta disponibile, al netto delle eccezioni |
| **Report** (Grype) | solo informativo, come secondo parere: sui pacchetti Alpine usa la severita' NVD, spesso piu' alta di quella della distribuzione |
| **Senza correzione** | non bloccano; restano nei report a ogni build |

La logica sta in `docker/scan_image.sh`, che si esegue uguale in locale su un'immagine gia' costruita (serve solo docker):

```bash
./docker/scan_image.sh linkitaly/govpay-gde-api-dev:2.0.1-SNAPSHOT
```

I report finiscono in `./image-scan-report` (`SCAN_OUT_DIR` per cambiarla): `riepilogo.md`, `trivy-gate.md` con l'elenco di cio' che blocca, i report completi di Trivy (JSON, SARIF, HTML) e di Grype (JSON, SARIF, testo), e lo SBOM CycloneDX dell'immagine. In pipeline lo stesso riepilogo compare nella pagina della run, i SARIF nella scheda Security del repository, i report come artifact e, su tag, allegati alla release come `image-scan-report-<tag>.zip`; lo SBOM dell'immagine va su Dependency-Track (progetto `govpay-gde-api-image`).

### Se il gate blocca

| Tipo di segnalazione | Cosa fare |
|---|---|
| Sistema operativo o JRE, con correzione | Rieseguire il build: la base (`pull`) e i pacchetti (`apk upgrade`) vengono aggiornati. Se la correzione non e' ancora nella base, aggiornare l'immagine base nel Dockerfile. |
| Libreria Java, con correzione | Aggiornare la dipendenza in `govpay-bom` o nel `pom.xml`; se la versione e' imposta da un'altra dipendenza, override in `dependencyManagement` con le CVE in commento. Poi nuovo build. |
| Pacchetto non necessario a runtime | Toglierlo dall'immagine. |
| Non sfruttabile nell'uso di GovPay GDE, o falso positivo | Eccezione documentata, dopo verifica (vedi sotto). |

**Eccezioni.** Si dichiarano in `.trivyignore.yaml`, nella radice del repository, con motivo, autore, issue di riferimento e **scadenza** (`expired_at`): alla scadenza l'eccezione decade e il gate torna a segnalare. Le stesse eccezioni vanno riportate in `.grype.yaml`, perche' i due report restino confrontabili. Le eccezioni servono a rendere leggibile il gate, non a nascondere i problemi.

**Deroga temporanea.** Se un rilascio urgente e' bloccato da una vulnerabilita' appena pubblicata e non correggibile in tempi brevi, la variabile di repository `IMAGE_SCAN_GATE=warn` porta il gate in sola segnalazione: viene valutato e riportato, ma l'immagine viene pubblicata. La deroga va motivata in una issue e la variabile rimossa subito dopo il rilascio.

### Pacchetti dell'immagine

Oltre alla base `eclipse-temurin:25-jre-alpine`, l'immagine installa solo `bash` (per l'entrypoint) e `tzdata`. `curl`, `tar`, `unzip` e `netcat-openbsd` non ci sono: l'entrypoint non li usa, e ogni pacchetto in piu' e' superficie esposta. Per una verifica dall'interno del container c'e' il `wget` di busybox, lo stesso usato dagli healthcheck:

```bash
docker exec <container> wget -q -O - http://localhost:10002/actuator/health
```

Le segnalazioni di Grype su `coreutils` e `busybox`, pacchetti della base senza correzione in Alpine, sono eccezioni motivate in `.grype.yaml`.

### Checksum del jar

`Dockerfile.github` scarica il jar dalla release GitHub con verifica del certificato e del checksum, passato con il build-arg `govpay_gde_sha256`: in pipeline e' calcolato dall'artifact della build, `build_image.sh` lo legge dal digest dell'asset nelle API delle release (opzione `-s` per indicarlo a mano).

## Struttura File

```
govpay-gde-api/
├── docker/
│   ├── build_image.sh              # Script di build
│   ├── commons/
│   │   └── entrypoint.sh           # Script di avvio container
│   ├── govpay-gde/
│   │   ├── Dockerfile.daFile       # Build da file locale
│   │   └── Dockerfile.github       # Build da release GitHub
│   ├── jdbc-drivers/
│   │   └── README.md               # Documentazione driver JDBC
│   ├── scan_image.sh               # Scansione dell'immagine (Trivy, Grype)
│   └── DOCKER.md                   # Questo file
├── src/                            # Codice sorgente Java
├── pom.xml                         # Configurazione Maven
└── README.md                       # Documentazione sviluppo Java
```

## Supporto

Per problemi relativi a:
- **GovPay**: https://github.com/link-it/govpay
- **Sviluppo Java**: Vedere `README.md`
- **Configurazione Docker**: Contattare il team DevOps

