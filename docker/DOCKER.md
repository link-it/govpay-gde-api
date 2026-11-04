# GovPay GDE (Giornale degli Eventi) API - Configurazione Docker

Containerizzazione del servizio REST GovPay GDE, che registra gli eventi inviati dagli altri componenti di GovPay e ne consente la consultazione.

Immagine, variabili d'ambiente, proprietà configurabili ed endpoint actuator sono descritti nel [README](../README.md#esecuzione-con-docker). Questo file copre la costruzione dell'immagine in locale, la configurazione dei componenti che scrivono eventi, la scansione delle vulnerabilita' e il contenuto dell'immagine.

## Costruire l'immagine

`build_image.sh` costruisce l'immagine `linkitaly/govpay-gde-api:<versione>`. L'immagine e' unica per tutti i database: il tipo si sceglie all'avvio con `GOVPAY_DB_TYPE`.

```bash
cd docker

# Ultima release pubblicata su GitHub
./build_image.sh

# Release indicata
./build_image.sh -v 2.0.0

# Jar costruito in locale, con un tag a scelta
./build_image.sh -l ../target/govpay-gde-api.jar -t linkitaly/govpay-gde-api:locale
```

| Opzione | Descrizione |
|---|---|
| `-v <versione>` | Release GitHub da cui scaricare il jar (default: l'ultima) |
| `-l <file>` | Jar sul filesystem locale, al posto della release (usa `Dockerfile.daFile`) |
| `-s <sha256>` | Checksum del jar della release (default: letto dalle API delle release di GitHub) |
| `-t <repository>:<tag>` | Nome dell'immagine prodotta (default: `linkitaly/govpay-gde-api:<versione>`) |
| `-e <path>` | Directory dell'applicazione nell'immagine (default: `/opt/govpay-gde`) |
| `-f <path>` | Directory dei log nell'immagine (default: `/var/log/govpay-gde`) |

Per avviarla, vedere l'esempio `docker run` nel [README](../README.md#esecuzione-con-docker).

## Driver JDBC

I driver JDBC **non sono inclusi** nell'immagine e vanno montati come volume in `/opt/jdbc-drivers` (directory modificabile con `GOVPAY_DS_JDBC_LIBS`). L'entrypoint imposta `LOADER_PATH` sulla directory per il `PropertiesLauncher` di Spring Boot.

```bash
docker run -v ./jdbc-drivers:/opt/jdbc-drivers ... linkitaly/govpay-gde-api
```

I driver supportati sono elencati in [jdbc-drivers/README.md](jdbc-drivers/README.md).

## Componenti che scrivono eventi

I batch e le API di GovPay (elencati nel [README](../README.md)) inviano gli eventi con `POST /eventi`. Non hanno proprieta' o variabili d'ambiente per il GDE: indirizzo e autenticazione sono nel connettore `govpay_gde_api` di GovPay (tabella `connettori`). Se il connettore non e' abilitato, gli eventi non vengono inviati; un GDE non raggiungibile non blocca il componente che scrive.

Nella stessa rete Docker l'URL del connettore e' il nome del servizio con la porta del container, per esempio `http://govpay-gde-api:10002`.

## Impostazioni dell'immagine

| Impostazione | Valore |
|---|---|
| Porta HTTP | `10002` |
| Timezone | `Europe/Rome` |
| Timeout di connessione del pool | `20000` ms |
| Timeout di inattivita' del pool | `10000` ms |
| Durata massima di una connessione | `600000` ms |
| File di log | `/var/log/govpay-gde/govpay-gde-api.log` |
| Utente | `govpay` (non root) |

L'entrypoint sceglie il mapping ORM in base a `GOVPAY_DB_TYPE`: `META-INF/orm-postgres.xml`, `orm-mysql.xml` (anche per MariaDB) o `orm-oracle.xml`.

## War per il deploy su application server

L'immagine di sviluppo `linkitaly/govpay-gde-api-dev:<versione>` contiene, oltre
al jar avviato dall'entrypoint, anche il war prodotto dalla stessa build, in
`/opt/govpay-gde/dist/govpay-gde-api.war`. Serve a provare il deploy su
application server partendo da una SNAPSHOT, quando non esiste ancora una
release GitHub a cui il war sia allegato.

Per estrarlo:

```bash
# Creare un container senza avviarlo e copiare il war
id=$(docker create linkitaly/govpay-gde-api-dev:2.0.1-SNAPSHOT)
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
│   ├── DOCKERHUB.md                # Descrizione della pagina Docker Hub
│   └── DOCKER.md                   # Questo file
├── src/                            # Codice sorgente Java
├── pom.xml                         # Configurazione Maven
└── README.md                       # Documentazione del progetto
```

## Supporto

Segnalazioni e richieste: [issue del progetto](https://github.com/link-it/govpay-gde-api/issues).
