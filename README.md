[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=link-it_govpay-gde-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=link-it_govpay-gde-api)

# govpay-gde-api
API di accesso al Giornale degli Eventi di GovPay

## Istruzioni di compilazione

Il progetto utilizza librerie spring-boot versione 3.4.1 e JDK 21.

Per la compilazione eseguire il seguente comando, verranno eseguiti anche i test.


``` bash
mvn clean install -P [jar|war]
```

Il profilo permette di selezionare il packaging dei progetti (jar o war).

Per l'avvio dell'applicativo come standalone eseguire:

``` bash
mvn spring-boot:run
```

Per sovrascrivere le proprieta' definite nel file `application.properties` utilizzare il seguente sistema:

``` bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=[NUOVO_VALORE] ..."

```

# Configurazione

All'interno del file `application.properties` sono definite le seguenti proprieta':


``` bash
# ----------- SPRING SERVLET ------------

server.port=[Porta su cui esporre il servizio in caso di avvio come applicazione standalone]

spring.mvc.servlet.path=[Basepath servizi]

# Abilitazione Endpoint /actuator/health/liveness
management.endpoints.web.base-path=[Basepath dove esporre i servizi di stato applicazione]

# -------------- BUSINESS LOGIC PROPERTIES  ----------------

gde.time-zone=[TimeZone dell'applicazione]

```

## Configurazione connessione al db

Per la configurazione della connessione al db utilizzare le seguenti proprieta':

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


All'interno del file `log4j2.xml` si definisce la configurazione di log dell'applicazione.
