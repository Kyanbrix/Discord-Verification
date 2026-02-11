package com.github.kyanbrix.restapi.controllers;


import com.github.kyanbrix.restapi.DiscordUserModel;
import com.github.kyanbrix.restapi.TokenResponse;
import com.github.kyanbrix.restapi.service.DiscordWebhookService;
import com.github.kyanbrix.restapi.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void requestAuth(HttpServletResponse response) throws IOException {


        response.sendRedirect("https://discord.com/oauth2/authorize?client_id=1400504977134325781&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fauth%2Fdiscord%2Fredirect&scope=guilds.join+identify");

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

            if (!isMemberOnGuild(userModel.getId())) {

                addMemberToGuild(userModel.getId(), tokenResponse.getAccess_token());

                addRoleToMember(userModel.getId(),tokenResponse.getAccess_token(),"1469676754267275437");

            } else addRoleToMember(userModel.getId(), tokenResponse.getAccess_token(),"1469676754267275437");


            String verifiedUrl = UriComponentsBuilder.fromPath("/verified.html")
                    .queryParam("username", userModel.getUsername())
                    .queryParam("email", userModel.getEmail())
                    .queryParam("id", userModel.getId())
                    .queryParam("avatar", String.format("https://cdn.discordapp.com/avatars/%s/%s.png?size=64",userModel.getId(),userModel.getAvatar()))
                    .build()
                    .toUriString();


            sendWebhookMessageToDiscord(userModel.getId(),userModel.getUsername(),userModel.getAvatar());

            tokenService.revokeToken(tokenResponse.getAccess_token());

            response.sendRedirect(verifiedUrl);


        }catch (Exception e) {

            IO.println(e.getMessage());
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

    private void addRoleToMember(String userId, String access_token,String roleId) {

        String url = String.format("%s/guilds/%s/members/%s/roles/%s",DISCORD_URL_BASE,GUILD_ID,userId,roleId);


        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","Bot " + TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("access_token",access_token);
        requestBody.put("roles",roleId);

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(requestBody,headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                req,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            IO.println("Successfully added a role to user");

        }else IO.println(response.getBody());






    }


    public void addMemberToGuild(String userId, String access_token) {
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
            IO.println(response.getBody());
        }else IO.println(response.getBody());

    }



    private boolean isMemberOnGuild(String userId) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format("%s/guilds/%s/members/%s",DISCORD_URL_BASE,GUILD_ID,userId))
                .header("Authorization","Bot "+TOKEN)
                .build();


        try (Response response = client.newCall(request).execute()) {

            IO.println(response.code());

            return response.code() != 404;

        } catch (IOException e) {
            IO.println(e);
            return false;
        }

    }


    private void sendWebhookMessageToDiscord(String userId, String userName, String user_avatar) {


        final String AVATAR_URL = String.format("https://cdn.discordapp.com/avatars/%s/%s.png",userId,user_avatar);

        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> embed = new HashMap<>();
        embed.put("color","5814783");
        embed.put("description",String.format("<@%s> has joined the server",userId));
        embed.put("timestamp", Instant.now().toString());


        Map<String, String> author = Map.of("name",userName,
                "icon_url",AVATAR_URL
        );

        embed.put("author",author);

        List<Map<String, Object>> embeds = new ArrayList<>();
        embeds.add(embed);

        payload.put("embeds",embeds);



        webhookService.sendToDiscord(payload);


    }








}
