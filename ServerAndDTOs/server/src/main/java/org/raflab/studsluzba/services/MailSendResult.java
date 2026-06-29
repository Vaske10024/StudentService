package org.raflab.studsluzba.services;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MailSendResult {
    private final boolean sent;
    private final String providerMessageId;
    private final String errorMessage;

    public static MailSendResult sent(String providerMessageId) {
        return new MailSendResult(true, providerMessageId, null);
    }

    public static MailSendResult failed(String errorMessage) {
        return new MailSendResult(false, null, errorMessage);
    }
}
