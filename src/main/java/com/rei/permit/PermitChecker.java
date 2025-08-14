package com.rei.permit;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PermitChecker {
    private static final Logger logger = LoggerFactory.getLogger(PermitChecker.class);
    private static RecreationGovClient apiClient = new RecreationGovClient();
    
    // Permit IDs for Enchantments and Mount Whitney
    private static final List<Permit> PERMITS = Arrays.asList(
        new Permit(
            Config.getProperty("permit.enchatments.id"),
            Config.getProperty("permit.enchatments.name"),
            Config.getProperty("permit.enchatments.url"),
            Config.getProperty("permit.enchatments.dates")
        ),
        new Permit(
            Config.getProperty("permit.whitney.id"),
            Config.getProperty("permit.whitney.name"),
            Config.getProperty("permit.whitney.url"),
            Config.getProperty("permit.whitney.dates")
        )
    );

    // Keep track of notified dates with their timestamps
    private static final Map<String, LocalDateTime> notifiedDates = new HashMap<>();
    private static final int NOTIFICATION_TTL_HOURS = 24; // Notifications expire after 24 hours

    // Test-only hook to inject a mock client
    static void setApiClientForTesting(RecreationGovClient client) {
        apiClient = client;
    }

    public static void main(String[] args) {
        logger.info("Starting Permit Status Checker");
        
        try {
            // Initialize SMS notification service
            SmsNotificationService.initialize();
            logger.info("SMS notification service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize SMS notification service", e);
            return;
        }
        
        // Create a scheduler that runs at configured interval
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAllPermits();
            } catch (Exception e) {
                logger.error("Error checking permit availability", e);
            }
        }, 0, Config.getCheckIntervalMinutes(), TimeUnit.MINUTES);
    }

    private static void checkAllPermits() {
        // Clean up expired notifications first
        cleanupExpiredNotifications();
        
        for (Permit permit : PERMITS) {
            try {
                checkPermitAvailability(permit);
                // Add a small delay between requests to avoid overwhelming the API
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("Error checking permit {}: {}", permit.getName(), e.getMessage());
            }
        }
    }

    private static void cleanupExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        notifiedDates.entrySet().removeIf(entry -> 
            entry.getValue().plusHours(NOTIFICATION_TTL_HOURS).isBefore(now));
    }

    /**
     * Checks the availability of a specific permit and sends notifications if available.
     *
     * @param permit The permit to check
     * @throws IOException if there's an error communicating with the API
     */
    public static void checkPermitAvailability(Permit permit) throws IOException {
        try {
            JsonNode jsonNode = apiClient.getPermitAvailability(permit.getId());

            // Log the current time and availability status
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            logger.info("Checking {} permit availability at: {}", permit.getName(), currentTime);

            // Check if permits are available
            PermitAvailability availability = checkAvailability(jsonNode, permit);
            if (availability.isAvailable() && isTargetDate(permit, availability.getAvailableDate())) {
                String notificationKey = permit.getId() + "_" + availability.getAvailableDate();
                
                // Only send notification if we haven't notified about this date before
                if (!notifiedDates.containsKey(notificationKey)) {
                    String message = String.format("%s permit is available for date: %s with %d permits remaining!",
                        permit.getName(),
                        availability.getAvailableDate(),
                        availability.getRemainingPermits());
                    
                    logger.info(message);
                    SmsNotificationService.sendPermitAvailableNotification(permit.getId(), message);
                    notifiedDates.put(notificationKey, LocalDateTime.now());
                } else {
                    logger.info("Already notified about {} permit for date {}", 
                        permit.getName(), availability.getAvailableDate());
                }
            } else if (!availability.isAvailable()) {
                // If permits are no longer available, remove from notified dates
                String notificationKey = permit.getId() + "_" + availability.getAvailableDate();
                if (notifiedDates.remove(notificationKey) != null) {
                    logger.info("Removed {} permit for date {} from notified dates as it's no longer available",
                        permit.getName(), availability.getAvailableDate());
                }
            }
        } catch (IOException e) {
            logger.error("Error making API request for {}: {}", permit.getName(), e.getMessage());
            throw e;
        }
    }

    private static boolean isTargetDate(Permit permit, String availableDate) {
        if (!permit.hasTargetDates()) {
            return true; // If no specific dates are configured, notify for any available date
        }

        try {
            LocalDate date = LocalDate.parse(availableDate);
            return permit.getTargetDates().contains(date);
        } catch (Exception e) {
            logger.error("Error parsing date {} for permit {}: {}", availableDate, permit.getName(), e.getMessage());
            return false;
        }
    }

    private static PermitAvailability checkAvailability(JsonNode jsonNode, Permit permit) {
        try {
            // Different logic for different permits
            switch (permit.getId()) {
                case "233260": // Enchantments
                    return checkEnchantmentsAvailability(jsonNode);

                case "445859": // Mount Whitney
                    return checkMountWhitneyAvailability(jsonNode);

                default:
                    logger.warn("Unknown permit type: {}", permit.getId());
                    return new PermitAvailability(false, null, 0);
            }
        } catch (Exception e) {
            logger.error("Error parsing availability response for {}: {}", permit.getName(), e.getMessage());
            return new PermitAvailability(false, null, 0);
        }
    }

    private static PermitAvailability checkEnchantmentsAvailability(JsonNode jsonNode) {
        if (jsonNode.has("availability")) {
            JsonNode availability = jsonNode.get("availability");
            for (JsonNode date : availability) {
                if (date.has("remaining") && date.get("remaining").asInt() > 0) {
                    String dateStr = date.has("date") ? date.get("date").asText() : "Unknown date";
                    int remaining = date.get("remaining").asInt();
                    return new PermitAvailability(true, dateStr, remaining);
                }
            }
        }
        return new PermitAvailability(false, null, 0);
    }

    private static PermitAvailability checkMountWhitneyAvailability(JsonNode jsonNode) {
        if (jsonNode.has("availability")) {
            JsonNode availability = jsonNode.get("availability");
            for (JsonNode date : availability) {
                if (date.has("remaining") && date.get("remaining").asInt() > 0) {
                    String dateStr = date.has("date") ? date.get("date").asText() : "Unknown date";
                    int remaining = date.get("remaining").asInt();
                    return new PermitAvailability(true, dateStr, remaining);
                }
            }
        }
        return new PermitAvailability(false, null, 0);
    }
} 