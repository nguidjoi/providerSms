package org.keycloak.examples.authenticator.credential.dto;

public class Content {
    String message;
    String senderId;
    String receiverPhone;

    public Content() {
    }

    public Content(String message, String senderId, String receiverPhone) {
        this.message = message;
        this.senderId = senderId;
        this.receiverPhone = receiverPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    @Override
    public String toString() {
        return "Content{" +
                "message='" + message + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverPhone='" + receiverPhone + '\'' +
                '}';
    }
}
