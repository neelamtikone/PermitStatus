package com.permits.notification;

import io.github.cdimascio.dotenv.Dotenv;

public class TwilioServiceTest {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String to = dotenv.get("RECIPIENT_PHONE_NUMBER");
        if (to == null) {
            System.err.println("RECIPIENT_PHONE_NUMBER not found in .env file");
            return;
        }
        String message = "This is a test SMS from PermitStatus!";
        try {
            TwilioService.sendSMS(to, message);
            System.out.println("SMS sent successfully!");
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 