package com.example.UbitAutoTrading.account;

import com.example.UbitAutoTrading.jwt.JwtTokenProvider;
import com.example.UbitAutoTrading.paths.UpbitApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AccountService {

    public static final String ACCOUNTS_URL = UpbitApiPaths.BASE_SERVER_URL + "/accounts";
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;


    public List<AccountResponse> getAccounts() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtTokenProvider.createTokenForAccount());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(ACCOUNTS_URL, HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<AccountResponse>>() {
                }).getBody();
    }
}
