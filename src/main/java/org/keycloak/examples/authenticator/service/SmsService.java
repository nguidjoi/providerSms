package org.keycloak.examples.authenticator.service;

/**
 * Sms sender service.
 */
public interface SmsService {
    /**
     * Send message to phone mumber.
     * @param phoneNumber the number to send message.
     * @param message to sent
     */
    void send(String phoneNumber, String message);
}
