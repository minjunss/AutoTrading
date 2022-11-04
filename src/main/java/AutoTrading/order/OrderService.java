package AutoTrading.order;

import AutoTrading.account.AccountBalance;
import AutoTrading.candle.CandleService;
import AutoTrading.exception.BusinessException;
import AutoTrading.exception.ExceptionCode;
import AutoTrading.paths.UpbitApiPaths;
import AutoTrading.uuid.Uuid;
import AutoTrading.uuid.UuidRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UuidRepository uuidRepository;
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


        QueryAndJwtDto queryAndJwtDto = getJwtToken(params);

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(ORDER_URL);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", queryAndJwtDto.getJwtToken());
            request.setEntity(new StringEntity(gson.toJson(params)));

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String result = EntityUtils.toString(entity, "UTF-8");
            JsonObject obj = gson.fromJson(result, JsonObject.class);

            Uuid uuid = Uuid.builder()
                    .uuid(obj.get("uuid").getAsString())
                    .id("id")
                    .build();

            uuidRepository.save(uuid);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public BalanceDto getPossibleOrder() throws NoSuchAlgorithmException, UnsupportedEncodingException {

        HashMap<String, String> params = new HashMap<>();
        params.put("market", "KRW-BTC");

        QueryAndJwtDto queryAndJwtDto = getJwtToken(params);

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(ORDER_URL + "/chance?" + queryAndJwtDto.getQuery());
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", queryAndJwtDto.getJwtToken());

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String result = EntityUtils.toString(entity, "UTF-8");

            BalanceDto balanceDto = viewAccountBalance(result);

            log.info("판매가능수량: {}, 구매가능금액: {}", balanceDto.getAskBalance(), balanceDto.getBidBalance());

            return balanceDto;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cancelOrder() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        HashMap<String, String> params = getUuidParams();

        QueryAndJwtDto queryAndJwtDto = getJwtToken(params);

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpDelete request = new HttpDelete(ORDER_URL + "?" + queryAndJwtDto.getQuery());
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", queryAndJwtDto.getJwtToken());

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            log.info(EntityUtils.toString(entity, "UTF-8"));

            uuidRepository.deleteAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public OrderStateDto checkOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        HashMap<String, String> params = getUuidParams();
        QueryAndJwtDto queryAndJwtDto = getJwtToken(params);

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(ORDER_URL + "?" + queryAndJwtDto.getQuery());
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", queryAndJwtDto.getJwtToken());

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String result = EntityUtils.toString(entity, "UTF-8");

            JsonArray objArr = gson.fromJson(result, JsonArray.class);
            if(!objArr.isEmpty()) {
                JsonElement jsonElement = objArr.get(0);

                JsonObject obj = jsonElement.getAsJsonObject();

                OrderStateDto orderStateDto = OrderStateDto.builder()
                        .side(obj.get("side").getAsString())
                        .market(obj.get("market").getAsString())
                        .price(obj.get("price").getAsString())
                        .state(obj.get("state").getAsString()).build();

                if (!orderStateDto.getState().equals("done")) {
                    cancelOrder();
                }
                return orderStateDto;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BalanceDto viewAccountBalance(String result) {

        JsonObject obj = gson.fromJson(result, JsonObject.class);

        PossibleOrder possibleOrder = PossibleOrder.builder()
                .askAccount(obj.get("ask_account").getAsJsonObject())
                .bidAccount(obj.get("bid_account").getAsJsonObject())
                .build();

        AccountBalance accountBalance = AccountBalance.builder()
                .askBalance(possibleOrder.getAskAccount().get("balance").getAsString())
                .bidBalance(possibleOrder.getBidAccount().get("balance").getAsString())
                .build();

        String askBalance = accountBalance.getAskBalance();
        String bidBalance = accountBalance.getBidBalance();

        BalanceDto balanceDto = new BalanceDto(askBalance, bidBalance);

        return balanceDto;
    }

    @Async
    public void autoTrade(int minute){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                double rsi = candleService.viewMinuteCandleRSI(minute);

                if (rsi <= 20) {
                    try {
                        BalanceDto balanceDto = getPossibleOrder();
                        if(Integer.parseInt(balanceDto.getBidBalance()) >= 5000) {
                            order("KRW-BTC", "BID", "", balanceDto.getBidBalance(), "price");
                        }
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    orderStateDoneCheck30sec();
                }

                if (rsi >= 80) {
                    try {
                        BalanceDto balanceDto = getPossibleOrder();
                        if(Integer.parseInt(balanceDto.getAskBalance()) > 0) {
                            order("KRW-BTC", "ASK", balanceDto.getAskBalance(), "", "market");
                        }
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    orderStateDoneCheck30sec();
                }
            }
        };
        timer.schedule(timerTask, 1000, 10000);
    }

    private QueryAndJwtDto getJwtToken(HashMap<String, String> params) throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
        QueryAndJwtDto queryAndJwtDto = new QueryAndJwtDto(queryString, authenticationToken);

        return queryAndJwtDto;
    }

    private HashMap<String, String> getUuidParams() {
        HashMap<String, String> params = new HashMap<>();
        Uuid findUuid = uuidRepository.findById("id").orElseThrow(
                () -> new BusinessException(ExceptionCode.NOT_EXIST_UUID));

        String uuid = findUuid.getUuid();

        params.put("uuid", uuid);
        return params;
    }

    @Async
    public void orderStateDoneCheck30sec() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                if(count++ < 1) {
                    try {
                        checkOrder();
                    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
                else timer.cancel();
            }
        };
        timer.schedule(timerTask, 30000);
    }
}
