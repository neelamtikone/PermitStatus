package com.rei.permit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecreationGovClientTest {
    @Mock
    private CloseableHttpClient httpClient;
    
    @Mock
    private CloseableHttpResponse response;
    
    private RecreationGovClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new RecreationGovClient(3, java.time.Duration.ofSeconds(10));
        objectMapper = new ObjectMapper();
    }

    @Test
    void getPermitAvailability_Success() throws IOException {
        // Prepare mock response
        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 2\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8)));
        when(response.getEntity()).thenReturn(entity);
        when(response.getStatusLine().getStatusCode()).thenReturn(200);
        when(httpClient.execute(any())).thenReturn(response);

        // Test the method
        JsonNode result = client.getPermitAvailability("233260");

        // Verify the result
        assertNotNull(result);
        assertTrue(result.has("availability"));
        assertEquals(1, result.get("availability").size());
        assertEquals("2024-08-01", result.get("availability").get(0).get("date").asText());
        assertEquals(2, result.get("availability").get(0).get("remaining").asInt());
    }

    @Test
    void getPermitAvailability_Error() throws IOException {
        // Mock HTTP error
        when(response.getStatusLine().getStatusCode()).thenReturn(500);
        when(httpClient.execute(any())).thenReturn(response);

        // Test the method
        assertThrows(IOException.class, () -> client.getPermitAvailability("233260"));
    }

    @Test
    void getPermitAvailability_Retry() throws IOException {
        // Mock first two failures, then success
        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 2\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8)));
        when(response.getEntity()).thenReturn(entity);
        when(response.getStatusLine())
            .thenReturn(new org.apache.http.message.BasicStatusLine(
                org.apache.http.HttpVersion.HTTP_1_1, 500, "Internal Server Error"))
            .thenReturn(new org.apache.http.message.BasicStatusLine(
                org.apache.http.HttpVersion.HTTP_1_1, 500, "Internal Server Error"))
            .thenReturn(new org.apache.http.message.BasicStatusLine(
                org.apache.http.HttpVersion.HTTP_1_1, 200, "OK"));
        
        when(httpClient.execute(any())).thenReturn(response);

        // Test the method
        JsonNode result = client.getPermitAvailability("233260");

        // Verify the result
        assertNotNull(result);
        assertTrue(result.has("availability"));
        assertEquals(1, result.get("availability").size());
        
        // Verify that execute was called 3 times
        verify(httpClient, times(3)).execute(any());
    }
} 