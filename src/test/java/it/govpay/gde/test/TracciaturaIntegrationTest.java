package it.govpay.gde.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import it.govpay.gde.Application;
import it.govpay.gde.test.costanti.Costanti;

/**
 * Verifica end-to-end della tracciatura di govpay-common (BP-LOG-3) su gde-api:
 * ogni risposta porta un transaction id generato dal componente e un correlation
 * id che riusa quello del chiamante quando presente.
 */
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Test Tracciatura Transaction ID / Correlation ID")
class TracciaturaIntegrationTest {

    private static final String HEADER_TRANSACTION_ID = "X-Transaction-ID";
    private static final String HEADER_TRANSACTION_ID_LEGACY = "X-Govpay-IdTransazione";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    private static final String HEADER_REQUEST_ID = "X-Request-ID";

    @LocalServerPort
    private int serverPort;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    @DisplayName("La risposta espone transaction id e correlation id generati")
    void rispostaEsponeIdentificativiGenerati() throws Exception {
        HttpResponse<String> response = get(Costanti.EVENTI_PATH, null, null);

        String transactionId = header(response, HEADER_TRANSACTION_ID);
        String correlationId = header(response, HEADER_CORRELATION_ID);

        assertNotNull(UUID.fromString(transactionId));
        assertNotNull(UUID.fromString(correlationId));
        assertNotEquals(transactionId, correlationId);

        // header legacy di GovPay 3, per i client gia' esistenti
        assertEquals(transactionId, header(response, HEADER_TRANSACTION_ID_LEGACY));
        assertEquals(correlationId, header(response, HEADER_REQUEST_ID));
    }

    @Test
    @DisplayName("Il correlation id del chiamante viene riusato")
    void correlationIdDelChiamanteRiusato() throws Exception {
        HttpResponse<String> response = get(Costanti.EVENTI_PATH, HEADER_CORRELATION_ID, "flusso-esterno-1");

        assertEquals("flusso-esterno-1", header(response, HEADER_CORRELATION_ID));
        assertEquals("flusso-esterno-1", header(response, HEADER_REQUEST_ID));
    }

    @Test
    @DisplayName("Anche X-Request-ID e' accettato come correlation id")
    void requestIdAccettatoComeCorrelationId() throws Exception {
        HttpResponse<String> response = get(Costanti.EVENTI_PATH, HEADER_REQUEST_ID, "da-gateway");

        assertEquals("da-gateway", header(response, HEADER_CORRELATION_ID));
    }

    @Test
    @DisplayName("Il transaction id non e' imponibile dal client")
    void transactionIdNonImponibileDalClient() throws Exception {
        HttpResponse<String> response = get(Costanti.EVENTI_PATH, HEADER_TRANSACTION_ID, "imposto-dal-client");

        String transactionId = header(response, HEADER_TRANSACTION_ID);
        assertNotEquals("imposto-dal-client", transactionId);
        assertNotNull(UUID.fromString(transactionId));
    }

    @Test
    @DisplayName("Un correlation id con caratteri non ammessi viene scartato")
    void correlationIdNonConformeScartato() throws Exception {
        HttpResponse<String> response = get(Costanti.EVENTI_PATH, HEADER_CORRELATION_ID, "valore con spazi");

        String correlationId = header(response, HEADER_CORRELATION_ID);
        assertNotEquals("valore con spazi", correlationId);
        assertNotNull(UUID.fromString(correlationId));
    }

    @Test
    @DisplayName("Ogni richiesta ha un transaction id diverso")
    void transactionIdDiversoPerOgniRichiesta() throws Exception {
        String primo = header(get(Costanti.EVENTI_PATH, null, null), HEADER_TRANSACTION_ID);
        String secondo = header(get(Costanti.EVENTI_PATH, null, null), HEADER_TRANSACTION_ID);

        assertNotEquals(primo, secondo);
    }

    private static String header(HttpResponse<String> response, String nome) {
        String valore = response.headers().firstValue(nome).orElse(null);
        assertTrue(valore != null && !valore.isBlank(),
                "Header " + nome + " assente o vuoto nella risposta");
        return valore;
    }

    private HttpResponse<String> get(String path, String headerNome, String headerValore) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + path))
                .GET();
        if (headerNome != null) {
            builder.header(headerNome, headerValore);
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
