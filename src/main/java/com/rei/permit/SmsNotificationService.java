package com.rei.permit;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String API_KEY_SID = System.getenv("TWILIO_API_KEY_SID");
    private static final String API_KEY_SECRET = System.getenv("TWILIO_API_KEY_SECRET");
    private static final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");
    private static final String NOTIFICATION_PHONE_NUMBER = System.getenv("NOTIFICATION_PHONE_NUMBER");

    // Test mode controls (enabled when -Dsms.test.mode=true)
    private static boolean isTestMode() {
        return Boolean.parseBoolean(System.getProperty("sms.test.mode", "false"));
    }
    private static volatile String lastMessageBodyForTesting;

    /**
     * Initializes the SMS notification service and validates the configuration.
     * @throws IllegalStateException if the configuration is invalid
     */
    public static void initialize() {
        if (isTestMode()) {
            logger.info("SMS notification service initialized in TEST MODE (Twilio disabled)");
            return;
        }
        validateConfiguration();
        Twilio.init(API_KEY_SID, API_KEY_SECRET, ACCOUNT_SID);
        logger.info("SMS notification service initialized successfully");
    }

    /**
     * Validates the SMS notification configuration.
     * @throws IllegalStateException if any required configuration is missing
     */
    private static void validateConfiguration() {
        if (ACCOUNT_SID == null || ACCOUNT_SID.trim().isEmpty()) {
            throw new IllegalStateException("TWILIO_ACCOUNT_SID environment variable is not set");
        }
        if (API_KEY_SID == null || API_KEY_SID.trim().isEmpty()) {
            throw new IllegalStateException("TWILIO_API_KEY_SID environment variable is not set");
        }
        if (API_KEY_SECRET == null || API_KEY_SECRET.trim().isEmpty()) {
            throw new IllegalStateException("TWILIO_API_KEY_SECRET environment variable is not set");
        }
        if (TWILIO_PHONE_NUMBER == null || TWILIO_PHONE_NUMBER.trim().isEmpty()) {
            throw new IllegalStateException("TWILIO_PHONE_NUMBER environment variable is not set");
        }
        if (NOTIFICATION_PHONE_NUMBER == null || NOTIFICATION_PHONE_NUMBER.trim().isEmpty()) {
            throw new IllegalStateException("NOTIFICATION_PHONE_NUMBER environment variable is not set");
        }
    }

    /**
     * Sends a test SMS to verify the configuration.
     * @return true if the test message was sent successfully
     */
    public static boolean sendTestMessage() {
        try {
            String messageBody = "Test message from Permit Status Checker. If you receive this, SMS notifications are working correctly!";
            return sendMessage(messageBody);
        } catch (Exception e) {
            logger.error("Failed to send test SMS", e);
            return false;
        }
    }

    /**
     * Sends a notification about permit availability.
     * @param permitId The ID of the permit
     * @param message The message to send
     */
    public static void sendPermitAvailableNotification(String permitId, String message) {
        try {
            sendMessage(message);
        } catch (Exception e) {
            logger.error("Failed to send permit availability notification for permit {}: {}", permitId, e.getMessage());
        }
    }

    /**
     * Sends an SMS message.
     * @param messageBody The message to send
     * @return true if the message was sent successfully
     * @throws Exception if there's an error sending the message
     */
    private static boolean sendMessage(String messageBody) throws Exception {
        if (isTestMode()) {
            lastMessageBodyForTesting = messageBody;
            logger.info("[TEST MODE] SMS notification would be sent: {}", messageBody);
            return true;
        }
        try {
            Message message = Message.creator(
                new PhoneNumber(NOTIFICATION_PHONE_NUMBER),
                new PhoneNumber(TWILIO_PHONE_NUMBER),
                messageBody)
                .create();

            logger.info("SMS notification sent successfully. Message SID: {}", message.getSid());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS: {}", e.getMessage());
            throw e;
        }
    }

    // Test helper: returns last message when in test mode
    static String getLastMessageBodyForTesting() {
        return lastMessageBodyForTesting;
    }
} 