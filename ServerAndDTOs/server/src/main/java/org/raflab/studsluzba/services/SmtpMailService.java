package org.raflab.studsluzba.services;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class SmtpMailService implements MailService {
    private final ObjectProvider<JavaMailSender> senderProvider;
    private final String host;
    private final String from;

    public SmtpMailService(ObjectProvider<JavaMailSender> senderProvider,
                           @Value("${spring.mail.host:}") String host,
                           @Value("${app.mail.from:}") String from) {
        this.senderProvider = senderProvider;
        this.host = host == null ? "" : host.trim();
        this.from = from == null ? "" : from.trim();
    }

    @Override
    public MailSendResult send(String recipientEmail, String subject, String body) {
        JavaMailSender sender = senderProvider.getIfAvailable();
        if (host.isEmpty() || sender == null) {
            return MailSendResult.failed("SMTP is not configured.");
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            message.setRecipients(javax.mail.Message.RecipientType.TO, recipientEmail);
            if (!from.isEmpty()) {
                message.setFrom(from);
            }
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");
            sender.send(message);
            return MailSendResult.sent(message.getMessageID());
        } catch (Exception ignored) {
            return MailSendResult.failed("Email provider rejected or could not deliver the message.");
        }
    }
}
