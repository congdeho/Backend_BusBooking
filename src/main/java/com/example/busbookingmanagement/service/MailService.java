package com.example.busbookingmanagement.service;

public interface MailService {
    void sendPaymentConfirmation(String toEmail, String subject, String body);
}

