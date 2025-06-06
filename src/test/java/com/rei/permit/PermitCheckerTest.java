package com.rei.permit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PermitCheckerTest {
    @Mock
    private RecreationGovClient apiClient;
    
    @Mock
    private SmsNotificationService smsService;
    
    private PermitChecker permitChecker;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        permitChecker = new PermitChecker();
        objectMapper = new ObjectMapper();
    }

    @Test
    void checkPermitAvailability_Available() throws IOException {
        // Prepare test data
        String permitId = "233260";
        String permitName = "Enchantments";
        String permitUrl = "https://www.recreation.gov/permits/233260";
        Set<LocalDate> targetDates = new HashSet<>(Arrays.asList(
            LocalDate.parse("2024-08-01"),
            LocalDate.parse("2024-08-02")
        ));
        
        Permit permit = new Permit(permitId, permitName, permitUrl, targetDates);
        
        // Mock API response
        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 2\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        when(apiClient.getPermitAvailability(permitId)).thenReturn(jsonNode);

        // Test the method
        permitChecker.checkPermitAvailability(permit);

        // Verify SMS notification was sent
        verify(smsService, times(1)).sendPermitAvailableNotification(eq(permitId), anyString());
    }

    @Test
    void checkPermitAvailability_NotAvailable() throws IOException {
        // Prepare test data
        String permitId = "233260";
        String permitName = "Enchantments";
        String permitUrl = "https://www.recreation.gov/permits/233260";
        Set<LocalDate> targetDates = new HashSet<>(Arrays.asList(
            LocalDate.parse("2024-08-01"),
            LocalDate.parse("2024-08-02")
        ));
        
        Permit permit = new Permit(permitId, permitName, permitUrl, targetDates);
        
        // Mock API response with no availability
        String jsonResponse = "{\n" +
            "    \"availability\": [\n" +
            "        {\n" +
            "            \"date\": \"2024-08-01\",\n" +
            "            \"remaining\": 0\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        when(apiClient.getPermitAvailability(permitId)).thenReturn(jsonNode);

        // Test the method
        permitChecker.checkPermitAvailability(permit);

        // Verify no SMS notification was sent
        verify(smsService, never()).sendPermitAvailableNotification(anyString(), anyString());
    }

    @Test
    void checkPermitAvailability_ApiError() throws IOException {
        // Prepare test data
        String permitId = "233260";
        String permitName = "Enchantments";
        String permitUrl = "https://www.recreation.gov/permits/233260";
        Set<LocalDate> targetDates = new HashSet<>(Arrays.asList(
            LocalDate.parse("2024-08-01"),
            LocalDate.parse("2024-08-02")
        ));
        
        Permit permit = new Permit(permitId, permitName, permitUrl, targetDates);
        
        // Mock API error
        when(apiClient.getPermitAvailability(permitId)).thenThrow(new IOException("API Error"));

        // Test the method
        permitChecker.checkPermitAvailability(permit);

        // Verify no SMS notification was sent
        verify(smsService, never()).sendPermitAvailableNotification(anyString(), anyString());
    }
} 