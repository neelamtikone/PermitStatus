package com.rei.permit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * Client for interacting with the Recreation.gov API.
 * Handles HTTP requests with proper headers, timeouts, and retry logic.
 */
public class RecreationGovClient {
    private static final Logger logger = LoggerFactory.getLogger(RecreationGovClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "https://www.recreation.gov/api/permits/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    private final CloseableHttpClient httpClient;
    private final int maxRetries;
    private final Duration timeout;

    /**
     * Creates a new RecreationGovClient with default settings.
     */
    public RecreationGovClient() {
        this(3, Duration.ofSeconds(10));
    }

    /**
     * Creates a new RecreationGovClient with custom settings.
     *
     * @param maxRetries Maximum number of retry attempts for failed requests
     * @param timeout Request timeout duration
     */
    public RecreationGovClient(int maxRetries, Duration timeout) {
        this.maxRetries = maxRetries;
        this.timeout = timeout;
        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout((int) timeout.toMillis())
                .setSocketTimeout((int) timeout.toMillis())
                .build())
            .build();
    }

    /**
     * Creates a new RecreationGovClient with a provided HTTP client.
     * Intended for testing to allow injecting a mocked client.
     *
     * @param httpClient The HTTP client to use
     * @param maxRetries Maximum number of retry attempts for failed requests
     * @param timeout Request timeout duration
     */
    public RecreationGovClient(CloseableHttpClient httpClient, int maxRetries, Duration timeout) {
        this.maxRetries = maxRetries;
        this.timeout = timeout;
        this.httpClient = httpClient;
    }

    /**
     * Retrieves permit availability information for a specific permit ID.
     *
     * @param permitId The ID of the permit to check
     * @return JsonNode containing the permit availability information
     * @throws IOException if the request fails after all retry attempts
     */
    public JsonNode getPermitAvailability(String permitId) throws IOException {
        String url = BASE_URL + permitId + "/availability";
        logger.debug("Fetching permit availability from: {}", url);

        try {
            return RetryUtil.retry(() -> {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", USER_AGENT);
                request.setHeader("Accept", "application/json");

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        throw new IOException("Failed to get permit availability. Status code: " + statusCode);
                    }

                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        throw new IOException("Empty response from server");
                    }

                    String jsonResponse = EntityUtils.toString(entity);
                    return objectMapper.readTree(jsonResponse);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, maxRetries, 1000, 10000);
        } catch (Exception e) {
            IOException io = findCause(e, IOException.class);
            if (io != null) {
                throw io;
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
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

    /**
     * Closes the HTTP client and releases any resources.
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
} 