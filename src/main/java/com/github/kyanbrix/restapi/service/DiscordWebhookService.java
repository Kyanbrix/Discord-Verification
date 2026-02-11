package com.github.kyanbrix.restapi.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscordWebhookService {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1468371784654131358/SOHAVGoa2hQT9w__PcRjgS7b-EXOEnjtmymglXl3oFtNth7NmapZGEsUDXOZ2Gx4m-U6";

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

    public void postRequestWebhook(String quote) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> payload = new HashMap<>();


        //Embed Builder
        Map<String, Object> embed = new HashMap<>();

        embed.put("title","Quote Created");
        embed.put("description","Successfully added a quote `` "+quote+" ``");
        embed.put("timestamp", Instant.now().toString());





        List<Map<String,Object>> embeds = new ArrayList<>();
        embeds.add(embed);

        payload.put("embeds",embeds);

        payload.put("username","Rest API");
        payload.put("avatar_url","https://avatars.githubusercontent.com/u/100282972?v=4");



        HttpEntity<Map<String,Object>> request = new HttpEntity<>(payload,headers);

        ResponseEntity<String> response = restTemplate.postForEntity(WEBHOOK_URL,request,String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Successfully sent to discord");
        }else System.out.println("Error!");


    }


}
