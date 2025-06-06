package com.rei.permit;

public class SmsTest {
    public static void main(String[] args) {
        try {
            System.out.println("Initializing SMS notification service...");
            SmsNotificationService.initialize();
            
            System.out.println("Sending test SMS message...");
            boolean success = SmsNotificationService.sendTestMessage();
            
            if (success) {
                System.out.println("Test SMS sent successfully! Please check your phone.");
            } else {
                System.out.println("Failed to send test SMS. Check the logs for details.");
            }
        } catch (Exception e) {
            System.err.println("Error testing SMS configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 