package com.rei.permit;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a permit with its details and target dates.
 */
public class Permit {
    private final String id;
    private final String name;
    private final String url;
    private final Set<LocalDate> targetDates;

    /**
     * Creates a new Permit with the specified details and target dates.
     *
     * @param id The permit ID
     * @param name The permit name
     * @param url The permit URL
     * @param targetDates The set of target dates to monitor
     */
    public Permit(String id, String name, String url, Set<LocalDate> targetDates) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.targetDates = new HashSet<>(targetDates);
    }

    /**
     * Creates a new Permit with the specified details and target dates string.
     * The dates string should be comma-separated dates in YYYY-MM-DD format.
     *
     * @param id The permit ID
     * @param name The permit name
     * @param url The permit URL
     * @param datesConfig Comma-separated dates in YYYY-MM-DD format
     */
    public Permit(String id, String name, String url, String datesConfig) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.targetDates = parseDates(datesConfig);
    }

    private Set<LocalDate> parseDates(String datesConfig) {
        Set<LocalDate> dates = new HashSet<>();
        if (datesConfig != null && !datesConfig.trim().isEmpty()) {
            String[] dateStrings = datesConfig.split(",");
            for (String dateStr : dateStrings) {
                try {
                    dates.add(LocalDate.parse(dateStr.trim()));
                } catch (Exception e) {
                    // Log error and continue with other dates
                    System.err.println("Error parsing date: " + dateStr);
                }
            }
        }
        return dates;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Set<LocalDate> getTargetDates() {
        return new HashSet<>(targetDates);
    }

    public boolean hasTargetDates() {
        return !targetDates.isEmpty();
    }
} 