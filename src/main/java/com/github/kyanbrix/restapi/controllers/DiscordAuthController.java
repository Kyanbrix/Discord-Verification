package com.github.kyanbrix.restapi.controllers;


import com.github.kyanbrix.restapi.DiscordUserModel;
import com.github.kyanbrix.restapi.GuildResource;
import com.github.kyanbrix.restapi.MemberResource;
import com.github.kyanbrix.restapi.TokenResponse;
import com.github.kyanbrix.restapi.service.DiscordWebhookService;
import com.github.kyanbrix.restapi.service.TokenService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.*;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/auth/discord")
public class DiscordAuthController {



    @Value("${client.id}")
    private String CLIENT_ID;

    @Value("${client.secret}")
    private String CLIENT_SECRET;

    @Value("${bot.token}")
    private String TOKEN;

    @Value("${discord.token.url}")
    private String TOKEN_URL;

    @Value("${discord.auth.url}")
    private String AUTHORIZE_URL;

    @Value("${discord.redirect.uri}")
    private String REDIRECT_URI;



    @Autowired
    private TokenService tokenService;

    @Autowired
    private DiscordWebhookService webhookService;

    @Value("${guild.id}")
    private String GUILD_ID;

    private static final String DISCORD_URL_BASE = "https://discord.com/api/v10";




    //User Login

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {

        response
                .sendRedirect("https://discord.com/oauth2/authorize?client_id=1400504977134325781&response_type=code&redirect_uri=https%3A%2F%2Fdiscord-verification.up.railway.app%2Fauth%2Fdiscord%2Fredirect&scope=identify+guilds+guilds.join");

    }

    //Return a user information
    @GetMapping("/redirect")
    public void callback(@RequestParam(value = "code", required = false) String code,@RequestParam(value = "error",required = false) String error,@RequestParam(value = "description", required = false) String description,HttpServletResponse response) throws IOException {

        try {

            if (error != null) {

                if (error.equals("access_denied")) {
                    response.sendRedirect("/cancel-login.html?error="+error);
                }

                return;
            }


            TokenResponse tokenResponse = exchangeCodeForToken(code);

            DiscordUserModel userModel = getDiscordUserModel(tokenResponse.getAccess_token());

            if (isUserIfAdmin(userModel.getId())) {

                response.sendRedirect("/admin-prompt.html");
                return;

            } else if (isUserIfAlreadyVerified(userModel.getId())) {

                response.sendRedirect("/user-verified-prompt.html");

                return;
            }


            validateMember(userModel.getId(),tokenResponse.getAccess_token(),userModel.getUsername(),userModel.getAvatar());

            String verifiedUrl = UriComponentsBuilder.fromPath("/verified.html")
                    .queryParam("username", userModel.getUsername())
                    .queryParam("email", userModel.getEmail())
                    .queryParam("id", userModel.getId())
                    .queryParam("avatar", String.format("https://cdn.discordapp.com/avatars/%s/%s.png?size=64",userModel.getId(),userModel.getAvatar()))
                    .build()
                    .toUriString();


            response.sendRedirect(verifiedUrl);

        }catch (Exception e) {

            System.out.println(e.getMessage());
        }

    }



    private TokenResponse exchangeCodeForToken(String code) {


        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", REDIRECT_URI);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(TOKEN_URL,request, TokenResponse.class);

    }

    private DiscordUserModel getDiscordUserModel(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);


        ResponseEntity<DiscordUserModel> response = restTemplate.exchange(
                "https://discord.com/api/users/@me",
                HttpMethod.GET,
                request,
                DiscordUserModel.class
        );


        return response.getBody();

    }

    private void addRoleToMember(String userId, String access_token,String userName,String user_avatar) {

        String url = String.format("%s/guilds/%s/members/%s/roles/1469676754267275437",DISCORD_URL_BASE,GUILD_ID,userId);

        final String AVATAR_URL = String.format("https://cdn.discordapp.com/avatars/%s/%s.png",userId,user_avatar);


        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","Bot " + TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("access_token",access_token);
        requestBody.put("roles","1469676754267275437");

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(requestBody,headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                req,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Successfully added a role to user");
            sendWebhookMessageToDiscord(userId,userName,AVATAR_URL);
        }

    }


    public void addMemberToGuild(String userId, String access_token, String userName, String user_avatar) {
        String url = String.format("%s/guilds/%s/members/%s",DISCORD_URL_BASE,GUILD_ID,userId);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","Bot " + TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("access_token",access_token);


        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody,headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            addRoleToMember(userId,access_token,userName,user_avatar);
            sendWebhookMessageToDiscord(userId,userName,user_avatar);
        }else System.out.println("Cannot add user to the guild");

    }



    private void validateMember(String userId, String access_token, String userName, String avatar) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format("%s/guilds/%s/members/%s",DISCORD_URL_BASE,GUILD_ID,userId))
                .header("Authorization","Bot "+TOKEN)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.code() != 404) {
                    addRoleToMember(userId,access_token,userName,avatar);
                }else addMemberToGuild(userId,access_token,userName,avatar);


                tokenService.revokeToken(access_token);

            }
        });

    }


    private void sendWebhookMessageToDiscord(String userId, String userName, String user_avatar) {


        final String AVATAR_URL = String.format("https://cdn.discordapp.com/avatars/%s/%s.png",userId,user_avatar);

        Map<String, Object> data = getEmbed(userId, userName, AVATAR_URL);

        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

        String json = gson.toJson(data);

        webhookService.sendWebhookMessage(json);


    }

    private static @NonNull Map<String, Object> getEmbed(String userId, String userName, String AVATAR_URL) {

        Map<String, Object> author = new HashMap<>();
        author.put("name", userName);
        author.put("icon_url", AVATAR_URL);


        Map<String, Object> firstEmbed = new HashMap<>();
        firstEmbed.put("description",String.format("<@%s> has joined the server", userId));
        firstEmbed.put("author",author);
        firstEmbed.put("color","15314480");


        List<Map<String, Object>> embeds = new ArrayList<>();
        embeds.add(firstEmbed);


        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("embeds",embeds);
        jsonBody.put("content",null);

        return jsonBody;
    }


    private boolean isUserIfAdmin(String userId) {

        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
            .url(DISCORD_URL_BASE+"/guilds/"+GUILD_ID)
                .addHeader("Authorization","Bot "+ TOKEN)
                .addHeader("Content-Type","application/json")

                .build();

        try (Response rs = client.newCall(request).execute()) {

            if (rs.isSuccessful()) {
                String data = rs.body().string();
                Gson gson = new Gson();

                GuildResource guildResource = gson.fromJson(data, GuildResource.class);

                return (userId.equals(guildResource.owner_id));

            }else {
                System.out.println("Response unsuccessful "+ rs.body());
                return false;
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

        return false;


    }

    private boolean isUserIfAlreadyVerified(String userId) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(DISCORD_URL_BASE+"/guilds/%s/members/%s",GUILD_ID,userId))
                .addHeader("Authorization","Bot "+TOKEN)
                .get()
                .build();

        try(Response response = client.newCall(request).execute()) {

            if (response.isSuccessful()) {

                Gson gson = new Gson();

                String data = response.body().string();

                MemberResource memberResource = gson.fromJson(data, MemberResource.class);

                return memberResource.roles.contains("1469676754267275437");

            }

        }catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }







}
