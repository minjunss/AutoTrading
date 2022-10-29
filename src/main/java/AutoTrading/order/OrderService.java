package AutoTrading.order;

import AutoTrading.candle.CandleService;
import AutoTrading.jwt.JwtTokenProvider;
import AutoTrading.paths.UpbitApiPaths;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class OrderService {
    private final CandleService candleService;
    private final PossibleOrderRepository possibleOrderRepository;
    private final Gson gson;
    public static final String ORDER_URL = UpbitApiPaths.BASE_SERVER_URL + "/orders";
    @Value("${security.access-key}")
    private String accessKey;
    @Value("${security.secret-key}")
    private String secretKey;

    public void order(String market, String side,
                      String volume, String price, String ord_type) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        HashMap<String, String> params = new HashMap<>();
        params.put("market", market);
        params.put("side", side);// bid매수 ask매도
        params.put("volume", volume);
        params.put("price", price);
        params.put("ord_type", ord_type);


        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(ORDER_URL);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);
            request.setEntity(new StringEntity(gson.toJson(params)));

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            log.info(EntityUtils.toString(entity, "UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getPossibleOrder() throws NoSuchAlgorithmException, UnsupportedEncodingException {

        HashMap<String, String> params = new HashMap<>();
        params.put("market", "KRW-BTC");

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(ORDER_URL + "/chance?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String result = EntityUtils.toString(entity, "UTF-8");

            PossibleOrder possibleOrder = savePossibleOrder(result);

            return "매수가능금액: " + possibleOrder.getBidPossibleOrder() + " 매도가능금액: " + possibleOrder.getAskPossibleOrder();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    private PossibleOrder savePossibleOrder(String result) {
//        JsonArray obj = gson.fromJson(result, JsonArray.class);
//        JsonObject temp = obj.get(0).getAsJsonObject();
        JsonObject obj = gson.fromJson(result, JsonObject.class);
        PossibleOrder possibleOrder = PossibleOrder.builder()
                .bidPossibleOrder(obj.get("bid_account.balance").getAsString())
                .askPossibleOrder(obj.get("ask_account.balance").getAsString())
                .build();

        possibleOrderRepository.save(possibleOrder);

        return possibleOrder;
    }


    public void autoTrade(int unit) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        double rsi = candleService.viewMinuteCandleRSI(unit);

        if (rsi <= 20) {
            order("KRW-BTC", "BID", "", "5000", "price");
        }

//        if (rsi >= 80) {
//            order("KRW-BTC", "ASK", )
//        }

    }
}
