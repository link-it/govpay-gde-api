package it.govpay.gde.config;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.module.SimpleModule;

import it.govpay.gde.costanti.Costanti;
import it.govpay.gde.utils.OffsetDateTimeDeserializer;
import it.govpay.gde.utils.OffsetDateTimeSerializer;


@Configuration
public class WebConfig {

    @Value("${gde.time-zone:Europe/Rome}")
    private String timeZone;

    /**
     * Personalizza il JsonMapper primario di Spring Boot (Jackson 3).
     * Usare il customizer del builder, invece di esporre un bean ObjectMapper,
     * garantisce che la configurazione sia applicata al mapper effettivamente
     * usato dai message converter HTTP (incluse le risposte application/*+json).
     */
    @Bean
    public JsonMapperBuilderCustomizer gdeJsonMapperBuilderCustomizer() {
        SimpleModule offsetDateTimeModule = new SimpleModule();
        offsetDateTimeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        offsetDateTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());

        return builder -> builder
                .defaultDateFormat(new SimpleDateFormat(Costanti.PATTERN_TIMESTAMP_3_YYYY_MM_DD_T_HH_MM_SS_SSSXXX))
                .defaultTimeZone(TimeZone.getTimeZone(this.timeZone))
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                // Jackson 3 di default omette i null: ripristina l'inclusione dei null nelle risposte
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.ALWAYS))
                .addModule(offsetDateTimeModule);
    }
}
