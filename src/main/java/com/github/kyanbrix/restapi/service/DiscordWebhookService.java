package com.github.kyanbrix.restapi.service;

import okhttp3.*;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscordWebhookService {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1472353251578876197/CUL1FXmhAPL1XrPKqE8ghULfmd1jJT1KgZprgEUjUbRyrL3AQT0CsniAG_VaY3UfQ8fQ";

    private final RestTemplate restTemplate;



    public DiscordWebhookService() {
        this.restTemplate = new RestTemplate();
    }


    public void sendToDiscord(Map<String,Object> payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    WEBHOOK_URL,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Discord embed notification sent successfully");
            } else {
                System.err.println("Failed to send Discord notification: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error sending Discord webhook: " + e.getMessage());
        }
    }


    public void sendWebhookMessage(String jsonData) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(jsonData, okhttp3.MediaType.get("application/json; charset=utf8"));

        Request request = new Request.Builder()
                .url(WEBHOOK_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("Error: "+ e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    System.out.println("Webhook message sent to discord");
                }else System.out.println("Not Success");

            }
        });



    }


}
