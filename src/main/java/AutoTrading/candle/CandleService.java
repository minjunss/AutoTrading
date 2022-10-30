package AutoTrading.candle;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CandleService {

    private final CandleRepository candleRepository;
    private final Gson gson;


    public double viewMinuteCandleRSI(int unit) {

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://api.upbit.com/v1/candles/minutes/" + unit + "?market=KRW-BTC&count=72");
            request.addHeader("accept", "application/json");

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");


            saveCandle(result);

            double RSI = getRSI();

            return RSI;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private double getRSI() {
        Iterable<Candle> candles = candleRepository.findAll();
        List<Candle> candleList = new ArrayList<>();


        for (Candle candle : candles) {
            candleList.add(candle);
        }
        candleList.sort(new Comparator<Candle>() {
            @Override
            public int compare(Candle o1, Candle o2) {
                String o1Time = o1.getCandleDateTimeKst().replace("-", "");
                o1Time = o1Time.replace("T", "");
                o1Time = o1Time.replace(":", "");
                String o2Time = o2.getCandleDateTimeKst().replace("-", "");
                o2Time = o2Time.replace("T", "");
                o2Time = o2Time.replace(":", "");
                if (Long.parseLong(o1Time) < Long.parseLong(o2Time)) return -1;
                else if (Long.parseLong(o1Time) > Long.parseLong(o2Time)) return 1;
                return 0;
            }
        });

        double AU = 0;
        double AD = 0;
        int temp = 0;

        List<Integer> auList = new ArrayList<>();
        List<Integer> adList = new ArrayList<>();

        int i=1;
        for (Candle candle : candleList) {
            int currentPrice = candle.getTradePrice();
            if (i >= 2) {
                if(currentPrice >= temp) auList.add(currentPrice - temp);
                else adList.add(temp - currentPrice);
            }
            temp = currentPrice;
            i++;
        }

        IntSummaryStatistics auStats = auList.stream()
                .mapToInt(Integer::intValue)
                .summaryStatistics();
        IntSummaryStatistics adStats = adList.stream()
                .mapToInt(Integer::intValue)
                .summaryStatistics();
        AU = auStats.getAverage();
        AD = adStats.getAverage();
        double RS = AU / AD;

        double RSI = RS / (1 + RS) * 100;

        candleRepository.deleteAll();

        log.info("RSI: {}", RSI);

        return RSI;
    }

    private void saveCandle(String result) {
        JsonArray obj = gson.fromJson(result, JsonArray.class);


        for (JsonElement jsonElement : obj) {
            JsonObject temp = jsonElement.getAsJsonObject();

            Candle candle = Candle.builder()
                    .market(temp.get("market").getAsString())
                    .tradePrice(temp.get("trade_price").getAsInt())
                    .candleDateTimeKst(temp.get("candle_date_time_kst").getAsString())
                    .build();

            candleRepository.save(candle);
        }
    }

}
