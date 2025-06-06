package com.rei.permit;

public class PermitAvailability {
    private final boolean available;
    private final String availableDate;
    private final int remainingPermits;

    public PermitAvailability(boolean available, String availableDate, int remainingPermits) {
        this.available = available;
        this.availableDate = availableDate;
        this.remainingPermits = remainingPermits;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getAvailableDate() {
        return availableDate;
    }

    public int getRemainingPermits() {
        return remainingPermits;
    }
} 