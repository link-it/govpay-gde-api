<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay GDE API

[![GitHub](https://img.shields.io/badge/GitHub-link--it%2Fgovpay--gde--api-blue?logo=github)](https://github.com/link-it/govpay-gde-api)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

API REST Spring Boot per la gestione del **Giornale degli Eventi (G.D.E.)** di GovPay.

## Cos'è GovPay GDE API

GovPay GDE API è un componente del progetto [GovPay](https://github.com/link-it/govpay) che fornisce API REST per la registrazione, ricerca e consultazione degli eventi del sistema di pagamento.

### Funzionalità principali

- Registrazione eventi tramite API REST
- Ricerca eventi con filtri avanzati (data, dominio, IUV, esito, ecc.)
- Consultazione dettaglio singolo evento
- Supporto multi-database: PostgreSQL, MySQL/MariaDB, Oracle, SQL Server
- Health check e monitoraggio tramite Spring Boot Actuator
- Documentazione OpenAPI/Swagger integrata

## Versioni disponibili

- `latest` - ultima versione stabile
- `1.0.4`

Storico completo delle modifiche consultabile nel [ChangeLog](https://github.com/link-it/govpay-gde-api/blob/main/ChangeLog) del progetto.

## Quick Start

```bash
docker pull linkitaly/govpay-gde-api:latest
```

## Documentazione

- [README e istruzioni di configurazione](https://github.com/link-it/govpay-gde-api/blob/main/README.md)
- [Documentazione Docker](https://github.com/link-it/govpay-gde-api/blob/main/docker/DOCKER.md)
- [Dockerfile](https://github.com/link-it/govpay-gde-api/blob/main/docker/govpay-gde/Dockerfile.github)

## Licenza

GovPay GDE API è rilasciato con licenza [GPL v3](https://www.gnu.org/licenses/gpl-3.0).

## Supporto

- **Issues**: [GitHub Issues](https://github.com/link-it/govpay-gde-api/issues)
- **GovPay**: [govpay.readthedocs.io](https://govpay.readthedocs.io/)

---

Sviluppato da [Link.it s.r.l.](https://www.link.it)
