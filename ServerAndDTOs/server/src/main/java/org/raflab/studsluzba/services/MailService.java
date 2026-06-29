package org.raflab.studsluzba.services;

public interface MailService {
    MailSendResult send(String recipientEmail, String subject, String body);
}
