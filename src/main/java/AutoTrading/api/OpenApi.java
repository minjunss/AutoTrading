package AutoTrading.api;

import AutoTrading.dto.AccountResponseDto;
import AutoTrading.dto.QueryAndJwtDto;
import AutoTrading.jwt.JwtTokenProvider;
import AutoTrading.paths.UpbitApiPaths;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenApi {

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    private final Gson gson;
    private static final String ORDER_URL = UpbitApiPaths.BASE_SERVER_URL + "/orders";
    private static final String ACCOUNTS_URL = UpbitApiPaths.BASE_SERVER_URL + "/accounts";
    private static final String CANDLE_URL = UpbitApiPaths.BASE_SERVER_URL + "/candles/minutes/";

    public String order(String market, String side,
                        String volume, String price, String ord_type) throws NoSuchAlgorithmException, IOException {

        HashMap<String, String> params = new HashMap<>();
        params.put("market", market);
        params.put("side", side);// bid매수 ask매도
        params.put("volume", volume);
        params.put("price", price);
        params.put("ord_type", ord_type);

        QueryAndJwtDto queryAndJwtDto = jwtTokenProvider.createTokenForOrder(params);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(ORDER_URL);
        request.setHeader("Content-Type", "application/json");
        request.addHeader("Authorization", queryAndJwtDto.getJwtToken());
        request.setEntity(new StringEntity(gson.toJson(params)));

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        String result = EntityUtils.toString(entity, "UTF-8");

        return result;
    }

    public String possibleOrder() throws IOException, NoSuchAlgorithmException {
        HashMap<String, String> params = new HashMap<>();
        params.put("market", "KRW-BTC");

        QueryAndJwtDto queryAndJwtDto = jwtTokenProvider.createTokenForOrder(params);

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(ORDER_URL + "/chance?" + queryAndJwtDto.getQuery());
        request.setHeader("Content-Type", "application/json");
        request.addHeader("Authorization", queryAndJwtDto.getJwtToken());

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        String result = EntityUtils.toString(entity, "UTF-8");

        return result;
    }

    public String cancelOrder(HashMap<String, String> params) throws IOException, NoSuchAlgorithmException {

        QueryAndJwtDto queryAndJwtDto = jwtTokenProvider.createTokenForOrder(params);

        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete request = new HttpDelete(ORDER_URL + "?" + queryAndJwtDto.getQuery());
        request.setHeader("Content-Type", "application/json");
        request.addHeader("Authorization", queryAndJwtDto.getJwtToken());

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        String result = EntityUtils.toString(entity, "UTF-8");

        return result;
    }

    public String checkOrder(HashMap<String, String> params) throws IOException, NoSuchAlgorithmException {
        QueryAndJwtDto queryAndJwtDto = jwtTokenProvider.createTokenForOrder(params);

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(ORDER_URL + "?" + queryAndJwtDto.getQuery());
        request.setHeader("Content-Type", "application/json");
        request.addHeader("Authorization", queryAndJwtDto.getJwtToken());

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        String result = EntityUtils.toString(entity, "UTF-8");

        return result;
    }

    public List<AccountResponseDto> getAccounts() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtTokenProvider.createTokenForAccount());
        headers.setContentType(MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

        List<AccountResponseDto> accounts = restTemplate.exchange(ACCOUNTS_URL, HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<AccountResponseDto>>() {
                }).getBody();

        return accounts;
    }

    public String getMinuteCandle(String market, String indicator, int unit) throws IOException {
        int count = 0;
        if(indicator.equalsIgnoreCase("rsi")) count = 200;
        else if(indicator.equalsIgnoreCase("cci")) count = 20;

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(CANDLE_URL + unit + "?market=" + market + "&count=" + count);
        request.addHeader("accept", "application/json");

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");

        return result;
    }

    public String getPresentPrice() throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(UpbitApiPaths.BASE_SERVER_URL + "/ticker?markets=KRW-BTC");
        request.addHeader("accept", "application/json");

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        String result = EntityUtils.toString(entity, "UTF-8");

        return result;
    }

}
