package com.github.kyanbrix.restapi.service;

import com.github.kyanbrix.restapi.UserToken;
import com.github.kyanbrix.restapi.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


@Service
public class TokenService {



    @Autowired
    private TokenRepository tokenRepository;


    @Value("${discord.redirect.uri}")
    private String REDIRECT_URI;

    @Value("${client.secret}")
    private String CLIENT_SECRET;

    @Value("${client.id}")
    private String CLIENT_ID;

    public void saveUserToken(String id, String accessToken, String refreshToken, int expiresAt) {

        UserToken userToken = new UserToken();

        userToken.setUserId(id);
        userToken.setAccessToken(accessToken);
        userToken.setRefreshToken(refreshToken);
        userToken.setExpiresAt(LocalDateTime.now().plusSeconds(expiresAt));

        tokenRepository.save(userToken);

    }

    public String getAccessToken(String userId) {

        UserToken userToken = tokenRepository.findByUserId(userId);

        if (userToken != null && userToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneId.of("Asia/Manila")))) {

            return null;

        }

        return userToken != null ? userToken.getAccessToken() : null;

    }

    public String getRefreshToken(String userId) {

        UserToken userToken = tokenRepository.findByUserId(userId);

        return userToken != null ? userToken.getRefreshToken() : null;
    }

    public List<UserToken> getRefreshTokens(String userId) {

        return tokenRepository.findTokenByUserId(userId);

    }

    public void revokeToken(String access_token) {

        RestTemplate restTemplate = new RestTemplate();


        MultiValueMap<String ,String> body = new LinkedMultiValueMap<>();

        body.add("client_id",CLIENT_ID);
        body.add("client_secret",CLIENT_SECRET);
        body.add("token",access_token);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);


        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://discord.com/api/oauth2/token/revoke",
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) System.out.println("Token is now revoked!");

    }






}
