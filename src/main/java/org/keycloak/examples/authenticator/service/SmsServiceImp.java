package org.keycloak.examples.authenticator.service;

import com.google.gson.Gson;
import org.keycloak.examples.authenticator.credential.dto.Content;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class SmsServiceImp implements SmsService{
    @Override
    public void send(String phoneNumber, String message) {
        HttpClient client= HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

        Content content = new Content();
        content.setMessage(message);
        content.setSenderId("test sms");
        content.setReceiverPhone(phoneNumber);
        String messageContent = new Gson().toJson(content);

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8081/send"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(messageContent)).build();

        client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
    }
}
