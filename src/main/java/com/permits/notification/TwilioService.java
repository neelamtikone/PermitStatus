package com.permits.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;

public class TwilioService {
    private static final String ACCOUNT_SID;
    private static final String AUTH_TOKEN;
    private static final String API_KEY;
    private static final String API_SECRET;
    private static final String TWILIO_PHONE_NUMBER;

    static {
        Dotenv dotenv = Dotenv.load();
        ACCOUNT_SID = dotenv.get("TWILIO_ACCOUNT_SID");
        API_KEY = dotenv.get("TWILIO_API_KEY");
        API_SECRET = dotenv.get("TWILIO_API_SECRET");
        AUTH_TOKEN = dotenv.get("TWILIO_AUTH_TOKEN"); // fallback
        TWILIO_PHONE_NUMBER = dotenv.get("TWILIO_PHONE_NUMBER");

        // Debug prints
        System.out.println("Loaded from .env:");
        System.out.println("TWILIO_ACCOUNT_SID=" + ACCOUNT_SID);
        System.out.println("TWILIO_API_KEY=" + API_KEY);
        System.out.println("TWILIO_API_SECRET=" + (API_SECRET != null ? "[HIDDEN]" : null));
        System.out.println("TWILIO_AUTH_TOKEN=" + (AUTH_TOKEN != null ? "[HIDDEN]" : null));
        System.out.println("TWILIO_PHONE_NUMBER=" + TWILIO_PHONE_NUMBER);
        
        if (ACCOUNT_SID == null || TWILIO_PHONE_NUMBER == null ||
            ((API_KEY == null || API_SECRET == null) && AUTH_TOKEN == null)) {
            throw new IllegalStateException("Twilio credentials not found in environment variables");
        }

        if (API_KEY != null && API_SECRET != null) {
            Twilio.init(API_KEY, API_SECRET, ACCOUNT_SID);
        } else {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        }
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