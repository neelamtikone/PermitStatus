package com.rei.permit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

class RecreationGovClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getPermitAvailability_Success() throws Exception {
        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 2\n" +
            "        }\n" +
            "    ]\n" +
            "}";

        CloseableHttpResponse ok = new SimpleCloseableHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"),
            new StringEntity(jsonResponse, StandardCharsets.UTF_8)
        );

        TestHttpClient httpClient = new TestHttpClient(ok);
        RecreationGovClient client = new RecreationGovClient(httpClient, 3, Duration.ofSeconds(1));

        JsonNode result = client.getPermitAvailability("233260");
        assertNotNull(result);
        assertTrue(result.has("availability"));
        assertEquals(1, result.get("availability").size());
        assertEquals("2024-08-01", result.get("availability").get(0).get("date").asText());
        assertEquals(2, result.get("availability").get(0).get("remaining").asInt());
    }

    @Test
    void getPermitAvailability_Error() {
        CloseableHttpResponse error = new SimpleCloseableHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Internal Server Error"),
            null
        );

        TestHttpClient httpClient = new TestHttpClient(error);
        RecreationGovClient client = new RecreationGovClient(httpClient, 1, Duration.ofSeconds(1));

        assertThrows(IOException.class, () -> client.getPermitAvailability("233260"));
    }

    @Test
    void getPermitAvailability_Retry() throws Exception {
        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 2\n" +
            "        }\n" +
            "    ]\n" +
            "}";

        CloseableHttpResponse err1 = new SimpleCloseableHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Internal Server Error"),
            null
        );
        CloseableHttpResponse err2 = new SimpleCloseableHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Internal Server Error"),
            null
        );
        CloseableHttpResponse ok = new SimpleCloseableHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"),
            new StringEntity(jsonResponse, StandardCharsets.UTF_8)
        );

        TestHttpClient httpClient = new TestHttpClient(err1, err2, ok);
        RecreationGovClient client = new RecreationGovClient(httpClient, 3, Duration.ofSeconds(1));

        JsonNode result = client.getPermitAvailability("233260");
        assertNotNull(result);
        assertTrue(result.has("availability"));
        assertEquals(1, result.get("availability").size());
        assertEquals("2024-08-01", result.get("availability").get(0).get("date").asText());
    }

    private static <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    // Simple HTTP client that returns pre-seeded responses in order
    static class TestHttpClient extends CloseableHttpClient {
        private final Deque<CloseableHttpResponse> responses = new ArrayDeque<>();

        TestHttpClient(CloseableHttpResponse... responses) {
            for (CloseableHttpResponse r : responses) {
                this.responses.add(r);
            }
        }

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) {
            CloseableHttpResponse response = responses.pollFirst();
            if (response == null) {
                return new SimpleCloseableHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "No more responses"), null);
            }
            return response;
        }

        @Override
        public void close() {}

        @Override
        public HttpParams getParams() { return null; }

        @Override
        public ClientConnectionManager getConnectionManager() { return null; }
    }

    // Minimal CloseableHttpResponse backed by BasicHttpResponse
    static class SimpleCloseableHttpResponse extends BasicHttpResponse implements CloseableHttpResponse {
        SimpleCloseableHttpResponse(StatusLine statusline, HttpEntity entity) {
            super(statusline);
            setEntity(entity);
        }
        @Override
        public void close() {}
    }
} 