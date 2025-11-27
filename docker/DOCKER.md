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

| Variabile | Richiesta | Predefinito | Descrizione |
|----------|----------|---------|-------------|
| `GOVPAY_DB_TYPE` | Sì | - | Tipo di database (postgresql, mysql, mariadb, oracle) |
| `GOVPAY_DB_SERVER` | Sì | - | Server database (formato: host:porta) |
| `GOVPAY_DB_NAME` | Sì | - | Nome del database |
| `GOVPAY_DB_USER` | Sì | - | Username del database |
| `GOVPAY_DB_PASSWORD` | Sì | - | Password del database |
| `GOVPAY_DS_CONN_PARAM` | No | - | Parametri aggiuntivi connessione JDBC |
| `GOVPAY_DS_JDBC_LIBS` | No | `/opt/jdbc-drivers` | Percorso driver JDBC |
| `SERVER_PORT` | No | `10002` | Porta di ascolto API |
| `GOVPAY_GDE_MIN_POOL` | No | `2` | Connessioni idle minime |
| `GOVPAY_GDE_MAX_POOL` | No | `5` | Dimensione massima pool |
| `GOVPAY_GDE_JVM_MAX_RAM_PERCENTAGE` | No | `80` | Percentuale massima RAM per JVM |
| `GOVPAY_GDE_JVM_INITIAL_RAM_PERCENTAGE` | No | - | Percentuale iniziale RAM per JVM |
| `GOVPAY_GDE_JVM_MIN_RAM_PERCENTAGE` | No | - | Percentuale minima RAM per JVM |
| `GOVPAY_GDE_JVM_MAX_METASPACE_SIZE` | No | - | Dimensione massima Metaspace |
| `GOVPAY_GDE_JVM_MAX_DIRECT_MEMORY_SIZE` | No | - | Dimensione massima memoria diretta |
| `GOVPAY_ORACLE_JDBC_URL_TYPE` | No | `servicename` | Tipo URL Oracle (servicename o sid) |
| `JAVA_OPTS` | No | - | Opzioni aggiuntive JVM |

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

### Metriche
```bash
curl http://localhost:10002/actuator/metrics
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

## Struttura File

```
govpay-gde-api/
├── docker/
│   ├── build_image.sh              # Script di build
│   ├── commons/
│   │   └── entrypoint.sh           # Script di avvio container
│   ├── govpay-gde/
│   │   └── Dockerfile.github       # Definizione immagine Docker
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

