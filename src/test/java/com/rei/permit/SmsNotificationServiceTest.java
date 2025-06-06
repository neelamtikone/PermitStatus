package com.rei.permit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SmsNotificationServiceTest {
    
    @Test
    public void testSmsConfiguration() {
        // Initialize the service
        SmsNotificationService.initialize();
        
        // Send a test message
        boolean success = SmsNotificationService.sendTestMessage();
        
        // Assert that the message was sent successfully
        assertTrue(success, "SMS test message should be sent successfully");
    }
} 