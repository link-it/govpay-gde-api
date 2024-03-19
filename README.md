# govpay-gde-api
API di accesso al Giornale degli Eventi di GovPay

## Istruzioni di compilazione

Il progetto utilizza librerie spring-boot versione 2.7.147 e JDK 11.

Per la compilazione eseguire il seguente comando, verranno eseguiti anche i test.


``` bash
mvn clean install -P [jar|war] -Denv=[localhost]
```

Il profilo permette di selezionare il packaging dei progetti (jar o war).

Il parametro env invece consente di valorizzare le properties per l'ambiente di installazione scelto (default: localhost).

Per l'avvio dell'applicativo come standalone eseguire:

``` bash
mvn spring-boot:run -Denv=[localhost]
```

# Configurazione

All'interno del file di filtro si possono definire le seguenti proprieta':


``` bash
# ------------ DIRECTORY LAVORO ESTERNA -----------

it.govpay.gde.resource.path=[WORK_DIR]

# ------------ LOGGING -------------------

it.govpay.gde.log.path=[LOG_DIR]
it.govpay.gde.log.level=[LOG_LEVEL]

# ----------- SPRING SERVLET ------------

it.govpay.gde.server.port=[Porta su cui esporre il servizio in caso di avvio come applicazione standalone]

it.govpay.gde.spring.mvc.servlet.path=[Basepath servizi]

# Abilitazione Endpoint /actuator/health/liveness
it.govpay.gde.spring.actuator.path=[Basepath dove esporre i servizi di stato applicazione]

# ------------ HIBERNATE & JPA -------------------

# Configurazione DB
#it.govpay.gde.spring.datasource.jndiName=[JNDI NAME del datasource]
it.govpay.gde.spring.datasource.url=[URL CONNESSIONE DB]
it.govpay.gde.spring.datasource.driverClassName=[CLASSE DRIVER JDBC]
it.govpay.gde.spring.datasource.username=[USERNAME DB]
it.govpay.gde.spring.datasource.password=[PASSWORD DB]

it.govpay.gde.spring.jpa.database-platform=[DIALECT JPA]

it.govpay.gde.spring.jpa.hibernate.ddl-auto=[Configura il comportamento di Hibernate nella generazione dello schema del database.]

# -------------- BUSINESS LOGIC PROPERTIES  ----------------

```
