package com.rei.permit;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermitTest {

    @Test
    void testPermitCreationWithSet() {
        // Prepare test data
        String id = "233260";
        String name = "Enchantments";
        String url = "https://www.recreation.gov/permits/233260";
        Set<LocalDate> targetDates = new HashSet<>();
        targetDates.add(LocalDate.parse("2024-08-01"));
        targetDates.add(LocalDate.parse("2024-08-02"));

        // Create permit
        Permit permit = new Permit(id, name, url, targetDates);

        // Verify permit details
        assertEquals(id, permit.getId());
        assertEquals(name, permit.getName());
        assertEquals(url, permit.getUrl());
        assertEquals(targetDates, permit.getTargetDates());
        assertTrue(permit.hasTargetDates());
    }

    @Test
    void testPermitCreationWithDatesString() {
        // Prepare test data
        String id = "233260";
        String name = "Enchantments";
        String url = "https://www.recreation.gov/permits/233260";
        String datesConfig = "2024-08-01,2024-08-02";

        // Create permit
        Permit permit = new Permit(id, name, url, datesConfig);

        // Verify permit details
        assertEquals(id, permit.getId());
        assertEquals(name, permit.getName());
        assertEquals(url, permit.getUrl());
        
        // Verify dates
        Set<LocalDate> expectedDates = new HashSet<>();
        expectedDates.add(LocalDate.parse("2024-08-01"));
        expectedDates.add(LocalDate.parse("2024-08-02"));
        assertEquals(expectedDates, permit.getTargetDates());
        assertTrue(permit.hasTargetDates());
    }

    @Test
    void testPermitCreationWithEmptyDates() {
        // Prepare test data
        String id = "233260";
        String name = "Enchantments";
        String url = "https://www.recreation.gov/permits/233260";
        String datesConfig = "";

        // Create permit
        Permit permit = new Permit(id, name, url, datesConfig);

        // Verify permit details
        assertEquals(id, permit.getId());
        assertEquals(name, permit.getName());
        assertEquals(url, permit.getUrl());
        assertTrue(permit.getTargetDates().isEmpty());
        assertFalse(permit.hasTargetDates());
    }

    @Test
    void testPermitCreationWithInvalidDates() {
        // Prepare test data
        String id = "233260";
        String name = "Enchantments";
        String url = "https://www.recreation.gov/permits/233260";
        String datesConfig = "2024-08-01,invalid-date,2024-08-02";

        // Create permit
        Permit permit = new Permit(id, name, url, datesConfig);

        // Verify permit details
        assertEquals(id, permit.getId());
        assertEquals(name, permit.getName());
        assertEquals(url, permit.getUrl());
        
        // Verify dates (should only contain valid dates)
        Set<LocalDate> expectedDates = new HashSet<>();
        expectedDates.add(LocalDate.parse("2024-08-01"));
        expectedDates.add(LocalDate.parse("2024-08-02"));
        assertEquals(expectedDates, permit.getTargetDates());
        assertTrue(permit.hasTargetDates());
    }
} 