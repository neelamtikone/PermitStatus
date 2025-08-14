package com.rei.permit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PermitCheckerTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        System.setProperty("sms.test.mode", "true");
        SmsNotificationService.initialize();
    }

    @Test
    void checkPermitAvailability_Available() throws Exception {
        String permitId = "233260";
        String permitName = "Enchantments";
        String permitUrl = "https://www.recreation.gov/permits/233260";
        Set<LocalDate> targetDates = new HashSet<>(Arrays.asList(
            LocalDate.parse("2024-08-01"),
            LocalDate.parse("2024-08-02")
        ));
        Permit permit = new Permit(permitId, permitName, permitUrl, targetDates);

        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 2\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        // Stub client
        RecreationGovClient stubClient = new RecreationGovClient(1, java.time.Duration.ofSeconds(1)) {
            @Override
            public JsonNode getPermitAvailability(String id) {
                return jsonNode;
            }
        };
        PermitChecker.setApiClientForTesting(stubClient);

        // Execute
        SmsNotificationServiceTestHelper.clearLastMessage();
        PermitChecker.checkPermitAvailability(permit);

        // Verify SMS was "sent" in test mode
        assertNotNull(SmsNotificationService.getLastMessageBodyForTesting());
        assertTrue(SmsNotificationService.getLastMessageBodyForTesting().contains("2024-08-01"));
    }

    @Test
    void checkPermitAvailability_NotAvailable() throws Exception {
        String permitId = "233260";
        String permitName = "Enchantments";
        String permitUrl = "https://www.recreation.gov/permits/233260";
        Set<LocalDate> targetDates = new HashSet<>(Arrays.asList(
            LocalDate.parse("2024-08-01"),
            LocalDate.parse("2024-08-02")
        ));
        Permit permit = new Permit(permitId, permitName, permitUrl, targetDates);

        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 0\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        RecreationGovClient stubClient = new RecreationGovClient(1, java.time.Duration.ofSeconds(1)) {
            @Override
            public JsonNode getPermitAvailability(String id) {
                return jsonNode;
            }
        };
        PermitChecker.setApiClientForTesting(stubClient);

        SmsNotificationServiceTestHelper.clearLastMessage();
        PermitChecker.checkPermitAvailability(permit);

        assertNull(SmsNotificationService.getLastMessageBodyForTesting());
    }

    @Test
    void checkPermitAvailability_ApiError() {
        String permitId = "233260";
        String permitName = "Enchantments";
        String permitUrl = "https://www.recreation.gov/permits/233260";
        Set<LocalDate> targetDates = new HashSet<>(Arrays.asList(
            LocalDate.parse("2024-08-01"),
            LocalDate.parse("2024-08-02")
        ));
        Permit permit = new Permit(permitId, permitName, permitUrl, targetDates);

        RecreationGovClient stubClient = new RecreationGovClient(1, java.time.Duration.ofSeconds(1)) {
            @Override
            public JsonNode getPermitAvailability(String id) throws IOException {
                throw new IOException("API Error");
            }
        };
        PermitChecker.setApiClientForTesting(stubClient);

        SmsNotificationServiceTestHelper.clearLastMessage();
        assertThrows(IOException.class, () -> PermitChecker.checkPermitAvailability(permit));
        assertNull(SmsNotificationService.getLastMessageBodyForTesting());
    }
}

// Helper to reset captured message between tests
class SmsNotificationServiceTestHelper {
    static void clearLastMessage() {
        System.setProperty("sms.test.mode", "true");
        // Reset by sending an empty message
        try {
            java.lang.reflect.Field f = com.rei.permit.SmsNotificationService.class.getDeclaredField("lastMessageBodyForTesting");
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception ignored) {}
    }
} 