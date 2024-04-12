# govpay-gde-api
API di accesso al Giornale degli Eventi di GovPay

## Istruzioni di compilazione

Il progetto utilizza librerie spring-boot versione 2.7.147 e JDK 11.

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

# ------------ HIBERNATE & JPA -------------------

# Configurazione DB
spring.datasource.jndiName=[JNDI NAME del datasource]
spring.datasource.url=[URL CONNESSIONE DB]
spring.datasource.driverClassName=[CLASSE DRIVER JDBC]
spring.datasource.username=[USERNAME DB]
spring.datasource.password=[PASSWORD DB]

spring.jpa.database-platform=[DIALECT JPA]
spring.jpa.properties.hibernate.dialect=[DIALECT JPA]

spring.jpa.hibernate.ddl-auto=[Configura il comportamento di Hibernate nella generazione dello schema del database.]

# -------------- BUSINESS LOGIC PROPERTIES  ----------------

gde.time-zone=[TimeZone dell'applicazione]

```

All'interno del file `log4j2.xml` si definisce la configurazione di log dell'applicazione.
