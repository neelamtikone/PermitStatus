package com.permits.notification;

import com.permits.model.Permit;
import com.permits.model.PermitStatus;
import com.permits.model.User;
import com.permits.repository.PermitRepository;
import com.permits.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final PermitRepository permitRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(PermitRepository permitRepository, UserRepository userRepository) {
        this.permitRepository = permitRepository;
        this.userRepository = userRepository;
    }

    public void sendStatusUpdateNotification(Permit permit) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Your permit %s status has been updated to %s",
            permit.getPermitNumber(),
            permit.getStatus()
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendExpirationNotification(Permit permit) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Your permit %s will expire in 30 days. Please renew it soon.",
            permit.getPermitNumber()
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendRenewalReminder(Permit permit) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Your permit %s is due for renewal. Please submit your renewal application.",
            permit.getPermitNumber()
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendApprovalNotification(Permit permit) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Congratulations! Your permit %s has been approved.",
            permit.getPermitNumber()
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendRejectionNotification(Permit permit, String reason) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Your permit %s application has been rejected. Reason: %s",
            permit.getPermitNumber(),
            reason
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendDocumentRequestNotification(Permit permit, String documentType) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Additional document required for permit %s: %s. Please submit it as soon as possible.",
            permit.getPermitNumber(),
            documentType
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendInspectionScheduledNotification(Permit permit, String inspectionDate) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "An inspection has been scheduled for your permit %s on %s. Please ensure the site is ready.",
            permit.getPermitNumber(),
            inspectionDate
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendFeePaymentReminder(Permit permit, double amount) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Payment reminder for permit %s: $%.2f is due. Please make the payment to avoid delays.",
            permit.getPermitNumber(),
            amount
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendViolationNotification(Permit permit, String violationDetails) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Violation notice for permit %s: %s. Please address this issue immediately.",
            permit.getPermitNumber(),
            violationDetails
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }

    public void sendEmergencyClosureNotification(Permit permit, String reason) {
        User user = userRepository.findById(permit.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "EMERGENCY: Your permit %s has been temporarily suspended. Reason: %s. Please contact the office immediately.",
            permit.getPermitNumber(),
            reason
        );

        TwilioService.sendSMS(user.getPhoneNumber(), message);
    }
} 