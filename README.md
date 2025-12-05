<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay - Porta di accesso al sistema pagoPA - GDE Api

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=link-it_govpay-gde-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=link-it_govpay-gde-api)
[![Docker Hub](https://img.shields.io/docker/v/linkitaly/govpay-gde-api?label=Docker%20Hub&logo=docker)](https://hub.docker.com/r/linkitaly/govpay-gde-api)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-gde-api/main/LICENSE)

## Sommario

API di accesso al Giornale degli Eventi di GovPay

## Istruzioni di compilazione

Il progetto utilizza librerie Spring Boot versione 3.5.7 e JDK 21.

Per la compilazione eseguire il seguente comando, verranno eseguiti anche i test.

``` bash
mvn clean install -P [jar|war]
```

Il profilo permette di selezionare il packaging dei progetti (jar o war).

Per l'avvio dell'applicativo come standalone eseguire:

``` bash
mvn spring-boot:run
```

Per sovrascrivere le proprietà definite nel file `application.properties` utilizzare il seguente sistema:

``` bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=[NUOVO_VALORE] ..."
```

## Configurazione

All'interno del file `application.properties` sono definite le seguenti proprietà:

``` bash
# ----------- SPRING SERVLET ------------

server.port=[Porta su cui esporre il servizio in caso di avvio come applicazione standalone]

spring.mvc.servlet.path=[Basepath servizi]

# Abilitazione Endpoint /actuator/health/liveness
management.endpoints.web.base-path=[Basepath dove esporre i servizi di stato applicazione]

# -------------- BUSINESS LOGIC PROPERTIES  ----------------

gde.time-zone=[TimeZone dell'applicazione]
```

## Configurazione connessione al database

Per la configurazione della connessione al database utilizzare le seguenti proprietà:

``` bash
# Configurazione DB
spring.datasource.jndiName=[JNDI NAME del datasource]
spring.datasource.url=[URL CONNESSIONE DB]
spring.datasource.driverClassName=[CLASSE DRIVER JDBC]
spring.datasource.username=[USERNAME DB]
spring.datasource.password=[PASSWORD DB]

spring.jpa.database-platform=[DIALECT JPA]
spring.jpa.properties.hibernate.dialect=[DIALECT JPA]
spring.jpa.mapping-resources=META-INF/orm-[h2|hsql|mysql|oracle|postgres|sqlserver].xml

spring.jpa.hibernate.ddl-auto=[Configura il comportamento di Hibernate nella generazione dello schema del database.]

spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.idle-timeout=10000
spring.datasource.hikari.max-lifetime=1000
```

## Configurazione logging

La configurazione del logging è gestita tramite le proprietà definite in `application.properties`:

``` bash
logging.file.name=[Path completo del file di log]
logging.level.it.govpay=[Livello di log: DEBUG, INFO, WARN, ERROR]
```

## Docker

L'immagine Docker è disponibile su Docker Hub: [linkitaly/govpay-gde-api](https://hub.docker.com/r/linkitaly/govpay-gde-api)

``` bash
docker pull linkitaly/govpay-gde-api:latest
```

Per la documentazione completa sull'utilizzo dell'immagine Docker consultare il file [docker/DOCKER.md](docker/DOCKER.md).
