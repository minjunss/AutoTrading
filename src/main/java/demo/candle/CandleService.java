package com.example.UbitAutoTrading.candle;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class CandleService {

    private final CandleRepository candleRepository;
    private final Gson gson;


    public String viewMinuteCandle(int unit) {

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://api.upbit.com/v1/candles/minutes/" + unit + "?market=KRW-BTC&count=1");
            request.addHeader("accept", "application/json");

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");

            log.info(result);



//            JsonParser parser = new JsonParser();
            JsonArray obj = gson.fromJson(result, JsonArray.class);
//            JsonObject obj = parser.parse(result).getAsJsonObject();

//            JsonArray arr = obj.get("market").getAsJsonObject()
//                               .get("trade_price").getAsJsonArray();

            for (JsonElement jsonElement : obj) {
                JsonObject temp = jsonElement.getAsJsonObject();

                candleRepository.save(Candle.builder()
                        .market(temp.get("market").getAsString())
                        .tradePrice(temp.get("trade_price").getAsDouble())
                        .build());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "ok";
    }
}
