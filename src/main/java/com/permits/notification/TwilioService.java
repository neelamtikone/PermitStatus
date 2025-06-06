package com.permits.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;

public class TwilioService {
    private static final String ACCOUNT_SID;
    private static final String AUTH_TOKEN;
    private static final String TWILIO_PHONE_NUMBER;

    static {
        Dotenv dotenv = Dotenv.load();
        ACCOUNT_SID = dotenv.get("TWILIO_ACCOUNT_SID");
        AUTH_TOKEN = dotenv.get("TWILIO_AUTH_TOKEN");
        TWILIO_PHONE_NUMBER = dotenv.get("TWILIO_PHONE_NUMBER");
        
        if (ACCOUNT_SID == null || AUTH_TOKEN == null || TWILIO_PHONE_NUMBER == null) {
            throw new IllegalStateException("Twilio credentials not found in environment variables");
        }
        
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendSMS(String to, String message) {
        try {
            Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(TWILIO_PHONE_NUMBER),
                message
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }
} 