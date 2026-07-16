package it.govpay.gde.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import it.govpay.gde.Application;
import it.govpay.gde.test.costanti.Costanti;

/**
 * Verifica end-to-end dell'esposizione delle metriche Prometheus:
 * <ul>
 *   <li>lo scrape {@code GET /actuator/prometheus} risponde sulla porta
 *       management in formato testuale Prometheus, con il tag comune
 *       {@code application};</li>
 *   <li>le richieste HTTP servite dall'API producono
 *       {@code http_server_requests} con bucket di istogramma;</li>
 *   <li>sulla porta applicativa l'actuator non e' mappato: le
 *       metriche non sono raggiungibili dai client dell'API;</li>
 *   <li>health risponde sulla porta management con il solo status,
 *       senza dettagli sui componenti.</li>
 * </ul>
 */
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "management.server.port=0"
        })
@ActiveProfiles("test")
@DisplayName("Test Esposizione Metriche Prometheus")
class PrometheusScrapeIntegrationTest {

    @LocalServerPort
    private int serverPort;

    @LocalManagementPort
    private int managementPort;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void scrapeOnManagementPortReturns200PrometheusFormat() throws Exception {
        HttpResponse<String> response = get(managementPort, "/actuator/prometheus");

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/plain"));
        assertTrue(response.body().contains("# TYPE jvm_memory_used_bytes gauge"));
        assertTrue(response.body().contains("application=\"govpay-gde-api\""));
    }

    @Test
    void apiCallProducesHttpServerRequestsWithHistogramBuckets() throws Exception {
        HttpResponse<String> apiResponse = get(serverPort, Costanti.EVENTI_PATH);
        assertEquals(200, apiResponse.statusCode());

        String scrape = get(managementPort, "/actuator/prometheus").body();
        assertTrue(scrape.contains("http_server_requests_seconds_bucket"));
        assertTrue(scrape.contains("uri=\"" + Costanti.EVENTI_PATH + "\""));
    }

    @Test
    void actuatorNotServedOnApplicationPort() throws Exception {
        // L'invariante e' che scrape e health non siano raggiungibili dai
        // client dell'API: lo status esatto (404/500) dipende dalla gestione
        // degli errori dell'applicazione per i path non mappati.
        HttpResponse<String> prometheus = get(serverPort, "/actuator/prometheus");
        assertTrue(prometheus.statusCode() >= 400);
        assertFalse(prometheus.body().contains("jvm_memory_used_bytes"));

        HttpResponse<String> health = get(serverPort, "/actuator/health");
        assertTrue(health.statusCode() >= 400);
    }

    @Test
    void healthOnManagementPortReturns200StatusOnly() throws Exception {
        HttpResponse<String> response = get(managementPort, "/actuator/health");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\""));
        // niente show-details: nessun dettaglio sui componenti
        assertFalse(response.body().contains("\"components\""));
    }

    private HttpResponse<String> get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
